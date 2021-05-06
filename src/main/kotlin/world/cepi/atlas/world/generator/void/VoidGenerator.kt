package world.cepi.atlas.world.generator.void

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.ChunkGenerator
import net.minestom.server.instance.ChunkPopulator
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.world.biomes.Biome
import world.cepi.kstom.Manager
import java.util.*

/** Generates a completely empty void world. */
class VoidGenerator(
    /** List of chunk populators for the generator */
    private val chunkPopulators: List<ChunkPopulator> = listOf()
): ChunkGenerator {
    override fun generateChunkData(batch: ChunkBatch, chunkX: Int, chunkZ: Int) {
        batch.setBlock(0, 0, 0, Block.AIR)
    }

    override fun fillBiomes(biomes: Array<out Biome>, chunkX: Int, chunkZ: Int) {
        Arrays.fill(biomes, Manager.biome.getById(0))
    }

    override fun getPopulators(): MutableList<ChunkPopulator> = chunkPopulators.toMutableList()

}