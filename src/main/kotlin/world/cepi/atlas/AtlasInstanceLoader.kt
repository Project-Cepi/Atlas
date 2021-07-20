package world.cepi.atlas

import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import world.cepi.kstom.Manager

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

    fun onSpawn(event: PlayerSpawnEvent) = instance?.let {

        // Set the respawn point to the instance's (serializable) spawn
        event.player.respawnPoint = it.spawn
        event.player.teleport(it.spawn)

    }

    fun loadEvent(event: PlayerLoginEvent) = with(event) {

        // set the spawning instance to the instance's container
        instance?.let { setSpawningInstance(it.instanceContainer) }

    }
}