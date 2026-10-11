// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbm.util.GameTime;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.dim.Target;
import com.hbmspace.handler.RocketStruct;
import com.hbmspace.inventory.container.MenuOrbitalStationLauncher;
import com.hbmspace.items.ItemVOTVdrive;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenOrbitalStationLauncher extends ScreenInfoContainer<MenuOrbitalStationLauncher> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_orbital_launcher.png");
    private static final int WIDTH = 192;

    private final RocketPreview preview = new RocketPreview(65, 5, 90, 106, 116, 103);
    private double currentScale = 1;
    private double currentOffset;
    private long lastTime = GameTime.now();

    public ScreenOrbitalStationLauncher(
            MenuOrbitalStationLauncher menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 224);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        BlockEntityOrbitalStationLauncher launcher = menu.blockEntity();
        RocketStruct rocket = launcher.rocket;
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

        if (launcher.hasDocked) {
            graphics.text(
                    font,
                    Component.translatable("desc.gui.orbitalStationLauncher.ready"),
                    74,
                    11,
                    0xFF00FF00,
                    false);
        } else {
            currentScale =
                    currentScale
                            + (86 / Math.max(rocket.getHeight(stage), 6) - currentScale) * dt * 4;
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
                    ItemVOTVdrive.getTarget(
                            launcher.getItem(BlockEntityOrbitalStationLauncher.programFrom(layer)),
                            launcher.getLevel());
            Target to =
                    ItemVOTVdrive.getTarget(
                            launcher.getItem(BlockEntityOrbitalStationLauncher.programTo(layer)),
                            launcher.getLevel());

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

            List<Component> launchIssues = launcher.findIssues();
            for (int i = 0; i < launchIssues.size(); i++) {
                graphics.text(
                        font,
                        launchIssues.get(i),
                        65 * 2,
                        111 * 2 + i * 8 - launchIssues.size() * 8,
                        0xFFFFFFFF,
                        false);
            }
            graphics.pose().popMatrix();
        }

        if (checkClick(mouseX, mouseY, 17, 34, 18, 8)) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, TEXTURE, 17, 34, WIDTH, 36, 18, 8, 256, 256);
        }
        if (checkClick(mouseX, mouseY, 17, 98, 18, 8)) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, TEXTURE, 17, 98, WIDTH, 44, 18, 8, 256, 256);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = (int) event.x(), y = (int) event.y();
        RocketStruct rocket = menu.blockEntity().rocket;

        if (checkClick(x, y, 17, 34, 18, 8)) {
            playClick();
            if (menu.layer() > 0) turnTo(menu.layer() - 1);
        }

        if (checkClick(x, y, 17, 98, 18, 8)) {
            playClick();
            if (menu.layer() < Math.min(rocket.stages.size(), RocketStruct.MAX_STAGES - 1))
                turnTo(menu.layer() + 1);
        }

        if (rocket.validate() && checkClick(x, y, 41, 12, 18, 17)) {
            playClick();
            CompoundTag data = new CompoundTag();
            data.putBoolean("launch", true);
            PacketWire.sendToServer(new NbtControlPayload(menu.blockEntity().getBlockPos(), data));
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void turnTo(int layer) {
        menu.setLayer(layer);
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, layer);
    }
}
