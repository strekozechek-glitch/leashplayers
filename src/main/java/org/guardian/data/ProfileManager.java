package org.guardian.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProfileManager {
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public PlayerProfile getOrCreate(UUID uuid, String name) {
        return profiles.computeIfAbsent(uuid, id -> new PlayerProfile(id, name));
    }

    public PlayerProfile get(UUID uuid) {
        return profiles.get(uuid);
    }

    public void remove(UUID uuid) {
        profiles.remove(uuid);
    }
}
