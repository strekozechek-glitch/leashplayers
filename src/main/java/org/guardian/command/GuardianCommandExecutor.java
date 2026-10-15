package org.guardian.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.guardian.GuardianAntiCheat;
import org.guardian.data.PlayerProfile;

public class GuardianCommandExecutor implements CommandExecutor {
    private final GuardianAntiCheat plugin;

    public GuardianCommandExecutor(GuardianAntiCheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("guardian.admin")) {
            sender.sendMessage("§cУ вас нет прав на использование Guardian AntiCheat.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§8================ §c§lGUARDIAN AC §8================");
            sender.sendMessage("§e/guardian info <ник> §7- полная диагностика клиента и читов");
            sender.sendMessage("§8================================================");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cИгрок " + args[1] + " не найден в сети.");
            return true;
        }

        PlayerProfile profile = plugin.getProfileManager().getOrCreate(target.getUniqueId(), target.getName());

        sender.sendMessage("§8================ §c§lДИАГНОСТИКА: §e" + target.getName() + " §8================");
        sender.sendMessage("§7[•] Клиент (Brand): §a" + profile.getClientBrand());
        sender.sendMessage("§7[•] Пинг: §f" + target.getPing() + "ms");
        sender.sendMessage("§7[•] Anti-Spoofer: " + (profile.isSpoofDetected() ? "§c§lОБНАРУЖЕН СПУФИНГ / ЧИТ" : "§2ОК (Валиден)"));
        sender.sendMessage("§7[•] Нарушения Speed VL: §f" + profile.getSpeedVL());
        sender.sendMessage("§7[•] Нарушения Fly VL: §f" + profile.getFlyVL());
        sender.sendMessage("§7[•] Нарушения Reach VL: §f" + profile.getReachVL());
        sender.sendMessage("§7[•] Нарушения KillAura VL: §f" + profile.getKillauraVL());
        sender.sendMessage("§7[•] Всего нарушений: §6" + profile.getTotalVL());
        sender.sendMessage("§8========================================================");

        return true;
    }
}
