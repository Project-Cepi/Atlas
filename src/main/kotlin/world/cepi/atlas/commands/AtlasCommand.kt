package world.cepi.atlas.commands

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import world.cepi.atlas.AtlasInstance
import world.cepi.atlas.asAtlas
import world.cepi.atlas.world.loader.Loader
import world.cepi.kstom.command.addSyntax
import world.cepi.kstom.command.arguments.literal
import java.util.*
import java.util.function.Supplier

object AtlasCommand : Command("atlas") {

    init {

        val list = "list".literal()
        val info = "info".literal()
        val import = "import".literal()
        val setspawn = "setspawn".literal()
        val tp = "tp".literal()
        val generate = "generate".literal()

        val instances = ArgumentType.Word("instance").map { uuid ->
            return@map MinecraftServer.getInstanceManager()
                .instances.firstOrNull { it.uniqueId.toString() == uuid }
                ?: throw ArgumentSyntaxException("Instance not found", uuid, 1)
        }

        val loaders = ArgumentType.Word("loader").from(*Loader.values().map { it.name }.toTypedArray())
        loaders.defaultValue = Supplier { Loader.FALSE.name }

        addSyntax(list) {
            MinecraftServer.getInstanceManager().instances.forEach {
                sender.sendMessage(Component.text(it.uniqueId.toString()))
            }
        }

        addSyntax(info, instances) {
            val instance = context.get(instances)
            sender.sendMessage(Component.text("UUID: ${instance.uniqueId}"))
        }

        addSyntax(info) {

            val player = sender as Player
            player.sendMessage(Component.text("UUID: ${player.instance?.uniqueId}"))
        }

        addSyntax(setspawn, instances) {

            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            val player = sender as Player

            val instance = context.get(instances)
            setSpawn(player, instance)
        }

        addSyntax(tp, instances) {
            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            val player = sender as Player

            val instance = context.get(instances)

            if (player.instance?.uniqueId != instance.uniqueId)
                player.setInstance(instance)

            instance.asAtlas?.spawn?.let {
                player.teleport(it)
            } ?: let {
                player.teleport(Pos(0.0, 300.0, 0.0))
            }

            sender.sendMessage(Component.text("Teleported to the instance's spawn!"))
        }

        addSyntax(tp) {
            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            val player = sender as Player

            player.instance?.asAtlas?.spawn?.let {
                player.teleport(it)
                player.sendMessage(Component.text("Teleported to the instance's spawn!"))
            }
        }

        addSyntax(setspawn) {

            if (sender !is Player) {
                sender.sendMessage(Component.text("You are not a player!"))
                return@addSyntax
            }

            val player = sender as Player

            setSpawn(player, player.instance)
        }

        addSyntax(generate, loaders) {

            val instance = AtlasInstance(loader = Loader.valueOf(context.get(loaders).uppercase()))
            sender.sendMessage(Component.text("Instance (${instance.instanceContainer.uniqueId}) added!"))

        }
    }

    private fun findInstance(uuid: UUID): Instance = MinecraftServer.getInstanceManager().instances
        .first { it.uniqueId == uuid }

    private fun setSpawn(player: Player, instance: Instance?) {
        if (instance == null) player.sendMessage(Component.text("This instance does not exist!"))

        if (player.instance!!.uniqueId != instance!!.uniqueId) {
            player.sendMessage(Component.text("This is the wrong instance you're setting the spawn of!"))
            return
        }

        instance.asAtlas?.spawn = player.position

        player.sendMessage(Component.text("Set the spawn of ${instance.uniqueId}!"))
    }

}