package world.cepi.atlas

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.minestom.server.MinecraftServer
import world.cepi.atlas.world.ChunkType
import world.cepi.atlas.world.generator.Generator
import world.cepi.atlas.world.loader.Loader
import java.util.*
import java.io.File

/** Represents an instance that is owned by the Atlas loader*/
@Serializable
data class AtlasInstance(
        /** The human-readable name of the world. Solely used for command shortening purposes */
        val name: String = UUID.randomUUID().toString(),
        /** How unknown/non known chunks should be generated */
        val generator: Generator = Generator.FLAT,
        /** How a world is passed to the player. */
        val chunkType: ChunkType = ChunkType.PASSED,
        /** If the chunks should save to disk. */
        val shouldSave: Boolean = false,
        /** How a world should be represented in a file. */
        val loader: Loader = Loader.MINESTOM,
        /** If the instance should automatically load chunks */
        val autoChunkLoad: Boolean = true
) {

        fun load() {
                val instanceManager = MinecraftServer.getInstanceManager()

                val instance = instanceManager.createInstanceContainer()

                instance.chunkGenerator = generator.generator.invoke("")
                instance.enableAutoChunkLoad(autoChunkLoad)
        }

        companion object {

                private val serilalizer: KSerializer<List<AtlasInstance>> = ListSerializer(AtlasInstance.serializer())

                val instanceFile = File("./instances/instances.json")
                val instanceFolder = File("./instances")

                private val instances: MutableList<AtlasInstance> = mutableListOf()

                fun add(instance: AtlasInstance) {
                        instances.add(instance)
                        update()
                }

                fun remove(instance: AtlasInstance) {
                        instances.remove(instance)
                        update()
                }

                fun update() {
                        if (!instanceFile.exists())
                                instanceFile.createNewFile()
                        instanceFile.writeText(Json.encodeToString(serilalizer, instances))
                }

        }
}