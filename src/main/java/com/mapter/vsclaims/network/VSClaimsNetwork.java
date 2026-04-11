package com.mapter.vsclaims.network;

import com.mapter.vsclaims.vsclaims;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class VSClaimsNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(vsclaims.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int nextId = 0;

    public static void init() {
        CHANNEL.registerMessage(nextId++, UpdateClaimSettingsPacket.class,
                UpdateClaimSettingsPacket::encode,
                UpdateClaimSettingsPacket::decode,
                UpdateClaimSettingsPacket::handle);
        CHANNEL.registerMessage(nextId++, RefreshClaimPacket.class,
                RefreshClaimPacket::encode,
                RefreshClaimPacket::decode,
                RefreshClaimPacket::handle);
        CHANNEL.registerMessage(nextId++, RegisterShipPacket.class,
                RegisterShipPacket::encode,
                RegisterShipPacket::decode,
                RegisterShipPacket::handle);
    }
}
