// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2026 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.orbit;

import com.hbm.blocks.multiblock.MultiblockHandlerXR;
import com.hbmspace.api.tile.IPropulsion;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStation;
import com.hbmspace.blocks.StationBlocksSpace;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.AstroMetric;
import com.hbmspace.dim.SolarSystem;
import com.hbmspace.dim.SolarSystemWorldSavedData;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.entity.missile.EntityRideableRocket.RocketState;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.items.ItemVOTVdrive.Destination;
import io.netty.buffer.ByteBuf;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Utf8String;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public class OrbitalStation {

    public String name = "";

    public CelestialBody orbiting;
    public CelestialBody target;

    private double eclipseAmount;

    public StationState state = StationState.ORBIT;
    public int stateTimer;
    public int maxStateTimer = 100;

    public boolean hasStation = false;

    public int dX;
    public int dZ;

    public boolean hasEngines = true;
    public List<BlockPos> errorsAt = new ArrayList<>();
    public int errorTimer;

    public float gravityMultiplier = 1;

    public enum StationState {
        ORBIT,
        LEAVING,
        TRANSFER,
        ARRIVING,
    }

    private @Nullable BlockEntityOrbitalStation mainPort;
    private final LinkedHashMap<BlockPos, BlockEntityOrbitalStation> ports = new LinkedHashMap<>();
    private int portIndex = 0;

    private final Set<IPropulsion> engines = new LinkedHashSet<>();

    public static OrbitalStation clientStation =
            new OrbitalStation(CelestialBody.getBody(Level.OVERWORLD));
    public static List<OrbitalStation> orbitingStations = new ArrayList<>();

    public static final int STATION_SIZE = 1024;
    public static final int BUFFER_SIZE = 256;
    public static final int WARNING_SIZE = 32;

    private static final int SHIP_MASS = 200_000;

    public OrbitalStation(CelestialBody orbiting) {
        this.orbiting = orbiting;
        this.target = orbiting;
    }

    public OrbitalStation(CelestialBody orbiting, int x, int z) {
        this(orbiting);
        this.dX = x;
        this.dZ = z;
    }

    public void travelTo(CelestialBody target) {
        if (state != StationState.ORBIT) return;
        if (!canTravel(orbiting, target)) return;

        setState(StationState.LEAVING, getLeaveTime());
        this.target = target;
    }

    public void update(Level level) {
        if (!level.isClientSide()) {
            eclipseAmount = -1;

            if (state == StationState.LEAVING) {
                if (stateTimer > maxStateTimer) {
                    setState(StationState.TRANSFER, getTransferTime());
                }
            } else if (state == StationState.TRANSFER) {
                if (stateTimer > maxStateTimer) {
                    setState(StationState.ARRIVING, getArriveTime());
                    orbiting = target;
                }
            } else if (state == StationState.ARRIVING) {
                if (stateTimer > maxStateTimer) {
                    setState(StationState.ORBIT, 0);
                }
            }

            SolarSystemWorldSavedData.get(level).setDirty();

            hasEngines = !engines.isEmpty();

            errorTimer--;
            if (errorTimer <= 0) {
                errorsAt = new ArrayList<>();
                errorTimer = 0;
            }
        }

        stateTimer++;
    }

    private boolean canTravel(CelestialBody from, CelestialBody to) {
        if (engines.isEmpty()) return false;

        double deltaV = SolarSystem.getDeltaVBetween(from, to);
        float totalThrust = getTotalThrust();

        boolean canTravel = true;
        errorsAt = new ArrayList<>();

        for (IPropulsion engine : engines) {
            float massPortion = engine.getThrust() / totalThrust;
            if (!engine.canPerformBurn(Math.round(SHIP_MASS * massPortion), deltaV)) {
                canTravel = false;
                errorsAt.add(engine.getBlockEntity().getBlockPos());
                errorTimer = 100;
            }
        }

        return canTravel;
    }

    private float getTotalThrust() {
        float thrust = 0;
        for (IPropulsion engine : engines) {
            thrust += engine.getThrust();
        }
        return thrust;
    }

    private int getLeaveTime() {
        int leaveTime = 20;
        for (IPropulsion engine : engines) {
            int time = engine.startBurn();
            if (time > leaveTime) leaveTime = time;
        }
        return leaveTime;
    }

    private int getArriveTime() {
        int arriveTime = 20;
        for (IPropulsion engine : engines) {
            int time = engine.endBurn();
            if (time > arriveTime) arriveTime = time;
        }
        return arriveTime;
    }

    private int getTransferTime() {
        if (mainPort == null) return -1;

        int size = calculateSize();
        double distance =
                SolarSystem.calculateDistanceBetweenTwoBodies(
                        mainPort.getLevel(), orbiting, target);
        float thrust = getTotalThrust();

        return calculateTransferTime(distance, size, thrust);
    }

    public static int calculateTransferTime(double distance, int size, float thrust) {
        return (int) (Math.log(1 + (distance * size / thrust * 100)) * 150);
    }

    public void setState(StationState state, int timeUntilNext) {
        this.state = state;
        stateTimer = 0;
        maxStateTimer = timeUntilNext;
    }

    public int engineCount() {
        return engines.size();
    }

    public boolean recallPod(Destination destination) {
        if (!hasStation) return false;
        if (destination.body().getBody() != orbiting) return false;

        for (BlockEntityOrbitalStation port : ports.values()) {
            EntityRideableRocket rocket = port.getDocked();

            if (rocket == null || !rocket.isReusable()) continue;

            RocketState state = rocket.getState();
            if (state != RocketState.AWAITING && state != RocketState.LANDED) continue;
            if (rocket.getFirstPassenger() != null) continue;

            rocket.recallPod(destination);
            return true;
        }

        return false;
    }

    public static void addPropulsion(IPropulsion propulsion) {
        BlockPos pos = propulsion.getBlockEntity().getBlockPos();
        getStationFromPosition(pos.getX(), pos.getZ()).engines.add(propulsion);
    }

    public static void removePropulsion(IPropulsion propulsion) {
        if (SolarSystemWorldSavedData.getIfLive() == null) return;
        BlockPos pos = propulsion.getBlockEntity().getBlockPos();
        getStationFromPosition(pos.getX(), pos.getZ()).engines.remove(propulsion);
    }

    public void addPort(BlockEntityOrbitalStation port) {
        ports.put(port.getBlockPos(), port);
        if (port.isCore()) mainPort = port;
    }

    public void removePort(BlockEntityOrbitalStation port) {
        ports.remove(port.getBlockPos());
    }

    public @Nullable BlockEntityOrbitalStation getPort() {
        if (ports.isEmpty()) return null;

        int index = 0;
        for (BlockEntityOrbitalStation port : ports.values()) {
            if (!port.hasDocked && !port.isReserved) {
                portIndex = index;
                return port;
            }
            index++;
        }

        portIndex++;
        if (portIndex >= ports.size()) portIndex = 0;

        return new ArrayList<>(ports.values()).get(portIndex);
    }

    public static @Nullable BlockEntityOrbitalStation getPort(int x, int z) {
        return getStationFromPosition(x, z).getPort();
    }

    private static final Direction[] HORIZONTALS = {
        Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    public int calculateSize() {
        if (mainPort == null) return 0;
        Level level = mainPort.getLevel();
        BlockPos origin = mainPort.getBlockPos();

        int minX, maxX;
        int minZ, maxZ;
        minX = maxX = origin.getX();
        minZ = maxZ = origin.getZ();

        ArrayDeque<BlockPos> stack = new ArrayDeque<>();
        stack.push(origin);

        HashSet<BlockPos> visited = new HashSet<>();

        while (!stack.isEmpty()) {
            BlockPos pos = stack.pop();
            visited.add(pos);

            if (pos.getX() < minX) minX = pos.getX();
            if (pos.getX() > maxX) maxX = pos.getX();
            if (pos.getZ() < minZ) minZ = pos.getZ();
            if (pos.getZ() > maxZ) maxZ = pos.getZ();

            for (Direction dir : HORIZONTALS) {
                BlockPos nextPos = pos.relative(dir);

                if (!visited.contains(nextPos) && isInStation(level, origin, nextPos)) {
                    stack.push(nextPos);
                }
            }
        }

        return (maxX - minX + 1) * (maxZ - minZ + 1);
    }

    private static boolean isInStation(Level level, BlockPos origin, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null
                && chunk.getSkyLightSources().getLowestSourceY(pos.getX() & 15, pos.getZ() & 15)
                        > level.getMinY() + 1) {
            return true;
        }
        return Math.abs(pos.getX() - origin.getX()) < 5 && Math.abs(pos.getZ() - origin.getZ()) < 5;
    }

    public double getUnscaledProgress(float partialTicks) {
        if (state == StationState.ORBIT) return 0;
        return Mth.clamp(((double) stateTimer + partialTicks) / (double) maxStateTimer, 0, 1);
    }

    public double getTransferProgress(float partialTicks) {
        if (state != StationState.TRANSFER) return 0;
        return easeInOutCirc(getUnscaledProgress(partialTicks));
    }

    public double getEclipseAmount(Level level) {
        if (eclipseAmount > -1) return eclipseAmount;

        double sunSize = SolarSystem.calculateSunSize(orbiting);

        double progress = getTransferProgress(0);

        List<AstroMetric> metrics;

        if (state == StationState.ORBIT) {
            double altitude = WorldProviderOrbit.getOrbitalAltitude(orbiting);
            metrics = SolarSystem.calculateMetricsFromSatellite(level, 0, orbiting, altitude);
        } else {
            double fromAlt = WorldProviderOrbit.getOrbitalAltitude(orbiting);
            double toAlt = WorldProviderOrbit.getOrbitalAltitude(target);
            metrics =
                    SolarSystem.calculateMetricsBetweenSatelliteOrbits(
                            level, 0, orbiting, target, fromAlt, toAlt, progress);

            double sunTargetSize = SolarSystem.calculateSunSize(target);
            sunSize = sunSize + progress * (sunTargetSize - sunSize);
        }

        eclipseAmount =
                WorldProviderCelestial.getEclipseFactor(
                        metrics, sunSize, SolarSystem.MAX_APPARENT_SIZE_ORBIT);

        return eclipseAmount;
    }

    private static double easeInOutCirc(double t) {
        return t < 0.5
                ? (1 - Math.sqrt(1 - Math.pow(2 * t, 3))) / 2
                : (Math.sqrt(1 - Math.pow(-2 * t + 2, 3)) + 1) / 2;
    }

    public static OrbitalStation getStationFromPosition(int x, int z) {
        SolarSystemWorldSavedData data = SolarSystemWorldSavedData.get();
        OrbitalStation station = data.getStationFromPosition(x, z);

        if (station == null) {
            station =
                    data.addStation(
                            Mth.floor((float) x / STATION_SIZE),
                            Mth.floor((float) z / STATION_SIZE),
                            CelestialBody.getBody(Level.OVERWORLD));
        }

        return station;
    }

    public static OrbitalStation getStation(int x, int z) {
        return getStationFromPosition(x * STATION_SIZE, z * STATION_SIZE);
    }

    public void serialize(ByteBuf buf) {
        Utf8String.write(buf, orbiting.name, Short.MAX_VALUE);
        Utf8String.write(buf, target.name, Short.MAX_VALUE);
        buf.writeInt(state.ordinal());
        buf.writeInt(stateTimer);
        buf.writeInt(maxStateTimer);
        buf.writeBoolean(hasEngines);
        buf.writeFloat(gravityMultiplier);

        Utf8String.write(buf, name, Short.MAX_VALUE);

        buf.writeInt(errorsAt.size());
        for (BlockPos error : errorsAt) {
            buf.writeLong(error.asLong());
        }
    }

    public static OrbitalStation deserialize(ByteBuf buf) {
        OrbitalStation station =
                new OrbitalStation(CelestialBody.getBody(Utf8String.read(buf, Short.MAX_VALUE)));
        station.target = CelestialBody.getBody(Utf8String.read(buf, Short.MAX_VALUE));
        station.state = StationState.values()[buf.readInt()];
        station.stateTimer = buf.readInt();
        station.maxStateTimer = buf.readInt();
        station.hasEngines = buf.readBoolean();
        station.gravityMultiplier = buf.readFloat();

        station.name = Utf8String.read(buf, Short.MAX_VALUE);

        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            station.errorsAt.add(BlockPos.of(buf.readLong()));
        }

        return station;
    }

    public static void spawn(ServerLevel level, int x, int z) {
        BlockPos core = new BlockPos(x, 127, z);
        if (level.getBlockState(core).is(StationBlocksSpace.ORBITAL_STATION.get())) return;

        MultiblockHandlerXR.placeAssembled(
                level, core, StationBlocksSpace.ORBITAL_STATION.get(), Direction.NORTH);
    }

    public static void addStation(int x, int z, CelestialBody body) {
        SolarSystemWorldSavedData data = SolarSystemWorldSavedData.get();
        OrbitalStation station = data.getStationFromPosition(x * STATION_SIZE, z * STATION_SIZE);

        if (station == null) {
            station = data.addStation(x, z, body);
        }

        station.orbiting = station.target = body;
        station.hasStation = true;
    }
}
