package com.mapter.vsclaims.claim;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class ClaimSavedData extends SavedData {

    private final List<Claim> claims = new ArrayList<>();

    public static ClaimSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ClaimSavedData::load,
                ClaimSavedData::new,
                "vsclaims_data"
        );
    }

    public static ClaimSavedData load(CompoundTag tag) {
        ClaimSavedData data = new ClaimSavedData();

        ListTag list = tag.getList("claims", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag c = (CompoundTag) t;
            Set<BlockPos> claimedBlocks = new HashSet<>();
            ListTag blocks = c.getList("claimedBlocks", Tag.TAG_COMPOUND);
            for (Tag bt : blocks) {
                CompoundTag b = (CompoundTag) bt;
                claimedBlocks.add(new BlockPos(b.getInt("x"), b.getInt("y"), b.getInt("z")));
            }
            boolean active = !c.contains("active") || c.getBoolean("active");
            data.claims.add(new Claim(
                    new BlockPos(c.getInt("x"), c.getInt("y"), c.getInt("z")),
                    c.getUUID("owner"),
                    claimedBlocks,
                    active,
                    c.getBoolean("allowParty"),
                    c.getBoolean("allowAllies"),
                    c.contains("allowOthers") && c.getBoolean("allowOthers")
            ));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag list = new ListTag();

        for (Claim claim : claims) {
            CompoundTag c = new CompoundTag();
            c.putInt("x", claim.getCenter().getX());
            c.putInt("y", claim.getCenter().getY());
            c.putInt("z", claim.getCenter().getZ());
            c.putUUID("owner", claim.getOwner());
            ListTag blocks = new ListTag();
            for (BlockPos pos : claim.getClaimedBlocks()) {
                CompoundTag b = new CompoundTag();
                b.putInt("x", pos.getX());
                b.putInt("y", pos.getY());
                b.putInt("z", pos.getZ());
                blocks.add(b);
            }
            c.put("claimedBlocks", blocks);
            c.putBoolean("active", claim.isActive());
            c.putBoolean("allowParty", claim.isAllowParty());
            c.putBoolean("allowAllies", claim.isAllowAllies());
            c.putBoolean("allowOthers", claim.isAllowOthers());
            list.add(c);
        }

        tag.put("claims", list);
        return tag;
    }

    public List<Claim> getClaims() {
        return claims;
    }
}