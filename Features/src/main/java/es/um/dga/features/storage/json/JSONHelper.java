/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage.json;

import java.util.Collection;

import org.bson.Document;
import org.bson.types.ObjectId;

import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.storage.mongodb.MongoDBHelper;
import es.um.dga.features.utils.Settings;

/**
 * Provides a document builder.
 */
public class JSONHelper {

    private Document root;
    
    private Document ftDocNLP = new Document();
    private Document ftDocNLP1G = new Document();
    private Document ftDocNLP2G = new Document();
    private Document ftDocNLP3G = new Document();
    //private Document ftDocNLP4G = new Document();
    //private Document ftDocNLP5G = new Document();
    //private Document ftDocNLP6G = new Document();
    //private Document ftDocNLP7G = new Document();
    //private Document ftDocNLP8G = new Document();
    //private Document ftDocNLP9G = new Document();
    private Document ftDocNLPR = new Document();
    private Document ftDocNLPL = new Document();
    //private Document ftDocDNS = new Document();
    //private Document ftDocOther = new Document();

    /**
     * Default Constructor.
     */
    public JSONHelper() {
        this.root = new Document();
    }

    /**
     * Closes and finalizes the builder.
     * @return Document object.
     */
    public Document build() {

        ftDocNLP.append("1G", ftDocNLP1G);
        ftDocNLP.append("2G", ftDocNLP2G);
        ftDocNLP.append("3G", ftDocNLP3G);
        //ftDocNLP.append("4G", ftDocNLP4G);
        //ftDocNLP.append("5G", ftDocNLP5G);
        //ftDocNLP.append("6G", ftDocNLP6G);
        //ftDocNLP.append("7G", ftDocNLP7G);
        //ftDocNLP.append("8G", ftDocNLP8G);
        //ftDocNLP.append("9G", ftDocNLP9G);
        ftDocNLP.append("L", ftDocNLPL);
        ftDocNLP.append("R", ftDocNLPR);
        
        this.root.append("NLP", ftDocNLP);
        //this.root.append("DNS", ftDocDNS);
        //this.root.append("Other", ftDocOther);
        return this.root;
    }

    /**
     * Add the provided object id.
     * @param objectId MongoDB object id.
     * @return This builder.
     */
    public JSONHelper withObjectId(ObjectId objectId) {
        this.root.append("_id",objectId);
        return this;
    }

    /**
     * Add a new object id to the document.
     * @return This builder.
     */
    public JSONHelper withNewObjectId() {
        return this.withObjectId(MongoDBHelper.newObjectID());
    }

    /**
     * Append all the features in the collection to this builder object.
     * @param features Feature Collection.
     * @return This builder.
     */
    public JSONHelper withFeatures(Collection<Feature> features) {
        features.forEach(this::withFeature);
        return this;
    }

    /**
     * Append the feature to this builder object.
     * @param feature Feature.
     * @return This builder.
     */
    public JSONHelper withFeature(Feature feature) {
        String featureName = feature.getKey().replaceAll("\\.", "_");
        Object featureValue = feature.getValue();

        if (featureName.contains("NLP")) {
            featureName = featureName.replaceAll("NLP_", "");
    
            if (featureName.startsWith("1G")) {
                featureName = featureName.replaceAll("1G_", "");
                ftDocNLP1G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("2G")) {
                featureName = featureName.replaceAll("2G_", "");
                ftDocNLP2G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("3G")) {
                featureName = featureName.replaceAll("3G_", "");
                ftDocNLP3G.append(featureName, featureValue);
            }
            /*
            else if (featureName.startsWith("4G")) {
                featureName = featureName.replaceAll("4G_", "");
                ftDocNLP4G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("5G")) {
                featureName = featureName.replaceAll("5G_", "");
                ftDocNLP5G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("6G")) {
                featureName = featureName.replaceAll("6G_", "");
                ftDocNLP6G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("7G")) {
                featureName = featureName.replaceAll("7G_", "");
                ftDocNLP7G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("8G")) {
                featureName = featureName.replaceAll("8G_", "");
                ftDocNLP8G.append(featureName, featureValue);
            }
            else if (featureName.startsWith("9G")) {
                featureName = featureName.replaceAll("9G_", "");
                ftDocNLP9G.append(featureName, featureValue);
            }
            */
            else if (featureName.startsWith("L_")) {
                featureName = featureName.replaceAll("L_", "");
                ftDocNLPL.append(featureName, featureValue);
            }
            else if (featureName.startsWith("R_")) {
                featureName = featureName.replaceAll("R_", "");
                ftDocNLPR.append(featureName, featureValue);
            }
            else {
                ftDocNLP.append(featureName, featureValue);
            }
        }
        //else if (featureName.contains("DNS")) {
        //    featureName = feature.getKey().replaceAll("DNS_", "");
        //    ftDocDNS.append(featureName, featureValue);
        //}
        else {
        //    ftDocOther.append(featureName, featureValue);
            Settings.getLogger().warning("Feature " + feature.getKey() + " has an invalid name.");
        }
        
        return this;
    }
}
