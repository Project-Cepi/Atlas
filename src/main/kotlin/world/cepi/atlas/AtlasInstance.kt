package world.cepi.atlas

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.tag.Tag
import world.cepi.atlas.world.ChunkType
import world.cepi.atlas.world.generator.Generator
import world.cepi.atlas.world.loader.Loader
import world.cepi.kstom.Manager
import world.cepi.kstom.serializer.PositionSerializer
import java.util.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.full.primaryConstructor

/**
 * Represents an instance that is owned by the Atlas loader.
 * Essentially a wrapper class for Minestom instances.
 */
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
        @Serializable(with = PositionSerializer::class)
        var spawn: Pos = Pos(0.0, 50.0, 0.0),
        /** The time rate of the instance */
        val timeRate: Int = 0
) {

        // DO NOT remove the lateinit. This is due to Transient breaking without the keyword.
        @Transient
        lateinit var instanceContainer: InstanceContainer

        init {
                val instanceManager = Manager.instance

                instanceContainer = instanceManager.createInstanceContainer()

                instanceContainer.chunkGenerator = generator.generator.invoke(name)
                instanceContainer.enableAutoChunkLoad(autoChunkLoad)

                instanceContainer.chunkLoader = loader.loader.java
                        .getDeclaredConstructor(Path::class.java)
                        .newInstance(Path.of("./atlas/$name"))

                instanceContainer.timeRate = timeRate

                instances[name] = this

                instanceContainer.setTag(Tag.Double("spawnX"), spawn.x())
                instanceContainer.setTag(Tag.Double("spawnY"), spawn.y())
                instanceContainer.setTag(Tag.Double("spawnZ"), spawn.z())

                instanceContainer.setTag(tag, name)

                update()
        }

        /** Unregister an atlas instance. */
        fun unregister() {
                instances.remove(name)

                Manager.instance.unregisterInstance(this.instanceContainer)

                update()
        }

        companion object {

                val tag = Tag.String("atlas")

                /** The AtlasInstance list serializer. */
                private val serializer: KSerializer<Map<String, AtlasInstance>> = MapSerializer(String.serializer(), serializer())

                /** A file representation of the instance configuration for atlas*/
                private val instanceFile = Path.of("./atlas/atlas.json")

                /** The folder where all atlas instances are contained*/
                private val instanceFolder = Path.of("./atlas")

                /** The current cache of the AtlasInstances. */
                val instances: MutableMap<String, AtlasInstance> = mutableMapOf()

                /** Update the config file of Atlas. Refer to [instanceFile] for more information*/
                fun update() {
                        if (!instanceFile.exists())
                                instanceFile.createFile()
                        instanceFile.writeText(Json.encodeToString(serializer, instances))
                }

                /** Load all instances from the [instanceFile] configuration. */
                fun loadInstances() {
                        instances.putAll(Json.decodeFromString(serializer, instanceFile.readText()))
                }

                init {
                        instanceFolder.createDirectories()
                        if (!instanceFile.exists()) update()
                }

        }
}

/** Check if a Minestom instance is registered as an atlas instance. */
val Instance.isAtlas: Boolean get() =
        AtlasInstance.instances.contains(this.getTag(AtlasInstance.tag) ?: false)

/** Gets an Atlas Instance, as long as the Minestom Instance is one. */
val Instance.asAtlas: AtlasInstance? get() =
        AtlasInstance.instances[this.getTag(AtlasInstance.tag)]
