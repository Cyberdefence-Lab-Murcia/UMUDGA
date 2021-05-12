/*
 * Project: DGA-RESTful
 * Copyright (c) 2020 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */
package es.um.dga.restful.models;

import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.nlp.utils.FeaturesFactory;

public class FeatureAdapter implements JsonbAdapter<Feature, JsonObject> {
    @Override
    public JsonObject adaptToJson(Feature obj) {
        return Json.createObjectBuilder().add(obj.getKey(), obj.getValue().toString()).build();
    }

    @Override
    public Feature adaptFromJson(JsonObject obj) {
        assert obj.size() == 1;
        Map.Entry<String, JsonValue> valueEntry = obj.entrySet().iterator().next();
        return FeaturesFactory.from(valueEntry.getKey(), valueEntry.getValue().toString());
    }
}
