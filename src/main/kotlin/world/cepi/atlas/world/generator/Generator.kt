package world.cepi.atlas.world.generator

import net.minestom.server.instance.ChunkGenerator
import world.cepi.atlas.world.generator.flat.FlatGenerator
import world.cepi.atlas.world.generator.void.VoidGenerator
import kotlin.reflect.KClass

enum class Generator(val generator: (String) -> ChunkGenerator) {

    FLAT({ FlatGenerator() }),
    VOID({ VoidGenerator() }),
    ANVIL({ file -> AnvilChunkGenerator("./atlas/$file") })

}