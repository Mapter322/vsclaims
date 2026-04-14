package com.mapter.vsclaims.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class VSClaimsConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_SHIP_BLOCKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        MAX_SHIP_BLOCKS = builder
                .defineInRange("maxShipBlocks", 10000, 1, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }

    private VSClaimsConfig() {
    }
}
