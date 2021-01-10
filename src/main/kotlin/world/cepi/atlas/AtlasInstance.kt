package world.cepi.atlas

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.minestom.server.MinecraftServer
import net.minestom.server.data.DataImpl
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.utils.Position
import world.cepi.atlas.world.ChunkType
import world.cepi.atlas.world.generator.Generator
import world.cepi.atlas.world.loader.Loader
import java.util.*
import java.io.File
import kotlin.reflect.full.primaryConstructor

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
        val loader: Loader = Loader.FALSE,
        /** If the instance should automatically load chunks */
        val autoChunkLoad: Boolean = true,
        /** The spawn of the instance. */
        val spawn: KPosition = KPosition(0f, 50f, 0f)
) {

        // DO NOT remove the lateinit. This is due to Transient breaking without the keyword.
        @Transient
        lateinit var instanceContainer: InstanceContainer

        init {
                val instanceManager = MinecraftServer.getInstanceManager()

                instanceContainer = instanceManager.createInstanceContainer()

                instanceContainer.chunkGenerator = generator.generator.invoke("")
                instanceContainer.enableAutoChunkLoad(autoChunkLoad)

                instanceContainer.chunkLoader = loader.loader.primaryConstructor?.call("./atlas/$name")

                instances.add(this)

                if (instanceContainer.data == null) instanceContainer.data = DataImpl()
                instanceContainer.data!!.set("spawn", spawn.asPosition)
                instanceContainer.data!!.set("atlas", this)

                update()
        }

        /** Unregister an atlas instance. */
        fun unregister() {
                instances.remove(this)

                MinecraftServer.getInstanceManager().unregisterInstance(this.instanceContainer)

                update()
        }

        companion object {

                private val serializer: KSerializer<List<AtlasInstance>> = ListSerializer(serializer())

                private val instanceFile = File("./atlas/atlas.json")
                private val instanceFolder = File("./atlas")

                private val instances: MutableList<AtlasInstance> = mutableListOf()

                fun update() {
                        if (!instanceFile.exists())
                                instanceFile.createNewFile()
                        instanceFile.writeText(Json.encodeToString(serializer, instances))
                }

                fun loadInstances() {
                        Json.decodeFromString(serializer, instanceFile.readText())
                }

                init {
                        instanceFolder.mkdirs()
                }

        }
}

val Instance.isAtlas: Boolean get() = run {
        if (this.data == null) true
        else this.data!!.get<AtlasInstance>("atlas") != null
}

val Instance.asAtlas: AtlasInstance? get() = run {
        if (this.data == null) null
        else this.data?.get<AtlasInstance>("atlas")
}