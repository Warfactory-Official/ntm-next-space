// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.hbm.client.ClientRegistry;
import com.hbm.client.VanishedEntities;
import com.hbm.client.render.RenderBeam;
import com.hbm.client.render.SocketCentrepieces;
import com.hbm.client.render.flywheel.FlywheelResources;
import com.hbm.client.render.flywheel.MobObjVisual;
import com.hbm.items.weapon.sedna.factory.LegoClient;
import com.hbmspace.entity.MobsSpace;
import com.hbmspace.entity.mob.MobWeaponsSpace;
import com.hbmspace.entity.mob.siege.EntitySiegeUFO;
import com.hbmspace.items.MobItemsSpace;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.resources.Identifier;

public final class SiegeRenders {

    private SiegeRenders() {}

    public static void registerEntityRenderers(ClientRegistry.EntityRendererRegistrar reg) {
        reg.accept(MobsSpace.SIEGE_UFO.get(), RenderSiegeUFO::new);
        reg.accept(MobsSpace.SIEGE_CRAFT.get(), RenderSiegeCraft::new);
        reg.accept(MobsSpace.BF_ANGEL.get(), RenderBFAngel::new);
    }

    public static void registerVisualizers() {
        Predicate<EntitySiegeUFO> ufoDrawn = entity -> !VanishedEntities.isVanished(entity);
        List<MobObjVisual.Prepared<EntitySiegeUFO>> ufo = new ArrayList<>(1);
        FlywheelResources.onInitialize(
                () -> ufo.add(MobObjVisual.prepare(ufoSpec().instanced(ufoDrawn))));
        SimpleEntityVisualizer.builder(MobsSpace.SIEGE_UFO.get())
                .factory(
                        (ctx, entity, partialTick) ->
                                new MobObjVisual<>(ctx, entity, partialTick, ufo.getFirst()))
                .skipVanillaRender(ufoDrawn)
                .apply();
        SimpleEntityVisualizer.builder(MobsSpace.SIEGE_CRAFT.get())
                .factory(SiegeCraftVisual::new)
                .skipVanillaRender(entity -> !VanishedEntities.isVanished(entity))
                .apply();
        SimpleEntityVisualizer.builder(MobsSpace.BF_ANGEL.get())
                .factory(BFAngelVisual::new)
                .skipVanillaRender(entity -> !VanishedEntities.isVanished(entity))
                .apply();
    }

    public static void registerRenderPipelines(Consumer<RenderPipeline> registrar) {
        RenderBFAngel.forEachPipeline(registrar);
    }

    public static void registerBulletDrawers() {
        RenderBeam.setRenderer(
                MobWeaponsSpace.EMERALD_OVERCHARGE_STILL, LegoClient.RENDER_LASER_EMERALD);
        RenderBeam.setRenderer(MobWeaponsSpace.TESLA_OVERCHARGE_STILL, LegoClient.RENDER_LIGHTNING);
    }

    public static void registerSocketCentrepieces() {
        SocketCentrepieces.register(
                MobItemsSpace.CORE_ANGEL.get(),
                new SocketCentrepieces.Centrepiece(
                        SiegeModels.ANGEL_SMOOTH, "body", SiegeModels.ANGEL_TEX, 2F, 5F));
    }

    private static MobObjVisual.Spec<EntitySiegeUFO> ufoSpec() {
        List<MobObjVisual.Part<EntitySiegeUFO>> parts = new ArrayList<>();
        for (int i = 0; i < SiegeModels.UFO_SKINS.size(); i++) {
            int tier = i;
            Identifier skin = SiegeModels.UFO_SKINS.get(i);
            parts.add(
                    MobObjVisual.Part.<EntitySiegeUFO>when(
                                    "Circle", entity -> entity.getTier().id == tier)
                            .material(() -> hull(skin)));
        }
        return new MobObjVisual.Spec<>(
                SiegeModels.MINI_UFO,
                () -> hull(SiegeModels.UFO_SKINS.getFirst()),
                List.copyOf(parts),
                RenderSiegeUFO::pose,
                0F,
                1F,
                entity -> true);
    }

    private static Material hull(Identifier skin) {
        return SimpleMaterial.builderOf(Materials.CUTOUT)
                .texture(skin)
                .mipmap(false)
                .cutout(CutoutShaders.ONE_TENTH)
                .cardinalLightingMode(CardinalLightingMode.ENTITY)
                .backfaceCulling(false)
                .useOverlay(false)
                .build();
    }
}
