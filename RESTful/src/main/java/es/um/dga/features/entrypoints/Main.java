/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.entrypoints;

import static es.um.dga.features.storage.FileManagement.getFiles;
import static es.um.dga.features.utils.DateHelper.humanReadableDifference;

import com.google.common.net.InternetDomainName;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import es.um.dga.features.nlp.Analyzer;
import es.um.dga.features.nlp.utils.EnglishCache;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.storage.arff.ARFFHelper;
import es.um.dga.features.storage.csv.CSVHelper;
import es.um.dga.features.utils.Settings;

public class Main {

    //private static BufferedWriter csvAllWriter;
    //private static final Object csvAllLock = new Object();
    //private boolean once = false;

    public static void main(String[] args) throws Exception {

        List<String> argsList = Arrays.asList(args);
        Settings.ENABLE_BASE = argsList.contains("B");
        Settings.ENABLE_1G = argsList.contains("1G");
        Settings.ENABLE_2G = argsList.contains("2G");
        Settings.ENABLE_3G = argsList.contains("3G");
        Settings.setDataAbsolutePath("/home/mattia/UMU-DGA-Data/");

        new Main().run();
        //csvAllWriter.close();
    }

    private void run() throws Exception {

        // Cache the English distribution objects and clear the connection.
        EnglishCache.getEnglishDistribution(1);
        EnglishCache.getEnglishDistribution(2);
        EnglishCache.getEnglishDistribution(3);
        //MongoDBHelper.closeAllCollectionHelpers();

        List<String> malwareFamilies = new LinkedList<>();
        malwareFamilies.add("legit");
        malwareFamilies.add("ccleaner");
        malwareFamilies.add("gozi_rfc4343");
        malwareFamilies.add("kraken_v1");
        malwareFamilies.add("kraken_v2");
        malwareFamilies.add("locky");
        malwareFamilies.add("matsnu");
        malwareFamilies.add("murofet_v1");
        malwareFamilies.add("murofet_v2");
        malwareFamilies.add("murofet_v3");
        malwareFamilies.add("necurs");
        malwareFamilies.add("nymaim");
        malwareFamilies.add("padcrypt");
        malwareFamilies.add("pizd");
        malwareFamilies.add("proslikefan");
        malwareFamilies.add("pushdo");
        malwareFamilies.add("pykspa");
        malwareFamilies.add("pykspa_noise");
        malwareFamilies.add("qadars");
        malwareFamilies.add("qakbot");
        malwareFamilies.add("ramdo");
        malwareFamilies.add("ramnit");
        malwareFamilies.add("ranbyus_v1");
        malwareFamilies.add("ranbyus_v2");
        malwareFamilies.add("rovnix");
        malwareFamilies.add("shiotob");
        malwareFamilies.add("simda");
        malwareFamilies.add("sisron");
        malwareFamilies.add("suppobox_1");
        malwareFamilies.add("suppobox_2");
        malwareFamilies.add("suppobox_3");
        malwareFamilies.add("symmi");
        malwareFamilies.add("tempedreve");
        malwareFamilies.add("tinba");
        malwareFamilies.add("vawtrak_v1");
        malwareFamilies.add("vawtrak_v2");
        malwareFamilies.add("vawtrak_v3");
        malwareFamilies.add("zeus-newgoz");
        malwareFamilies.add("alureon");
        malwareFamilies.add("banjori");
        malwareFamilies.add("bedep");
        malwareFamilies.add("chinad");
        malwareFamilies.add("corebot");
        malwareFamilies.add("cryptolocker");
        malwareFamilies.add("dircrypt");
        malwareFamilies.add("dyre");
        malwareFamilies.add("fobber_v1");
        malwareFamilies.add("fobber_v2");
        malwareFamilies.add("gozi_gpl");
        malwareFamilies.add("gozi_luther");
        malwareFamilies.add("gozi_nasa");

        //        ExecutorService executorService = Executors.newFixedThreadPool(
        //                malwareFamilies.size() > Runtime.getRuntime().availableProcessors() ?
        //                        (Runtime.getRuntime().availableProcessors() - 2) : malwareFamilies.size(),
        //                r -> new Thread(r, "Malware Families Pool"));
        //ExecutorService executorService = Executors.newFixedThreadPool(1);

        //List<Future<MalwareFamily>> futuresMalwareFamilies = new ArrayList<>();

        /*
        File csvAllFile = new File(
                Settings.getDataFolder() + "/all/lastRun" + Settings.getFeatureCapabilities() + "" + ".csv");
        csvAllWriter = new BufferedWriter(new FileWriter(csvAllFile, true));
        
        if (!(new File(Settings.getDataFolder() + "/all/")).exists()) {
            (new File(Settings.getDataFolder() + "/all/")).mkdirs();
        }
        */

        for (String malwareVariant : malwareFamilies) {
            String malwareVariantDirectory = Settings.getFQDNDataDirectory(malwareVariant);
            MalwareFamily family = new MalwareFamily(malwareVariantDirectory);
            //futuresMalwareFamilies.add(executorService.submit(family));
            family.call();
            family = null;
            System.gc();
        }

        //        executorService.shutdown();
        //
        //        for (Future<MalwareFamily> futuresMalwareFamily : futuresMalwareFamilies) {
        //            try {
        //                futuresMalwareFamily.get();
        //            } catch (Exception ex) {
        //                Settings.getLogger().log(Level.WARNING, "Error while analysing the malware family", ex);
        //            }
        //        }
    }

    class MalwareFamily implements Callable<MalwareFamily> {

        private String name;

        private File root;

        private File listDirectory;

        private File arffDirectory;

        private File csvDirectory;

        MalwareFamily(String directory) {

            this.root = new File(directory);
            this.name = this.root.getName();
            this.listDirectory = new File(this.root.getAbsolutePath() + "/list");
            this.arffDirectory = new File(this.root.getAbsolutePath() + "/arff");
            this.csvDirectory = new File(this.root.getAbsolutePath() + "/csv");

            if (!this.arffDirectory.exists()) {
                this.arffDirectory.mkdirs();
            }
            if (!this.csvDirectory.exists()) {
                this.csvDirectory.mkdirs();
            }
        }

        /**
         * Gets the 'name' property value.
         *
         * @return value of name##
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the 'root' property value.
         *
         * @return value of root##
         */
        public File getRoot() {
            return root;
        }

        /**
         * Gets the 'listDirectory' property value.
         *
         * @return value of listDirectory##
         */
        public File getListDirectory() {
            return listDirectory;
        }

        @Override
        public MalwareFamily call() throws Exception {
            Thread.currentThread().setName(String.format("%1$12s", this.name));
            LocalDateTime start = LocalDateTime.now();
            List<File> files = getFiles(this.listDirectory);

            // Check all the files
            //for (File file : files) {
            // Check only the biggest one
            Optional<File> maxOpt = files.stream().max(Comparator.comparingLong(File::length));
            assert maxOpt.isPresent();
            File file = maxOpt.get();
            //File file = new File(this.listDirectory.getAbsolutePath() + "/10000.txt");
            {
                LocalDateTime startL = LocalDateTime.now();
                Collection<InternetDomainName> internetDomainNames = CSVHelper.loadFQDNs(file);

                LocalDateTime startan = LocalDateTime.now();
                ExecutorService localExecutor = Executors.newFixedThreadPool(Runtime.getRuntime()
                                                                                    .availableProcessors() - 1);
                List<CompletableFuture> futureLines = new ArrayList<>();

                InternetDomainName firstFQDN = internetDomainNames.stream()
                                                                  .findFirst()
                                                                  .map(domainName -> {
                                                                      internetDomainNames.remove(
                                                                              domainName);
                                                                      return domainName;
                                                                  })
                                                                  .orElse(null); // This should always be a FQDN, never null.

                Analyzer first = new Analyzer(firstFQDN, name);
                first.computeAllFeatures();
                SortedSet<Feature> firstFeatures = new TreeSet<>(first.getAllFeatures());
                first = null;
                firstFQDN = null;
                System.gc();

                StringBuilder fileLines = new StringBuilder();

                ARFFHelper arffHelper = new ARFFHelper();
                arffHelper.setFeaturesNames(firstFeatures);
                arffHelper.addFeaturesName("class", Collections.singleton(name));
                //futureLines.add(localExecutor.submit(() -> arffHelper.getCompleteHeader(name)));
                //futureLines.get(0).get();
                fileLines.append(arffHelper.getFormattedFeatureValue(firstFeatures));
                //futureLines.add(makeCompletableFuture(
                //localExecutor.submit(() -> arffHelper.getFormattedFeatureValue(firstFeatures))
                //));

                CSVHelper csvHelper = new CSVHelper();
                csvHelper.setFeaturesNames(firstFeatures);
                csvHelper.addFeaturesName("class");

                for (InternetDomainName internetDomainName : internetDomainNames) {
                    futureLines.add(makeCompletableFuture(localExecutor.submit(() -> {
                        Analyzer analyzer = new Analyzer(internetDomainName, name);
                        analyzer.computeAllFeatures();
                        SortedSet<Feature> allFeatures = new TreeSet<>(analyzer.getAllFeatures());
                        analyzer = null;
                        String formattedFeatureValue = arffHelper.getFormattedFeatureValue(
                                allFeatures);
                        allFeatures = null;
                        // avoid double memory
                        fileLines.append(formattedFeatureValue);
                        return;
                    })));
                }

                localExecutor.shutdown();

                CompletableFuture[] futures = futureLines.toArray(new CompletableFuture[0]);
                futureLines = null;

                CompletableFuture.allOf(futures);
                futures = null;
/*
                for (Future<String> stringFuture : futureLines) {
                    //fileLines.append(stringFuture.get());
                    stringFuture.get();
                }
*/
                localExecutor = null;
                System.gc();

                LocalDateTime startWA = LocalDateTime.now();
                File arffFile = new File(this.root.getAbsolutePath() + "/arff/" + file.getName() + Settings
                        .getFeatureCapabilities() + ".arff");
                BufferedWriter arffWriter = new BufferedWriter(new FileWriter(arffFile));
                arffWriter.write(arffHelper.getCompleteHeader(name));
                arffWriter.append(fileLines);
                arffWriter.close();

                LocalDateTime startWC = LocalDateTime.now();
                File csvFile = new File(this.root.getAbsolutePath() + "/csv/" + file.getName() + Settings
                        .getFeatureCapabilities() + ".csv");
                BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile));
                csvWriter.write(csvHelper.getCompleteHeader(name));
                csvWriter.append(fileLines);
                csvWriter.close();
                /*
                synchronized (csvAllLock) {
                    if (!once) {
                        csvAllWriter.write(csvHelper.getCompleteHeader(name));
                        once = true;
                    }
                    csvAllWriter.append(fileLines);
                }
                */
                // Format: CLASS SIZE LOAD ANALYSIS WARFF WCSV TOTAL
                Settings.getLogger()
                        .log(Level.INFO,
                             String.format("%1$16s",
                                           file.getName()) + "\t" + humanReadableDifference(startL,
                                                                                            startan) + "\t" + humanReadableDifference(
                                     startan,
                                     startWA) + "\t" + humanReadableDifference(startWA,
                                                                               startWC) + "\t" + humanReadableDifference(
                                     startWC,
                                     LocalDateTime.now()) + "\t" + humanReadableDifference(start,
                                                                                           LocalDateTime
                                                                                                   .now()));

            }

            return this;
        }

    }

    public static <T> CompletableFuture<T> makeCompletableFuture(Future<T> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
