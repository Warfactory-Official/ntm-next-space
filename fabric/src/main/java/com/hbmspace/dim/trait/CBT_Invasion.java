/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2026 George Paton
 * SPDX-FileCopyrightText: 2026 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2026 George Paton. All rights reserved.
 * Copyright (C) 2026 JamesH_2. All rights reserved.
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

package com.hbmspace.dim.trait;

import com.hbm.entity.ModEntities;
import com.hbm.entity.mob.UFOEntity;
import com.hbm.platform.ServerEvents;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.entity.MobsSpace;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.entity.missile.CombatDropPodEntity;
import com.hbmspace.entity.mob.siege.SiegeCraftEntity;
import com.hbmspace.entity.mob.siege.SiegeTier;
import com.hbmspace.entity.mob.siege.SiegeUFOEntity;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;

public class CBT_Invasion extends CelestialBodyTrait {

    private static final String COUNTED_DEATH = "hbmspace:invasion_kill";

    public static Runnable CLIENT_WARNING = () -> {};

    public int wave;
    public int kills;
    public int killreq;
    public double waveTime;
    public boolean isInvading;
    public int lastSpawns;
    public int podBurstCounter = 0;
    public int podCooldown = 0;
    public boolean bossSpawned = false;
    public @Nullable UUID bossUUID;
    public boolean warningPlayed;
    public boolean paused;

    private @Nullable ServerBossEvent bar;

    public CBT_Invasion() {}

    public CBT_Invasion(int wave, double waveTime, boolean isInvading) {
        this.wave = wave;
        this.waveTime = waveTime;
        this.isInvading = isInvading;
    }

    public void prepare() {
        if (!isInvading && waveTime >= 0) {
            waveTime--;
            warningPlayed = true;
            if (waveTime <= 5) {
                warningPlayed = false;
            }
            if (waveTime <= 0) {
                isInvading = true;
            }
        }
    }

    @Override
    public void update(boolean isRemote, CelestialBody body) {
        prepare();

        MinecraftServer server = ServerEvents.getCurrentServer();
        if (server == null) return;
        updateBar(server, body);

        if (!isInvading) return;
        ServerLevel level = body.getLevel(server);
        paused =
                level == null
                        || level.getPlayers(EntitySelector.NO_SPECTATORS).isEmpty()
                        || level.getDifficulty() == Difficulty.PEACEFUL;
        if (paused) return;

        logicTick(level);
        handleBurstSpawning(level);
        spawnAttempt(level);
    }

    public @Nullable CombatDropPodEntity spawnCattle(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(EntitySelector.NO_SPECTATORS);
        RandomSource rand = level.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));
        if (player.getY() < 50 && !level.isFlat()) return null;

        CombatDropPodEntity pod =
                new CombatDropPodEntity(RocketryEntitiesSpace.COMBAT_DROP_POD.get(), level);
        pod.setPos(
                player.getX() + rand.nextGaussian() * 15,
                250,
                player.getZ() + rand.nextGaussian() * 15);
        pod.setDeltaMovement(0, -1.5, 0);

        EntityType<?> glyph;
        int amount = 1;
        if (wave == 1) {
            amount = 2;
            glyph = ModEntities.GLYPHID.get();
        } else if (wave == 2) {
            int roll = rand.nextInt(3);
            amount = Math.max(1, rand.nextInt(4));
            glyph =
                    switch (roll) {
                        case 0 -> ModEntities.GLYPHID.get();
                        case 1 -> ModEntities.GLYPHID_BRAWLER.get();
                        default -> ModEntities.GLYPHID_DIGGER.get();
                    };
        } else if (wave >= 3) {
            int roll = rand.nextInt(5);
            amount = Math.max(1, rand.nextInt(6));
            glyph =
                    switch (roll) {
                        case 0 -> ModEntities.GLYPHID.get();
                        case 1 -> ModEntities.GLYPHID_BRAWLER.get();
                        case 2 -> ModEntities.GLYPHID_DIGGER.get();
                        case 3 -> ModEntities.GLYPHID_BLASTER.get();
                        default -> ModEntities.GLYPHID_BEHEMOTH.get();
                    };
            if (roll >= 4) amount = 1;
        } else {
            glyph = ModEntities.GLYPHID.get();
        }

        pod.setPayload(template(glyph, level), amount, 2);
        return level.addFreshEntity(pod) ? pod : null;
    }

    private static CompoundTag template(EntityType<?> type, ServerLevel level) {
        Entity entity = type.create(level, new EntitySpawnRequest(EntitySpawnReason.EVENT, true));
        TagValueOutput output =
                TagValueOutput.createWithContext(
                        ProblemReporter.DISCARDING, level.registryAccess());
        entity.save(output);
        return output.buildResult();
    }

    public void handleBurstSpawning(ServerLevel level) {
        if (wave > 3) return;

        if (podCooldown > 0) {
            podCooldown--;
            return;
        }

        if (level.getGameTime() % 10 + level.getRandom().nextInt(3) == 0) {
            spawnCattle(level);
            podBurstCounter++;
            if (podBurstCounter >= 3 + (wave - 1)) {
                podBurstCounter = 0;
                podCooldown = 500;
            }
        }
    }

    public @Nullable UFOEntity logicTick(ServerLevel level) {
        switch (wave) {
            case 0 -> advanceWave(level);
            case 1 -> {
                killreq = 20;
                if (kills >= killreq) advanceWave(level);
            }
            case 2 -> {
                killreq = 100;
                if (kills >= killreq) advanceWave(level);
            }
            case 3 -> {
                killreq = 150;
                if (kills >= killreq) advanceWave(level);
            }
            case 4 -> {
                killreq = 1;
                if (!bossSpawned) {
                    UFOEntity boss = spawnBoss(level);
                    bossSpawned = bossUUID != null;
                    if (bossSpawned) SolarSystemWorldSavedData.get().setDirty();
                    return boss;
                }
            }
            default -> {}
        }
        return null;
    }

    private void advanceWave(ServerLevel level) {
        wave++;
        kills = 0;
        Component text =
                wave == 4
                        ? Component.translatable("invasion.hbmspace.finalWave")
                        : Component.translatable("invasion.hbmspace.waveStarting", wave);
        broadcast(level, text.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    public @Nullable Entity spawnAttempt(ServerLevel level) {
        if (wave > 3) return null;
        int timer = 200;
        if (wave == 2) timer = 100;
        if (wave == 3) timer = 80;

        if (level.getGameTime() % timer != 0) return null;

        List<ServerPlayer> players = level.getPlayers(EntitySelector.NO_SPECTATORS);
        RandomSource rand = level.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));
        if (player.getY() < 50 && !level.isFlat()) return null;

        double spawnX = player.getX() + rand.nextGaussian() * 30;
        double spawnZ = player.getZ() + rand.nextGaussian() * 30;
        double spawnY = player.getY() + 30 + rand.nextInt(20);

        float bigUfoChance = 0.0F;
        if (wave == 2) bigUfoChance = 0.1F;
        else if (wave == 3) bigUfoChance = 0.2F;

        float waveFactor = Math.min(wave * 0.05F, 0.2F);

        boolean added;
        Entity spawned;
        if (rand.nextFloat() < bigUfoChance) {
            SiegeCraftEntity bigUfo = new SiegeCraftEntity(MobsSpace.SIEGE_CRAFT.get(), level);
            bigUfo.snapTo(spawnX, spawnY, spawnZ, rand.nextFloat() * 360.0F, 0.0F);
            bigUfo.setTier(rand.nextFloat() < 0.5F - waveFactor ? SiegeTier.STONE : SiegeTier.IRON);
            added = level.addFreshEntity(bigUfo);
            spawned = bigUfo;
        } else {
            SiegeUFOEntity smallUfo = new SiegeUFOEntity(MobsSpace.SIEGE_UFO.get(), level);
            smallUfo.snapTo(spawnX, spawnY, spawnZ, rand.nextFloat() * 360.0F, 0.0F);
            float roll = rand.nextFloat();
            SiegeTier chosen;
            if (roll < 0.15F - waveFactor * 1.5F) chosen = SiegeTier.CLAY;
            else if (roll < 0.35F - waveFactor * 1.2F) chosen = SiegeTier.STONE;
            else if (roll < 0.50F - waveFactor) chosen = SiegeTier.IRON;
            else if (roll < 0.65F - waveFactor * 0.8F) chosen = SiegeTier.SILVER;
            else if (roll < 0.80F - waveFactor * 0.5F) chosen = SiegeTier.GOLD;
            else if (roll < 0.90F - waveFactor * 0.2F) chosen = SiegeTier.DESH;
            else if (roll < 0.97F) chosen = SiegeTier.SCHRAB;
            else chosen = SiegeTier.DNT;
            smallUfo.setTier(chosen);
            added = level.addFreshEntity(smallUfo);
            spawned = smallUfo;
        }

        lastSpawns++;
        return added ? spawned : null;
    }

    public static void onDeath(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel)) return;
        if (!(entity instanceof SiegeUFOEntity)
                && !(entity instanceof SiegeCraftEntity)
                && !(entity instanceof UFOEntity)) return;
        CelestialBody body = invasionBody(entity);
        if (body == null) return;
        CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);
        if (invasion != null) invasion.onKill(entity, body);
    }

    public static void onEntityUnload(Entity entity, ServerLevel level) {
        var reason = entity.getRemovalReason();
        if (reason != Entity.RemovalReason.KILLED
                && reason != Entity.RemovalReason.DISCARDED
                && reason != Entity.RemovalReason.UNLOADED_TO_CHUNK) return;
        if (entity instanceof LivingEntity living && living.isDeadOrDying()) {
            if (SolarSystemWorldSavedData.getIfLive() != null) onDeath(living);
        } else if (reason != Entity.RemovalReason.UNLOADED_TO_CHUNK
                && entity instanceof UFOEntity ufo) {
            CelestialBody body = invasionBody(ufo);
            if (body == null) return;
            CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);
            invasion.bossSpawned = false;
            invasion.bossUUID = null;
            SolarSystemWorldSavedData.get().setDirty();
        }
    }

    private static @Nullable CelestialBody invasionBody(LivingEntity entity) {
        if (entity instanceof UFOEntity) {
            for (CelestialBody body : CelestialBody.getAllBodies()) {
                CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);
                if (invasion != null && entity.getUUID().equals(invasion.bossUUID)) return body;
            }
            return null;
        }
        return CelestialBody.getBody(entity.level());
    }

    public void onKill(LivingEntity entity, CelestialBody body) {
        int value =
                switch (entity) {
                    case SiegeUFOEntity _ ->
                            entity.getMaxHealth() >= 100 * 0.25 ? 2 : 1;
                    case SiegeCraftEntity _ -> 10;
                    default -> 0;
                };
        boolean boss =
                wave >= 4 && entity instanceof UFOEntity && entity.getUUID().equals(bossUUID);
        if ((value == 0 && !boss) || !entity.addTag(COUNTED_DEATH)) return;
        if (value != 0) {
            kills += value;
            body.modifyTraits(this);
        }

        if (boss && entity.level() instanceof ServerLevel level) {
            HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                    body.getTraits();
            currentTraits.remove(CBT_Invasion.class);

            broadcast(
                    body.getLevel(level.getServer()),
                    Component.translatable("invasion.hbmspace.over")
                            .withStyle(ChatFormatting.YELLOW));

            body.setTraits(currentTraits);
        }
    }

    private @Nullable UFOEntity spawnBoss(ServerLevel level) {
        List<ServerPlayer> players = level.getPlayers(EntitySelector.NO_SPECTATORS);
        ServerPlayer player = players.get(level.getRandom().nextInt(players.size()));
        UFOEntity ufo = ModEntities.UFO.get().create(level, EntitySpawnReason.EVENT);
        if (ufo == null) return null;
        ufo.scanCooldown = 100;
        ufo.snapTo(
                player.getX(),
                player.getY() + 50,
                player.getZ(),
                Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0F),
                0.0F);
        ufo.yHeadRot = ufo.getYRot();
        ufo.yBodyRot = ufo.getYRot();
        ufo.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(ufo.blockPosition()),
                EntitySpawnReason.EVENT,
                null);
        bossUUID = ufo.getUUID();
        if (level.addFreshEntity(ufo)) return ufo;
        bossUUID = null;
        return null;
    }

    private static void broadcast(ServerLevel level, Component text) {
        for (ServerPlayer player : level.players()) player.sendSystemMessage(text);
    }

    public void updateBar(MinecraftServer server, CelestialBody body) {
        if (bar == null) {
            bar =
                    new ServerBossEvent(
                            UUID.randomUUID(),
                            Component.empty(),
                            BossEvent.BossBarColor.PINK,
                            BossEvent.BossBarOverlay.PROGRESS);
        }
        bar.setName(Component.translatable("invasion.hbmspace.wave", wave));
        bar.setProgress(killreq > 0 ? Mth.clamp((float) (killreq - kills) / killreq, 0F, 1F) : 1F);

        boolean shown = isInvading && wave < 4;
        Set<ServerPlayer> watching = new HashSet<>();
        if (shown) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (CelestialBody.getBody(player.level()) == body) watching.add(player);
            }
        }
        for (ServerPlayer player : List.copyOf(bar.getPlayers())) {
            if (!watching.contains(player)) bar.removePlayer(player);
        }
        for (ServerPlayer player : watching) bar.addPlayer(player);
    }

    public @Nullable ServerBossEvent bar() {
        return bar;
    }

    public void removeBar() {
        if (bar != null) bar.removeAllPlayers();
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("wave", wave);
        nbt.putInt("kills", kills);
        nbt.putInt("killreq", killreq);
        nbt.putDouble("waveTime", waveTime);
        nbt.putBoolean("isInvading", isInvading);
        nbt.putBoolean("warningPlayed", warningPlayed);
        nbt.putInt("podBurst", podBurstCounter);
        nbt.putInt("podCooldown", podCooldown);
        if (bossUUID != null) nbt.store("boss", UUIDUtil.CODEC, bossUUID);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        wave = nbt.getIntOr("wave", 0);
        kills = nbt.getIntOr("kills", 0);
        killreq = nbt.getIntOr("killreq", 0);
        waveTime = nbt.getDoubleOr("waveTime", 0);
        isInvading = nbt.getBooleanOr("isInvading", false);
        warningPlayed = nbt.getBooleanOr("warningPlayed", false);
        podBurstCounter = nbt.getIntOr("podBurst", 0);
        podCooldown = nbt.getIntOr("podCooldown", 0);
        bossUUID = nbt.read("boss", UUIDUtil.CODEC).orElse(null);
        bossSpawned = bossUUID != null;
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeInt(wave);
        buf.writeInt(kills);
        buf.writeInt(killreq);
        buf.writeDouble(waveTime);
        buf.writeBoolean(isInvading);
        buf.writeBoolean(warningPlayed);
        buf.writeInt(podBurstCounter);
        buf.writeInt(podCooldown);
        buf.writeBoolean(bossSpawned);
        buf.writeBoolean(paused);
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        wave = buf.readInt();
        kills = buf.readInt();
        killreq = buf.readInt();
        waveTime = buf.readDouble();
        isInvading = buf.readBoolean();
        warningPlayed = buf.readBoolean();
        podBurstCounter = buf.readInt();
        podCooldown = buf.readInt();
        bossSpawned = buf.readBoolean();
        paused = buf.readBoolean();
    }
}
