package com.mapter.vsclaims.block;

import com.mapter.vsclaims.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ClaimBlockEntity extends BlockEntity {

    public ClaimBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CLAIM_BE.get(), pos, state);
    }
}