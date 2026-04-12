package com.mapter.vsclaims.network;

import com.mapter.vsclaims.screen.ClaimSettingsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncClaimStatePacket {

    private final BlockPos center;
    private final boolean claimActive;
    private final boolean allowParty;
    private final boolean allowAllies;
    private final boolean allowOthers;

    public SyncClaimStatePacket(BlockPos center, boolean claimActive, boolean allowParty, boolean allowAllies, boolean allowOthers) {
        this.center = center;
        this.claimActive = claimActive;
        this.allowParty = allowParty;
        this.allowAllies = allowAllies;
        this.allowOthers = allowOthers;
    }

    public static void encode(SyncClaimStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.center);
        buf.writeBoolean(msg.claimActive);
        buf.writeBoolean(msg.allowParty);
        buf.writeBoolean(msg.allowAllies);
        buf.writeBoolean(msg.allowOthers);
    }

    public static SyncClaimStatePacket decode(FriendlyByteBuf buf) {
        BlockPos center = buf.readBlockPos();
        boolean claimActive = buf.readBoolean();
        boolean allowParty = buf.readBoolean();
        boolean allowAllies = buf.readBoolean();
        boolean allowOthers = buf.readBoolean();
        return new SyncClaimStatePacket(center, claimActive, allowParty, allowAllies, allowOthers);
    }

    public static void handle(SyncClaimStatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) return;
            if (!(mc.player != null && mc.player.containerMenu instanceof ClaimSettingsMenu menu)) return;
            if (!menu.getCenter().equals(msg.center)) return;

            menu.setClaimActive(msg.claimActive);
            menu.setAllowParty(msg.allowParty);
            menu.setAllowAllies(msg.allowAllies);
            menu.setAllowOthers(msg.allowOthers);


            if (mc.screen instanceof com.mapter.vsclaims.screen.ClaimSettingsScreen screen) {
                screen.syncFromMenu();
            }
        });
        ctx.setPacketHandled(true);
    }
}
