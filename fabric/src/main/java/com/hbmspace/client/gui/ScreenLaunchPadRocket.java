// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.bomb.BlockEntityLaunchPadRocket;
import com.hbmspace.inventory.container.MenuLaunchPadRocket;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenLaunchPadRocket extends ScreenInfoContainer<MenuLaunchPadRocket> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_launchpad_rocket.png");
    private static final int WIDTH = 188;

    private final RocketPreview preview = new RocketPreview(97, 18, 50, 106, 122, 119);

    public ScreenLaunchPadRocket(MenuLaunchPadRocket menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 236);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        BlockEntityLaunchPadRocket pad = menu.blockEntity();

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

        int p = (int) (pad.power * 52 / BlockEntityLaunchPadRocket.MAX_POWER);
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
            int ox = pad.power > BlockEntityLaunchPadRocket.MAX_POWER * 0.75 ? 0 : 6;
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
                BlockEntityLaunchPadRocket.MAX_POWER);
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

        BlockEntityLaunchPadRocket pad = menu.blockEntity();
        if (pad.rocket != null
                && pad.rocket.validate()
                && checkClick((int) event.x(), (int) event.y(), 56, 20, 18, 17)) {
            playClick();
            CompoundTag data = new CompoundTag();
            data.putBoolean("launch", true);
            PacketWire.sendToServer(new NbtControlPayload(pad.getBlockPos(), data));
        }

        return handled;
    }
}
