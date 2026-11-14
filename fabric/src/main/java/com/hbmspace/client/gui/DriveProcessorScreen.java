/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

import com.hbm.client.gui.InfoContainerScreen;
import com.hbm.packet.PacketWire;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.DriveProcessorBlockEntity;
import com.hbmspace.inventory.container.DriveProcessorMenu;
import com.hbmspace.packet.layout.SpaceDriveProcessorClone;
import com.hbmspace.packet.layout.SpaceDriveProcessorProcess;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DriveProcessorScreen extends InfoContainerScreen<DriveProcessorMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/processing/gui_drive_processor.png");
    private static final int WIDTH = 176;

    public DriveProcessorScreen(DriveProcessorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 207);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        DriveProcessorBlockEntity machine = menu.blockEntity();

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                0,
                0,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256);

        if (machine.isProcessing) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 38, 61, 192, 28, 18, 18, 256, 256);
        }

        int p = (int) (machine.power * 52 / DriveProcessorBlockEntity.MAX_POWER);
        if (p > 0)
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    134,
                    18 + 52 - p,
                    WIDTH,
                    52 - p,
                    16,
                    p,
                    256,
                    256);

        if (machine.isProcessing) {
            p = machine.progress * 44 / DriveProcessorBlockEntity.MAX_PROGRESS;
            if (p > 0)
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED, TEXTURE, 59, 63, 192, 14, p, 14, 256, 256);
        }

        graphics.text(font, machine.status.text(), 41, 86, 0xFFFFFFFF, false);

        drawElectricityInfo(
                graphics,
                mouseX,
                mouseY,
                134,
                18,
                16,
                52,
                machine.power,
                DriveProcessorBlockEntity.MAX_POWER);
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                49,
                17,
                18,
                18,
                List.of(Component.translatable("desc.gui.driveProcessor.clone")));
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                38,
                61,
                18,
                18,
                List.of(Component.translatable("desc.gui.driveProcessor.process")));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);

        DriveProcessorBlockEntity machine = menu.blockEntity();
        int x = (int) event.x();
        int y = (int) event.y();
        if (checkClick(x, y, 38, 61, 18, 18)) {
            playClick();
            PacketWire.sendToServer(
                    SpaceDriveProcessorProcess.encode(menu.containerId, !machine.isProcessing));
        }
        if (checkClick(x, y, 49, 17, 18, 18)) {
            playClick();
            PacketWire.sendToServer(SpaceDriveProcessorClone.encode(menu.containerId));
        }

        return handled;
    }
}
