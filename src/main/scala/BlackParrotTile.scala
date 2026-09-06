package bp

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, StringParam, RawParam}

import scala.collection.mutable.{ListBuffer}

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem.{RocketCrossingParams}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util._
import freechips.rocketchip.tile._
import freechips.rocketchip.prci._

case class BlackParrotParams(
  val mem_noc_did_width_p: Int = 0,
  val coh_noc_cord_width_p: Int = 0,
  val mem_fwd_header_width_lp: Int = 0,
  val bedrock_fill_width_p: Int = 0,
  val mem_rev_header_width_lp: Int = 0,
  val l2_slices_p: Int = 0,
  val l2_banks_p: Int = 0,
  val dma_pkt_width_lp: Int = 0,
  val l2_fill_width_p: Int = 0
) extends CoreParams {
  val xLen = 32
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

class BlackParrotTile(
  val blackParrotParams: BlackParrotTileParams,
  crossing: ClockCrossingType,
  lookup: LookupByHartIdImpl,
  q: Parameters)
  extends BaseTile(blackParrotParams, crossing, lookup, q)
  with SinksExternalInterrupts
  with SourcesExternalNotifications
{

  // Private constructor ensures altered LazyModule.p is used implicitly
  def this(params: BlackParrotParams, crossing: HierarchicalElementCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters) =
    this(params, crossing.crossingType, lookup, p)

  // Require TileLink nodes
  val intOutwardNode = None
  val masterNode = visibilityNode
  val slaveNode = TLIdentityNode()

  // Implementation class (See below)
  override lazy val module = new BlackParrotTileModuleImp(this)

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

  // TODO: Create TileLink nodes and connections here.
  def connectBlackParrotInterrupts(debug: Bool, msip: Bool, mtip: Bool, meip: Bool) {
    val (interrupts, _) = intSinkNode.in(0)
    debug := interrupts(0)
    msip := interrupts(1)
    mtip := interrupts(2)
    meip := interrupts(3)
  }
}

class BlackParrotTileModuleImp(outer: BlackParrotTile) extends BaseTileModuleImp(outer){

  val core = Module(new BlackParrotBlackbox(
    mem_noc_did_width_p = outer.blackParrotParams.core.mem_noc_did_width_p,
    coh_noc_cord_width_p = outer.blackParrotParams.core.coh_noc_cord_width_p,
    mem_fwd_header_width_lp = outer.blackParrotParams.core.mem_fwd_header_width_lp,
    bedrock_fill_width_p = outer.blackParrotParams.core.bedrock_fill_width_p,
    mem_rev_header_width_lp = outer.blackParrotParams.core.mem_rev_header_width_lp,
    l2_slices_p = outer.blackParrotParams.core.l2_slices_p,
    l2_banks_p = outer.blackParrotParams.core.l2_banks_p,
    dma_pkt_width_lp = outer.blackParrotParams.core.dma_pkt_width_lp,
    l2_fill_width_p = outer.blackParrotParams.core.l2_fill_width_p,
  ))

  //connect signals
  core.io.clk_i := clock
  core.io.reset_i := reset.asBool

  // outer.connectBlackParrotInterrupts(core.io.debug_req_i, core.io.irq_software_i, core.io.irq_timer_i, core.io.irq_external_i)
  // core.io.irq_nm_i := 0.U //recoverable nmi, tying off
  // core.io.irq_fast_i := 0.U //local interrupts, tying off

  // MEMORY
  // DMEM
  val (dmem, dmem_edge) = outer.dmemNode.out(0)

  val s_ready :: s_active :: s_inflight :: Nil = Enum(3)
  val dmem_state = RegInit(s_ready)

  val dmem_addr = Reg(UInt(32.W))
  val dmem_data = Reg(UInt(32.W))
  val dmem_mask = Reg(UInt(8.W))
  val byte_en = Reg(UInt(4.W))
  val num_bytes = Reg(UInt(3.W))
  val r_size = Reg(UInt(2.W))
  val w_size = Reg(UInt(2.W))
  r_size := 2.U

  when (dmem_state === s_ready && core.io.data_req_o) {
    dmem_state := s_active
    dmem_addr := core.io.data_addr_o + (PriorityEncoder(core.io.data_be_o) * core.io.data_we_o) //if write, shift address based on mask
    dmem_data := core.io.data_wdata_o
    byte_en := core.io.data_be_o
    dmem_mask := core.io.data_be_o
    w_size := PriorityEncoder(PopCount(core.io.data_be_o)) //log2Ceil
  }
  when (dmem_state === s_active && dmem.a.fire) {
    dmem_state := s_inflight
  }
  when (dmem_state === s_inflight && dmem.d.fire) {
    dmem_state := s_ready
  }
  dmem.a.valid := dmem_state === s_active
  core.io.data_gnt_i := dmem_state === s_ready && core.io.data_req_o
  dmem.d.ready := true.B
  core.io.data_rvalid_i := dmem.d.valid

  val dmem_get = dmem_edge.Get(0.U, dmem_addr, r_size)._2
  val dmem_put = dmem_edge.Put(0.U, dmem_addr, w_size, dmem_data, dmem_mask)._2

  dmem.a.bits := Mux(core.io.data_we_o, dmem_put, dmem_get)             //write or read depending on write enable
  core.io.data_rdata_i := dmem.d.bits.data                              //read data
  core.io.data_err_i := dmem.d.bits.corrupt | dmem.d.bits.denied        //set error

  //unused
  dmem.b.valid := false.B
  dmem.c.ready := true.B
  dmem.e.ready := true.B

  //IMEM
  val (imem, imem_edge) = outer.imemNode.out(0)
  val imem_state = RegInit(s_ready)

  val imem_addr = Reg(UInt(32.W))

  when (imem_state === s_ready && core.io.instr_req_o) {
    imem_state := s_active
    imem_addr := core.io.instr_addr_o
  }
  when (imem_state === s_active && imem.a.fire) {
    imem_state := s_inflight
  }
  when (imem_state === s_inflight && imem.d.fire) {
    imem_state := s_ready
  }

  imem.a.valid := imem_state === s_active
  core.io.instr_gnt_i := imem_state === s_ready
  imem.d.ready := true.B
  core.io.instr_rvalid_i := imem.d.valid

  val imem_get = imem_edge.Get(0.U, imem_addr, r_size)._2

  imem.a.bits := imem_get
  core.io.instr_rdata_i := imem.d.bits.data
  core.io.instr_err_i := imem.d.bits.corrupt | imem.d.bits.denied

  //unused
  imem.b.valid := false.B
  imem.c.ready := true.B
  imem.e.ready := true.B

  //used for icache, tie off
  core.io.ram_cfg_i_ram_cfg_en := 0.U
  core.io.ram_cfg_i_ram_cfg := 0.U
  core.io.ram_cfg_i_rf_cfg_en := 0.U
  core.io.ram_cfg_i_rf_cfg := 0.U

  //continuously fetch instructions
  core.io.fetch_enable_i := 1.U

  //DFT not used
  core.io.scan_rst_ni := 1.U
}