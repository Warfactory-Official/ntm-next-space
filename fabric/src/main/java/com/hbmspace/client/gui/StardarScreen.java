/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024-2026 George Paton
 * SPDX-FileCopyrightText: 2024 JamesH_2
 * SPDX-FileCopyrightText: 2026 detree05
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024-2026 George Paton. All rights reserved.
 * Copyright (C) 2024 JamesH_2. All rights reserved.
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

import com.hbm.client.gui.InfoContainerScreen;
import com.hbm.packet.PacketWire;
import com.hbmspace.HbmSpace;
import com.hbmspace.blockentity.machine.StardarBlockEntity;
import com.hbmspace.client.sky.SkyPipelines;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.inventory.container.StardarMenu;
import com.hbmspace.items.VOTVdriveItem;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.hbmspace.packet.layout.SpaceStardarLanding;
import com.hbmspace.packet.layout.SpaceStardarTarget;
import com.hbmspace.saveddata.satellites.ClientSatellites;
import com.hbmspace.saveddata.satellites.SatelliteItems;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import com.hbmspace.util.AstronomyUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class StardarScreen extends InfoContainerScreen<StardarMenu> {

    private static final Identifier TEXTURE = space("textures/gui/machine/gui_stardar.png");
    private static final Identifier GROUND = space("stardar_ground_map");

    private static final int WIDTH = 176;
    private static final int MAP_X = 9;
    private static final int MAP_Y = 9;
    private static final int MAP_W = 158;
    private static final int MAP_H = 108;

    private static final float MAP_TEX_W = 1024F;
    private static final float MAP_TEX_H = 1024F;
    private static final float STARMAP_BG_TEX_W = 1024F;
    private static final float STARMAP_BG_TEX_H = 1024F;
    private static final float SYSTEM_LAYOUT_REF_TEX = 256F;

    private static final float MIN_ZOOM = 0.5F;
    private static final float MAX_ZOOM = 180.0F;
    private static final float ZOOM_STEP = 1.1F;

    private static final float ISO_X = 0.70F;
    private static final float ISO_Y = 0.35F;
    private static final float STARDAR_SYSTEM_RADIUS_FRACTION = 0.50F;
    private static final float SATELLITE_ICON_BODY_SCALE = 0.75F;
    private static final float SATELLITE_ORBIT_RADIUS_SCALE = 1.5F;

    private final StardarBlockEntity star;
    private final DynamicTexture groundTexture;
    private final StarmapScene scene = new StarmapScene(Minecraft.getInstance());

    private final List<BodyRenderInfo> renderedBodies = new ArrayList<>();
    private final List<BodyLabelRenderInfo> renderedBodyLabels = new ArrayList<>();
    private final List<SatelliteRenderInfo> renderedSatellites = new ArrayList<>();
    private final BodyPosition trackedBodyPosition = new BodyPosition();
    private final BodyPosition trackedSatellitePosition = new BodyPosition();

    private final @Nullable CelestialBody currentBody;
    private @Nullable CelestialBody focusedBody;
    private @Nullable CelestialBody focusAnimationBody;
    private @Nullable BodyRenderInfo hoveredBody;
    private @Nullable SatelliteRenderInfo hoveredSatellite;
    private @Nullable SatelliteRenderInfo focusedSatellite;
    private @Nullable Integer focusedSatelliteFrequency;
    private @Nullable CelestialBody landingBody;

    private boolean landingMode;
    private boolean hadFullDrive;
    private SolarSystem.@Nullable Body lastFullDriveBody;

    private boolean draggingMap;
    private int dragLastX;
    private int dragLastY;

    private int mX;
    private int mY;
    private int sX;
    private int sY;
    private boolean draggingSurface;

    private float surfaceX;
    private float surfaceY;

    private float mapU = 0F;
    private float mapV = 0F;
    private float mapZoom = 1.0F;

    private boolean focusAnimationActive = false;
    private boolean focusAnimationToBody = false;
    private float focusAnimStartCenterU = 0F;
    private float focusAnimStartCenterV = 0F;
    private float focusAnimStartZoom = 1.0F;
    private float focusAnimTargetCenterU = 0F;
    private float focusAnimTargetCenterV = 0F;
    private float focusAnimTargetZoom = 1.0F;
    private boolean focusAnimTrackingInitialized = false;
    private float focusAnimTrackingOffsetU = 0F;
    private float focusAnimTrackingOffsetV = 0F;
    private double focusAnimStartTimeMs = 0D;
    private int focusAnimDurationMs = 500;

    private int lastMouseX;
    private int lastMouseY;
    private float partialTicks;

    public StardarScreen(StardarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, 256);
        this.star = menu.blockEntity();
        this.currentBody = CelestialBody.getBody(star.getLevel());

        groundTexture =
                new DynamicTexture(
                        () -> "Stardar ground map",
                        StardarBlockEntity.MAP_SIZE,
                        StardarBlockEntity.MAP_SIZE,
                        true);
        star.updateHeightmap = true;

        setViewFromCenter(MAP_TEX_W * 0.5F, MAP_TEX_H * 0.5F, mapZoom);
    }

    private static Identifier space(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    @Override
    protected void init() {
        super.init();
        minecraft.getTextureManager().register(GROUND, groundTexture);
    }

    @Override
    public void removed() {
        super.removed();
        minecraft.getTextureManager().release(GROUND);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateLandingModeFromDrive();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        partialTicks = a;
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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

        scene.begin(leftPos + MAP_X, topPos + MAP_Y);
        renderedBodyLabels.clear();
        if (landingMode) {
            drawSurfaceMap();
        } else {
            drawSystemMap(partialTicks);
        }
        graphics.guiRenderState.addPicturesInPictureState(
                new StardarMapRenderState(
                        scene.draws(),
                        leftPos + MAP_X,
                        topPos + MAP_Y,
                        leftPos + MAP_X + MAP_W,
                        topPos + MAP_Y + MAP_H));

        graphics.enableScissor(MAP_X, MAP_Y, MAP_X + MAP_W, MAP_Y + MAP_H);
        drawQueuedBodyLabels(graphics);
        graphics.disableScissor();

        if (landingMode) {
            drawLandingForeground(graphics);
        } else {
            drawMapForeground(graphics);
        }

        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                129,
                124,
                18,
                18,
                List.of(Component.translatable("gui.stardar.tooltip.focus_current_body")));
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                129,
                143,
                18,
                18,
                List.of(Component.translatable("gui.stardar.tooltip.program_new_station")));
        drawCustomInfoStat(
                graphics,
                mouseX,
                mouseY,
                149,
                143,
                18,
                18,
                List.of(Component.translatable("gui.stardar.tooltip.program_current_body")));
        drawSatelliteTooltip(graphics, mouseX, mouseY);
    }

    private void label(GuiGraphicsExtractor graphics, Component text, int y, int color) {
        graphics.text(font, text, 10, y, 0xFF000000 | color, false);
    }

    private void drawMapForeground(GuiGraphicsExtractor graphics) {
        if (focusedSatelliteFrequency != null) {
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.focus_satellite"),
                    128,
                    0x00FF00);
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.unfocus_hint"),
                    148,
                    0x00FF00);
            return;
        }

        CelestialBody activeFocus = getRenderFocusBody();
        CelestialBody displayBody = hoveredBody != null ? hoveredBody.body : activeFocus;

        if (displayBody == null) {
            label(graphics, Component.translatable("gui.stardar.label.drag_map"), 128, 0x00FF00);
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.click_body_focus"),
                    148,
                    0x00FF00);
            return;
        }

        label(
                graphics,
                Component.translatable(
                        "gui.stardar.label.focus_body", getBodyDisplayName(displayBody)),
                128,
                0x00FF00);
        if (activeFocus != null && displayBody == activeFocus) {
            if (!displayBody.canLand) {
                label(
                        graphics,
                        Component.translatable("gui.stardar.label.cant_land"),
                        148,
                        0xFF4040);
            } else if (menu.getSlot(0).getItem().is(MissilePartsSpace.HARD_DRIVE.get())) {
                label(
                        graphics,
                        Component.translatable("gui.stardar.label.click_again_land"),
                        148,
                        0x00FF00);
            } else {
                label(
                        graphics,
                        Component.translatable("gui.stardar.label.insert_drive"),
                        148,
                        0x00FF00);
            }
        } else {
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.click_body_focus"),
                    148,
                    0x00FF00);
        }
    }

    private void drawLandingForeground(GuiGraphicsExtractor graphics) {
        int[] heightmap = star.heightmap;
        if (heightmap == null) {
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.loading_surface_map"),
                    148,
                    0x00FF00);
            return;
        }

        int sx = lastMouseX - ((lastMouseX + (int) surfaceX) % 2);
        int sy = lastMouseY - ((lastMouseY + (int) surfaceY) % 2);

        if (!isMouseInsideMap(lastMouseX, lastMouseY)) {
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.select_landing_zone"),
                    128,
                    0x00FF00);
            return;
        }

        int hx = (lastMouseX - (int) surfaceX - leftPos - MAP_X + 256) / 2;
        int hz = (lastMouseY - (int) surfaceY - topPos - MAP_Y + 256) / 2;
        String info = StardarBlockEntity.landingIssue(heightmap, hx, hz);
        int altitude = altitude(heightmap, hx, hz);
        boolean canLand = info == null;

        graphics.enableScissor(MAP_X, MAP_Y, MAP_X + MAP_W, MAP_Y + MAP_H);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                sx - leftPos - 6,
                sy - topPos - 6,
                WIDTH + (canLand ? 14 : 0),
                28,
                14,
                14,
                256,
                256);
        graphics.disableScissor();

        label(
                graphics,
                Component.translatable(canLand ? "gui.stardar.label.valid_location" : info),
                128,
                canLand ? 0x00FF00 : 0xFF0000);
        if (altitude > 0) {
            label(
                    graphics,
                    Component.translatable("gui.stardar.label.target_altitude", altitude),
                    148,
                    0x00FF00);
        }
    }

    private void drawSystemMap(float partialTicks) {
        float bgSrcW = MAP_W;
        float bgSrcH = MAP_H;
        float bgU =
                Mth.clamp(mapU + (MAP_W / mapZoom) * 0.5F - bgSrcW * 0.5F, 0F, MAP_TEX_W - bgSrcW);
        float bgV =
                Mth.clamp(mapV + (MAP_H / mapZoom) * 0.5F - bgSrcH * 0.5F, 0F, MAP_TEX_H - bgSrcH);
        float bgScaleU = STARMAP_BG_TEX_W / MAP_TEX_W;
        float bgScaleV = STARMAP_BG_TEX_H / MAP_TEX_H;
        float bgUTex = bgU * bgScaleU;
        float bgVTex = bgV * bgScaleV;
        float bgSrcWTex = bgSrcW * bgScaleU;
        float bgSrcHTex = bgSrcH * bgScaleV;

        scene.partialTex(
                SkyPipelines.TEXTURED_TRANSLUCENT,
                StarmapScene.STARMAP,
                1F,
                1F,
                1F,
                1F,
                leftPos + MAP_X,
                topPos + MAP_Y,
                MAP_W,
                MAP_H,
                bgUTex / STARMAP_BG_TEX_W,
                bgVTex / STARMAP_BG_TEX_H,
                (bgUTex + bgSrcWTex) / STARMAP_BG_TEX_W,
                (bgVTex + bgSrcHTex) / STARMAP_BG_TEX_H);

        renderedBodies.clear();
        renderedSatellites.clear();
        hoveredSatellite = null;
        focusedSatellite = null;

        CelestialBody root = SolarSystem.kerbol;

        double dayTicks = (double) star.getLevel().getGameTime() + (double) partialTicks;
        double worldTicks = dayTicks * (double) AstronomyUtil.TIME_MULTIPLIER;
        float centerU = MAP_TEX_W * 0.5F;
        float centerV = MAP_TEX_H * 0.5F;
        float systemOrbitScale = getOrbitScalePxPerKm(root);

        if (focusedBody != null && focusedBody.getStar() != root) cancelFocusState();
        if (focusAnimationBody != null && focusAnimationBody.getStar() != root) cancelFocusState();

        updateFocusView(root, centerU, centerV, systemOrbitScale, worldTicks);
        drawBodyTree(
                root,
                centerU,
                centerV,
                systemOrbitScale,
                worldTicks,
                dayTicks,
                Float.NaN,
                Float.NaN);

        hoveredBody = findBodyUnderCursor(lastMouseX, lastMouseY);
        hoveredSatellite = findSatelliteUnderCursor(lastMouseX, lastMouseY);
    }

    private void drawSurfaceMap() {
        if (!draggingSurface) clampSurface();

        scene.partialTex(
                SkyPipelines.TEXTURED_TRANSLUCENT,
                StarmapScene.STARMAP,
                1F,
                1F,
                1F,
                1F,
                leftPos + MAP_X,
                topPos + MAP_Y,
                MAP_W,
                MAP_H,
                0F,
                0F,
                MAP_W / STARMAP_BG_TEX_W,
                MAP_H / STARMAP_BG_TEX_H);

        int[] heightmap = star.heightmap;
        if (heightmap == null) return;

        if (star.updateHeightmap) {
            NativeImage pixels = groundTexture.getPixels();
            for (int i = 0; i < heightmap.length; i++) {
                int h = Math.floorMod(heightmap[i], 16) * 16;
                pixels.setPixel(
                        i % StardarBlockEntity.MAP_SIZE,
                        i / StardarBlockEntity.MAP_SIZE,
                        0xFF000000 | h << 8);
            }
            groundTexture.upload();
            star.updateHeightmap = false;
        }

        scene.partialTex(
                SkyPipelines.TEXTURED_TRANSLUCENT,
                GROUND,
                1F,
                1F,
                1F,
                1F,
                leftPos + MAP_X + (int) surfaceX - 256,
                topPos + MAP_Y + (int) surfaceY - 256,
                512,
                512,
                0F,
                0F,
                1F,
                1F);
    }

    private void clampSurface() {
        surfaceX = Mth.clamp(surfaceX, -256 + MAP_W, 256);
        surfaceY = Mth.clamp(surfaceY, -256 + MAP_H, 256);
    }

    private void drawBodyTree(
            CelestialBody body,
            float bodyMapU,
            float bodyMapV,
            float systemOrbitScalePxPerKm,
            double worldTicks,
            double dayTicks,
            float parentMapU,
            float parentMapV) {
        List<ChildRenderState> childStates = new ArrayList<>();
        float parentScreenY = mapToScreenY(bodyMapU, bodyMapV);

        for (CelestialBody child : body.satellites) {
            float childScale = getChildOrbitScalePxPerKm(body, systemOrbitScalePxPerKm);
            double meanAnomaly = calculateMeanAnomaly(child, worldTicks);
            float[] offset = calculateOrbitOffsetPx(body, child, meanAnomaly, childScale);

            boolean moonChild = body.parent != null;
            float childMapU = bodyMapU + offset[0];
            float childMapV = bodyMapV + offset[1];
            boolean drawInFrontOfParent =
                    !moonChild || mapToScreenY(childMapU, childMapV) > parentScreenY;

            childStates.add(
                    new ChildRenderState(
                            child,
                            childScale,
                            offset[0],
                            offset[1],
                            moonChild,
                            drawInFrontOfParent));
        }

        for (ChildRenderState childState : childStates) {
            if (!shouldDrawChildOrbit(body, childState.childBody)) continue;

            if (childState.moonChild) {
                drawOrbitHalf(
                        bodyMapU,
                        bodyMapV,
                        body,
                        childState.childBody,
                        childState.orbitScalePxPerKm,
                        false);
            } else {
                drawOrbit(
                        bodyMapU,
                        bodyMapV,
                        body,
                        childState.childBody,
                        childState.orbitScalePxPerKm);
            }
        }

        for (ChildRenderState childState : childStates) {
            if (!childState.moonChild || childState.drawInFrontOfParent) continue;
            drawBodyTree(
                    childState.childBody,
                    bodyMapU + childState.orbitOffsetU,
                    bodyMapV + childState.orbitOffsetV,
                    systemOrbitScalePxPerKm,
                    worldTicks,
                    dayTicks,
                    bodyMapU,
                    bodyMapV);
        }

        drawArtificialSatellites(body, bodyMapU, bodyMapV, false);
        drawBody(body, bodyMapU, bodyMapV, dayTicks, parentMapU, parentMapV);
        drawArtificialSatellites(body, bodyMapU, bodyMapV, true);

        for (ChildRenderState childState : childStates) {
            if (!childState.moonChild || !shouldDrawChildOrbit(body, childState.childBody))
                continue;
            drawOrbitHalf(
                    bodyMapU,
                    bodyMapV,
                    body,
                    childState.childBody,
                    childState.orbitScalePxPerKm,
                    true);
        }

        for (ChildRenderState childState : childStates) {
            if (childState.moonChild && !childState.drawInFrontOfParent) continue;
            drawBodyTree(
                    childState.childBody,
                    bodyMapU + childState.orbitOffsetU,
                    bodyMapV + childState.orbitOffsetV,
                    systemOrbitScalePxPerKm,
                    worldTicks,
                    dayTicks,
                    bodyMapU,
                    bodyMapV);
        }
    }

    private boolean shouldDrawChildOrbit(CelestialBody parent, CelestialBody child) {
        if (parent.parent == null) return true;
        if (!StarmapScene.isMoon(child)) return true;

        CelestialBody activeFocus = getRenderFocusBody();
        return activeFocus != null && (activeFocus == parent || activeFocus.parent == parent);
    }

    private static float[] orbitColor(CelestialBody body) {
        float[] color = body.color != null && body.color.length >= 3 ? body.color : null;
        return new float[] {
            color != null ? color[0] : 0.8F,
            color != null ? color[1] : 0.8F,
            color != null ? color[2] : 0.8F
        };
    }

    private void drawOrbit(
            float parentMapU,
            float parentMapV,
            CelestialBody parent,
            CelestialBody body,
            float orbitScalePxPerKm) {
        if (body.semiMajorAxisKm <= 0F) return;
        float[] color = orbitColor(body);

        float prevX = 0F;
        float prevY = 0F;
        for (int i = 0; i <= 96; i++) {
            double meanAnomaly = 2D * Math.PI * ((double) i / 96D);
            float[] offset = calculateOrbitOffsetPx(parent, body, meanAnomaly, orbitScalePxPerKm);
            float orbitMapU = parentMapU + offset[0];
            float orbitMapV = parentMapV + offset[1];
            float x = mapToScreenX(orbitMapU, orbitMapV);
            float y = mapToScreenY(orbitMapU, orbitMapV);
            if (i > 0) scene.line(prevX, prevY, x, y, 2F, color[0], color[1], color[2], 0.4F);
            prevX = x;
            prevY = y;
        }
    }

    private void drawOrbitHalf(
            float parentMapU,
            float parentMapV,
            CelestialBody parent,
            CelestialBody body,
            float orbitScalePxPerKm,
            boolean frontHalf) {
        if (body.semiMajorAxisKm <= 0F) return;
        float[] color = orbitColor(body);

        float crossY = mapToScreenY(parentMapU, parentMapV);
        boolean hasPrevPoint = false;
        float prevScreenX = 0F;
        float prevScreenY = 0F;
        boolean prevInFrontHalf = false;

        for (int i = 0; i <= 96; i++) {
            double meanAnomaly = 2D * Math.PI * ((double) i / 96D);
            float[] offset = calculateOrbitOffsetPx(parent, body, meanAnomaly, orbitScalePxPerKm);
            float orbitMapU = parentMapU + offset[0];
            float orbitMapV = parentMapV + offset[1];
            float currScreenX = mapToScreenX(orbitMapU, orbitMapV);
            float currScreenY = mapToScreenY(orbitMapU, orbitMapV);
            boolean currInFrontHalf = currScreenY >= crossY;

            if (!hasPrevPoint) {
                prevScreenX = currScreenX;
                prevScreenY = currScreenY;
                prevInFrontHalf = currInFrontHalf;
                hasPrevPoint = true;
                continue;
            }

            boolean prevInSelectedHalf = prevInFrontHalf == frontHalf;
            boolean currInSelectedHalf = currInFrontHalf == frontHalf;

            if (prevInSelectedHalf && currInSelectedHalf) {
                scene.line(
                        prevScreenX,
                        prevScreenY,
                        currScreenX,
                        currScreenY,
                        2F,
                        color[0],
                        color[1],
                        color[2],
                        0.4F);
            } else if (prevInSelectedHalf != currInSelectedHalf) {
                float dy = currScreenY - prevScreenY;
                float tCross = dy == 0F ? 0.5F : (crossY - prevScreenY) / dy;
                tCross = Mth.clamp(tCross, 0F, 1F);
                float crossX = prevScreenX + (currScreenX - prevScreenX) * tCross;

                if (prevInSelectedHalf) {
                    scene.line(
                            prevScreenX,
                            prevScreenY,
                            crossX,
                            crossY,
                            2F,
                            color[0],
                            color[1],
                            color[2],
                            0.4F);
                } else {
                    scene.line(
                            crossX,
                            crossY,
                            currScreenX,
                            currScreenY,
                            2F,
                            color[0],
                            color[1],
                            color[2],
                            0.4F);
                }
            }

            prevScreenX = currScreenX;
            prevScreenY = currScreenY;
            prevInFrontHalf = currInFrontHalf;
        }
    }

    private void drawBody(
            CelestialBody body,
            float bodyMapU,
            float bodyMapV,
            double dayTicks,
            float parentMapU,
            float parentMapV) {
        float drawSize = getBodySizePxAtZoom(body);
        float bodyScreenX = mapToScreenX(bodyMapU, bodyMapV);
        float bodyScreenY = mapToScreenY(bodyMapU, bodyMapV);
        float half = drawSize * 0.5F;

        float minX = bodyScreenX - half;
        float maxX = bodyScreenX + half;
        float minY = bodyScreenY - half;
        float maxY = bodyScreenY + half;

        if (body.hasRings) {
            float ringHalfWidth = StarmapScene.ringHalfWidth(body, drawSize);
            float ringHalfHeight = StarmapScene.ringHalfHeight(body, ringHalfWidth);
            minX = Math.min(minX, bodyScreenX - ringHalfWidth);
            maxX = Math.max(maxX, bodyScreenX + ringHalfWidth);
            minY = Math.min(minY, bodyScreenY - ringHalfHeight);
            maxY = Math.max(maxY, bodyScreenY + ringHalfHeight);
        }

        if (maxX < leftPos + MAP_X
                || minX > leftPos + MAP_X + MAP_W
                || maxY < topPos + MAP_Y
                || minY > topPos + MAP_Y + MAP_H) {
            return;
        }

        scene.body(
                body,
                bodyScreenX,
                bodyScreenY,
                drawSize,
                dayTicks,
                calculateHorizontalCrescentPhase(body, bodyMapU, bodyMapV, parentMapU, parentMapV),
                star.getLevel().getGameTime(),
                colorArrayToRgb(body.color));

        float labelAnchorExtentX = half;
        float labelAnchorExtentY = half;
        if (body.parent == null) {
            float starLabelHalfExtent = (8F * mapZoom) * 0.5F;
            labelAnchorExtentX = Math.min(labelAnchorExtentX, starLabelHalfExtent);
            labelAnchorExtentY = Math.min(labelAnchorExtentY, starLabelHalfExtent);
        }
        renderedBodyLabels.add(
                new BodyLabelRenderInfo(
                        body, bodyScreenX + labelAnchorExtentX, bodyScreenY - labelAnchorExtentY));

        renderedBodies.add(new BodyRenderInfo(body, bodyMapU, bodyMapV, drawSize));
    }

    private void drawArtificialSatellites(
            CelestialBody body, float bodyMapU, float bodyMapV, boolean frontHalf) {
        if (body != currentBody) return;

        Map<Integer, ClientSatellites.Entry> satellites = ClientSatellites.current(star.getLevel());
        if (satellites.isEmpty()) return;

        float baseOrbitRadiusMapPx =
                StarmapScene.bodySizePxAt1x(body) * SATELLITE_ORBIT_RADIUS_SCALE;
        if (baseOrbitRadiusMapPx <= 0F) return;

        if (shouldDrawArtificialSatelliteOrbitLines(body)) {
            for (ClientSatellites.Entry satellite : satellites.values()) {
                drawArtificialSatelliteOrbitHalf(
                        bodyMapU, bodyMapV, baseOrbitRadiusMapPx, satellite.orbit(), frontHalf);
            }
        }

        float iconSize =
                Mth.clamp(
                        getBodySizePxAtZoom(body) * SATELLITE_ICON_BODY_SCALE * 0.25F, 0.2F, 4.0F);
        float iconHalf = iconSize * 0.5F;
        float mapLeft = leftPos + MAP_X;
        float mapTop = topPos + MAP_Y;
        double angle = artificialSatelliteAngle();

        for (Map.Entry<Integer, ClientSatellites.Entry> entry : satellites.entrySet()) {
            SatelliteOrbit orbit = entry.getValue().orbit();
            float[] point =
                    orbitPoint(orbit, orbit.orbitAngle(angle, Mth.TWO_PI), baseOrbitRadiusMapPx);
            float screenX = mapToScreenX(bodyMapU + point[0], bodyMapV + point[1]);
            float screenY = mapToScreenY(bodyMapU + point[0], bodyMapV + point[1]);
            if ((point[2] >= 0F) != frontHalf) continue;
            if (screenX + iconHalf < mapLeft
                    || screenX - iconHalf > mapLeft + MAP_W
                    || screenY + iconHalf < mapTop
                    || screenY - iconHalf > mapTop + MAP_H) {
                continue;
            }

            scene.partialTex(
                    SkyPipelines.TEXTURED_TRANSLUCENT,
                    StarmapScene.SATELLITE,
                    1F,
                    1F,
                    1F,
                    1F,
                    screenX - iconHalf,
                    screenY - iconHalf,
                    iconSize,
                    iconSize,
                    0F,
                    0F,
                    1F,
                    1F);

            SatelliteRenderInfo info =
                    new SatelliteRenderInfo(
                            entry.getKey(),
                            orbit,
                            bodyMapU + point[0],
                            bodyMapV + point[1],
                            iconSize);
            renderedSatellites.add(info);
            if (focusedSatelliteFrequency != null && focusedSatelliteFrequency == info.frequency) {
                focusedSatellite = info;
            }
        }
    }

    private void drawArtificialSatelliteOrbitHalf(
            float bodyMapU,
            float bodyMapV,
            float baseRadiusMapPx,
            SatelliteOrbit orbit,
            boolean frontHalf) {
        float r = Mth.clamp(orbit.red(), 0F, 1F);
        float g = Mth.clamp(orbit.green(), 0F, 1F);
        float b = Mth.clamp(orbit.blue(), 0F, 1F);

        boolean hasPrev = false;
        float prevX = 0F;
        float prevY = 0F;
        float prevDepth = 0F;
        boolean prevInFrontHalf = false;

        for (int i = 0; i <= 64; i++) {
            float[] point =
                    orbitPoint(orbit, (float) (2D * Math.PI * ((double) i / 64D)), baseRadiusMapPx);
            float currX = mapToScreenX(bodyMapU + point[0], bodyMapV + point[1]);
            float currY = mapToScreenY(bodyMapU + point[0], bodyMapV + point[1]);
            float currDepth = point[2];
            boolean currInFrontHalf = currDepth >= 0F;

            if (hasPrev) {
                boolean prevSelected = prevInFrontHalf == frontHalf;
                boolean currSelected = currInFrontHalf == frontHalf;

                if (prevSelected && currSelected) {
                    scene.line(prevX, prevY, currX, currY, 1F, r, g, b, 0.25F);
                } else if (prevSelected != currSelected) {
                    float depthDelta = currDepth - prevDepth;
                    float t = Mth.clamp(depthDelta == 0F ? 0.5F : -prevDepth / depthDelta, 0F, 1F);
                    float crossX = prevX + (currX - prevX) * t;
                    float crossY = prevY + (currY - prevY) * t;
                    if (prevSelected) {
                        scene.line(prevX, prevY, crossX, crossY, 1F, r, g, b, 0.25F);
                    } else {
                        scene.line(crossX, crossY, currX, currY, 1F, r, g, b, 0.25F);
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

    private static float[] orbitPoint(SatelliteOrbit orbit, float angle, float baseRadiusMapPx) {
        double inclination = Math.toRadians(orbit.inclination());
        double radiusMapPx =
                baseRadiusMapPx * (orbit.altitude() / AstronomyUtil.DEFAULT_ALTITUDE_KM);

        double x = radiusMapPx * Mth.cos(angle);
        double y = radiusMapPx * Mth.sin(angle);
        double z = Math.sin(inclination) * y;
        y = Math.cos(inclination) * y;

        y -= z * 0.35D;
        y *= 0.8D;

        return new float[] {(float) x, (float) y, (float) z};
    }

    private double artificialSatelliteAngle() {
        double ticks = star.getLevel().getGameTime() + (double) partialTicks;
        return -ticks / SatelliteOrbit.REVOLUTION_TICKS * 2D * Math.PI;
    }

    private boolean shouldDrawArtificialSatelliteOrbitLines(@Nullable CelestialBody body) {
        CelestialBody activeFocus = getRenderFocusBody();
        if (activeFocus == null && focusedSatelliteFrequency != null) activeFocus = currentBody;
        return body != null
                && activeFocus != null
                && (activeFocus == body || activeFocus.parent == body);
    }

    private @Nullable SatelliteRenderInfo findSatelliteUnderCursor(int mouseX, int mouseY) {
        SatelliteRenderInfo nearest = null;
        float nearestDistSq = Float.MAX_VALUE;

        for (int i = renderedSatellites.size() - 1; i >= 0; i--) {
            SatelliteRenderInfo info = renderedSatellites.get(i);
            float dx = mouseX - mapToScreenX(info.mapU, info.mapV);
            float dy = mouseY - mapToScreenY(info.mapU, info.mapV);
            float clickableRadius = Math.max(4F, info.drawSize * 0.75F);
            float distanceSq = dx * dx + dy * dy;

            if (distanceSq <= clickableRadius * clickableRadius && distanceSq < nearestDistSq) {
                nearestDistSq = distanceSq;
                nearest = info;
            }
        }

        return nearest;
    }

    private void clearSatelliteFocus() {
        focusedSatelliteFrequency = null;
        focusedSatellite = null;
    }

    private void focusSatellite(SatelliteRenderInfo info) {
        if (focusedSatelliteFrequency != null && focusedSatelliteFrequency == info.frequency)
            return;

        cancelFocusState();
        focusedSatelliteFrequency = info.frequency;
        focusedSatellite = info;
        setViewFromCenter(info.mapU, info.mapV, mapZoom);
        playClick();
    }

    private boolean updateFocusedSatelliteView(
            CelestialBody starBody,
            float starCenterU,
            float starCenterV,
            float systemOrbitScalePxPerKm,
            double worldTicks) {
        if (focusedSatelliteFrequency == null) return false;

        if (!findSatelliteMapPosition(
                starBody,
                starCenterU,
                starCenterV,
                systemOrbitScalePxPerKm,
                worldTicks,
                focusedSatelliteFrequency,
                trackedSatellitePosition)) {
            clearSatelliteFocus();
            return false;
        }

        setViewFromCenter(trackedSatellitePosition.mapU, trackedSatellitePosition.mapV, mapZoom);
        return true;
    }

    private boolean findSatelliteMapPosition(
            CelestialBody starBody,
            float starCenterU,
            float starCenterV,
            float systemOrbitScalePxPerKm,
            double worldTicks,
            int frequency,
            BodyPosition outPosition) {
        if (currentBody == null) return false;

        ClientSatellites.Entry satellite = ClientSatellites.current(star.getLevel()).get(frequency);
        if (satellite == null) return false;

        if (!findBodyMapPosition(
                starBody,
                starCenterU,
                starCenterV,
                systemOrbitScalePxPerKm,
                worldTicks,
                currentBody,
                trackedBodyPosition)) {
            return false;
        }

        float baseOrbitRadiusMapPx =
                StarmapScene.bodySizePxAt1x(currentBody) * SATELLITE_ORBIT_RADIUS_SCALE;
        if (baseOrbitRadiusMapPx <= 0F) return false;

        SatelliteOrbit orbit = satellite.orbit();
        float[] point =
                orbitPoint(
                        orbit,
                        orbit.orbitAngle(artificialSatelliteAngle(), Mth.TWO_PI),
                        baseOrbitRadiusMapPx);
        outPosition.mapU = trackedBodyPosition.mapU + point[0];
        outPosition.mapV = trackedBodyPosition.mapV + point[1];
        return true;
    }

    private void drawSatelliteTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (landingMode) return;

        boolean focusedTooltip = focusedSatelliteFrequency != null && focusedSatellite != null;
        if (!focusedTooltip
                && (!isMouseInsideMap(mouseX, mouseY)
                        || !shouldDrawArtificialSatelliteOrbitLines(currentBody))) {
            return;
        }

        SatelliteRenderInfo info = focusedTooltip ? focusedSatellite : hoveredSatellite;
        if (info == null) return;

        SatelliteOrbit orbit = info.orbit;
        String owner = !orbit.owner().isEmpty() ? orbit.owner() : SatelliteOrbit.DEFAULT_OWNER;
        List<Component> tooltip =
                List.of(
                        satelliteLine("owner", Component.literal(owner)),
                        satelliteLine(
                                "speed",
                                Component.literal(SatelliteItems.speed(orbit.altitude()))
                                        .append(
                                                Component.translatable(
                                                        "gui.sat.settings.unit.km_per_second"))),
                        satelliteLine(
                                "altitude",
                                Component.literal(SatelliteItems.format(orbit.altitude()))
                                        .append(
                                                Component.translatable(
                                                        "gui.sat.settings.unit.km"))),
                        satelliteLine(
                                "inclination",
                                Component.literal(
                                        SatelliteItems.format(orbit.inclination()) + "\u00B0")),
                        satelliteLine(
                                "phase",
                                Component.literal(
                                        SatelliteItems.phase(orbit.phaseOffset()) + "\u00B0")));

        int tooltipX = mouseX;
        int tooltipY = mouseY;
        if (focusedTooltip) {
            float half = info.drawSize * 0.5F;
            tooltipX = Math.round(mapToScreenX(info.mapU, info.mapV) + half);
            tooltipY = Math.round(mapToScreenY(info.mapU, info.mapV) - half);
        }

        graphics.setComponentTooltipForNextFrame(font, tooltip, tooltipX, tooltipY);
    }

    private static Component satelliteLine(String key, Component value) {
        return Component.translatable("item.hbmspace.sat.desc." + key)
                .append(": ")
                .withStyle(ChatFormatting.YELLOW)
                .append(value.copy().withStyle(ChatFormatting.GOLD));
    }

    private void drawQueuedBodyLabels(GuiGraphicsExtractor graphics) {
        List<BodyLabelRenderInfo> sortedLabels = new ArrayList<>(renderedBodyLabels);
        sortedLabels.sort(
                Comparator.comparingInt(
                                (BodyLabelRenderInfo label) -> getBodyHierarchyDepth(label.body))
                        .reversed());

        graphics.pose().pushMatrix();
        graphics.pose().translate(-leftPos, -topPos);
        graphics.pose().scale(0.6F, 0.6F);
        for (BodyLabelRenderInfo labelInfo : sortedLabels) {
            drawBodyLabel(
                    graphics,
                    labelInfo.body,
                    labelInfo.topRightAnchorScreenX,
                    labelInfo.topRightAnchorScreenY);
        }
        graphics.pose().popMatrix();
    }

    private void drawBodyLabel(
            GuiGraphicsExtractor graphics,
            CelestialBody body,
            float anchorScreenX,
            float anchorScreenY) {
        if (!shouldShowBodyLabel(body)) return;

        Component label = getBodyDisplayName(body);
        if (label.getString().isEmpty()) return;

        float labelX = anchorScreenX + 1.5F;
        float labelY = anchorScreenY - 1.5F - font.lineHeight * 0.6F;
        graphics.text(
                font,
                label,
                Math.round(labelX / 0.6F),
                Math.round(labelY / 0.6F),
                colorArrayToRgb(body.color),
                true);
    }

    private boolean shouldShowBodyLabel(CelestialBody body) {
        if (body.parent == null) return false;
        if (!StarmapScene.isMoon(body)) return true;

        CelestialBody focusedTopLevelPlanet = getTopLevelPlanetForBody(getRenderFocusBody());
        if (focusedTopLevelPlanet == null) return false;
        return getTopLevelPlanetForBody(body) == focusedTopLevelPlanet;
    }

    private float calculateHorizontalCrescentPhase(
            CelestialBody body,
            float bodyMapU,
            float bodyMapV,
            float parentMapU,
            float parentMapV) {
        float sunMapU = MAP_TEX_W * 0.5F;
        float sunMapV = MAP_TEX_H * 0.5F;
        float bodyProjX = projectMapToIsoX(bodyMapU, bodyMapV);
        float bodyProjY = projectMapToIsoY(bodyMapU, bodyMapV);
        float sunProjX = projectMapToIsoX(sunMapU, sunMapV);
        float sunProjY = projectMapToIsoY(sunMapU, sunMapV);

        float dx = bodyProjX - sunProjX;
        float dy = bodyProjY - sunProjY;
        float lengthSq = dx * dx + dy * dy;
        if (lengthSq <= 0.0001F) return 0F;

        float length = Mth.sqrt(lengthSq);
        float verticalFactor = dy / length;
        float phaseMagnitude = Mth.clamp((verticalFactor + 1F) * 0.5F, 0F, 1F);

        phaseMagnitude =
                applyMoonEclipseDarkening(
                        body,
                        bodyMapU,
                        bodyMapV,
                        parentMapU,
                        parentMapV,
                        sunMapU,
                        sunMapV,
                        phaseMagnitude);

        float phaseSign = dx <= 0F ? 1F : -1F;
        return Mth.clamp(phaseMagnitude * phaseSign, -1F, 1F);
    }

    private float applyMoonEclipseDarkening(
            CelestialBody body,
            float moonMapU,
            float moonMapV,
            float parentMapU,
            float parentMapV,
            float sunMapU,
            float sunMapV,
            float phaseMagnitude) {
        if (!StarmapScene.isMoon(body) || body.parent == null) return phaseMagnitude;
        if (Float.isNaN(parentMapU) || Float.isNaN(parentMapV)) return phaseMagnitude;

        float moonProjX = projectMapToIsoX(moonMapU, moonMapV);
        float moonProjY = projectMapToIsoY(moonMapU, moonMapV);
        float parentProjX = projectMapToIsoX(parentMapU, parentMapV);
        float parentProjY = projectMapToIsoY(parentMapU, parentMapV);
        float sunProjX = projectMapToIsoX(sunMapU, sunMapV);
        float sunProjY = projectMapToIsoY(sunMapU, sunMapV);

        float sunToParentX = parentProjX - sunProjX;
        float sunToParentY = parentProjY - sunProjY;
        float parentToMoonX = moonProjX - parentProjX;
        float parentToMoonY = moonProjY - parentProjY;

        float sunParentLenSq = sunToParentX * sunToParentX + sunToParentY * sunToParentY;
        if (sunParentLenSq <= 0.0001F) return phaseMagnitude;

        float sunParentLen = Mth.sqrt(sunParentLenSq);
        float alongShadowAxis =
                (sunToParentX * parentToMoonX + sunToParentY * parentToMoonY) / sunParentLen;
        float lineDistance =
                Math.abs(sunToParentX * parentToMoonY - sunToParentY * parentToMoonX)
                        / sunParentLen;
        float eclipseRadius = Math.max(1.5F, StarmapScene.bodySizePxAt1x(body.parent) * 0.45F);
        float penumbraRadius = eclipseRadius * 2F;
        float behindFade = Math.max(0.15F, eclipseRadius * 0.12F);

        if (lineDistance >= penumbraRadius || alongShadowAxis <= -behindFade) return phaseMagnitude;

        float behindFactor =
                alongShadowAxis >= 0F ? 1F : smoothstep(alongShadowAxis, -behindFade, 0F);
        if (lineDistance <= eclipseRadius) {
            return Mth.clamp(phaseMagnitude + (1F - phaseMagnitude) * behindFactor, 0F, 1F);
        }

        float penumbraFactor = 1F - smoothstep(lineDistance, eclipseRadius, penumbraRadius);
        float eclipseFactor = behindFactor * penumbraFactor;
        return Mth.clamp(phaseMagnitude + (1F - phaseMagnitude) * eclipseFactor, 0F, 1F);
    }

    private static float smoothstep(float t, float edge0, float edge1) {
        t = Mth.clamp((t - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private float projectMapToIsoX(float mapU, float mapV) {
        return (mapU - mapV) * ISO_X;
    }

    private float projectMapToIsoY(float mapU, float mapV) {
        return (mapU + mapV) * ISO_Y;
    }

    private float mapToScreenX(float mapCoordU, float mapCoordV) {
        float centerU = mapU + (MAP_W / mapZoom) * 0.5F;
        float centerV = mapV + (MAP_H / mapZoom) * 0.5F;
        float relU = mapCoordU - centerU;
        float relV = mapCoordV - centerV;
        return leftPos + MAP_X + MAP_W * 0.5F + (relU - relV) * mapZoom * ISO_X;
    }

    private float mapToScreenY(float mapCoordU, float mapCoordV) {
        float centerU = mapU + (MAP_W / mapZoom) * 0.5F;
        float centerV = mapV + (MAP_H / mapZoom) * 0.5F;
        float relU = mapCoordU - centerU;
        float relV = mapCoordV - centerV;
        return topPos + MAP_Y + MAP_H * 0.5F + (relU + relV) * mapZoom * ISO_Y;
    }

    private @Nullable BodyRenderInfo findBodyUnderCursor(int mouseX, int mouseY) {
        BodyRenderInfo nearest = null;
        float nearestDistSq = Float.MAX_VALUE;

        for (int i = renderedBodies.size() - 1; i >= 0; i--) {
            BodyRenderInfo bodyInfo = renderedBodies.get(i);
            float bodyX = mapToScreenX(bodyInfo.mapU, bodyInfo.mapV);
            float bodyY = mapToScreenY(bodyInfo.mapU, bodyInfo.mapV);
            float clickableRadius = Math.max(5F, bodyInfo.drawSize * 0.5F);
            float dx = mouseX - bodyX;
            float dy = mouseY - bodyY;
            float distanceSq = dx * dx + dy * dy;

            if (distanceSq <= clickableRadius * clickableRadius && distanceSq < nearestDistSq) {
                nearestDistSq = distanceSq;
                nearest = bodyInfo;
            }
        }

        return nearest;
    }

    @Override
    public boolean mouseScrolled(double mouseXd, double mouseYd, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseXd, mouseYd, scrollX, scrollY)) return true;
        if (landingMode || scrollY == 0) return false;

        int mouseX = (int) mouseXd;
        int mouseY = (int) mouseYd;
        if (!isMouseInsideMap(mouseX, mouseY) || focusAnimationActive) return false;
        if (StarmapScene.isMoon(focusedBody)) return false;

        float oldZoom = mapZoom;
        float minZoom = MIN_ZOOM;
        float maxZoom = MAX_ZOOM;

        if (focusedBody != null) {
            float[] bounds = new float[2];
            if (calculateFocusedChildZoomBounds(focusedBody, bounds)) {
                minZoom = bounds[0];
                maxZoom = Math.max(bounds[1], getBodyDetailZoomCap(focusedBody));
            }
        }

        float newZoom = oldZoom;
        if (scrollY > 0) newZoom = Math.min(maxZoom, oldZoom * ZOOM_STEP);
        if (scrollY < 0) newZoom = Math.max(minZoom, oldZoom / ZOOM_STEP);

        if (newZoom != oldZoom) {
            if (isFocusContextActive()) {
                mapZoom = newZoom;
            } else {
                zoomAtMouse(mouseX, mouseY, oldZoom, newZoom);
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        boolean handled = super.mouseDragged(event, dragX, dragY);
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (landingMode) {
            if (!draggingSurface) return handled;
            surfaceX += mouseX - mX;
            surfaceY += mouseY - mY;
            mX = mouseX;
            mY = mouseY;
            clampSurface();
            return true;
        }

        if (!draggingMap) return handled;
        if (isFocusContextActive() || event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            draggingMap = false;
            return handled;
        }
        int dx = mouseX - dragLastX;
        int dy = mouseY - dragLastY;
        if (dx != 0 || dy != 0) {
            panByScreenDelta(dx, dy);
            clampMapUV();
        }
        dragLastX = mouseX;
        dragLastY = mouseY;
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();

        if (checkClick(mouseX, mouseY, 129, 143, 18, 18)) {
            playClick();
            PacketWire.sendToServer(
                    SpaceStardarTarget.encode(
                            menu.containerId, SpaceDimensions.ORBIT.identifier(), false));
            return true;
        }

        if (checkClick(mouseX, mouseY, 129, 123, 18, 18)) {
            playClick();
            CelestialBody body = currentBody;
            if (body != null) {
                clearSatelliteFocus();
                if (landingMode) exitLandingMode();
                configureFocusAnimation(body, body, true, 420, 0F, 0F, getFocusZoomForBody(body));
            }
            return true;
        }

        if (checkClick(mouseX, mouseY, 149, 143, 18, 18)) {
            playClick();
            PacketWire.sendToServer(
                    SpaceStardarTarget.encode(
                            menu.containerId, star.getLevel().dimension().identifier(), true));
            return true;
        }

        if (!isMouseInsideMap(mouseX, mouseY)) {
            draggingMap = false;
            draggingSurface = false;
            return handled;
        }

        if (landingMode) {
            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                exitLandingMode();
                return true;
            }
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                draggingSurface = true;
                sX = mX = mouseX;
                sY = mY = mouseY;
            }
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            if (focusedSatelliteFrequency != null) {
                clearSatelliteFocus();
            } else {
                beginUnfocusAnimation();
            }
            draggingMap = false;
            return true;
        }

        if (button != InputConstants.MOUSE_BUTTON_LEFT) return handled;

        SatelliteRenderInfo clickedSatellite = findSatelliteUnderCursor(mouseX, mouseY);
        if (clickedSatellite != null) {
            focusSatellite(clickedSatellite);
            draggingMap = false;
            return true;
        }

        BodyRenderInfo clickedBody = findBodyUnderCursor(mouseX, mouseY);
        if (clickedBody != null) {
            CelestialBody activeFocusBody = getRenderFocusBody();
            if (activeFocusBody != null && clickedBody.body == activeFocusBody) {
                handleFocusedBodySecondClick(clickedBody.body);
                draggingMap = false;
                return true;
            }

            if (clickedBody.body.parent != null) {
                startFocusAnimation(clickedBody);
                draggingMap = false;
                return true;
            }
        }

        if (isFocusContextActive()) {
            draggingMap = false;
            return true;
        }

        draggingMap = true;
        dragLastX = mouseX;
        dragLastY = mouseY;
        return true;
    }

    private void handleFocusedBodySecondClick(CelestialBody body) {
        if (!body.canLand) return;
        if (menu.getSlot(0).getItem().is(MissilePartsSpace.HARD_DRIVE.get()))
            enterLandingMode(body);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = super.mouseReleased(event);
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            draggingMap = false;
            draggingSurface = false;
        }

        if (!landingMode || event.button() != InputConstants.MOUSE_BUTTON_LEFT) return handled;

        if (Math.abs(sX - mouseX) > 2 || Math.abs(sY - mouseY) > 2) return handled;
        if (!isMouseInsideMap(mouseX, mouseY)) return handled;
        int[] heightmap = star.heightmap;
        if (heightmap == null) return handled;

        int hx = (mouseX - (int) surfaceX - leftPos - MAP_X + 256) / 2;
        int hz = (mouseY - (int) surfaceY - topPos - MAP_Y + 256) / 2;
        if (StardarBlockEntity.landingIssue(heightmap, hx, hz) != null) return handled;

        playClick();
        PacketWire.sendToServer(SpaceStardarLanding.encode(menu.containerId, hx, hz));
        return true;
    }

    private void panByScreenDelta(int dx, int dy) {
        float zoom = Math.max(0.0001F, mapZoom);
        float normalizedZoom = (Mth.clamp(mapZoom, MIN_ZOOM, 5.0F) - MIN_ZOOM) / (5.0F - MIN_ZOOM);
        float speed = (0.35F + (1.20F - 0.35F) * normalizedZoom) * 0.75F;

        float a = -dx / (zoom * ISO_X);
        float b = -dy / (zoom * ISO_Y);
        float deltaU = 0.5F * (a + b);
        float deltaV = 0.5F * (b - a);

        mapU += deltaU * speed;
        mapV += deltaV * speed;
    }

    private void startFocusAnimation(BodyRenderInfo bodyInfo) {
        if (bodyInfo.body.parent == null) return;
        if (bodyInfo.body == focusedBody) return;
        if (focusAnimationActive && focusAnimationToBody && bodyInfo.body == focusAnimationBody)
            return;

        clearSatelliteFocus();
        playClick();
        configureFocusAnimation(
                bodyInfo.body,
                bodyInfo.body,
                true,
                500,
                bodyInfo.mapU,
                bodyInfo.mapV,
                getFocusZoomForBody(bodyInfo.body));
    }

    private void beginUnfocusAnimation() {
        if (!hasBodyFocusContext()) return;

        CelestialBody activeFocus = focusedBody != null ? focusedBody : focusAnimationBody;
        CelestialBody parentFocus = activeFocus != null ? activeFocus.parent : null;
        if (parentFocus != null && parentFocus.parent != null) {
            configureFocusAnimation(
                    parentFocus, parentFocus, true, 420, 0F, 0F, getFocusZoomForBody(parentFocus));
            return;
        }

        configureFocusAnimation(null, null, false, 420, MAP_TEX_W * 0.5F, MAP_TEX_H * 0.5F, 1.0F);
    }

    private void configureFocusAnimation(
            @Nullable CelestialBody newFocusedBody,
            @Nullable CelestialBody animationBody,
            boolean animationToBody,
            int durationMs,
            float targetCenterU,
            float targetCenterV,
            float targetZoom) {
        focusedBody = newFocusedBody;
        focusAnimationBody = animationBody;
        focusAnimationToBody = animationToBody;
        focusAnimationActive = true;
        focusAnimDurationMs = durationMs;
        focusAnimStartTimeMs = (minecraft.gui.hud.getGuiTicks() + (double) partialTicks) * 50D;
        focusAnimStartCenterU = mapU + (MAP_W / mapZoom) * 0.5F;
        focusAnimStartCenterV = mapV + (MAP_H / mapZoom) * 0.5F;
        focusAnimStartZoom = mapZoom;
        focusAnimTargetCenterU = targetCenterU;
        focusAnimTargetCenterV = targetCenterV;
        focusAnimTargetZoom = targetZoom;
        focusAnimTrackingInitialized = false;
        focusAnimTrackingOffsetU = 0F;
        focusAnimTrackingOffsetV = 0F;
    }

    private void updateFocusView(
            CelestialBody starBody,
            float starCenterU,
            float starCenterV,
            float systemOrbitScalePxPerKm,
            double worldTicks) {
        if (updateFocusedSatelliteView(
                starBody, starCenterU, starCenterV, systemOrbitScalePxPerKm, worldTicks)) {
            return;
        }
        if (!hasBodyFocusContext()) return;

        CelestialBody trackingBody = focusedBody != null ? focusedBody : focusAnimationBody;
        boolean hasTrackingBodyPosition = false;
        if (trackingBody != null) {
            hasTrackingBodyPosition =
                    findBodyMapPosition(
                            starBody,
                            starCenterU,
                            starCenterV,
                            systemOrbitScalePxPerKm,
                            worldTicks,
                            trackingBody,
                            trackedBodyPosition);
        }

        if (focusAnimationActive) {
            double now = (minecraft.gui.hud.getGuiTicks() + (double) partialTicks) * 50D;
            double duration = Math.max(1D, focusAnimDurationMs);
            float t = Mth.clamp((float) ((now - focusAnimStartTimeMs) / duration), 0F, 1F);
            float eased = t * t * (3F - 2F * t);

            float centerU;
            float centerV;
            if (focusAnimationToBody && hasTrackingBodyPosition) {
                if (!focusAnimTrackingInitialized) {
                    focusAnimTrackingOffsetU = focusAnimStartCenterU - trackedBodyPosition.mapU;
                    focusAnimTrackingOffsetV = focusAnimStartCenterV - trackedBodyPosition.mapV;
                    focusAnimTrackingInitialized = true;
                }
                float remaining = 1F - eased;
                centerU = trackedBodyPosition.mapU + focusAnimTrackingOffsetU * remaining;
                centerV = trackedBodyPosition.mapV + focusAnimTrackingOffsetV * remaining;
            } else {
                centerU =
                        focusAnimStartCenterU
                                + (focusAnimTargetCenterU - focusAnimStartCenterU) * eased;
                centerV =
                        focusAnimStartCenterV
                                + (focusAnimTargetCenterV - focusAnimStartCenterV) * eased;
            }

            float zoom = focusAnimStartZoom + (focusAnimTargetZoom - focusAnimStartZoom) * eased;
            setViewFromCenter(centerU, centerV, zoom);

            if (t >= 1F) {
                focusAnimationActive = false;
                focusAnimationBody = null;
                focusAnimTrackingInitialized = false;
            }
            return;
        }

        if (focusedBody != null && hasTrackingBodyPosition) {
            setViewFromCenter(trackedBodyPosition.mapU, trackedBodyPosition.mapV, mapZoom);
            return;
        }

        if (focusedBody != null) cancelFocusState();
    }

    private boolean findBodyMapPosition(
            CelestialBody body,
            float bodyMapU,
            float bodyMapV,
            float systemOrbitScalePxPerKm,
            double worldTicks,
            CelestialBody targetBody,
            BodyPosition outPosition) {
        if (body == targetBody) {
            outPosition.mapU = bodyMapU;
            outPosition.mapV = bodyMapV;
            return true;
        }

        float childOrbitScale = getChildOrbitScalePxPerKm(body, systemOrbitScalePxPerKm);
        for (CelestialBody childBody : body.satellites) {
            double meanAnomaly = calculateMeanAnomaly(childBody, worldTicks);
            float[] orbitOffset =
                    calculateOrbitOffsetPx(body, childBody, meanAnomaly, childOrbitScale);
            if (findBodyMapPosition(
                    childBody,
                    bodyMapU + orbitOffset[0],
                    bodyMapV + orbitOffset[1],
                    systemOrbitScalePxPerKm,
                    worldTicks,
                    targetBody,
                    outPosition)) {
                return true;
            }
        }

        return false;
    }

    private void zoomAtMouse(int mouseX, int mouseY, float oldZoom, float newZoom) {
        if (oldZoom <= 0F || newZoom <= 0F) {
            mapZoom = Mth.clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
            clampMapUV();
            return;
        }

        float mapCenterScreenX = leftPos + MAP_X + MAP_W * 0.5F;
        float mapCenterScreenY = topPos + MAP_Y + MAP_H * 0.5F;

        float oldCenterU = mapU + (MAP_W / oldZoom) * 0.5F;
        float oldCenterV = mapV + (MAP_H / oldZoom) * 0.5F;

        float oldA = (mouseX - mapCenterScreenX) / (oldZoom * ISO_X);
        float oldB = (mouseY - mapCenterScreenY) / (oldZoom * ISO_Y);
        float targetMapU = oldCenterU + 0.5F * (oldA + oldB);
        float targetMapV = oldCenterV + 0.5F * (oldB - oldA);

        float newA = (mouseX - mapCenterScreenX) / (newZoom * ISO_X);
        float newB = (mouseY - mapCenterScreenY) / (newZoom * ISO_Y);
        float newCenterU = targetMapU - 0.5F * (newA + newB);
        float newCenterV = targetMapV - 0.5F * (newB - newA);

        mapZoom = Mth.clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
        float viewW = MAP_W / mapZoom;
        float viewH = MAP_H / mapZoom;
        mapU = newCenterU - viewW * 0.5F;
        mapV = newCenterV - viewH * 0.5F;
        clampMapUV();
    }

    private @Nullable CelestialBody getRenderFocusBody() {
        if (focusedBody != null) return focusedBody;
        if (focusAnimationActive && focusAnimationToBody) return focusAnimationBody;
        return null;
    }

    private @Nullable CelestialBody getLocalOrbitParent() {
        CelestialBody activeFocus = getRenderFocusBody();
        if (activeFocus == null) return null;
        if (isTopLevelPlanet(activeFocus)) return activeFocus;
        return activeFocus.parent;
    }

    private @Nullable CelestialBody getTopLevelPlanetForBody(@Nullable CelestialBody body) {
        if (body == null) return null;

        CelestialBody current = body;
        int steps = 0;
        while (current.parent != null && current.parent.parent != null && steps <= 64) {
            current = current.parent;
            steps++;
        }
        return isTopLevelPlanet(current) ? current : null;
    }

    private static boolean isTopLevelPlanet(@Nullable CelestialBody body) {
        return body != null && body.parent != null && body.parent.parent == null;
    }

    private void cancelFocusState() {
        focusedBody = null;
        focusAnimationBody = null;
        focusAnimationActive = false;
        focusAnimationToBody = false;
        focusAnimTrackingInitialized = false;
    }

    private boolean isMouseInsideMap(int mouseX, int mouseY) {
        return mouseX >= leftPos + MAP_X
                && mouseX < leftPos + MAP_X + MAP_W
                && mouseY >= topPos + MAP_Y
                && mouseY < topPos + MAP_Y + MAP_H;
    }

    private boolean hasBodyFocusContext() {
        return focusedBody != null || focusAnimationActive;
    }

    private boolean isFocusContextActive() {
        return hasBodyFocusContext() || focusedSatelliteFrequency != null;
    }

    private void setViewFromCenter(float centerU, float centerV, float zoom) {
        mapZoom = Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
        float viewW = MAP_W / mapZoom;
        float viewH = MAP_H / mapZoom;
        mapU = centerU - viewW * 0.5F;
        mapV = centerV - viewH * 0.5F;
        clampMapUV();
    }

    private float getFocusZoomForBody(CelestialBody body) {
        float[] focusZoomBounds = new float[2];
        if (calculateFocusedChildZoomBounds(body, focusZoomBounds)) return focusZoomBounds[0];

        float bodySizeAt1x =
                StarmapScene.isMoon(body)
                        ? StarmapScene.moonSizePxAt1x(body)
                        : StarmapScene.bodySizePxAt1x(body);
        if (bodySizeAt1x <= 0F) return Mth.clamp(mapZoom, MIN_ZOOM, MAX_ZOOM);

        float targetBodySizePx = Math.min(MAP_W, MAP_H) * 0.88F;
        float targetZoom = targetBodySizePx / bodySizeAt1x;
        targetZoom /= 3.0F;
        targetZoom = Math.max(targetZoom, 4.0F);
        return Mth.clamp(targetZoom, MIN_ZOOM, MAX_ZOOM);
    }

    private boolean calculateFocusedChildZoomBounds(CelestialBody focusBody, float[] outBounds) {
        if (focusBody.satellites.isEmpty()) return false;

        CelestialBody starBody = focusBody.getStar();
        float systemOrbitScale = getOrbitScalePxPerKm(starBody);
        if (systemOrbitScale <= 0F) return false;

        float childOrbitScale = getChildOrbitScalePxPerKm(focusBody, systemOrbitScale);
        if (childOrbitScale <= 0F) return false;

        float nearestProjectedDist = Float.MAX_VALUE;
        float furthestProjectedDist = 0F;
        for (CelestialBody childBody : focusBody.satellites) {
            if (childBody.semiMajorAxisKm <= 0F) continue;
            for (int i = 0; i <= 96; i++) {
                double meanAnomaly = 2D * Math.PI * ((double) i / 96D);
                float[] orbitOffset =
                        calculateOrbitOffsetPx(focusBody, childBody, meanAnomaly, childOrbitScale);
                float projected = getProjectedOffsetDistanceAt1x(orbitOffset[0], orbitOffset[1]);
                if (projected <= 0F || Float.isNaN(projected) || Float.isInfinite(projected))
                    continue;
                nearestProjectedDist = Math.min(nearestProjectedDist, projected);
                furthestProjectedDist = Math.max(furthestProjectedDist, projected);
            }
        }

        if (nearestProjectedDist == Float.MAX_VALUE || furthestProjectedDist <= 0F) return false;

        float availableRadiusPx = Math.max(1F, Math.min(MAP_W, MAP_H) * 0.5F - 10F);
        float minZoom = availableRadiusPx / furthestProjectedDist;
        float maxZoom = availableRadiusPx / nearestProjectedDist;

        minZoom = Mth.clamp(minZoom, MIN_ZOOM, MAX_ZOOM);
        maxZoom = Mth.clamp(maxZoom, MIN_ZOOM, MAX_ZOOM);
        if (maxZoom < minZoom) {
            float swap = minZoom;
            minZoom = maxZoom;
            maxZoom = swap;
        }

        outBounds[0] = minZoom;
        outBounds[1] = maxZoom;
        return true;
    }

    private static float getProjectedOffsetDistanceAt1x(float offsetU, float offsetV) {
        float screenOffsetX = (offsetU - offsetV) * ISO_X;
        float screenOffsetY = (offsetU + offsetV) * ISO_Y;
        return Mth.sqrt(screenOffsetX * screenOffsetX + screenOffsetY * screenOffsetY);
    }

    private float getBodyDetailZoomCap(CelestialBody body) {
        float bodySizeAt1x =
                StarmapScene.isMoon(body)
                        ? StarmapScene.moonSizePxAt1x(body)
                        : StarmapScene.bodySizePxAt1x(body);
        if (bodySizeAt1x <= 0F || Float.isNaN(bodySizeAt1x) || Float.isInfinite(bodySizeAt1x))
            return MIN_ZOOM;

        float targetBodySizePx = Math.min(MAP_W, MAP_H) * 0.88F;
        float detailZoom = targetBodySizePx / bodySizeAt1x;
        return Mth.clamp(detailZoom, MIN_ZOOM, MAX_ZOOM);
    }

    private void clampMapUV() {
        float viewW = MAP_W / mapZoom;
        float viewH = MAP_H / mapZoom;
        float maxU = Math.max(0F, MAP_TEX_W - viewW);
        float maxV = Math.max(0F, MAP_TEX_H - viewH);
        mapU = Mth.clamp(mapU, 0F, maxU);
        mapV = Mth.clamp(mapV, 0F, maxV);
    }

    private float getOrbitScalePxPerKm(CelestialBody starBody) {
        float maxDistanceKm = getSystemMaxDistanceKm(starBody);
        if (maxDistanceKm <= 0F) return 0F;

        float starRadiusPxAt1x = StarmapScene.bodySizePxAt1x(starBody) * 0.5F;
        float availableRadiusPx =
                Math.max(
                        0F,
                        (SYSTEM_LAYOUT_REF_TEX * STARDAR_SYSTEM_RADIUS_FRACTION)
                                - starRadiusPxAt1x);
        if (availableRadiusPx <= 0F) return 0F;

        return availableRadiusPx / maxDistanceKm;
    }

    private static float getSystemMaxDistanceKm(CelestialBody starBody) {
        float maxDistance = 0F;
        for (CelestialBody child : starBody.satellites) {
            float apoapsisKm = child.semiMajorAxisKm * (1F + child.eccentricity);
            maxDistance = Math.max(maxDistance, Math.max(0F, apoapsisKm));
        }
        return maxDistance;
    }

    private float getChildOrbitScalePxPerKm(CelestialBody parent, float systemOrbitScalePxPerKm) {
        if (parent.parent != null) {
            float moonOrbitScale = getMoonOrbitScalePxPerKm(parent, systemOrbitScalePxPerKm);
            if (moonOrbitScale > 0F) return moonOrbitScale;
        }
        return systemOrbitScalePxPerKm;
    }

    private float getMoonOrbitScalePxPerKm(CelestialBody parent, float systemOrbitScalePxPerKm) {
        if (parent.satellites.isEmpty()) return 0F;

        float maxMoonDistanceKm = 0F;
        for (CelestialBody moon : parent.satellites) {
            float apoapsisKm = moon.semiMajorAxisKm * (1F + moon.eccentricity);
            maxMoonDistanceKm = Math.max(maxMoonDistanceKm, Math.max(0F, apoapsisKm));
        }
        if (maxMoonDistanceKm <= 0F) return 0F;

        float parentRadiusPxAt1x = StarmapScene.bodySizePxAt1x(parent) * 0.5F;
        float moonOrbitRadiusPxAt1x = Math.max(10.0F, parentRadiusPxAt1x * 14F);

        float nearestSiblingGapKm = getNearestSiblingOrbitGapKm(parent);
        if (systemOrbitScalePxPerKm > 0F && nearestSiblingGapKm > 0F) {
            float siblingGapPxAt1x = nearestSiblingGapKm * systemOrbitScalePxPerKm;
            float maxAllowedMoonOrbitRadiusPxAt1x = siblingGapPxAt1x * 0.45F;
            if (maxAllowedMoonOrbitRadiusPxAt1x > 0F) {
                moonOrbitRadiusPxAt1x =
                        Math.min(moonOrbitRadiusPxAt1x, maxAllowedMoonOrbitRadiusPxAt1x);
            }
        }

        return moonOrbitRadiusPxAt1x / maxMoonDistanceKm;
    }

    private static float getNearestSiblingOrbitGapKm(CelestialBody body) {
        if (body.parent == null || body.semiMajorAxisKm <= 0F) return 0F;

        float nearestGapKm = Float.MAX_VALUE;
        for (CelestialBody sibling : body.parent.satellites) {
            if (sibling == body || sibling.semiMajorAxisKm <= 0F) continue;
            float gapKm = Math.abs(sibling.semiMajorAxisKm - body.semiMajorAxisKm);
            if (gapKm > 0F) nearestGapKm = Math.min(nearestGapKm, gapKm);
        }

        return nearestGapKm == Float.MAX_VALUE ? 0F : nearestGapKm;
    }

    private float[] calculateOrbitOffsetPx(
            @Nullable CelestialBody parent,
            CelestialBody body,
            double meanAnomaly,
            float orbitScalePxPerKm) {
        double eccentricAnomaly = calculateEccentricAnomaly(meanAnomaly, body.eccentricity);
        double semiMinorAxisFactor =
                body.semiMinorAxisFactor > 0
                        ? body.semiMinorAxisFactor
                        : Math.sqrt(1D - (body.eccentricity * body.eccentricity));

        double x = body.semiMajorAxisKm * (Math.cos(eccentricAnomaly) - body.eccentricity);
        double y = body.semiMajorAxisKm * semiMinorAxisFactor * Math.sin(eccentricAnomaly);
        double z;

        double px = x;
        x = Math.cos(body.argumentPeriapsis) * px - Math.sin(body.argumentPeriapsis) * y;
        y = Math.sin(body.argumentPeriapsis) * px + Math.cos(body.argumentPeriapsis) * y;

        z = Math.sin(body.inclination) * y;
        y = Math.cos(body.inclination) * y;

        px = x;
        x = Math.cos(body.ascendingNode) * px - Math.sin(body.ascendingNode) * y;
        y = Math.sin(body.ascendingNode) * px + Math.cos(body.ascendingNode) * y;

        y -= z * 0.35D;
        y *= 0.8F;

        float mapX = (float) (x * orbitScalePxPerKm);
        float mapY = (float) (y * orbitScalePxPerKm);

        if (parent != null) {
            float parentVisualRadiusPx = StarmapScene.bodySizePxAt1x(parent) * 0.5F;
            float distanceFromParentPx = Mth.sqrt(mapX * mapX + mapY * mapY);
            if (distanceFromParentPx > 0F) {
                float radialScale =
                        (distanceFromParentPx + parentVisualRadiusPx) / distanceFromParentPx;
                mapX *= radialScale;
                mapY *= radialScale;
            } else {
                mapX = parentVisualRadiusPx;
                mapY = 0F;
            }
        }

        return new float[] {mapX, mapY};
    }

    private static double calculateMeanAnomaly(CelestialBody body, double worldTicks) {
        double orbitPeriodTicks = getOrbitalPeriodTicks(body);
        if (orbitPeriodTicks <= 0D
                || Double.isNaN(orbitPeriodTicks)
                || Double.isInfinite(orbitPeriodTicks)) {
            return 0D;
        }
        return 2D * Math.PI * (worldTicks / orbitPeriodTicks);
    }

    private static double getOrbitalPeriodTicks(CelestialBody body) {
        if (body.parent == null || body.semiMajorAxisKm <= 0F) return 0D;

        double orbitPeriodDays = body.getOrbitalPeriod();
        if (orbitPeriodDays <= 0D
                || Double.isNaN(orbitPeriodDays)
                || Double.isInfinite(orbitPeriodDays)) return 0D;

        return orbitPeriodDays * (double) AstronomyUtil.TICKS_IN_DAY;
    }

    private static double calculateEccentricAnomaly(double meanAnomaly, float eccentricity) {
        double eccentricAnomaly = meanAnomaly;
        for (int i = 0; i < 4; i++)
            eccentricAnomaly = meanAnomaly + eccentricity * Math.sin(eccentricAnomaly);
        return eccentricAnomaly;
    }

    private float getBodySizePxAtZoom(CelestialBody body) {
        float bodySize = StarmapScene.bodySizePxAt1x(body) * mapZoom;
        if (getRenderFocusBody() != null) {
            CelestialBody localOrbitParent = getLocalOrbitParent();
            boolean isLocalMoon = localOrbitParent != null && body.parent == localOrbitParent;
            if (isLocalMoon) bodySize = StarmapScene.moonSizePxAt1x(body) * mapZoom;
        }
        return bodySize;
    }

    private void enterLandingMode(CelestialBody body) {
        landingMode = true;
        landingBody = body;
        surfaceX = 0;
        surfaceY = 0;

        playClick();

        if (body.dimension == null) return;
        boolean here = body == currentBody && !CelestialBody.inOrbit(star.getLevel());
        PacketWire.sendToServer(
                SpaceStardarTarget.encode(menu.containerId, body.dimension.identifier(), here));
    }

    private void exitLandingMode() {
        landingMode = false;
        landingBody = null;
        draggingSurface = false;
    }

    private void updateLandingModeFromDrive() {
        ItemStack slotStack = menu.getSlot(0).getItem();

        if (slotStack.isEmpty()) {
            hadFullDrive = false;
            lastFullDriveBody = null;
            if (landingMode) exitLandingMode();
            return;
        }

        if (!(slotStack.getItem() instanceof VOTVdriveItem)) {
            hadFullDrive = false;
            lastFullDriveBody = null;
            if (landingMode && !slotStack.is(MissilePartsSpace.HARD_DRIVE.get())) exitLandingMode();
            return;
        }

        SolarSystem.Body type = VOTVdriveItem.getBody(slotStack);
        if (hadFullDrive && type == lastFullDriveBody) return;

        hadFullDrive = true;
        lastFullDriveBody = type;

        CelestialBody body = type.getBody();
        if (landingMode && landingBody == body) return;

        if (body != null && body.canLand) {
            enterLandingMode(body);
        } else if (landingMode) {
            exitLandingMode();
        }
    }

    private static int altitude(int[] heightmap, int x, int z) {
        if (x < 0 || x > 255 || z < 0 || z > 255) return -1;
        return heightmap[StardarBlockEntity.MAP_SIZE * z + x];
    }

    private static Component getBodyDisplayName(@Nullable CelestialBody body) {
        if (body == null || body.name == null)
            return Component.translatable("gui.stardar.body.unknown");
        String key = "body." + body.name;
        if (Language.getInstance().has(key)) return Component.translatable(key);
        if (body.name.length() <= 1) return Component.literal(body.name.toUpperCase(Locale.ROOT));
        return Component.literal(
                body.name.substring(0, 1).toUpperCase(Locale.ROOT) + body.name.substring(1));
    }

    private static int colorArrayToRgb(float @Nullable [] color) {
        if (color == null || color.length < 3) return 0xFFFFFFFF;
        int r = Mth.clamp((int) (color[0] * 255F), 0, 255);
        int g = Mth.clamp((int) (color[1] * 255F), 0, 255);
        int b = Mth.clamp((int) (color[2] * 255F), 0, 255);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int getBodyHierarchyDepth(CelestialBody body) {
        int depth = 0;
        CelestialBody current = body;
        while (current.parent != null && depth < 64) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    private static final class BodyPosition {
        float mapU;
        float mapV;
    }

    private record ChildRenderState(
            CelestialBody childBody,
            float orbitScalePxPerKm,
            float orbitOffsetU,
            float orbitOffsetV,
            boolean moonChild,
            boolean drawInFrontOfParent) {}

    private record BodyRenderInfo(CelestialBody body, float mapU, float mapV, float drawSize) {}

    private record SatelliteRenderInfo(
            int frequency, SatelliteOrbit orbit, float mapU, float mapV, float drawSize) {}

    private record BodyLabelRenderInfo(
            CelestialBody body, float topRightAnchorScreenX, float topRightAnchorScreenY) {}
}
