package chipyard

import chisel3._

import org.chipsalliance.cde.config.Config
import freechips.rocketchip.subsystem.WithBufferlessBroadcastHub


class BlackParrotConfig extends Config(
  new bp.WithNBlackParrotCores(1) ++
  new chipyard.config.WithInclusiveCacheWriteBytes(4) ++
  new chipyard.config.AbstractConfig)

