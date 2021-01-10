package world.cepi.atlas

import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import world.cepi.atlas.commands.AtlasCommand

class AtlasExtension : Extension() {

    override fun initialize() {

        MinecraftServer.getCommandManager().register(AtlasCommand())
        AtlasInstance.loadInstances()
        MinecraftServer.getConnectionManager().addPlayerInitialization {
            PlayerInstanceLoader.load(it)
        }

        logger.info("[Atlas] has been enabled!")
    }

    override fun terminate() {
        logger.info("[Atlas] has been disabled!")
    }

}