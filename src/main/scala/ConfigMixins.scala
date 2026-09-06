//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
// BlackParrot Tile Wrapper
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

package bp

import chisel3._
import chisel3.util.{log2Up}

import org.chipsalliance.cde.config.{Parameters, Config, Field}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.rocket._
import freechips.rocketchip.tile._

/**
 * Create multiple copies of a Black Parrot tile (and thus a core).
 * Override with the default mixins to control all params of the tiles.
 *
 * @param n amount of tiles to duplicate
 */
class WithNBlackParrotCores(n: Int = 1) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => {
    val prev = up(TilesLocated(InSubsystem), site)
    val idOffset = up(NumTiles)
    (0 until n).map { i =>
      BlackParrotTileAttachParams(
        tileParams = BlackParrotTileParams(tileId = i + idOffset),
        crossingParams = RocketCrossingParams()
      )
    } ++ prev
  }
  case SystemBusKey => up(SystemBusKey, site).copy(beatBytes = 4)
  case NumTiles => up(NumTiles) + n
})
