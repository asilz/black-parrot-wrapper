package bp

import sys.process._

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, StringParam, RawParam}

import scala.collection.mutable.{ListBuffer}

class BlackParrotBlackbox(
    cfg_bus_width_lp: Int,
    mem_fwd_header_width_lp: Int,
    bedrock_fill_width_p: Int,
    mem_rev_header_width_lp: Int
    )
    extends BlackBox(
    Map(
        "cfg_bus_width_lp" -> IntParam(cfg_bus_width_lp),
        "mem_fwd_header_width_lp" -> IntParam(mem_fwd_header_width_lp),
        "bedrock_fill_width_p" -> IntParam(bedrock_fill_width_p),
        "mem_rev_header_width_lp" -> IntParam(mem_rev_header_width_lp)
        )) 
    with HasBlackBoxPath {
  val io = IO(new Bundle {
        val clk_i = Input(Clock())
        val reset_i = Input(Bool())
        val cfg_bus_i = Input(UInt(mem_noc_did_width_p.W))
        val mem_fwd_header_o = Output(UInt(mem_fwd_header_width_lp.W))
        val mem_fwd_data_o = Output(UInt(bedrock_fill_width_p.W))
        val mem_fwd_v_o = Output(Bool())
        val mem_fwd_ready_and_i = Input(Bool())
        val mem_rev_header_i = Input(UInt(mem_rev_header_width_lp.W))
        val mem_rev_data_i = Input(UInt(bedrock_fill_width_p.W))
        val mem_rev_v_i = Input(Bool())
        val mem_rev_ready_and_o = Output(Bool())
        val debug_irq_i = Input(Bool())
        val timer_irq_i = Input(Bool())
        val software_irq_i = Input(Bool())
        val m_external_irq_i = Input(Bool())
        val s_external_irq_i = Input(Bool())
    })

    val chipyardDir = System.getProperty("user.dir")
    val bpVsrcDir = s"$chipyardDir/generators/black-parrot/src/main/resources/vsrc"

    val make = s"make -C ${bpVsrcDir} default "
    val proc = if (true) make + "EXTRA_PREPROC_DEFINES=FIRESIM_TRACE" else make
    require (proc.! == 0, "Failed to run preprocessing step")

    // generated from preprocessing step
    addPath(s"$bpVsrcDir/BlackParrotBlackbox.preprocessed.sv")
}