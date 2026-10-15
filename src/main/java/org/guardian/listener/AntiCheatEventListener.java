package org.guardian.listener;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.guardian.GuardianAntiCheat;
import org.guardian.check.CheckEngine;
import org.guardian.data.PlayerProfile;

import java.nio.charset.StandardCharsets;

public class AntiCheatEventListener implements Listener, PluginMessageListener {
    private final GuardianAntiCheat plugin;

    public AntiCheatEventListener(GuardianAntiCheat plugin) {
        this.plugin = plugin;
    }

    // Обработка данных бренда клиента (Anti-Spoof & Brand Inspector)
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player.getUniqueId(), player.getName());

        if ("minecraft:brand".equalsIgnoreCase(channel)) {
            try {
                // Извлекаем строку UTF-8 (первый байт часто длина строки в протоколе MC)
                int offset = 0;
                if (message.length > 0 && message[0] < 32) {
                    offset = 1;
                }
                String brand = new String(message, offset, message.length - offset, StandardCharsets.UTF_8).trim();
                profile.setClientBrand(brand);

                // Детект известных хакерских клиентов
                String lower = brand.toLowerCase();
                if (lower.contains("liquid") || lower.contains("wurst") || lower.contains("meteor") ||
                    lower.contains("aristois") || lower.contains("rusher") || lower.contains("future")) {
                    profile.setSpoofDetected(true);
                    player.getServer().broadcast("§8[§4§lALARM§8] §cВнимание: Игрок §e" + player.getName() +
                            " §cиспользует модифицированный чит-клиент: §f" + brand, "guardian.admin");
                }
            } catch (Exception ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getProfileManager().getOrCreate(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getProfileManager().remove(event.getPlayer().getUniqueId());
    }

    // 1. Проверка перемещений (Speed & Fly)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Игнорируем обычный поворот головы
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player.getUniqueId(), player.getName());

        // Проверка Fly
        boolean flyFlag = CheckEngine.checkFly(player, from, to, profile);
        if (flyFlag && profile.getFlyVL() > 3) {
            event.setTo(from); // Откат назад
            return;
        }

        // Проверка Speed
        boolean speedFlag = CheckEngine.checkSpeed(player, from, to, profile);
        if (speedFlag && profile.getSpeedVL() > 3) {
            event.setTo(from); // Откат назад
        }
    }

    // 2. Проверка боёвки (Reach & KillAura)
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        Entity target = event.getEntity();
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(attacker.getUniqueId(), attacker.getName());

        // Проверка Reach
        boolean reachFlag = CheckEngine.checkReach(attacker, target, profile);
        if (reachFlag) {
            event.setCancelled(true);
            return;
        }

        // Проверка KillAura
        boolean auraFlag = CheckEngine.checkKillAura(attacker, target, profile);
        if (auraFlag) {
            event.setCancelled(true);
        }
    }
}
