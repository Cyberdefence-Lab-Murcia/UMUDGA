/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp;

import com.google.common.net.InternetDomainName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import es.um.dga.features.nlp.utils.DomainDistribution;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.nlp.utils.FeaturesFactory;
import es.um.dga.features.nlp.utils.StringHelper;
import es.um.dga.features.storage.json.JSONHelper;
import es.um.dga.features.utils.Settings;

/**
 * Single DNS Query NLP analyser.
 */
public class Analyzer {
    
    private static final Object lock = new Object();
    /**
     * All computed features.
     */
    private Collection<Feature> featureCollection = new HashSet<>();
    /**
     * Fully qualified domain name.
     */
    private InternetDomainName domainName;
    /**
     * Record label, if known.
     */
    private String label;
    
    /**
     * Default constructor with known label.
     *
     * @param domainName Fully qualified domain name.
     * @param label      Known class.
     */
    public Analyzer(InternetDomainName domainName, String label) {
        this.domainName = domainName;
        this.label = label;
    }
    
    /**
     * Default constructor with unknown label.
     *
     * @param domainName Fully qulified domain name.
     */
    public Analyzer(InternetDomainName domainName) {
        this.domainName = domainName;
        this.label = null;
    }
    
    /**
     * Gets the 'label' property value.
     *
     * @return value of label##
     */
    public String getLabel() {
        return this.label;
    }
    
    /**
     * Provide a {@link org.bson.Document} object compatible with JSON output.
     * Only Features is FALSE.
     *
     * @return A Document object.
     */
    public Document ToDocument() {
        return this.ToDocument(false);
    }
    
    /**
     * Provide a {@link org.bson.Document} object compatible with JSON output.
     *
     * @param withoutObjectId If true will not append a randomly generated object id.
     *
     * @return A Document object.
     */
    private Document ToDocument(boolean withoutObjectId) {
        JSONHelper documentBuilder = FeaturesFactory.documentBuilder();
        
        if (!withoutObjectId) {
            documentBuilder = documentBuilder.withNewObjectId();
        }
        
        return documentBuilder.withFeatures(this.featureCollection).build();
    }
    
    /**
     * Force the computation of all features.
     */
    public void computeAllFeatures() {
        if (Settings.ENABLE_BASE) {
            this.computeRatios();
            this.computeLongestConsonantSequence();
            this.computeLongestVowelSequence();
            this.computeLongestNumberSequence();
            this.computeNumberOfLevels();
        }
        
        if (Settings.ENABLE_1G) {
            DomainDistribution oneGram = DomainDistribution.builder().nGramSize(1).domainName(this.domainName).build();
            oneGram.loadData();
            oneGram.computeFeatures();
            this.featureCollection.addAll(oneGram.getFeatures());
            oneGram = null;
        }
    
        if (Settings.ENABLE_2G) {
            DomainDistribution twoGram = DomainDistribution.builder().nGramSize(2).domainName(this.domainName).build();
            twoGram.loadData();
            twoGram.computeFeatures();
            this.featureCollection.addAll(twoGram.getFeatures());
            twoGram = null;
        }
        
        if (Settings.ENABLE_3G) {
            DomainDistribution threeGram = DomainDistribution.builder().nGramSize(3).domainName(this.domainName).build();
            threeGram.loadData();
            threeGram.computeFeatures();
            this.featureCollection.addAll(threeGram.getFeatures());
            threeGram = null;
        }
        
        this.featureCollection.add(FeaturesFactory.from("domain", this.getDomainName().toString()));
        this.featureCollection.add(FeaturesFactory.from("class", this.getLabel()));
    }
    
    /**
     * String Shape - Length Distribution of Full Domain Name
     */
    private void computeRatios() {
        this.featureCollection.add(FeaturesFactory.from("NLP_L_FQDN", this.domainName.toString().length()));
        
        this.computeRatioLetters(this.domainName.toString(), "NLP_R_LET_FQDN");
        this.computeRatioConsonants(this.domainName.toString(), "NLP_R_CON_FQDN");
        this.computeRatioVowels(this.domainName.toString(), "NLP_R_VOW_FQDN");
        this.computeRatioNumbers(this.domainName.toString(), "NLP_R_NUM_FQDN");
        this.computeRatioSymbols(this.domainName.toString(), "NLP_R_SYM_FQDN");
        
        // Example: mail.google.com, this method returns the list ["mail", "google", "com"]
        List<String> parts = new ArrayList<>(this.domainName.parts().reverse());
        // ["com", "google", "mail"]
        try {
            this.featureCollection.add(FeaturesFactory.from("NLP_L_2DN", parts.get(1).length()));
            
            this.computeRatioLetters(parts.get(1), "NLP_R_LET_2DN");
            this.computeRatioConsonants(parts.get(1), "NLP_R_CON_2DN");
            this.computeRatioVowels(parts.get(1), "NLP_R_VOW_2DN");
            this.computeRatioNumbers(parts.get(1), "NLP_R_NUM_2DN");
            this.computeRatioSymbols(parts.get(1), "NLP_R_SYM_2DN");
            
            
            if (parts.size() > 2) {
                parts.remove(0); // Remove TLD
                parts.remove(0); // Remove 2LD
                
                String odn = StringUtils.join(parts, ".");
                
                this.featureCollection.add(FeaturesFactory.from("NLP_L_ODN", odn.length()));
                
                this.computeRatioLetters(odn, "NLP_R_LET_ODN");
                this.computeRatioConsonants(odn, "NLP_R_CON_ODN");
                this.computeRatioVowels(odn, "NLP_R_VOW_ODN");
                this.computeRatioNumbers(odn, "NLP_R_NUM_ODN");
                this.computeRatioSymbols(odn, "NLP_R_SYM_ODN");
            }
            else {
                this.featureCollection.add(FeaturesFactory.from("NLP_L_ODN", 0.0));
                this.featureCollection.add(FeaturesFactory.from("NLP_R_LET_ODN", 0.0));
                this.featureCollection.add(FeaturesFactory.from("NLP_R_CON_ODN", 0.0));
                this.featureCollection.add(FeaturesFactory.from("NLP_R_VOW_ODN", 0.0));
                this.featureCollection.add(FeaturesFactory.from("NLP_R_NUM_ODN", 0.0));
                this.featureCollection.add(FeaturesFactory.from("NLP_R_SYM_ODN", 0.0));
            }
            
        }
        catch (Exception ex) {
            synchronized (lock) {
                System.out.println(String.join(",", parts));
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Compute the ratio of letters w.r.t. the domain name.
     */
    private void computeRatioLetters(String partialDomain, String featureName) {
        this.featureCollection.add(FeaturesFactory.from(featureName, StringHelper.getRatioLettersChars(partialDomain)));
        
    }
    
    /**
     * Compute the ratio of letters w.r.t. the domain name.
     */
    private void computeRatioConsonants(String partialDomain, String featureName) {
        this.featureCollection
                .add(FeaturesFactory.from(featureName, StringHelper.getRatioConsonantsChars(partialDomain)));
        
    }
    
    /**
     * Compute the ratio of letters w.r.t. the domain name.
     */
    private void computeRatioVowels(String partialDomain, String featureName) {
        this.featureCollection.add(FeaturesFactory.from(featureName, StringHelper.getRatioVowelsChars(partialDomain)));
        
    }
    
    /**
     * Compute the ratio of numbers w.r.t. the domain name.
     */
    private void computeRatioNumbers(String partialDomain, String featureName) {
        this.featureCollection.add(FeaturesFactory.from(featureName, StringHelper.getRatioNumericChars(partialDomain)));
        
    }
    
    /**
     * Compute the ratio of symbols w.r.t. the domain name.
     */
    private void computeRatioSymbols(String partialDomain, String featureName) {
        this.featureCollection.add(FeaturesFactory.from(featureName, StringHelper.getRatioSymbolChars(partialDomain)));
        
    }
    
    /**
     * Compute the longest consonants sequence.
     */
    private void computeLongestConsonantSequence() {
        int value = StringHelper
                .longestConsecutiveSubstringInAlphabet(this.domainName.toString(), StringHelper.CONSONANTS);
        this.featureCollection.add(FeaturesFactory.from("NLP_LC_C", value));
    }
    
    /**
     * Compute the longest vowel sequence.
     */
    private void computeLongestVowelSequence() {
        int value = StringHelper.longestConsecutiveSubstringInAlphabet(this.domainName.toString(), StringHelper.VOWELS);
        this.featureCollection.add(FeaturesFactory.from("NLP_LC_V", value));
    }
    /**
     * Compute the longest number sequence.
     */
    private void computeLongestNumberSequence() {
        int value = StringHelper.longestConsecutiveSubstringInAlphabet(this.domainName.toString(), StringHelper.DIGITS);
        this.featureCollection.add(FeaturesFactory.from("NLP_LC_V", value));
    }
    
    /**
     * Number of levels in the domain name.
     */
    private void computeNumberOfLevels() {
        this.featureCollection.add(FeaturesFactory.from("NLP_N", this.domainName.parts().size()));
    }
    
    /**
     * Gets the 'domainName' property value.
     *
     * @return value of domainName##
     */
    public InternetDomainName getDomainName() {
        return this.domainName;
    }
    
    /**
     * Gets all the calculated features.
     *
     * @return All features.
     */
    public Collection<Feature> getAllFeatures() {
        return this.featureCollection;
    }
    
}
