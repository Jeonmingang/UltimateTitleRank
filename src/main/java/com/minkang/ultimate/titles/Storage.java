
package com.minkang.ultimate.titles;

import java.util.UUID;

public interface Storage {
    PlayerData get(UUID uuid);
    void save(UUID uuid, PlayerData data);
    void flush();
}
