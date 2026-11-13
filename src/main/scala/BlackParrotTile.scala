package bp

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, StringParam}

import scala.collection.mutable.{ListBuffer}

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci._
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem.{RocketCrossingParams}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util._
import freechips.rocketchip.tile._
import freechips.rocketchip.amba.axi4._

case class BlackParrotParams(
  val cfg_bus_width_lp: Int = 64,
  val mem_fwd_header_width_lp: Int = 65,
  val bedrock_fill_width_p: Int = 64,
  val mem_rev_header_width_lp: Int = 65
) extends CoreParams {
  val xLen = 32
  val pmpGranularity: Int = 0
  val bootFreqHz: BigInt = BigInt(1700000000)
  val pgLevels = 2
  val useVM: Boolean = false
  val useHypervisor: Boolean = false
  val useUser: Boolean = true
  val useSupervisor: Boolean = false
  val useDebug: Boolean = true
  val useAtomics: Boolean = false
  val useAtomicsOnlyForIO: Boolean = false
  val useCompressed: Boolean = false
  override val useVector: Boolean = false
  val useSCIE: Boolean = false
  val useRVE: Boolean = true
  val mulDiv: Option[MulDivParams] = Some(MulDivParams()) // copied from Rocket
  val fpu: Option[FPUParams] = None //floating point not supported
  val fetchWidth: Int = 1
  val decodeWidth: Int = 1
  val retireWidth: Int = 2
  val instBits: Int = if (useCompressed) 16 else 32
  val nLocalInterrupts: Int = 15
  val nPMPs: Int = 0
  val nBreakpoints: Int = 0
  val useBPWatch: Boolean = false
  val nPerfCounters: Int = 29
  val haveBasicCounters: Boolean = true
  val haveFSDirty: Boolean = false
  val misaWritable: Boolean = false
  val haveCFlush: Boolean = false
  val nL2TLBEntries: Int = 0
  val mtvecInit: Option[BigInt] = Some(BigInt(0))
  val mtvecWritable: Boolean = true
  val nL2TLBWays: Int = 1
  val lrscCycles: Int = 80
  val mcontextWidth: Int = 0
  val scontextWidth: Int = 0
  val useNMI: Boolean = true
  val nPTECacheEntries: Int = 0
  val traceHasWdata: Boolean = false
  val useConditionalZero: Boolean = false
  val useZba: Boolean = false
  val useZbb: Boolean = false
  val useZbs: Boolean = false
}

case class BlackParrotTileAttachParams(
  tileParams: BlackParrotTileParams,
  crossingParams: RocketCrossingParams
) extends CanAttachTile {
  type TileType = BlackParrotTile
  val lookup = PriorityMuxHartIdFromSeq(Seq(tileParams))
}

case class BlackParrotTileParams(
  name: Option[String] = Some("black_parrot_tile"),
  tileId: Int = 0,
  val core: BlackParrotParams = BlackParrotParams()
) extends InstantiableTileParams[BlackParrotTile]
{
  val beuAddr: Option[BigInt] = None
  val blockerCtrlAddr: Option[BigInt] = None
  val btb: Option[BTBParams] = None
  val boundaryBuffers: Boolean = false
  val dcache: Option[DCacheParams] = None //no dcache
  val icache: Option[ICacheParams] = None //optional icache, currently in draft so turning option off
  val clockSinkParams: ClockSinkParameters = ClockSinkParameters()
  def instantiate(crossing: HierarchicalElementCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters): BlackParrotTile = {
    new BlackParrotTile(this, crossing, lookup)
  }
  val baseName = name.getOrElse("black_parrot_tile")
  val uniqueName = s"${baseName}_$tileId"
} 

class BlackParrotTile private(
  val blackParrotParams: BlackParrotTileParams,
  crossing: ClockCrossingType,
  lookup: LookupByHartIdImpl,
  q: Parameters)
  extends BaseTile(blackParrotParams, crossing, lookup, q)
  with SinksExternalInterrupts
  with SourcesExternalNotifications
{

  // Private constructor ensures altered LazyModule.p is used implicitly
  def this(params: BlackParrotTileParams, crossing: HierarchicalElementCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters) =
    this(params, crossing.crossingType, lookup, p)

  // Require TileLink nodes
  val intOutwardNode = None
  val slaveNode = TLIdentityNode()
  DisableMonitors { implicit p => tlSlaveXbar.node :*= slaveNode }

    // Dummy manager to satisfy the crossbar's requirement for at least one manager.
  // Nothing ever routes here because the tile's slave port has no address range
  // attached in the system bus, but the TLXbar still requires at least one manager.
  val dummyManager = TLManagerNode(Seq(TLManagerPortParameters(
    managers = Seq(TLManagerParameters(
      address            = Seq(AddressSet(0x40000000L, 0xf)),   // 16 bytes at address 0x40000000L
      supportsGet        = TransferSizes(1, 8),
      supportsPutFull    = TransferSizes(1, 8),
      supportsPutPartial = TransferSizes(1, 8)
    )),
    beatBytes = 4
  )))
  dummyManager := tlSlaveXbar.node   // outward connection

  // Implementation class (See below)
  override lazy val module = new BlackParrotTileModuleImp(this)

  val dmemPortName = "black-parrot-dmem-port"
  val imemPortName = "black-parrot-imem-port"



     val beatBytes = 4   // matches SystemBusKey.beatBytes in WithNBlackParrotCores

  val dmemNode = TLClientNode(
    Seq(TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name               = dmemPortName,
        sourceId           = IdRange(0, 1),
        supportsProbe      = TransferSizes(1, beatBytes),
        supportsArithmetic = TransferSizes(1, beatBytes),
        supportsLogical    = TransferSizes(1, beatBytes),
        supportsGet        = TransferSizes(1, beatBytes),
        supportsPutFull    = TransferSizes(1, beatBytes),
        supportsPutPartial = TransferSizes(1, beatBytes),
        supportsHint       = TransferSizes(1, beatBytes)
      ))
    ))
  )

  val imemNode = TLClientNode(
    Seq(TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name               = imemPortName,
        sourceId           = IdRange(0, 1),
        supportsProbe      = TransferSizes(1, beatBytes),
        supportsArithmetic = TransferSizes(1, beatBytes), 
        supportsLogical    = TransferSizes(1, beatBytes),
        supportsGet        = TransferSizes(1, beatBytes),
        supportsPutFull    = TransferSizes(1, beatBytes),
        supportsPutPartial = TransferSizes(1, beatBytes),
        supportsHint       = TransferSizes(1, beatBytes)
      ))
    ))
  )

  val fillBytes =
  blackParrotParams.core.bedrock_fill_width_p / 8

  require(fillBytes >= 8, "BedRock data channel must be at least 64 bits")
  require((fillBytes & (fillBytes - 1)) == 0,
  "BedRock fill width must be a power-of-two number of bytes")

    tlMasterXbar.node := TLBuffer() := TLWidthWidget(fillBytes) := dmemNode
  tlMasterXbar.node := TLBuffer() := TLWidthWidget(fillBytes) := imemNode

  val masterNode = visibilityNode

  tlOtherMastersNode := TLBuffer() := tlMasterXbar.node
  masterNode        :=* tlOtherMastersNode

  // Required entry of CPU device in the device tree for interrupt purpose
  val cpuDevice: SimpleDevice = new SimpleDevice("cpu", Seq("my-organization,my-cpu", "riscv")) {
    override def parent = Some(ResourceAnchors.cpus)
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++
                        cpuProperties ++
                        nextLevelCacheProperty ++
                        tileProperties)
    }
  }

  ResourceBinding {
    Resource(cpuDevice, "reg").bind(ResourceAddress(tileId))
  }

  def connectBlackParrotInterrupts(debug: Bool, msip: Bool, mtip: Bool, meip: Bool) {
    val (interrupts, _) = intSinkNode.in(0)
    debug := interrupts(0)
    msip := interrupts(1)
    mtip := interrupts(2)
    meip := interrupts(3)
  }
}

class BlackParrotTileModuleImp(outer: BlackParrotTile) extends BaseTileModuleImp(outer){
/*
  def bridgeBedrockLane(
  tl: TLBundle,
  edge: TLEdgeOut,
  fwdHeader: UInt,
  fwdData: UInt,
  fwdValid: Bool,
  revReady: Bool,
  addrWidth: Int
): (Bool, UInt, UInt, Bool) = {

  val bedrockHeaderWidth = fwdHeader.getWidth
  val bedrockDataWidth   = fwdData.getWidth
  val bedrockBytes       = bedrockDataWidth / 8

  require(bedrockDataWidth % 8 == 0)
  require(tl.a.bits.data.getWidth == bedrockDataWidth,
    "Bridge TL width must equal BedRock fill width; use TLWidthWidget downstream")

  val s_idle :: s_tl_a :: s_tl_d :: s_rev :: Nil = Enum(4)
  val state = RegInit(s_idle)

  val reqHeader = RegInit(0.U(bedrockHeaderWidth.W))
  val reqData   = RegInit(0.U(bedrockDataWidth.W))

  // ---- Forward header field extraction (combinational slices of reqHeader) ----
  val reqMsgType = reqHeader(3, 0)
  val reqSubop   = reqHeader(7, 4)
  val reqAddr    = reqHeader(8 + addrWidth - 1, 8)
  val reqSize    = reqHeader(8 + addrWidth + 2, 8 + addrWidth)

  val reqSpeculative = reqHeader(8 + addrWidth + 3)
  val reqUncached    = reqHeader(8 + addrWidth + 4)
  val reqPrefetch    = reqHeader(8 + addrWidth + 5)
  val reqSrcDid      = reqHeader(8 + addrWidth + 6 + 2, 8 + addrWidth + 6)

  val isRead     = reqMsgType === 0.U
  val isWrite    = reqMsgType === 1.U
  val isPrefetch = reqMsgType === 8.U

  val fwdMsgType    = fwdHeader(3, 0)
  val supportedReq  = (fwdMsgType === 0.U) || (fwdMsgType === 1.U) || (fwdMsgType === 8.U)

  val fwdReady      = (state === s_idle) && supportedReq
  val fwdFire       = fwdValid && fwdReady
  printf(p"fwdValid = ${fwdValid}\n");
  printf(p"fwdReady = ${fwdReady}\n");
  printf(p"s_idle = ${s_idle}\n");
  printf(p"state = ${state}\n");

  when (fwdFire) {
    printf(p"fwd reqAddr = 0x${Hexadecimal(fwdHeader(8 + addrWidth - 1, 8))}\n")
    printf(p"fwd reqSize = 0x${Hexadecimal(fwdHeader(8 + addrWidth + 2, 8 + addrWidth))}\n")
    reqHeader := fwdHeader
    reqData   := fwdData
    state     := s_tl_a
  }

  // ---- Byte lane / mask computation ----
  val reqBytes = Wire(UInt(8.W))
  reqBytes := 1.U
  for (i <- 0 until 8) {
    when (reqSize === i.U) { reqBytes := (1 << i).U }
  }

  val laneBits = log2Ceil(bedrockBytes)
  val byteLane = reqAddr(laneBits - 1, 0)

  when (state =/= s_idle) {
    assert(byteLane + reqBytes <= bedrockBytes.U,
      "BedRock transfer crosses a TileLink beat")
  }

  val maskVec = Wire(Vec(bedrockBytes, Bool()))
  for (i <- 0 until bedrockBytes) {
    maskVec(i) := (i.U >= byteLane) && (i.U < byteLane + reqBytes)
  }
  val tlMask = maskVec.asUInt

  val shiftBits = Cat(byteLane, 0.U(3.W))
  val writeData = (reqData << shiftBits)(bedrockDataWidth - 1, 0)

  val tlGet = edge.Get(0.U, reqAddr, reqSize)._2
  val tlPut = edge.Put(0.U, reqAddr, reqSize, writeData, tlMask)._2
  printf("reqAddr = %d\n", reqAddr);
  printf("reqSize = %d\n", reqSize);
  // printf(p"addrWidth = ${addrWidth.U}\n");
  // printf(p"header width = ${bedrockHeaderWidth.U}\n");

  tl.a.valid := state === s_tl_a
  tl.a.bits  := Mux(isWrite, tlPut, tlGet)
  when (tl.a.fire) { state := s_tl_d }

  tl.d.ready := state === s_tl_d

  val revHeaderReg = RegInit(0.U(bedrockHeaderWidth.W))
  val revDataReg   = RegInit(0.U(bedrockDataWidth.W))

  when (tl.d.fire) {

    revHeaderReg := Cat(
      0.U((bedrockHeaderWidth - 11).W),   // payload = 0
      reqSrcDid,                          // did      [10:8]
      reqHeader(7, 4),                    // subop    [7:4]
      reqHeader(3, 0)                     // msg_type [3:0]
    )

    when (isRead) {
      revDataReg := (tl.d.bits.data >> shiftBits)(bedrockDataWidth - 1, 0)
    } .otherwise {
      revDataReg := 0.U
    }

    when (isPrefetch) {
      state := s_idle
    } .otherwise {
      state := s_rev
    }

    assert(!tl.d.bits.denied,
      "TileLink denied response cannot be represented by current BedRock adapter")
    assert(!tl.d.bits.corrupt,
      "TileLink corrupt response cannot be represented by current BedRock adapter")
  }

  val revValid = state === s_rev
  when (revValid && revReady) { state := s_idle }

  tl.b.valid := false.B
  tl.c.ready := true.B
  tl.e.ready := true.B

  (fwdReady, revHeaderReg, revDataReg, revValid)
}
  

  val bpFwdHdrWidth  = outer.blackParrotParams.core.mem_fwd_header_width_lp
  val bpRevHdrWidth  = outer.blackParrotParams.core.mem_rev_header_width_lp
  val bpFillWidth    = outer.blackParrotParams.core.bedrock_fill_width_p


  //  I-cache
  val imemFwdHeader = core.io.mem_fwd_header_o(
    bpFwdHdrWidth - 1, 0
  )

  val imemFwdData = core.io.mem_fwd_data_o(
    bpFillWidth - 1, 0
  )

  val imemFwdValid =
    core.io.mem_fwd_v_o(0)

  // D-cache
  val dmemFwdHeader = core.io.mem_fwd_header_o(
    2 * bpFwdHdrWidth - 1,
    bpFwdHdrWidth
  )

  val dmemFwdData = core.io.mem_fwd_data_o(
    2 * bpFillWidth - 1,
    bpFillWidth
  )

  val dmemFwdValid =
    core.io.mem_fwd_v_o(1)

  val (dmem, dmemEdge) = outer.dmemNode.out(0)
  val (imem, imemEdge) = outer.imemNode.out(0)

  val (imemFwdReady,
     imemRevHeader,
     imemRevData,
     imemRevValid) =
  bridgeBedrockLane(
    imem,
    imemEdge,
    imemFwdHeader,
    imemFwdData,
    imemFwdValid,
    core.io.mem_rev_ready_and_o(0),
    addrWidth = 40
  )

  val (dmemFwdReady,
     dmemRevHeader,
     dmemRevData,
     dmemRevValid) =
  bridgeBedrockLane(
    dmem,
    dmemEdge,
    dmemFwdHeader,
    dmemFwdData,
    dmemFwdValid,
    core.io.mem_rev_ready_and_o(1),
    addrWidth = 40
  )

  */


  val core = Module(new BlackParrotBlackbox(
    cfg_bus_width_lp = outer.blackParrotParams.core.cfg_bus_width_lp,
    mem_fwd_header_width_lp = outer.blackParrotParams.core.mem_fwd_header_width_lp,
    bedrock_fill_width_p = outer.blackParrotParams.core.bedrock_fill_width_p,
    mem_rev_header_width_lp = outer.blackParrotParams.core.mem_rev_header_width_lp
  ))

  //connect signals
  core.io.clk_i := clock
  core.io.reset_i := reset.asBool

  outer.connectBlackParrotInterrupts(core.io.debug_irq_i, core.io.software_irq_i, core.io.timer_irq_i, core.io.m_external_irq_i)
  core.io.s_external_irq_i := false.B
  core.io.cfg_bus_i := 0.U
  // core.io.irq_nm_i := 0.U //recoverable nmi, tying off
  // core.io.irq_fast_i := 0.U //local interrupts, tying off

  val (dmem, dmem_edge) = outer.dmemNode.out(0)


  val dmem_addr = Reg(UInt(40.W))
  val dmem_data = Reg(UInt(128.W))
  val imem_data = Reg(UInt(128.W))
  val reqSize_dmem = Reg(UInt(3.W))
  val reqAddr_dmem = Reg(UInt(32.W))
  val reqAddr_imem = Reg(UInt(32.W))
  val reqSize_imem = Reg(UInt(3.W))
  val r_size = Reg(UInt(3.W))
  val w_size = Reg(UInt(3.W))

  val s_ready :: s_active :: s_inflight :: Nil = Enum(3)
  val dmem_state = RegInit(s_ready)

  when (dmem_state === s_ready && core.io.mem_rev_ready_and_o(1)) {
    dmem_state := s_active
    dmem_addr := core.io.mem_fwd_header_o(47 + 65, 8 + 65)
    dmem_data := core.io.mem_fwd_data_o(63 + 64, 0 + 64)
    // byte_en := core.io.data_be_o
    w_size := core.io.mem_fwd_header_o(51 + 65, 48 + 65)

    if(core.io.mem_fwd_header_o(3, 0) == 0.U(4.W)) { // Read
      
     
    }
    else if(core.io.mem_fwd_header_o(3, 0) == 1.U(4.W)) { // Write
      
    }
  }
  when (dmem_state === s_active && dmem.a.fire) {
    dmem_state := s_inflight
  }
  when (dmem_state === s_inflight && dmem.d.fire) {
    dmem_state := s_ready
  }

  dmem.a.valid := dmem_state === s_active

  dmem.b.valid := false.B
  dmem.c.ready := true.B
  dmem.e.ready := true.B

  //val dmem_put = dmem_edge.Put(0.U, dmem_addr, w_size, dmem_data)._2

 

    

  //IMEM
  val (imem, imem_edge) = outer.imemNode.out(0)
  val imem_state = RegInit(s_ready)

  val imem_addr = Reg(UInt(32.W))

  when (imem_state === s_ready && core.io.mem_rev_ready_and_o(0)) {
    imem_state := s_active
    imem_addr := core.io.mem_fwd_header_o(47, 8)
    imem_data := core.io.mem_fwd_data_o(63, 0)
    r_size := core.io.mem_fwd_header_o(51, 48)
    // printf("reqAddr_imem = %d\n", reqAddr_imem);
    if(core.io.mem_fwd_header_o(3, 0) == 0.U(4.W)) { // Read
      // val tlGet_imem = imem_edge.Get(0.U, imem_addr, r_size)._2
    }
    else if(core.io.mem_fwd_header_o(3, 0) == 1.U(4.W)) { // Write
      // val tlPut_imem = imem_edge.Put(0.U, imem_addr, r_size, imem_data)._2
      // imem.a.bits := tlPut_imem
    }
  }
  when (imem_state === s_active && imem.a.fire) {
    imem_state := s_inflight
  }
  when (imem_state === s_inflight && imem.d.fire) {
    imem_state := s_ready
  }

  imem.a.valid := imem_state === s_active
  // core.io.instr_gnt_i := imem_state === s_ready
  imem.d.ready := true.B
  // core.io.instr_rvalid_i := imem.d.valid

  // val imem_put = imem_edge.Put(0.U, imem_addr, r_size, imem_data)._2

  // core.io.instr_rdata_i := imem.d.bits.data
  // core.io.instr_err_i := imem.d.bits.corrupt | imem.d.bits.denied

  //unused
  imem.b.valid := false.B
  imem.c.ready := true.B
  imem.e.ready := true.B
  
  reqSize_imem := imem.a.bits.size
  reqSize_dmem := dmem.a.bits.size
  reqAddr_imem := imem.a.bits.address
  reqAddr_dmem := dmem.a.bits.address

  val imemHeader = 0.U(3.W) ## 0.U(3.W) ## 0.U(2.W) ## 1.U(3.W) ## 0.U(1.W) ## 0.U(1.W) ## 0.U(1.W) ## reqSize_dmem ## 0.U(8.W) ## reqAddr_dmem ## 0.U(4.W) ## 0.U(4.W)
  val dmemHeader = 0.U(3.W) ## 0.U(3.W) ## 0.U(2.W) ## 1.U(3.W) ## 0.U(1.W) ## 0.U(1.W) ## 0.U(1.W) ## reqSize_imem ## 0.U(8.W) ## reqAddr_imem ## 0.U(4.W) ## 0.U(4.W)



  core.io.mem_rev_header_i := dmemHeader ## imemHeader


  core.io.mem_fwd_ready_and_i := Cat(true.B, true.B)
  // core.io.mem_rev_header_i := Cat(dmemRevHeader, imemRevHeader)
  core.io.mem_rev_data_i := Cat(dmem.d.bits.data, imem.d.bits.data)
  core.io.mem_rev_v_i := Cat(dmem.d.valid, imem.d.valid)

  val tlPut_dmem = dmem_edge.Put(0.U, dmem_addr, r_size, dmem_data)._2
  val tlGet_dmem = dmem_edge.Get(0.U, dmem_addr, r_size)._2
  val tlGet_imem = imem_edge.Get(0.U, imem_addr, r_size)._2
  val tlPut_imem = imem_edge.Put(0.U, imem_addr, r_size, imem_data)._2


  dmem.a.bits := Mux(core.io.mem_fwd_header_o(0), tlPut_dmem, tlGet_dmem)    
  imem.a.bits := Mux(core.io.mem_fwd_header_o(0), tlPut_imem, tlGet_imem)  

  // dmem.a.bits := dmem_put
  // imem.a.bits := imem_put
}

