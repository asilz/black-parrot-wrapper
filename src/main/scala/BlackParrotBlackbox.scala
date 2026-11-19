package bp

import sys.process._

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, StringParam, RawParam}

import scala.collection.mutable.{ListBuffer}

class BlackParrotBlackbox(
    axi_core_clk_async_p: Int,
    lg_async_fifo_size_lp: Int,
    m_axil_addr_width_p: Int,
    m_axil_data_width_p: Int,
    m_axil_mask_width_lp: Int,
    s_axil_addr_width_p: Int,
    s_axil_data_width_p: Int,
    s_axil_mask_width_lp: Int,
    axi_addr_width_p: Int,
    axi_data_width_p: Int,
    axi_id_width_p: Int,
    axi_mask_width_lp: Int,
    paddr_width_p: Int                        // <-- NEW
    )
    extends BlackBox(
    Map(
        "axi_core_clk_async_p" -> IntParam(axi_core_clk_async_p),
        "lg_async_fifo_size_lp" -> IntParam(lg_async_fifo_size_lp),
        "m_axil_addr_width_p" -> IntParam(m_axil_addr_width_p),
        "m_axil_data_width_p" -> IntParam(m_axil_data_width_p),
        "m_axil_mask_width_lp" -> IntParam(m_axil_mask_width_lp),
        "s_axil_addr_width_p" -> IntParam(s_axil_addr_width_p),
        "s_axil_data_width_p" -> IntParam(s_axil_data_width_p),
        "s_axil_mask_width_lp" -> IntParam(s_axil_mask_width_lp),
        "axi_addr_width_p" -> IntParam(axi_addr_width_p),
        "axi_data_width_p" -> IntParam(axi_data_width_p),
        "axi_id_width_p" -> IntParam(axi_id_width_p),
        "axi_mask_width_lp" -> IntParam(axi_mask_width_lp),
        "paddr_width_p"         -> IntParam(paddr_width_p)   // <-- NEW
        )) 
    with HasBlackBoxPath {
  val io = IO(new Bundle {
       val axi_clk_i = Input(Clock())      
       val core_clk_i = Input(Clock())     
       val rt_clk_i = Input(Clock())        
       val async_reset_i = Input(Bool())
       val m_axil_awaddr_o = Output(UInt((m_axil_addr_width_p).W))
       val m_axil_awprot_o = Output(UInt(3.W))
       val m_axil_awvalid_o = Output(Bool())
       val m_axil_awready_i = Input(Bool())
       val m_axil_wdata_o = Output(UInt((m_axil_data_width_p).W))
       val m_axil_wstrb_o = Output(UInt((m_axil_mask_width_lp).W))
       val m_axil_wvalid_o = Output(Bool())
       val m_axil_wready_i = Input(Bool())
       val m_axil_bresp_i = Input(UInt(2.W))
       val m_axil_bvalid_i = Input(Bool())
       val m_axil_bready_o = Output(Bool())
       val m_axil_araddr_o = Output(UInt((m_axil_addr_width_p).W))
       val m_axil_arprot_o = Output(UInt(3.W))
       val m_axil_arvalid_o = Output(Bool())
       val m_axil_arready_i = Input(Bool())
       val m_axil_rdata_i = Input(UInt((m_axil_data_width_p).W))
       val m_axil_rresp_i = Input(UInt(2.W))
       val m_axil_rvalid_i = Input(Bool())
       val m_axil_rready_o = Output(Bool())
       val s_axil_awaddr_i = Input(UInt((s_axil_addr_width_p).W))
       val s_axil_awprot_i = Input(UInt(3.W))
       val s_axil_awvalid_i = Input(Bool())
       val s_axil_awready_o = Output(Bool())
       val s_axil_wdata_i = Input(UInt((s_axil_data_width_p).W))
       val s_axil_wstrb_i = Input(UInt((s_axil_mask_width_lp).W))  
       val s_axil_wvalid_i = Input(Bool())
       val s_axil_wready_o = Output(Bool())
       val s_axil_bresp_o = Output(UInt(2.W))
       val s_axil_bvalid_o = Output(Bool()) 
       val s_axil_bready_i = Input(Bool())
       val s_axil_araddr_i = Input(UInt((s_axil_addr_width_p).W))  
       val s_axil_arprot_i = Input(UInt(3.W))
       val s_axil_arvalid_i = Input(Bool())
       val s_axil_arready_o = Output(Bool())
       val s_axil_rdata_o = Output(UInt((s_axil_data_width_p).W))  
       val s_axil_rresp_o = Output(UInt(2.W))
       val s_axil_rvalid_o = Output(Bool())
       val s_axil_rready_i = Input(Bool())
       val m_axi_awaddr_o = Output(UInt((axi_addr_width_p).W))  
       val m_axi_awvalid_o = Output(Bool()) 
       val m_axi_awready_i = Input(Bool())
       val m_axi_awid_o = Output(UInt((axi_id_width_p).W))
       val m_axi_awlock_o = Output(Bool()) 
       val m_axi_awcache_o = Output(UInt(4.W))
       val m_axi_awprot_o = Output(UInt(3.W))  
       val m_axi_awlen_o = Output(UInt(8.W))   
       val m_axi_awsize_o = Output(UInt(3.W))  
       val m_axi_awburst_o = Output(UInt(2.W)) 
       val m_axi_awqos_o = Output(UInt(4.W))   
       val m_axi_wdata_o = Output(UInt((axi_data_width_p).W))
       val m_axi_wvalid_o = Output(Bool()) 
       val m_axi_wready_i = Input(Bool())
       val m_axi_wid_o = Output(UInt((axi_id_width_p).W))
       val m_axi_wlast_o = Output(Bool())    
       val m_axi_wstrb_o = Output(UInt((axi_mask_width_lp).W))
       val m_axi_bvalid_i = Input(Bool()) 
       val m_axi_bready_o = Output(Bool())
       val m_axi_bid_i = Input(UInt((axi_id_width_p).W))
       val m_axi_bresp_i = Input(UInt(2.W))  
       val m_axi_araddr_o = Output(UInt((axi_addr_width_p).W)) 
       val m_axi_arvalid_o = Output(Bool()) 
       val m_axi_arready_i = Input(Bool())  
       val m_axi_arid_o = Output(UInt((axi_id_width_p).W))
       val m_axi_arlock_o = Output(Bool())  
       val m_axi_arcache_o = Output(UInt(4.W)) 
       val m_axi_arprot_o = Output(UInt(3.W))  
       val m_axi_arlen_o = Output(UInt(8.W))   
       val m_axi_arsize_o = Output(UInt(3.W))  
       val m_axi_arburst_o = Output(UInt(2.W)) 
       val m_axi_arqos_o = Output(UInt(4.W))   
       val m_axi_rdata_i = Input(UInt((axi_data_width_p).W))  
       val m_axi_rvalid_i = Input(Bool())  
       val m_axi_rready_o = Output(Bool())   
       val m_axi_rid_i = Input(UInt((axi_id_width_p).W))   
       val m_axi_rlast_i = Input(Bool())    
       val m_axi_rresp_i = Input(UInt(2.W))         
    })

    val chipyardDir = System.getProperty("user.dir")
    val bpVsrcDir   = s"$chipyardDir/generators/black-parrot/src/main/resources/vsrc"
    
    val makeCmd = Seq(
      "make", "-C", bpVsrcDir, "default",
      "EXTRA_PREPROC_DEFINES=FIRESIM_TRACE BSG_HIDE_FROM_SYNTHESIS"
    )
    require(makeCmd.! == 0, "Failed to run preprocessing step")
    
    addPath(s"$bpVsrcDir/BlackParrotBlackbox.preprocessed.sv")
}