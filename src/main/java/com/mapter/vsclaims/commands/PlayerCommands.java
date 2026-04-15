package com.mapter.vsclaims.commands;

import com.mapter.vsclaims.claim.ShipClaimManager;
import com.mapter.vsclaims.ship.RegisteredShipsManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public class PlayerCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vsclaims")
                        // /vsclaims info
                        .then(Commands.literal("info")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    if (!(source.getEntity() instanceof ServerPlayer player)) {
                                        source.sendFailure(Component.translatable("commands.vsclaims.only_player"));
                                        return 0;
                                    }

                                    Map<String, String> ships = RegisteredShipsManager.getRegisteredShips(player.getUUID());
                                    int current = ships.size();
                                    UUID playerId = player.getUUID();

                                    int migratedSlots = ShipClaimManager.getMigratedSlots(player.serverLevel(), playerId);
                                    int usedSlots     = ShipClaimManager.getUsedSlots(player.serverLevel(), playerId);

                                    source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.registered_count", current), false);
                                    source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.ship_slots", usedSlots, migratedSlots), false);

                                    if (ships.isEmpty()) {
                                        source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.empty"), false);
                                    } else {
                                        for (String name : ships.values()) {
                                            source.sendSuccess(() -> Component.translatable("commands.vsclaims.info.entry", name), false);
                                        }
                                    }

                                    return 1;
                                }))

                        // /vsclaims claim info
                        .then(Commands.literal("claim")
                                .then(Commands.literal("info")
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                                source.sendFailure(Component.translatable("commands.vsclaims.only_player"));
                                                return 0;
                                            }

                                            UUID playerId = player.getUUID();
                                            int migratedSlots = ShipClaimManager.getMigratedSlots(player.serverLevel(), playerId);
                                            int usedSlots     = ShipClaimManager.getUsedSlots(player.serverLevel(), playerId);
                                            int freeOpac = ShipClaimManager.getFreeOpacClaims(player);

                                            source.sendSuccess(() -> Component.translatable(
                                                    "commands.vsclaims.claim_info.slots",
                                                    usedSlots, migratedSlots
                                            ), false);
                                            if (freeOpac >= 0) {
                                                source.sendSuccess(() -> Component.translatable(
                                                        "commands.vsclaims.claim_info.opac_free",
                                                        freeOpac
                                                ), false);
                                            } else {
                                                source.sendSuccess(() -> Component.translatable(
                                                        "commands.vsclaims.claim_info.opac_unavailable"
                                                ), false);
                                            }

                                            return 1;
                                        }))

                                // /vsclaims claim transfer ship <amount>
                                .then(Commands.literal("transfer")
                                        .then(Commands.literal("ship")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> {
                                                            CommandSourceStack source = ctx.getSource();
                                                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                                                source.sendFailure(Component.translatable("commands.vsclaims.only_player"));
                                                                return 0;
                                                            }

                                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                            int freeOpac = ShipClaimManager.getFreeOpacClaims(player);

                                                            ShipClaimManager.TransferResult result =
                                                                    ShipClaimManager.transferFromOpac(player, amount);

                                                            switch (result) {
                                                                case SUCCESS -> {
                                                                    UUID playerId = player.getUUID();
                                                                    int newMigrated = ShipClaimManager.getMigratedSlots(player.serverLevel(), playerId);
                                                                    int newUsed     = ShipClaimManager.getUsedSlots(player.serverLevel(), playerId);
                                                                    source.sendSuccess(() -> Component.translatable(
                                                                            "commands.vsclaims.transfer.success",
                                                                            amount, newUsed, newMigrated
                                                                    ), false);
                                                                }
                                                                case OPAC_NOT_LOADED ->
                                                                        source.sendFailure(Component.translatable("commands.vsclaims.transfer.opac_not_loaded"));
                                                                case NOT_ENOUGH_FREE ->
                                                                        source.sendFailure(Component.translatable(
                                                                                "commands.vsclaims.transfer.not_enough",
                                                                                freeOpac, amount
                                                                        ));
                                                                case API_ERROR ->
                                                                        source.sendFailure(Component.translatable("commands.vsclaims.transfer.error"));
                                                            }

                                                            return result == ShipClaimManager.TransferResult.SUCCESS ? 1 : 0;
                                                        })))))

        );
    }
}
