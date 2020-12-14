package world.cepi.example

import net.minestom.server.extensions.Extension;

class AtlasExtension : Extension() {

    override fun initialize() {
        logger.info("[AtlasExtension] has been enabled!")
    }

    override fun terminate() {
        logger.info("[AtlasExtension] has been disabled!")
    }

}