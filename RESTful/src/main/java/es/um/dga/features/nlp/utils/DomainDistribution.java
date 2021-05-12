/*
 * Project: DomainNameProfiler
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import com.google.common.base.Splitter;
import com.google.common.net.InternetDomainName;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.HammingDistance;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;

import es.um.dga.features.nlp.ngrams.AbstractNGramDistribution;
import es.um.dga.features.nlp.ngrams.nGramDomainDistribution;
import es.um.dga.features.nlp.ngrams.nGramLanguageDistribution;
import es.um.dga.features.utils.Settings;

public final class DomainDistribution extends AbstractNGramDistribution implements nGramDomainDistribution {
    
    /**
     * The processed FQDN;
     */
    private InternetDomainName domainName;
    
    /**
     * The domain name as nGram list.
     */
    private HashMap<Boolean, List<String>> domainNameAsToken = new HashMap<>();
    
    /**
     * Private constructor.
     */
    private DomainDistribution() {
        super();
        // NOTHING ELSE TO DO.
    }
    
    /**
     * Gets the builder for creating a new instance.
     *
     * @return Builder class for this object.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void loadData() {
        EnglishEmptyDistribution englishEmptyDistribution = EnglishCache
                .getEnglishEmptyDistribution(this.getNGramSize());
        this.englishDistribution = EnglishCache.getEnglishDistribution(this.getNGramSize());
        
        this.frequencyDistribution = englishEmptyDistribution.getNGramsFrequencyDistribution();
        this.countDistribution = englishEmptyDistribution.getNGramsCountDistribution();
        this.statistics = new DescriptiveStatistics();
        this.targetLanguageStatistics = new DescriptiveStatistics();
        
        String domain = this.domainName.toString().toLowerCase();
        this.processDomainName(domain);
        
        
        for (Map.Entry<String, Double> entry : this.countDistribution.entrySet()) {
            Double frequency = entry.getValue() / this.getSumOfOccurences();
            if (Double.isFinite(frequency)) {
                // https://stackoverflow.com/a/14138032
                this.statistics.addValue(frequency);
            }
            else {
                this.statistics.addValue(0.0);
            }
            this.frequencyDistribution.put(entry.getKey(), frequency);
            
            // It occurs at least once, so not zero
            if (frequency > 0) {
                this.targetLanguageStatistics.addValue(this.englishDistribution.getNGramFrequency(entry.getKey()));
            } else {
                this.targetLanguageStatistics.addValue(0.0);
            }
        }
        
        if (this.getSumOfOccurences() == 0) {
            Settings.getLogger().warning(this.domainName + "\t [n:" + this.getNGramSize() + "] every counter is zero.");
        }
        
        
        this.updateStatisticsCache();
        
        //this.countDistribution.forEach((String, aDouble) -> {
        //    Double frequency = aDouble / this.totalCountOfOccurrencies;
        //    this.statistics.addValue(frequency);
        //    this.frequencyDistribution.put(String, frequency);
        //});
    }
    
    /**
     * Parse the domain name, use the public version.
     *
     * @param domain Domain to be parsed.
     */
    private void processDomainName(String domain) {
        Iterable<String> split = getDomainNameAsTokens(true);
        Settings.getLogger().log(Level.FINEST, "Splits: " + split);
        for (String s : split) {
            try {
                Double actualValue = this.countDistribution.get(s);
                ++actualValue;
                this.countDistribution.put(s, actualValue);
            }
            catch (NullPointerException ex) {
                // They key doesn't exist in the collection, it means that it is irrelevant,
                // so we ignore it.
            }
        }
        
        cacheOccurrencesStat();
    }
    
    /**
     * Tokenize the domain name.
     *
     * @param withWindow If true removes one character at the time from the beginning of the string
     *
     * @return Domain name tokens
     */
    private Collection<String> getDomainNameAsTokens(Boolean withWindow) {
        if (!domainNameAsToken.containsKey(withWindow)) {LinkedList<String> result = new LinkedList<>();
            String domain = this.getDomainName().toString().toLowerCase().replaceAll("[^a-z0-9]", "");
            List<String> split = Splitter.fixedLength(this.getNGramSize()).splitToList(domain);
            result.addAll(split);
    
            if (withWindow) {
                for (int i = 1; i < this.getNGramSize(); ++i) {
                    String modifiedDomain = domain.substring(i);
                    List<String> modifiedSplits = Splitter.fixedLength(this.getNGramSize()).splitToList(modifiedDomain);
                    result.addAll(modifiedSplits);
                }
            }
            domainNameAsToken.put(withWindow,result);
        }
        return domainNameAsToken.get(withWindow);
    }
    
    /**
     * Builder class.
     */
    public static class Builder {
        /**
         * Instance to be returned.
         */
        DomainDistribution instance;
        
        /**
         * Builder initialiser.
         */
        private Builder() {
            this.instance = new DomainDistribution();
        }
        
        /**
         * Sets the size of the nGram.
         *
         * @param nGramSize nGram Size.
         *
         * @return Builder object with updated value.
         */
        public Builder nGramSize(Integer nGramSize) {
            this.instance.setNGramSize(nGramSize);
            return this;
        }
        
        /**
         * Sets the domain name.
         *
         * @param domainName Fully Qualified Domain Name.
         *
         * @return Builder object with updated value.
         */
        public Builder domainName(InternetDomainName domainName) {
            this.instance.setDomainName(domainName);
            return this;
        }
        
        /**
         * Finalise and build the object.
         *
         * @return Domain Distribution object.
         */
        public DomainDistribution build() {
            assert this.instance.getNGramSize() != null;
            assert this.instance.getDomainName() != null;
            return this.instance;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public void setDomainName(InternetDomainName fqdn) {
        this.domainName = fqdn;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public InternetDomainName getDomainName() {
        return this.domainName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getCosineDistance(InternetDomainName fqdn) {
        return new CosineDistance().apply(this.domainName.toString(), fqdn.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getHammingDistance(InternetDomainName fqdn) {
        return new HammingDistance().apply(this.domainName.toString(), fqdn.toString()).doubleValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getJaccardDistance(InternetDomainName fqdn) {
        return new JaccardDistance().apply(this.domainName.toString(), fqdn.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getJaccardSimilarity(InternetDomainName fqdn) {
        return new JaccardSimilarity().apply(this.domainName.toString(), fqdn.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getJaroWinklerDistance(InternetDomainName fqdn) {
        return new JaroWinklerDistance().apply(this.domainName.toString(), fqdn.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getLevenshteinDistance(InternetDomainName fqdn) {
        return new LevenshteinDistance().apply(this.domainName.toString(), fqdn.toString()).doubleValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getLongestCommonSubsequence(InternetDomainName fqdn) {
        return new LongestCommonSubsequence().apply(this.domainName.toString(), fqdn.toString()).doubleValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getLongestCommonSubsequenceDistance(InternetDomainName fqdn) {
        return new LongestCommonSubsequenceDistance().apply(this.domainName.toString(), fqdn.toString()).doubleValue();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getEntropy() {
        Double sum = 0.0;
        try {
            Collection<String> nGrams = this.getDomainNameAsTokens(true);
            double log2 = Math.log(2);
            for (String nGram : nGrams) {
                try {
                    Double p = this.englishDistribution.getNGramFrequency(nGram);
                    // If the probability is zero, then assume the term is zero.
                    if (p == null || !Double.isFinite(p) || p == 0) {
                        continue;
                    }
                    Double log2p = Math.log(p) / log2;
                    sum += p * log2p;
                }
                catch (IllegalArgumentException ex) {
                    // Passed a partial chuck, ignore it.
                }
            }
            return -sum;
        }
        catch (Exception ex) {
            return Double.NaN;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getPronounceabilityScore(nGramLanguageDistribution targetDistribution)
            throws IllegalArgumentException {
        Double num = 0.0;
        Double det = (double) targetDistribution.getSumOfOccurences() - this.getNGramSize() + 1;
        if (det <= 0) {
            return 0.0;
        }
        try {
            // Be aware that this is different from normality score and entropy. It does cycle over a different collection.
            for (String nGram : this.getDomainNameAsTokens(true)) {
                double nGramCount;
                try {
                    nGramCount = targetDistribution.getNGramCount(nGram);
                }
                catch (IllegalArgumentException ex) {
                    // Passed a partial chuck, ignore it.
                    continue;
                }
                num += nGramCount;
            }
            double result = num / det;
            if (Double.isFinite(result)) {
                return result;
            }
            else {
                throw new ArithmeticException("Result is infinite!!");
            }
        }
        catch (Exception ex) {
            return Double.NaN;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getNormalityScore(nGramLanguageDistribution targetDistribution) {
        Double num = 0.0;
        Double det = (double) this.getSumOfOccurences() - this.getNGramSize() + 1;
        if (det <= 0) {
            return 0.0;
        }
        try {
            for (Map.Entry<String, Double> entry : this.getOccurrences().entrySet()) {
                double v;
                try {
                    v = entry.getValue() * targetDistribution.getNGramFrequency(entry.getKey());
                }
                catch (IllegalArgumentException ex) {
                    // Passed a partial chuck, ignore it.
                    continue;
                }
                num += v;
            }
            
            double result = num / det;
            if (Double.isFinite(result)) {
                return result;
            }
            else {
                throw new ArithmeticException("Result is infinite!!");
            }
        }
        catch (Exception ex) {
            return Double.NaN;
        }
    }
}
