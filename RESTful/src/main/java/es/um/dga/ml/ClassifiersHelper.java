/*
 * Project: DGA-RESTful
 * Copyright (c) 2020 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */
package es.um.dga.ml;

import com.google.common.net.InternetDomainName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.um.dga.features.nlp.Analyzer;
import es.um.dga.features.nlp.utils.Feature;
import es.um.dga.features.utils.DateHelper;
import es.um.dga.features.utils.Settings;
import es.um.dga.restful.DGAApplication;
import es.um.dga.restful.models.FQDN;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * Provides support methods for classification purposes.
 */
public class ClassifiersHelper {
    
    /**
     * Creates the classifier helper class. It requires the initialization.
     *
     * @param mode
     */
    public ClassifiersHelper(String mode) {
        this.mode = mode;
    }
    
    /**
     * Collection of models to be used for classification purposes.
     */
    private Collection<Classifier> classifiers;
    
    /**
     * Empty dataset guaranteed compatible with the loaded classifiers.
     */
    private Instances dataStructure;
    
    /**
     * Classifier recognised classes.
     */
    private List<String> classes;
    
    /**
     * Classifier mode. Either binary or multiclass.
     */
    private String mode = "assets/ml/binary";
    
    /**
     * Initializes the resources.
     *
     * @throws IOException Any exception during this process will be thrown.
     */
    public void initResources() throws IOException {
        LocalDateTime start = LocalDateTime.now();
        
        this.classifiers = new ArrayList<>();
        this.classes = new ArrayList<>();
        
        FileInputStream fileInputStream = new FileInputStream(Settings.getClassifierDataDirectory(mode) + "header_nodomain.arff");
        InputStream headerStream = Objects.requireNonNull(fileInputStream,
                "Missing resource ARFF header " + Settings.getClassifierDataDirectory(
                        mode) + "header_nodomain.arff");
        InputStreamReader dataset = new InputStreamReader(headerStream);
        dataStructure = new Instances(dataset);
        dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
        Enumeration<Object> classesEnumeration = dataStructure.classAttribute().enumerateValues();
        while (classesEnumeration.hasMoreElements()) {
            classes.add(classesEnumeration.nextElement().toString());
        }
        
        
        try (Stream<Path> walk = Files.walk(Paths.get(Settings.getClassifierModelDirectory(mode)))) {
            
            List<String> result = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
            
            result.forEach(this::loadClassifier);
            
        } catch (IOException ex) {
            Settings.getLogger().log(Level.SEVERE, "Error while loading the classifiers", ex);
            throw new RuntimeException("Cannot properly initialise the required resources.", ex);
        }
        
        Settings.getLogger().info("N. " + classifiers.size() + " models loaded in " + DateHelper.humanReadableDifference(start));
    }
    
    /**
     * Deserialize a classifier from the provided resource file.
     *
     * @param resource Filename of the model.
     */
    private void loadClassifier(String resource) {
        try {
            LocalDateTime start = LocalDateTime.now();
            Path path = Paths.get(resource);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()));
            Classifier cls = (Classifier) ois.readObject();
            this.classifiers.add(cls);
            
            ois.close();
            String name = cls.toString().substring(0, 20).replaceAll("\n", "");
            Settings.getLogger().fine("Model " + name + " loaded in " + DateHelper.humanReadableDifference(start));
        } catch (IOException ex) {
            Settings.getLogger().log(Level.SEVERE, "Classifier file IOException", ex);
        } catch (ClassNotFoundException ex) {
            Settings.getLogger().log(Level.SEVERE, "Cannot deserialize stored classifier.", ex);
        }
    }
    
    /**
     * Register a new model. If store is enabled, it also saves it for future uses.
     *
     * @param classifier The classifier to be registered.
     * @param store      If true, will serialize and save the model.
     *
     * @throws IOException Any writing IO exception.
     */
    public void registerClassifier(Classifier classifier, boolean store) throws IOException {
        LocalDateTime start = LocalDateTime.now();
        
        String name = classifier.toString();
        name = name.substring(0, name.indexOf("\n"));
        
        if (store) {
            File model = new File(Settings.getClassifierModelDirectory(mode) + name + ".model");
            FileOutputStream outputStream = new FileOutputStream(model, false);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(classifier);
            oos.flush();
            oos.close();
        }
        
        classifiers.add(classifier);
        Settings.getLogger().fine("Classifier " + name + " registered in " + DateHelper.humanReadableDifference(start));
    }
    
    public FQDN classify(FQDN fqdn) throws Exception {
        Analyzer analyzer = new Analyzer(InternetDomainName.from(fqdn.getFqdn()));
        analyzer.computeAllFeatures();
        Collection<Feature> allFeatures = analyzer.getAllFeatures();
        Map.Entry<String, Double> classify = this.classify(allFeatures);
        fqdn.setConfidence(classify.getValue());
        fqdn.setLabel(classify.getKey());
        return fqdn;
    }
    
    /**
     * Classify a collection of features. It takes care of converting the collection to an {@link weka.core.Instance} object and calculating
     * the voting majority.
     *
     * @param features Collection of features.
     *
     * @return
     *
     * @throws Exception Any exception raised by the classifier, except the ones related to missing or incompatible features.
     */
    public Map.Entry<String, Double> classify(Collection<Feature> features) throws Exception {
        
        DenseInstance instance = new DenseInstance(dataStructure.numAttributes());
        Instances test = dataStructure;
        
        for (Feature feature : features) {
            try {
                try {
                    instance.setValue(dataStructure.attribute(feature.getKey()), (double) feature.getValue());
                } catch (ClassCastException ex) {
                    if (!feature.getKey().equalsIgnoreCase("class")) {
                        instance.setValue(dataStructure.attribute(feature.getKey()), feature.getValue().toString());
                    } else {
                        Settings.getLogger().info("Skipping class feature");
                    }
                }
            } catch (NullPointerException ex) {
                Settings.getLogger().warning("Feature " + feature + " is not included in the training data.");
            } catch (Exception ex) {
                Settings.getLogger().warning("Feature " + feature + " - Exception: " + ex);
            }
        }
        test.add(instance);
        test.setClassIndex(test.numAttributes() - 1);
        
        HashMap<String, List<Double>> predictions = new HashMap<>();
        
        for (Classifier classifier : classifiers) {
            double[] doubles = classifier.distributionForInstance(test.firstInstance());
            for (int i = 0, doublesLength = doubles.length; i < doublesLength; i++) {
                double value = doubles[i];
                String label = classes.get(i);
                List<Double> values = predictions.getOrDefault(label, new ArrayList<>());
                values.add(value);
                predictions.put(label, values);
                Settings.getLogger()
                        .fine(classifier.toString().substring(0, 10) + " predicts " + label + " p: " + String.format("%.2f", value));
            }
        }
        
        Map.Entry<String, Double> max = new AbstractMap.SimpleEntry<String, Double>("", -1.0);
        
        for (Map.Entry<String, List<Double>> entry : predictions.entrySet()) {
            Double sum = 0.0;
            for (Double aDouble : entry.getValue()) {
                sum += aDouble;
            }
            Double avg = sum / entry.getValue().size();
            if (avg >= max.getValue()) {
                max = new AbstractMap.SimpleEntry<String, Double>(entry.getKey(), avg);
            }
            Settings.getLogger().fine("Aggregated: " + entry.getKey() + " (" + avg + ")");
        }
        Settings.getLogger().fine("Predicted " + max.getKey() + " with " + String.format("%.2f", max.getValue()) + " confidence.");
        return max;
    }
    
    public void testClassifiers(Instances test) throws Exception {
        Evaluation eval = new Evaluation(test);
        for (Classifier classifier : this.classifiers) {
            LocalDateTime start = LocalDateTime.now();
            eval.evaluateModel(classifier, test);
            Settings.getLogger().info(eval.toSummaryString());
            Settings.getLogger().fine("Classifier evaluated in: " + DateHelper.humanReadableDifference(start));
        }
    }
    
    public static void main(String[] args) throws Exception {
        Settings.getLogger().setLevel(Level.FINE);
        Settings.ENABLE_BASE = true;
        Settings.ENABLE_1G = true;
        Settings.ENABLE_2G = true;
        Settings.ENABLE_3G = true;
        
        DGAApplication.initializeStaticResources();
        
        ClassifiersHelper binaryClassifier = DGAApplication.binaryClassifier;
        
        Settings.getLogger().info("--- END OF INITIALIZATION ----------------------------");
        
        /*
        String test = Settings.getClassifierDataDirectory("binary") + "1000_nodomain.arff";
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(test);
        Instances data = source.getDataSet();
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        binaryClassifier.testClassifiers(data);
        
        Settings.getLogger().info("--- END OF EVALUATION ----------------------------");
        */
        /*
        Analyzer analyzer = new Analyzer(InternetDomainName.from("google.com"));
        analyzer.computeAllFeatures();
        Collection<Feature> allFeatures = analyzer.getAllFeatures();
        binaryClassifier.classify(allFeatures);
        
        Settings.getLogger().info("--- END OF TEST SAMPLE DOMAIN ----------------------------");
        */
        
        BufferedReader reader = new BufferedReader(new FileReader("/home/mattia/DGA-RESTful/data/binary/data/1000" +
                ".arff"));
        Instances data = new Instances(reader);
        reader.close();
        data.setClassIndex(data.numAttributes() - 1);
        
        
        Evaluation evaluation = new Evaluation(data);
        evaluation.useNoPriors();
        Classifier classifier = DGAApplication.binaryClassifier.classifiers.stream().findFirst().get();
        evaluation.evaluateModel(classifier, data);
        evaluation.evaluateModelOnce(classifier, data.firstInstance());
        System.out.println(evaluation.toSummaryString());
    }
}
