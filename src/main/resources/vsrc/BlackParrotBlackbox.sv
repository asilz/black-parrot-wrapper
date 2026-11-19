function automatic integer clog2;
    input integer value;
    integer i;
    begin
        value = value - 1;
        for (i = 0; value > 0; i = i + 1)
            value = value >> 1;
        clog2 = i;
    end
endfunction

module BlackParrotBlackbox
    #(
        parameter axi_core_clk_async_p = 0,
    parameter lg_async_fifo_size_lp = 3,
    parameter m_axil_addr_width_p   = 32,
    parameter m_axil_data_width_p   = 32,
    parameter m_axil_mask_width_lp  = m_axil_data_width_p/8,
    parameter s_axil_addr_width_p   = 32,
    parameter s_axil_data_width_p   = 32,
    parameter s_axil_mask_width_lp  = s_axil_data_width_p/8,
    parameter axi_addr_width_p      = 48, // <-- corrected
    parameter axi_data_width_p      = 64,
    parameter axi_id_width_p        = 4,
    parameter axi_mask_width_lp     = axi_data_width_p/8,
    parameter paddr_width_p         = 48   // <-- NEW, must match axi_addr_width_p

   // `declare_bp_bedrock_if_widths(paddr_width_p, lce_id_width_p, cce_id_width_p, did_width_p, lce_assoc_p)
    )
(
    input                                       axi_clk_i
   , input                                     core_clk_i
   , input                                     rt_clk_i
   , input                                     async_reset_i

   //======================== Outgoing I/O ========================
   , output logic [m_axil_addr_width_p-1:0]    m_axil_awaddr_o
   , output [2:0]                              m_axil_awprot_o
   , output logic                              m_axil_awvalid_o
   , input                                     m_axil_awready_i

   , output logic [m_axil_data_width_p-1:0]    m_axil_wdata_o
   , output logic [m_axil_mask_width_lp-1:0]   m_axil_wstrb_o
   , output logic                              m_axil_wvalid_o
   , input                                     m_axil_wready_i

   , input [1:0]                               m_axil_bresp_i
   , input                                     m_axil_bvalid_i
   , output logic                              m_axil_bready_o

   , output logic [m_axil_addr_width_p-1:0]    m_axil_araddr_o
   , output [2:0]                              m_axil_arprot_o
   , output logic                              m_axil_arvalid_o
   , input                                     m_axil_arready_i

   , input [m_axil_data_width_p-1:0]           m_axil_rdata_i
   , input [1:0]                               m_axil_rresp_i
   , input                                     m_axil_rvalid_i
   , output logic                              m_axil_rready_o

   //======================== Incoming I/O ========================
   , input [s_axil_addr_width_p-1:0]           s_axil_awaddr_i
   , input [2:0]                               s_axil_awprot_i
   , input                                     s_axil_awvalid_i
   , output logic                              s_axil_awready_o

   , input [s_axil_data_width_p-1:0]           s_axil_wdata_i
   , input [s_axil_mask_width_lp-1:0]          s_axil_wstrb_i
   , input                                     s_axil_wvalid_i
   , output logic                              s_axil_wready_o

   , output [1:0]                              s_axil_bresp_o
   , output logic                              s_axil_bvalid_o
   , input                                     s_axil_bready_i

   , input [s_axil_addr_width_p-1:0]           s_axil_araddr_i
   , input [2:0]                               s_axil_arprot_i
   , input                                     s_axil_arvalid_i
   , output logic                              s_axil_arready_o

   , output logic [s_axil_data_width_p-1:0]    s_axil_rdata_o
   , output [1:0]                              s_axil_rresp_o
   , output logic                              s_axil_rvalid_o
   , input                                     s_axil_rready_i

   //======================== Outgoing Memory ========================
   , output logic [axi_addr_width_p-1:0]       m_axi_awaddr_o
   , output logic                              m_axi_awvalid_o
   , input                                     m_axi_awready_i
   , output logic [axi_id_width_p-1:0]         m_axi_awid_o
   , output logic                              m_axi_awlock_o
   , output logic [3:0]                        m_axi_awcache_o
   , output logic [2:0]                        m_axi_awprot_o
   , output logic [7:0]                        m_axi_awlen_o
   , output logic [2:0]                        m_axi_awsize_o
   , output logic [1:0]                        m_axi_awburst_o
   , output logic [3:0]                        m_axi_awqos_o

   , output logic [axi_data_width_p-1:0]       m_axi_wdata_o
   , output logic                              m_axi_wvalid_o
   , input                                     m_axi_wready_i
   , output logic [axi_id_width_p-1:0]         m_axi_wid_o
   , output logic                              m_axi_wlast_o
   , output logic [axi_mask_width_lp-1:0]      m_axi_wstrb_o

   , input                                     m_axi_bvalid_i
   , output logic                              m_axi_bready_o
   , input [axi_id_width_p-1:0]                m_axi_bid_i
   , input [1:0]                               m_axi_bresp_i

   , output logic [axi_addr_width_p-1:0]       m_axi_araddr_o
   , output logic                              m_axi_arvalid_o
   , input                                     m_axi_arready_i
   , output logic [axi_id_width_p-1:0]         m_axi_arid_o
   , output logic                              m_axi_arlock_o
   , output logic [3:0]                        m_axi_arcache_o
   , output logic [2:0]                        m_axi_arprot_o
   , output logic [7:0]                        m_axi_arlen_o
   , output logic [2:0]                        m_axi_arsize_o
   , output logic [1:0]                        m_axi_arburst_o
   , output logic [3:0]                        m_axi_arqos_o

   , input [axi_data_width_p-1:0]              m_axi_rdata_i
   , input                                     m_axi_rvalid_i
   , output logic                              m_axi_rready_o
   , input [axi_id_width_p-1:0]                m_axi_rid_i
   , input                                     m_axi_rlast_i
   , input [1:0]                               m_axi_rresp_i
);

   bp_axi_top #(
    .axi_core_clk_async_p (axi_core_clk_async_p),
    //.lg_async_fifo_size_lp(lg_async_fifo_size_lp),
    .m_axil_addr_width_p  (m_axil_addr_width_p),
    .m_axil_data_width_p  (m_axil_data_width_p),
    .s_axil_addr_width_p  (s_axil_addr_width_p),
    .s_axil_data_width_p  (s_axil_data_width_p),
    .axi_addr_width_p     (axi_addr_width_p),
    .axi_data_width_p     (axi_data_width_p),
    .axi_id_width_p       (axi_id_width_p)
  ) i_bp_axi_top (
    .axi_clk_i      (axi_clk_i),
    .core_clk_i     (core_clk_i),
    .rt_clk_i       (rt_clk_i),
    .async_reset_i  (async_reset_i),

    .m_axil_awaddr_o(m_axil_awaddr_o),
    .m_axil_awprot_o(m_axil_awprot_o),
    .m_axil_awvalid_o(m_axil_awvalid_o),
    .m_axil_awready_i(m_axil_awready_i),

    .m_axil_wdata_o (m_axil_wdata_o),
    .m_axil_wstrb_o (m_axil_wstrb_o),
    .m_axil_wvalid_o(m_axil_wvalid_o),
    .m_axil_wready_i(m_axil_wready_i),

    .m_axil_bresp_i (m_axil_bresp_i),
    .m_axil_bvalid_i(m_axil_bvalid_i),
    .m_axil_bready_o(m_axil_bready_o),

    .m_axil_araddr_o(m_axil_araddr_o),
    .m_axil_arprot_o(m_axil_arprot_o),
    .m_axil_arvalid_o(m_axil_arvalid_o),
    .m_axil_arready_i(m_axil_arready_i),

    .m_axil_rdata_i (m_axil_rdata_i),
    .m_axil_rresp_i (m_axil_rresp_i),
    .m_axil_rvalid_i(m_axil_rvalid_i),
    .m_axil_rready_o(m_axil_rready_o),

    .s_axil_awaddr_i(s_axil_awaddr_i),
    .s_axil_awprot_i(s_axil_awprot_i),
    .s_axil_awvalid_i(s_axil_awvalid_i),
    .s_axil_awready_o(s_axil_awready_o),

    .s_axil_wdata_i (s_axil_wdata_i),
    .s_axil_wstrb_i (s_axil_wstrb_i),
    .s_axil_wvalid_i(s_axil_wvalid_i),
    .s_axil_wready_o(s_axil_wready_o),

    .s_axil_bresp_o (s_axil_bresp_o),
    .s_axil_bvalid_o(s_axil_bvalid_o),
    .s_axil_bready_i(s_axil_bready_i),

    .s_axil_araddr_i(s_axil_araddr_i),
    .s_axil_arprot_i(s_axil_arprot_i),
    .s_axil_arvalid_i(s_axil_arvalid_i),
    .s_axil_arready_o(s_axil_arready_o),

    .s_axil_rdata_o (s_axil_rdata_o),
    .s_axil_rresp_o (s_axil_rresp_o),
    .s_axil_rvalid_o(s_axil_rvalid_o),
    .s_axil_rready_i(s_axil_rready_i),

    .m_axi_awaddr_o (m_axi_awaddr_o),
    .m_axi_awvalid_o(m_axi_awvalid_o),
    .m_axi_awready_i(m_axi_awready_i),
    .m_axi_awid_o   (m_axi_awid_o),
    .m_axi_awlock_o (m_axi_awlock_o),
    .m_axi_awcache_o(m_axi_awcache_o),
    .m_axi_awprot_o (m_axi_awprot_o),
    .m_axi_awlen_o  (m_axi_awlen_o),
    .m_axi_awsize_o (m_axi_awsize_o),
    .m_axi_awburst_o(m_axi_awburst_o),
    .m_axi_awqos_o  (m_axi_awqos_o),

    .m_axi_wdata_o  (m_axi_wdata_o),
    .m_axi_wvalid_o (m_axi_wvalid_o),
    .m_axi_wready_i (m_axi_wready_i),
    .m_axi_wid_o    (m_axi_wid_o),
    .m_axi_wlast_o  (m_axi_wlast_o),
    .m_axi_wstrb_o  (m_axi_wstrb_o),

    .m_axi_bvalid_i (m_axi_bvalid_i),
    .m_axi_bready_o (m_axi_bready_o),
    .m_axi_bid_i    (m_axi_bid_i),
    .m_axi_bresp_i  (m_axi_bresp_i),

    .m_axi_araddr_o (m_axi_araddr_o),
    .m_axi_arvalid_o(m_axi_arvalid_o),
    .m_axi_arready_i(m_axi_arready_i),
    .m_axi_arid_o   (m_axi_arid_o),
    .m_axi_arlock_o (m_axi_arlock_o),
    .m_axi_arcache_o(m_axi_arcache_o),
    .m_axi_arprot_o (m_axi_arprot_o),
    .m_axi_arlen_o  (m_axi_arlen_o),
    .m_axi_arsize_o (m_axi_arsize_o),
    .m_axi_arburst_o(m_axi_arburst_o),
    .m_axi_arqos_o  (m_axi_arqos_o),

    .m_axi_rdata_i  (m_axi_rdata_i),
    .m_axi_rvalid_i (m_axi_rvalid_i),
    .m_axi_rready_o (m_axi_rready_o),
    .m_axi_rid_i    (m_axi_rid_i),
    .m_axi_rlast_i  (m_axi_rlast_i),
    .m_axi_rresp_i  (m_axi_rresp_i)
  );
endmodule