/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.client.gui;

import com.hbm.client.gui.InfoContainerScreen;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.CryoDistillBlockEntity;
import com.hbmspace.inventory.container.CryoDistillMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CryoDistillScreen extends InfoContainerScreen<CryoDistillMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/processing/gui_cryodistill.png");

    private static final int BOTTOM = 70, HEIGHT = 52, WIDTH = 16;
    private static final int POWER_X = 26;
    private static final int[] TANK_X = {44, 80, 98, 116, 134};

    public CryoDistillScreen(CryoDistillMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 238);
        this.titleLabelY = 5;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected int titleColor() {
        return 0xFFFFFFFF;
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
        int filled = (int) (power * 54 / CryoDistillBlockEntity.MAX_POWER);
        if (filled > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    POWER_X,
                    BOTTOM - filled,
                    176,
                    HEIGHT - filled,
                    WIDTH,
                    filled,
                    256,
                    256);
        }

        CryoDistillBlockEntity be = menu.blockEntity();
        for (int i = 0; i < TANK_X.length; i++) {
            drawFluidBar(graphics, TANK_X[i], BOTTOM - HEIGHT, WIDTH, HEIGHT, be.tanks[i]);
        }
        for (int i = 0; i < TANK_X.length; i++) {
            drawFluidGaugeInfo(
                    graphics,
                    mouseX,
                    mouseY,
                    TANK_X[i],
                    BOTTOM - HEIGHT,
                    WIDTH,
                    HEIGHT,
                    be.tanks[i]);
        }
        drawElectricityInfo(
                graphics,
                mouseX,
                mouseY,
                POWER_X,
                BOTTOM - HEIGHT,
                WIDTH,
                HEIGHT,
                power,
                CryoDistillBlockEntity.MAX_POWER);

        super.extractLabels(graphics, mouseX, mouseY);
    }
}
