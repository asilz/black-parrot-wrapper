package bp

import sys.process._

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, StringParam, RawParam}

import scala.collection.mutable.{ListBuffer}

class BlackParrotBlackBox(val w: Int) extends BlackBox(
    Map(
        "mem_noc_did_width_p" -> IntParam(mem_noc_did_width_p),
        "coh_noc_cord_width_p" -> IntParam(coh_noc_cord_width_p),
        "mem_fwd_header_width_lp" -> IntParam(mem_fwd_header_width_lp),
        "bedrock_fill_width_p" -> IntParam(bedrock_fill_width_p),
        "mem_rev_header_width_lp" -> IntParam(mem_rev_header_width_lp),
        "l2_slices_p" -> IntParam(l2_slices_p),
        "l2_banks_p" -> IntParam(l2_banks_p),
        "dma_pkt_width_lp" -> IntParam(dma_pkt_width_lp),
        "l2_fill_width_p" -> IntParam(l2_fill_width_p)
        )) 
    with HasBlackBoxResource {
  val io = IO(new Bundle {
        val clk_i = Input(Clock())
        val rt_clk_i = Input(Bool())
        val reset_i = Input(Bool())
        val my_did_i = Input(UInt(mem_noc_did_width_p.W))
        val host_did_i = Input(UInt(mem_noc_did_width_p.W))
        val my_cord_i = Input(UInt(coh_noc_cord_width_p.W))
        val mem_fwd_header_o = Output(UInt(mem_fwd_header_width_lp.W))
        val mem_fwd_data_o = Output(UInt(bedrock_fill_width_p.W))
        val mem_fwd_v_o = Output(Bool())
        val mem_fwd_ready_and_i = Input(Bool())
        val mem_rev_header_i = Input(UInt(mem_rev_header_width_lp.W))
        val mem_rev_data_i = Input(UInt(bedrock_fill_width_p.W))
        val mem_rev_v_i = Input(Bool())
        val mem_rev_ready_and_o = Output(Bool())
        val mem_fwd_header_i = Input(UInt(mem_fwd_header_width_lp.W))
        val mem_fwd_data_i = Input(UInt(bedrock_fill_width_p.W))
        val mem_fwd_v_i = Input(Bool())
        val mem_fwd_ready_and_o = Output(Bool())
        val mem_rev_header_o = Output(UInt(mem_rev_header_width_lp.w))
        val mem_rev_data_o = Output(UInt(bedrock_fill_width_p.W))
        val mem_rev_v_o = Output(Bool())
        val mem_rev_ready_and_i = Input(Bool())
        val dma_pkt_o = Output(UInt((l2_slices_p*l2_banks_p*dma_pkt_width_lp).W))
        val dma_pkt_v_o = Output(UInt((l2_slices_p*l2_banks_p).W))
        val dma_pkt_ready_and_i = Input(UInt((l2_slices_p*l2_banks_p).W))
        val dma_data_i = Input(UInt((l2_slices_p*l2_banks_p*l2_fill_width_p).W))
        val dma_data_v_i = Input(UInt((l2_slices_p*l2_banks_p).W))
        val dma_data_ready_and_o = Output(UInt((l2_slices*l2_banks_p).W))
        val dma_data_o = Output(UInt(l2_slices_p*l2_banks_p*l2_fill_width_p).W)
        val dma_data_v_o = Output(UInt(l2_slices_p*l2_banks_p).W)
        val dma_data_ready_and_i = Input(UInt((l2_slices_p*l2_banks_p).W))
    })

    val chipyardDir = System.getProperty("user.dir")
    val bpVsrcDir = s"$chipyardDir/generators/black-parrot/src/main/resources/vsrc"

    val proc = s"make -C $bpVsrcDir default"
    require (proc.! == 0, "Failed to run preprocessing step")

    // generated from preprocessing step
    addPath(s"$bpVsrcDir/BlackParrotCoreBlackbox.preprocessed.sv")
}