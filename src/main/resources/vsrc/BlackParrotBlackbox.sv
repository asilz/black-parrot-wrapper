module BlackParrotBlackbox
    #(
        parameter cfg_bus_width_lp = 64,
        parameter mem_fwd_header_width_lp = 64,
        parameter bedrock_fill_width_p = 64,
        parameter mem_rev_header_width_lp = 64
    )
(
     input                                                                  clk_i
   , input                                                                reset_i
   , input [cfg_bus_width_lp-1:0]                                         cfg_bus_i


   // Outgoing I/O
   , output logic [2*mem_fwd_header_width_lp-1:0]                           mem_fwd_header_o
   , output logic [2*bedrock_fill_width_p-1:0]                              mem_fwd_data_o
   , output logic [1:0]                                                        mem_fwd_v_o
   , input  logic [1:0]                                                              mem_fwd_ready_and_i

   , input logic [2*mem_rev_header_width_lp-1:0]                                  mem_rev_header_i
   , input [2*bedrock_fill_width_p-1:0]                                     mem_rev_data_i
   , input logic [1:0]                                                              mem_rev_v_i
   , output logic [1:0]                                                        mem_rev_ready_and_o

    , input                                             debug_irq_i
    , input                                             timer_irq_i
    , input                                             software_irq_i
    , input                                             m_external_irq_i
    , input                                             s_external_irq_i
);

    bp_unicore_lite i_bp_unicore(.clk_i,
                            .reset_i,
                            .cfg_bus_i,
                            .mem_fwd_header_o,
                            .mem_fwd_data_o,
                            .mem_fwd_v_o,
                            .mem_fwd_ready_and_i,
                            .mem_rev_header_i,
                            .mem_rev_data_i,
                            .mem_rev_v_i,
                            .mem_rev_ready_and_o,
                            debug_irq_i,
                            timer_irq_i,
                            software_irq_i,
                            m_external_irq_i,
                            s_external_irq_i
                            );
endmodule