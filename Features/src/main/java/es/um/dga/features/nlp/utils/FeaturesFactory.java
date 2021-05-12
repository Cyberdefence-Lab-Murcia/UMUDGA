/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import es.um.dga.features.storage.json.JSONHelper;

public class FeaturesFactory {
    
    /**
     * Create a new feature object from a double number
     * @param key Feature Name
     * @param value Feature Value
     * @return Feature object.
     */
    public static <T extends Number> Feature from(String key, T value) {
        return new Feature(key, value.doubleValue());
    }
    
    public static <T extends String> Feature from(String key, T value) {
        if (StringUtils.isNotEmpty(value)) {
            return new Feature(key, value);
        } else {
            return new Feature(key,"?");
        }
    }
    
    public static <T extends Number> Collection<Feature> from(String prefix, Map<String, T> features) {
        Collection<Feature> result = new HashSet<>();
        
        for (Map.Entry<String, T> entry : features.entrySet()) {
            result.add(FeaturesFactory.from(prefix + entry.getKey(), entry.getValue().doubleValue()));
        }
        
        return result;
    }
    
    public static JSONHelper documentBuilder() {
        return new JSONHelper();
    }
    
}
