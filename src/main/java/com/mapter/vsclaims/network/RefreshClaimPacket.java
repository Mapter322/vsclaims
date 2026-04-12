package com.mapter.vsclaims.network;

import com.mapter.vsclaims.claim.Claim;
import com.mapter.vsclaims.claim.ClaimManager;
import com.mapter.vsclaims.config.VSClaimsConfig;
import com.mapter.vsclaims.ship.VSShipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

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

            Claim claim = ClaimManager.getClaimByCenter(player.serverLevel(), msg.center);
            if (claim == null) return;

            if (!player.getUUID().equals(claim.getOwner())) return;

            if (!VSShipUtils.isOnShip(player.serverLevel(), msg.center)) {
                player.sendSystemMessage(Component.translatable("message.vsclaims.refresh_only_on_ship"));
                return;
            }

            int maxSize = VSClaimsConfig.MAX_SHIP_BLOCKS.get();
            if (ClaimManager.countShipBlocks(player.serverLevel(), msg.center, maxSize) > maxSize) {
                int exact = ClaimManager.countShipBlocksExact(player.serverLevel(), msg.center);
                ClaimManager.deactivateClaim(player.serverLevel(), msg.center);
                player.sendSystemMessage(Component.translatable("message.vsclaims.ship_too_large", exact, maxSize));
                // Синхронизируем деактивацию клиенту
                VSClaimsNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncClaimStatePacket(msg.center, false,
                                claim.isAllowParty(), claim.isAllowAllies(), claim.isAllowOthers())
                );
                return;
            }

            ClaimManager.refreshClaim(player.serverLevel(), msg.center);
            player.sendSystemMessage(Component.translatable("message.vsclaims.claim_refreshed"));

            // Синхронизируем активацию клиенту в реальном времени
            VSClaimsNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncClaimStatePacket(msg.center, claim.isActive(),
                            claim.isAllowParty(), claim.isAllowAllies(), claim.isAllowOthers())
            );
        });
        ctx.setPacketHandled(true);
    }
}
