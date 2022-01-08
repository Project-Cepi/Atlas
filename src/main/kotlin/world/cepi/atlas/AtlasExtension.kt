package world.cepi.atlas

import net.minestom.server.extensions.Extension
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockHandler
import world.cepi.atlas.commands.AtlasCommand
import world.cepi.atlas.handler.BannerHandler
import world.cepi.atlas.handler.SignHandler
import world.cepi.atlas.handler.SkullHandler
import world.cepi.kstom.Manager
import world.cepi.kstom.event.listenOnly
import world.cepi.kstom.util.log
import world.cepi.kstom.util.node

class AtlasExtension : Extension() {

    override fun initialize(): LoadStatus {
        AtlasCommand.register()

        AtlasInstance.loadInstances()
        node.listenOnly(AtlasInstanceLoader::onSpawn)
        node.listenOnly(AtlasInstanceLoader::loadEvent)

        arrayOf(
            "minecraft:smoker",
            "minecraft:beacon",
            "minecraft:furnace",
            "minecraft:campfire",
            "minecraft:barrel",
            "minecraft:hopper",
            "minecraft:beehive",
            "minecraft:chest"
        ).forEach {
            Manager.block.registerHandler(it) { BlockHandler.Dummy.get(it) }
        }

        Manager.block.registerHandler("minecraft:sign") { SignHandler }
        Manager.block.registerHandler("minecraft:skull") { SkullHandler }

        Block.values().filter { it.name().contains("banner") }.forEach {
            Manager.block.registerHandler(it.namespace().toString()) { BannerHandler }
        }

        log.info("[Atlas] has been enabled!")

        return LoadStatus.SUCCESS
    }

    override fun terminate() {

        AtlasCommand.unregister()

        log.info("[Atlas] has been disabled!")
    }

}