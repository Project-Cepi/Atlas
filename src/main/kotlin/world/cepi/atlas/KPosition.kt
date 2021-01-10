package world.cepi.atlas

import kotlinx.serialization.Serializable
import net.minestom.server.utils.Position

@Serializable
class KPosition(val x: Float, val y: Float, val z: Float, val pitch: Float = 0f, val yaw: Float = 0f) {
    val asPosition: Position
        get() = Position(x, y, z, pitch, yaw)
}