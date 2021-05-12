/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import es.um.dga.features.nlp.ngrams.AbstractNGramDistribution;
import es.um.dga.features.nlp.ngrams.nGramLanguageDistribution;
import es.um.dga.features.utils.Settings;

/**
 * Gets the nGram distribution from the English corpus.
 * TODO: Fix documentation to include Google nGram reference.
 */
public final class EnglishDistribution extends AbstractNGramDistribution implements nGramLanguageDistribution {
    
    /**
     * Private constructor.
     */
    private EnglishDistribution() {
        super();
        // NOTHING ELSE TO DO.
    }
    
    /**
     * Gets the English Distribution for the requested nGram size.
     *
     * @param nGramSize nGram size.
     *
     * @return English Distribution.
     *
     * @deprecated Prefer the cached method at
     * {@link es.um.dga.features.nlp.utils.EnglishCache#getEnglishDistribution(Integer)}
     */
    @Deprecated static EnglishDistribution loadDistribution(Integer nGramSize) {
        EnglishDistribution distribution = new EnglishDistribution();
        distribution.setNGramSize(nGramSize);
        distribution.loadData();
        
        return distribution;
    }
    
    /**
     * Load all the data required to build this distribution.
     */
    @Override public void loadData() {
        this.frequencyDistribution = new HashMap<>();
        this.countDistribution = new HashMap<>();
        this.statistics = new DescriptiveStatistics();
        
        //MongoCollection<Document> documentCollection = MongoDBHelper.getCollection(this.getNGramSize() + "GRAM");
        
        //FindIterable<Document> documents = documentCollection.find();
        
        //documents.forEach((Consumer<? super Document>) document -> {
        //    String tmpKey = document.get(this.getNGramSize() + "-gram").toString().toLowerCase();
        //    Double tmpFrValue = Double.valueOf(document.getString("frequency"));
        //    Double tmpCoValue = Double.valueOf(document.getString("*/*"));
        //    this.frequencyDistribution.put(tmpKey, tmpFrValue);
        //    this.countDistribution.put(tmpKey, tmpCoValue);
        //    this.statistics.addValue(tmpFrValue);
        //});
        
        try {
            CSVParser parser = new CSVParser(new FileReader(
                    new File(Settings.getDataFolder() + "english/" + this.getNGramSize() + ".csv")),
                    CSVFormat.TDF.withFirstRecordAsHeader());
            for (CSVRecord record : parser.getRecords()) {
                this.countDistribution.put(record.get("Token"), Double.valueOf(record.get("Count")));
                this.frequencyDistribution.put(record.get("Token"), Double.valueOf(record.get("Frequency")));
                this.statistics.addValue(Double.parseDouble(record.get("Frequency")));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        this.updateStatisticsCache();
    }
    
    @Override public Double getEntropy() {
        try {
            Double sum = 0.0;
            double log2 = Math.log(2);
            for (Double p : this.getFrequencies().values()) {
                if (p == null || !Double.isFinite(p) || p == 0) {
                    continue;
                }
                Double log2p = Math.log(p) / log2;
                sum += p * log2p;
            }
            return sum;
        }
        catch (Exception ex) {
            return Double.NaN;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getPronounceabilityScore(nGramLanguageDistribution targetDistribution) {
        try {
            // Be aware that this is different from normality score and entropy. It does cycle over a different collection.
            double sum = 0.0;
            for (String entry : targetDistribution.getFrequencies().keySet()) {
                double nGramCount = targetDistribution.getNGramCount(entry);
                sum += nGramCount;
            }
            Double num = sum;
            Double det = (double) targetDistribution.getSumOfOccurences() - this.getNGramSize() + 1;
            return num / det;
        }
        catch (Exception ex) {
            return Double.NaN;
        }
    }
    
    /**
     * Not defined for a language.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Double.NaN
     */
    @Override public Double getNormalityScore(nGramLanguageDistribution targetDistribution) {
        return Double.NaN;
    }
    
    /**
     * Returns a copy of the English empty frequency distribution.
     *
     * @return nGrams distribution.
     */
    Map<String, Double> getNGramsFrequencyDistribution() {
        return new HashMap<>(this.frequencyDistribution);
    }
    
    /**
     * Gets a copy of the English empty distribution.
     *
     * @return new HashMap of the nGrams distribution.
     */
    Map<String, Double> getNGramsCountDistribution() {
        return new HashMap<>(this.countDistribution);
    }
}
