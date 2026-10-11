// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.handler;

import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.items.weapon.MissileKindsSpace;
import com.hbmspace.items.weapon.MissilePartsSpace;
import com.hbmspace.saveddata.satellites.SatelliteOrbit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class RocketStruct {

    public static final int MAX_STAGES = 5;

    public static final Codec<RocketStruct> CODEC =
            RecordCodecBuilder.create(
                    i ->
                            i.group(
                                            RocketPart.CODEC
                                                    .optionalFieldOf("capsule")
                                                    .forGetter(r -> Optional.ofNullable(r.capsule)),
                                            RocketStage.CODEC
                                                    .listOf()
                                                    .fieldOf("stages")
                                                    .forGetter(r -> r.stages))
                                    .apply(
                                            i,
                                            (capsule, stages) ->
                                                    new RocketStruct(
                                                            capsule.orElse(null),
                                                            stages,
                                                            List.of())));

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketStruct> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(RocketPart.STREAM_CODEC),
                    r -> Optional.ofNullable(r.capsule),
                    RocketStage.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_STAGES)),
                    r -> r.stages,
                    ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    r -> r.extraIssues,
                    (capsule, stages, issues) ->
                            new RocketStruct(capsule.orElse(null), stages, issues));

    public @Nullable RocketPart capsule;
    public final List<RocketStage> stages = new ArrayList<>();
    public int satFreq = 0;
    public @Nullable SatelliteOrbit satOrbit;
    public final List<Component> extraIssues = new ArrayList<>();

    public RocketStruct() {}

    public RocketStruct(ItemStack capsule) {
        this.capsule = RocketPart.of(capsule);
    }

    private RocketStruct(
            @Nullable RocketPart capsule, List<RocketStage> stages, List<Component> issues) {
        this.capsule = capsule;
        this.stages.addAll(stages);
        this.extraIssues.addAll(issues);
    }

    public RocketStruct copy() {
        RocketStruct copy = new RocketStruct(capsule, stages, extraIssues);
        copy.satFreq = satFreq;
        copy.satOrbit = satOrbit;
        return copy;
    }

    public void addStage(ItemStack fuselage, ItemStack fins, ItemStack thruster) {
        addStage(
                RocketPart.of(fuselage),
                RocketPart.of(fins),
                RocketPart.of(thruster),
                fuselage.isEmpty() ? 1 : fuselage.getCount(),
                thruster.isEmpty() ? 1 : thruster.getCount());
    }

    public void addStage(
            @Nullable RocketPart fuselage,
            @Nullable RocketPart fins,
            @Nullable RocketPart thruster,
            int fuselageCount,
            int thrusterCount) {
        stages.addFirst(new RocketStage(fuselage, fins, thruster, fuselageCount, thrusterCount));
    }

    public boolean validate() {
        if (!extraIssues.isEmpty()) return false;
        if (capsule == null || capsule.type != PartType.WARHEAD) return false;
        if (!capsule.isPayload()) return false;
        if (stages.isEmpty()) return false;

        for (RocketStage stage : stages) {
            if (stage.fuselage == null || stage.fuselage.type != PartType.FUSELAGE) return false;
            if (stage.fins != null && stage.fins.type != PartType.FINS) return false;
            if (stage.thruster == null || stage.thruster.type != PartType.THRUSTER) return false;

            if (stage.thrusterCount > stage.fuselageCount
                    || stage.fuselageCount % stage.thrusterCount != 0) {
                return false;
            }

            if (stage.fuselage.fuel != MissileKindsSpace.ANY
                    && stage.fuselage.fuel != stage.thruster.fuel) {
                return false;
            }
        }

        return true;
    }

    public void addIssue(Component issue) {
        extraIssues.add(issue);
    }

    public List<Component> findIssues(
            int stageNum,
            @Nullable CelestialBody from,
            @Nullable CelestialBody to,
            boolean fromOrbit,
            boolean toOrbit) {
        List<Component> issues = new ArrayList<>();

        if (capsule == null && stages.isEmpty()) return issues;

        if (capsule == null || !capsule.isPayload()) {
            issues.add(issue("invalidCapsule").withStyle(ChatFormatting.RED));
        }

        if (stageNum < stages.size()) {
            RocketStage stage = stages.get(stageNum);
            issues.add(issue("dryMass", getLaunchMass(stageNum)));
            issues.add(issue("wetMass", getWetMass(stageNum)));
            if (stage.thruster != null) {
                issues.add(issue("thrust", getThrust(stage)));
                issues.add(issue("isp", getISP(stage)));
            }
        }

        for (int i = 0; i < stages.size(); i++) {
            RocketStage stage = stages.get(i);
            int number = i + 1;
            if (stage.fuselage == null)
                issues.add(issue("missingFuselage", number).withStyle(ChatFormatting.RED));
            if (stage.thruster == null)
                issues.add(issue("missingThruster", number).withStyle(ChatFormatting.RED));

            if (stage.fuselage == null || stage.thruster == null) continue;

            if (stage.thrusterCount > stage.fuselageCount) {
                issues.add(issue("tooManyThrusters", number).withStyle(ChatFormatting.RED));
            }
            if (stage.fuselageCount % stage.thrusterCount != 0) {
                issues.add(issue("unevenThrusters", number).withStyle(ChatFormatting.RED));
            }

            if (stage.fuselage.fuel != MissileKindsSpace.ANY
                    && stage.fuselage.fuel != stage.thruster.fuel) {
                issues.add(issue("fuelMismatch", number).withStyle(ChatFormatting.RED));
            }

            if (i > 0 && stage.fins == null)
                issues.add(issue("noLegs", number).withStyle(ChatFormatting.YELLOW));
        }

        if (from != null && to != null) {
            int fuelRequirement = getFuelRequired(stageNum, from, to, fromOrbit, toOrbit);
            int fuelCapacity = getFuelCapacity(stageNum);

            if (fuelRequirement == Integer.MAX_VALUE) {
                issues.add(issue("insufficientThrust").withStyle(ChatFormatting.YELLOW));
            } else if (fuelCapacity < fuelRequirement) {
                issues.add(
                        issue("insufficientFuel", fuelCapacity, fuelRequirement)
                                .withStyle(ChatFormatting.YELLOW));
            } else if (fuelCapacity > 0 && fuelRequirement > 0) {
                issues.add(
                        issue("tripPossible", fuelCapacity, fuelRequirement)
                                .withStyle(ChatFormatting.GREEN));
            }
        }

        issues.addAll(extraIssues);

        return issues;
    }

    private static MutableComponent issue(String key, Object... args) {
        return Component.translatable("desc.gui.rocket." + key, args);
    }

    public Map<Fluid, Integer> getFillRequirement() {
        Map<Fluid, Integer> tanks = new LinkedHashMap<>();

        for (RocketStage stage : stages) {
            if (stage.thruster == null || stage.fuselage == null) continue;

            int amount = stage.fuselage.tank * stage.fuselageCount;
            Fluid fuel = stage.thruster.getFuel();
            Fluid oxidizer = stage.thruster.getOxidizer();

            if (fuel != null) tanks.merge(fuel, amount, Integer::sum);
            if (oxidizer != null) tanks.merge(oxidizer, amount, Integer::sum);
        }

        return tanks;
    }

    public boolean hasSufficientFuel(
            @Nullable CelestialBody from,
            @Nullable CelestialBody to,
            boolean fromOrbit,
            boolean toOrbit) {
        if (capsule != null && capsule.item == MissilePartsSpace.RP_POD_20.get()) {
            return from == to && (fromOrbit || toOrbit);
        }

        if (stages.isEmpty()) return from == to && fromOrbit && !toOrbit;

        return getFuelCapacity(0) >= getFuelRequired(0, from, to, fromOrbit, toOrbit);
    }

    private int getFuelCapacity(int stageNum) {
        if (stageNum >= stages.size()) return -1;
        RocketStage stage = stages.get(stageNum);
        if (stage.fuselage == null) return -1;
        return stage.fuselage.tank * stage.fuselageCount;
    }

    private int getFuelRequired(
            int stageNum,
            @Nullable CelestialBody from,
            @Nullable CelestialBody to,
            boolean fromOrbit,
            boolean toOrbit) {
        if (stageNum >= stages.size()) return -1;
        RocketStage stage = stages.get(stageNum);
        if (stage.fuselage == null || stage.thruster == null) return -1;

        return SolarSystem.getCostBetween(
                from,
                to,
                getLaunchMass(stageNum),
                getThrust(stage),
                getISP(stage),
                fromOrbit,
                toOrbit);
    }

    public int getThrust() {
        return getThrust(stages.getFirst());
    }

    private static int getThrust(RocketStage stage) {
        return stage.thruster.thrust * stage.thrusterCount;
    }

    private static int getISP(RocketStage stage) {
        return stage.thruster.isp;
    }

    public int getLaunchMass() {
        return getMass(0, false);
    }

    public int getLaunchMass(int stageNum) {
        return getMass(stageNum, false);
    }

    public int getWetMass(int stageNum) {
        return getMass(stageNum, true);
    }

    private int getMass(int stageNum, boolean wet) {
        int mass = 0;

        if (capsule != null) mass += capsule.mass;

        for (int i = stageNum; i < stages.size(); i++) {
            RocketStage stage = stages.get(i);
            if (stage.fuselage != null) mass += stage.fuselage.mass * stage.fuselageCount;
            if (stage.thruster != null) mass += stage.thruster.mass * stage.thrusterCount;

            if (stage.fuselage != null && (i > stageNum || wet)) {
                mass += stage.fuselage.tank * stage.fuselageCount / 4;
            }
        }

        return mass;
    }

    public double getHeight() {
        double height = 0;

        if (capsule != null) height += capsule.height;

        boolean isDeployed = true;

        for (RocketStage stage : stages) {
            if (stage.fuselage != null) height += stage.fuselage.height * stage.getStack();
            height +=
                    Math.max(
                            stage.thruster != null ? stage.thruster.height : 0,
                            isDeployed && stage.fins != null ? stage.fins.height : 0);
            isDeployed = false;
        }

        return height;
    }

    public double getHeight(int stageNum) {
        double height = 0;

        if (!stages.isEmpty()) {
            RocketStage stage = stages.get(Math.min(stageNum, stages.size() - 1));
            if (stage.fuselage != null) height += stage.fuselage.height * stage.getStack();
            height +=
                    Math.max(
                            stage.thruster != null ? stage.thruster.height : 0,
                            stageNum == 0 && stage.fins != null ? stage.fins.height : 0);
        }

        if (stages.isEmpty() || stageNum == stages.size() - 1) {
            if (capsule != null) height += capsule.height;
        }

        return height;
    }

    public double getOffset(int stageNum) {
        double height = 0;

        for (int i = 0; i < Math.min(stageNum, stages.size() - 1); i++) {
            RocketStage stage = stages.get(i);
            if (stage.fuselage != null) height += stage.fuselage.height * stage.getStack();
            height +=
                    Math.max(
                            stage.thruster != null ? stage.thruster.height : 0,
                            i == 0 && stage.fins != null ? stage.fins.height : 0);
        }

        return height;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RocketStruct other
                && capsule == other.capsule
                && stages.equals(other.stages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capsule, stages);
    }

    public record RocketStage(
            @Nullable RocketPart fuselage,
            @Nullable RocketPart fins,
            @Nullable RocketPart thruster,
            int fuselageCount,
            int thrusterCount) {
        public static final Codec<RocketStage> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                RocketPart.CODEC
                                                        .optionalFieldOf("fuselage")
                                                        .forGetter(
                                                                s ->
                                                                        Optional.ofNullable(
                                                                                s.fuselage)),
                                                RocketPart.CODEC
                                                        .optionalFieldOf("fins")
                                                        .forGetter(
                                                                s -> Optional.ofNullable(s.fins)),
                                                RocketPart.CODEC
                                                        .optionalFieldOf("thruster")
                                                        .forGetter(
                                                                s ->
                                                                        Optional.ofNullable(
                                                                                s.thruster)),
                                                Codec.INT
                                                        .optionalFieldOf("fc", 1)
                                                        .forGetter(RocketStage::fuselageCount),
                                                Codec.INT
                                                        .optionalFieldOf("tc", 1)
                                                        .forGetter(RocketStage::thrusterCount))
                                        .apply(
                                                i,
                                                (fuselage, fins, thruster, fc, tc) ->
                                                        new RocketStage(
                                                                fuselage.orElse(null),
                                                                fins.orElse(null),
                                                                thruster.orElse(null),
                                                                Math.max(fc, 1),
                                                                Math.max(tc, 1))));

        public static final StreamCodec<RegistryFriendlyByteBuf, RocketStage> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.optional(RocketPart.STREAM_CODEC),
                        s -> Optional.ofNullable(s.fuselage),
                        ByteBufCodecs.optional(RocketPart.STREAM_CODEC),
                        s -> Optional.ofNullable(s.fins),
                        ByteBufCodecs.optional(RocketPart.STREAM_CODEC),
                        s -> Optional.ofNullable(s.thruster),
                        ByteBufCodecs.VAR_INT,
                        RocketStage::fuselageCount,
                        ByteBufCodecs.VAR_INT,
                        RocketStage::thrusterCount,
                        (fuselage, fins, thruster, fc, tc) ->
                                new RocketStage(
                                        fuselage.orElse(null),
                                        fins.orElse(null),
                                        thruster.orElse(null),
                                        fc,
                                        tc));

        public int getStack() {
            return Math.max(fuselageCount / thrusterCount, 1);
        }

        public int getCluster() {
            return Math.max(fuselageCount / getStack(), 1);
        }
    }
}
