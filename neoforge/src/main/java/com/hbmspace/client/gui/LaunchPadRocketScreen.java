/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2025 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2025 George Paton. All rights reserved.
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
import com.hbmspace.blockentity.bomb.LaunchPadRocketBlockEntity;
import com.hbmspace.inventory.container.LaunchPadRocketMenu;
import com.hbmspace.packet.layout.SpaceLaunchPadRocketLaunch;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LaunchPadRocketScreen extends InfoContainerScreen<LaunchPadRocketMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_launchpad_rocket.png");
    private static final int WIDTH = 188;

    private final RocketPreview preview = new RocketPreview(97, 18, 50, 106, 122, 119);

    public LaunchPadRocketScreen(LaunchPadRocketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 236);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        LaunchPadRocketBlockEntity pad = menu.blockEntity();

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

        int p = (int) (pad.power * 52 / LaunchPadRocketBlockEntity.MAX_POWER);
        if (p > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    167,
                    36 + 52 - p,
                    WIDTH,
                    8 + 52 - p,
                    16,
                    p,
                    256,
                    256);
        }

        if (pad.rocket != null) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    59,
                    43,
                    WIDTH + (pad.canLaunch() ? 12 : 0),
                    0,
                    12,
                    8,
                    256,
                    256);
            int ox = pad.power > LaunchPadRocketBlockEntity.MAX_POWER * 0.75 ? 0 : 6;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    172,
                    23,
                    WIDTH + 16 + ox,
                    8,
                    6,
                    8,
                    256,
                    256);

            float scale = (float) (86 / Math.max(pad.rocket.getHeight(), 6));
            preview.draw(
                    (state, s, translation, rotation, left, top, width, height) ->
                            submitEntity(
                                    graphics,
                                    state,
                                    s,
                                    translation,
                                    rotation,
                                    left,
                                    top,
                                    width,
                                    height),
                    pad.rocket,
                    scale,
                    0F);

            graphics.pose().pushMatrix();
            graphics.pose().scale(0.5F, 0.5F);
            List<Component> issues = pad.findIssues();
            for (int i = 0; i < issues.size(); i++) {
                graphics.text(font, issues.get(i), 6 * 2, 66 * 2 + i * 8, 0xFFFFFFFF, false);
            }
            graphics.pose().popMatrix();
        }

        drawElectricityInfo(
                graphics,
                mouseX,
                mouseY,
                167,
                36,
                16,
                52,
                pad.power,
                LaunchPadRocketBlockEntity.MAX_POWER);
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                56,
                20,
                18,
                17,
                List.of(Component.translatable("desc.gui.launchPadRocket.launch")));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);

        LaunchPadRocketBlockEntity pad = menu.blockEntity();
        if (pad.rocket != null
                && pad.rocket.validate()
                && checkClick((int) event.x(), (int) event.y(), 56, 20, 18, 17)) {
            playClick();
            PacketWire.sendToServer(SpaceLaunchPadRocketLaunch.encode(menu.containerId));
        }

        return handled;
    }
}
