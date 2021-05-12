/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp;

import com.google.common.net.InternetDomainName;
import es.um.dga.features.nlp.utils.DomainDistribution;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.nlp.utils.FeaturesFactory;
import es.um.dga.features.nlp.utils.StringHelper;
import es.um.dga.features.storage.json.JSONHelper;
import es.um.dga.features.utils.Settings;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
        this.featureCollection.add(FeaturesFactory.from("NLP_LC_D", value));
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

    /**
     * Computes some features instead of all the available.
     * @param features Features names to be computed.
     */
    public void computeSomeFeatures(List<String> features) {

        List<String> parts = new ArrayList<>(this.domainName.parts().reverse());
        List<String> parts_odn = null;
        if (parts.size() > 2) {
            parts_odn = new ArrayList<>(parts);
            parts_odn.remove(0); // Remove TLD
            parts_odn.remove(0); // Remove 2LD
        }

        boolean check_1G = false;
        boolean check_2G = false;
        boolean check_3G = false;
        DomainDistribution oneGram = null;
        DomainDistribution twoGram = null;
        DomainDistribution threeGram = null;

        for (String feature : features) {
            String ft = feature.toUpperCase();
            if (ft.contains("1G")) {
                oneGram = DomainDistribution.builder().nGramSize(1).domainName(this.domainName).build();
                oneGram.loadData();
                oneGram.initComputeFeatures();
                check_1G = true;
            } else if (ft.contains("2G")) {
                twoGram = DomainDistribution.builder().nGramSize(1).domainName(this.domainName).build();
                twoGram.loadData();
                twoGram.initComputeFeatures();
                check_2G = true;
            } else if (ft.contains("3G")) {
                threeGram = DomainDistribution.builder().nGramSize(1).domainName(this.domainName).build();
                threeGram.loadData();
                threeGram.initComputeFeatures();
                check_3G = true;
            }

            if (check_1G && check_2G && check_3G) {
                break;
            }
        }

        for (String feature : features) {
            String ft = feature.toUpperCase();
            if (check_1G && ft.contains("1G")) {
                switch (ft) {
                    case "NLP_1G_DIST": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DIST", oneGram.getNGramsWithMoreOccurrencesThanThreshold(1).size()));} break;
                    case "NLP_1G_REP": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_REP", oneGram.getNGramsWithMoreOccurrencesThanThreshold(2).size()));} break;
                    case "NLP_1G_DST_KL": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_KL", oneGram.getKullbackLeiblerDivergence(oneGram.englishDistribution)));} break;
                    case "NLP_1G_DST_JI": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_JI", oneGram.getJaccardIndex(oneGram.englishDistribution)));} break;
                    case "NLP_1G_DST_CA": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_CA", oneGram.getCanberraDistance(oneGram.englishDistribution)));} break;
                    case "NLP_1G_DST_CH": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_CH", oneGram.getChebyshevDistance(oneGram.englishDistribution)));} break;
                    case "NLP_1G_DST_EM": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_EM", oneGram.getEarthMoversDistance(oneGram.englishDistribution)));} break;
                    case "NLP_1G_DST_EU": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_EU", oneGram.getEuclideanDistance(oneGram.englishDistribution)));} break;
                    case "NLP_1G_DST_MA": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_DST_MA", oneGram.getManhattanDistance(oneGram.englishDistribution)));} break;
                    case "NLP_1G_MEAN": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_MEAN", oneGram.getMean()));} break;
                    case "NLP_1G_25P": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_25P", oneGram.get25Percentile()));} break;
                    case "NLP_1G_50P": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_50P", oneGram.get50Percentile()));} break;
                    case "NLP_1G_75P": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_75P", oneGram.get75Percentile()));} break;
                    case "NLP_1G_QMEAN": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_QMEAN", oneGram.getQuadraticMean()));} break;
                    case "NLP_1G_SUMSQ": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_SUMSQ", oneGram.getSumsq()));} break;
                    case "NLP_1G_VAR": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_VAR", oneGram.getVariance()));} break;
                    case "NLP_1G_PVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_PVAR", oneGram.getPopulationVariance()));} break;
                    case "NLP_1G_STD": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_STD", oneGram.getStandardDeviation()));} break;
                    case "NLP_1G_PSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_PSTD", oneGram.getPopulationStandardDeviation()));} break;
                    case "NLP_1G_SKE": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_SKE", oneGram.getSkewness()));} break;
                    case "NLP_1G_KUR": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_KUR", oneGram.getKurtosis()));} break;
                    case "NLP_1G_TSUM": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TSUM", oneGram.getTargetLanguageSum()));} break;
                    case "NLP_1G_TSUMSQ": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TSUMSQ", oneGram.getTargetLanguageSumsq()));} break;
                    case "NLP_1G_TVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TVAR", oneGram.getTargetLanguageVariance()));} break;
                    case "NLP_1G_TPVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TPVAR", oneGram.getTargetLanguagePopulationVariance()));} break;
                    case "NLP_1G_TSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TSTD", oneGram.getTargetLanguageStandardDeviation()));} break;
                    case "NLP_1G_TPSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TPSTD", oneGram.getTargetLanguagePopulationStandardDeviation()));} break;
                    case "NLP_1G_TSKE": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TSKE", oneGram.getTargetLanguageSkewness()));} break;
                    case "NLP_1G_TKUR": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_TKUR", oneGram.getTargetLanguageKurtosis()));} break;
                    case "NLP_1G_COV": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_COV", oneGram.getCovariance(oneGram.englishDistribution)));} break;
                    case "NLP_1G_KEN": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_KEN", oneGram.getKendallsCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_1G_PEA": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_PEA", oneGram.getPearsonsCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_1G_SPE": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_SPE", oneGram.getSpearmansCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_1G_PRO": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_PRO", oneGram.getPronounceabilityScore(oneGram.englishDistribution)));} break;
                    case "NLP_1G_NORM": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_NORM", oneGram.getNormalityScore(oneGram.englishDistribution)));} break;
                    case "NLP_1G_E": {this.featureCollection.add(FeaturesFactory.from("NLP_1G_E", oneGram.getEntropy()));} break;

                    default: {
                        // do nothing
                    }
                    break;
                }

            } else if (check_2G && ft.contains("2G")) {
                switch (ft) {
                    case "NLP_2G_DIST": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DIST", oneGram.getNGramsWithMoreOccurrencesThanThreshold(1).size()));} break;
                    case "NLP_2G_REP": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_REP", oneGram.getNGramsWithMoreOccurrencesThanThreshold(2).size()));} break;
                    case "NLP_2G_DST_KL": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_KL", oneGram.getKullbackLeiblerDivergence(oneGram.englishDistribution)));} break;
                    case "NLP_2G_DST_JI": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_JI", oneGram.getJaccardIndex(oneGram.englishDistribution)));} break;
                    case "NLP_2G_DST_CA": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_CA", oneGram.getCanberraDistance(oneGram.englishDistribution)));} break;
                    case "NLP_2G_DST_CH": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_CH", oneGram.getChebyshevDistance(oneGram.englishDistribution)));} break;
                    case "NLP_2G_DST_EM": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_EM", oneGram.getEarthMoversDistance(oneGram.englishDistribution)));} break;
                    case "NLP_2G_DST_EU": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_EU", oneGram.getEuclideanDistance(oneGram.englishDistribution)));} break;
                    case "NLP_2G_DST_MA": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_DST_MA", oneGram.getManhattanDistance(oneGram.englishDistribution)));} break;
                    case "NLP_2G_MEAN": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_MEAN", oneGram.getMean()));} break;
                    case "NLP_2G_25P": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_25P", oneGram.get25Percentile()));} break;
                    case "NLP_2G_50P": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_50P", oneGram.get50Percentile()));} break;
                    case "NLP_2G_75P": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_75P", oneGram.get75Percentile()));} break;
                    case "NLP_2G_QMEAN": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_QMEAN", oneGram.getQuadraticMean()));} break;
                    case "NLP_2G_SUMSQ": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_SUMSQ", oneGram.getSumsq()));} break;
                    case "NLP_2G_VAR": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_VAR", oneGram.getVariance()));} break;
                    case "NLP_2G_PVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_PVAR", oneGram.getPopulationVariance()));} break;
                    case "NLP_2G_STD": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_STD", oneGram.getStandardDeviation()));} break;
                    case "NLP_2G_PSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_PSTD", oneGram.getPopulationStandardDeviation()));} break;
                    case "NLP_2G_SKE": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_SKE", oneGram.getSkewness()));} break;
                    case "NLP_2G_KUR": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_KUR", oneGram.getKurtosis()));} break;
                    case "NLP_2G_TSUM": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TSUM", oneGram.getTargetLanguageSum()));} break;
                    case "NLP_2G_TSUMSQ": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TSUMSQ", oneGram.getTargetLanguageSumsq()));} break;
                    case "NLP_2G_TVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TVAR", oneGram.getTargetLanguageVariance()));} break;
                    case "NLP_2G_TPVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TPVAR", oneGram.getTargetLanguagePopulationVariance()));} break;
                    case "NLP_2G_TSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TSTD", oneGram.getTargetLanguageStandardDeviation()));} break;
                    case "NLP_2G_TPSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TPSTD", oneGram.getTargetLanguagePopulationStandardDeviation()));} break;
                    case "NLP_2G_TSKE": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TSKE", oneGram.getTargetLanguageSkewness()));} break;
                    case "NLP_2G_TKUR": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_TKUR", oneGram.getTargetLanguageKurtosis()));} break;
                    case "NLP_2G_COV": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_COV", oneGram.getCovariance(oneGram.englishDistribution)));} break;
                    case "NLP_2G_KEN": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_KEN", oneGram.getKendallsCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_2G_PEA": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_PEA", oneGram.getPearsonsCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_2G_SPE": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_SPE", oneGram.getSpearmansCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_2G_PRO": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_PRO", oneGram.getPronounceabilityScore(oneGram.englishDistribution)));} break;
                    case "NLP_2G_NORM": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_NORM", oneGram.getNormalityScore(oneGram.englishDistribution)));} break;
                    case "NLP_2G_E": {this.featureCollection.add(FeaturesFactory.from("NLP_2G_E", oneGram.getEntropy()));} break;

                    default: {
                        // do nothing
                    }
                    break;
                }

            } else if (check_3G && ft.contains("3G")) {
                switch (ft) {
                    case "NLP_3G_DIST": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DIST", oneGram.getNGramsWithMoreOccurrencesThanThreshold(1).size()));} break;
                    case "NLP_3G_REP": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_REP", oneGram.getNGramsWithMoreOccurrencesThanThreshold(2).size()));} break;
                    case "NLP_3G_DST_KL": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_KL", oneGram.getKullbackLeiblerDivergence(oneGram.englishDistribution)));} break;
                    case "NLP_3G_DST_JI": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_JI", oneGram.getJaccardIndex(oneGram.englishDistribution)));} break;
                    case "NLP_3G_DST_CA": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_CA", oneGram.getCanberraDistance(oneGram.englishDistribution)));} break;
                    case "NLP_3G_DST_CH": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_CH", oneGram.getChebyshevDistance(oneGram.englishDistribution)));} break;
                    case "NLP_3G_DST_EM": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_EM", oneGram.getEarthMoversDistance(oneGram.englishDistribution)));} break;
                    case "NLP_3G_DST_EU": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_EU", oneGram.getEuclideanDistance(oneGram.englishDistribution)));} break;
                    case "NLP_3G_DST_MA": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_DST_MA", oneGram.getManhattanDistance(oneGram.englishDistribution)));} break;
                    case "NLP_3G_MEAN": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_MEAN", oneGram.getMean()));} break;
                    case "NLP_3G_25P": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_25P", oneGram.get25Percentile()));} break;
                    case "NLP_3G_50P": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_50P", oneGram.get50Percentile()));} break;
                    case "NLP_3G_75P": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_75P", oneGram.get75Percentile()));} break;
                    case "NLP_3G_QMEAN": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_QMEAN", oneGram.getQuadraticMean()));} break;
                    case "NLP_3G_SUMSQ": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_SUMSQ", oneGram.getSumsq()));} break;
                    case "NLP_3G_VAR": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_VAR", oneGram.getVariance()));} break;
                    case "NLP_3G_PVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_PVAR", oneGram.getPopulationVariance()));} break;
                    case "NLP_3G_STD": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_STD", oneGram.getStandardDeviation()));} break;
                    case "NLP_3G_PSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_PSTD", oneGram.getPopulationStandardDeviation()));} break;
                    case "NLP_3G_SKE": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_SKE", oneGram.getSkewness()));} break;
                    case "NLP_3G_KUR": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_KUR", oneGram.getKurtosis()));} break;
                    case "NLP_3G_TSUM": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TSUM", oneGram.getTargetLanguageSum()));} break;
                    case "NLP_3G_TSUMSQ": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TSUMSQ", oneGram.getTargetLanguageSumsq()));} break;
                    case "NLP_3G_TVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TVAR", oneGram.getTargetLanguageVariance()));} break;
                    case "NLP_3G_TPVAR": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TPVAR", oneGram.getTargetLanguagePopulationVariance()));} break;
                    case "NLP_3G_TSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TSTD", oneGram.getTargetLanguageStandardDeviation()));} break;
                    case "NLP_3G_TPSTD": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TPSTD", oneGram.getTargetLanguagePopulationStandardDeviation()));} break;
                    case "NLP_3G_TSKE": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TSKE", oneGram.getTargetLanguageSkewness()));} break;
                    case "NLP_3G_TKUR": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_TKUR", oneGram.getTargetLanguageKurtosis()));} break;
                    case "NLP_3G_COV": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_COV", oneGram.getCovariance(oneGram.englishDistribution)));} break;
                    case "NLP_3G_KEN": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_KEN", oneGram.getKendallsCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_3G_PEA": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_PEA", oneGram.getPearsonsCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_3G_SPE": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_SPE", oneGram.getSpearmansCorrelation(oneGram.englishDistribution)));} break;
                    case "NLP_3G_PRO": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_PRO", oneGram.getPronounceabilityScore(oneGram.englishDistribution)));} break;
                    case "NLP_3G_NORM": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_NORM", oneGram.getNormalityScore(oneGram.englishDistribution)));} break;
                    case "NLP_3G_E": {this.featureCollection.add(FeaturesFactory.from("NLP_3G_E", oneGram.getEntropy()));} break;

                    default: {
                        // do nothing
                    }
                    break;
                }
            } else {
                switch (ft) {
                    case "NLP_L_FQDN": {
                        this.featureCollection.add(FeaturesFactory.from("NLP_L_FQDN", this.domainName.toString().length()));
                    }
                    break;
                    case "NLP_R_LET_FQDN": {
                        this.computeRatioLetters(this.domainName.toString(), "NLP_R_LET_FQDN");
                    }
                    break;
                    case "NLP_R_CON_FQDN": {
                        this.computeRatioConsonants(this.domainName.toString(), "NLP_R_CON_FQDN");
                    }
                    break;
                    case "NLP_R_VOW_FQDN": {
                        this.computeRatioVowels(this.domainName.toString(), "NLP_R_VOW_FQDN");
                    }
                    break;
                    case "NLP_R_NUM_FQDN": {
                        this.computeRatioNumbers(this.domainName.toString(), "NLP_R_NUM_FQDN");
                    }
                    break;
                    case "NLP_R_SYM_FQDN": {
                        this.computeRatioSymbols(this.domainName.toString(), "NLP_R_SYM_FQDN");
                    }
                    break;
                    case "NLP_L_2DN": {
                        this.featureCollection.add(FeaturesFactory.from("NLP_L_2DN", parts.get(1).length()));
                    }
                    break;
                    case "NLP_R_LET_2DN": {
                        this.computeRatioLetters(parts.get(1), "NLP_R_LET_2DN");
                    }
                    break;
                    case "NLP_R_CON_2DN": {
                        this.computeRatioConsonants(parts.get(1), "NLP_R_CON_2DN");
                    }
                    break;
                    case "NLP_R_VOW_2DN": {
                        this.computeRatioVowels(parts.get(1), "NLP_R_VOW_2DN");
                    }
                    break;
                    case "NLP_R_NUM_2DN": {
                        this.computeRatioNumbers(parts.get(1), "NLP_R_NUM_2DN");
                    }
                    break;
                    case "NLP_R_SYM_2DN": {
                        this.computeRatioSymbols(parts.get(1), "NLP_R_SYM_2DN");
                    }
                    break;
                    case "NLP_L_ODN": {
                        if (parts_odn == null) {
                            this.featureCollection.add(FeaturesFactory.from("NLP_L_ODN", 0.0));
                        } else {
                            this.featureCollection.add(FeaturesFactory.from("NLP_L_ODN", parts_odn.get(1).length()));
                        }
                    }
                    break;
                    case "NLP_R_LET_ODN": {
                        if (parts_odn == null) {
                            this.featureCollection.add(FeaturesFactory.from("NLP_R_LET_ODN", 0.0));
                        } else {
                            this.computeRatioLetters(parts_odn.get(1), "NLP_R_LET_ODN");
                        }
                    }
                    break;
                    case "NLP_R_CON_ODN": {
                        if (parts_odn == null) {
                            this.featureCollection.add(FeaturesFactory.from("NLP_R_CON_ODN", 0.0));
                        } else {
                            this.computeRatioConsonants(parts_odn.get(1), "NLP_R_CON_ODN");
                        }
                    }
                    break;
                    case "NLP_R_VOW_ODN": {
                        if (parts_odn == null) {
                            this.featureCollection.add(FeaturesFactory.from("NLP_R_VOW_ODN", 0.0));
                        } else {
                            this.computeRatioVowels(parts_odn.get(1), "NLP_R_VOW_ODN");
                        }
                    }
                    break;
                    case "NLP_R_NUM_ODN": {
                        if (parts_odn == null) {
                            this.featureCollection.add(FeaturesFactory.from("NLP_R_NUM_ODN", 0.0));
                        } else {
                            this.computeRatioNumbers(parts_odn.get(1), "NLP_R_NUM_ODN");
                        }
                    }
                    break;
                    case "NLP_R_SYM_ODN": {
                        if (parts_odn == null) {
                            this.featureCollection.add(FeaturesFactory.from("NLP_R_SYM_ODN", 0.0));
                        } else {
                            this.computeRatioSymbols(parts_odn.get(1), "NLP_R_SYM_ODN");
                        }
                    }
                    break;
                    case "NLP_LC_C": {
                        this.computeLongestConsonantSequence();
                    }
                    break;
                    case "NLP_LC_V": {
                        this.computeLongestVowelSequence();
                    }
                    break;
                    case "NLP_LC_D": {
                        this.computeLongestNumberSequence();
                    }
                    break;
                    case "NLP_N": {
                        this.computeNumberOfLevels();
                    }
                    break;
                    default:
                        // do nothing
                        break;
                }
            }
        }
    }
}
