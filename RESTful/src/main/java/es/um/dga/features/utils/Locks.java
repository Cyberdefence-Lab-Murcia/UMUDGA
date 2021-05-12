/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.utils;

/**
 * Collection of static locks to be used in the app.
 */
public class Locks {
    
    /**
     * Settings Logger Lock.
     * @see es.um.dga.features.utils.Settings#getLogger()
     */
    public static final Object LOGGER = new Object();
    
    /**
     * Configuration Lock.
     * @see es.um.dga.features.utils.Settings#//loadConfiguration()
     */
    public static final Object CONFIGURATION = new Object();
    
    /**
     * English cache storage Lock.
     * @see es.um.dga.features.nlp.utils.EnglishCache
     */
    public final static Object ENGLISH_CACHE = new Object();
    
    public final static Object LOAD_MONGO_CONFIGURATION = new Object();
}
