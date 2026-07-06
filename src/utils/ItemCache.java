package utils;

import model.LibraryItem;

/**
 * Fixed-size array cache for most frequently accessed library items.
 * Uses a plain array (not ArrayList) as required — evicts the least-accessed slot when full.
 */
public class ItemCache {
    private static final int CACHE_SIZE = 10;
    private final LibraryItem[] cache;      // fixed-size array slot for each cached item
    private final int[] accessCounts;       // parallel array tracking hit counts

    public ItemCache() {
        this.cache = new LibraryItem[CACHE_SIZE];
        this.accessCounts = new int[CACHE_SIZE];
    }

    /** Records a view/search access; increments count if item already cached. */
    public void recordAccess(LibraryItem item) {
        // Check if item is already in the cache — just bump its counter
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cache[i] != null && cache[i].getId().equals(item.getId())) {
                accessCounts[i]++;
                return;
            }
        }

        // Cache miss — evict the slot with the lowest access count (LFU replacement)
        int minIndex = 0;
        for (int i = 1; i < CACHE_SIZE; i++) {
            if (accessCounts[i] < accessCounts[minIndex]) {
                minIndex = i;
            }
        }

        cache[minIndex] = item;
        accessCounts[minIndex] = 1;
    }

    /** Returns items ranked by access count (highest first). */
    public LibraryItem[] getMostFrequentItems() {
        LibraryItem[] result = new LibraryItem[CACHE_SIZE];
        int[] countsCopy = accessCounts.clone();
        LibraryItem[] cacheCopy = cache.clone();

        for (int i = 0; i < CACHE_SIZE; i++) {
            int maxIndex = -1;
            int maxCount = -1;
            for (int j = 0; j < CACHE_SIZE; j++) {
                if (cacheCopy[j] != null && countsCopy[j] > maxCount) {
                    maxCount = countsCopy[j];
                    maxIndex = j;
                }
            }
            if (maxIndex >= 0) {
                result[i] = cacheCopy[maxIndex];
                countsCopy[maxIndex] = -1;
            }
        }
        return result;
    }

    public String getCacheReport() {
        StringBuilder sb = new StringBuilder("Most Frequently Accessed Items:\n");
        LibraryItem[] frequent = getMostFrequentItems();
        int rank = 1;
        for (LibraryItem item : frequent) {
            if (item != null) {
                sb.append(String.format("  %d. %s - %s (%d accesses)\n",
                        rank++, item.getTitle(), item.getItemType(), getAccessCount(item)));
            }
        }
        if (rank == 1) {
            sb.append("  (No items accessed yet)\n");
        }
        return sb.toString();
    }

    private int getAccessCount(LibraryItem item) {
        for (int i = 0; i < CACHE_SIZE; i++) {
            if (cache[i] != null && cache[i].getId().equals(item.getId())) {
                return accessCounts[i];
            }
        }
        return 0;
    }

    public void clear() {
        for (int i = 0; i < CACHE_SIZE; i++) {
            cache[i] = null;
            accessCounts[i] = 0;
        }
    }
}
