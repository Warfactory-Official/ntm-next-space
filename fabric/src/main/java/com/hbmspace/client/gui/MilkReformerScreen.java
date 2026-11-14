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
import com.hbmspace.blockentity.machine.MilkReformerBlockEntity;
import com.hbmspace.inventory.container.MilkReformerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MilkReformerScreen extends InfoContainerScreen<MilkReformerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/processing/gui_milk_reformer.png");

    private static final int POWER_X = 98,
            POWER_Y = 13,
            POWER_U = 176,
            POWER_V = 6,
            POWER_W = 63,
            POWER_H = 6;
    private static final int[][] TANK_BARS = {
        {50, 32, 5, 30}, {101, 53, 3, 10}, {128, 53, 3, 10}, {155, 53, 3, 10}
    };
    private static final int[][] TANK_INFO = {
        {45, 38, 16, 29}, {90, 55, 21, 10}, {117, 55, 21, 10}, {143, 55, 21, 10}
    };

    public MilkReformerScreen(MilkReformerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 238);
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected boolean drawTitle() {
        return false;
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
        int filled = (int) (power * POWER_W / MilkReformerBlockEntity.MAX_POWER);
        if (filled > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    POWER_X,
                    POWER_Y,
                    POWER_U,
                    POWER_V,
                    filled,
                    POWER_H,
                    256,
                    256);
        }

        MilkReformerBlockEntity be = menu.blockEntity();
        for (int i = 0; i < TANK_BARS.length; i++) {
            int[] bar = TANK_BARS[i];
            drawFluidBar(graphics, bar[0], bar[1], bar[2], bar[3], be.tanks[i]);
        }

        for (int i = 0; i < TANK_INFO.length; i++) {
            int[] info = TANK_INFO[i];
            drawFluidGaugeInfo(
                    graphics, mouseX, mouseY, info[0], info[1], info[2], info[3], be.tanks[i]);
        }
        drawElectricityInfo(
                graphics, mouseX, mouseY, 98, 8, 70, 20, power, MilkReformerBlockEntity.MAX_POWER);

        super.extractLabels(graphics, mouseX, mouseY);
    }
}
