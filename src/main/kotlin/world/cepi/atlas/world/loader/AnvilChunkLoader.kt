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
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class AnvilChunkLoader(private val regionFolder: String) : IChunkLoader {

    private val voidBiome: Biome =
            MinecraftServer.getBiomeManager().getByName(NamespaceID.from("minecraft:the_void")) ?: Biome.PLAINS

    private val alreadyLoaded = ConcurrentHashMap<String, RegionFile?>()

    override fun loadChunk(instance: Instance, chunkX: Int, chunkZ: Int, callback: ChunkCallback?): Boolean {

        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ)

        try {
            val chunk = loadMCA(instance, chunkX, chunkZ, callback)
            return chunk != null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    private fun loadMCA(instance: Instance, chunkX: Int, chunkZ: Int, callback: ChunkCallback?): Chunk? {

        val mcaFile = getMCAFile(chunkX, chunkZ)
        val fileChunk = mcaFile!!.getChunk(chunkX, chunkZ)

        if (fileChunk != null) {
            val biomes = arrayOfNulls<Biome>(Chunk.BIOME_COUNT)
            if (fileChunk.generationStatus > GenerationStatus.Biomes) {
                val fileChunkBiomes = fileChunk.biomes
                for (i in fileChunkBiomes!!.indices) {
                    val id = fileChunkBiomes[i]
                    biomes[i] = MinecraftServer.getBiomeManager().getById(id) ?: voidBiome
                }
            } else
                Arrays.fill(biomes, voidBiome)


            val loadedChunk: Chunk = DynamicChunk(biomes, chunkX, chunkZ)
            val batch = instance.createChunkBatch(loadedChunk)

            loadBlocks(instance, chunkX, chunkZ, batch, fileChunk)
            batch.unsafeFlush { c: Chunk ->
                loadTileEntities(c, chunkX, chunkZ, fileChunk)
                callback?.accept(c)
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
                e.printStackTrace()
                return@computeIfAbsent null
            } catch (e: AnvilException) {
                e.printStackTrace()
                return@computeIfAbsent null
            }
        }
    }

    private fun loadTileEntities(loadedChunk: Chunk, chunkX: Int, chunkZ: Int, fileChunk: ChunkColumn) {
        val pos = BlockPosition(0, 0, 0)
        for (te in fileChunk.tileEntities) {
            val x = te.getInt("x")!! + chunkX * 16
            val y = te.getInt("y")!!
            val z = te.getInt("z")!! + chunkZ * 16
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
                        val customBlock = MinecraftServer.getBlockManager().getCustomBlock(registryBlock.blockId)

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
        with(chunk) {
            var mcaFile: RegionFile?
            synchronized(alreadyLoaded) {
                mcaFile = getMCAFile(chunkX, chunkZ)
                if (mcaFile == null) {
                    val regionX = chunkX.chunkToRegion()
                    val regionZ = chunkZ.chunkToRegion()
                    val n = RegionFile.createFileName(regionX, regionZ)
                    val regionFile = File(regionFolder, n)
                    try {
                        if (!regionFile.exists()) {
                            if (!regionFile.parentFile.exists()) {
                                regionFile.parentFile.mkdirs()
                            }
                            regionFile.createNewFile()
                        }
                        mcaFile = RegionFile(RandomAccessFile(regionFile, "rw"), regionX, regionZ)
                        alreadyLoaded[n] = mcaFile!!
                    } catch (e: AnvilException) {
                        LOGGER.error("Failed to save chunk $chunkX, $chunkZ", e)
                        e.printStackTrace()
                        return
                    } catch (e: IOException) {
                        LOGGER.error("Failed to save chunk $chunkX, $chunkZ", e)
                        e.printStackTrace()
                        return
                    }
                }
            }
            val biomes = IntArray(Chunk.BIOME_COUNT)
            for (i in biomes.indices) {
                val biome = chunk.biomes[i] ?: voidBiome
                biomes[i] = biome.id
            }
            val column = try {
                mcaFile!!.getOrCreateChunk(chunkX, chunkZ)
            } catch (e: Exception) {
                LOGGER.error("Failed to save chunk $chunkX, $chunkZ", e)
                e.printStackTrace()
                return
            }

            save(chunk, column)
            try {
                LOGGER.debug("Attempt saving at {} {}", chunkX, chunkZ)
                mcaFile!!.writeColumn(column)
            } catch (e: Exception) {
                LOGGER.error("Failed to save chunk $chunkX, $chunkZ", e)
                e.printStackTrace()
                return
            }

            callback?.run()
        }
    }

    private fun saveTileEntities(chunk: Chunk, fileChunk: ChunkColumn) {
        val tileEntities = NBTList<NBTCompound>(TAG_Compound)
        val position = BlockPosition(0, 0, 0)
        for (index in chunk.blockEntities) {
            val x = ChunkUtils.blockIndexToChunkPositionX(index).toInt()
            val y = ChunkUtils.blockIndexToChunkPositionY(index).toInt()
            val z = ChunkUtils.blockIndexToChunkPositionZ(index).toInt()
            position.x = x
            position.y = y
            position.z = z
            val customBlock = chunk.getCustomBlock(x, y, z)
            val nbt = NBTCompound()
            nbt.setInt("x", x)
            nbt.setInt("y", y)
            nbt.setInt("z", z)
            nbt.setByte("keepPacked", 0.toByte())
            val block = Block.fromStateId(customBlock!!.defaultBlockStateId)
            val data = chunk.getBlockData(ChunkUtils.getBlockIndex(x, y, z))
            customBlock.writeBlockEntity(position, data, nbt)
            if (block.hasBlockEntity()) {
                nbt.setString("id", block.blockEntityName.toString())
                tileEntities.add(nbt)
            } else
                LOGGER.warn("Tried to save block entity for a block which is not a block entity? Block is {} at {},{},{}", customBlock, x, y, z)
        }
        fileChunk.tileEntities = tileEntities
    }

    private fun save(chunk: Chunk, chunkColumn: ChunkColumn) {
        chunkColumn.generationStatus = GenerationStatus.Full

        // TODO: other elements to save
        saveTileEntities(chunk, chunkColumn)
        for (x in 0 until Chunk.CHUNK_SIZE_X) {
            for (z in 0 until Chunk.CHUNK_SIZE_Z) {
                for (y in 0 until Chunk.CHUNK_SIZE_Y) {
                    val id = chunk.getBlockStateId(x, y, z)
                    // CustomBlock customBlock = chunk.getCustomBlock(x, y, z);
                    val block = Block.fromStateId(id)
                    val alt = block.getAlternative(id)
                    val properties = alt.createPropertiesMap()
                    val state = BlockState(block.getName(), properties)
                    chunkColumn.setBlockState(x, y, z, state)
                    val index = y shr 2 and 63 shl 4 or (z shr 2 and 3 shl 2) or (x shr 2 and 3) // https://wiki.vg/Chunk_Format#Biomes
                    val biome = chunk.biomes[index]
                    chunkColumn.setBiome(x, 0, z, biome.id)
                }
            }
        }
    }

    override fun supportsParallelLoading() = true

    override fun supportsParallelSaving() = true

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AnvilChunkLoader::class.java)
    }
}