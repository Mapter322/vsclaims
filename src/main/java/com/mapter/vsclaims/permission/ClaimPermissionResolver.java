package com.mapter.vsclaims.permission;

import com.mapter.vsclaims.claim.Claim;
import net.minecraft.server.level.ServerPlayer;

public interface ClaimPermissionResolver {
    boolean canAccess(ServerPlayer player, Claim claim);
}