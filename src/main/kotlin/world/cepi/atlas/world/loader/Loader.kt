package world.cepi.atlas.world.loader

import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.MinestomBasicChunkLoader
import kotlin.reflect.KClass

/** The loaders and the data storage method they use. */
enum class Loader(val loader: KClass<out IChunkLoader>, val loaderType: LoaderType) {

    /** Loads the anvil based format*/
    ANVIL(AnvilChunkLoader::class, LoaderType.PHYSICAL),

    /** Loads a world from the minestom data container. */
    MINESTOM(MinestomBasicChunkLoader::class, LoaderType.MINESTOM)

}