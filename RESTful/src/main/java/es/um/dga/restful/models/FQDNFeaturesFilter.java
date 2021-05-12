package es.um.dga.restful.models;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FQDNFeaturesFilter extends FQDN {
    @NotNull
    private List<String> features;

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
