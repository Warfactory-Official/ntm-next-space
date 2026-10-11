// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineDriveProcessor;
import com.hbmspace.inventory.container.MenuMachineDriveProcessor;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenMachineDriveProcessor extends ScreenInfoContainer<MenuMachineDriveProcessor> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/processing/gui_drive_processor.png");
    private static final int WIDTH = 176;

    public ScreenMachineDriveProcessor(
            MenuMachineDriveProcessor menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 207);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        BlockEntityMachineDriveProcessor machine = menu.blockEntity();

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

        if (machine.isProcessing) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 38, 61, 192, 28, 18, 18, 256, 256);
        }

        int p = (int) (machine.power * 52 / BlockEntityMachineDriveProcessor.MAX_POWER);
        if (p > 0)
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    134,
                    18 + 52 - p,
                    WIDTH,
                    52 - p,
                    16,
                    p,
                    256,
                    256);

        if (machine.isProcessing) {
            p = machine.progress * 44 / BlockEntityMachineDriveProcessor.MAX_PROGRESS;
            if (p > 0)
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED, TEXTURE, 59, 63, 192, 14, p, 14, 256, 256);
        }

        graphics.text(font, machine.status.text(), 41, 86, 0xFFFFFFFF, false);

        drawElectricityInfo(
                graphics,
                mouseX,
                mouseY,
                134,
                18,
                16,
                52,
                machine.power,
                BlockEntityMachineDriveProcessor.MAX_POWER);
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                49,
                17,
                18,
                18,
                List.of(Component.translatable("desc.gui.driveProcessor.clone")));
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                38,
                61,
                18,
                18,
                List.of(Component.translatable("desc.gui.driveProcessor.process")));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);

        BlockEntityMachineDriveProcessor machine = menu.blockEntity();
        int x = (int) event.x();
        int y = (int) event.y();
        if (checkClick(x, y, 38, 61, 18, 18)) {
            playClick();
            CompoundTag data = new CompoundTag();
            data.putBoolean("process", !machine.isProcessing);
            PacketWire.sendToServer(new NbtControlPayload(machine.getBlockPos(), data));
        }
        if (checkClick(x, y, 49, 17, 18, 18)) {
            playClick();
            CompoundTag data = new CompoundTag();
            data.putBoolean("clone", true);
            PacketWire.sendToServer(new NbtControlPayload(machine.getBlockPos(), data));
        }

        return handled;
    }
}
