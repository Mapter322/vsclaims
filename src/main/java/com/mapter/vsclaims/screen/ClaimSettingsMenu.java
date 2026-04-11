package com.mapter.vsclaims.screen;

import com.mapter.vsclaims.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.UUID;

public class ClaimSettingsMenu extends AbstractContainerMenu {

    private final BlockPos center;
    private final UUID owner;
    private boolean claimActive;
    private boolean allowParty;
    private boolean allowAllies;
    private boolean allowOthers;

    public ClaimSettingsMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos(), buf.readUUID(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public ClaimSettingsMenu(int containerId, Inventory playerInventory, BlockPos center, UUID owner, boolean claimActive, boolean allowParty, boolean allowAllies, boolean allowOthers) {
        super(ModMenus.CLAIM_SETTINGS_MENU.get(), containerId);
        this.center = center;
        this.owner = owner;
        this.claimActive = claimActive;
        this.allowParty = allowParty;
        this.allowAllies = allowAllies;
        this.allowOthers = allowOthers;
    }

    public BlockPos getCenter() {
        return center;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isClaimActive() {
        return claimActive;
    }

    public void setClaimActive(boolean claimActive) {
        this.claimActive = claimActive;
    }

    public boolean isAllowParty() {
        return allowParty;
    }

    public void setAllowParty(boolean allowParty) {
        this.allowParty = allowParty;
    }

    public boolean isAllowAllies() {
        return allowAllies;
    }

    public void setAllowAllies(boolean allowAllies) {
        this.allowAllies = allowAllies;
    }

    public boolean isAllowOthers() {
        return allowOthers;
    }

    public void setAllowOthers(boolean allowOthers) {
        this.allowOthers = allowOthers;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
