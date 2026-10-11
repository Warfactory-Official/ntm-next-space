// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render.mob;

import com.google.common.base.Suppliers;
import com.hbm.client.ClientRegistry;
import com.hbm.client.VanishedEntities;
import com.hbm.client.model.Meshes;
import com.hbm.client.render.RenderBulletMK4;
import com.hbm.client.render.RenderCreeperUniversal;
import com.hbm.client.render.flywheel.FlywheelResources;
import com.hbm.client.render.flywheel.MobObjVisual;
import com.hbm.client.render.flywheel.MobVisuals;
import com.hbm.client.render.flywheel.ObjEntityVisual;
import com.hbm.items.weapon.sedna.factory.LegoClient;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbmspace.HbmSpace;
import com.hbmspace.entity.MobEntitiesSpace;
import com.hbmspace.entity.mob.EntityDepthSquid;
import com.hbmspace.entity.mob.EntityScrapFish;
import com.hbmspace.entity.mob.EntityScutterfish;
import com.hbmspace.entity.mob.EntityScuttlecrab;
import com.hbmspace.entity.mob.EntitySifterEel;
import com.hbmspace.entity.mob.EntityTankbot;
import com.hbmspace.entity.mob.EntityWarBehemoth;
import com.hbmspace.entity.mob.MobWeaponsSpace;
import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public final class MobRenders {
    private static final double[] EEL_PHASE = {0.2D, 0.4D, 0.6D, 0.9D, 1.2D, 1.5D, 1.9D};
    private static final double[] EEL_YAW = {10.0D, -2.0D, -2.0D, -6.0D, -4.0D, -6.0D, -5.0D};
    private static final double[] SCRAP_PHASE = {0.6D, 0.8D, 1.0D};
    private static final double[] SCRAP_YAW = {10.0D, 6.0D, 4.0D};

    private static final ObjMobRig<EntityScutterfish> SCUTTERFISH = scutterfish();
    private static final ObjMobRig<EntityScrapFish> SCRAP_FISH = scrapFish();
    private static final ObjMobRig<EntitySifterEel> SIFTER_EEL = sifterEel();
    private static final ObjMobRig<EntityScuttlecrab> SCUTTLECRAB = scuttlecrab();
    private static final ObjMobRig<EntityDepthSquid> DEPTH_SQUID = depthSquid();
    private static final ObjMobRig<EntityTankbot> TANKBOT = tankbot();
    private static final ObjMobRig<EntityWarBehemoth> WAR_BEHEMOTH = warBehemoth();

    private MobRenders() {}

    public static void registerEntityRenderers(ClientRegistry.EntityRendererRegistrar reg) {
        reg.accept(
                MobEntitiesSpace.CREEPER_FLESH.get(),
                context ->
                        new RenderCreeperUniversal(
                                context, space("textures/entity/creeper_flesh.png"), 1.0F));
        reg.accept(MobEntitiesSpace.MOON_COW.get(), RenderMoonCow::new);
        reg.accept(
                MobEntitiesSpace.SCUTTERFISH.get(),
                context -> new RenderObjMob<>(context, SCUTTERFISH));
        reg.accept(
                MobEntitiesSpace.SCRAP_FISH.get(),
                context -> new RenderObjMob<>(context, SCRAP_FISH));
        reg.accept(
                MobEntitiesSpace.SIFTER_EEL.get(),
                context -> new RenderObjMob<>(context, SIFTER_EEL));
        reg.accept(
                MobEntitiesSpace.SCUTTLECRAB.get(),
                context -> new RenderObjMob<>(context, SCUTTLECRAB));
        reg.accept(
                MobEntitiesSpace.DEPTH_SQUID.get(),
                context -> new RenderObjMob<>(context, DEPTH_SQUID));
        reg.accept(MobEntitiesSpace.TANKBOT.get(), context -> new RenderObjMob<>(context, TANKBOT));
        reg.accept(
                MobEntitiesSpace.WAR_BEHEMOTH.get(),
                context -> new RenderObjMob<>(context, WAR_BEHEMOTH));
    }

    public static void registerBulletDrawers() {
        RenderBulletMK4.setRenderer(MobWeaponsSpace.R762_AP_STILL, LegoClient.RENDER_AP_BULLET);
    }

    public static void registerVisualizers() {
        MobVisuals.creeper(
                MobEntitiesSpace.CREEPER_FLESH.get(),
                () -> space("textures/entity/creeper_flesh.png"),
                1.0F);
        visualize(MobEntitiesSpace.SCUTTERFISH.get(), SCUTTERFISH);
        visualize(MobEntitiesSpace.SCRAP_FISH.get(), SCRAP_FISH);
        visualize(MobEntitiesSpace.SIFTER_EEL.get(), SIFTER_EEL);
        visualize(MobEntitiesSpace.SCUTTLECRAB.get(), SCUTTLECRAB);
        visualize(MobEntitiesSpace.DEPTH_SQUID.get(), DEPTH_SQUID);
        visualize(MobEntitiesSpace.TANKBOT.get(), TANKBOT);
        visualize(MobEntitiesSpace.WAR_BEHEMOTH.get(), WAR_BEHEMOTH);
    }

    private static <T extends LivingEntity> void visualize(EntityType<T> type, ObjMobRig<T> rig) {
        Predicate<T> drawn = entity -> !VanishedEntities.isVanished(entity);
        List<MobObjVisual.Prepared<T>> prepared = new ArrayList<>();
        FlywheelResources.onInitialize(
                () -> {
                    for (int i = 0; i < rig.skins().size(); i++)
                        prepared.add(MobObjVisual.prepare(rig.spec(i, drawn)));
                });
        SimpleEntityVisualizer.builder(type)
                .factory(
                        (ctx, entity, partialTick) ->
                                new MobObjVisual<>(
                                        ctx,
                                        entity,
                                        partialTick,
                                        prepared.get(rig.skin().applyAsInt(entity))))
                .skipVanillaRender(drawn)
                .apply();
    }

    private static Identifier space(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    private static Supplier<HFRWavefrontObject> mesh(String name) {
        return Suppliers.memoize(() -> Meshes.load(space("models/mobs/" + name + ".obj")));
    }

    private static <E extends Enum<E>> List<Identifier> skins(Class<E> type) {
        List<Identifier> skins = new ArrayList<>();
        for (E constant : type.getEnumConstants()) {
            skins.add(
                    space(
                            "textures/entity/"
                                    + type.getSimpleName().toLowerCase(Locale.ROOT)
                                    + "_"
                                    + constant.name().toLowerCase(Locale.ROOT)
                                    + ".png"));
        }
        return List.copyOf(skins);
    }

    private static float rad(double degrees) {
        return (float) Math.toRadians(degrees);
    }

    private static float bodyYaw(LivingEntity entity, float partialTick) {
        return Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
    }

    private static void corpse(Matrix4f pose, LivingEntity entity, float partialTick) {
        pose.rotateY((180.0F - bodyYaw(entity, partialTick)) * Mth.DEG_TO_RAD);
        if (entity.deathTime > 0) {
            float fall = Mth.sqrt((entity.deathTime + partialTick - 1.0F) / 20.0F * 1.6F);
            pose.rotateZ(Math.min(fall, 1.0F) * 90.0F * Mth.DEG_TO_RAD);
        }
    }

    private static <T extends LivingEntity> ObjEntityVisual.Pose<T> upright() {
        return (pose, entity, partialTick) -> {
            corpse(pose, entity, partialTick);
            pose.translate(0.0F, 0.0078125F, 0.0F);
        };
    }

    private static void turned(Matrix4f pose, LivingEntity entity, float partialTick) {
        corpse(pose, entity, partialTick);
        pose.rotateY((float) Math.PI);
        pose.translate(0.0F, 0.0078125F, 0.0F);
    }

    private static double cycle(LivingEntity entity, float partialTick, float rate, double phase) {
        float limb = entity.walkAnimation.position(partialTick) * rate;
        return Math.sin(limb % (Math.PI * 2.0D) - Math.PI * phase);
    }

    private static <T extends LivingEntity> MobObjVisual.Part<T> head(
            String group, float x, float y, float z) {
        return MobObjVisual.Part.at(
                group,
                (pose, entity, partialTick) -> {
                    float netHeadYaw =
                            Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot)
                                    - bodyYaw(entity, partialTick);
                    pose.translate(x, y, z);
                    pose.rotateX(entity.getXRot(partialTick) * Mth.DEG_TO_RAD);
                    pose.rotateY(netHeadYaw * Mth.DEG_TO_RAD);
                    pose.translate(-x, -y, -z);
                });
    }

    private static <T extends LivingEntity> MobObjVisual.Part<T> rollZ(
            String group, float rate, double phase, double degrees) {
        return MobObjVisual.Part.at(
                group,
                (pose, entity, partialTick) ->
                        pose.rotateZ(rad(cycle(entity, partialTick, rate, phase) * degrees)));
    }

    private static <T extends LivingEntity> MobObjVisual.Part<T> pitchX(
            String group, double phase, double degrees) {
        return MobObjVisual.Part.at(
                group,
                (pose, entity, partialTick) ->
                        pose.rotateX(rad(cycle(entity, partialTick, 1.0F, phase) * degrees)));
    }

    private static <T extends LivingEntity> MobObjVisual.Part<T> yawChain(
            String group, double[] phase, double[] degrees, int links) {
        return MobObjVisual.Part.at(
                group,
                (pose, entity, partialTick) -> {
                    for (int i = 0; i < links; i++)
                        pose.rotateY(rad(cycle(entity, partialTick, 1.0F, phase[i]) * degrees[i]));
                });
    }

    private static ObjMobRig<EntityScutterfish> scutterfish() {
        ObjEntityVisual.Pose<EntityScutterfish> tail =
                (pose, entity, partialTick) -> {
                    pose.rotateZ(rad(cycle(entity, partialTick, 1.0F, 0.2D) * 10.0D));
                    pose.rotateX(rad(cycle(entity, partialTick, 1.0F, 0.4D) * -5.0D));
                };
        List<MobObjVisual.Part<EntityScutterfish>> parts =
                List.of(
                        MobObjVisual.Part.of("body"),
                        head("head", 0.0F, 0.0F, 0.0F),
                        rollZ("leftfin1", 1.0F, 0.0D, 20.0D),
                        rollZ("rightfin1", 1.0F, 0.0D, -20.0D),
                        rollZ("leftfin2", 1.0F, 0.2D, 20.0D),
                        rollZ("rightfin2", 1.0F, 0.2D, -20.0D),
                        rollZ("leftfin3", 1.0F, 0.4D, 20.0D),
                        rollZ("rightfin3", 1.0F, 0.4D, -20.0D),
                        MobObjVisual.Part.at("tailmeat", tail),
                        MobObjVisual.Part.at(
                                "tailthin",
                                (pose, entity, partialTick) -> {
                                    tail.apply(pose, entity, partialTick);
                                    pose.rotateX(
                                            rad(cycle(entity, partialTick, 1.0F, 0.6D) * -5.0D));
                                }));
        return new ObjMobRig<>(
                mesh("scutterfish"),
                List.of(space("textures/entity/scutterfish.png")),
                entity -> 0,
                Map.of(),
                upright(),
                parts,
                0.3F);
    }

    private static ObjMobRig<EntityScrapFish> scrapFish() {
        List<MobObjVisual.Part<EntityScrapFish>> parts =
                List.of(
                        MobObjVisual.Part.of("Body"),
                        head("Head", 0.0F, 0.0F, -0.3125F),
                        pitchX("FinL", 0.0D, 20.0D),
                        pitchX("FinR", 0.0D, -20.0D),
                        pitchX("VentralFL", 0.2D, 20.0D),
                        pitchX("VentralFR", 0.2D, -20.0D),
                        pitchX("VentralBL", 0.4D, 20.0D),
                        pitchX("VentralBR", 0.4D, -20.0D),
                        yawChain("Dorsal", SCRAP_PHASE, SCRAP_YAW, 1),
                        yawChain("Tail", SCRAP_PHASE, SCRAP_YAW, 2),
                        yawChain("TailFin", SCRAP_PHASE, SCRAP_YAW, 3));
        return new ObjMobRig<>(
                mesh("scrapfish"),
                skins(EntityScrapFish.ScrapFish.class),
                entity -> entity.getEnum().ordinal(),
                Map.of(),
                upright(),
                parts,
                0.5F);
    }

    private static ObjMobRig<EntitySifterEel> sifterEel() {
        List<MobObjVisual.Part<EntitySifterEel>> parts =
                List.of(
                        head("Head", 0.0F, 0.0F, -0.5F),
                        head("Jaw", 0.0F, 0.0F, -0.5F),
                        pitchX("FinL", 0.0D, 20.0D),
                        pitchX("FinR", 0.0D, -20.0D),
                        MobObjVisual.Part.of("Body1"),
                        yawChain("Dorsal1", EEL_PHASE, EEL_YAW, 2),
                        yawChain("Body2", EEL_PHASE, EEL_YAW, 2),
                        yawChain("Dorsal2", EEL_PHASE, EEL_YAW, 3),
                        yawChain("Dorsal3", EEL_PHASE, EEL_YAW, 3),
                        yawChain("Body3", EEL_PHASE, EEL_YAW, 4),
                        yawChain("Ventral1", EEL_PHASE, EEL_YAW, 4),
                        yawChain("Ventral2", EEL_PHASE, EEL_YAW, 4),
                        yawChain("Ventral3", EEL_PHASE, EEL_YAW, 4),
                        yawChain("Dorsal4", EEL_PHASE, EEL_YAW, 5),
                        yawChain("Ventral4", EEL_PHASE, EEL_YAW, 5),
                        yawChain("Body4", EEL_PHASE, EEL_YAW, 5),
                        yawChain("Body5", EEL_PHASE, EEL_YAW, 6),
                        yawChain("Body6", EEL_PHASE, EEL_YAW, 6),
                        yawChain("Tail", EEL_PHASE, EEL_YAW, 7));
        return new ObjMobRig<>(
                mesh("siftereel"),
                skins(EntitySifterEel.SifterEel.class),
                entity -> entity.getEnum().ordinal(),
                Map.of(),
                upright(),
                parts,
                0.5F);
    }

    private static ObjMobRig<EntityScuttlecrab> scuttlecrab() {
        List<MobObjVisual.Part<EntityScuttlecrab>> parts =
                List.of(
                        MobObjVisual.Part.of("Body"),
                        head("EyeL", -0.125F, 0.5625F, -0.3125F),
                        head("EyeR", 0.125F, 0.5625F, -0.3125F),
                        MobObjVisual.Part.of("ClawL"),
                        MobObjVisual.Part.of("ClawR"),
                        rollZ("LegL1", 6.0F, 0.0D, 8.0D),
                        rollZ("LegL2", 6.0F, 0.4D, 8.0D),
                        rollZ("LegL3", 6.0F, 0.8D, 8.0D),
                        rollZ("LegL4", 6.0F, 1.2D, 8.0D),
                        rollZ("LegR1", 6.0F, 0.4D, -8.0D),
                        rollZ("LegR2", 6.0F, 0.8D, -8.0D),
                        rollZ("LegR3", 6.0F, 1.2D, -8.0D),
                        rollZ("LegR4", 6.0F, 1.6D, -8.0D));
        return new ObjMobRig<>(
                mesh("scuttlecrab"),
                skins(EntityScuttlecrab.Scuttlecrab.class),
                entity -> entity.getEnum().ordinal(),
                Map.of(),
                upright(),
                parts,
                0.5F);
    }

    private static ObjMobRig<EntityDepthSquid> depthSquid() {
        List<MobObjVisual.Part<EntityDepthSquid>> parts = new ArrayList<>();
        parts.add(MobObjVisual.Part.of("Body"));
        parts.add(
                MobObjVisual.Part.at(
                        "FinL",
                        (pose, entity, partialTick) -> {
                            pose.rotateX(rad(cycle(entity, partialTick, 1.0F, 0.4D) * 20.0D));
                            pose.rotateY(rad(cycle(entity, partialTick, 1.0F, 0.0D) * 10.0D));
                        }));
        parts.add(
                MobObjVisual.Part.at(
                        "FinR",
                        (pose, entity, partialTick) -> {
                            pose.rotateX(rad(cycle(entity, partialTick, 1.0F, 0.4D) * 20.0D));
                            pose.rotateY(rad(cycle(entity, partialTick, 1.0F, 0.0D) * -10.0D));
                        }));
        for (int i = 0; i < 8; i++) {
            float yaw = i * -45.0F * Mth.DEG_TO_RAD;
            parts.add(
                    MobObjVisual.Part.at(
                            "T0",
                            (pose, entity, partialTick) -> {
                                pose.translate(0.0F, -0.9F, 0.0F);
                                pose.rotateY(yaw);
                                pose.rotateX(
                                        Mth.lerp(
                                                partialTick,
                                                entity.oldTentacleAngle,
                                                entity.tentacleAngle));
                                pose.translate(0.0F, 0.9F, 0.0F);
                            }));
        }
        ObjEntityVisual.Pose<EntityDepthSquid> body =
                (pose, entity, partialTick) -> {
                    corpse(pose, entity, partialTick);
                    pose.scale(-1.0F, -1.0F, 1.0F);
                    pose.translate(0.0F, -1.5078125F + 1.0F, 0.0F);
                    pose.rotateX(
                            Mth.lerp(partialTick, entity.xBodyRotO, entity.xBodyRot)
                                    * Mth.DEG_TO_RAD);
                };
        return new ObjMobRig<>(
                mesh("depthsquid"),
                skins(EntityDepthSquid.DepthSquid.class),
                entity -> entity.getEnum().ordinal(),
                Map.of(),
                body,
                List.copyOf(parts),
                0.5F);
    }

    private static ObjMobRig<EntityTankbot> tankbot() {
        ObjEntityVisual.Pose<EntityTankbot> base =
                (pose, entity, partialTick) -> {
                    turned(pose, entity, partialTick);
                    pose.scale(2.0F);
                };
        ObjEntityVisual.Pose<EntityTankbot> front2 =
                (pose, entity, partialTick) -> {
                    pose.rotateY(rad(10.0D - cycle(entity, partialTick, 1.0F, 0.75D) * 10.0D));
                    pose.rotateZ(rad(cycle(entity, partialTick, 1.0F, 1.0D) * -2.0D));
                };
        ObjEntityVisual.Pose<EntityTankbot> front1 =
                (pose, entity, partialTick) -> {
                    double cy2 = cycle(entity, partialTick, 1.0F, 1.0D);
                    double cy3 = cycle(entity, partialTick, 1.0F, 0.75D);
                    pose.rotateY(rad(cy3 - cy2 * 10.0D));
                    pose.rotateZ(rad(cy2 - cy3 * -2.0D));
                };
        ObjEntityVisual.Pose<EntityTankbot> back2 =
                (pose, entity, partialTick) -> {
                    double cy2 = cycle(entity, partialTick, 1.0F, 1.0D);
                    pose.rotateY(rad(cy2 - cycle(entity, partialTick, 1.0F, 0.75D) * 5.0D));
                    pose.rotateX(rad(cy2 * 2.0D));
                };
        ObjEntityVisual.Pose<EntityTankbot> turret =
                (pose, entity, partialTick) ->
                        pose.rotateY(
                                (Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot)
                                                - entity.headTargetYaw(partialTick)
                                                - 90.0F)
                                        * Mth.DEG_TO_RAD);
        List<MobObjVisual.Part<EntityTankbot>> parts = new ArrayList<>();
        parts.add(MobObjVisual.Part.of("body"));
        tankLegs(parts, "front2", 0.0625F, front2);
        tankLegs(parts, "front1", 0.4375F, front1);
        tankLegs(parts, "back1", 0.9375F, front1);
        tankLegs(parts, "back2", 0.9375F, back2);
        parts.add(MobObjVisual.Part.at("head", turret));
        parts.add(MobObjVisual.Part.at("barrelbottom", turret));
        parts.add(MobObjVisual.Part.at("barreltop", turret));
        return new ObjMobRig<>(
                mesh("tankbot"),
                List.of(space("textures/entity/robtex.png")),
                entity -> 0,
                Map.of(),
                base,
                List.copyOf(parts),
                0.0F);
    }

    private static void tankLegs(
            List<MobObjVisual.Part<EntityTankbot>> parts,
            String leg,
            float z,
            ObjEntityVisual.Pose<EntityTankbot> swing) {
        for (String joint : List.of("foot", "leg", "knee")) {
            parts.add(
                    MobObjVisual.Part.at(
                            leg + joint,
                            (pose, entity, partialTick) -> {
                                pose.translate(0.25F, 0.625F, z);
                                swing.apply(pose, entity, partialTick);
                                pose.translate(-0.25F, -0.625F, -z);
                            }));
        }
    }

    private static ObjMobRig<EntityWarBehemoth> warBehemoth() {
        ObjEntityVisual.Pose<EntityWarBehemoth> base =
                (pose, entity, partialTick) -> {
                    turned(pose, entity, partialTick);
                    pose.scale(1.2F);
                    pose.translate(0.0F, -0.5F, 0.0F);
                    pose.rotateY(-(float) Math.PI);
                };
        ObjEntityVisual.Pose<EntityWarBehemoth> body =
                (pose, entity, partialTick) -> {
                    pose.translate(0.0F, 8.6F, 0.5F);
                    pose.rotateX(-entity.aimPitch(partialTick) * Mth.DEG_TO_RAD);
                    pose.translate(0.0F, -8.7F, -0.5F);
                };
        List<MobObjVisual.Part<EntityWarBehemoth>> parts = new ArrayList<>();
        for (String group : List.of("body", "hatch1", "hatch2", "gun", "helmet", "eye")) {
            parts.add(MobObjVisual.Part.at(group, body));
        }
        for (boolean right : new boolean[] {true, false}) {
            String side = right ? "_right" : "_left";
            parts.add(
                    MobObjVisual.Part.at(
                            "hip" + side,
                            (pose, entity, partialTick) ->
                                    behemothLeg(pose, entity, partialTick, right, 0)));
            parts.add(
                    MobObjVisual.Part.at(
                            "knee" + side,
                            (pose, entity, partialTick) ->
                                    behemothLeg(pose, entity, partialTick, right, 1)));
            parts.add(
                    MobObjVisual.Part.at(
                            "leg" + side,
                            (pose, entity, partialTick) ->
                                    behemothLeg(pose, entity, partialTick, right, 2)));
        }
        Identifier hip = space("textures/entity/wm_hip.png");
        Identifier knee = space("textures/entity/wm_knee.png");
        Identifier leg = space("textures/entity/wm_leg.png");
        Map<String, Identifier> sheets =
                Map.of(
                        "helmet",
                        space("textures/entity/wm_helmet.png"),
                        "eye",
                        space("textures/entity/wm_eye.png"),
                        "hip_right",
                        hip,
                        "hip_left",
                        hip,
                        "knee_right",
                        knee,
                        "knee_left",
                        knee,
                        "leg_right",
                        leg,
                        "leg_left",
                        leg);
        return new ObjMobRig<>(
                mesh("war_machine"),
                List.of(space("textures/entity/wm_body.png")),
                entity -> 0,
                sheets,
                base,
                List.copyOf(parts),
                0.0F);
    }

    private static void behemothLeg(
            Matrix4f pose, EntityWarBehemoth entity, float partialTick, boolean right, int joint) {
        double step = entity.walkAnimation.position(partialTick) * 0.25D + (right ? Math.PI : 0.0D);
        double footX = Math.sin(step) * 2.5D + 5.0D;
        double footY = Math.max(Math.sin(step) * 5.5D - (right ? 4.0D : 2.0D), 7.0D);
        double footZ = Math.cos(step) * 2.5D + 1.5D;
        double dy = footY - 8.5D;
        double dz = footZ - 1.5D;
        double reach = footX * footX + dy * dy + dz * dz;
        double cosKnee = (6.0D * 6.0D + 8.0D * 8.0D - reach) / (2.0D * 6.0D * 8.0D);
        double knee = Math.acos(cosKnee);
        double hip =
                Math.atan2(dy, footX)
                        - Math.atan2(8.0D * Math.sin(knee), 6.0D + 8.0D * Math.cos(knee));
        pose.translate(0.0F, 8.5F, 0.5F);
        pose.rotateX((float) hip);
        if (joint == 0) {
            pose.translate(0.0F, -8.6F, -0.5F);
            return;
        }
        pose.translate(0.0F, -1.8F, 0.0F);
        pose.rotateX((float) knee);
        pose.translate(0.0F, -6.7F, -0.5F);
        if (joint == 1) return;
        pose.translate(0.0F, 6.7F, 0.5F);
        pose.translate(0.0F, -5.6F, 0.0F);
        pose.rotateX((float) (cosKnee * -0.5D));
        pose.translate(0.0F, -1.0F, -0.5F);
    }
}
