// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2026 George Paton
// SPDX-FileCopyrightText: 2026 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbmspace.dim.WorldProviderCelestial;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class Meteors {

    public enum MeteorType {
        STANDARD,
        FRAGMENT,
        SMOKE
    }

    public static final class Meteor {
        public double posX;
        public double posY;
        public double posZ;
        public double prevPosX;
        public double prevPosY;
        public double prevPosZ;
        public double motionX;
        public double motionY;
        public double motionZ;
        public boolean isDead = false;
        public long age;
        public MeteorType type;

        public Meteor(double posX, double posY, double posZ) {
            this(posX, posY, posZ, MeteorType.STANDARD, -31.2, -20.8, 20);
        }

        public Meteor(
                double posX,
                double posY,
                double posZ,
                MeteorType type,
                double motionX,
                double motionY,
                double motionZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.type = type;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
        }

        private @Nullable Meteor update(RandomSource rand) {
            if (this.posY <= 500 && this.type != MeteorType.SMOKE) {
                this.isDead = true;
            }

            if (this.type == MeteorType.SMOKE) {
                this.age++;
                if (this.age >= 60) this.isDead = true;
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;

            if (this.type != MeteorType.SMOKE && this.type != MeteorType.FRAGMENT) {
                return new Meteor(
                        (this.posX + rand.nextInt(16)) - 8,
                        (this.posY + rand.nextInt(16)),
                        (this.posZ + rand.nextInt(16)) - 8,
                        MeteorType.SMOKE,
                        0,
                        0,
                        0);
            }

            return null;
        }
    }

    static final List<Meteor> meteors = new ArrayList<>();

    private Meteors() {}

    public static void addMeteor() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        RandomSource rand = player.level().getRandom();
        meteors.add(
                new Meteor(
                        player.getX() + rand.nextInt(16000) - 8000,
                        2017,
                        player.getZ() + rand.nextInt(16000) - 8000));
    }

    public static void clientTick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null
                || Minecraft.getInstance().player == null
                || WorldProviderCelestial.of(level) == null) {
            return;
        }

        ListIterator<Meteor> iterator = meteors.listIterator();
        while (iterator.hasNext()) {
            Meteor meteor = iterator.next();
            Meteor fragment = meteor.update(level.getRandom());
            if (meteor.isDead) iterator.remove();
            if (fragment != null) iterator.add(fragment);
        }
    }
}
