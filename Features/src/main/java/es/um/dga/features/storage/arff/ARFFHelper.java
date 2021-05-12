/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage.arff;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.nlp.utils.FeaturesFormatter;

public class ARFFHelper extends FeaturesFormatter {
    
    public ARFFHelper(SortedMap<String, String> featuresNames) {
        this.featuresNames.putAll(featuresNames);
    }
    
    public ARFFHelper() {
        // Force the class feature to be at the end.
        this.featuresNames = new TreeMap<>((o1, o2) -> {
            if (o1.equals("class")) {
                if (o2.equals("class")) {
                    return 0;
                }
                return +1;
            }
            if (o2.equals("class")) {
                return -1;
            }
            return o1.compareTo(o2);
        });
    }
    
    /**
     * Features map with name and object type.
     */
    private TreeMap<String, String> featuresNames;
    
    /**
     * Sets the feature names (a.k.a. the ARFF features).
     *
     * @param featuresNames Features collection with type.
     */
    public void setFeaturesNames(Map<String, Class> featuresNames) {
        featuresNames.forEach(this::addFeaturesName);
    }
    
    /**
     * Adds the feature name.
     *
     * @param name Feature Name
     * @param type Feature Type.
     */
    private void addFeaturesName(String name, Class type) {
        Pair<String, String> pair = featureToHeader(name, type);
        this.featuresNames.put(pair.getKey(), pair.getValue());
    }
    
    /**
     * Adds the feature name.
     *
     * @param feature Feature.
     */
    @Override public void addFeaturesName(Feature feature) {
        this.addFeaturesName(feature.getKey(), feature.getType());
    }
    /**
     * Returns the passed feature name as ARFF header format.
     *
     * @param name Feature name.
     * @param type Feature type.
     *
     * @return Pair with the correct format type.
     */
    private Pair<String, String> featureToHeader(String name, Class type) {
        
        if (Number.class.isAssignableFrom(type)) {
            return new ImmutablePair<>(name.toLowerCase(), "NUMERIC");
        }
        
        if (Date.class.isAssignableFrom(type)) {
            return new ImmutablePair<>(name.toLowerCase(), "DATE \"" + FeaturesFormatter.DATE_FORMATTER.toPattern() + "\"");
        }
        
        if (Collection.class.isAssignableFrom(type)) {
            return new ImmutablePair<>(name.toLowerCase(), "{}");
        }
        
        // Default case.
        return new ImmutablePair<>(name.toLowerCase(), "STRING");
    }
    
    /**
     * Add the feature name with the enumerator.
     *
     * @param name   Feature Name.
     * @param values Feature enumerator values.
     */
    public void addFeaturesName(String name, Collection<String> values) {
        StringBuilder resultType = new StringBuilder();
        resultType.append("{");
        
        for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            resultType.append(s);
            if (iterator.hasNext()) {
                resultType.append(",");
            }
        }
        resultType.append("}");
        this.featuresNames.put(name, resultType.toString());
    }
    
    @Override public SortedSet<String> getFeatureNames() {
        return this.featuresNames.navigableKeySet();
    }
    
    /**
     * Convert to string the full ARFF header.
     * Features are sorted by natural order provided by the {@link java.util.SortedMap}.
     *
     * @param relationName Relation name. WIll be stripped of any space.
     *
     * @return String header.
     */
    @Override public String getCompleteHeader(@NotNull String relationName) {
        StringBuilder result = new StringBuilder();
        result.append("% 1. Title: ");
        result.append(relationName);
        result.append(System.lineSeparator());
        result.append("%");
        result.append(System.lineSeparator());
        result.append("% 2. Sources:");
        result.append(System.lineSeparator());
        result.append("%      (a) Creator: Mattia Zago");
        result.append(System.lineSeparator());
        result.append("%      (b) Date: ");
        result.append(LocalDate.now());
        result.append(System.lineSeparator());
        result.append("%");
        result.append(System.lineSeparator());
        result.append("@RELATION ");
        result.append(relationName.replaceAll(" ", ""));
        result.append(System.lineSeparator());
        result.append(System.lineSeparator());
    
        for (Map.Entry<String, String> feature : this.featuresNames.entrySet()) {
            result.append("@ATTRIBUTE\t ");
            result.append(feature.getKey());
            result.append("\t ");
            result.append(feature.getValue());
            result.append(System.lineSeparator());
        }
        
        result.append(System.lineSeparator());
        result.append("@DATA");
        result.append(System.lineSeparator());
        return result.toString();
    }
}
