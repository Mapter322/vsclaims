package com.mapter.vsclaims.permission;

import com.mapter.vsclaims.claim.Claim;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public class DefaultPermissionResolver implements ClaimPermissionResolver {

    @Override
    public boolean canAccess(ServerPlayer player, Claim claim) {
        UUID owner = claim.getOwner();
        return player.getUUID().equals(owner);
    }
}