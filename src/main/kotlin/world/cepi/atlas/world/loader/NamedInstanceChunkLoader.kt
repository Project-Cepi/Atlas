package world.cepi.atlas.world.loader

import net.minestom.server.instance.Chunk
import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.utils.binary.BinaryReader
import net.minestom.server.utils.chunk.ChunkCallback
import world.cepi.kstom.Manager
import java.io.*

/**
 * Saves an instance to a folder
 *
 * @author Krystilize
 */
class NamedInstanceChunkLoader(private val regionFolder: String): IChunkLoader {
    override fun loadChunk(instance: Instance, chunkX: Int, chunkZ: Int, callback: ChunkCallback?): Boolean {
        File(regionFolder).mkdirs()
        return try {
            val `is` = FileInputStream("$regionFolder/chunk.$chunkX.$chunkZ.save")
            val ois = ObjectInputStream(`is`)
            val chunkData = ois.readObject() as ByteArray
            val chunk = (instance as InstanceContainer).chunkSupplier.createChunk(instance, null, chunkX, chunkZ)
            val reader = BinaryReader(chunkData)
            chunk.readChunk(reader, callback)
            ois.close()
            true
        } catch (e: FileNotFoundException) {
            // Player's save file not found
            false
        } catch (e: IOException) {
            // Error in opening save file
            Manager.exception.handleException(e)
            false
        } catch (e: ClassNotFoundException) {
            // Error in loading save file
            Manager.exception.handleException(e)
            false
        }
    }

    override fun saveChunk(chunk: Chunk, callback: Runnable?) {
        val chunkX = chunk.chunkX
        val chunkZ = chunk.chunkZ
        val fileName = "${regionFolder}chunk.$chunkX.$chunkZ.save"
        File(regionFolder).mkdirs()
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
            Manager.exception.handleException(e)
        } catch (e: IOException) {
            Manager.exception.handleException(e)
        }
    }

    override fun supportsParallelSaving() = true
    override fun supportsParallelLoading() = true
}
