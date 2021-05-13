package world.cepi.atlas.world.loader

import net.minestom.server.MinecraftServer
import net.minestom.server.data.Data
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.DynamicChunk
import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.Instance
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.registry.Registries
import net.minestom.server.utils.BlockPosition
import net.minestom.server.utils.NamespaceID
import net.minestom.server.utils.chunk.ChunkCallback
import net.minestom.server.utils.chunk.ChunkUtils
import net.minestom.server.world.biomes.Biome
import org.jglrxavpok.hephaistos.mca.*
import org.jglrxavpok.hephaistos.mca.ChunkColumn.GenerationStatus
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTTypes.TAG_Compound
import org.slf4j.LoggerFactory
import world.cepi.kstom.Manager
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ImportOnlyAnvilChunkLoader(private val regionFolder: String) : IChunkLoader {

    private val voidBiome: Biome =
            Manager.biome.getByName(NamespaceID.from("minecraft:the_void")) ?: Biome.PLAINS

    private val alreadyLoaded = ConcurrentHashMap<String, RegionFile?>()

    override fun loadChunk(instance: Instance, chunkX: Int, chunkZ: Int, callback: ChunkCallback?): Boolean {

        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ)

        try {
            val chunk = loadMCA(instance, chunkX, chunkZ, callback)
            callback?.accept(chunk)
            return chunk != null
        } catch (e: Exception) {
            Manager.exception.handleException(e)
        }

        return false
    }

    private fun loadMCA(instance: Instance, chunkX: Int, chunkZ: Int, callback: ChunkCallback?): Chunk? {

        val mcaFile = getMCAFile(chunkX, chunkZ)
        val fileChunk = mcaFile?.getChunk(chunkX, chunkZ)

        if (fileChunk != null) {
            val biomes = arrayOfNulls<Biome>(Chunk.BIOME_COUNT)
            if (fileChunk.generationStatus > GenerationStatus.Biomes) {
                val fileChunkBiomes = fileChunk.biomes ?: return null
                for (i in fileChunkBiomes.indices) {
                    val id = fileChunkBiomes[i]
                    biomes[i] = MinecraftServer.getBiomeManager().getById(id) ?: voidBiome
                }
            } else
                Arrays.fill(biomes, voidBiome)


            val loadedChunk: Chunk = DynamicChunk(instance, biomes, chunkX, chunkZ)
            val batch = ChunkBatch()
            loadBlocks(instance, chunkX, chunkZ, batch, fileChunk)

            batch.unsafeApply(instance, loadedChunk) {
                loadTileEntities(it, chunkX, chunkZ, fileChunk)
                callback?.accept(it)
            }

            return loadedChunk
        }

        return null
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
                return@computeIfAbsent RegionFile(RandomAccessFile(regionFile, "rw"), regionX, regionZ)
            } catch (e: IOException) {
                Manager.exception.handleException(e)
                return@computeIfAbsent null
            } catch (e: AnvilException) {
                Manager.exception.handleException(e)
                return@computeIfAbsent null
            }
        }
    }

    private fun loadTileEntities(loadedChunk: Chunk, chunkX: Int, chunkZ: Int, fileChunk: ChunkColumn) {
        val pos = BlockPosition(0, 0, 0)
        for (te in fileChunk.tileEntities) {
            val x = (te.getInt("x") ?: return) + chunkX * 16
            val y = te.getInt("y") ?: return
            val z = (te.getInt("z") ?: return) + chunkZ * 16
            val block = loadedChunk.getCustomBlock(x, y, z)
            if (block != null) {
                pos.x = x
                pos.y = y
                pos.z = z
                val data = loadedChunk.getBlockData(ChunkUtils.getBlockIndex(x, y, z))
                loadedChunk.setBlockData(x, y, z, data)
            }
        }
    }

    private fun loadBlocks(instance: Instance, chunkX: Int, chunkZ: Int, batch: ChunkBatch, fileChunk: ChunkColumn) {
        for (x in 0 until Chunk.CHUNK_SIZE_X) {
            for (z in 0 until Chunk.CHUNK_SIZE_Z) {
                for (y in 0 until Chunk.CHUNK_SIZE_Y) {
                    try {

                        val (name, properties) = fileChunk.getBlockState(x, y, z)
                        val registryBlock = Registries.getBlock(name)
                        var customBlockId: Short = 0
                        var data: Data? = null
                        val customBlock = Manager.block.getCustomBlock(registryBlock.blockId)

                        if (customBlock != null) {
                            customBlockId = registryBlock.blockId
                            data = customBlock.createData(instance, BlockPosition(x + chunkX * 16, y, z + chunkZ * 16), null)
                        }

                        if (properties.isNotEmpty()) {

                            val propertiesArray = properties
                                    .map { (key, value ) ->
                                        key + "=" + value.replace("\"", "") }
                                    .sorted()

                            val block = registryBlock.withProperties(*propertiesArray.toTypedArray())

                            if (customBlock != null)
                                batch.setSeparateBlocks(x, y, z, block, customBlockId, data)
                            else
                                batch.setBlockStateId(x, y, z, block)

                        } else {

                            if (customBlock != null)
                                batch.setSeparateBlocks(x, y, z, registryBlock.blockId, customBlockId, data)
                            else
                                batch.setBlock(x, y, z, registryBlock)

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // TODO: find a way to unload MCAFiles when an entire region is unloaded
    override fun saveChunk(chunk: Chunk, callback: Runnable?) {
        callback?.run()
    }
    override fun supportsParallelLoading() = true

    override fun supportsParallelSaving() = true

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ImportOnlyAnvilChunkLoader::class.java)
    }
}