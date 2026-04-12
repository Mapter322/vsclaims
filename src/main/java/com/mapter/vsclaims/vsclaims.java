package com.mapter.vsclaims;

import com.mapter.vsclaims.config.VSClaimsConfig;
import com.mapter.vsclaims.event.ProtectionEvents;
import com.mapter.vsclaims.network.VSClaimsNetwork;
import com.mapter.vsclaims.registry.ModBlocks;
import com.mapter.vsclaims.registry.ModMenus;
import com.mapter.vsclaims.claim.ClaimManager;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.config.ModConfig;

@Mod(vsclaims.MODID)
public class vsclaims {

    public static final String MODID = "vsclaims";

    public vsclaims() {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modBus);
        ModMenus.register(modBus);

        modBus.addListener(vsclaims::addCreative);

        VSClaimsNetwork.init();

        MinecraftForge.EVENT_BUS.register(ProtectionEvents.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VSClaimsConfig.SPEC);

        ClaimManager.init(ModList.get().isLoaded("openpartiesandclaims"));
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.CLAIM_BLOCK_ITEM.get());
        }
    }
}