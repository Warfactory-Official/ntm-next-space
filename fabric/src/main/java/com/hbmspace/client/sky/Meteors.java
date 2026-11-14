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
    private static @Nullable ClientLevel level;

    private Meteors() {}

    public static void addMeteor() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || WorldProviderCelestial.of(player.level()) == null) return;

        useLevel((ClientLevel) player.level());
        RandomSource rand = player.level().getRandom();
        meteors.add(
                new Meteor(
                        player.getX() + rand.nextInt(16000) - 8000,
                        2017,
                        player.getZ() + rand.nextInt(16000) - 8000));
    }

    public static void clientTick() {
        ClientLevel current = Minecraft.getInstance().level;
        useLevel(current);
        if (current == null || WorldProviderCelestial.of(current) == null) {
            return;
        }

        ListIterator<Meteor> iterator = meteors.listIterator();
        while (iterator.hasNext()) {
            Meteor meteor = iterator.next();
            Meteor fragment = meteor.update(current.getRandom());
            if (meteor.isDead) iterator.remove();
            if (fragment != null) iterator.add(fragment);
        }
    }

    public static void clear() {
        meteors.clear();
        level = null;
    }

    private static void useLevel(@Nullable ClientLevel current) {
        if (current != level) {
            clear();
            level = current;
        }
    }
}
