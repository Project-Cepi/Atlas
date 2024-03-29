package world.cepi.atlas

import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import org.slf4j.LoggerFactory

object AtlasInstanceLoader {

    val logger = LoggerFactory.getLogger(this::class.java)

    val instance = run instance@ {

        // Find the first loaded atlas instance
        val atlasInstance = AtlasInstance.instances.values.firstOrNull()
            ?: run {
                logger.warn("No Atlas instance found.")
                return@instance null
            }

        // Load 0 / 0
        atlasInstance.instanceContainer.loadChunk(0, 0)

        // And return the instance
        atlasInstance
    }

    fun onSpawn(event: PlayerSpawnEvent) = event.spawnInstance.asAtlas?.let {

        // Set the respawn point to the instance's (serializable) spawn
        event.player.respawnPoint = it.spawn
        event.player.teleport(it.spawn)

    }

    fun loadEvent(event: PlayerLoginEvent) = with(event) {

        // set the spawning instance to the instance's container
        instance?.let { setSpawningInstance(it.instanceContainer) }

    }
}