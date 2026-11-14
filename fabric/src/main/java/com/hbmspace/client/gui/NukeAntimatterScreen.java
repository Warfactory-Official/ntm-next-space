/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 DUODEC
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 DUODEC. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
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
import com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity;
import com.hbmspace.inventory.container.NukeAntimatterMenu;
import com.hbmspace.packet.layout.SpaceAntimatterStart;
import com.hbmspace.packet.layout.SpaceAntimatterTimer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class NukeAntimatterScreen extends InfoContainerScreen<NukeAntimatterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/weapon/blombschematic.png");

    private static final int FIELD_X = 80, FIELD_Y = 108, FIELD_W = 29, FIELD_H = 12;
    private static final int START_X = 80, START_Y = 85, START_SIZE = 18;
    private static final float CLOCK_SCALE = 0.6F;
    private static final int CLOCK_CENTRE = 95, CLOCK_Y = 66;

    public NukeAntimatterScreen(NukeAntimatterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
    }

    @Override
    protected void init() {
        super.init();
        EditBox field =
                new EditBox(
                        font,
                        leftPos + FIELD_X,
                        topPos + FIELD_Y,
                        FIELD_W,
                        FIELD_H,
                        Component.empty());
        field.setTextColor(0xFFFF0000);
        field.setTextColorUneditable(0xFF800000);
        field.setBordered(false);
        field.setMaxLength(3);
        field.setValue(String.valueOf(menu.blockEntity().timer / 20));
        field.setResponder(this::commit);
        addRenderableWidget(field);
    }

    private void commit(String text) {
        if (text.isEmpty() || !text.chars().allMatch(Character::isDigit)) return;
        int seconds =
                Math.clamp(
                        Integer.parseInt(text),
                        NukeAntimatterBlockEntity.MIN_SECONDS,
                        NukeAntimatterBlockEntity.MAX_SECONDS);
        PacketWire.sendToServer(SpaceAntimatterTimer.encode(menu.containerId, seconds));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (!menu.blockEntity().started
                && checkClick(
                        (int) event.x(),
                        (int) event.y(),
                        START_X,
                        START_Y,
                        START_SIZE,
                        START_SIZE)) {
            PacketWire.sendToServer(SpaceAntimatterStart.encode(menu.containerId));
            return true;
        }
        return handled;
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
        if (menu.blockEntity().started) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + START_X,
                    topPos + START_Y,
                    176.0F,
                    26.0F,
                    START_SIZE,
                    START_SIZE,
                    256,
                    256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        NukeAntimatterBlockEntity bomb = menu.blockEntity();

        graphics.text(font, title, (imageWidth - font.width(title)) / 2, 6, -12566464, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 96 + 2, -12566464, false);

        if (bomb.core) {
            Component clock = Component.literal(bomb.getMinutes() + ":" + bomb.getSeconds());
            graphics.pose().pushMatrix();
            graphics.pose().scale(CLOCK_SCALE, CLOCK_SCALE);
            graphics.text(
                    font,
                    clock,
                    (int) ((CLOCK_CENTRE - font.width(clock) / 2) / CLOCK_SCALE),
                    (int) (CLOCK_Y / CLOCK_SCALE),
                    0xFFFF0000,
                    false);
            graphics.pose().popMatrix();
        }
    }
}
