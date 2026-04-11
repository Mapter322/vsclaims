package com.mapter.vsclaims.network;

import com.mapter.vsclaims.claim.Claim;
import com.mapter.vsclaims.claim.ClaimManager;
import com.mapter.vsclaims.claim.ClaimSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateClaimSettingsPacket {

    private final BlockPos center;
    private final boolean allowParty;
    private final boolean allowAllies;
    private final boolean allowOthers;

    public UpdateClaimSettingsPacket(BlockPos center, boolean allowParty, boolean allowAllies, boolean allowOthers) {
        this.center = center;
        this.allowParty = allowParty;
        this.allowAllies = allowAllies;
        this.allowOthers = allowOthers;
    }

    public static void encode(UpdateClaimSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.center);
        buf.writeBoolean(msg.allowParty);
        buf.writeBoolean(msg.allowAllies);
        buf.writeBoolean(msg.allowOthers);
    }

    public static UpdateClaimSettingsPacket decode(FriendlyByteBuf buf) {
        BlockPos center = buf.readBlockPos();
        boolean allowParty = buf.readBoolean();
        boolean allowAllies = buf.readBoolean();
        boolean allowOthers = buf.readBoolean();
        return new UpdateClaimSettingsPacket(center, allowParty, allowAllies, allowOthers);
    }

    public static void handle(UpdateClaimSettingsPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
                return;

            Claim claim = ClaimManager.getClaimByCenter(player.serverLevel(), msg.center);
            if (claim == null)
                return;

            if (!player.getUUID().equals(claim.getOwner()))
                return;

            claim.setAllowParty(msg.allowParty);
            claim.setAllowAllies(msg.allowAllies);
            claim.setAllowOthers(msg.allowOthers);
            ClaimSavedData.get(player.serverLevel()).setDirty();
        });
        ctx.setPacketHandled(true);
    }
}
