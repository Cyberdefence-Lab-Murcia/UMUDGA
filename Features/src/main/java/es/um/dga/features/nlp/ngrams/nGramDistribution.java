/*
 * Project: DomainNameProfiler
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.ngrams;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import es.um.dga.features.nlp.utils.Feature;

/**
 * A boolean interface implementation.
 */
public interface nGramDistribution {
    
    /**
     * Gets the size of the nGram.
     *
     * @return nGram size.
     */
    Integer getNGramSize();
    
    /**
     * Set the nGram size.
     * It must be between 1 and 9 included.
     *
     * @param nGramSize nGram size.
     */
    void setNGramSize(Integer nGramSize);
    
    /**
     * Load all the data required to build this distribution.
     */
    void loadData();
    
    /**
     * Compute all the features related to this distribution.
     */
    void computeFeatures();
    
    /**
     * Gets the collection of features available.
     *
     * @return Collection of all the features obtainable from this distribution.
     */
    Collection<Feature> getFeatures();
    
    /**
     * Calculates the Kullback-Leiver Divergence from the target distribution.
     *
     * @return The Kullback-Leiver Divergence.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     */
    Double getKullbackLeiblerDivergence(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Get the nGrams with a value above (or at least) the threshold (>=).
     *
     * @param threshold threshold to count the nGrams.
     *
     * @return Number of nGrams that have at least the minimumAmount of occurrences.
     */
    List<String> getNGramsWithMoreOccurrencesThanThreshold(int threshold);
    
    /**
     * Get the nGrams with a value below (or at maximum) the threshold (<=).
     *
     * @param threshold threshold to count the nGrams.
     *
     * @return Number of nGrams that have at maximum the minimumAmount of occurrences.
     */
    List<String> getNGramsWithLessOccurrencesThanThreshold(int threshold);
    
    /**
     * Get the nGrams with a value exactly equal to the threshold.
     *
     * @param threshold threshold to count the nGrams.
     *
     * @return Number of nGrams that have exactly this number of occurrences.
     */
    List<String> getNGramsWithExactOccurrency(int threshold);
    
    /**
     * Gets the Geometric Mean value of the frequency distribution.
     *
     * @return Double value representing the Geometric Mean value.
     */
    // Excluded because always zero.
    //Double getGeometricMean();
    
    /**
     * Gets the Kurtosis value of the frequency distribution.
     *
     * @return Double value representing the Kurtosis value.
     */
    Double getKurtosis();
    
    /**
     * Gets the Mean value of the frequency distribution.
     *
     * @return Double value representing the Mean value.
     */
    Double getMean();
    
    /**
     * Gets the Median value of the frequency distribution.
     *
     * @return Double value representing the Median value.
     * @deprecated It actually returns the 50th percentile.
     */
    Double getMedian();
    
    /**
     * Gets the 25th Percentile value of the frequency distribution.
     *
     * @return Double value representing the 25th Percentile value.
     */
    Double get25Percentile();
    
    /**
     * Gets the 50th Percentile value of the frequency distribution.
     *
     * @return Double value representing the 50th Percentile value.
     */
    Double get50Percentile();
    
    /**
     * Gets the 75th Percentile value of the frequency distribution.
     *
     * @return Double value representing the 75th Percentile value.
     */
    Double get75Percentile();
    
    /**
     * Gets the QuadraticMean value of the frequency distribution.
     *
     * @return Double value representing the QuadraticMean value.
     */
    Double getQuadraticMean();
    
    /**
     * Gets the Skewness value of the frequency distribution.
     *
     * @return Double value representing the Skewness value.
     */
    Double getSkewness();
    
    /**
     * Gets the Sum value of the frequency distribution.
     *
     * @return Double value representing the Sum value.
     * @deprecated It actually returns the number of nGrams.
     */
    Double getSum();
    
    /**
     * Gets the Sum Squared value of the frequency distribution.
     *
     * @return Double value representing the Sum Squared value.
     */
    Double getSumsq();
    
    /**
     * Gets the Variance value of the frequency distribution.
     *
     * @return Double value representing the Variance value.
     */
    Double getVariance();
    
    /**
     * Gets the Standard Deviation value of the frequency distribution.
     *
     * @return Double value representing the Standard Deviation value.
     */
    Double getStandardDeviation();
    
    /**
     * Gets the Population Variance value of the frequency distribution.
     *
     * @return Double value representing the Population Variance value.
     */
    Double getPopulationVariance();
    
    /**
     * Gets the Population Standard Deviation value of the frequency distribution.
     *
     * @return Double value representing the Population Standard Deviation value.
     */
    Double getPopulationStandardDeviation();
    
    
    /**
     * Gets the Sum value of the frequency distribution.
     *
     * @return Double value representing the Sum value.
     */
    Double getTargetLanguageSum();
    
    /**
     * Gets the Sum Squared value of the frequency distribution.
     *
     * @return Double value representing the Sum Squared value.
     */
    Double getTargetLanguageSumsq();
    
    /**
     * Gets the Variance value of the frequency distribution.
     *
     * @return Double value representing the Variance value.
     */
    Double getTargetLanguageVariance();
    
    /**
     * Gets the Standard Deviation value of the frequency distribution.
     *
     * @return Double value representing the Standard Deviation value.
     */
    Double getTargetLanguageStandardDeviation();
    
    /**
     * Gets the Population Variance value of the frequency distribution.
     *
     * @return Double value representing the Population Variance value.
     */
    Double getTargetLanguagePopulationVariance();
    
    /**
     * Gets the Population Standard Deviation value of the frequency distribution.
     *
     * @return Double value representing the Population Standard Deviation value.
     */
    Double getTargetLanguagePopulationStandardDeviation();
    
    /**
     * Gets the Skewness value of the frequency distribution.
     *
     * @return Double value representing the Skewness value.
     */
    Double getTargetLanguageSkewness();
    
    /**
     * Gets the Kurtosis value of the frequency distribution.
     *
     * @return Double value representing the Kurtosis value.
     */
    Double getTargetLanguageKurtosis();
    
    
    /**
     * Gets the covariance value with regards to the English profile.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Covariance value.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.stat.correlation.Covariance#covariance(double[], double[])
     */
    Double getCovariance(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Gets the Pearson's Correlation value with regards to the English profile.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Pearson's Correlation value.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.stat.correlation.PearsonsCorrelation#correlation(double[], double[])
     */
    Double getPearsonsCorrelation(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Gets the Kendalls's tau rank correlation coefficient value with regards to the English profile.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Kendalls's tau rank correlation coefficient value.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.stat.correlation.KendallsCorrelation#correlation(double[], double[])
     */
    Double getKendallsCorrelation(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Gets the Spearman's rank correlation coefficient value with regards to the English profile.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Spearman's rank correlation coefficient value.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.stat.correlation.SpearmansCorrelation#correlation(double[], double[])
     */
    Double getSpearmansCorrelation(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * The Jaccard index, also known as Intersection over Union and the Jaccard similarity coefficient (originally
     * coined coefficient de communauté by Paul Jaccard), is a statistic used for comparing the similarity and
     * diversity of sample sets. The Jaccard coefficient measures similarity between finite sample sets, and is
     * defined as the size of the intersection divided by the size of the union of the sample sets.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return JaccardIndex with respect to the target distribution.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index#Generalized_Jaccard_similarity_and_distance">Wikipedia
     * </a>
     */
    Double getJaccardIndex(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Calculates the Canberra distance between two points.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Camberra distance.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.ml.distance.CanberraDistance
     */
    Double getCanberraDistance(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Calculates the Chebyshev distance, the L∞ (max of abs) distance between two points.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Chebyshev distance.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.ml.distance.ChebyshevDistance
     */
    Double getChebyshevDistance(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Calculates the Earh Mover's distance (also known as Wasserstein metric) between two distributions.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Earth Movers distance.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.ml.distance.EarthMoversDistance
     */
    Double getEarthMoversDistance(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Calculates the Euclidean's distance, L2 (Euclidean) distance between two points.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Euclidean distance.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.ml.distance.EuclideanDistance
     */
    Double getEuclideanDistance(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Calculates the Manhattan's distance, L1 (sum of abs) distance between two points.
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Manhattan distance.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     * @see org.apache.commons.math3.ml.distance.ManhattanDistance
     */
    Double getManhattanDistance(nGramDistribution targetDistribution) throws IllegalArgumentException;
    
    /**
     * Gets the frequency of the specific nGram passed as argument.
     *
     * @param nGram nGram to be retrieved.
     *
     * @return nGram frequency.
     *
     * @throws IllegalArgumentException nGram size is different from the one specified for the instance.
     */
    Double getNGramFrequency(String nGram) throws IllegalArgumentException;
    
    /**
     * Gets the count of the specific nGram passed as argument.
     *
     * @param nGram nGram to be retrieved.
     *
     * @return nGram count or {@code null} if the nGram has a insignificant frequency.
     */
    Double getNGramCount(String nGram) throws IllegalArgumentException;
    
    /**
     * Gets the distribution as a sorted array of doubles.
     *
     * @return Distribution values as a sorted array of doubles.
     */
    double[] getSortedValues();
    
    /**
     * Gets the distribution as an array of doubles.
     *
     * @return Distribution values as an array of doubles.
     */
    double[] getValues();
    
    /**
     * Gets the frequencies' map having the nGrams as key and their frequencies as values.
     *
     * @return Frequency map.
     */
    Map<String, Double> getFrequencies();
    
    /**
     * Gets the entropy of this distribution using the frequency as probability distribution.
     * Calculated using as probabilities the frequencies of the English language distribution.
     *
     * @return Shannon Entropy of the distribution in object.
     */
    Double getEntropy();
    
    /**
     * Gets the nGram normality score according to the linguistic definition of phonotactics adherence.
     * A.k.a. the more permissible the combination of phonemes, the pronounceable the domain is.
     * <p>
     * In [1] it is referred as nGram Normality Score.
     * [1] S. Schiavoni, F. Maggi, L. Cavallaro, and S. Zanero, "Phoenix: DGA-Based Botnet Tracking and
     * Intelligence," in Detection of Intrusions and Malware, and Vulnerability Assessment: 11th International
     * Conference, DIMVA 2014, Egham, UK, July 10-11, 2014. Proceedings, S. Dietrich, Ed. Cham: Springer International
     * Publishing, 2014, pp. 192–211.
     *
     * @param targetDistribution Language Distribution to be compared with.
     *
     * @return Score of this nGram with regards to targetDistribution.
     *
     * @throws IllegalArgumentException The targetDistribution has a different number of elements than the
     *                                            one instantiated here.
     */
    Double getPronounceabilityScore(nGramLanguageDistribution targetDistribution);
    
    /**
     * Gets the nGram score according to "W. J. Song and B. Li, "A Method to Detect Machine Generated Domain Names
     * Based on Random Forest Algorithm," in 2016 International Conference on Information System and Artificial
     * Intelligence (ISAI), 2016, pp. 509–513."
     *
     * @param targetDistribution Distribution to be compared with.
     *
     * @return Score of this nGram with regards to targetDistribution.
     */
    Double getNormalityScore(nGramLanguageDistribution targetDistribution);
    
    /**
     * Return the sum of all the counts in this distribution.
     * @return Sum of all the counts in this distribution.
     */
    Double getSumOfOccurences();
}
