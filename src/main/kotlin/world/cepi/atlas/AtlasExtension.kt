package world.cepi.atlas

import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import world.cepi.atlas.commands.AtlasCommand
import world.cepi.kstom.Manager
import world.cepi.kstom.command.register
import world.cepi.kstom.command.unregister

class AtlasExtension : Extension() {

    override fun initialize() {


        AtlasCommand.register()

        AtlasInstance.loadInstances()
        Manager.connection.addPlayerInitialization(PlayerInstanceLoader::load)

        logger.info("[Atlas] has been enabled!")
    }

    override fun terminate() {

        AtlasCommand.unregister()

        Manager.connection.removePlayerInitialization(PlayerInstanceLoader::load)

        logger.info("[Atlas] has been disabled!")
    }

}