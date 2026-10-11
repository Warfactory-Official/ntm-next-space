// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client;

import com.hbm.render.util.Vertices;
import com.hbmspace.handler.atmosphere.SpaceLivingProps;
import com.hbmspace.items.armor.ArmorUtilSpace;
import com.hbmspace.items.armor.ItemModOxy;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public final class SpaceOxygenHud {

    private static final Identifier AIR = Identifier.withDefaultNamespace("hud/air");
    private static final Identifier AIR_POPPING =
            Identifier.withDefaultNamespace("hud/air_bursting");

    private SpaceOxygenHud() {}

    public static int vanillaAirTop(Player player, int guiHeight) {
        int hearts = 0;
        if (player.getVehicle() instanceof LivingEntity vehicle && vehicle.showVehicleHealth()) {
            hearts = Math.min((int) (vehicle.getMaxHealth() + 0.5F) / 2, 30);
        }
        int rows = (int) Math.ceil(hearts / 10.0);
        return guiHeight - 49 - Math.max(0, rows - 1) * 10;
    }

    public static boolean replacesAir(Player player) {
        return SpaceLivingProps.getOxy(player) < SpaceLivingProps.FULL_BREATH
                || !ArmorUtilSpace.getOxygenTank(player).isEmpty();
    }

    public static int render(GuiGraphicsExtractor graphics, Player player, int right, int top) {
        int taken = 0;
        int air = SpaceLivingProps.getOxy(player);
        if (air < SpaceLivingProps.FULL_BREATH) {
            int full = Mth.ceil((double) (air - 2) * 10.0D / 100.0D);
            int partial = Mth.ceil((double) air * 10.0D / 100.0D) - full;
            for (int i = 0; i < full + partial; ++i) {
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        i < full ? AIR : AIR_POPPING,
                        right - i * 8 - 9,
                        top,
                        9,
                        9);
            }
            taken += 10;
        }

        ItemStack tank = ArmorUtilSpace.getOxygenTank(player);
        if (!tank.isEmpty() && tank.getItem() instanceof ItemModOxy oxy) {
            float fill = (float) oxy.getFuel(tank) / (float) oxy.getMaxFuel();
            float y = top - taken + 3;
            Matrix3x2f pose = new Matrix3x2f(graphics.pose());
            graphics.guiRenderState.addGuiElement(
                    new Gauge(
                            pose,
                            right,
                            y,
                            fill,
                            new ScreenRectangle(right - 82, (int) y - 1, 83, 6)
                                    .transformMaxBounds(pose)));
            taken += 6;
        }
        return taken;
    }

    private record Gauge(
            Matrix3x2fc pose, float right, float top, float fill, ScreenRectangle bounds)
            implements GuiElementRenderState {

        @Override
        public void buildVertices(VertexConsumer buffer) {
            int back = rgb(0.25F, 0.25F, 0.25F);
            Vertices.emit(buffer, pose, right - 81.5F, top - 0.5F, back);
            Vertices.emit(buffer, pose, right - 81.5F, top + 4.5F, back);
            Vertices.emit(buffer, pose, right + 0.5F, top + 4.5F, back);
            Vertices.emit(buffer, pose, right + 0.5F, top - 0.5F, back);
            int bar = rgb(1F - fill, fill, fill);
            Vertices.emit(buffer, pose, right - 81 * fill, top, bar);
            Vertices.emit(buffer, pose, right - 81 * fill, top + 4, bar);
            Vertices.emit(buffer, pose, right, top + 4, bar);
            Vertices.emit(buffer, pose, right, top, bar);
        }

        private static int rgb(float r, float g, float b) {
            return 0xFF000000 | (int) (r * 255F) << 16 | (int) (g * 255F) << 8 | (int) (b * 255F);
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return null;
        }
    }
}
