/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.util.Map;
import javax.json.bind.annotation.JsonbTypeAdapter;
import org.jetbrains.annotations.NotNull;
import es.um.dga.restful.models.FeatureAdapter;
import weka.core.Attribute;

/*
  Feature class, implements Map.Entry to be able to use it in a map. Implements Comparable to be able to sort the collection.
 */
@JsonbTypeAdapter(value = FeatureAdapter.class)
public class Feature implements Map.Entry<String, Object>, Comparable<Feature> {

    public String key;

    public Object value;

    public Class<?> type;

    public String getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

    public Object setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @Override
    public String toString() {
        return "Feature{" + "key='" + key + '\'' + ", value=" + value + ", type=" + type + '}';
    }

    /**
     * Gets the 'type' property value.
     *
     * @return value of type##
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Sets the 'type' property value.
     *
     * @param type value of type
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * Private constructor
     *
     * @param key   Feature Name
     * @param value Feature Value
     */
    public Feature(String key, Number value) {
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
    public Feature(String key, String value) {
        this.key = key.toLowerCase();
        this.value = value;
        this.type = String.class;
    }

    /**
     * Private constructor
     *
     * @param key        Feature Name
     * @param value      Feature Value
     * @param valueclass Feature Value's class.
     */
    public Feature(String key, Object value, Class<?> valueclass) {
        this.key = key.toLowerCase();
        this.value = value;
        this.type = valueclass;
    }

    public int compareTo(@NotNull Feature o) {
        return FeaturesFactory.featureComparator.compare(this, o);
    }

    /**
     * Returns the feature key as {@link weka.core.Attribute} instance for {@link weka.core.Instance}.
     * @return Attribute with {@link es.um.dga.features.nlp.utils.Feature#key} as {@link weka.core.Attribute#name()}
     */
    public Attribute toAttribute() {
        return new Attribute(this.getKey());
    }
}
