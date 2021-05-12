/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Feature implements Map.Entry<String, Object>, Comparable<Feature> {
    
    private String key;
    private Object value;
    private Class type;
    
    @Override public String getKey() {
        return this.key;
    }
    
    @Override public Object getValue() {
        return this.value;
    }
    
    @Override public Object setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        return oldValue;
    }
    
    /**
     * Gets the 'type' property value.
     *
     * @return value of type##
     */
    public Class getType() {
        return type;
    }
    
    /**
     * Sets the 'type' property value.
     *
     * @param type value of type
     */
    public void setType(Class type) {
        this.type = type;
    }
    
    /**
     * Private constructor
     *
     * @param key   Feature Name
     * @param value Feature Value
     */
    Feature(String key, Number value) {
        this.key = key.toLowerCase();
        this.value = value;
        this.type = Number.class;
    }
    
    /**
     * Private constructor
     *
     * @param key   Feature Name
     * @param value Feature Value
     */
    Feature(String key, String value) {
        this.key = key.toLowerCase();
        this.value = value;
        this.type = String.class;
    }
    
    /**
     * Feature comparator. Alphabetically order the keys, then the values.
     */
    public static Comparator<Feature> featureComparator = new Comparator<Feature>() {
        @Override public int compare(Feature o1, Feature o2) {
            
            String f1Key = o1.getKey().toLowerCase();
            String f2Key = o2.getKey().toLowerCase();
            Object f1Value = o1.getValue();
            Object f2Value = o2.getValue();
            
            int compareKey = f1Key.compareTo(f2Key);
            
            if (compareKey != 0) {
                return compareKey;
            }
            
            // Both numbers
            if (Number.class.isAssignableFrom(f1Value.getClass()) && Number.class
                    .isAssignableFrom(f2Value.getClass())) {
                int compareValue = Double.compare(((Number) f1Value).doubleValue(), ((Number) f2Value).doubleValue());
                if (compareValue != 0) {
                    return compareValue;
                }
                else {
                    return 0;
                }
            }
            
            // Both strings
            if (String.class.isAssignableFrom(f1Value.getClass()) && String.class
                    .isAssignableFrom(f2Value.getClass())) {
                if (f1Value == f2Value) {
                    return 0;
                }
                if (StringUtils.isNotEmpty(f1Value.toString())) {
                    return -1;
                }
                if (StringUtils.isNotEmpty(f2Value.toString())) {
                    return 1;
                }
                return f1Value.toString().compareTo(f2Value.toString());
            }
            
            // Different type for value object.
            // Fixed o1 less than o2.
            return -1;
        }
    };
    
    @Override public int compareTo(@NotNull Feature o) {
        return featureComparator.compare(this, o);
    }
}
