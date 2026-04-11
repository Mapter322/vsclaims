package com.mapter.vsclaims.commands;

import com.mapter.vsclaims.ship.RegisteredShipsManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class PlayerCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vsclaims")
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.translatable("commands.vsclaims.only_player"));
                                        return 0;
                                    }

                                    Map<String, String> ships = RegisteredShipsManager.getRegisteredShips(player.getUUID());
                                    int current = ships.size();

                                    source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.registered_count", current), false);
                                    if (ships.isEmpty()) {
                                        source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.empty"), false);
                                    } else {
                                        for (String name : ships.values()) {
                                            source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.entry", name), false);
                                        }
                                    }

                                    return 1;
                                }))
        );
    }
}
