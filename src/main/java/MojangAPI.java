import me.kbrewster.exceptions.APIException;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

/**
 * Caches all requests fulfilled by KevinPriv's
 * MojangAPI class
 */
class MojangAPI {

    private HashMap<String, TimestampedUUID> uuidHashMap = new HashMap<>();
    private HashMap<UUID, TimestampedUsername> usernameHashMap = new HashMap<>();
    private int cacheExpiryInSeconds = 0; // 1 week caching by default

    /**
     * Constructor checks config and sets up caching accordingly
     * @param plugin Plugin used to check configs
     */
    MojangAPI(EagleEyePlugin plugin) {
        if (plugin.getConfig().getBoolean("Caching.enabled"))
            this.cacheExpiryInSeconds = plugin.getConfig().getInt("Caching.expiryInMinutes");
    }

    /**
     * Attempts to get UUID from cache if available, else
     * sends a request to Mojang API
     * @param username Unique Minecraft username
     * @return UUID of username
     * @throws IOException Connection related error
     * @throws APIException API threw error
     */
    UUID getUUID(String username) throws IOException, APIException {

        // Check cache if enabled
        TimestampedUUID timestampedUUID;
        if (cacheExpiryInSeconds != 0 && (timestampedUUID = uuidHashMap.get(username)) != null) {
            if (!timestampedUUID.hasExpired(cacheExpiryInSeconds))
                return timestampedUUID.getUUID(); // Valid cache exists
            else
                uuidHashMap.remove(username); // Remove expired cache entry
        }

        // Request from Mojang
        UUID uuid = me.kbrewster.mojangapi.MojangAPI.getUUID(username);
        uuidHashMap.put(username, new TimestampedUUID(uuid));
        return uuid;

    }

    /**
     * Attempts to get username from cache if available, else
     * sends a request to Mojang API
     * @param uuid Unique Minecraft UUID
     * @return Username associated with UUID
     * @throws IOException Connection related error
     * @throws APIException API threw error
     */
    String getName(UUID uuid) throws IOException, APIException {

        // Cache cache if enabled
        TimestampedUsername timestampedUsername;
        if (cacheExpiryInSeconds != 0 && (timestampedUsername = usernameHashMap.get(uuid)) != null) {
            if (!timestampedUsername.hasExpired(cacheExpiryInSeconds))
                return timestampedUsername.getUsername(); // Valid cache exists
            else
                usernameHashMap.remove(uuid); // Remove expired cache entry
        }

        // Request from Mojang
        String username = me.kbrewster.mojangapi.MojangAPI.getUsername(uuid);
        usernameHashMap.put(uuid, new TimestampedUsername(username));
        return username;

    }

    /**
     * Used to manually cache a player
     * @param username Username of player
     * @param uuid UUID of player
     */
    void cache(String username, UUID uuid) {

        // If caching disabled, return immediately
        if (cacheExpiryInSeconds == 0) return;

        // Cache username and uuid
        uuidHashMap.put(username, new TimestampedUUID(uuid));
        usernameHashMap.put(uuid, new TimestampedUsername(username));

    }

}

/**
 * Username with an associated timestamp created on construction
 */
class TimestampedUsername {

    private final Timestamp cachedAt;
    private final String username;

    /**
     * Constructor associated timestamp of current time to username
     * @param username Username to cache
     */
    TimestampedUsername(String username) {
        this.cachedAt = Timestamp.from(Instant.now());
        this.username = username;
    }

    /**
     * States whether the cached response is expired
     * @param cacheExpiryInSeconds Expiry time
     * @return True if expired and vice versa
     */
    boolean hasExpired(int cacheExpiryInSeconds) {
        Timestamp now = Timestamp.from(Instant.now());
        return (now.getTime() - (cacheExpiryInSeconds*1000)) > (cachedAt.getTime());
    }

    /**
     * Gets stored value
     * @return Username
     */
    String getUsername() { return username; }

    /**
     * Formatted data for debugging
     * @return Formatted string representing instance
     */
    @Override
    public String toString() {
        return String.format("[\"username\": \"%s\", \"expiry\": \"%s\"]", username, cachedAt.getTime());
    }

}

/**
 * UUID with an associated timestamp created on construction
 */
class TimestampedUUID {

    private final Timestamp cachedAt;
    private final UUID uuid;

    /**
     * Constructor associated timestamp of current time to username
     * @param uuid UUID to cache
     */
    TimestampedUUID(UUID uuid) {
        this.cachedAt = Timestamp.from(Instant.now());
        this.uuid = uuid;
    }

    /**
     * States whether the cached response is expired
     * @param cacheExpiryInSeconds Expiry time
     * @return True if expired and vice versa
     */
    boolean hasExpired(int cacheExpiryInSeconds) {
        Timestamp now = Timestamp.from(Instant.now());
        return (now.getTime() - (cacheExpiryInSeconds*1000)) > (cachedAt.getTime());
    }

    /**
     * Gets stored value
     * @return UUID
     */
    UUID getUUID() { return uuid; }

    /**
     * Formatted data for debugging
     * @return Formatted string representing instance
     */
    @Override
    public String toString() {
        return String.format("[\"uuid\": \"%s\", \"expiry\": \"%s\"]", uuid.toString(), cachedAt.getTime());
    }

}
