/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2026 detree05. All rights reserved.
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
import com.hbm.inventory.container.HeldItemMenu;
import com.hbm.packet.PacketWire;
import com.hbm.saveddata.satellites.SatelliteType;
import com.hbm.sound.ModSounds;
import com.hbmspace.HbmSpace;
import com.hbmspace.client.sky.SkyPipelines;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.items.ModDataComponentsSpace;
import com.hbmspace.packet.layout.SpaceSatelliteSettings;
import com.hbmspace.saveddata.satellites.ClientSatellites;
import com.hbmspace.saveddata.satellites.SatelliteItems;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import com.hbmspace.saveddata.satellites.SatellitesSpace;
import com.hbmspace.sound.RocketrySoundsSpace;
import com.hbmspace.util.AstronomyUtil;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class SatelliteSettingsScreen extends MenuScreen<HeldItemMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    HbmSpace.MOD_ID, "textures/gui/satellites/gui_sat_settings.png");
    private static final int WIDTH = 134;
    private static final int HEIGHT = 221;
    private static final int SHEET_WIDTH = 152;
    private static final int PREVIEW_X = 9;
    private static final int PREVIEW_Y = 8;
    private static final int PREVIEW_SIZE = 116;
    private static final int GREEN = 0xFF00FF00;

    private final Player player;
    private final StarmapScene scene;
    private int guiLeft;
    private int guiTop;
    private int draggedSlider = -1;
    private boolean hasPendingChanges;
    private String editOwner = SatelliteOrbit.DEFAULT_OWNER;
    private float editAltitude;
    private float editInclination;
    private float editPhaseOffset;
    private boolean editBlinking;
    private float editBlinkPeriod;
    private int editColorR;
    private int editColorG;
    private int editColorB;
    private boolean showSatelliteDetails = true;
    private int detailsMode = 0;

    public SatelliteSettingsScreen(HeldItemMenu menu, Inventory inventory, Component title) {
        super(menu, title);
        this.player = inventory.player;
        this.scene = new StarmapScene(Minecraft.getInstance());
        loadEditableValues();
    }

    @Override
    public void added() {
        super.added();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BOBBLE.get(), 1.0F));
    }

    @Override
    protected void init() {
        guiLeft = (width - WIDTH) / 2;
        guiTop = (height - HEIGHT) / 2;
    }

    @Override
    public void tick() {
        if (heldSatellite() == null) onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        blit(graphics, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

        ItemStack held = heldSatellite();
        if (held == null) return;

        if (showSatelliteDetails) {
            drawBatterySlice(graphics);
            drawOrbitPreview(graphics, held, a);
            if (detailsMode == 0) {
                drawLeftAligned(
                        graphics,
                        10,
                        130,
                        140,
                        row(
                                "speed",
                                SatelliteItems.speed(editAltitude),
                                "gui.sat.settings.unit.km_per_second"),
                        GREEN);
                drawLeftAligned(
                        graphics,
                        10,
                        145,
                        155,
                        row(
                                "altitude",
                                SatelliteItems.format(editAltitude),
                                "gui.sat.settings.unit.km"),
                        GREEN);
                drawLeftAligned(
                        graphics,
                        10,
                        160,
                        170,
                        Component.translatable("item.hbmspace.sat.desc.inclination")
                                .append(": " + SatelliteItems.format(editInclination) + "\u00B0"),
                        GREEN);
            } else {
                drawLeftAligned(
                        graphics,
                        10,
                        130,
                        140,
                        Component.translatable("gui.sat.settings.label.orbital_phase")
                                .append(": " + SatelliteItems.phase(editPhaseOffset) + "\u00B0"),
                        GREEN);
                drawLeftAligned(
                        graphics,
                        10,
                        145,
                        155,
                        Component.translatable("item.hbmspace.sat.desc.owner")
                                .append(": " + editOwner),
                        GREEN);
            }
            graphics.fill(
                    guiLeft + 81,
                    guiTop + 177,
                    guiLeft + 110,
                    guiTop + 199,
                    0xFF000000 | editColorR << 16 | editColorG << 8 | editColorB);
            drawRightAligned(
                    graphics,
                    108,
                    204,
                    213,
                    Component.literal(SatelliteItems.format(editBlinkPeriod))
                            .append(Component.translatable("gui.sat.settings.unit.seconds")),
                    0xFFFFFFFF);
        }

        drawSlider(graphics, editColorR, 180, 17);
        drawSlider(graphics, editColorG, 187, 24);
        drawSlider(graphics, editColorB, 194, 31);
        if (showSatelliteDetails && editBlinking)
            blit(graphics, guiLeft + 114, guiTop + 202, 136, 3, 12, 12);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (heldSatellite() == null || !showSatelliteDetails) return;

        String tooltip = null;
        if (isOwnerButtonAt(mouseX, mouseY)) {
            tooltip = "gui.sat.settings.tooltip.owner_assign";
        } else if (isModeButtonAt(mouseX, mouseY)) {
            tooltip = "gui.sat.settings.tooltip.switch_settings_list";
        } else {
            switch (getScrollFieldAt(mouseX, mouseY)) {
                case 0 ->
                        tooltip =
                                detailsMode == 0
                                        ? "gui.sat.settings.tooltip.scroll_speed"
                                        : "gui.sat.settings.tooltip.scroll_phase";
                case 1 -> tooltip = "gui.sat.settings.tooltip.scroll_altitude";
                case 2 -> tooltip = "gui.sat.settings.tooltip.scroll_inclination";
                case 3 -> tooltip = "gui.sat.settings.tooltip.scroll_blink";
                default -> {}
            }
        }
        if (tooltip != null)
            graphics.setTooltipForNextFrame(font, Component.translatable(tooltip), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || heldSatellite() == null)
            return handled;
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (isBatteryToggleAt(mouseX, mouseY)) {
            play(
                    showSatelliteDetails
                            ? RocketrySoundsSpace.ITEM_UNPACK_REMOVE.get()
                            : RocketrySoundsSpace.ITEM_UNPACK_INSERT.get());
            showSatelliteDetails = !showSatelliteDetails;
            return true;
        }

        if (isBlinkButtonAt(mouseX, mouseY)) {
            play(SoundEvents.UI_BUTTON_CLICK.value());
            editBlinking = !editBlinking;
            hasPendingChanges = true;
            return true;
        }

        if (isOwnerButtonAt(mouseX, mouseY)) {
            play(SoundEvents.UI_BUTTON_CLICK.value());
            String owner = player.getName().getString();
            if (!owner.equals(editOwner)) {
                editOwner = owner;
                hasPendingChanges = true;
            }
            return true;
        }

        if (isModeButtonAt(mouseX, mouseY)) {
            play(SoundEvents.UI_BUTTON_CLICK.value());
            detailsMode = detailsMode == 0 ? 1 : 0;
            return true;
        }

        draggedSlider = getSliderAt(mouseX, mouseY);
        if (draggedSlider >= 0) {
            updateSlider(draggedSlider, mouseX);
            return true;
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && draggedSlider >= 0) {
            updateSlider(draggedSlider, (int) event.x());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) draggedSlider = -1;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) return false;
        int field = getScrollFieldAt((int) mouseX, (int) mouseY);
        if (field < 0) return false;
        adjustScrollField(field, scrollY > 0 ? 1 : -1);
        return true;
    }

    @Override
    public void onClose() {
        if (hasPendingChanges && heldSatellite() != null) {
            PacketWire.sendToServer(
                    SpaceSatelliteSettings.encode(
                            menu.containerId,
                            editOwner,
                            editAltitude,
                            editInclination,
                            editPhaseOffset,
                            editBlinking,
                            editBlinkPeriod,
                            editColorR,
                            editColorG,
                            editColorB));
        }
        super.onClose();
    }

    private void play(SoundEvent sound) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    private static void blit(
            GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, w, h, SHEET_WIDTH, HEIGHT);
    }

    private static Component row(String key, String value, String unit) {
        return Component.translatable("item.hbmspace.sat.desc." + key)
                .append(": " + value)
                .append(Component.translatable(unit));
    }

    private void drawLeftAligned(
            GuiGraphicsExtractor graphics, int x, int y1, int y2, Component text, int color) {
        drawScaled(graphics, text, guiLeft + x, centeredY(y1, y2, 2F / 3F), color, 2F / 3F);
    }

    private void drawRightAligned(
            GuiGraphicsExtractor graphics, int x, int y1, int y2, Component text, int color) {
        float scale = 2F / 3F;
        drawScaled(
                graphics,
                text,
                guiLeft + x - Math.round(font.width(text) * scale),
                centeredY(y1, y2, scale),
                color,
                scale);
    }

    private int centeredY(int y1, int y2, float scale) {
        return guiTop + y1 + ((y2 - y1 + 1) - Math.round(font.lineHeight * scale)) / 2;
    }

    private void drawScaled(
            GuiGraphicsExtractor graphics, Component text, int x, int y, int color, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, Math.round(x / scale), Math.round(y / scale), color, false);
        graphics.pose().popMatrix();
    }

    private void drawSlider(GuiGraphicsExtractor graphics, int value, int y, int v) {
        blit(graphics, guiLeft + 12 + Math.round(value * 62 / 255F), guiTop + y - 2, 136, v, 2, 6);
    }

    private void drawBatterySlice(GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(guiLeft + 15 + 29, guiTop + 200);
        graphics.pose().rotate(Mth.HALF_PI);
        blit(graphics, 0, 0, 136, 38, 13, 29);
        graphics.pose().popMatrix();
    }

    private int getSliderAt(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;

        if (x < 12 || x >= 12 + 64) return -1;
        if (y >= 180 - 2 && y < 180 - 2 + 6) return 0;
        if (y >= 187 - 2 && y < 187 - 2 + 6) return 1;
        if (y >= 194 - 2 && y < 194 - 2 + 6) return 2;
        return -1;
    }

    private void updateSlider(int slider, int mouseX) {
        int value = Math.round(Mth.clamp(mouseX - guiLeft - 12, 0, 62) * 255F / 62);
        switch (slider) {
            case 0 -> {
                if (editColorR == value) return;
                editColorR = value;
            }
            case 1 -> {
                if (editColorG == value) return;
                editColorG = value;
            }
            default -> {
                if (editColorB == value) return;
                editColorB = value;
            }
        }
        hasPendingChanges = true;
    }

    private void adjustScrollField(int field, int delta) {
        if (heldSatellite() == null) return;

        if (detailsMode != 0) {
            if (field == 0) adjustPhaseOffset(delta);
            return;
        }

        switch (field) {
            case 0 -> adjustAltitude(-delta);
            case 1 -> adjustAltitude(delta);
            case 2 -> adjustInclination(delta);
            case 3 -> adjustBlinkPeriod(delta);
            default -> {}
        }
    }

    private void adjustAltitude(int delta) {
        float step = minecraft.hasShiftDown() ? 5.0F : 1.0F;
        float value =
                Mth.clamp(
                        editAltitude + (delta > 0 ? step : -step),
                        SatelliteOrbit.MIN_ALTITUDE_KM,
                        SatelliteOrbit.MAX_ALTITUDE_KM);
        if (value == editAltitude) return;
        editAltitude = value;
        hasPendingChanges = true;
    }

    private void adjustInclination(int delta) {
        float step = minecraft.hasShiftDown() ? 15.0F : 1.0F;
        float value =
                Mth.clamp(
                        editInclination + (delta > 0 ? step : -step),
                        SatelliteOrbit.MIN_INCLINATION,
                        SatelliteOrbit.MAX_INCLINATION);
        if (value == editInclination) return;
        editInclination = value;
        hasPendingChanges = true;
    }

    private void adjustPhaseOffset(int delta) {
        float step = minecraft.hasShiftDown() ? 15.0F : 1.0F;
        float value =
                SatelliteOrbit.normalizePhaseOffset(editPhaseOffset + (delta > 0 ? step : -step));
        if (Math.abs(value - editPhaseOffset) < 0.0005F) return;
        editPhaseOffset = value;
        hasPendingChanges = true;
    }

    private void adjustBlinkPeriod(int delta) {
        float value =
                SatelliteOrbit.clampBlinkPeriod(
                        Math.round((editBlinkPeriod + (delta > 0 ? 0.1F : -0.1F)) * 10F) / 10F);
        if (value == editBlinkPeriod) return;
        editBlinkPeriod = value;
        hasPendingChanges = true;
    }

    private int getScrollFieldAt(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;

        if (x >= 9 && x < 111) {
            if (y >= 130 && y <= 140) return 0;
            if (detailsMode == 0) {
                if (y >= 145 && y <= 155) return 1;
                if (y >= 160 && y <= 170) return 2;
            }
        }
        if (x >= 81 && x < 111 && y >= 204 && y <= 213) return 3;
        return -1;
    }

    private boolean isBlinkButtonAt(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        return x >= 114 && x < 127 && y >= 202 && y < 215;
    }

    private boolean isBatteryToggleAt(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        return x >= 15 && x < 44 && y >= 200 && y < 213;
    }

    private boolean isOwnerButtonAt(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        return x >= 113 && x < 126 && y >= 128 && y < 141;
    }

    private boolean isModeButtonAt(int mouseX, int mouseY) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        return x >= 113 && x < 126 && y >= 143 && y < 156;
    }

    private @Nullable ItemStack heldSatellite() {
        ItemStack held = player.getItemInHand(menu.hand);
        SatelliteType type = SatelliteType.fromStack(held);
        return type != null && SatellitesSpace.isSpace(type) ? held : null;
    }

    private void loadEditableValues() {
        ItemStack held = heldSatellite();
        if (held == null) return;

        SatelliteOrbit orbit =
                held.getOrDefault(
                        ModDataComponentsSpace.SATELLITE_ORBIT.get(),
                        SatellitesSpace.defaults(SatelliteType.fromStack(held)));
        hasPendingChanges = false;
        editOwner = orbit.owner();
        editAltitude = orbit.altitude();
        editInclination = orbit.inclination();
        editPhaseOffset = orbit.phaseOffset();
        editBlinking = orbit.blinking();
        editBlinkPeriod = orbit.blinkPeriod();
        editColorR = SatelliteItems.channel(orbit.red());
        editColorG = SatelliteItems.channel(orbit.green());
        editColorB = SatelliteItems.channel(orbit.blue());
    }

    private void drawOrbitPreview(
            GuiGraphicsExtractor graphics, ItemStack held, float partialTicks) {
        CelestialBody body = previewBody(held);
        float bodySizeAt1x = StarmapScene.bodySizePxAt1x(body);
        float baseOrbitRadiusMapPx = bodySizeAt1x * 1.5F;
        Map<Integer, ClientSatellites.Entry> satellites =
                body.dimension == null ? Map.of() : ClientSatellites.of(body.dimension);
        float maxAltitude = editAltitude;
        for (ClientSatellites.Entry satellite : satellites.values()) {
            if (editOwner.equals(satellite.orbit().owner())) {
                maxAltitude = Math.max(maxAltitude, satellite.orbit().altitude());
            }
        }

        float renderZoom = previewZoom(bodySizeAt1x, baseOrbitRadiusMapPx, maxAltitude) * 2.0F;
        float centerX = guiLeft + PREVIEW_X + PREVIEW_SIZE * 0.5F;
        float centerY = guiTop + PREVIEW_Y + PREVIEW_SIZE * 0.5F;
        float bodySize = Mth.clamp(bodySizeAt1x * renderZoom, 8F, 96F);
        float iconSize = Mth.clamp(bodySize * 0.75F * 0.25F, 0.4F, 9.0F);
        double ticks = minecraft.level.getGameTime() + (double) partialTicks;
        double angle = -ticks / SatelliteOrbit.REVOLUTION_TICKS * 2D * Math.PI;

        float heldR = editColorR / 255F;
        float heldG = editColorG / 255F;
        float heldB = editColorB / 255F;
        float heldBlinkAlpha = SatelliteOrbit.blinkAlpha(editBlinking, editBlinkPeriod, ticks);

        scene.begin(guiLeft + PREVIEW_X, guiTop + PREVIEW_Y);
        drawStarmapBackground();

        drawOwnedSatellites(
                satellites,
                centerX,
                centerY,
                baseOrbitRadiusMapPx,
                renderZoom,
                angle,
                iconSize,
                ticks,
                false);
        drawSatelliteOrbitHalf(
                centerX,
                centerY,
                baseOrbitRadiusMapPx,
                renderZoom,
                editAltitude,
                editInclination,
                heldR,
                heldG,
                heldB,
                false,
                0.45F * heldBlinkAlpha);
        drawSatelliteIcon(
                centerX,
                centerY,
                baseOrbitRadiusMapPx,
                renderZoom,
                editPhaseOffset,
                editAltitude,
                editInclination,
                angle,
                false,
                iconSize * 1.2F);
        drawBodyPreview(body, centerX, centerY, bodySize, ticks);

        drawOwnedSatellites(
                satellites,
                centerX,
                centerY,
                baseOrbitRadiusMapPx,
                renderZoom,
                angle,
                iconSize,
                ticks,
                true);
        drawSatelliteOrbitHalf(
                centerX,
                centerY,
                baseOrbitRadiusMapPx,
                renderZoom,
                editAltitude,
                editInclination,
                heldR,
                heldG,
                heldB,
                true,
                0.45F * heldBlinkAlpha);
        drawSatelliteIcon(
                centerX,
                centerY,
                baseOrbitRadiusMapPx,
                renderZoom,
                editPhaseOffset,
                editAltitude,
                editInclination,
                angle,
                true,
                iconSize * 1.2F);

        graphics.guiRenderState.addPicturesInPictureState(
                new StardarMapRenderState(
                        scene.draws(),
                        guiLeft + PREVIEW_X,
                        guiTop + PREVIEW_Y,
                        guiLeft + PREVIEW_X + PREVIEW_SIZE,
                        guiTop + PREVIEW_Y + PREVIEW_SIZE));
    }

    private void drawStarmapBackground() {
        float bgU = Mth.clamp(1024F * 0.5F - PREVIEW_SIZE * 0.5F, 0F, 1024F - PREVIEW_SIZE);
        float bgV = Mth.clamp(1024F * 0.5F - PREVIEW_SIZE * 0.5F, 0F, 1024F - PREVIEW_SIZE);
        scene.partialTex(
                SkyPipelines.TEXTURED_TRANSLUCENT,
                StarmapScene.STARMAP,
                1F,
                1F,
                1F,
                1F,
                guiLeft + PREVIEW_X,
                guiTop + PREVIEW_Y,
                PREVIEW_SIZE,
                PREVIEW_SIZE,
                bgU / 1024F,
                bgV / 1024F,
                (bgU + PREVIEW_SIZE) / 1024F,
                (bgV + PREVIEW_SIZE) / 1024F);
    }

    private void drawOwnedSatellites(
            Map<Integer, ClientSatellites.Entry> satellites,
            float centerX,
            float centerY,
            float baseOrbitRadiusMapPx,
            float zoom,
            double angle,
            float iconSize,
            double ticks,
            boolean frontHalf) {
        for (ClientSatellites.Entry satellite : satellites.values()) {
            SatelliteOrbit orbit = satellite.orbit();
            if (!editOwner.equals(orbit.owner())) continue;
            drawSatelliteOrbitHalf(
                    centerX,
                    centerY,
                    baseOrbitRadiusMapPx,
                    zoom,
                    orbit.altitude(),
                    orbit.inclination(),
                    orbit.red(),
                    orbit.green(),
                    orbit.blue(),
                    frontHalf,
                    0.25F * orbit.blinkAlpha(ticks));
        }

        for (ClientSatellites.Entry satellite : satellites.values()) {
            SatelliteOrbit orbit = satellite.orbit();
            if (!editOwner.equals(orbit.owner())) continue;
            drawSatelliteIcon(
                    centerX,
                    centerY,
                    baseOrbitRadiusMapPx,
                    zoom,
                    orbit.phaseOffset(),
                    orbit.altitude(),
                    orbit.inclination(),
                    angle,
                    frontHalf,
                    iconSize);
        }
    }

    private void drawSatelliteOrbitHalf(
            float centerX,
            float centerY,
            float baseRadiusMapPx,
            float zoom,
            float altitude,
            float inclination,
            float r,
            float g,
            float b,
            boolean frontHalf,
            float alpha) {
        float lineR = Mth.clamp(r, 0F, 1F);
        float lineG = Mth.clamp(g, 0F, 1F);
        float lineB = Mth.clamp(b, 0F, 1F);

        boolean hasPrev = false;
        float prevX = 0F;
        float prevY = 0F;
        float prevDepth = 0F;
        boolean prevInFrontHalf = false;

        for (int i = 0; i <= 64; i++) {
            float[] point =
                    orbitPoint(
                            altitude,
                            inclination,
                            (float) (2D * Math.PI * ((double) i / 64D)),
                            baseRadiusMapPx);
            float currX = centerX + point[0] * zoom;
            float currY = centerY + point[1] * zoom;
            float currDepth = point[2];
            boolean currInFrontHalf = currDepth >= 0F;

            if (hasPrev) {
                boolean prevSelected = prevInFrontHalf == frontHalf;
                boolean currSelected = currInFrontHalf == frontHalf;

                if (prevSelected && currSelected) {
                    scene.line(prevX, prevY, currX, currY, 1F, lineR, lineG, lineB, alpha);
                } else if (prevSelected != currSelected) {
                    float depthDelta = currDepth - prevDepth;
                    float t = Mth.clamp(depthDelta == 0F ? 0.5F : -prevDepth / depthDelta, 0F, 1F);
                    float crossX = prevX + (currX - prevX) * t;
                    float crossY = prevY + (currY - prevY) * t;
                    if (prevSelected) {
                        scene.line(prevX, prevY, crossX, crossY, 1F, lineR, lineG, lineB, alpha);
                    } else {
                        scene.line(crossX, crossY, currX, currY, 1F, lineR, lineG, lineB, alpha);
                    }
                }
            }

            prevX = currX;
            prevY = currY;
            prevDepth = currDepth;
            prevInFrontHalf = currInFrontHalf;
            hasPrev = true;
        }
    }

    private void drawSatelliteIcon(
            float centerX,
            float centerY,
            float baseRadiusMapPx,
            float zoom,
            float phaseOffset,
            float altitude,
            float inclination,
            double angle,
            boolean frontHalf,
            float size) {
        float satelliteAngle =
                SatelliteOrbit.applyPhaseOffsetToOrbitAngle(
                        phaseOffset, altitude, angle, Mth.TWO_PI);
        float[] point = orbitPoint(altitude, inclination, satelliteAngle, baseRadiusMapPx);
        float screenX = centerX + point[0] * zoom;
        float screenY = centerY + point[1] * zoom;
        if ((point[2] >= 0F) != frontHalf) return;

        float half = size * 0.5F;
        float minX = guiLeft + PREVIEW_X;
        float minY = guiTop + PREVIEW_Y;
        if (screenX + half < minX
                || screenX - half > minX + PREVIEW_SIZE
                || screenY + half < minY
                || screenY - half > minY + PREVIEW_SIZE) {
            return;
        }

        scene.partialTex(
                SkyPipelines.TEXTURED_TRANSLUCENT,
                StarmapScene.SATELLITE,
                1F,
                1F,
                1F,
                1F,
                screenX - half,
                screenY - half,
                size,
                size,
                0F,
                0F,
                1F,
                1F);
    }

    private void drawBodyPreview(
            CelestialBody body, float centerX, float centerY, float size, double ticks) {
        float half = size * 0.5F;
        float minX = centerX - half;
        float maxX = centerX + half;
        float minY = centerY - half;
        float maxY = centerY + half;

        if (body.hasRings) {
            float ringHalfWidth = StarmapScene.ringHalfWidth(body, size);
            float ringHalfHeight = StarmapScene.ringHalfHeight(body, ringHalfWidth);
            minX = Math.min(minX, centerX - ringHalfWidth);
            maxX = Math.max(maxX, centerX + ringHalfWidth);
            minY = Math.min(minY, centerY - ringHalfHeight);
            maxY = Math.max(maxY, centerY + ringHalfHeight);
        }

        if (maxX < guiLeft + PREVIEW_X
                || minX > guiLeft + PREVIEW_X + PREVIEW_SIZE
                || maxY < guiTop + PREVIEW_Y
                || minY > guiTop + PREVIEW_Y + PREVIEW_SIZE) {
            return;
        }

        int untextured = 0xFF666666;
        if (body.color != null && body.color.length >= 3) {
            untextured =
                    0xFF000000
                            | SatelliteItems.channel(body.color[0]) << 16
                            | SatelliteItems.channel(body.color[1]) << 8
                            | SatelliteItems.channel(body.color[2]);
        }
        scene.body(
                body,
                centerX,
                centerY,
                size,
                ticks,
                crescentPhase(body, ticks),
                minecraft.level.getGameTime(),
                untextured);
    }

    private static float crescentPhase(CelestialBody body, double ticks) {
        if (body.parent == null) return 0F;

        double orbitalPeriodTicks = body.getOrbitalPeriod() * (double) AstronomyUtil.TICKS_IN_DAY;
        if (orbitalPeriodTicks <= 0D) return 0F;

        double worldTicks = ticks * AstronomyUtil.TIME_MULTIPLIER;
        float orbitalAngle = (float) (2D * Math.PI * (worldTicks / orbitalPeriodTicks));
        float dx = Mth.cos(orbitalAngle);
        float dy = Mth.sin(orbitalAngle);

        float phaseMagnitude = Mth.clamp((dy + 1F) * 0.5F, 0F, 1F);
        float phaseSign = dx <= 0F ? 1F : -1F;
        return Mth.clamp(phaseMagnitude * phaseSign, -1F, 1F);
    }

    private static float previewZoom(
            float bodySizeAt1x, float baseOrbitRadiusMapPx, float maxAltitude) {
        float altitude = Math.max(AstronomyUtil.DEFAULT_ALTITUDE_KM, maxAltitude);
        float altitudeFactor = altitude / AstronomyUtil.DEFAULT_ALTITUDE_KM;
        float maxOrbitRadius = PREVIEW_SIZE * 0.46F;
        float zoomForOrbit =
                maxOrbitRadius / Math.max(0.0001F, baseOrbitRadiusMapPx * altitudeFactor);
        float zoomForBody = 16F / Math.max(0.0001F, bodySizeAt1x);
        return Mth.clamp(Math.min(zoomForOrbit, zoomForBody), 16F, 240F);
    }

    private static float[] orbitPoint(
            float altitude, float inclination, float angle, float baseRadiusMapPx) {
        double satInclination = Math.toRadians(inclination);
        double radiusMapPx = baseRadiusMapPx * (altitude / AstronomyUtil.DEFAULT_ALTITUDE_KM);

        double x = radiusMapPx * Mth.cos(angle);
        double orbitY = radiusMapPx * Mth.sin(angle);
        double y = Math.sin(satInclination) * orbitY;
        double z = Math.cos(satInclination) * orbitY;

        return new float[] {(float) x, (float) y, (float) z};
    }

    private CelestialBody previewBody(ItemStack held) {
        SolarSystem.Body home = SatellitesSpace.home(SatelliteType.fromStack(held));
        CelestialBody body = home != null ? home.getBody() : null;
        if (body != null) return body;
        return CelestialBody.getTarget(player.level(), player.getBlockX(), player.getBlockZ())
                .body();
    }
}
