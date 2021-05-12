/*
 * Project: DomainNameProfiler
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.ngrams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.CanberraDistance;
import org.apache.commons.math3.ml.distance.ChebyshevDistance;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.nlp.utils.FeaturesFactory;

public abstract class AbstractNGramDistribution implements nGramDistribution {
    
    /**
     * Descriptive Statistics object.
     */
    protected DescriptiveStatistics statistics;
    
    /**
     * Descriptive Statistics object. It includes the frequencies obtained from the target language instead of the
     * ones calculated on the domain.
     */
    protected DescriptiveStatistics targetLanguageStatistics;
    
    /**
     * This nGram frequency distribution map.
     */
    protected Map<String, Double> frequencyDistribution;
    
    private double countdistValuesSum;
    private double countdistValuesSumSq;
    private double countdistValuesCount;
    
    /**
     * This nGram count distribution map.
     */
    protected Map<String, Double> countDistribution;
    /**
     * Already computed features.
     */
    protected Collection<Feature> featureCollection;
    /**
     * Size of this nGram distribution.
     */
    private Integer nGramSize;
    /**
     * Feature Name.
     */
    private String name;
    
    /**
     * English Distribution.
     */
    protected nGramLanguageDistribution englishDistribution;
    
    /**
     * {@inheritDoc}
     */
    @Override public Double getSumOfOccurences() {
        if (!Double.isFinite(this.countdistValuesSum) || this.countdistValuesSum <= 0) {
            cacheOccurrencesStat();
        }
        return countdistValuesSum;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Double getCountOfOccurences() {
        if (!Double.isFinite(this.countdistValuesCount) || this.countdistValuesSum <= 0) {
            cacheOccurrencesStat();
        }
        return countdistValuesCount;
    }
    
    protected void cacheOccurrencesStat() {
        countdistValuesSum = 0.0;
        countdistValuesSumSq = 0.0;
        countdistValuesCount = 0.0;
    
        for (Map.Entry<String, Double> entry : this.countDistribution.entrySet()) {
            String nGram = entry.getKey();
            Double val = entry.getValue();
    
            if (val < 0) {
                throw new IllegalArgumentException("Something wrong here!");
            } else if (val > 0) {
                countdistValuesCount++;
                countdistValuesSum += val;
                countdistValuesSumSq += val * val;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTotalSumSqOfOccurrences() {
        if (!Double.isFinite(this.countdistValuesSumSq) || this.countdistValuesSumSq <= 0) {
            cacheOccurrencesStat();
        }
        return countdistValuesSumSq;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<String, Double> getValue() {
        if (this.countDistribution == null || this.countDistribution.isEmpty()) {
            this.loadData();
        }
        
        return this.countDistribution;
    }
    
    /**
     * {@inheritDoc}
     */
    public Integer getNGramSize() {
        return this.nGramSize;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setNGramSize(Integer nGramSize) {
        this.nGramSize = nGramSize;
    }
    
    /**
     * Gets the occurrences' map having the nGrams as key and their count as values.
     *
     * @return Frequency map.
     */
    public Map<String, Double> getOccurrences() {
        return this.countDistribution;
    }
    
    /**
     * {@inheritDoc}
     */
    public void computeFeatures() {
        
        if (this.countDistribution == null) {
            this.loadData();
        }
        
        this.featureCollection = new HashSet<Feature>();
        
        //Feature hist = this;
        //hist.setName(this.getFeatureBaseName() + "FR");
        //this.featureCollection.add(hist);
        // Disable the histogram output as distinct columns.
        //this.featureCollection
        //        .addAll(FeaturesFactory.from(this.getFeatureBaseName() + "HIST", this.frequencyDistribution));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DIST", this.getNGramsWithMoreOccurrencesThanThreshold(1).size()));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "REP", this.getNGramsWithMoreOccurrencesThanThreshold(2).size()));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_KL", this.getKullbackLeiblerDivergence(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_JI", this.getJaccardIndex(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_CA", this.getCanberraDistance(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_CH", this.getChebyshevDistance(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_EM", this.getEarthMoversDistance(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_EU", this.getEuclideanDistance(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "DST_MA", this.getManhattanDistance(englishDistribution)));
        
        // One single term at zero is enough to have this feature at zero.
        //this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "GMEAN", this.getGeometricMean()));
        
        
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "MEAN", this.getMean()));
        
        // The median is the 50th percentile.
        //this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "MED", this.getMedian()));
        
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "25P", this.get25Percentile()));
        
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "50P", this.get50Percentile()));
        
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "75P", this.get75Percentile()));
        
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "QMEAN", this.getQuadraticMean()));
        
        
        // This does not make sense. Removed.
        //this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "SUM", this.getSum()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "SUMSQ", this.getSumsq()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "VAR", this.getVariance()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "PVAR", this.getPopulationVariance()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "STD", this.getStandardDeviation()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "PSTD", this.getPopulationStandardDeviation()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "SKE", this.getSkewness()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "KUR", this.getKurtosis()));
    
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TSUM", this.getTargetLanguageSum()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TSUMSQ", this.getTargetLanguageSumsq()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TVAR", this.getTargetLanguageVariance()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TPVAR", this.getTargetLanguagePopulationVariance()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TSTD", this.getTargetLanguageStandardDeviation()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TPSTD", this.getTargetLanguagePopulationStandardDeviation()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TSKE", this.getTargetLanguageSkewness()));
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "TKUR", this.getTargetLanguageKurtosis()));
        
        this.featureCollection
                .add(FeaturesFactory.from(this.getFeatureBaseName() + "COV", this.getCovariance(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "KEN", this.getKendallsCorrelation(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "PEA", this.getPearsonsCorrelation(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "SPE", this.getSpearmansCorrelation(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "PRO", this.getPronounceabilityScore(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory
                .from(this.getFeatureBaseName() + "NORM", this.getNormalityScore(englishDistribution)));
        
        this.featureCollection.add(FeaturesFactory.from(this.getFeatureBaseName() + "E", this.getEntropy()));
    }
    
    /**
     * {@inheritDoc}
     */
    public Collection<Feature> getFeatures() {
        return this.featureCollection;
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getKullbackLeiblerDivergence(nGramDistribution targetDistribution) throws IllegalArgumentException {
        double[] firstDistribution = this.getValues();
        double[] secondDistribution = targetDistribution.getValues();
        
        if (firstDistribution.length != secondDistribution.length) {
            throw new IllegalArgumentException("The target distribution has a different size than this one.");
        }
        
        Double result = 0.0;
        for (int i = 0; i < firstDistribution.length; ++i) {
            if (firstDistribution[i] == 0) {
                continue;
            }
            if (secondDistribution[i] == 0.0) {
                continue;
            }
            
            result += secondDistribution[i] * Math.log(firstDistribution[i] / secondDistribution[i]);
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<String> getNGramsWithMoreOccurrencesThanThreshold(int threshold) {
        List<String> result = new ArrayList<>();
        
        //this.countDistribution.forEach((String, aDouble) -> {
        //    if (aDouble >= threshold) {
        //        result.add(String);
        //    }
        //});
        
        Iterator<Map.Entry<String, Double>> it = this.countDistribution.entrySet().iterator();
        // According to stack overflow this is the fastest way to do this.
        // https://stackoverflow.com/a/35558955
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            if (entry.getValue() >= threshold) {
                result.add(entry.getKey());
            }
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<String> getNGramsWithExactOccurrency(int threshold) {
        List<String> result = new ArrayList<>();
        //this.countDistribution.forEach((String, aDouble) -> {
        //    if (aDouble == threshold) {
        //        result.add(String);
        //    }
        //});
        Iterator<Map.Entry<String, Double>> it = this.countDistribution.entrySet().iterator();
        // According to stack overflow this is the fastest way to do this.
        // https://stackoverflow.com/a/35558955
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            if (entry.getValue() == threshold) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<String> getNGramsWithLessOccurrencesThanThreshold(int threshold) {
        List<String> result = new ArrayList<>();
        //this.countDistribution.forEach((String, aDouble) -> {
        //    if (aDouble <= threshold) {
        //        result.add(String);
        //    }
        //});
        Iterator<Map.Entry<String, Double>> it = this.countDistribution.entrySet().iterator();
        // According to stack overflow this is the fastest way to do this.
        // https://stackoverflow.com/a/35558955
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            if (entry.getValue() <= threshold) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    /*
    public Double getGeometricMean() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getGeometricMean();
    }
    */
    
    /**
     * {@inheritDoc}
     */
    public Double getMean() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getMean();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getMedian() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        try {
            double[] numArray = this.statistics.getSortedValues();
            if (numArray.length % 2 == 0) {
                return (numArray[numArray.length / 2] + numArray[numArray.length / 2 - 1]) / 2;
            }
            else {
                return numArray[numArray.length / 2];
            }
        }
        catch (Exception ex) {
            return Double.NaN;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double get25Percentile() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getPercentile(25);
    }
    
    /**
     * {@inheritDoc}
     */
    public Double get50Percentile() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getPercentile(50);
    }
    
    /**
     * {@inheritDoc}
     */
    public Double get75Percentile() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getPercentile(75);
    }

    /**
     * {@inheritDoc}
     */
    public Double getQuadraticMean() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getQuadraticMean();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getSkewness() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getSkewness();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getKurtosis() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getKurtosis();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getSum() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.getSumOfOccurences();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getSumsq() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.getTotalSumSqOfOccurrences();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getVariance() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getVariance();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getStandardDeviation() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getStandardDeviation();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getPopulationVariance() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.statistics.getPopulationVariance();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getPopulationStandardDeviation() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return Math.sqrt(this.statistics.getPopulationVariance());
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguageSum() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getSum();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguageSumsq() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getSumsq();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguageVariance() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getVariance();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguageStandardDeviation() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getStandardDeviation();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguagePopulationVariance() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getPopulationVariance();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguagePopulationStandardDeviation() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return Math.sqrt(this.targetLanguageStatistics.getPopulationVariance());
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguageSkewness() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getSkewness();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getTargetLanguageKurtosis() {
        //if (this.statistics == null) {
        //this.loadData();
        //}
        return this.targetLanguageStatistics.getKurtosis();
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getCovariance(nGramDistribution targetDistribution) {
        return new Covariance().covariance(this.getSortedValues(), targetDistribution.getSortedValues());
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getPearsonsCorrelation(nGramDistribution targetDistribution) {
        return new PearsonsCorrelation().correlation(this.getSortedValues(), targetDistribution.getSortedValues());
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getKendallsCorrelation(nGramDistribution targetDistribution) {
        return new KendallsCorrelation().correlation(this.getSortedValues(), targetDistribution.getSortedValues());
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getSpearmansCorrelation(nGramDistribution targetDistribution) {
        return new SpearmansCorrelation().correlation(this.getSortedValues(), targetDistribution.getSortedValues());
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getNGramFrequency(String nGram) throws IllegalArgumentException {
        if (nGram.length() != this.getNGramSize()) {
            throw new IllegalArgumentException("The nGram passed as argument is not compatible with this instance.");
        }
        return this.frequencyDistribution.getOrDefault(nGram, 0.0);
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getNGramCount(String nGram) throws IllegalArgumentException {
        if (nGram.length() != this.getNGramSize()) {
            throw new IllegalArgumentException("The nGram passed as argument is not compatible with this instance.");
        }
        return this.countDistribution.getOrDefault(nGram, 0.0);
    }
    
    /**
     * {@inheritDoc}
     */
    public double[] getSortedValues() {
        return this.cachedStatisticsSortedValues;
        //return this.statistics.getSortedValues();
    }
    
    private double[] cachedStatisticsValues;
    private double[] cachedStatisticsSortedValues;
    
    public void updateStatisticsCache() {
        this.cachedStatisticsValues = this.statistics.getValues();
        this.cachedStatisticsSortedValues = this.statistics.getSortedValues();
    }
    
    /**
     * {@inheritDoc}
     */
    public double[] getValues() {
        return this.cachedStatisticsValues;
        //return this.statistics.getValues();
    }
    
    /**
     * Get nGram feature base name.
     *
     * @return nGramSize feature base name for features.
     */
    protected String getFeatureBaseName() {
        return "NLP_" + this.getNGramSize() + "G_";
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<String, Double> getFrequencies() {
        return this.frequencyDistribution;
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getJaccardIndex(nGramDistribution targetDistribution) throws IllegalArgumentException {
        Double num = 0.0;
        Double den = 0.0;
        
        for (int i = 0; i < this.getValues().length; i++) {
            num += this.getValues()[i] < targetDistribution.getValues()[i] ?
                    this.getValues()[i] :
                    targetDistribution.getValues()[i];
            
            den += this.getValues()[i] > targetDistribution.getValues()[i] ?
                    this.getValues()[i] :
                    targetDistribution.getValues()[i];
        }
        
        return num / den;
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getCanberraDistance(nGramDistribution targetDistribution) throws IllegalArgumentException {
        if (this.getValues().length != targetDistribution.getValues().length) {
            throw new IllegalArgumentException("Distributions have different sizes.");
        }
        try {
            return new CanberraDistance().compute(this.getValues(), targetDistribution.getValues());
        }
        catch (DimensionMismatchException ex) {
            throw new IllegalArgumentException("Distributions have different sizes.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getChebyshevDistance(nGramDistribution targetDistribution) throws IllegalArgumentException {
        if (this.getValues().length != targetDistribution.getValues().length) {
            throw new IllegalArgumentException("Distributions have different sizes.");
        }
        try {
            return new ChebyshevDistance().compute(this.getValues(), targetDistribution.getValues());
        }
        catch (DimensionMismatchException ex) {
            throw new IllegalArgumentException("Distributions have different sizes.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getEarthMoversDistance(nGramDistribution targetDistribution) throws IllegalArgumentException {
        
        if (this.getValues().length != targetDistribution.getValues().length) {
            throw new IllegalArgumentException("Distributions have different sizes.");
        }
        try {
            return new EarthMoversDistance().compute(this.getValues(), targetDistribution.getValues());
        }
        catch (DimensionMismatchException ex) {
            throw new IllegalArgumentException("Distributions have different sizes.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getEuclideanDistance(nGramDistribution targetDistribution) throws IllegalArgumentException {
        
        if (this.getValues().length != targetDistribution.getValues().length) {
            throw new IllegalArgumentException("Distributions have different sizes.");
        }
        try {
            return new EuclideanDistance().compute(this.getValues(), targetDistribution.getValues());
        }
        catch (DimensionMismatchException ex) {
            throw new IllegalArgumentException("Distributions have different sizes.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Double getManhattanDistance(nGramDistribution targetDistribution) throws IllegalArgumentException {
        
        if (this.getValues().length != targetDistribution.getValues().length) {
            throw new IllegalArgumentException("Distributions have different sizes.");
        }
        try {
            return new ManhattanDistance().compute(this.getValues(), targetDistribution.getValues());
        }
        catch (DimensionMismatchException ex) {
            throw new IllegalArgumentException("Distributions have different sizes.", ex);
        }
    }
}
