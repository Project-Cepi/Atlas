package world.cepi.atlas.commands

import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import world.cepi.kstom.addSyntax

class AtlasCommand : Command("atlas") {

    init {

        val list = ArgumentType.Word("list").from("list")
        val info = ArgumentType.Word("info").from("info")
        val instance = ArgumentType.DynamicWord("instance").fromRestrictions { uuid ->
            return@fromRestrictions MinecraftServer.getInstanceManager().instances.any { it.uniqueId.toString() == uuid }
        }

        addSyntax(list) { sender ->
            MinecraftServer.getInstanceManager().instances.forEach {
                sender.sendMessage(it.uniqueId.toString())
            }
        }

        addSyntax(info, instance) { sender, args ->

        }
    }

    override fun onDynamicWrite(text: String): Array<String> {
        return MinecraftServer.getInstanceManager().instances.map { it.uniqueId.toString() }.toTypedArray()
    }

}