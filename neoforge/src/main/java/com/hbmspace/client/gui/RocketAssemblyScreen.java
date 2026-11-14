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
import com.hbm.util.GameTime;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.RocketAssemblyBlockEntity;
import com.hbmspace.dim.Target;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.RocketAssemblyMenu;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.packet.layout.SpaceRocketAssemblyConstruct;
import com.hbmspace.packet.layout.SpaceRocketAssemblyDeconstruct;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class RocketAssemblyScreen extends InfoContainerScreen<RocketAssemblyMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_rocket_assembly.png");
    private static final int WIDTH = 192;

    private final RocketPreview preview = new RocketPreview(65, 5, 90, 106, 116, 103);
    private double currentScale = 1;
    private double currentOffset;
    private long lastTime = GameTime.now();

    public RocketAssemblyScreen(RocketAssemblyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 224);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        RocketAssemblyBlockEntity be = menu.blockEntity();
        RocketStruct rocket = be.rocket;
        int layer = menu.layer();

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

        int stage = Math.max(rocket.stages.size() - 1 - layer, -1);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                47,
                39,
                WIDTH + 18 + (stage + 1) * 6,
                0,
                6,
                8,
                256,
                256);
        stage = Math.max(stage, 0);

        long now = GameTime.now();
        double dt = (now - lastTime) / 1000D;
        lastTime = now;
        double targetScale = 86 / Math.max(rocket.getHeight(stage), 6);
        currentScale = currentScale + (targetScale - currentScale) * dt * 4;
        currentOffset = currentOffset + (rocket.getOffset(stage) - currentOffset) * dt * 4;
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
                rocket,
                (float) currentScale,
                (float) currentOffset);

        Target from =
                VOTVdriveItem.getTarget(
                        be.getItem(RocketAssemblyBlockEntity.driveFrom(layer)), be.getLevel());
        Target to =
                VOTVdriveItem.getTarget(
                        be.getItem(RocketAssemblyBlockEntity.driveTo(layer)), be.getLevel());

        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5F, 0.5F);
        List<Component> issues =
                rocket.findIssues(stage, from.body(), to.body(), from.inOrbit(), to.inOrbit());
        for (int i = 0; i < issues.size(); i++) {
            graphics.text(font, issues.get(i), 65 * 2, 5 * 2 + i * 8, 0xFFFFFFFF, true);
        }
        if (from.body() != null) {
            graphics.text(
                    font,
                    Component.translatable("body." + from.body().name),
                    162 * 2,
                    75 * 2,
                    0xFF00FF00,
                    false);
        }
        if (to.body() != null) {
            graphics.text(
                    font,
                    Component.translatable("body." + to.body().name),
                    162 * 2,
                    108 * 2,
                    0xFF00FF00,
                    false);
        }
        graphics.pose().popMatrix();

        if (checkClick(mouseX, mouseY, 17, 34, 18, 8)) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, TEXTURE, 17, 34, WIDTH, 36, 18, 8, 256, 256);
        }
        if (checkClick(mouseX, mouseY, 17, 98, 18, 8)) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, TEXTURE, 17, 98, WIDTH, 44, 18, 8, 256, 256);
        }
        if (rocket.validate()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, TEXTURE, 41, 62, WIDTH + 18, 8, 18, 18, 256, 256);
        }
        if (be.canDeconstruct()) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, TEXTURE, 39, 52, WIDTH + 36, 8, 20, 38, 256, 256);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = (int) event.x(), y = (int) event.y();
        RocketAssemblyBlockEntity be = menu.blockEntity();

        if (checkClick(x, y, 17, 34, 18, 8)) {
            playClick();
            if (menu.layer() > 0) turnTo(menu.layer() - 1);
        }

        if (checkClick(x, y, 17, 98, 18, 8)) {
            playClick();
            if (menu.layer() < Math.min(be.rocket.stages.size(), RocketStruct.MAX_STAGES - 1)) {
                turnTo(menu.layer() + 1);
            }
        }

        if (checkClick(x, y, 41, 62, 18, 18)) {
            if (be.rocket.validate()) {
                playClick();
                PacketWire.sendToServer(SpaceRocketAssemblyConstruct.encode(menu.containerId));
            } else if (be.canDeconstruct()) {
                playClick();
                PacketWire.sendToServer(SpaceRocketAssemblyDeconstruct.encode(menu.containerId));
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void turnTo(int layer) {
        menu.setLayer(layer);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, layer);
    }
}
