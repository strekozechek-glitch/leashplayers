# Hybrid Core: Leaf + Fabric (Minecraft 26.2)

Гибридное серверное ядро для **Minecraft Java Edition 26.2**, объединяющее:
* **Fabric Loader**: поддержку модов (Mixin, Yarn mappings, папка `mods/`).
* **Leaf Engine**: оптимизации тиков/чанков Paper/Leaf и загрузку плагинов Spigot/Paper/Leaf (папка `plugins/`).

---

## Структура ядра

```text
├── build.gradle                               # Сборка проекта
├── settings.gradle
└── src/main/java/org/hybrid/
    ├── server/HybridServerMain.java           # Точка входа сервера 26.2
    ├── bridge/BridgeClassLoader.java          # Изолированный загрузчик классов (Mixin ↔ Paper)
    └── plugin/PluginManagerBridge.java        # Мост для загрузки Bukkit/Leaf плагинов
```

---

## Архитектурные особенности

1. **Dual-Runtime Bridge:**
   Ядро разделяет пространства классов модов и плагинов с помощью `BridgeClassLoader`, перенаправляя вызовы API и предотвращая краши при модификации байткода.
2. **Параллельная загрузка:**
   Сервер инициализирует Fabric Loader на уровне ванильного жизненного цикла сервера и параллельно поднимает слой Bukkit/Paper API для плагинов.

---

## Сборка ядра

Для сборки используйте Gradle (требуется Java 21+):

```bash
gradle build
```

Собранный исполняемый `.jar` будет находиться в:
`build/libs/leaf-fabric-hybrid-26.2-ALPHA.jar`

## Запуск

```bash
java -jar build/libs/leaf-fabric-hybrid-26.2-ALPHA.jar --nogui
```
