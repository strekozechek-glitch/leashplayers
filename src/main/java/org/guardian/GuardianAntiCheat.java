package org.guardian;

import org.bukkit.plugin.java.JavaPlugin;
import org.guardian.command.GuardianCommandExecutor;
import org.guardian.data.ProfileManager;
import org.guardian.listener.AntiCheatEventListener;

public class GuardianAntiCheat extends JavaPlugin {
    private ProfileManager profileManager;

    @Override
    public void onEnable() {
        this.profileManager = new ProfileManager();

        AntiCheatEventListener listener = new AntiCheatEventListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        // Регистрация каналов сообщений от клиента
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", listener);
        getServer().getMessenger().registerIncomingPluginChannel(this, "fml:handshake", listener);

        if (getCommand("guardian") != null) {
            getCommand("guardian").setExecutor(new GuardianCommandExecutor(this));
        }

        getLogger().info("[GuardianAC] Полный комплекс проверок активен: KillAura, Reach, Speed, Fly, Brand Detector & AntiSpoof!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[GuardianAC] Выключен.");
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
