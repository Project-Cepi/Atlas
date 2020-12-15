package world.cepi.example.generator.loader

import net.minestom.server.instance.IChunkLoader
import world.cepi.example.generator.loader.AnvilChunkLoader
import kotlin.reflect.KClass

enum class Loader(val loader: KClass<out IChunkLoader>) {

    ANVIL(AnvilChunkLoader::class)

}