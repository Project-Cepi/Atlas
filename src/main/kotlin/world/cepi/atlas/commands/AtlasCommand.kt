package world.cepi.atlas.commands

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.data.DataImpl
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.utils.Position
import world.cepi.atlas.AtlasInstance
import world.cepi.atlas.world.loader.Loader
import world.cepi.kstom.command.addSyntax
import world.cepi.kstom.command.arguments.literal
import java.util.*
import java.util.function.Supplier

class AtlasCommand : Command("atlas") {

    init {

        val list = "list".literal()
        val info = "info".literal()
        val import = "import".literal()
        val setspawn = "setspawn".literal()
        val tp = "tp".literal()
        val generate = "generate".literal()

        val instances = ArgumentType.DynamicWord("instance").fromRestrictions { uuid ->
            return@fromRestrictions MinecraftServer.getInstanceManager().instances.any { it.uniqueId.toString() == uuid }
        }

        val loaders = ArgumentType.Word("loader").from(*Loader.values().map { it.name }.toTypedArray())
        loaders.defaultValue = Supplier { Loader.FALSE.name }

        addSyntax(list) { sender ->
            MinecraftServer.getInstanceManager().instances.forEach {
                sender.sendMessage(Component.text(it.uniqueId.toString()))
            }
        }

        addSyntax(info, instances) { sender, args ->
            val instance = getInstance(UUID.fromString(args.get(instances)))
            sender.sendMessage(Component.text("UUID: ${instance.uniqueId}"))
        }

        addSyntax(info) { sender ->

            val player = sender as Player
            player.sendMessage(Component.text("UUID: ${player.instance?.uniqueId}"))
        }

        addSyntax(setspawn, instances) { sender, args ->

            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            val instance = getInstance(UUID.fromString(args.get(instances)))
            setSpawn(sender, instance)
        }

        addSyntax(tp, instances) { sender, args ->
            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            val instance = getInstance(UUID.fromString(args.get(instances)))

            if (sender.instance?.uniqueId != instance.uniqueId)
                sender.setInstance(instance)

            instance.data?.get<Position>("spawn")?.let {
                sender.teleport(it)
            } ?: let {
                sender.teleport(Position(0.0, 300.0, 0.0))
            }

            sender.sendMessage(Component.text("Teleported to the instance's spawn!"))
        }

        addSyntax(tp) { sender ->
            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            sender.instance?.data?.get<Position>("spawn")?.let {
                sender.teleport(it)
                sender.sendMessage(Component.text("Teleported to the instance's spawn!"))
            }
        }

        addSyntax(setspawn) { sender ->

            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            setSpawn(sender, sender.instance)
        }

        addSyntax(generate, loaders) { sender, args ->

            val instance = AtlasInstance(loader = Loader.valueOf(args.get(loaders).toUpperCase()))
            sender.sendMessage(Component.text("Instance (${instance.instanceContainer.uniqueId}) added!"))

        }
    }

    override fun onDynamicWrite(sender: CommandSender, text: String): Array<out String> {
        return MinecraftServer.getInstanceManager().instances.map { it.uniqueId.toString() }.toTypedArray()
    }

    private fun getInstance(uuid: UUID): Instance = MinecraftServer.getInstanceManager().instances
        .first { it.uniqueId == uuid }

    private fun setSpawn(player: Player, instance: Instance?) {
        if (player.instance == null) {
            player.sendMessage(Component.text("This instance does not exist!"))
            return
        }

        if (instance == null) player.sendMessage(Component.text("This instance does not exist!"))

        if (player.instance!!.uniqueId != instance!!.uniqueId) {
            player.sendMessage(Component.text("This is the wrong instance you're setting the spawn of!"))
            return
        }

        if (instance.data == null) instance.data = DataImpl()

        instance.data!!.set("spawn", player.position)

        player.sendMessage(Component.text("Set the spawn of ${instance.uniqueId}!"))
    }

}