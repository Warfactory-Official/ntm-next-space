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

import com.hbm.client.gui.MenuScreen;
import com.hbm.packet.PacketWire;
import com.hbmspace.HbmSpace;
import com.hbmspace.inventory.container.TransporterLinkerMenu;
import com.hbmspace.items.StationItemsSpace;
import com.hbmspace.items.tool.TransporterInfo;
import com.hbmspace.packet.layout.SpaceTransporterLink;
import com.hbmspace.packet.layout.SpaceTransporterUnlink;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public class TransporterLinkerScreen extends MenuScreen<TransporterLinkerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/tool/gui_transport_linker.png");
    private static final int X_SIZE = 218, Y_SIZE = 201;
    private static final int ROWS = 5;

    private final TransporterInfo linkFromTransporter;
    private final @Nullable TransporterInfo linkToTransporter;
    private final List<TransporterInfo> transporters;
    private List<TransporterInfo> visibleTransporters = List.of();

    private EditBox search;
    private int index = 0;
    private int guiLeft;
    private int guiTop;

    public TransporterLinkerScreen(
            TransporterLinkerMenu menu, Inventory inventory, Component title) {
        super(menu, title);
        this.linkFromTransporter = menu.opening.from();
        this.linkToTransporter = linkFromTransporter.linkedTo();
        this.transporters = menu.opening.transporters();
    }

    @Override
    protected void init() {
        guiLeft = (width - X_SIZE) / 2;
        guiTop = (height - Y_SIZE) / 2;

        search = new EditBox(font, guiLeft + 59, guiTop + 46, 86, 12, Component.empty());
        search.setTextColor(0xFFFFFFFF);
        search.setTextColorUneditable(0xFFFFFFFF);
        search.setBordered(false);
        search.setResponder(text -> updateSearch());
        addRenderableWidget(search);
        setInitialFocus(search);

        updateSearch();
    }

    @Override
    public void tick() {
        if (!minecraft.player.getMainHandItem().is(StationItemsSpace.TRANSPORTER_LINKER.get()))
            onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int hideScroll = visibleTransporters.size() <= ROWS ? 20 : 0;
        blit(graphics, guiLeft, guiTop, 0, 0, X_SIZE - hideScroll, Y_SIZE);

        if (search.isFocused()) blit(graphics, guiLeft + 55, guiTop + 41, 0, Y_SIZE, 107, 18);

        int x = guiLeft + 37;
        drawTransporter(graphics, linkFromTransporter, x, guiTop + 14, guiTop + 13, guiTop + 23);
        for (int i = index; i < Math.min(index + ROWS, visibleTransporters.size()); i++) {
            int y = guiTop + 70 + (i - index) * 26;
            drawTransporter(graphics, visibleTransporters.get(i), x, y, y - 1, y + 9);
        }

        x = guiLeft + 10;
        if (isInAABB(mouseX, mouseY, x, guiTop + 13, 18, 18))
            blit(graphics, x, guiTop + 13, X_SIZE + 18, 0, 18, 18);

        for (int i = index; i < Math.min(index + ROWS, visibleTransporters.size()); i++) {
            TransporterInfo transporter = visibleTransporters.get(i);
            int y = guiTop + 69 + (i - index) * 26;
            boolean hovered = isInAABB(mouseX, mouseY, x, y, 18, 18);
            if (transporter.equals(linkToTransporter)) {
                blit(graphics, x, y, X_SIZE, 18, 18, 18);
                if (hovered) blit(graphics, x, y, X_SIZE + 18, 0, 18, 18);
            } else if (hovered) {
                blit(graphics, x, y, X_SIZE, 0, 18, 18);
            }
        }

        if (visibleTransporters.size() > ROWS) {
            int y =
                    guiTop
                            + 68
                            + Mth.floor(
                                    (float) index
                                            / (float) (visibleTransporters.size() - ROWS)
                                            * 114.0F);
            blit(graphics, guiLeft + 202, y, X_SIZE, 54, 14, 15);
        }
    }

    private void drawTransporter(
            GuiGraphicsExtractor graphics,
            TransporterInfo transporter,
            int iconX,
            int iconY,
            int nameY,
            int coordinatesY) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                transporter.planet(),
                iconX,
                iconY,
                0,
                0,
                16,
                16,
                16,
                16);

        String coordinates = "x: " + transporter.pos().getX() + ", z: " + transporter.pos().getZ();
        graphics.text(font, transporter.name(), guiLeft + 62, nameY, 0xFF00FF00, true);
        graphics.text(
                font,
                coordinates,
                guiLeft + 188 - font.width(coordinates),
                coordinatesY,
                0xFF00FF00,
                true);
    }

    private static void blit(
            GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, w, h, 256, 256);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        int mouseX = (int) event.x(), mouseY = (int) event.y();
        int x = guiLeft + 10;

        if (isInAABB(mouseX, mouseY, x, guiTop + 13, 18, 18) && selectTransporter(null)) {
            onClose();
            return true;
        }

        for (int i = index; i < Math.min(index + ROWS, visibleTransporters.size()); i++) {
            int y = guiTop + 69 + (i - index) * 26;
            if (isInAABB(mouseX, mouseY, x, y, 18, 18)
                    && selectTransporter(visibleTransporters.get(i))) {
                onClose();
                return true;
            }
        }
        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && index > 0) index--;
        if (scrollY < 0 && index < visibleTransporters.size() - ROWS) index++;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int mouseX = (int) event.x(), mouseY = (int) event.y();
        if (isInAABB(mouseX, mouseY, guiLeft + 198, guiTop + 64, 22, 137)) {
            index =
                    Mth.floor(
                            (mouseY - (guiTop + 68))
                                    / 114.0F
                                    * (float) (visibleTransporters.size() - ROWS));
            index = Mth.clamp(index, 0, visibleTransporters.size() - ROWS);
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    private boolean selectTransporter(@Nullable TransporterInfo transporter) {
        if (linkFromTransporter.equals(transporter)) return false;

        if (linkToTransporter != null && linkToTransporter.equals(transporter)) {
            linkTransporters(null);
        } else {
            linkTransporters(transporter);
        }

        return true;
    }

    private void linkTransporters(@Nullable TransporterInfo to) {
        if (to == null) {
            PacketWire.sendToServer(SpaceTransporterUnlink.encode(menu.containerId));
        } else {
            PacketWire.sendToServer(
                    SpaceTransporterLink.encode(
                            menu.containerId,
                            to.dimension().identifier(),
                            to.pos().getX(),
                            to.pos().getY(),
                            to.pos().getZ()));
        }
    }

    private void updateSearch() {
        List<TransporterInfo> visible = new ArrayList<>();
        index = 0;

        String subs = search.getValue().toLowerCase(Locale.ROOT);

        for (TransporterInfo transporter : transporters) {
            if (transporter.equals(linkFromTransporter)) continue;
            if (transporter.linkedTo() != null
                    && !transporter.linkedTo().equals(linkFromTransporter)) continue;
            if (transporter.name().toLowerCase(Locale.ROOT).contains(subs))
                visible.add(transporter);
        }
        visibleTransporters = visible;
    }

    private static boolean isInAABB(int mouseX, int mouseY, int x, int y, int width, int height) {
        return x <= mouseX && x + width > mouseX && y <= mouseY && y + height > mouseY;
    }
}
