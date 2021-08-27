package world.cepi.atlas.handler

import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID
import org.jglrxavpok.hephaistos.nbt.NBTCompound

object SkullHandler : BlockHandler {
    override fun getNamespaceId() = NamespaceID.from("minecraft:sign")

    override fun getBlockEntityTags(): MutableCollection<Tag<*>> {
        return mutableListOf(
            Tag.NBT<NBTCompound>("SkullOwner"),
            Tag.NBT<NBTCompound>("Id")
        )
    }
}