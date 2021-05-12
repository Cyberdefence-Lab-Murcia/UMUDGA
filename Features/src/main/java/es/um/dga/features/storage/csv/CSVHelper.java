/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage.csv;

import com.google.common.net.InternetDomainName;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.nlp.utils.FeaturesFormatter;
import es.um.dga.features.utils.Settings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * CSV support class.
 */
public class CSVHelper extends FeaturesFormatter {

    public CSVHelper(SortedSet<String> featuresNames) {
        this.featuresNames.addAll(featuresNames);
    }

    public CSVHelper() {
        this.featuresNames = new TreeSet<>((o1, o2) -> {
            if (o1.equals("class")) {
                if (o2.equals("class")) {
                    return 0;
                }
                return +1;
            }
            if (o2.equals("class")) {
                return -1;
            }
            return o1.compareTo(o2);
        });
    }

    /**
     * Loads a list of FQDNs.
     *
     * @param projectRelativePath Data source path relative to the project main folder specified in config file.
     * @return Collection of validated {@link com.google.common.net.InternetDomainName}
     * @throws IOException Any IO exception.
     */
    public static Collection<InternetDomainName> loadFQDNs(String projectRelativePath) throws IOException {
        return loadFQDNs(Paths.get(Settings.getAbsoluteProjectPath() + projectRelativePath));
    }

    /**
     * Loads a list of FQDNs.
     *
     * @param absolutePathFile Data source path relative to the project main folder specified in config file.
     * @return Collection of validated {@link com.google.common.net.InternetDomainName}
     * @throws IOException Any IO exception.
     */
    public static Collection<InternetDomainName> loadFQDNs(File absolutePathFile) throws IOException {
        return loadFQDNs(absolutePathFile.toPath());
    }

    /**
     * Loads a list of FQDNs.
     *
     * @param absolutePath Data source absolute path.
     * @return Collection of validated {@link com.google.common.net.InternetDomainName}
     * @throws IOException Any IO exception.
     */
    public static Collection<InternetDomainName> loadFQDNs(Path absolutePath) throws IOException {

        Collection<InternetDomainName> domainNames = new HashSet<>();

        Reader in = new FileReader(absolutePath.toFile());
        Iterable<CSVRecord> records = CSVFormat.TDF.parse(in);

        DomainValidator domainValidator = DomainValidator.getInstance(false);
        for (CSVRecord record : records) {
            String recordDomain = record.get(0);
            try {
                if (StringUtils.isNotEmpty(recordDomain)) {
                    if (domainValidator.isValid(recordDomain)) {
                        InternetDomainName domainName = InternetDomainName.from(recordDomain);
                        if (!domainNames.add(domainName)) {
                            Settings.getLogger().warning("Domain " + recordDomain + " is a duplicate.");
                        }
                    }
                    else {
                        Settings.getLogger().warning(
                                "Domain " + recordDomain + " is not valid according to " + domainValidator.getClass());
                    }
                }
                else {
                    Settings.getLogger().warning("Empty string!");
                }
            }
            catch (Exception ex) {
                Settings.getLogger().log(Level.WARNING, "Domain " + recordDomain + " is not a valid FQDN.", ex);
            }
        }

        return domainNames;
    }

    /**
     * Features map with name and object type.
     */
    private SortedSet<String> featuresNames;

    /**
     * Sets the feature names (a.k.a. the CSV header).
     *
     * @param featuresNames Features collection with type.
     */
    public void setFeaturesNames(Set<String> featuresNames) {
        this.featuresNames.addAll(featuresNames);
    }

    /**
     * Sets the feature names (a.k.a. the CSV header).
     *
     * @param featuresNames Features collection.
     */
    public void setFeaturesNames(Collection<Feature> featuresNames) {
        featuresNames.forEach(feature -> this.featuresNames.add(feature.getKey()));
    }

    @Override
    public void addFeaturesName(Feature feature) {
        this.featuresNames.add(feature.getKey());
    }

    public void addFeaturesName(String featureName) {
        this.featuresNames.add(featureName);
    }

    @Override
    public SortedSet<String> getFeatureNames() {
        return this.featuresNames;
    }

    /**
     * Convert to string the full CSV header.
     * Features are sorted by natural order provided by the {@link java.util.SortedMap}.
     *
     * @param relationName Relation name. WIll be stripped of any space.
     * @return String header.
     */
    @Override
    public String getCompleteHeader(@NotNull String relationName) {
        StringBuilder result = new StringBuilder();
        for (String featureName : this.featuresNames) {
            result.append(featureName);
            result.append(",");
        }
        result.append(System.lineSeparator());
        return result.toString();
    }
}
