package net.deanly.solana.sdk.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * A thread-safe Least Recently Used (LRU) cache implementation with additional support
 * for entry expiration and removal notification. This cache evicts the least recently accessed entries
 * when the size exceeds the specified maximum size or the entries expire due to inactivity.
 * The cache allows a listener to be registered for handling entry removal events.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class RemovalAwareLRUCache<K, V> {

    private final int maxSize;
    private final ConcurrentHashMap<K, V> cache;
    private final LinkedHashMap<K, Long> lruTracker;
    private final BiConsumer<K, RemovalCause> removalListener;
    private final long expireAfterAccessMillis;
    private final Executor listenerExecutor;

    /**
     * Constructs a RemovalAwareLRUCache with a specified maximum size, expiration policy,
     * and removal listener.
     *
     * @param maxSize the maximum number of elements the cache can hold; when the size is exceeded,
     *                the least recently used element is evicted
     * @param expireAfterAccessMillis the time in milliseconds after the last access
     *                                that a cache entry should expire
     * @param removalListener a listener that is triggered asynchronously when an entry is removed
     *                        from the cache due to expiration, size limit, or manual invalidation
     */
    public RemovalAwareLRUCache(int maxSize, long expireAfterAccessMillis, BiConsumer<K, RemovalCause> removalListener) {
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>(maxSize);
        this.lruTracker = new LinkedHashMap<>(maxSize, 0.75f, true);
        this.expireAfterAccessMillis = expireAfterAccessMillis;
        this.removalListener = removalListener;
        this.listenerExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieves the value associated with the specified key from the cache.
     * If the key is not present in the cache or the entry has expired, this method returns null.
     * The access time for the key is updated if the key exists and is not expired.
     *
     * @param key the key whose associated value is to be returned; must not be null
     * @return the value associated with the key if it is present in the cache and not expired;
     *         null if the key is absent or expired
     */
    public V get(K key) {
        synchronized (lruTracker) {
            if (!cache.containsKey(key)) {
                return null;
            }

            long lastAccess = lruTracker.getOrDefault(key, 0L);
            if (isExpired(lastAccess)) {
                remove(key, RemovalCause.EXPIRED);
                return null;
            }

            // Update access time
            lruTracker.put(key, System.currentTimeMillis());
            return cache.get(key);
        }
    }

    /**
     * Puts the specified key-value pair into the cache. If the cache has reached its maximum size,
     * the least recently used entry is removed to make room for the new entry. Updates the last access
     * time for the given key.
     *
     * @param key the key to associate with the specified value; must not be null
     * @param value the value to be associated with the specified key; can be null
     */
    public void put(K key, V value) {
        synchronized (lruTracker) {
            if (cache.size() >= maxSize) {
                removeEldest();
            }
            cache.put(key, value);
            lruTracker.put(key, System.currentTimeMillis());
        }
    }

    /**
     * Invalidates the cache entry associated with the specified key, removing it from the cache.
     * This method triggers the removal listener with the cause {@link RemovalCause#MANUAL}.
     *
     * @param key the key whose cache entry is to be invalidated; must not be null
     */
    public void invalidate(K key) {
        synchronized (lruTracker) {
            remove(key, RemovalCause.MANUAL);
        }
    }

    /**
     * Removes all entries from the cache. This method iterates through all keys
     * currently stored in the cache and triggers their removal with the cause
     * {@link RemovalCause#MANUAL}. The removal listener, if configured, will be
     * notified for each removed entry.
     * <p>
     * This operation is thread-safe as it synchronizes on the internal tracker
     * to avoid concurrent modifications while clearing the cache.
     */
    public void clear() {
        synchronized (lruTracker) {
            for (K key : cache.keySet()) {
                remove(key, RemovalCause.MANUAL);
            }
        }
    }

    /**
     * Returns the current number of elements in the cache.
     *
     * @return the number of elements currently stored in the cache
     */
    public int size() {
        return cache.size();
    }

    /**
     * Determines whether a cache entry has expired based on its last access time.
     *
     * @param lastAccessTime the timestamp of the last access to the cache entry, in milliseconds
     * @return true if the cache entry is expired; false otherwise
     */
    private boolean isExpired(long lastAccessTime) {
        return (System.currentTimeMillis() - lastAccessTime) > expireAfterAccessMillis;
    }

    /**
     * Removes the eldest entry from the cache based on the least recently used (LRU) policy.
     * This method identifies the eldest entry by retrieving the first key from the
     * internal LRU tracker and then delegates the removal logic to the `remove` method.
     * If the eldest key is found, it is removed with a removal cause of {@link RemovalCause#SIZE}.
     * <p>
     * This operation is thread-safe as it synchronizes access to the LRU tracker to prevent
     * concurrent modifications during the removal process.
     */
    private void removeEldest() {
        K eldestKey = null;
        synchronized (lruTracker) {
            if (!lruTracker.isEmpty()) {
                eldestKey = lruTracker.keySet().iterator().next();
            }
        }
        if (eldestKey != null) {
            remove(eldestKey, RemovalCause.SIZE);
        }
    }

    /**
     * Removes the specified key from the cache and notifies the removal listener with the given cause.
     * This operation is thread-safe and ensures the key is removed from both the internal cache and
     * the LRU tracker. If the key is successfully removed, the removal listener is invoked asynchronously.
     *
     * @param key the key to be removed from the cache; must not be null
     * @param cause the reason for the removal, represented by the {@link RemovalCause} enum; must not be null
     */
    private void remove(K key, RemovalCause cause) {
        synchronized (lruTracker) {
            if (cache.containsKey(key)) {
                cache.remove(key);
                lruTracker.remove(key);

                // Invoke removal listener asynchronously for better performance
                listenerExecutor.execute(() -> removalListener.accept(key, cause));
            }
        }
    }

    /**
     * Enum representing the possible reasons for the removal of entries in a cache.
     * <p>
     * This enum is used in conjunction with the {@code RemovalAwareLRUCache} to describe
     * why a specific entry is removed. The removal cause can be one of the following:
     * <p>
     * - {@code EXPIRED}: Indicates that the entry was removed because it exceeded its
     *   expiration time based on the cache's expiration policy.
     * <p>
     * - {@code SIZE}: Indicates that the entry was removed because the cache reached
     *   its maximum size, and a removal was necessary to make space for new entries.
     * <p>
     * - {@code MANUAL}: Indicates that the entry was explicitly removed by a manual
     *   action, such as invalidation or a clear operation, performed on the cache.
     */
    public enum RemovalCause {
        EXPIRED, SIZE, MANUAL
    }

    /**
     * Provides an unmodifiable view of the cache as a map. This map represents the current
     * contents of the cache and reflects any changes made to the cache. Modifications
     * to the returned map are not allowed.
     *
     * @return an unmodifiable map view of the cache's current contents
     */
    public Map<K, V> asMap() {
        return Collections.unmodifiableMap(cache);
    }

    /**
     * Retrieves the value associated with the given key if it is present in the cache.
     * This method does not modify or affect the access order of the cache.
     *
     * @param key the key whose associated value is to be returned, must not be null
     * @return the value associated with the key if it is present in the cache;
     *         null if the key is not in the cache or*/
    public V getIfPresent(K key) {
        return cache.get(key);
    }
}