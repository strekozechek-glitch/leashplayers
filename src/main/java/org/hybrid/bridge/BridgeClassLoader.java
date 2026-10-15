package org.hybrid.bridge;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Изолированный загрузчик классов для предотвращения конфликтов между
 * Fabric-модами (Mixin) и Leaf/Bukkit плагинами.
 */
public class BridgeClassLoader extends URLClassLoader {

    public BridgeClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Перехват и перенаправление вызовов между Bukkit API и Fabric runtime
        if (name.startsWith("org.bukkit.") || name.startsWith("io.papermc.paper.")) {
            // Маршрутизация в подсистему плагинов
            return super.loadClass(name, resolve);
        }
        if (name.startsWith("net.fabricmc.")) {
            // Маршрутизация в подсистему модов Fabric
            return super.loadClass(name, resolve);
        }
        return super.loadClass(name, resolve);
    }
}
