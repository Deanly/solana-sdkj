package net.deanly.solana.sdk.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RemovalAwareLRUCacheTest {

    private RemovalAwareLRUCache<String, String> cache;
    private AtomicInteger removalCount;

    @BeforeEach
    void setUp() {
        removalCount = new AtomicInteger(0);
        cache = new RemovalAwareLRUCache<>(
                3, // 최대 크기
                5000, // 5000ms 만료 시간
                (key, cause) -> {
                    System.out.println("Removed Key: " + key + ", Cause: " + cause);
                    removalCount.incrementAndGet();
                }
        );
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));

        // 존재하지 않는 키 확인
        assertNull(cache.get("key3"), "Should return null for missing keys");
    }

    @Test
    void testLRUPolicy() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // LRU 동작 테스트: key1에 접근
        cache.get("key1");

        // 캐시가 꽉 찼을 때 key2를 제거해야 함 (LRU 순서: key1, key3 => key4 추가)
        cache.put("key4", "value4");

        assertNull(cache.get("key2"), "Key2 should have been evicted by LRU policy");
        assertEquals("value1", cache.get("key1"));  // 접근된 key1은 유지되어야 함
        assertEquals("value3", cache.get("key3"));
        assertEquals("value4", cache.get("key4"));
    }

    @Test
    void testExpiration() throws InterruptedException {
        AtomicInteger removalCount = new AtomicInteger(0);
        RemovalAwareLRUCache<String, String> cache = new RemovalAwareLRUCache<>(
                3, // 최대 크기
                300, // 300ms 만료 시간
                (key, cause) -> {
                    System.out.println("Removed Key: " + key + ", Cause: " + cause);
                    removalCount.incrementAndGet();
                }
        );
        cache.put("key1", "value1");
        Thread.sleep(310); // 6초 대기 (5000ms 만료 시간 초과)

        assertNull(cache.get("key1"), "Key1 should have been expired");
    }

    @Test
    void testManualInvalidation() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        cache.invalidate("key1");
        assertNull(cache.get("key1"), "Key1 should have been removed manually");

        assertEquals("value2", cache.get("key2"), "Key2 should still exist");
    }

    @Test
    void testClear() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        cache.clear();

        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
        assertNull(cache.get("key3"));
        assertEquals(0, cache.size(), "Cache size should be 0 after clear");
    }

    @Test
    void testRemovalListener() throws InterruptedException {
        // CountDownLatch를 사용해 Listener가 처리 완료될 때까지 대기
        CountDownLatch latch = new CountDownLatch(1);

        // 캐시 인스턴스 재생성하여 Latch 활용
        RemovalAwareLRUCache<String, String> cache = new RemovalAwareLRUCache<>(
                3,
                5000,
                (key, cause) -> {
                    System.out.println("Removed Key: " + key + ", Cause: " + cause);
                    removalCount.incrementAndGet();
                    latch.countDown(); // Listener 실행 시 Latch 감소
                }
        );

        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // 캐시 제거 유발
        cache.put("key4", "value4"); // LRU에 따라 key1 제거

        // Listener의 실행을 최대 2초 대기
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "RemovalListener did not execute in time");

        // Listener 호출 횟수 검증
        assertEquals(1, removalCount.get(), "Exactly one listener call should have been made");
    }

    @Test
    void testConcurrency() throws InterruptedException {
        RemovalAwareLRUCache<Integer, String> concurrentCache = new RemovalAwareLRUCache<>(
                100,
                5000,
                (key, cause) -> removalCount.incrementAndGet()
        );

        int threads = 10;
        int itemsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < itemsPerThread; j++) {
                    int key = threadId * itemsPerThread + j;
                    concurrentCache.put(key, "value" + key);

                    // 동기화 또는 간단한 처리 지연으로 순서 안정화
                    try {
                        Thread.sleep(1); // 경합 완화
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    assertEquals("value" + key, concurrentCache.get(key));
                }
                latch.countDown();
            }).start();
        }

        if (!latch.await(20, TimeUnit.SECONDS)) { // 타임아웃 증가
            System.out.println("Concurrency test timed out. Cache Content: " + concurrentCache.asMap());
            fail("Concurrency test timed out");
        }

        // LRU 크기 확인
        assertTrue(concurrentCache.size() <= 100, "Cache size should respect max size");
    }

    @Test
    void testLRUCacheInsertAndEviction() {
        RemovalAwareLRUCache<Integer, String> lruCache = new RemovalAwareLRUCache<>(3, 5000, (key, cause) -> {});

        lruCache.put(1, "A");
        lruCache.put(2, "B");
        lruCache.put(3, "C");

        assertEquals("A", lruCache.getIfPresent(1));
        assertEquals("B", lruCache.getIfPresent(2));
        assertEquals("C", lruCache.getIfPresent(3));

        lruCache.put(4, "D"); // LRU eviction: "A" should be evicted

        assertNull(lruCache.getIfPresent(1)); // "1:A" is evicted
        assertEquals("B", lruCache.getIfPresent(2)); // "2:B" is still present
        assertEquals("C", lruCache.getIfPresent(3)); // "3:C" is still present
        assertEquals("D", lruCache.getIfPresent(4)); // "4:D" is present

        lruCache.get(2); // Access "2:B"

        lruCache.put(5, "E");

        assertNull(lruCache.getIfPresent(3)); // "C" is evicted
        assertEquals("B", lruCache.getIfPresent(2)); // "B" is kept
        assertEquals("D", lruCache.getIfPresent(4)); // "D" is kept
        assertEquals("E", lruCache.getIfPresent(5)); // "E" is added
    }

    @Test
    void testConcurrentAccessAndEviction() throws InterruptedException {
        RemovalAwareLRUCache<Integer, String> lruCache = new RemovalAwareLRUCache<>(5, 5000, (key, cause) -> {});
        int threads = 10;
        int itemsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            new Thread(() -> {
                for (int j = 0; j < itemsPerThread; j++) {
                    int key = threadId * itemsPerThread + j;
                    lruCache.put(key, "value" + key);

                    // Ensure the value can be retrieved
                    assertEquals("value" + key, lruCache.getIfPresent(key));
                }
                latch.countDown();
            }).start();
        }

        latch.await(10, TimeUnit.SECONDS);

        // Check LRU size limit is respected
        assertTrue(lruCache.size() <= 5);

        // Ensure entries were evicted correctly
        System.out.println("Cache contents after test: " + lruCache.asMap());
    }

}
