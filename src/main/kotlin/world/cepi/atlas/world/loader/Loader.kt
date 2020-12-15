package world.cepi.atlas.world.loader

import net.minestom.server.instance.IChunkLoader
import kotlin.reflect.KClass

enum class Loader(val loader: KClass<out IChunkLoader>) {

    ANVIL(AnvilChunkLoader::class)

}