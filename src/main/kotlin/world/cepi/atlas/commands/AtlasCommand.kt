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
import world.cepi.kstom.command.arguments.literal
import world.cepi.kstom.command.arguments.suggest
import world.cepi.kstom.command.kommand.Kommand
import java.util.*
import java.util.function.Supplier

object AtlasCommand : Kommand({

    fun findInstance(uuid: UUID): Instance = MinecraftServer.getInstanceManager().instances
        .first { it.uniqueId == uuid }

    fun setSpawn(player: Player, instance: Instance?) {
        if (instance == null) player.sendMessage(Component.text("This instance does not exist!"))

        if (player.instance!!.uniqueId != instance!!.uniqueId) {
            player.sendMessage(Component.text("This is the wrong instance you're setting the spawn of!"))
            return
        }

        instance.asAtlas?.spawn = player.position

        player.sendMessage(Component.text("Set the spawn of ${instance.uniqueId}!"))
    }

    val list by literal
    val info by literal
    val import by literal
    val setspawn by literal
    val tp by literal
    val generate by literal

    val instances = ArgumentType.Word("instance").map { uuid ->
        return@map AtlasInstance.instances.values
            .firstOrNull { it.name == uuid }
            ?: throw ArgumentSyntaxException("Instance not found", uuid, 1)
    }.suggest {
        AtlasInstance.instances.keys.toList()
    }

    val loaders = ArgumentType.Word("loader").from(*Loader.values().map { it.name }.toTypedArray())
    loaders.defaultValue = Supplier { Loader.FALSE.name }

    syntax(list) {
        MinecraftServer.getInstanceManager().instances.forEach {
            sender.sendMessage(Component.text(it.uniqueId.toString()))
        }
    }

    syntax(info, instances) {
        val instance = context.get(instances)
        sender.sendMessage(Component.text("UUID: ${instance.instanceContainer.uniqueId}"))
    }

    syntax(info) {

        val player = sender as Player
        player.sendMessage(Component.text("UUID: ${player.instance?.uniqueId}"))
    }

    syntax(setspawn, instances) {

        if (sender !is Player) {
            sender.sendMessage(Component.text("You are not a player!"))
            return@syntax
        }

        val instance = context.get(instances)
        setSpawn(player, instance.instanceContainer)
    }

    syntax(tp, instances) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("You are not a player!"))
            return@syntax
        }

        val instance = context.get(instances)

        if (player.instance?.uniqueId != instance.instanceContainer.uniqueId)
            player.setInstance(instance.instanceContainer)

        instance?.spawn?.let {
            player.teleport(it)
        } ?: let {
            player.teleport(Pos(0.0, 300.0, 0.0))
        }

        sender.sendMessage(Component.text("Teleported to the instance's spawn!"))
    }

    syntax(tp) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("You are not a player!"))
            return@syntax
        }

        player.instance?.asAtlas?.spawn?.let {
            player.teleport(it)
            player.sendMessage(Component.text("Teleported to the instance's spawn!"))
        }
    }

    syntax(setspawn) {

        if (sender !is Player) {
            sender.sendMessage(Component.text("You are not a player!"))
            return@syntax
        }

        setSpawn(player, player.instance)
    }

    syntax(generate, loaders) {

        val instance = AtlasInstance(loader = Loader.valueOf(context.get(loaders).uppercase()))
        sender.sendMessage(Component.text("Instance (${instance.instanceContainer.uniqueId}) added!"))

    }

}, "atlas")