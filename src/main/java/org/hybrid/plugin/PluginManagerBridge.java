package org.hybrid.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Менеджер моста плагинов Leaf / Spigot / Paper.
 */
public class PluginManagerBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("PluginManager");
    private final File pluginFolder;

    public PluginManagerBridge(File pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    public void initialize() {
        LOGGER.info("[Leaf-Bridge] Загрузка подсистемы Bukkit API...");
        File[] files = pluginFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            LOGGER.info("[Leaf-Bridge] Плагины не обнаружены в папке plugins/.");
            return;
        }

        for (File pluginFile : files) {
            LOGGER.info("[Leaf-Bridge] Обнаружен плагин: " + pluginFile.getName() + " — подготовка внедрения...");
        }
    }
}
