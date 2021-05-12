/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage.mongodb;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Static filters for MongoDB queries.
 */
public class Filters {
    
    /**
     * Regexp for Starts With TOKEN
     * @param search TOKEN
     * @return "/^TOKEN/"
     */
    public static String StartsWith(String search) {
        return "/^" + search + "/";
    }
    
    /**
     * Regexp for Ends With TOKEN
     * @param search TOKEN
     * @return "/TOKEN$/"
     */
    public static String EndsWith(String search) {
        return "/" + search + "$/";
    }
    
    /**
     * Regexp for Contains TOKEN
     * @param search TOKEN
     * @return "/TOKEN/"
     */
    public static String Contains(String search) {
        return "/" + search + "/";
    }
    
    public static Bson equalsObjectID(String objectId) {
        return equalsObjectID(new ObjectId(objectId));
    }
    
    public static Bson equalsObjectID(ObjectId objectId) {
        return com.mongodb.client.model.Filters.eq("_id", objectId);
    }
}
