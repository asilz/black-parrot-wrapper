module BlackParrotBlackBox
    #(
    )
(
     input                                                                  clk_i
   , input                                                                rt_clk_i
   , input                                                                reset_i

   , input [mem_noc_did_width_p-1:0]                                      my_did_i
   , input [mem_noc_did_width_p-1:0]                                      host_did_i
   , input [coh_noc_cord_width_p-1:0]                                     my_cord_i

   // Outgoing I/O
   , output logic [mem_fwd_header_width_lp-1:0]                           mem_fwd_header_o
   , output logic [bedrock_fill_width_p-1:0]                              mem_fwd_data_o
   , output logic                                                         mem_fwd_v_o
   , input                                                                mem_fwd_ready_and_i

   , input [mem_rev_header_width_lp-1:0]                                  mem_rev_header_i
   , input [bedrock_fill_width_p-1:0]                                     mem_rev_data_i
   , input                                                                mem_rev_v_i
   , output logic                                                         mem_rev_ready_and_o

   // Incoming I/O
   , input [mem_fwd_header_width_lp-1:0]                                  mem_fwd_header_i
   , input [bedrock_fill_width_p-1:0]                                     mem_fwd_data_i
   , input                                                                mem_fwd_v_i
   , output logic                                                         mem_fwd_ready_and_o

   , output logic [mem_rev_header_width_lp-1:0]                           mem_rev_header_o
   , output logic [bedrock_fill_width_p-1:0]                              mem_rev_data_o
   , output logic                                                         mem_rev_v_o
   , input                                                                mem_rev_ready_and_i

   // DRAM interface
   /*
    For the multidimensional outputs and inputs, we need to flatten them it seems.
    https://stackoverflow.com/questions/73189966/scala-chisel-blackbox-with-2-d-verilog-ports
    https://github.com/chipsalliance/chisel/issues/743
    */
   , output logic [l2_slices_p*l2_banks_p*dma_pkt_width_lp-1:0]           dma_pkt_o
   , output logic [l2_slices_p*l2_banks_p-1:0]                            dma_pkt_v_o
   , input [l2_slices_p*l2_banks_p-1:0]                                   dma_pkt_ready_and_i

   , input [l2_slices_p*l2_banks_p*l2_fill_width_p-1:0]                   dma_data_i
   , input [l2_slices_p*l2_banks_p-1:0]                                   dma_data_v_i
   , output logic [l2_slices*l2_banks_p-1:0]                              dma_data_ready_and_o

   , output logic [l2_slices_p*l2_banks_p*l2_fill_width_p-1:0]            dma_data_o
   , output logic [l2_slices_p*l2_banks_p-1:0]                            dma_data_v_o
   , input [l2_slices_p*l2_banks_p-1:0]                                   dma_data_ready_and_i
   
);
    logic [l2_slices_p-1:0][l2_banks_p-1:0][dma_pkt_width_lp-1:0] dma_pkt_o_;
    logic [l2_slices_p-1:0][l2_banks_p-1:0] dma_pkt_v_o_;
    logic [l2_slices_p-1:0][l2_banks_p-1:0] dma_pkt_ready_and_i_;

    logic [l2_slices_p-1:0][l2_banks_p-1:0][l2_fill_width_p-1:0] dma_data_i_;
    logic [l2_slices_p-1:0][l2_banks_p-1:0] dma_data_v_i_;
    logic [l2_slices_p-1:0][l2_banks_p-1:0] dma_data_ready_and_o_;

    logic [l2_slices_p-1:0][l2_banks_p-1:0][l2_fill_width_p-1:0] dma_data_o_;
    logic [l2_slices_p-1:0][l2_banks_p-1:0] dma_data_v_o_;
    logic [l2_slices_p-1:0][l2_banks_p-1:0] dma_data_ready_and_i_; 
    genvar i, j, k;
    generate
        for (i = 0; i < l2_slices_p; i = i + 1) begin
            for (j = 0; j < l2_banks_p; j = j + 1) begin
                for(k = 0; k < dma_pkt_width_lp; k = k + 1) begin
                    assign dma_pkt_o_[i][j][k] = dma_pkt_o[i*dma_pkt_width_lp*l2_banks_p+j*dma_pkt_width_lp+k];
                end
            end
        end
    endgenerate

    bp_unicore i_bp_unicore(.clk_i,
                            .rt_clk_i,
                            .reset_i,
                            .my_did_i,
                            .host_did_i,
                            .my_cord_i,
                            .mem_fwd_header_o,
                            .mem_fwd_data_o,
                            .mem_fwd_v_o,
                            .mem_fwd_ready_and_i,
                            .mem_rev_header_i,
                            .mem_rev_data_i,
                            .mem_rev_v_i,
                            .mem_rev_ready_and_o,
                            .mem_fwd_header_i
                            .mem_fwd_data_i,
                            .mem_fwd_v_i,
                            .mem_fwd_ready_and_o,
                            .mem_rev_header_o,
                            .mem_rev_data_o,
                            .mem_rev_v_o,
                            .mem_rev_ready_and_i,
                            .dma_pkt_o(dma_pkt_o_),
                            .dma_pkt_v_o(dma_pkt_v_o_),
                            .dma_pkt_ready_and_i(dma_pkt_ready_and_i_),
                            .dma_data_i(dma_data_i_),
                            .dma_data_v_i(dma_data_v_i_),
                            .dma_data_ready_and_o(dma_data_ready_and_o_),
                            .dma_data_o(dma_data_o_),
                            .dma_data_v_o(dma_data_v_o_),
                            .dma_data_ready_and_i(dma_data_ready_and_i_),
                            );
endmodule