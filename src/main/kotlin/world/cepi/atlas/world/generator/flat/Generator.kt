package world.cepi.atlas.world.generator.flat

import net.minestom.server.instance.ChunkGenerator
import kotlin.reflect.KClass

enum class Generator(val generator: KClass<out ChunkGenerator>) {

    FLAT(Flat::class)

}