package world.cepi.atlas

import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.extensions.Extension
import world.cepi.atlas.commands.AtlasCommand
import world.cepi.kstom.Manager
import world.cepi.kstom.command.register
import world.cepi.kstom.command.unregister
import world.cepi.kstom.event.listenOnly
import world.cepi.kstom.extension.ExtensionCompanion

class AtlasExtension : Extension() {

    override fun initialize() {


        AtlasCommand.register()

        AtlasInstance.loadInstances()
        eventNode.listenOnly<PlayerSpawnEvent> {
            AtlasInstanceLoader.attachPlayerInitialization(player)
        }
        eventNode.listenOnly(AtlasInstanceLoader::loadEvent)

        logger.info("[Atlas] has been enabled!")
    }

    override fun terminate() {

        AtlasCommand.unregister()

        logger.info("[Atlas] has been disabled!")
    }

    companion object: ExtensionCompanion<AtlasExtension>(Any())

}