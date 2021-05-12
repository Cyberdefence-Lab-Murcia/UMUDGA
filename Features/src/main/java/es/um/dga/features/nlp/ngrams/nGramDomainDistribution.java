/*
 * Project: DomainNameProfiler
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */
package es.um.dga.features.nlp.ngrams;

import com.google.common.net.InternetDomainName;

/**
 * A boolean interface implementation.
 */
public interface nGramDomainDistribution extends nGramDistribution {
    
    /**
     * Provide the domain name to be analysed.
     * @param fqdn Fully Qualified Domain Name.
     */
    void setDomainName(InternetDomainName fqdn);
    
    /**
     * Gets the domain name to be analysed.
     * @return Fully Qualified Domain Name.
     */
    InternetDomainName getDomainName();
    
    /**
     * Gets the similarity score using the Cosine distance.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.CosineDistance
     */
    Double getCosineDistance(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Hamming distance.
     * @param fqdn Fully Qualified Domain Name.
     * @return Hamming Score.
     * @see org.apache.commons.text.similarity.HammingDistance
     */
    Double getHammingDistance(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Jaccard distance.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.JaccardDistance
     */
    Double getJaccardDistance(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Jaccard Similarity.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.JaccardSimilarity
     */
    Double getJaccardSimilarity(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Jaro Winkler distance.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.JaroWinklerDistance
     */
    Double getJaroWinklerDistance(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Levenshtein distance.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.LevenshteinDistance
     */
    Double getLevenshteinDistance(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Longest Common Subsequence.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.LongestCommonSubsequence
     */
    Double getLongestCommonSubsequence(InternetDomainName fqdn);
    
    /**
     * Gets the similarity score using the Longest Common Subsequence Distance.
     * @param fqdn Fully Qualified Domain Name.
     * @return Similarity Score.
     * @see org.apache.commons.text.similarity.LongestCommonSubsequenceDistance
     */
    Double getLongestCommonSubsequenceDistance(InternetDomainName fqdn);
}
