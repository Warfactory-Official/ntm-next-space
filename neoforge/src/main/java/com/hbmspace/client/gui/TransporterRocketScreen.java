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
import com.hbmspace.blockentity.machine.TransporterRocketBlockEntity;
import com.hbmspace.inventory.container.TransporterRocketMenu;
import com.hbmspace.packet.layout.SpaceTransporterName;
import com.hbmspace.packet.layout.SpaceTransporterThreshold;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class TransporterRocketScreen extends InfoContainerScreen<TransporterRocketMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_transporter.png");

    private EditBox transporterName;

    public TransporterRocketScreen(
            TransporterRocketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 230, 236);
    }

    private TransporterRocketBlockEntity pad() {
        return menu.blockEntity();
    }

    @Override
    protected void init() {
        super.init();
        transporterName = new EditBox(font, leftPos + 8, topPos + 12, 122, 12, Component.empty());
        transporterName.setTextColor(0xFF00FF00);
        transporterName.setTextColorUneditable(0xFF00FF00);
        transporterName.setBordered(false);
        transporterName.setValue(pad().getTransporterName());
        transporterName.setResponder(this::setName);
        addRenderableWidget(transporterName);
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos,
                0.0F,
                0.0F,
                imageWidth,
                imageHeight,
                256,
                256);

        TransporterRocketBlockEntity pad = pad();
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos + 100 + (int) (pad.threshold * 6.78),
                topPos + 122,
                imageWidth,
                0,
                4,
                15,
                256,
                256);

        String threshold = "x" + pad.getThreshold();
        graphics.text(
                font,
                threshold,
                leftPos + 167 - font.width(threshold),
                topPos + 12,
                0xFFFFFFFF,
                true);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TransporterRocketBlockEntity pad = pad();
        for (int i = 0; i < 4; i++) {
            drawFluidBar(graphics, 8 + i * 18, 70, 16, 34, pad.tanks[i]);
            drawFluidBar(graphics, 98 + i * 18, 70, 16, 34, pad.tanks[i + 4]);
        }
        for (int i = 0; i < 2; i++)
            drawFluidBar(graphics, 188 + i * 18, 18, 16, 70, pad.tanks[i + 8]);

        for (int i = 0; i < 4; i++) {
            drawFluidGaugeInfo(graphics, mouseX, mouseY, 8 + i * 18, 70, 16, 34, pad.tanks[i]);
            drawFluidGaugeInfo(graphics, mouseX, mouseY, 98 + i * 18, 70, 16, 34, pad.tanks[i + 4]);
        }
        for (int i = 0; i < 2; i++) {
            drawFluidGaugeInfo(
                    graphics, mouseX, mouseY, 188 + i * 18, 18, 16, 70, pad.tanks[i + 8]);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int mx = (int) event.x(), my = (int) event.y();
        int x = leftPos + 98, y = topPos + 120;
        if (x <= mx && x + 74 > mx && y <= my && y + 20 > my) {
            int slidPos = Mth.clamp((int) ((mx - x) / 6.78), 0, 10);
            PacketWire.sendToServer(SpaceTransporterThreshold.encode(menu.containerId, slidPos));
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    private void setName(String name) {
        PacketWire.sendToServer(SpaceTransporterName.encode(menu.containerId, name));
    }
}
