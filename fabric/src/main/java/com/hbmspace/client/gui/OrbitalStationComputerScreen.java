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
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.inventory.container.OrbitalStationComputerMenu;
import com.hbmspace.packet.layout.SpaceOrbitalStationGravity;
import com.hbmspace.packet.layout.SpaceOrbitalStationName;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class OrbitalStationComputerScreen extends InfoContainerScreen<OrbitalStationComputerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_ship_computer.png");

    private static final int GRAVITY_X = 36, GRAVITY_Y = 84, GRAVITY_W = 29, GRAVITY_H = 5;

    private EditBox stationName;

    public OrbitalStationComputerScreen(
            OrbitalStationComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 198, 135);
    }

    @Override
    protected void init() {
        super.init();
        stationName = new EditBox(font, leftPos + 52, topPos + 26, 136, 12, Component.empty());
        stationName.setTextColor(0xFFFFFFFF);
        stationName.setTextColorUneditable(0xFFDDDDDD);
        stationName.setBordered(false);
        stationName.setValue(OrbitalStation.clientStation.name);
        stationName.setResponder(this::setName);
        addRenderableWidget(stationName);
        setInitialFocus(stationName);
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

        if (OrbitalStation.clientStation.gravityMultiplier > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + GRAVITY_X,
                    topPos + GRAVITY_Y,
                    imageWidth,
                    0,
                    GRAVITY_W,
                    GRAVITY_H,
                    256,
                    256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);

        if (checkClick(
                (int) event.x(), (int) event.y(), GRAVITY_X, GRAVITY_Y, GRAVITY_W, GRAVITY_H)) {
            playClick();
            setGravity(OrbitalStation.clientStation.gravityMultiplier == 0);
            return true;
        }
        return handled;
    }

    private void setName(String name) {
        PacketWire.sendToServer(SpaceOrbitalStationName.encode(menu.containerId, name));
    }

    private void setGravity(boolean enabled) {
        PacketWire.sendToServer(SpaceOrbitalStationGravity.encode(menu.containerId, enabled));
    }
}
