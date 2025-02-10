package net.deanly.solana.sdk.types.guava;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class UnsignedLongCacheTest {

    private static int cacheHits = 0;
    private static int cacheMisses = 0;

    public void testCacheHitRate(int cacheMax, int testIterations, int requestRange) {
        // 캐시 초기화
        UnsignedLong[] cache = new UnsignedLong[cacheMax];
        for (int i = 0; i < cacheMax; i++) {
            cache[i] = new UnsignedLong(i);
        }

        cacheHits = 0;
        cacheMisses = 0;

        // 랜덤한 값 사용
        Random random = new Random();
        for (int i = 0; i < testIterations; i++) {
            long value = random.nextInt(requestRange);
            fromCache(value, cache);
        }

        // 결과 출력
        System.out.println("Cache Size: " + cacheMax);
        System.out.println("Request Range: 0 ~ " + (requestRange - 1));
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.printf("Hit Rate: %.2f%%\n", (cacheHits * 100.0) / (cacheHits + cacheMisses));
        System.out.println("=================================");
    }

    public UnsignedLong fromCache(long value, UnsignedLong[] cache) {
        if (value >= 0 && value < cache.length) {
            cacheHits++;
            return cache[(int) value];
        }
        cacheMisses++;
        return new UnsignedLong(value);
    }

    @Test
    public void testCacheUnderVariousConditions() {
        testCacheHitRate(256, 1_000_000, 10000); // 6KB
        testCacheHitRate(512, 1_000_000, 10000); // 12KB
        testCacheHitRate(1024, 1_000_000, 10000); // 24KB
        testCacheHitRate(2048, 1_000_000, 10000); // 48KB
        testCacheHitRate(4096, 1_000_000, 10000); // 96KB
        testCacheHitRate(8192, 1_000_000, 10000); // 192KB
    }
}