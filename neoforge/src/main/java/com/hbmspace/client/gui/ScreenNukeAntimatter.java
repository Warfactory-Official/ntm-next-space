// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter;
import com.hbmspace.inventory.container.MenuNukeAntimatter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenNukeAntimatter extends ScreenInfoContainer<MenuNukeAntimatter> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/weapon/blombschematic.png");

    private static final int FIELD_X = 80, FIELD_Y = 108, FIELD_W = 29, FIELD_H = 12;
    private static final int START_X = 80, START_Y = 85, START_SIZE = 18;
    private static final float CLOCK_SCALE = 0.6F;
    private static final int CLOCK_CENTRE = 95, CLOCK_Y = 66;

    public ScreenNukeAntimatter(MenuNukeAntimatter menu, Inventory inventory, Component title) {
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
                        BlockEntityNukeAntimatter.MIN_SECONDS,
                        BlockEntityNukeAntimatter.MAX_SECONDS);
        CompoundTag data = new CompoundTag();
        data.putInt("timer", seconds);
        PacketWire.sendToServer(new NbtControlPayload(menu.blockEntity().getBlockPos(), data));
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
            CompoundTag data = new CompoundTag();
            data.putBoolean("start", true);
            PacketWire.sendToServer(new NbtControlPayload(menu.blockEntity().getBlockPos(), data));
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
        BlockEntityNukeAntimatter bomb = menu.blockEntity();

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
