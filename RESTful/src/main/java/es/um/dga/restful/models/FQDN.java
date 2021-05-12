package es.um.dga.restful.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fully qualified domain name support class.
 */
public class FQDN {

    @NotNull
    private String fqdn;

    @Nullable
    private String label;

    @Nullable
    private Double confidence;

    public String getFqdn() {
        return fqdn;
    }
    
    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    /**
     * Gets the 'label' property value.
     *
     * @return value of label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the 'label' property value.
     *
     * @param label value of label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the 'confidence' property value.
     *
     * @return value of confidence
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * Sets the 'confidence' property value.
     *
     * @param confidence value of confidence
     */
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
