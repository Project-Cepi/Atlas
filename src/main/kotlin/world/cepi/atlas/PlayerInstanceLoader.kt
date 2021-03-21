package world.cepi.atlas

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerLoginEvent
import world.cepi.kstom.addEventCallback

object PlayerInstanceLoader {
    fun load(player: Player) {
        player.addEventCallback(PlayerLoginEvent::class) {
            if (MinecraftServer.getInstanceManager().instances.any { !it.isAtlas }) return@addEventCallback

            val atlasInstance = MinecraftServer.getInstanceManager().instances.first().asAtlas

            atlasInstance?.let {
                setSpawningInstance(atlasInstance.instanceContainer)
                player.respawnPoint = atlasInstance.spawn.asPosition
            }
        }
    }
}