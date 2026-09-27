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
  val mem_fwd_header_width_lp: Int = 64,
  val bedrock_fill_width_p: Int = 64,
  val mem_rev_header_width_lp: Int = 64
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

  val reqHeader = Reg(UInt(bedrockHeaderWidth.W))
  val reqData   = Reg(UInt(bedrockDataWidth.W))

  val msgType = reqHeader(3, 0)
  val subop   = reqHeader(7, 4)

  val reqAddr =
    reqHeader(8 + addrWidth - 1, 8)

  val reqSize =
    reqHeader(8 + addrWidth + 2, 8 + addrWidth)

  val isRead     = msgType === 0.U
  val isWrite    = msgType === 1.U
  val isAmo      = msgType === 2.U
  val isPrefetch = msgType === 8.U

  val supportedReq =
    isRead || isWrite || isPrefetch

  val fwdReady =
    (state === s_idle) && supportedReq

  val fwdFire =
    fwdValid && fwdReady

  when (fwdFire) {
    reqHeader := fwdHeader
    reqData   := fwdData
    state     := s_tl_a
  }


  val reqBytes = Wire(UInt(8.W))
  reqBytes := 1.U

  for (i <- 0 until 8) {
    when (reqSize === i.U) {
      reqBytes := (1 << i).U
    }
  }

  val laneBits = log2Ceil(bedrockBytes)

  val byteLane =
    reqAddr(laneBits - 1, 0)


  when (state =/= s_idle) {
    assert(
      byteLane + reqBytes <= bedrockBytes.U,
      "BedRock transfer crosses a TileLink beat"
    )
  }


  val maskVec = Wire(Vec(bedrockBytes, Bool()))

  for (i <- 0 until bedrockBytes) {
    maskVec(i) :=
  (i.U >= byteLane) &&
  (i.U < byteLane + reqBytes)
  }

  val tlMask = maskVec.asUInt


  val shiftBits =
    Cat(byteLane, 0.U(3.W))

  val writeData =
    (reqData << shiftBits)(bedrockDataWidth - 1, 0)

  val tlGet =
    edge.Get(
      0.U,
      reqAddr,
      reqSize
    )._2

  val tlPut =
    edge.Put(
      0.U,
      reqAddr,
      reqSize,
      writeData,
      tlMask
    )._2

  tl.a.valid := state === s_tl_a

  tl.a.bits :=
    Mux(isWrite, tlPut, tlGet)

  when (tl.a.fire) {
    state := s_tl_d
  }

  tl.d.ready := state === s_tl_d

  val revHeaderReg = Reg(UInt(bedrockHeaderWidth.W))
  val revDataReg   = Reg(UInt(bedrockDataWidth.W))

  when (tl.d.fire) {

    revHeaderReg := reqHeader

    when (isRead) {
      revDataReg :=
        (tl.d.bits.data >> shiftBits)(
          bedrockDataWidth - 1, 0
        )
    } .elsewhen (isWrite) {
      revDataReg := 0.U
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


  val revValid =
    state === s_rev

  when (revValid && revReady) {
    state := s_idle
  }

  tl.b.valid := false.B
  tl.c.ready := true.B
  tl.e.ready := true.B

  (
    fwdReady,
    revHeaderReg,
    revDataReg,
    revValid
  )
}
  val core = Module(new BlackParrotBlackbox(
    cfg_bus_width_lp = outer.blackParrotParams.core.cfg_bus_width_lp,
    mem_fwd_header_width_lp = outer.blackParrotParams.core.mem_fwd_header_width_lp,
    bedrock_fill_width_p = outer.blackParrotParams.core.bedrock_fill_width_p,
    mem_rev_header_width_lp = outer.blackParrotParams.core.mem_rev_header_width_lp
  ))

  //connect signals
  core.io.clk_i := clock
  core.io.reset_i := reset.asBool

  outer.connectBlackParrotInterrupts(core.io.debug_irq_i, core.io.software_irq_i, core.io.timer_irq_i, core.io.s_external_irq_i)
  // core.io.irq_nm_i := 0.U //recoverable nmi, tying off
  // core.io.irq_fast_i := 0.U //local interrupts, tying off

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
    addrWidth = 32
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
    addrWidth = 32
  )

  core.io.mem_fwd_ready_and_i := Cat(dmemFwdReady, imemFwdReady)
  core.io.mem_rev_header_i := Cat(dmemRevHeader, imemRevHeader)
  core.io.mem_rev_data_i := Cat(dmemRevData, imemRevData)
  core.io.mem_rev_v_i := Cat(dmemRevValid, imemRevValid)
}

