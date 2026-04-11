package com.mapter.vsclaims.network;

import com.mapter.vsclaims.ship.RegisteredShipsManager;
import com.mapter.vsclaims.ship.UnregisteredShipsManager;
import com.mapter.vsclaims.ship.VSShipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class RegisterShipPacket {

    private static final Logger LOGGER = LogManager.getLogger("vsclaims/RegisterShipPacket");

    private final BlockPos pos;

    public RegisterShipPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RegisterShipPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static RegisterShipPacket decode(FriendlyByteBuf buf) {
        return new RegisterShipPacket(buf.readBlockPos());
    }

    public static void handle(RegisterShipPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                LOGGER.warn("Player is null");
                return;
            }

            LOGGER.info("RegisterShipPacket received from {} at {}", player.getName().getString(), msg.pos);

            ServerLevel level = player.serverLevel();
            Object ship = VSShipUtils.getShipAt(level, msg.pos);
            LOGGER.info("Ship at pos: {}", ship);

            if (ship == null) {
                player.sendSystemMessage(Component.literal("§cКорабль не найден на данной позиции!"));
                return;
            }

            // Если ship == Boolean.TRUE — isBlockInShipyard сработал но объект не получен
            // Нужно получить реальный объект через getLoadedShips
            if (ship instanceof Boolean) {
                LOGGER.info("Ship is Boolean.TRUE — trying to find via getLoadedShips");
                ship = VSShipUtils.getShipObjectAt(level, msg.pos);
                LOGGER.info("Ship object: {}", ship);
            }

            if (ship == null) {
                player.sendSystemMessage(Component.literal("§cНе удалось получить объект корабля!"));
                return;
            }

            String shipId = VSShipUtils.getShipId(ship);
            String slug = VSShipUtils.getShipSlug(ship);
            LOGGER.info("shipId={} slug={}", shipId, slug);

            if (shipId == null) {
                player.sendSystemMessage(Component.literal("§cНе удалось получить ID корабля!"));
                return;
            }

            if (slug == null) slug = "ship";

            RegisteredShipsManager.registerShip(shipId, slug, player.getUUID(), player.getName().getString());
            UnregisteredShipsManager.removeShip(shipId);
            player.sendSystemMessage(Component.literal("Корабль зарегистрирован: " + slug));
        });
        ctx.setPacketHandled(true);
    }
}