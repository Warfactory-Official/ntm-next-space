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

import com.hbm.client.gui.InfoContainerScreen;
import com.hbm.items.SatelliteFrequencyProvider;
import com.hbm.packet.PacketWire;
import com.hbm.saveddata.satellites.Satellite;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.WarControllerBlockEntity;
import com.hbmspace.inventory.container.WarControllerMenu;
import com.hbmspace.packet.layout.SpaceWarControllerTarget;
import com.hbmspace.saveddata.satellites.ClientSatellites;
import com.hbmspace.saveddata.satellites.SatelliteWar;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public class WarControllerScreen extends InfoContainerScreen<WarControllerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_controlpanel.png");
    private static final int MAX_HISTORY = 6;
    private static final int GREEN = 0xFF00FF00;

    private List<Component> history = new ArrayList<>();
    private EditBox command;

    public WarControllerScreen(WarControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 204);
    }

    @Override
    protected void init() {
        super.init();
        command = new EditBox(font, leftPos - 74, topPos + 28, 100, 20, Component.empty());
        command.setMaxLength(100);
        command.setBordered(false);
        addWidget(command);
        setInitialFocus(command);
    }

    @Override
    protected EditBox typingField() {
        return command;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return command.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN) {
            processCommand(command.getValue().trim());
            command.setValue("");
            return true;
        }
        return super.keyPressed(event);
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
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5F, 0.5F);
        int y = 15 * 2;
        for (int i = Math.max(0, history.size() - MAX_HISTORY); i < history.size(); i++) {
            graphics.text(font, history.get(i), 78 * 2, y, GREEN, false);
            y += 12;
        }
        boolean cursor = command.isFocused() && minecraft.level.getGameTime() % 20 < 10;
        graphics.text(
                font,
                "> " + command.getValue() + (cursor ? "_" : ""),
                78 * 2,
                57 * 2,
                GREEN,
                false);
        graphics.pose().popMatrix();

        drawElectricityInfo(graphics, mouseX, mouseY, 132, 18, 16, 52, 0, 0);
    }

    private void processCommand(String line) {
        if (line.isEmpty()) return;

        WarControllerBlockEntity controller = menu.blockEntity();
        int satId =
                SatelliteFrequencyProvider.frequency(
                        controller.getItem(WarControllerBlockEntity.SLOT_CHIP));
        ClientSatellites.@Nullable Entry entry =
                ClientSatellites.current(minecraft.level).get(satId);
        @Nullable Satellite sat = entry == null ? null : entry.type().create();
        boolean inOrbit = sat != null;
        String[] parts = line.split(" ");

        switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "setpos" -> {
                add(Component.literal("> " + line));
                if (parts.length < 3) {
                    add("invalidArgs", ChatFormatting.RED);
                    return;
                }
                if (controller.getItem(WarControllerBlockEntity.SLOT_DRIVE).isEmpty()) {
                    add("noDrive", ChatFormatting.RED);
                    return;
                }
                if (!parts[1].matches("-?\\d+") || !parts[2].matches("-?\\d+")) {
                    add("invalidNumber", ChatFormatting.RED);
                    return;
                }
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                add(Component.translatable("desc.gui.warController.setTo", x, z));

                PacketWire.sendToServer(SpaceWarControllerTarget.encode(menu.containerId, x, z));
            }
            case "health" -> {
                add(Component.literal("> " + line));
                add(
                        Component.translatable(
                                "desc.gui.warController.requesting",
                                parts[0].toLowerCase(Locale.ROOT)));
                if (!inOrbit) add("notInOrbit", ChatFormatting.RED);
                else add(Component.translatable("desc.gui.warController.health", 0));
            }
            case "fire" -> {
                if (!inOrbit) add("notInOrbit", ChatFormatting.RED);
                else if (sat instanceof SatelliteWar) add("firing", null);
                else add("wrongSatellite", null);
            }
            case "getsat" -> {
                add(Component.literal("> " + line));
                if (controller.getItem(WarControllerBlockEntity.SLOT_CHIP).isEmpty()) {
                    add("noChip", ChatFormatting.RED);
                } else {
                    add(Component.translatable("desc.gui.warController.requestingId", satId));
                    if (!inOrbit) add("notInOrbit", ChatFormatting.RED);
                    else
                        add(
                                Component.translatable(
                                        "desc.gui.warController.satellite",
                                        sat.getClass().getSimpleName()));
                }
            }
            default -> {
                add(Component.literal("> " + line));
                add("unknown", ChatFormatting.RED);
            }
        }
    }

    private void add(String key, @Nullable ChatFormatting color) {
        MutableComponent line = Component.translatable("desc.gui.warController." + key);
        add(color == null ? line : line.withStyle(color));
    }

    private void add(Component line) {
        history.add(line);
        if (history.size() > MAX_HISTORY * 2) {
            history =
                    new ArrayList<>(
                            history.subList(history.size() - MAX_HISTORY * 2, history.size()));
        }
    }
}
