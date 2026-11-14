/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2025 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.client.gui;

import com.hbm.client.gui.GUIElements;
import com.hbm.client.gui.InfoContainerScreen;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.hbmspace.inventory.container.HydroponicMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class HydroponicScreen extends InfoContainerScreen<HydroponicMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_hydrobay.png");

    private static final int TANK_TOP = 18, TANK_HEIGHT = 52, WIDTH = 16;
    private static final int[] TANK_X = {13, 31};
    private static final int POWER_X = 147, POWER_BOTTOM = 52, POWER_HEIGHT = 34;

    public HydroponicScreen(HydroponicMenu menu, Inventory inventory, Component title) {
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
        int bar = (int) (power * POWER_HEIGHT / HydroponicBlockEntity.MAX_POWER);
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
        int fertilizer = menu.getFertilizer() * 32 / HydroponicBlockEntity.MAX_FERTILIZER;
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
        if (power >= HydroponicBlockEntity.LIGHT_POWER) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 151, 4, 176, 34, 9, 12, 256, 256);
        }

        HydroponicBlockEntity be = menu.blockEntity();
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
                HydroponicBlockEntity.MAX_POWER);

        super.extractLabels(graphics, mouseX, mouseY);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Slot slot = menu.getSlot(HydroponicBlockEntity.SLOT_FERTILIZER);
        if (!menu.getCarried().isEmpty()
                || slot.hasItem()
                || !isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            return;
        }
        GUIElements.drawCyclingStackText(
                graphics,
                this.font,
                null,
                HydroponicBlockEntity.validFertilizers(),
                mouseX,
                mouseY,
                this.width,
                this.height);
    }
}
