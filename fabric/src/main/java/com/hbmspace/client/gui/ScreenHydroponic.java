// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.GUIElements;
import com.hbm.client.gui.ScreenInfoContainer;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityHydroponic;
import com.hbmspace.inventory.container.MenuHydroponic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ScreenHydroponic extends ScreenInfoContainer<MenuHydroponic> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_hydrobay.png");

    private static final int TANK_TOP = 18, TANK_HEIGHT = 52, WIDTH = 16;
    private static final int[] TANK_X = {13, 31};
    private static final int POWER_X = 147, POWER_BOTTOM = 52, POWER_HEIGHT = 34;

    public ScreenHydroponic(MenuHydroponic menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 186);
        this.titleLabelY = 5;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected int titleCenterX() {
        return 97;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                0,
                0,
                0.0F,
                0.0F,
                imageWidth,
                imageHeight,
                256,
                256);

        long power = menu.getPower();
        int bar = (int) (power * POWER_HEIGHT / BlockEntityHydroponic.MAX_POWER);
        if (bar > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    POWER_X,
                    POWER_BOTTOM - bar,
                    176,
                    POWER_HEIGHT - bar,
                    WIDTH,
                    bar,
                    256,
                    256);
        }
        int fertilizer = menu.getFertilizer() * 32 / BlockEntityHydroponic.MAX_FERTILIZER;
        if (fertilizer > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    102,
                    69 - fertilizer,
                    192,
                    32 - fertilizer,
                    6,
                    fertilizer,
                    256,
                    256);
        }
        if (power >= BlockEntityHydroponic.LIGHT_POWER) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 151, 4, 176, 34, 9, 12, 256, 256);
        }

        BlockEntityHydroponic be = menu.blockEntity();
        for (int i = 0; i < TANK_X.length; i++) {
            drawFluidBar(graphics, TANK_X[i], TANK_TOP, WIDTH, TANK_HEIGHT, be.tanks[i]);
        }
        for (int i = 0; i < TANK_X.length; i++) {
            drawFluidGaugeInfo(
                    graphics, mouseX, mouseY, TANK_X[i], TANK_TOP, WIDTH, TANK_HEIGHT, be.tanks[i]);
        }
        drawElectricityInfo(
                graphics,
                mouseX,
                mouseY,
                POWER_X,
                POWER_BOTTOM - POWER_HEIGHT,
                WIDTH,
                POWER_HEIGHT,
                power,
                BlockEntityHydroponic.MAX_POWER);

        super.extractLabels(graphics, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Slot slot = menu.getSlot(BlockEntityHydroponic.SLOT_FERTILIZER);
        if (!menu.getCarried().isEmpty()
                || slot.hasItem()
                || !isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            return;
        }
        GUIElements.drawCyclingStackText(
                graphics,
                this.font,
                null,
                BlockEntityHydroponic.validFertilizers(),
                mouseX,
                mouseY,
                this.width,
                this.height);
    }
}
