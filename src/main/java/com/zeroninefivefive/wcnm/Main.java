package com.zeroninefivefive.wcnm;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zeroninefivefive.wcnm.Arena.ArenaEvents;
import com.zeroninefivefive.wcnm.Arena.ArenaManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class Main extends JavaPlugin {

    public ArenaManager arenaManager;
    public ArenaEvents arenaEvents;
    public ScoreboardLibrary Scoreboard;

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onEnable() {
        // Welcome to OSU
        saveResource("eh_maps.yml", /* replace */ false);
        File dataFolder = new File(getDataFolder(), "maps");
        this.arenaManager = new ArenaManager(this);

        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs(); // Creates folders, including parents
            if (created) {
                getLogger().info("Map folder created.");
            } else {
                getLogger().warning("Failed to create maps folder.");
            }
        }
        Bukkit.getScheduler().runTaskLater(this, bukkitTask -> {
            MultiverseCoreApi coreApi = MultiverseCoreApi.get();
            for (MultiverseWorld world : coreApi.getWorldManager().getWorlds()) {
                if (world.getName().startsWith("wmbt_")) {
                    getLogger().info("Deleteting Old World " + world.getName());
                    coreApi.getWorldManager().deleteWorld(DeleteWorldOptions.world(world));
                }
            }
        }, 25L);

        try {
            Scoreboard = ScoreboardLibrary.loadScoreboardLibrary(this);
        } catch (NoPacketAdapterAvailableException e) {
            Scoreboard = new NoopScoreboardLibrary();
            getLogger().warning("no scoreboard library found!!! call 911 asap as soon as soup!!!!!");
        }

        this.arenaEvents = new ArenaEvents(this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, command -> {
            LiteralArgumentBuilder<CommandSourceStack> startgame_cmd = Commands.literal("start")
                    .requires(player -> player.getSender().isOp())
                    .then(Commands.literal("players"))
                    .then(Commands.literal("map"))
                    .executes(ctx -> {
                        try {
                            String ServerUUID = arenaManager.CreateGameLobby(null);
                            ctx.getSource().getSender().sendMessage(Component.text(ServerUUID));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return Command.SINGLE_SUCCESS;
                    });

            LiteralArgumentBuilder<CommandSourceStack> GameJoin = Commands.literal("join")
                    .then(Commands.argument("lobby", StringArgumentType.string()))
                    .then(Commands.argument("players", ArgumentTypes.players()))
                    .executes(ctx -> {
                        final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
                        final List<Player> targets = targetResolver.resolve(ctx.getSource());
                        String ArenaUUID = ctx.getArgument("lobby", String.class);
                        if (targets.size() > 1 & !ctx.getSource().getSender().isOp()) {
                            ctx.getSource().getSender().sendMessage("go die!!! you are not op!!!!!! you cant do this you stupid im going to ban you!!!!");
                            return Command.SINGLE_SUCCESS;
                        }
                        if (!targets.isEmpty()) {
                            ctx.getSource().getSender().sendMessage(Component.text("Players missing.").color(NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        if (arenaManager.GetArenaFromWorldUUID(ArenaUUID) != null) {
                            targets.forEach( player -> {
                                player.sendMessage(Component.text("Teleporting you to ").color(NamedTextColor.YELLOW).append(Component.text()));
                                arenaManager.JoinGame(player, ctx.getArgument("lobby", String.class));
                            });
                        } else {
                            ctx.getSource().getSender().sendMessage(Component.text("Server not exists!").color(NamedTextColor.RED));
                        }

                         return Command.SINGLE_SUCCESS;
                    });


            command.registrar().register(startgame_cmd.build());
            command.registrar().register(GameJoin.build());
        });
    }

    @Override
    public void onDisable() {
        MultiverseCoreApi coreApi = MultiverseCoreApi.get();
        for (MultiverseWorld world : coreApi.getWorldManager().getWorlds()) {
            if (world.getName().startsWith("wmbt_")) {
                getLogger().info("Deleteting Old World " + world.getName());
                coreApi.getWorldManager().deleteWorld(DeleteWorldOptions.world(world));
            }
        }
    }
}
