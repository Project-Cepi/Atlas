package world.cepi.example

import net.minestom.server.extensions.Extension;

class AtlasExtension : Extension() {

    override fun initialize() {
        logger.info("[Atlas] has been enabled!")
    }

    override fun terminate() {
        logger.info("[Atlas] has been disabled!")
    }

}