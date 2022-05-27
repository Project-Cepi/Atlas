package world.cepi.atlas.world.generator

import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit

enum class Generator(val generator: (GenerationUnit, String) -> Unit) {

    FLAT({ unit, _ ->
        unit.modifier().fillHeight(0, 40, Block.DIRT)
        unit.modifier().fillHeight(41, 41, Block.GRASS_BLOCK)
    }),
    VOID({ unit, _ -> unit.modifier().setBlock(0, 0, 0, Block.AIR) })

}