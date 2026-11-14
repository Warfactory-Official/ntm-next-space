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
import com.hbm.items.machine.upgrade.UpgradeType;
import com.hbmspace.HbmSpace;
import com.hbmspace.inventory.container.VacuumCircuitMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class VacuumCircuitScreen extends InfoContainerScreen<VacuumCircuitMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/processing/gui_vac_cir_station.png");

    private static final int POWER_X = 143, POWER_BOTTOM = 70, POWER_W = 16, POWER_H = 52;
    private static final int PROG_X = 63,
            PROG_Y = 28,
            PROG_U = 192,
            PROG_V = 0,
            PROG_W = 33,
            PROG_H = 14;
    private static final int READY_X = 146,
            READY_Y = 4,
            READY_U = 176,
            READY_V = 52,
            READY_W = 9,
            READY_H = 12;
    private static final int UPGRADE_X = 78, UPGRADE_Y = 76, UPGRADE_W = 8, UPGRADE_H = 8;

    public VacuumCircuitScreen(VacuumCircuitMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 204);
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected int titleCenterX() {
        return this.imageWidth / 2 - 18;
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
        long maxPower = menu.getMaxPower();
        int filled = (int) (power * POWER_H / Math.max(maxPower, 1L));
        if (filled > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    POWER_X,
                    POWER_BOTTOM - filled,
                    176,
                    POWER_H - filled,
                    POWER_W,
                    filled,
                    256,
                    256);
        }

        int arrow = menu.getProgress() * PROG_W / Math.max(menu.getProcessTime(), 1);
        if (arrow > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    PROG_X,
                    PROG_Y,
                    PROG_U,
                    PROG_V,
                    arrow,
                    PROG_H,
                    256,
                    256);
        }

        if (power >= menu.getConsumption()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    READY_X,
                    READY_Y,
                    READY_U,
                    READY_V,
                    READY_W,
                    READY_H,
                    256,
                    256);
        }

        drawInfoPanel(graphics, UPGRADE_X, UPGRADE_Y, UPGRADE_W, UPGRADE_H, 8);
        drawElectricityInfo(
                graphics,
                mouseX,
                mouseY,
                POWER_X,
                POWER_BOTTOM - POWER_H,
                POWER_W,
                POWER_H,
                power,
                maxPower);
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                UPGRADE_X,
                UPGRADE_Y,
                UPGRADE_W,
                UPGRADE_H,
                UPGRADE_X,
                UPGRADE_Y,
                upgradeInfo(menu.blockEntity(), UpgradeType.SPEED, UpgradeType.POWER));

        super.extractLabels(graphics, mouseX, mouseY);
    }
}
