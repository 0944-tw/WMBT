package com.zeroninefivefive.wcnm.Handler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.zeroninefivefive.wcnm.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinCommandHandler {
    public static int execute(CommandContext<CommandSourceStack> ctx, Main main) {
        try {
            final PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
            final List<Player> targets = targetResolver.resolve(ctx.getSource());
            String ArenaUUID = ctx.getArgument("lobby", String.class);
            if (targets.size() > 1 & !ctx.getSource().getSender().isOp()) {
                ctx.getSource().getSender().sendMessage("go die!!! you are not op!!!!!! you cant do this you stupid im going to ban you!!!!");
                return Command.SINGLE_SUCCESS;
            }
            if (targets.isEmpty()) {
                ctx.getSource().getSender().sendMessage(Component.text("Players missing.").color(NamedTextColor.RED));
                return Command.SINGLE_SUCCESS;
            }
            if (main.arenaManager.GetArenaFromWorldUUID(ArenaUUID) != null) {
                targets.forEach(player -> {
                    player.sendMessage(Component.text("Teleporting you to ").color(NamedTextColor.YELLOW).append(Component.text()));
                    main.arenaManager.JoinGame(player, ctx.getArgument("lobby", String.class));
                });
            } else {
                main.getLogger().warning("JAVA 你媽死了!");
                ctx.getSource().getSender().sendMessage(Component.text("Server not exists!").color(NamedTextColor.RED));
            }

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            main.getLogger().warning("IDEA 你媽死了!");
            main.getLogger().warning(e.getMessage());

        }
        return Command.SINGLE_SUCCESS;

    }
}

