// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2024-2025 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.trait;

import com.hbm.platform.NeoForgeServerEvents;
import com.hbmspace.dim.CelestialBody;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Utf8String;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class CBT_War extends CelestialBodyTrait {

    public int health;
    public int shield;

    public List<Projectile> projectiles;

    public CBT_War() {
        this.health = 100;
        this.shield = 0;
        this.projectiles = new ArrayList<>();
    }

    public CBT_War(int health, int shield) {
        this.health = health;
        this.shield = shield;
        this.projectiles = new ArrayList<>();
    }

    public void launchProjectile(Projectile proj) {
        projectiles.add(proj);
    }

    public void launchProjectile(
            float traveltime,
            int size,
            int damage,
            float x,
            double y,
            double z,
            ProjectileType type,
            ResourceKey<Level> target) {
        Projectile projectile = new Projectile(traveltime, size, damage, x, y, z, type, target);
        projectiles.add(projectile);
    }

    public void split(int amount, Projectile projectile, ProjectileType type) {
        if (projectile.getTravel() <= 0) {
            for (int j = 0; j < amount; j++) {
                Random rand = new Random();
                float randX = rand.nextFloat() * 160 - 80;
                float randY = rand.nextFloat() * 90 - 80;

                this.launchProjectile(
                        Math.abs(20 + j * 10),
                        projectile.getSize(),
                        projectile.getDamage(),
                        (float) (projectile.getTranslateX()),
                        projectile.getTranslateY() - randY * j,
                        projectile.getTranslateZ() + randX * j,
                        type,
                        projectile.getTarget());
            }

            this.destroyProjectile(projectile);
        }
    }

    @Override
    public void update(boolean isRemote, CelestialBody body) {
        if (isRemote || !NeoForgeServerEvents.getCurrentServer().tickRateManager().runsNormally())
            return;

        for (int i = 0; i < this.getProjectiles().size(); i++) {
            Projectile projectile = this.getProjectiles().get(i);

            projectile.update();

            if (projectile.getTravel() <= 0) {
                projectile.impact();
            }

            if (projectile.getAnimtime() >= 100) {
                this.destroyProjectile(projectile);
                i--;

                if (this.health > 0) {
                    CelestialBody.damage(
                            projectile.getDamage(), CelestialBody.getBody(projectile.getTarget()));
                } else {
                    CelestialBody target =
                            CelestialBody.getBody(projectile.getTarget()).getPlanet();
                    target.modifyTraits(new CBT_Destroyed());
                    this.health = 0;
                }
            }

            if (projectile.getType() == ProjectileType.SPLITSHOT) {
                if (projectile.getTravel() <= 0) {
                    this.split(4, projectile, ProjectileType.SMALL);
                    this.destroyProjectile(projectile);
                    i--;
                }
            }
        }
    }

    public void destroyProjectile(Projectile proj) {
        projectiles.remove(proj);
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("health", health);
        nbt.putInt("shield", shield);

        CompoundTag projectilesTag = new CompoundTag();
        for (int i = 0; i < projectiles.size(); i++) {
            CompoundTag projTag = new CompoundTag();
            projectiles.get(i).writeToNBT(projTag);
            projectilesTag.put("projectile" + i, projTag);
        }
        nbt.put("projectiles", projectilesTag);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        shield = nbt.getIntOr("shield", 0);
        health = nbt.getIntOr("health", 0);
        CompoundTag projectilesTag = nbt.getCompoundOrEmpty("projectiles");
        projectiles = new ArrayList<>();
        for (int i = 0; projectilesTag.contains("projectile" + i); i++) {
            Projectile projectile = new Projectile();
            projectile.readFromNBT(projectilesTag.getCompoundOrEmpty("projectile" + i));
            projectiles.add(projectile);
        }
    }

    @Override
    public void writeToBytes(ByteBuf buf) {
        buf.writeInt(health);
        buf.writeInt(shield);
        buf.writeInt(projectiles.size());
        for (Projectile projectile : projectiles) {
            projectile.writeToBytes(buf);
        }
    }

    @Override
    public void readFromBytes(ByteBuf buf) {
        health = buf.readInt();
        shield = buf.readInt();
        int numProjectiles = buf.readInt();
        projectiles = new ArrayList<>(numProjectiles);
        for (int i = 0; i < numProjectiles; i++) {
            Projectile projectile = new Projectile();
            projectile.readFromBytes(buf);
            projectiles.add(projectile);
        }
    }

    public static class Projectile {

        private float traveltime;
        private int size;
        private int damage;
        private int animtime;
        private float flashtime;
        private double translateX;
        private double translateY;
        private double translateZ;
        private ProjectileType type;
        public int GUIangle;
        private ResourceKey<Level> target = Level.OVERWORLD;

        public Projectile() {
            this.animtime = 0;
            this.flashtime = 0;
            this.type = ProjectileType.MEDIUM;
        }

        public Projectile(
                float traveltime,
                int size,
                int damage,
                double posX,
                double posY,
                double posZ,
                ProjectileType type,
                ResourceKey<Level> target) {
            this.traveltime = traveltime;
            this.size = size;
            this.damage = damage;
            this.animtime = 0;
            this.flashtime = 0;
            this.translateX = posX;
            this.translateY = posY;
            this.translateZ = posZ;
            this.type = type;
            this.target = target;
        }

        public void writeToNBT(CompoundTag nbt) {
            nbt.putInt("damage", damage);
            nbt.putFloat("traveltime", traveltime);
            nbt.putInt("size", size);
            nbt.putDouble("translateX", translateX);
            nbt.putDouble("translateY", translateY);
            nbt.putDouble("translateZ", translateZ);
            nbt.putInt("animtime", animtime);
            nbt.putString("type", type.name());
            nbt.putString("target", target.identifier().toString());
        }

        public void readFromNBT(CompoundTag nbt) {
            damage = nbt.getIntOr("damage", 0);
            traveltime = nbt.getFloatOr("traveltime", 0);
            size = nbt.getIntOr("size", 0);
            translateX = nbt.getDoubleOr("translateX", 0);
            translateY = nbt.getDoubleOr("translateY", 0);
            translateZ = nbt.getDoubleOr("translateZ", 0);
            animtime = nbt.getIntOr("animtime", 0);
            type = ProjectileType.valueOf(nbt.getStringOr("type", ProjectileType.MEDIUM.name()));
            target = dimension(nbt.getStringOr("target", Level.OVERWORLD.identifier().toString()));
        }

        public void writeToBytes(ByteBuf buf) {
            buf.writeInt(damage);
            buf.writeFloat(traveltime);
            buf.writeInt(size);
            buf.writeDouble(translateX);
            buf.writeDouble(translateY);
            buf.writeDouble(translateZ);
            buf.writeInt(animtime);
            buf.writeFloat(flashtime);
            buf.writeByte(type.ordinal());
            buf.writeInt(GUIangle);
            Utf8String.write(buf, target.identifier().toString(), Short.MAX_VALUE);
        }

        public void readFromBytes(ByteBuf buf) {
            damage = buf.readInt();
            traveltime = buf.readFloat();
            size = buf.readInt();
            translateX = buf.readDouble();
            translateY = buf.readDouble();
            translateZ = buf.readDouble();
            animtime = buf.readInt();
            flashtime = buf.readFloat();
            type = ProjectileType.values()[buf.readByte()];
            GUIangle = buf.readInt();
            target = dimension(Utf8String.read(buf, Short.MAX_VALUE));
        }

        private static ResourceKey<Level> dimension(String id) {
            return ResourceKey.create(Registries.DIMENSION, Identifier.parse(id));
        }

        public ProjectileType getType() {
            return type;
        }

        public void setType(ProjectileType type) {
            this.type = type;
        }

        public float getFlashtime() {
            return flashtime;
        }

        public void update() {
            if (traveltime > 0) {
                traveltime--;
            } else {
                traveltime = 0;
                if (this.getType() != ProjectileType.SPLITSHOT) {
                    animtime = Math.min(100, animtime + 1);
                }
            }
        }

        public void impact() {
            flashtime += 0.6f;
            flashtime = Math.min(100.0f, flashtime + 0.1f * (100.0f - flashtime) * 0.15f);

            if (flashtime >= 100) {
                flashtime = 100;
            }

            if (animtime == 0) {
                flashtime = 0;
            }
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getDamage() {
            return damage;
        }

        public float getTravel() {
            return traveltime;
        }

        public void setTravel(float travel) {
            this.traveltime = travel;
        }

        public void setDamage(int damage) {
            this.damage = damage;
        }

        public int getAnimtime() {
            return animtime;
        }

        public void setAnimtime(int animtime) {
            this.animtime = animtime;
        }

        public void setFlashtime(float flashtime) {
            this.flashtime = flashtime;
        }

        public double getTranslateX() {
            return translateX;
        }

        public ResourceKey<Level> getTarget() {
            return target;
        }

        public double getTranslateY() {
            return translateY;
        }

        public double getTranslateZ() {
            return translateZ;
        }
    }

    public enum ProjectileType {
        SMALL,
        MEDIUM,
        HUGE,
        INCENDIARY,
        NUCLEAR,
        SPLITSHOT
    }
}
