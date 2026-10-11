// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-FileCopyrightText: 2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbm.entity.ModEntities;
import com.hbm.entity.mob.EntityUFO;
import com.hbm.platform.FabricServerEvents;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.entity.MobsSpace;
import com.hbmspace.entity.RocketryEntitiesSpace;
import com.hbmspace.entity.missile.EntityCombatDropPod;
import com.hbmspace.entity.mob.siege.EntitySiegeCraft;
import com.hbmspace.entity.mob.siege.EntitySiegeUFO;
import com.hbmspace.entity.mob.siege.SiegeTier;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;

public class CBT_Invasion extends CelestialBodyTrait {
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
    public boolean warningPlayed;

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
        if (isRemote) {
            if (!isInvading && !warningPlayed) {
                warningPlayed = true;
                CLIENT_WARNING.run();
            }
            return;
        }

        prepare();

        MinecraftServer server = FabricServerEvents.getCurrentServer();
        if (server == null) return;
        updateBar(server, body);

        if (!isInvading) return;
        ServerLevel level = body.getLevel(server);
        if (level == null || level.players().isEmpty()) return;

        logicTick(level);
        handleBurstSpawning(level);
        spawnAttempt(level);
    }

    public @Nullable EntityCombatDropPod spawnCattle(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        RandomSource rand = level.getRandom();
        ServerPlayer player = players.get(rand.nextInt(players.size()));
        if (player.getY() < 50 && !level.isFlat()) return null;

        EntityCombatDropPod pod =
                new EntityCombatDropPod(RocketryEntitiesSpace.COMBAT_DROP_POD.get(), level);
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
        Entity entity = type.create(level, EntitySpawnReason.EVENT);
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

    public @Nullable EntityUFO logicTick(ServerLevel level) {
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
                    EntityUFO boss = spawnBoss(level);
                    bossSpawned = true;
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

        List<ServerPlayer> players = level.players();
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
            EntitySiegeCraft bigUfo = new EntitySiegeCraft(MobsSpace.SIEGE_CRAFT.get(), level);
            bigUfo.snapTo(spawnX, spawnY, spawnZ, rand.nextFloat() * 360.0F, 0.0F);
            bigUfo.setTier(rand.nextFloat() < 0.5F - waveFactor ? SiegeTier.STONE : SiegeTier.IRON);
            added = level.addFreshEntity(bigUfo);
            spawned = bigUfo;
        } else {
            EntitySiegeUFO smallUfo = new EntitySiegeUFO(MobsSpace.SIEGE_UFO.get(), level);
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

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel)) return;
        CelestialBody body = CelestialBody.getBody(entity.level());
        CBT_Invasion invasion = body.getTrait(CBT_Invasion.class);
        if (invasion != null) invasion.onKill(entity, body);
    }

    public void onKill(LivingEntity entity, CelestialBody body) {
        if (entity instanceof EntitySiegeUFO) {
            kills += entity.getMaxHealth() >= 100 * 0.25 ? 2 : 1;
            body.modifyTraits(this);
        } else if (entity instanceof EntitySiegeCraft) {
            kills += 10;
            body.modifyTraits(this);
        }

        if (wave >= 4
                && entity instanceof EntityUFO
                && entity.level() instanceof ServerLevel level) {
            HashMap<Class<? extends CelestialBodyTrait>, CelestialBodyTrait> currentTraits =
                    body.getTraits();
            currentTraits.remove(CBT_Invasion.class);

            broadcast(
                    level,
                    Component.translatable("invasion.hbmspace.over")
                            .withStyle(ChatFormatting.YELLOW));

            body.setTraits(currentTraits);
        }
    }

    private @Nullable EntityUFO spawnBoss(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        ServerPlayer player = players.get(level.getRandom().nextInt(players.size()));
        EntityUFO ufo = ModEntities.UFO.get().create(level, EntitySpawnReason.EVENT);
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
        return level.addFreshEntity(ufo) ? ufo : null;
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
        nbt.putBoolean("bossSpawned", bossSpawned);
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
        bossSpawned = nbt.getBooleanOr("bossSpawned", false);
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
    }
}
