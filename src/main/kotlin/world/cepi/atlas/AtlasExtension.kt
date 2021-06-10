package world.cepi.atlas

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
        Manager.connection.addPlayerInitialization(AtlasInstanceLoader::attatchPlayerInitialization)
        eventNode.listenOnly(AtlasInstanceLoader::loadEvent)

        logger.info("[Atlas] has been enabled!")
    }

    override fun terminate() {

        AtlasCommand.unregister()

        Manager.connection.removePlayerInitialization(AtlasInstanceLoader::attatchPlayerInitialization)

        logger.info("[Atlas] has been disabled!")
    }

    companion object: ExtensionCompanion<AtlasExtension>(AtlasExtension::class)

}