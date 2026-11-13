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

    logic [1:0][mem_rev_header_width_lp-1:0] mem_rev_header_i_;
    logic [1:0][bedrock_fill_width_p-1:0] mem_rev_data_i_;

    genvar i, j;
    generate
        for (i = 0; i < 2; i = i + 1) begin
            for (j = 0; j < mem_rev_header_width_lp; j = j + 1) begin
                    assign mem_rev_header_i_[i][j] = mem_rev_header_i[i*mem_rev_header_width_lp+j];
            end
        end
    endgenerate

    generate
        for (i = 0; i < 2; i = i + 1) begin
            for (j = 0; j < bedrock_fill_width_p; j = j + 1) begin
                    assign mem_rev_data_i_[i][j] = mem_rev_data_i[i*bedrock_fill_width_p+j];
            end
        end
    endgenerate

    bp_axi_top i_bp_unicore(.clk_i,
                            .reset_i,
                            .cfg_bus_i,
                            .mem_fwd_header_o,
                            .mem_fwd_data_o,
                            .mem_fwd_v_o,
                            .mem_fwd_ready_and_i,
                            .mem_rev_header_i(mem_rev_header_i_),
                            .mem_rev_data_i(mem_rev_data_i_),
                            .mem_rev_v_i,
                            .mem_rev_ready_and_o,
                            .debug_irq_i,
                            .timer_irq_i,
                            .software_irq_i,
                            .m_external_irq_i,
                            .s_external_irq_i
                            );
endmodule