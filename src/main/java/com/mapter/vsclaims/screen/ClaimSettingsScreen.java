package com.mapter.vsclaims.screen;

import com.mapter.vsclaims.network.VSClaimsNetwork;
import com.mapter.vsclaims.network.RefreshClaimPacket;
import com.mapter.vsclaims.network.UpdateClaimSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ClaimSettingsScreen extends AbstractContainerScreen<ClaimSettingsMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation("vsclaims", "textures/screen/claim-menu.png");

    private static final int TEXTURE_SIZE = 180;

    // Тёмный текст на светло-сером фоне, без тени
    private static final int COLOR_TITLE = 0x222222;
    private static final int COLOR_LABEL = 0x555555;

    private Button partyButton;
    private Button alliesButton;
    private Button othersButton;
    private Button refreshButton;
    private long lastRefreshTime = 0;
    private Object ship;

    public ClaimSettingsScreen(ClaimSettingsMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = TEXTURE_SIZE;
        this.imageHeight = TEXTURE_SIZE;
    }

    @Override
    protected void init() {
        super.init();

        if (Minecraft.getInstance().level != null) {
            try {
                Class<?> utilsClass = Class.forName("org.valkyrienskies.mod.common.VSGameUtilsKt");
                this.ship = utilsClass
                        .getMethod("getShipManagingPos",
                                net.minecraft.world.level.Level.class,
                                net.minecraft.core.BlockPos.class)
                        .invoke(null, Minecraft.getInstance().level, menu.getCenter());
            } catch (Exception ignored) {}
        }

        int btnX = this.leftPos + 10;
        int btnW = this.imageWidth - 20;

        this.partyButton = Button.builder(getPartyText(), btn -> {
            this.menu.setAllowParty(!this.menu.isAllowParty());
            btn.setMessage(getPartyText());
            sendUpdate();
        }).bounds(btnX, this.topPos + 24, btnW, 18).build();

        this.alliesButton = Button.builder(getAlliesText(), btn -> {
            this.menu.setAllowAllies(!this.menu.isAllowAllies());
            btn.setMessage(getAlliesText());
            sendUpdate();
        }).bounds(btnX, this.topPos + 46, btnW, 18).build();

        this.othersButton = Button.builder(getOthersText(), btn -> {
            this.menu.setAllowOthers(!this.menu.isAllowOthers());
            btn.setMessage(getOthersText());
            sendUpdate();
        }).bounds(btnX, this.topPos + 68, btnW, 18).build();

        this.refreshButton = Button.builder(Component.literal("Активировать/Обновить"), btn -> sendRefresh())
                .bounds(btnX, this.topPos + 90, btnW, 18).build();

        this.addRenderableWidget(this.partyButton);
        this.addRenderableWidget(this.alliesButton);
        this.addRenderableWidget(this.othersButton);
        this.addRenderableWidget(this.refreshButton);
    }

    private Component getPartyText() {
        return Component.literal(this.menu.isAllowParty()
                ? "Группа:  §a✔ Разрешено"
                : "Группа:  §c✘ Запрещено");
    }

    private Component getAlliesText() {
        return Component.literal(this.menu.isAllowAllies()
                ? "Союзники:  §a✔ Разрешено"
                : "Союзники:  §c✘ Запрещено");
    }

    private Component getOthersText() {
        return Component.literal(this.menu.isAllowOthers()
                ? "Остальные:  §a✔ Разрешено"
                : "Остальные:  §c✘ Запрещено");
    }

    private void sendUpdate() {
        VSClaimsNetwork.CHANNEL.sendToServer(new UpdateClaimSettingsPacket(
                this.menu.getCenter(),
                this.menu.isAllowParty(),
                this.menu.isAllowAllies(),
                this.menu.isAllowOthers()
        ));
    }

    private void sendRefresh() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshTime > 30000) {
            VSClaimsNetwork.CHANNEL.sendToServer(new RefreshClaimPacket(this.menu.getCenter()));
            this.menu.setClaimActive(true);
            lastRefreshTime = now;
            refreshButton.active = false;
            refreshButton.setMessage(Component.literal("Подождите 30 сек..."));
        }
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics g, float partialTick, int mx, int my) {
        g.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderLabels(net.minecraft.client.gui.GuiGraphics g, int mx, int my) {
        // Координаты относительно угла фона (leftPos/topPos вычтены движком)

        // Заголовок по центру
        String title = "Настройки привата";
        g.drawString(this.font, title,
                (this.imageWidth - this.font.width(title)) / 2, 7, COLOR_TITLE, false);

        // Разделитель под заголовком
        g.fill(10, 18, this.imageWidth - 10, 19, 0x66888888);

        // Разделитель перед инфо-блоком
        g.fill(10, 116, this.imageWidth - 10, 117, 0x66888888);

        // Статус привата
        String statusText = this.menu.isClaimActive() ? "активен" : "отключен";
        int statusColor = this.menu.isClaimActive() ? 0x22AA22 : 0xCC3333;
        int prefixWidth = this.font.width("Приват: ");
        g.drawString(this.font, "Приват: ", 10, 120, COLOR_LABEL, false);
        g.drawString(this.font, statusText, 10 + prefixWidth, 120, statusColor, false);

        // Владелец
        try {
            String name = Minecraft.getInstance()
                    .getConnection()
                    .getPlayerInfo(menu.getOwner())
                    .getProfile()
                    .getName();
            g.drawString(this.font, "Владелец: " + name, 10, 132, COLOR_LABEL, false);
        } catch (Exception ignored) {}

        // Корабль
        if (ship != null) {
            try {
                String slug = (String) ship.getClass().getMethod("getSlug").invoke(ship);
                if (slug != null && !slug.isEmpty()) {
                    Component shipText = Component.literal("Корабль: " + slug);
                    int x = 10;
                    int y = 146;
                    int maxWidth = this.imageWidth - 20;
                    int line = 0;
                    for (FormattedCharSequence seq : this.font.split(shipText, maxWidth)) {
                        g.drawString(this.font, seq, x, y + (line * this.font.lineHeight), COLOR_LABEL, false);
                        line++;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics g, int mx, int my, float partialTick) {
        this.renderBackground(g);
        super.render(g, mx, my, partialTick);
        this.renderTooltip(g, mx, my);

        if (!refreshButton.active && System.currentTimeMillis() - lastRefreshTime > 30000) {
            refreshButton.active = true;
            refreshButton.setMessage(Component.literal("Активировать/Обновить"));
        }
    }
}