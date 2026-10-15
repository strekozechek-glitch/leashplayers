package org.guardian.check;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.guardian.data.PlayerProfile;

/**
 * Модуль эвристических проверок античита
 */
public class CheckEngine {

    // 1. ПРОВЕРКА REACH (Дистанция удара)
    // Ванильный лимит в выживании - 3.0 блока (3.5 c запасом пинга). Свыше 4.0 - 100% Reach.
    public static boolean checkReach(Player player, Entity target, PlayerProfile profile) {
        if (player.getGameMode() == GameMode.CREATIVE) return false;

        Location eyeLoc = player.getEyeLocation();
        Location targetLoc = target.getLocation();
        double dist = eyeLoc.distance(targetLoc);

        // 3.85 - строгий лимит с учётом хитбоксов и лагов
        if (dist > 3.85) {
            profile.addReachVL();
            alertStaff(player, "Reach", String.format("Дистанция: %.2f (Лимит: 3.85)", dist), profile.getReachVL());
            return true;
        }
        return false;
    }

    // 2. ПРОВЕРКА KILL-AURA (Угол атаки Raytrace + CPS Spikes)
    public static boolean checkKillAura(Player player, Entity target, PlayerProfile profile) {
        if (player.getGameMode() == GameMode.CREATIVE) return false;

        profile.recordAttack();

        // Проверка 1: CPS / Скорость атак (более 16 атак в секунду в 1.9+ боевой системе невозможно ванильно)
        if (profile.getAttacksThisSecond() > 16) {
            profile.addKillauraVL();
            alertStaff(player, "KillAura (CPS)", "Атак в секунду: " + profile.getAttacksThisSecond(), profile.getKillauraVL());
            return true;
        }

        // Проверка 2: Угол взгляда (Angle / Field of View Check)
        // Вектор от игрока к цели
        Location eye = player.getEyeLocation();
        double toTargetX = target.getLocation().getX() - eye.getX();
        double toTargetZ = target.getLocation().getZ() - eye.getZ();

        // Направление взгляда игрока
        double dirX = eye.getDirection().getX();
        double dirZ = eye.getDirection().getZ();

        // Нормализация и скалярное произведение
        double mag1 = Math.sqrt(toTargetX * toTargetX + toTargetZ * toTargetZ);
        double mag2 = Math.sqrt(dirX * dirX + dirZ * dirZ);

        if (mag1 > 0.01 && mag2 > 0.01) {
            double dot = (toTargetX * dirX + toTargetZ * dirZ) / (mag1 * mag2);
            // Если игрок бьет цель за спиной (угол больше 90 градусов, dot < 0)
            if (dot < -0.15) {
                profile.addKillauraVL();
                alertStaff(player, "KillAura (Angle)", "Удар без наведения взгляда на цель", profile.getKillauraVL());
                return true;
            }
        }

        return false;
    }

    // 3. ПРОВЕРКА SPEED (Горизонтальная скорость)
    public static boolean checkSpeed(Player player, Location from, Location to, PlayerProfile profile) {
        if (player.isFlying() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        if (player.isInsideVehicle() || player.isGliding()) {
            return false;
        }

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horizontalDistSq = (dx * dx) + (dz * dz);

        // Максимальная ванильная скорость бега с прыжком без эффектов ~ 0.36 б/тик (0.13 distSq)
        // С эффектом Скорость II ~ 0.55 б/тик (0.30 distSq)
        double maxDistSq = 0.50; // Базовый порог для 1 тика (с запасом на рассинхрон)
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            maxDistSq = 0.85;
        }

        if (horizontalDistSq > maxDistSq && horizontalDistSq < 100.0) { // Исключаем обычный телепорт
            profile.addSpeedVL();
            alertStaff(player, "Speed", String.format("Смещение: %.2f (Максимум: %.2f)", Math.sqrt(horizontalDistSq), Math.sqrt(maxDistSq)), profile.getSpeedVL());
            return true;
        }

        return false;
    }

    // 4. ПРОВЕРКА FLY (Зависание в воздухе / Ascension)
    public static boolean checkFly(Player player, Location from, Location to, PlayerProfile profile) {
        if (player.isFlying() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            profile.resetAirTicks();
            return false;
        }
        if (player.isInsideVehicle() || player.isGliding()) {
            profile.resetAirTicks();
            return false;
        }

        // Проверяем, находится ли игрок в воздухе
        Block blockBelow = to.clone().subtract(0, 0.1, 0).getBlock();
        Block blockAt = to.getBlock();

        boolean inAir = (blockBelow.getType() == Material.AIR || blockBelow.isPassable())
                && !blockAt.isLiquid() && !blockBelow.isLiquid();

        if (inAir) {
            profile.incrementAirTicks();

            // Если игрок парит без падения (dy = 0 или dy > 0 без прыжков) более 20 тиков (1 сек)
            double dy = to.getY() - from.getY();
            if (profile.getAirTicks() > 18 && (dy >= -0.05 && dy <= 0.05)) {
                profile.addFlyVL();
                alertStaff(player, "Fly (Hover)", "Зависание в воздухе (" + profile.getAirTicks() + " тиков)", profile.getFlyVL());
                return true;
            }

            // Прыжки в воздух без блоков под ногами (Ascension Fly)
            if (profile.getAirTicks() > 15 && dy > 0.42) {
                profile.addFlyVL();
                alertStaff(player, "Fly (Ascension)", "Набор высоты в воздухе", profile.getFlyVL());
                return true;
            }
        } else {
            profile.resetAirTicks();
        }

        return false;
    }

    private static void alertStaff(Player player, String check, String details, int vl) {
        String msg = String.format("§8[§c§lGuardian§4§lAC§8] §e%s §7провалил проверку §c%s §8(VL: %d) §7- §f%s",
                player.getName(), check, vl, details);
        Bukkit.broadcast(msg, "guardian.admin");
    }
}
