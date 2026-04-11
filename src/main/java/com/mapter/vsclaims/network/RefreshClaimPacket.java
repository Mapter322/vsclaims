package com.mapter.vsclaims.network;

import com.mapter.vsclaims.claim.ClaimManager;
import com.mapter.vsclaims.ship.VSShipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RefreshClaimPacket {

    private final BlockPos center;

    public RefreshClaimPacket(BlockPos center) {
        this.center = center;
    }

    public static void encode(RefreshClaimPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.center);
    }

    public static RefreshClaimPacket decode(FriendlyByteBuf buf) {
        return new RefreshClaimPacket(buf.readBlockPos());
    }

    public static void handle(RefreshClaimPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            var claim = ClaimManager.getClaimByCenter(player.serverLevel(), msg.center);
            if (claim == null) return;

            if (!player.getUUID().equals(claim.getOwner())) return;

            if (!VSShipUtils.isOnShip(player.serverLevel(), msg.center)) {
                player.sendSystemMessage(Component.literal("§cОбновление доступно только на корабле!"));
                return;
            }

            ClaimManager.refreshClaim(player.serverLevel(), msg.center);
            player.sendSystemMessage(Component.literal("§aПриват активирован и границы обновлены"));
        });
        ctx.setPacketHandled(true);
    }
}