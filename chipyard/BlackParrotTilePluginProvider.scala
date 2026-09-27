package chipyard.config

import bp.BlackParrotTileAttachParams
import freechips.rocketchip.subsystem.HierarchicalElementPortParamsLike

class BlackParrotTilePluginProvider extends TilePluginProvider {

  override def tilePrefetchInjectors(make: (Int, HierarchicalElementPortParamsLike) => HierarchicalElementPortParamsLike) = Seq({
    case tp: BlackParrotTileAttachParams => tp.copy(crossingParams = tp.crossingParams.copy(
      master = make(tp.tileParams.tileId, tp.crossingParams.master)))
  })
}

