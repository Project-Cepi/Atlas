package world.cepi.atlas.handler

import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID
import org.jglrxavpok.hephaistos.nbt.NBTCompound

object BannerHandler : BlockHandler {

    override fun getNamespaceId() = NamespaceID.from("minecraft:banner")

    override fun getBlockEntityTags(): MutableCollection<Tag<*>> {
        return mutableListOf(
            Tag.NBT("Patterns")
        )
    }

}