package com.mapter.vsclaims.registry;

import com.mapter.vsclaims.vsclaims;
import com.mapter.vsclaims.block.ClaimBlock;
import com.mapter.vsclaims.block.ClaimBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, vsclaims.MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, vsclaims.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, vsclaims.MODID);

    public static final RegistryObject<Block> CLAIM_BLOCK =
            BLOCKS.register("claim_block",
                    () -> new ClaimBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final RegistryObject<Item> CLAIM_BLOCK_ITEM =
            ITEMS.register("claim_block",
                    () -> new BlockItem(CLAIM_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<ClaimBlockEntity>> CLAIM_BE =
            BLOCK_ENTITIES.register("claim_be",
                    () -> BlockEntityType.Builder.of(
                            ClaimBlockEntity::new,
                            CLAIM_BLOCK.get()
                    ).build(null));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}