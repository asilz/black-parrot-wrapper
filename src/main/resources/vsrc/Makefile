base_dir=$(abspath ../../../../../..)
vsrc_dir=$(abspath .)
bp_dir=$(vsrc_dir)/black-parrot

PREPROC_VERILOG = $(vsrc_dir)/BlackParrotBlackbox.preprocessed.sv

.PHONY: default $(PREPROC_VERILOG)
default: $(PREPROC_VERILOG)

BLACK_PARROT_PKGS = \
	$(bp_dir)/external/basejump_stl/bsg_axi/bsg_axi_pkg.sv \
	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_pkg.sv \
	$(bp_dir)/external/basejump_stl/bsg_noc/bsg_noc_pkg.sv \
	$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router_pkg.sv \
	$(bp_dir)/bp_common/src/include/bp_common_pkg.sv \
	$(bp_dir)/bp_fe/src/include/bp_fe_pkg.sv \
	$(bp_dir)/bp_be/src/include/bp_be_pkg.sv \
	$(bp_dir)/bp_me/src/include/bp_me_pkg.sv \
	$(bp_dir)/bp_top/src/include/bp_top_pkg.sv \
	

INC_DIRS = \
	$(bp_dir)/external/basejump_stl/bsg_misc \
	$(bp_dir)/external/basejump_stl/bsg_cache \
	$(bp_dir)/external/basejump_stl/bsg_noc \
	$(bp_dir)/external/basejump_stl/bsg_dataflow \
	$(bp_dir)/external/basejump_stl/bsg_mem \
	$(bp_dir)/external/basejump_stl/bsg_async \
	$(bp_dir)/bp_common/src/include \
	$(bp_dir)/bp_common/src/v \
	$(bp_dir)/bp_common/src/include \
	$(bp_dir)/bp_fe/src/include \
	$(bp_dir)/bp_be/src/include \
	$(bp_dir)/bp_me/src/include \
	$(bp_dir)/bp_top/src/include \
	$(bp_dir)/external/HardFloat/source \
	$(bp_dir)/external/HardFloat/source/RISCV \


#INC_DIRS = \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_buffer_queue.sv             \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_decode.sv                   \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_dma.sv                      \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_dma_to_wormhole.sv          \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_miss.sv                     \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_data_mem.sv    \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_decode.sv      \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_dma.sv         \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_mhu.sv         \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_miss_fifo.sv   \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_pkg.sv         \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_stat_mem.sv    \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking.sv          \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking.svh         \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_tag_mem.sv  \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_non_blocking_tl_stage.sv    \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_sbuf.sv                  \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache.sv                       \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache.svh                      \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_tbuf.sv                  \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_axi_ordering.sv       \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_axi_rx.sv             \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_axi.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_axi_tx.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_dram_ctrl_rx.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_dram_ctrl.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_dram_ctrl_tx.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_test_dram_rx_reorder.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_test_dram_rx.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_test_dram.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_to_test_dram_tx.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_nonsynth_cache_axe_tracer.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_wormhole_to_cache_dma_fanout.sv \
#	$(bp_dir)/external/basejump_stl/bsg_cache/bsg_wormhole_to_cache_dma_inorder.sv \
#	$(bp_dir)/external/basejump_stl/bsg_misc/bsg_abs.sv                         \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_adder_cin.sv                    \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_adder_one_hot.sv                 \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_adder_ripple_carry.sv             \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_and.sv                             \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_arb_fixed.sv                       \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_arb_round_robin.sv                 \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_array_concentrate_static.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_array_reverse.sv                   \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_binary_plus_one_to_gray.sv         \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_buf_ctrl.sv                        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_buf.sv                             \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_circular_ptr.sv                    \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_clkbuf.sv                          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_clkgate_optional.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_concentrate_static.sv              \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clear_up_multi.sv          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clear_up_one_hot.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clear_up_saturating.sv     \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clear_up.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clock_downsample.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_dynamic_limit_en.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_dynamic_limit.sv           \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_max_clear_up.sv            \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_overflow_en.sv             \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_overflow_set_en.sv         \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_set_down.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_set_en.sv                  \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_up_down.sv                 \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_up_down_variable.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_counting_leading_zeros.sv          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_crossbar_control_basic_o_by_i.sv   \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_crossbar_control_locking_o_by_i.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_crossbar_o_by_i.sv                 \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_cycle_counter.sv                   \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_decode.sv                          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_decode_thermometer.sv              \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_decode_with_v.sv                   \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_defines.sv                         \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_async_reset.sv                 \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_chain.sv                      \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_en_bypass.sv  \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_en_segmented.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_en.sv           \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_gatestack.sv     \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_negedge_reset.sv  \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_en_bypass.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_en.sv         \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_set_clear.sv   \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset.sv              \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_var_en.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff.sv                      \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_dlatch.sv                    \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_edge_detect.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_encode_one_hot.sv              \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_expand_bitmask.sv               \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_gray_to_binary.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_hash_bank_reverse.sv             \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_hash_bank.sv                     \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_hashing_ipoly.sv                 \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_icg_neg.sv                       \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_icg_pos.sv                       \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_idiv_iterative_controller.sv     \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_idiv_iterative.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_idiv_unsigned_recip.sv           \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_idiv_unsigned_recip.svh          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_id_pool_dealloc_alloc_one_hot.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_id_pool.sv                       \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_id_pool_with_reserve.sv          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_imod_range.sv                    \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_imul_iterative.sv                \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_inv.sv                           \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_less_than.sv                     \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_level_shift_up_down_sink.sv      \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_level_shift_up_down_source.sv    \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_lfsr.sv                          \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_locking_arb_fixed.sv             \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_lru_pseudo_tree_backup.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_lru_pseudo_tree_decode.sv        \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_lru_pseudo_tree_encode.sv         \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul_add_unsigned.sv              \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul_array_row.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul_array.sv     \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul_pipelined.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul_synth.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux2_gatestack.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_bitwise.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_butterfly.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_muxi2_gatestack.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_one_hot.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_segmented.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_nand.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_nor2.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_nor3.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_pg_tree.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_popcount.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_priority_encode_one_hot_out.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_priority_encode.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_reduce_segmented.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_reduce.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_rotate_left.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_rotate_right.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_round_robin_arb.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_scan.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_scheduler_dataflow.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_scheduler_resource.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_scoreboard_dealloc_alloc.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_sparse_to_dense_boolean.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_strobe.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_swap.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_thermometer_count.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_tiehi.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_tielo.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_transpose_reduce.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_transpose.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_unconcentrate_static.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_wait_after_reset.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_wait_cycles.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_xnor.sv \
#   $(bp_dir)/external/basejump_stl/bsg_misc/bsg_xor.sv \
  
BLACK_PARROT_VSRCS = \
$(bp_dir)/external/basejump_stl/bsg_async/bsg_async_fifo.sv \
$(bp_dir)/external/basejump_stl/bsg_async/bsg_launch_sync_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_async/bsg_sync_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_async/bsg_async_ptr_gray.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_dma.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_dma_to_wormhole.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_miss.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_decode.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_sbuf.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_tbuf.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_cache_buffer_queue.sv \
$(bp_dir)/external/basejump_stl/bsg_cache/bsg_wormhole_to_cache_dma_fanout.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_channel_tunnel.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_channel_tunnel_in.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_channel_tunnel_out.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_1_to_n_tagged_fifo.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_1_to_n_tagged.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_1r1w_large.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_1r1w_pseudo_large.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_1r1w_small.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_1r1w_small_unhardened.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_1r1w_small_hardened.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_1rw_large.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_fifo_tracker.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_flow_counter.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_one_fifo.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_parallel_in_serial_out.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_parallel_in_serial_out_dynamic.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_parallel_in_serial_out_passthrough.sv \
$(bp_dir)/bp_common/src/v/bsg_parallel_in_serial_out_passthrough_dynamic.sv \
$(bp_dir)/bp_common/src/v/bsg_serial_in_parallel_out_passthrough_dynamic.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_round_robin_1_to_n.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_round_robin_2_to_2.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_round_robin_n_to_1.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_serial_in_parallel_out.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_serial_in_parallel_out_dynamic.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_serial_in_parallel_out_full.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_serial_in_parallel_out_passthrough.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_shift_reg.sv \
$(bp_dir)/external/basejump_stl/bsg_dataflow/bsg_two_fifo.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_cam_1r1w_replacement.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_cam_1r1w_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_cam_1r1w_tag_array.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1r1w.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1r1w_one_hot.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1r1w_sync_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1r1w_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1r1w_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1rw_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1rw_sync_mask_write_bit.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1rw_sync_mask_write_bit_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1rw_sync_mask_write_byte.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1rw_sync_mask_write_byte_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_1rw_sync_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_2r1w_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_2r1w_sync_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_3r1w_sync.sv \
$(bp_dir)/external/basejump_stl/bsg_mem/bsg_mem_3r1w_sync_synth.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_adder_cin.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_adder_one_hot.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_adder_ripple_carry.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_arb_fixed.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_arb_round_robin.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_array_concentrate_static.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_buf.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_buf_ctrl.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_circular_ptr.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_concentrate_static.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counting_leading_zeros.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clear_up.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clear_up_one_hot.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_clock_downsample.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_set_down.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_set_en.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_up_down.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_counter_up_down_variable.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_crossbar_o_by_i.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_crossbar_control_locking_o_by_i.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_cycle_counter.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_decode.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_decode_with_v.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_chain.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_en.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_en_bypass.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_en.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_en_bypass.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dff_reset_set_clear.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_dlatch.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_edge_detect.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_encode_one_hot.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_expand_bitmask.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_gray_to_binary.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_hash_bank.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_hash_bank_reverse.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_imul_iterative.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_idiv_iterative.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_idiv_iterative_controller.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_lfsr.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_lru_pseudo_tree_backup.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_lru_pseudo_tree_decode.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_lru_pseudo_tree_encode.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_locking_arb_fixed.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_mul_add_unsigned.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_bitwise.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_butterfly.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_one_hot.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_mux_segmented.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_muxi2_gatestack.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_nor2.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_nor3.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_nand.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_popcount.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_priority_encode.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_priority_encode_one_hot_out.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_reduce.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_reduce_segmented.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_rotate_left.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_rotate_right.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_round_robin_arb.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_scan.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_strobe.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_swap.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_thermometer_count.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_transpose.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_unconcentrate_static.sv \
$(bp_dir)/external/basejump_stl/bsg_misc/bsg_xnor.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_mesh_stitch.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_noc_repeater_node.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_concentrator.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_concentrator_in.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_concentrator_out.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router_adapter_in.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router_adapter_out.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router_decoder_dor.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router_input_control.sv \
$(bp_dir)/external/basejump_stl/bsg_noc/bsg_wormhole_router_output_control.sv \
$(bp_dir)/external/HardFloat/source/compareRecFN.v \
$(bp_dir)/external/HardFloat/source/divSqrtRecFN.v \
$(bp_dir)/external/HardFloat/source/divSqrtRecFN_medium.v \
$(bp_dir)/external/HardFloat/source/divSqrtRecFN_small.v \
$(bp_dir)/external/HardFloat/source/fNToRecFN.v \
$(bp_dir)/external/HardFloat/source/HardFloat_primitives.v \
$(bp_dir)/external/HardFloat/source/HardFloat_rawFN.v \
$(bp_dir)/external/HardFloat/source/iNToRecFN.v \
$(bp_dir)/external/HardFloat/source/isSigNaNRecFN.v \
$(bp_dir)/external/HardFloat/source/mulAddRecFN.v \
$(bp_dir)/external/HardFloat/source/mulRecFN.v \
$(bp_dir)/external/HardFloat/source/recFNToFN.v \
$(bp_dir)/external/HardFloat/source/recFNToIN.v \
$(bp_dir)/external/HardFloat/source/recFNToRecFN.v \
$(bp_dir)/external/HardFloat/source/RISCV/HardFloat_specialize.v \
$(bp_dir)/bp_common/src/v/bsg_bus_pack.sv \
$(bp_dir)/bp_common/src/v/bp_mmu.sv \
$(bp_dir)/bp_common/src/v/bp_pma.sv \
$(bp_dir)/bp_common/src/v/bp_tlb.sv \
$(bp_dir)/bp_be/src/v/bp_be_top.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_calculator_top.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_csr.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_fp_box.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_fp_rebox.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_fp_unbox.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_int_unbox.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_int_box.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_pipe_int.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_pipe_aux.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_pipe_fma.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_pipe_long.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_pipe_mem.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_pipe_sys.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_ptw.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_rec_to_raw.sv \
$(bp_dir)/bp_be/src/v/bp_be_calculator/bp_be_reservation.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_cmd_queue.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_detector.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_director.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_expander.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_instr_decoder.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_issue_queue.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_regfile.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_scheduler.sv \
$(bp_dir)/bp_be/src/v/bp_be_checker/bp_be_scoreboard.sv \
$(bp_dir)/bp_be/src/v/bp_be_dcache/bp_be_dcache.sv \
$(bp_dir)/bp_be/src/v/bp_be_dcache/bp_be_dcache_decoder.sv \
$(bp_dir)/bp_be/src/v/bp_be_dcache/bp_be_dcache_wbuf.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_ras.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_bht.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_btb.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_controller.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_icache.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_scan.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_pc_gen.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_realigner.sv \
$(bp_dir)/bp_fe/src/v/bp_fe_top.sv \
$(bp_dir)/bp_me/src/v/lce/bp_lce.sv \
$(bp_dir)/bp_me/src/v/lce/bp_lce_req.sv \
$(bp_dir)/bp_me/src/v/lce/bp_lce_cmd.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_bedrock_register.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_cache_controller.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_dram_hash_decode.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_dram_hash_encode.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_cache_slice.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_cfg_slice.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_clint_slice.sv \
$(bp_dir)/bp_me/src/v/dev/bp_me_loopback.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_alu.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_arbitrate.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_branch.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_dir.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_dir_lru_extract.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_dir_segment.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_dir_tag_checker.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_gad.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_inst_decode.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_inst_predecode.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_inst_ram.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_inst_stall.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_msg.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_pending_bits.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_pma.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_reg.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_spec_bits.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_src_sel.sv \
$(bp_dir)/bp_me/src/v/cce/bp_io_cce.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_fsm.sv \
$(bp_dir)/bp_me/src/v/cce/bp_cce_wrapper.sv \
$(bp_dir)/bp_me/src/v/cce/bp_bedrock_size_to_len.sv \
$(bp_dir)/bp_me/src/v/cce/bp_uce.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_addr_to_cce_id.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_cce_id_to_cord.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_cord_to_id.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_lce_id_to_cord.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_stream_pump.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_stream_pump_in.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_stream_pump_out.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_stream_pump_control.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_stream_to_wormhole.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_wormhole_header_encode.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_wormhole_to_stream.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_wormhole_stream_control.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_xbar_stream.sv \
$(bp_dir)/bp_me/src/v/network/bp_me_stream_gearbox.sv \
$(bp_dir)/bp_top/src/v/bp_nd_socket.sv \
$(bp_dir)/bp_top/src/v/bp_cacc_vdp.sv \
$(bp_dir)/bp_top/src/v/bp_cacc_tile.sv \
$(bp_dir)/bp_top/src/v/bp_cacc_tile_node.sv \
$(bp_dir)/bp_top/src/v/bp_cacc_complex.sv \
$(bp_dir)/bp_top/src/v/bp_sacc_vdp.sv \
$(bp_dir)/bp_top/src/v/bp_sacc_scratchpad.sv \
$(bp_dir)/bp_top/src/v/bp_sacc_tile.sv \
$(bp_dir)/bp_top/src/v/bp_sacc_tile_node.sv \
$(bp_dir)/bp_top/src/v/bp_sacc_complex.sv \
$(bp_dir)/bp_top/src/v/bp_core.sv \
$(bp_dir)/bp_top/src/v/bp_core_lite.sv \
$(bp_dir)/bp_top/src/v/bp_core_minimal.sv \
$(bp_dir)/bp_top/src/v/bp_core_complex.sv \
$(bp_dir)/bp_top/src/v/bp_l2e_tile.sv \
$(bp_dir)/bp_top/src/v/bp_l2e_tile_node.sv \
$(bp_dir)/bp_top/src/v/bp_io_complex.sv \
$(bp_dir)/bp_top/src/v/bp_io_link_to_lce.sv \
$(bp_dir)/bp_top/src/v/bp_io_tile.sv \
$(bp_dir)/bp_top/src/v/bp_io_tile_node.sv \
$(bp_dir)/bp_top/src/v/bp_mem_complex.sv \
$(bp_dir)/bp_top/src/v/bp_multicore.sv \
$(bp_dir)/bp_top/src/v/bp_unicore.sv \
$(bp_dir)/bp_top/src/v/bp_unicore_lite.sv \
$(bp_dir)/bp_top/src/v/bp_core_tile.sv \
$(bp_dir)/bp_top/src/v/bp_core_tile_node.sv \
$(bp_dir)/bp_top/src/v/bp_processor.sv \
$(bp_dir)/bp_common/src/v/bsg_async_noc_link.sv \
$(bp_dir)/bp_common/src/v/bsg_dff_sync_read.sv \
$(bp_dir)/bp_common/src/v/bsg_fifo_1r1w_edge.sv \

BLACK_PARROT_WRAPPER = \
	$(vsrc_dir)/BlackParrotBlackbox.sv

ALL_VSRCS = $(BLACK_PARROT_PKGS) $(BLACK_PARROT_VSRCS) $(BLACK_PARROT_WRAPPER)

lookup_dirs = $(shell find -L $(bp_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
INC_DIR_NAMES ?= vendor/lowrisc_ip/ip/prim/rtl formal/data_ind_timing vendor/lowrisc_ip/dv/sv/dv_utils
#INC_DIRS ?= $(foreach dir_name,$(INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
EXTRA_PREPROC_DEFINES ?=
PREPROC_DEFINES ?= \
	VERILATOR \
	$(EXTRA_PREPROC_DEFINES)

PREPROC_SCRIPT = $(base_dir)/scripts/insert-includes.py

$(PREPROC_VERILOG): $(ALL_VSRCS)
	mkdir -p $(dir $(PREPROC_VERILOG))
	$(foreach def,$(PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(ALL_VSRCS) undef.v > combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(INC_DIRS)
	sed -i '/.*prim_xilinx_clock_gating/,/;/c\    // Remove when upgrading verilator version' $@ # Remove FPGA module so Ibex works in Verilator v4.304
	rm -rf combined.sv def.v undef.v

clean:
	rm -rf $(PREPROC_VERILOG)