/**
 * Project: DomainNameProfiler Copyright (c) 2017 University of Murcia
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
public final class EnglishEmptyDistribution extends AbstractNGramDistribution
        implements nGramLanguageDistribution {
    
    /**
     * Private constructor.
     */
    private EnglishEmptyDistribution() {
        super();
        // NOTHING ELSE TO DO.
    }
    
    /**
     * Gets the English Distribution for the requested nGram size.
     *
     * @param nGramSize nGram size.
     *
     * @return English Distribution.
     */
    public static EnglishEmptyDistribution loadDistribution(Integer nGramSize) {
        EnglishEmptyDistribution distribution = new EnglishEmptyDistribution();
        distribution.setNGramSize(nGramSize);
        distribution.loadData();
        
        return distribution;
    }
    
    /**
     * Load all the data required to build this distribution.
     */
    @Override
    public void loadData() {
        this.frequencyDistribution = new HashMap<>();
        this.countDistribution = new HashMap<>();
        
        //MongoCollection<Document> documentCollection = MongoDBHelper.getCollection(this.getNGramSize() + "GRAM");
        
        //FindIterable<Document> documents = documentCollection.find();
        
        //documents.forEach((Consumer<? super Document>) document -> {
        //    String tmpKey = document.get(this.getNGramSize() + "-gram").toString().toLowerCase();
        //    this.frequencyDistribution.put(tmpKey, 0.0);
        //    this.countDistribution.put(tmpKey, 0.0);
        //});
        
        try {
            CSVParser parser = new CSVParser(new FileReader(new File(Settings.getLanguageDataDirectory(
                    "english") + this.getNGramSize() + ".csv")),
                    CSVFormat.TDF.withFirstRecordAsHeader());
            for (CSVRecord record : parser.getRecords()) {
                this.countDistribution.put(record.get("Token"), 0.0);
                this.frequencyDistribution.put(record.get("Token"), 0.0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Double getEntropy() {
        return Double.NaN;
    }
    
    /**
     * Not defined for empty distributions.
     *
     * @param targetDistribution Language Distribution to be compared with.
     *
     * @return Double.NaN
     */
    @Override
    public Double getPronounceabilityScore(nGramLanguageDistribution targetDistribution) {
        return Double.NaN;
    }
    
    /**
     * Not defined for a language.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Double.NaN
     */
    @Override
    public Double getNormalityScore(nGramLanguageDistribution targetDistribution) {
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
    
    /**
     * Gets the Descriptive Statistics object.
     *
     * @return Statistics.
     */
    @Deprecated
    private DescriptiveStatistics getStatistics() {
        throw new UnsupportedOperationException(
                "Use default DescriptiveStatistics constructor instead of this.");
    }
}
