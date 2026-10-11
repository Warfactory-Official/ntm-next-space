// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.gui;

import com.hbm.client.gui.ScreenInfoContainer;
import com.hbm.items.ISatChip;
import com.hbm.packet.PacketWire;
import com.hbm.packet.toserver.NbtControlPayload;
import com.hbm.saveddata.satellites.Satellite;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.BlockEntityMachineWarController;
import com.hbmspace.inventory.container.MenuMachineWarController;
import com.hbmspace.saveddata.satellites.ClientSatellites;
import com.hbmspace.saveddata.satellites.SatelliteWar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ScreenWarController extends ScreenInfoContainer<MenuMachineWarController> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/machine/gui_controlpanel.png");
    private static final int MAX_HISTORY = 6;
    private static final int GREEN = 0xFF00FF00;

    private List<Component> history = new ArrayList<>();
    private EditBox command;

    public ScreenWarController(
            MenuMachineWarController menu, Inventory inventory, Component title) {
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
        if (event.key() == GLFW.GLFW_KEY_ENTER) {
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

        BlockEntityMachineWarController controller = menu.blockEntity();
        int satId =
                ISatChip.getFreqS(controller.getItem(BlockEntityMachineWarController.SLOT_CHIP));
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
                if (controller.getItem(BlockEntityMachineWarController.SLOT_DRIVE).isEmpty()) {
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

                CompoundTag data = new CompoundTag();
                data.putInt("xcoord", x);
                data.putInt("zcoord", z);
                PacketWire.sendToServer(new NbtControlPayload(controller.getBlockPos(), data));
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
                if (controller.getItem(BlockEntityMachineWarController.SLOT_CHIP).isEmpty()) {
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
