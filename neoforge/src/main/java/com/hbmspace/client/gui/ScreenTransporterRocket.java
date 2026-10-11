// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityTransporterRocket;
import com.hbmspace.inventory.container.MenuTransporterRocket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class ScreenTransporterRocket extends ScreenInfoContainer<MenuTransporterRocket> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_transporter.png");

    private EditBox transporterName;

    public ScreenTransporterRocket(
            MenuTransporterRocket menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 230, 236);
    }

    private BlockEntityTransporterRocket pad() {
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

        BlockEntityTransporterRocket pad = pad();
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
        BlockEntityTransporterRocket pad = pad();
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
            CompoundTag data = new CompoundTag();
            data.putInt("threshold", slidPos);
            send(data);
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    private void setName(String name) {
        CompoundTag data = new CompoundTag();
        data.putString("name", name);
        send(data);
    }

    private void send(CompoundTag data) {
        PacketWire.sendToServer(new NbtControlPayload(pad().getBlockPos(), data));
    }
}
