package world.cepi.atlas.world.loader

import net.minestom.server.instance.Chunk
import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.utils.binary.BinaryReader
import net.minestom.server.utils.chunk.ChunkCallback
import java.io.*

/**
 * Saves an instance to a folder
 *
 * @author Krystilize
 */
class NamedInstanceChunkLoader(private val name: String): IChunkLoader {
    override fun loadChunk(instance: Instance, chunkX: Int, chunkZ: Int, callback: ChunkCallback?): Boolean {
        File("Saves/Instances/$name/").mkdirs()
        return try {
            val `is` = FileInputStream("Saves/Instances/$name/chunk.$chunkX.$chunkZ.save")
            val ois = ObjectInputStream(`is`)
            val chunkData = ois.readObject() as ByteArray
            val chunk = (instance as InstanceContainer).chunkSupplier.createChunk(null, chunkX, chunkZ)
            val reader = BinaryReader(chunkData)
            chunk.readChunk(reader, callback)
            ois.close()
            true
        } catch (e: FileNotFoundException) {
            // Player's save file not found
            false
        } catch (e: IOException) {
            // Error in opening save file
            e.printStackTrace()
            false
        } catch (e: ClassNotFoundException) {
            // Error in loading save file
            e.printStackTrace()
            false
        }
    }

    override fun saveChunk(chunk: Chunk, callback: Runnable?) {
        val chunkX = chunk.chunkX
        val chunkZ = chunk.chunkZ
        val fileName = "instances/$name/chunk.$chunkX.$chunkZ.save"
        File("instances/$name/").mkdirs()
        try {
            val file = File(fileName)
            if (file.isFile)
                file.delete()
            file.createNewFile()
            val os = FileOutputStream(fileName)
            val oos = ObjectOutputStream(os)
            oos.writeObject(chunk.serializedData)
            oos.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    override fun supportsParallelSaving() = true
    override fun supportsParallelLoading() = true

    init {
        File("instances/$name/").mkdirs()
    }
}
