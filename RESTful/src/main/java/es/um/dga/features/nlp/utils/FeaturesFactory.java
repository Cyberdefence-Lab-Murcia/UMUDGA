/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import es.um.dga.features.storage.json.JSONHelper;

public class FeaturesFactory {

    /**
     * Feature comparator. Alphabetically order the keys, then the values.
     */
    public static Comparator<Feature> featureComparator = (o1, o2) -> {

        String f1Key = o1.getKey().toLowerCase();
        String f2Key = o2.getKey().toLowerCase();
        Object f1Value = o1.getValue();
        Object f2Value = o2.getValue();

        int compareKey = f1Key.compareTo(f2Key);

        if (compareKey != 0) {
            return compareKey;
        }

        // Both numbers
        if (Number.class.isAssignableFrom(f1Value.getClass()) && Number.class.isAssignableFrom(
                f2Value.getClass())) {
            return Double.compare(((Number)f1Value).doubleValue(), ((Number)f2Value).doubleValue());
        }

        // Both strings
        if (String.class.isAssignableFrom(f1Value.getClass()) && String.class.isAssignableFrom(
                f2Value.getClass())) {
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
    };

    /**
     * Create a new feature object from a double number
     *
     * @param key   Feature Name
     * @param value Feature Value
     *
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
