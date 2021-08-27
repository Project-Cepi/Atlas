package world.cepi.atlas

import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.extensions.Extension
import net.minestom.server.instance.block.Block
import world.cepi.atlas.commands.AtlasCommand
import world.cepi.atlas.handler.SignHandler
import world.cepi.atlas.handler.SkullHandler
import world.cepi.kstom.Manager
import world.cepi.kstom.command.register
import world.cepi.kstom.command.unregister
import world.cepi.kstom.event.listenOnly

class AtlasExtension : Extension() {

    override fun initialize() {
        AtlasCommand.register()

        AtlasInstance.loadInstances()
        eventNode.listenOnly(AtlasInstanceLoader::onSpawn)
        eventNode.listenOnly(AtlasInstanceLoader::loadEvent)

        Manager.block.registerHandler("minecraft:sign") { SignHandler }
        Manager.block.registerHandler("minecraft:skull") { SkullHandler }

        logger.info("[Atlas] has been enabled!")
    }

    override fun terminate() {

        AtlasCommand.unregister()

        logger.info("[Atlas] has been disabled!")
    }

}