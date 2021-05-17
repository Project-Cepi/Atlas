package world.cepi.atlas.world.generator

import net.minestom.server.instance.*
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.registry.Registries
import net.minestom.server.world.biomes.Biome
import org.jglrxavpok.hephaistos.mca.*
import org.slf4j.LoggerFactory
import world.cepi.kstom.Manager
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AnvilChunkGenerator(private val regionFolder: String) : ChunkGenerator {

    private val defaultBiome = Biome.PLAINS

    private val alreadyLoaded = ConcurrentHashMap<String, RegionFile?>()

    private fun loadMCA(batch: ChunkBatch, chunkX: Int, chunkZ: Int) {

        val mcaFile = getMCAFile(chunkX, chunkZ) ?: return
        val fileChunk = mcaFile.getChunk(chunkX, chunkZ) ?: return

        loadBlocks(batch, fileChunk)

    }

    private fun getMCAFile(chunkX: Int, chunkZ: Int): RegionFile? {
        val regionX = chunkX.chunkToRegion()
        val regionZ = chunkZ.chunkToRegion()
        return alreadyLoaded.computeIfAbsent(RegionFile.createFileName(regionX, regionZ)) { n: String ->
            try {
                val regionFile = File(regionFolder, n)
                if (!regionFile.exists()) {
                    return@computeIfAbsent null
                }
                return@computeIfAbsent RegionFile(RandomAccessFile(regionFile, "r"), regionX, regionZ)
            } catch (e: IOException) {
                Manager.exception.handleException(e)
                return@computeIfAbsent null
            } catch (e: AnvilException) {
                Manager.exception.handleException(e)
                return@computeIfAbsent null
            }
        }
    }

    private fun loadBlocks(batch: ChunkBatch, fileChunk: ChunkColumn) {
        for (x in 0 until Chunk.CHUNK_SIZE_X) for (z in 0 until Chunk.CHUNK_SIZE_Z) for (y in 0 until Chunk.CHUNK_SIZE_Y) {
            try {

                val (name, properties) = fileChunk.getBlockState(x, y, z)
                val registryBlock = Registries.getBlock(name)

                if (properties.isNotEmpty()) {

                    val propertiesArray = properties
                            .map { (key, value) ->
                                "$key=${value.replace("\"", "")}" }
                            .sorted()

                    val block = registryBlock.withProperties(*propertiesArray.toTypedArray())

                    batch.setBlockStateId(x, y, z, block)

                } else {

                    batch.setBlock(x, y, z, registryBlock)

                }
            } catch (e: Exception) {
                Manager.exception.handleException(e)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AnvilChunkGenerator::class.java)
    }

    override fun generateChunkData(batch: ChunkBatch, chunkX: Int, chunkZ: Int) {
        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ)

        try {
            loadMCA(batch, chunkX, chunkZ)
        } catch (e: Exception) {
            Manager.exception.handleException(e)
        }

    }

    override fun fillBiomes(biomes: Array<out Biome>, chunkX: Int, chunkZ: Int) {
        // TODO handle biomes
        Arrays.fill(biomes, defaultBiome)
    }

    override fun getPopulators(): MutableList<ChunkPopulator> = mutableListOf()
}