package com.mapter.vsclaims.client;

import com.mapter.vsclaims.vsclaims;
import com.mapter.vsclaims.registry.ModMenus;
import com.mapter.vsclaims.screen.ClaimSettingsScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = vsclaims.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.CLAIM_SETTINGS_MENU.get(), ClaimSettingsScreen::new);
        });
    }
}
