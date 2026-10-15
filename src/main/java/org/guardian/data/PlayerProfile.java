package org.guardian.data;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Хранилище данных и состояния игрока в реальном времени
 */
public class PlayerProfile {
    private final UUID uuid;
    private final String name;
    private String clientBrand = "Vanilla / Unspecified";
    private boolean spoofDetected = false;

    // Счётчики нарушений (Violation Levels)
    private int speedVL = 0;
    private int flyVL = 0;
    private int reachVL = 0;
    private int killauraVL = 0;

    // Данные перемещений
    private Location lastLocation;
    private long lastMoveTime = System.currentTimeMillis();
    private int airTicks = 0;

    // Данные боя
    private long lastAttackTime = 0;
    private int attacksThisSecond = 0;
    private long lastSecondWindow = System.currentTimeMillis();

    public PlayerProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getClientBrand() {
        return clientBrand;
    }

    public void setClientBrand(String clientBrand) {
        this.clientBrand = clientBrand;
    }

    public boolean isSpoofDetected() {
        return spoofDetected;
    }

    public void setSpoofDetected(boolean spoofDetected) {
        this.spoofDetected = spoofDetected;
    }

    public int getSpeedVL() {
        return speedVL;
    }

    public void addSpeedVL() {
        this.speedVL++;
    }

    public int getFlyVL() {
        return flyVL;
    }

    public void addFlyVL() {
        this.flyVL++;
    }

    public int getReachVL() {
        return reachVL;
    }

    public void addReachVL() {
        this.reachVL++;
    }

    public int getKillauraVL() {
        return killauraVL;
    }

    public void addKillauraVL() {
        this.killauraVL++;
    }

    public int getTotalVL() {
        return speedVL + flyVL + reachVL + killauraVL;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public void incrementAirTicks() {
        this.airTicks++;
    }

    public void resetAirTicks() {
        this.airTicks = 0;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void recordAttack() {
        long now = System.currentTimeMillis();
        this.lastAttackTime = now;
        if (now - lastSecondWindow > 1000) {
            attacksThisSecond = 1;
            lastSecondWindow = now;
        } else {
            attacksThisSecond++;
        }
    }

    public int getAttacksThisSecond() {
        return attacksThisSecond;
    }
}
