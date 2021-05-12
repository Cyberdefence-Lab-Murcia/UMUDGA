/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import es.um.dga.features.utils.Locks;

public class EnglishCache {
    
    private static Map<Integer, EnglishDistribution> ENGLISH_CACHE = new HashMap<>();
    
    private static Map<Integer, EnglishEmptyDistribution> ENGLISH_EMPTY_CACHE = new HashMap<>();
    
    /**
     * Retrieves from cache (or recomputes) the requested distribution.
     * @param nGramSize nGram size.
     * @return Requested nGram distribution.
     */
    public static EnglishDistribution getEnglishDistribution(Integer nGramSize) {
        synchronized (Locks.ENGLISH_CACHE) {
            if (!ENGLISH_CACHE.containsKey(nGramSize)) {
                loadDistribution(nGramSize);
            }
            return ENGLISH_CACHE.get(nGramSize);
        }
    }
    
    /**
     * Loads and caches the default distributions for the requested nGramSize.
     * @param nGramSize nGram size.
     */
    private static void loadDistribution(Integer nGramSize) {
        synchronized (Locks.ENGLISH_CACHE) {
            ENGLISH_CACHE.put(nGramSize, EnglishDistribution.loadDistribution(nGramSize));
            ENGLISH_EMPTY_CACHE.put(nGramSize, EnglishEmptyDistribution.loadDistribution(nGramSize));
        }
    }
    
    /**
     * Retrieves from cache (or recomputes) the requested empty distribution.
     * @param nGramSize nGram size.
     * @return Requested nGram empty distribution.
     */
    public static EnglishEmptyDistribution getEnglishEmptyDistribution(Integer nGramSize) {
        synchronized (Locks.ENGLISH_CACHE) {
            if (!ENGLISH_EMPTY_CACHE.containsKey(nGramSize)) {
                loadDistribution(nGramSize);
            }
            return ENGLISH_EMPTY_CACHE.get(nGramSize);
        }
    }
    
    /**
     * Reset all the static objects and calls the Garbage Collector.
     */
    public static void reset() {
        ENGLISH_CACHE = new TreeMap<>();
        ENGLISH_EMPTY_CACHE = new TreeMap<>();
        System.gc();
    }
}
