/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public abstract class FeaturesFormatter {
    /**
     * Decimal formatter.
     *
     * @see java.text.DecimalFormat
     */
    public static final DecimalFormat NUMERIC_FORMATTER = new DecimalFormat("0.00#####");
    
    /**
     * Date formatter.
     *
     * @see java.text.SimpleDateFormat
     */
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("d-M-Y H:m:s.S");
    
    /**
     * Gets the feature names.
     *
     * @return Sorted set of feature names.
     */
    public abstract SortedSet<String> getFeatureNames();
    
    /**
     * Gets the feature header according to the file format.
     * @param relationName Relation name.
     * @return String header.
     */
    public abstract String getCompleteHeader(String relationName);
    
    /**
     * Formats the provided features collection to be printed in a data file.
     *
     * @return String line to be printed.
     */
    public String getFormattedFeatureValue(SortedMap<String, Object> features) {
        StringBuilder lines = new StringBuilder();
        for (Iterator<String> iterator = this.getFeatureNames().iterator(); iterator.hasNext(); ) {
            String validName = iterator.next();
            Object value;
            try {
                value = features.get(validName);
            } catch (NullPointerException ex) {
                value = "?";
            }
            String sValue = this.featureToValue(value);
            lines.append(sValue);
            
            if (iterator.hasNext()) {
                lines.append(",");
            }
        }
        
        lines.append(System.lineSeparator());
        return lines.toString();
    }
    
    /**
     * Formats the provided features collection to be printed in a data file.
     *
     * @return String line to be printed.
     */
    public String getFormattedFeatureValue(Collection<Feature> features) {
        SortedMap<String, Object> featuresMap = new TreeMap<>();
        for (Feature feature : features) {
            featuresMap.put(feature.getKey(), feature.getValue());
        }
        return getFormattedFeatureValue(featuresMap);
    }
    
    /**
     * Converts the feature to a value that can be used in a data file.
     *
     * @param feature Feature to be converted
     *
     * @return String representation.
     */
    private String featureToValue(Feature feature) {
        return featureToValue(feature.getValue());
    }
    
    /**
     * Converts the feature to a value that can be used in a data file.
     *
     * @param value Feature value to be converted
     *
     * @return String representation.
     */
    private String featureToValue(Object value) {
        // Default case.
        return value.toString();
    }
    
    /**
     * Converts the feature to a value that can be used in a data file.
     *
     * @param value Feature value to be converted
     *
     * @return String representation.
     */
    private String featureToValue(Number value) {
        if (Double.isFinite(((Number) value).doubleValue())) {
            return NUMERIC_FORMATTER.format(value);
        }
        else {
            return this.featureToValue("?");
        }
    }
    
    /**
     * Converts the feature to a value that can be used in a data file.
     *
     * @param value Feature value to be converted
     *
     * @return String representation.
     */
    private String featureToValue(Date value) {
        return DATE_FORMATTER.format(value);
    }
    
    /**
     * Sets the feature names (a.k.a. the ARFF features).
     *
     * @param featuresNames Features collection.
     */
    public void setFeaturesNames(Collection<Feature> featuresNames) {
        featuresNames.forEach(this::addFeaturesName);
    }

    /**
     * Adds the feature name.
     *
     * @param feature Feature.
     */
    public abstract void addFeaturesName(Feature feature);
}
