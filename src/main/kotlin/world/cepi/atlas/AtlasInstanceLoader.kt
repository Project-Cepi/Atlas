package world.cepi.atlas

import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerLoginEvent
import world.cepi.kstom.Manager
import world.cepi.kstom.addEventCallback

object AtlasInstanceLoader {

    val instance by lazy {

        // If any loaded instance isnt an atlas instance (from other extensions) don't continue
        if (Manager.instance.instances.any { !it.isAtlas }) return@lazy null

        // Find the first loaded atlas instance
        val atlasInstance = Manager.instance.instances.first().asAtlas

        // Load 0 / 0
        atlasInstance?.instanceContainer?.loadChunk(0, 0)

        // And return the instance
        atlasInstance
    }

    fun load(player: Player) = instance?.let {

        // Set the respawn point to the instance's (serializable) spawn
        player.respawnPoint = instance!!.spawn.asPosition

        player.addEventCallback<PlayerLoginEvent> {

            // set the spawning instance to the instance's container
            setSpawningInstance(instance!!.instanceContainer)

        }
    }
}