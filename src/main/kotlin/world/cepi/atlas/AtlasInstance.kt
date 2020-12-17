package world.cepi.atlas

import kotlinx.serialization.Serializable
import world.cepi.atlas.world.generator.Generator
import world.cepi.atlas.world.loader.Loader
import java.util.*

/** Represents an instance that is owned by the Atlas loader*/
@Serializable
data class AtlasInstance(
        /** The human-readable name of the world. Solely used for command shortening purposes */
        val name: String = UUID.randomUUID().toString(),
        /** How unknown/non known chunks should be generated */
        val generator: Generator = Generator.FLAT,
        /** How a world should save and load. If it doesn't have a loader, it wont save or load. */
        val loader: Loader? = Loader.MINESTOM)