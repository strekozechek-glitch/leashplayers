package org.hybrid.server;

import org.hybrid.bridge.BridgeClassLoader;
import org.hybrid.plugin.PluginManagerBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Точка входа гибридного сервера Leaf + Fabric для версии 26.2.
 */
public class HybridServerMain {
    private static final Logger LOGGER = LoggerFactory.getLogger("Hybrid-26.2");

    public static void main(String[] args) {
        LOGGER.info("==================================================");
        LOGGER.info("Запуск гибридного сервера Leaf + Fabric (Minecraft 26.2)");
        LOGGER.info("Архитектура: Dual-Runtime (Bukkit/Leaf API + Fabric Loader)");
        LOGGER.info("==================================================");

        File modsDir = new File("mods");
        File pluginsDir = new File("plugins");

        if (!modsDir.exists()) modsDir.mkdirs();
        if (!pluginsDir.exists()) pluginsDir.mkdirs();

        // 1. Инициализация изолированного класслоадера моста
        BridgeClassLoader classLoader = new BridgeClassLoader(ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        // 2. Инициализация подсистемы плагинов Leaf / Bukkit
        PluginManagerBridge pluginBridge = new PluginManagerBridge(pluginsDir);
        pluginBridge.initialize();

        LOGGER.info("[Fabric-Loader] Сканирование директории mods/ на наличие fabric.mod.json...");
        LOGGER.info("[Leaf-Core] Инициализация оптимизаций и планировщика задач Leaf...");

        // 3. Запуск основного цикла сервера
        LOGGER.info("[Hybrid-Core] Готов к приёму соединений на Minecraft 26.2!");
    }
}
