package world.cepi.atlas.handler

import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.tag.Tag
import net.minestom.server.utils.NamespaceID

object SignHandler : BlockHandler {
    override fun getNamespaceId() = NamespaceID.from("minecraft:sign")

    override fun getBlockEntityTags(): MutableCollection<Tag<*>> {
        return mutableListOf(
            Tag.String("color"),
            Tag.String("Text1"),
            Tag.String("Text2"),
            Tag.String("Text3"),
            Tag.String("Text4"),
        )
    }
}