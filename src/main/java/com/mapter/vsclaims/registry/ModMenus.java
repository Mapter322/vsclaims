package com.mapter.vsclaims.registry;

import com.mapter.vsclaims.vsclaims;
import com.mapter.vsclaims.screen.ClaimSettingsMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, vsclaims.MODID);

    public static final RegistryObject<MenuType<ClaimSettingsMenu>> CLAIM_SETTINGS_MENU =
            MENUS.register("claim_settings", () -> IForgeMenuType.create(ClaimSettingsMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
