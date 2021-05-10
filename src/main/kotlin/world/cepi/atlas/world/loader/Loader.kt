package world.cepi.atlas.world.loader

import net.minestom.server.instance.IChunkLoader
import kotlin.reflect.KClass

/** The loaders and the data storage method they use. */
enum class Loader(val loader: KClass<out IChunkLoader>) {

    /** Loads the anvil based format*/
    ANVIL(ImportOnlyAnvilChunkLoader::class),

    /** Loads a world from the minestom data container. */
    MINESTOM(NamedInstanceChunkLoader::class),

    /** Doesnt attempt to load anything */
    FALSE(FalseChunkLoader::class)

}