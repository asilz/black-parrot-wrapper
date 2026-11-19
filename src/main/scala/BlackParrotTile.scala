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

//******************************************************************************
// Copyright (c) 2019 - 2019, The Regents of the University of California (Regents).
// All Rights Reserved. See LICENSE and LICENSE.SiFive for license details.
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
// BlackParrot Tile Wrapper
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

case class BlackParrotCoreParams(
  xLen: Int = 64,
  bootFreqHz: BigInt = BigInt(1700000000),
  rasEntries: Int = 4,
  btbEntries: Int = 16,
  bhtEntries: Int = 16,
  pmpEntries: Int = 4,
  enableToFromHostCaching: Boolean = false,
) extends CoreParams {
  /* DO NOT CHANGE BELOW THIS */
  val useVM: Boolean = true
  val useHypervisor: Boolean = false
  val useUser: Boolean = true
  val useSupervisor: Boolean = false
  val useDebug: Boolean = true
  val useAtomics: Boolean = true
  val useAtomicsOnlyForIO: Boolean = false // copied from Rocket
  val useCompressed: Boolean = true
  override val useVector: Boolean = false
  val useSCIE: Boolean = false
  val useRVE: Boolean = false
  val mulDiv: Option[MulDivParams] = Some(MulDivParams()) // copied from Rocket
  val fpu: Option[FPUParams] = Some(FPUParams()) // copied fma latencies from Rocket
  val nLocalInterrupts: Int = 0
  val useNMI: Boolean = false
  val nPMPs: Int = 0 // TODO: Check
  val pmpGranularity: Int = 4 // copied from Rocket
  val nBreakpoints: Int = 0 // TODO: Check
  val useBPWatch: Boolean = false
  val mcontextWidth: Int = 0 // TODO: Check
  val scontextWidth: Int = 0 // TODO: Check
  val nPerfCounters: Int = 29
  val haveBasicCounters: Boolean = true
  val haveFSDirty: Boolean = false
  val misaWritable: Boolean = false
  val haveCFlush: Boolean = false
  val nL2TLBEntries: Int = 512 // copied from Rocket
  val nL2TLBWays: Int = 1
  val mtvecInit: Option[BigInt] = Some(BigInt(0)) // copied from Rocket
  val mtvecWritable: Boolean = true // copied from Rocket
  val instBits: Int = if (useCompressed) 16 else 32
  val lrscCycles: Int = 80 // copied from Rocket
  val decodeWidth: Int = 1 // TODO: Check
  val fetchWidth: Int = 1 // TODO: Check
  val retireWidth: Int = 2
  val nPTECacheEntries: Int = 8 // TODO: Check
  val traceHasWdata: Boolean = false
  val useConditionalZero: Boolean = false
  val useZba: Boolean = false
  val useZbb: Boolean = false
  val useZbs: Boolean = false
  val pgLevels = if (xLen == 64) 3 else 2
}

case class BlackParrotTileAttachParams(
  tileParams: BlackParrotTileParams,
  crossingParams: RocketCrossingParams
) extends CanAttachTile {
  type TileType = BlackParrotTile
  val lookup = PriorityMuxHartIdFromSeq(Seq(tileParams))
}

// TODO: BTBParams, DCacheParams, ICacheParams are incorrect in DTB... figure out defaults in BlackParrot and put in DTB
case class BlackParrotTileParams(
  name: Option[String] = Some("black_parrot_tile"),
  tileId: Int = 0,
  trace: Boolean = false,
  val core: BlackParrotCoreParams = BlackParrotCoreParams()
) extends InstantiableTileParams[BlackParrotTile]
{
  val beuAddr: Option[BigInt] = None
  val blockerCtrlAddr: Option[BigInt] = None
  val btb: Option[BTBParams] = Some(BTBParams())
  val boundaryBuffers: Boolean = false
  val dcache: Option[DCacheParams] = Some(DCacheParams())
  val icache: Option[ICacheParams] = Some(ICacheParams())
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
  /**
   * Setup parameters:
   * Private constructor ensures altered LazyModule.p is used implicitly
   */
  def this(params: BlackParrotTileParams, crossing: HierarchicalElementCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters) =
    this(params, crossing.crossingType, lookup, p)

  val intOutwardNode = None
  val slaveNode = TLIdentityNode()
  val masterNode = visibilityNode

  tlOtherMastersNode := tlMasterXbar.node
  masterNode :=* tlOtherMastersNode
  DisableMonitors { implicit p => tlSlaveXbar.node :*= slaveNode }

  val cpuDevice: SimpleDevice = new SimpleDevice("cpu", Seq("openhwgroup,black_parrot", "riscv")) {
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

 override def makeMasterBoundaryBuffers(crossing: ClockCrossingType)(implicit p: Parameters) = crossing match {
    case _: RationalCrossing =>
      if (!blackParrotParams.boundaryBuffers) TLBuffer(BufferParams.none)
      else TLBuffer(BufferParams.none, BufferParams.flow, BufferParams.none, BufferParams.flow, BufferParams(1))
    case _ => TLBuffer(BufferParams.none)
  }

  override def makeSlaveBoundaryBuffers(crossing: ClockCrossingType)(implicit p: Parameters) = crossing match {
    case _: RationalCrossing =>
      if (!blackParrotParams.boundaryBuffers) TLBuffer(BufferParams.none)
      else TLBuffer(BufferParams.flow, BufferParams.none, BufferParams.none, BufferParams.none, BufferParams.none)
    case _ => TLBuffer(BufferParams.none)
  }

  override lazy val module = new BlackParrotTileModuleImp(this)

  /**
   * Setup AXI4 memory interface.
   * THESE ARE CONSTANTS.
   */
  val portName = "black-parrot-mem-port-axi4"
  val idBits = 4
  val beatBytes = masterPortBeatBytes
  val bpAxiBytes = 16          // must equal BP's l2_fill_width_p / 8 (128 bits)
  val sourceBits = 1

  val memAXI4Node = AXI4MasterNode(
    Seq(AXI4MasterPortParameters(
      masters = Seq(AXI4MasterParameters(
        name = portName,
        id = IdRange(0, 1 << idBits))))))

  val memoryTap = TLIdentityNode()
  (tlMasterXbar.node
    := memoryTap
    := TLBuffer()
    := TLFIFOFixer(TLFIFOFixer.all) // fix FIFO ordering
    := TLWidthWidget(bpAxiBytes) // reduce size of TL
    := AXI4ToTL() // convert to TL
    := AXI4UserYanker(Some(2)) // remove user field on AXI interface. need but in reality user intf. not needed
    := AXI4Fragmenter() // deal with multi-beat xacts
    := memAXI4Node)

  def connectBlackParrotInterrupts(debug: Bool, msip: Bool, mtip: Bool, m_s_eip: UInt) {
    val (interrupts, _) = intSinkNode.in(0)
    debug := interrupts(0)
    msip := interrupts(1)
    mtip := interrupts(2)
    m_s_eip := Cat(interrupts(4), interrupts(3))
  }

    val ioAXI4Node = AXI4MasterNode(Seq(AXI4MasterPortParameters(
    masters = Seq(AXI4MasterParameters(name = "black-parrot-io-axil", id = IdRange(0, 1))))))
  (tlMasterXbar.node := TLBuffer() := TLFIFOFixer(TLFIFOFixer.all) := TLWidthWidget(4)
    := AXI4ToTL() := AXI4UserYanker(Some(2)) := AXI4Fragmenter() := ioAXI4Node)

     // Public accessor: intSinkNode is protected, so the module can't touch it directly.
  // Order from Chipyard's interrupt bundle: 0 = debug, 1 = msip, 2 = mtip, 3 = meip
  def bpMsip: Bool = {
    val (interrupts, _) = intSinkNode.in(0)
    interrupts(1)
  }
}

class BlackParrotTileModuleImp(outer: BlackParrotTile) extends BaseTileModuleImp(outer){
  val debugBaseAddr = BigInt(0x0) // CONSTANT: based on default debug module
  val debugSz = BigInt(0x1000) // CONSTANT: based on default debug module
  val tohostAddr = BigInt(0x80001000L) // CONSTANT: based on default sw (assume within extMem region)
  val fromhostAddr = BigInt(0x80001040L) // CONSTANT: based on default sw (assume within extMem region)

  // have the main memory, bootrom, debug regions be executable
  val bootromParams = p(BootROMLocated(InSubsystem)).head
  val executeRegionBases = Seq(p(ExtMem).get.master.base,      bootromParams.address, debugBaseAddr, BigInt(0x0), BigInt(0x0))
  val executeRegionSzs   = Seq(p(ExtMem).get.master.size, BigInt(bootromParams.size),       debugSz, BigInt(0x0), BigInt(0x0))
  val executeRegionCnt   = executeRegionBases.length

  // have the main memory be cached, but don't cache tohost/fromhost addresses
  // TODO: current cache subsystem can only support 1 cacheable region... so cache AFTER the tohost/fromhost addresses
  val wordOffset = 0x40
  val (cacheableRegionBases, cacheableRegionSzs) = if (outer.blackParrotParams.core.enableToFromHostCaching) {
    val bases = Seq(p(ExtMem).get.master.base, BigInt(0x0), BigInt(0x0), BigInt(0x0), BigInt(0x0))
    val sizes   = Seq(p(ExtMem).get.master.size, BigInt(0x0), BigInt(0x0), BigInt(0x0), BigInt(0x0))
    (bases, sizes)
  } else {
    val bases = Seq(                                                          fromhostAddr + 0x40,              p(ExtMem).get.master.base, BigInt(0x0), BigInt(0x0), BigInt(0x0))
    val sizes = Seq(p(ExtMem).get.master.size - (fromhostAddr + 0x40 - p(ExtMem).get.master.base), tohostAddr - p(ExtMem).get.master.base, BigInt(0x0), BigInt(0x0), BigInt(0x0))
    (bases, sizes)
  }
  val cacheableRegionCnt   = cacheableRegionBases.length

  // Add 2 to account for the extra clock and reset included with each
  // instruction in the original trace port implementation. These have since
  // been removed from TracedInstruction.
  val traceInstSz = (new freechips.rocketchip.rocket.TracedInstruction).getWidth + 2

  // connect the black parrot core
  val core = Module(new BlackParrotBlackbox(
    // traceport params
    // traceportEnabled = outer.blackParrotParams.trace,
    // traceportSz = (outer.blackParrotParams.core.retireWidth * traceInstSz),

    // general core params
    // xLen = outer.blackParrotParams.core.xLen,
    // rasEntries = outer.blackParrotParams.core.rasEntries,
    // btbEntries = outer.blackParrotParams.core.btbEntries,
    // bhtEntries = outer.blackParrotParams.core.bhtEntries,
    // exeRegCnt = executeRegionCnt,
    // exeRegBase = executeRegionBases,
    // exeRegSz = executeRegionSzs,
    // cacheRegCnt = cacheableRegionCnt,
    // cacheRegBase = cacheableRegionBases,
    // cacheRegSz = cacheableRegionSzs,
    // debugBase = debugBaseAddr,
    axi_core_clk_async_p = 0,
    lg_async_fifo_size_lp = 3,
    m_axil_addr_width_p = 32,
    m_axil_data_width_p = 32,
    m_axil_mask_width_lp = 4,
    s_axil_addr_width_p = 32,
    s_axil_data_width_p = 32,
    s_axil_mask_width_lp = 4,
    axi_addr_width_p = 48,   // not 64
axi_data_width_p = outer.bpAxiBytes * 8 ,  // keep 8‑byte beats → 64
axi_id_width_p   = outer.idBits     ,    // 4 is fine
axi_mask_width_lp = outer.bpAxiBytes    ,   // 8 for 8‑byte beats
paddr_width_p         = 48                  // <-- NEW, SAME AS ABOVE
    // pmpEntries = outer.blackParrotParams.core.pmpEntries
  ))

  core.io.core_clk_i := clock
  core.io.axi_clk_i := clock
  core.io.rt_clk_i := clock
  // Hold async reset high for 4 cycles after the synchronous reset drops
  core.io.async_reset_i := reset.asBool   // <‑‑ true async level reset
  // core.io.boot_addr_i := outer.resetVectorSinkNode.bundle
  // core.io.hart_id_i := outer.hartIdSinkNode.bundle

   outer.ioAXI4Node.out.foreach { case (io, _) =>
    io.aw.valid := core.io.m_axil_awvalid_o;  core.io.m_axil_awready_i := io.aw.ready
    io.aw.bits.id := 0.U; io.aw.bits.addr := core.io.m_axil_awaddr_o
    io.aw.bits.len := 0.U; io.aw.bits.size := 2.U; io.aw.bits.burst := AXI4Parameters.BURST_INCR
    io.aw.bits.lock := 0.U; io.aw.bits.cache := 0.U; io.aw.bits.prot := core.io.m_axil_awprot_o; io.aw.bits.qos := 0.U

    io.w.valid := core.io.m_axil_wvalid_o;    core.io.m_axil_wready_i := io.w.ready
    io.w.bits.data := core.io.m_axil_wdata_o; io.w.bits.strb := core.io.m_axil_wstrb_o; io.w.bits.last := true.B

    core.io.m_axil_bvalid_i := io.b.valid;    io.b.ready := core.io.m_axil_bready_o
    core.io.m_axil_bresp_i  := io.b.bits.resp

    io.ar.valid := core.io.m_axil_arvalid_o;  core.io.m_axil_arready_i := io.ar.ready
    io.ar.bits.id := 0.U; io.ar.bits.addr := core.io.m_axil_araddr_o
    io.ar.bits.len := 0.U; io.ar.bits.size := 2.U; io.ar.bits.burst := AXI4Parameters.BURST_INCR
    io.ar.bits.lock := 0.U; io.ar.bits.cache := 0.U; io.ar.bits.prot := core.io.m_axil_arprot_o; io.ar.bits.qos := 0.U

    core.io.m_axil_rvalid_i := io.r.valid;    io.r.ready := core.io.m_axil_rready_o
    core.io.m_axil_rdata_i  := io.r.bits.data; core.io.m_axil_rresp_i := io.r.bits.resp
  }

    // ---- s_axil: tile boots BP once the program has been loaded ----

    

   val msip = outer.bpMsip                   // order: debug, msip, mtip, meip

  val bootWrites = Seq(
    (BigInt(0x00200010L), BigInt(0x80000000L)), // cfg: npc    = DRAM base
    (BigInt(0x00200008L), BigInt(0))            // cfg: freeze = 0
  )
  val bootAddr = VecInit(bootWrites.map(_._1.U(32.W)))
  val bootData = VecInit(bootWrites.map(_._2.U(32.W)))

  val sIdle :: sReq :: sResp :: sDone :: Nil = Enum(4)
  val state  = RegInit(sIdle)
  val idx    = RegInit(0.U(log2Ceil(bootWrites.length).max(1).W))
  val awSent = RegInit(false.B)
  val wSent  = RegInit(false.B)

  core.io.s_axil_awaddr_i  := bootAddr(idx)
  core.io.s_axil_awprot_i  := 0.U
  core.io.s_axil_awvalid_i := state === sReq && !awSent
  core.io.s_axil_wdata_i   := bootData(idx)
  core.io.s_axil_wstrb_i   := "hF".U
  core.io.s_axil_wvalid_i  := state === sReq && !wSent
  core.io.s_axil_bready_i  := state === sResp
  core.io.s_axil_araddr_i  := 0.U
  core.io.s_axil_arprot_i  := 0.U
  core.io.s_axil_arvalid_i := false.B
  core.io.s_axil_rready_i  := true.B

  switch (state) {
    is (sIdle) { when (msip) { state := sReq } }
    is (sReq) {
      val aw = awSent || core.io.s_axil_awready_o
      val w  = wSent  || core.io.s_axil_wready_o
      awSent := aw; wSent := w
      when (aw && w) { state := sResp; awSent := false.B; wSent := false.B }
    }
    is (sResp) {
      when (core.io.s_axil_bvalid_o) {
        when (idx === (bootWrites.length - 1).U) { state := sDone }
          .otherwise { idx := idx + 1.U; state := sReq }
      }
    }
  }

  // outer.connectBlackParrotInterrupts(core.io.debug_req_i, core.io.ipi_i, core.io.time_irq_i, core.io.irq_i)

  if (outer.blackParrotParams.trace) {
    // unpack the trace io from a UInt into Vec(TracedInstructions)
    //outer.traceSourceNode.bundle <> core.io.trace_o.asTypeOf(outer.traceSourceNode.bundle)

  } else {
    outer.traceSourceNode.bundle := DontCare
    outer.traceSourceNode.bundle.insns map (t => t.valid := false.B)
  }

  // connect the axi interface
  outer.memAXI4Node.out foreach { case (out, edgeOut) =>
    core.io.m_axi_awready_i    := out.aw.ready
    out.aw.valid                   := core.io.m_axi_awvalid_o
    out.aw.bits.id                 := core.io.m_axi_awid_o
    out.aw.bits.addr               := core.io.m_axi_awaddr_o
    out.aw.bits.len                := core.io.m_axi_awlen_o
    out.aw.bits.size               := core.io.m_axi_awsize_o
    out.aw.bits.burst              := core.io.m_axi_awburst_o
    out.aw.bits.lock               := core.io.m_axi_awlock_o
    out.aw.bits.cache              := core.io.m_axi_awcache_o
    out.aw.bits.prot               := core.io.m_axi_awprot_o
    out.aw.bits.qos                := core.io.m_axi_awqos_o
    // unused signals
    // assert(core.io.axi_req_o_aw_bits_region === 0.U)
    // assert(core.io.axi_req_o_aw_bits_atop === 0.U)
    // assert(core.io.axi_req_o_aw_bits_user === 0.U)

    core.io.m_axi_wready_i     := out.w.ready
    out.w.valid                    := core.io.m_axi_wvalid_o 
    out.w.bits.data                := core.io.m_axi_wdata_o
    out.w.bits.strb                := core.io.m_axi_wstrb_o
    out.w.bits.last                := core.io.m_axi_wlast_o // check
    // unused signals
    // assert(core.io.axi_req_o_w_bits_user === 0.U)

    out.b.ready                    := core.io.m_axi_bready_o
    core.io.m_axi_bvalid_i     := out.b.valid
    core.io.m_axi_bid_i   := out.b.bits.id
    core.io.m_axi_bresp_i := out.b.bits.resp
    // core.io.axi_resp_i_b_bits_user := 0.U // unused

    core.io.m_axi_arready_i    := out.ar.ready
    out.ar.valid                   := core.io.m_axi_arvalid_o
    out.ar.bits.id                 := core.io.m_axi_arid_o
    out.ar.bits.addr               := core.io.m_axi_araddr_o
    out.ar.bits.len                := core.io.m_axi_arlen_o
    out.ar.bits.size               := core.io.m_axi_arsize_o
    out.ar.bits.burst              := core.io.m_axi_arburst_o
    out.ar.bits.lock               := core.io.m_axi_arlock_o
    out.ar.bits.cache              := core.io.m_axi_arcache_o
    out.ar.bits.prot               := core.io.m_axi_arprot_o
    out.ar.bits.qos                := core.io.m_axi_arqos_o
    // unused signals
    // assert(core.io.axi_req_o_ar_bits_region === 0.U)
    // assert(core.io.axi_req_o_ar_bits_user === 0.U)

    out.r.ready                    := core.io.m_axi_rready_o
    core.io.m_axi_rvalid_i     := out.r.valid
    core.io.m_axi_rid_i   := out.r.bits.id
    core.io.m_axi_rdata_i := out.r.bits.data
    core.io.m_axi_rresp_i := out.r.bits.resp
    core.io.m_axi_rlast_i := out.r.bits.last
    // core.io.axi_resp_i_r_bits_user := 0.U // unused
  }
}
