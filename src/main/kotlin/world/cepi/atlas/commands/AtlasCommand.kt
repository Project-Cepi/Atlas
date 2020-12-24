package world.cepi.atlas.commands

import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.data.DataImpl
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.utils.Position
import world.cepi.kstom.addSyntax
import world.cepi.kstom.arguments.asSubcommand
import java.util.*

class AtlasCommand : Command("atlas") {

    init {

        val list = "list".asSubcommand()
        val info = "info".asSubcommand()
        val import = "import".asSubcommand()
        val setspawn = "setspawn".asSubcommand()
        val tp = "tp".asSubcommand()
        val generate = "generate".asSubcommand()

        val instances = ArgumentType.DynamicWord("instance").fromRestrictions { uuid ->
            return@fromRestrictions MinecraftServer.getInstanceManager().instances.any { it.uniqueId.toString() == uuid }
        }

        addSyntax(list) { sender ->
            MinecraftServer.getInstanceManager().instances.forEach {
                sender.sendMessage(it.uniqueId.toString())
            }
        }

        addSyntax(info, instances) { sender, args ->
            val instance = getInstance(UUID.fromString(args.getString("instance")))
            sender.sendMessage("UUID: ${instance.uniqueId}")
        }

        addSyntax(setspawn, instances) { sender, args ->

            if (sender !is Player) {
                sender.sendMessage("You are not a player!")
                return@addSyntax
            }

            val instance = getInstance(UUID.fromString(args.getString("instance")))
            setSpawn(sender, instance)
        }

        addSyntax(tp, instances) { sender, args ->
            if (sender !is Player) {
                sender.sendMessage("You are not a player!")
                return@addSyntax
            }

            val instance = getInstance(UUID.fromString(args.getString("instance")))

            sender.setInstance(instance)
            instance.data?.get<Position>("spawn")?.let {
                sender.teleport(it)
                sender.sendMessage("Teleported to the instance's spawn!")
            }
        }

        addSyntax(tp) { sender ->
            if (sender !is Player) {
                sender.sendMessage("You are not a player!")
                return@addSyntax
            }

            sender.instance?.data?.get<Position>("spawn")?.let {
                sender.teleport(it)
                sender.sendMessage("Teleported to the instance's spawn!")
            }
        }

        addSyntax(setspawn) { sender ->

            if (sender !is Player) {
                sender.sendMessage("You are not a player!")
                return@addSyntax
            }

            setSpawn(sender, sender.instance)
        }
    }

    override fun onDynamicWrite(text: String): Array<String> {
        return MinecraftServer.getInstanceManager().instances.map { it.uniqueId.toString() }.toTypedArray()
    }

    private fun getInstance(uuid: UUID): Instance = MinecraftServer.getInstanceManager().instances
        .first { it.uniqueId == uuid }

    private fun setSpawn(player: Player, instance: Instance?) {
        if (player.instance == null) {
            player.sendMessage("This instance does not exist!")
            return
        }

        if (instance == null) player.sendMessage("This instance does not exist!")

        if (player.instance!!.uniqueId != instance!!.uniqueId) {
            player.sendMessage("This is the wrong instance you're setting the spawn of!")
            return
        }

        if (instance.data == null) instance.data = DataImpl()

        instance.data!!.set("spawn", player.position)

        player.sendMessage("Set the spawn of ${instance.uniqueId}!")
    }

}