package com.mapter.vsclaims.commands;

import com.mapter.vsclaims.vsclaims;
import com.mapter.vsclaims.ship.RegisteredShipsManager;
import com.mapter.vsclaims.ship.UnregisteredShipsManager;
import com.mapter.vsclaims.ship.VSShipUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Map;

@Mod.EventBusSubscriber(modid = vsclaims.MODID)
public class ShipRegistrationCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mk")
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
                        .then(Commands.literal("ships")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("clear")
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();

                                            int total = UnregisteredShipsManager.getCount();
                                            if (total == 0) {
                                                source.sendSuccess(() -> Component.translatable("commands.vsclaims.ships.clear.none"), true);
                                                return 1;
                                            }

                                            int deleted = 0;
                                            int failed = 0;
                                            java.util.List<String> ids = new ArrayList<>(UnregisteredShipsManager.getShipIds());
                                            for (String shipId : ids) {
                                                boolean ok = VSShipUtils.deleteShipById(source.getLevel(), shipId);
                                                if (ok) {
                                                    deleted++;
                                                    UnregisteredShipsManager.removeShip(shipId);
                                                } else {
                                                    failed++;
                                                }
                                            }

                                            int finalDeleted = deleted;
                                            int finalFailed = failed;
                                            source.sendSuccess(
                                                    () -> Component.translatable("commands.vsclaims.ships.clear.done", finalDeleted, finalFailed, total),
                                                    true
                                            );
                                            return 1;
                                        })))
                );

    }
}
