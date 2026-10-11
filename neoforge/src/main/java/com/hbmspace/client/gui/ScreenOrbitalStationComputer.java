// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.dim.orbit.OrbitalStation;
import com.hbmspace.inventory.container.MenuOrbitalStationComputer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenOrbitalStationComputer extends ScreenInfoContainer<MenuOrbitalStationComputer> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_ship_computer.png");

    private static final int GRAVITY_X = 36, GRAVITY_Y = 84, GRAVITY_W = 29, GRAVITY_H = 5;

    private EditBox stationName;

    public ScreenOrbitalStationComputer(
            MenuOrbitalStationComputer menu, Inventory inventory, Component title) {
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
        CompoundTag data = new CompoundTag();
        data.putString("name", name);
        PacketWire.sendToServer(new NbtControlPayload(menu.blockEntity().getBlockPos(), data));
    }

    private void setGravity(boolean enabled) {
        CompoundTag data = new CompoundTag();
        data.putBoolean("gravity", enabled);
        PacketWire.sendToServer(new NbtControlPayload(menu.blockEntity().getBlockPos(), data));
    }
}
