package world.cepi.atlas

import kotlinx.serialization.Serializable
import net.minestom.server.utils.Position

/** Represents a serializable position for use in Minestom. */
@Serializable
class KPosition(
        /** The X coordinate of the position. */
        val x: Double,
        /** The Y coordinate of the position. */
        val y: Double,
        /** The Z coordinate of the position. */
        val z: Double,
        /** The pitch (left and right) of the position. */
        val pitch: Float = 0f,
        /** The yaw (up and down) of the position.*/
        val yaw: Float = 0f
) {
    /** Converts a [KPosition] as a [Position] */
    val asPosition: Position by lazy {
        Position(x, y, z, pitch, yaw)
    }
}