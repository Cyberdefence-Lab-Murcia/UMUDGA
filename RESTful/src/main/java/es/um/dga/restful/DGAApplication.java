package es.um.dga.restful;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import es.um.dga.features.nlp.utils.EnglishCache;
import es.um.dga.features.nlp.utils.EnglishDistribution;
import es.um.dga.features.utils.DateHelper;
import es.um.dga.features.utils.Settings;
import es.um.dga.ml.ClassifiersHelper;
import es.um.dga.restful.pages.DomainAnalyzer;
import es.um.dga.restful.pages.Main;

/**
 * The java class declares root resource and provider classes.
 */
@ApplicationPath("/")
public class DGAApplication extends Application {

    /**
     * Binary classifier.
     */
    public static ClassifiersHelper binaryClassifier;

    public DGAApplication() {
        addPage(Main.class);
        addPage(DomainAnalyzer.class);
    
        Settings.ENABLE_BASE = true;
        Settings.ENABLE_1G = true;
        Settings.ENABLE_2G = true;
        Settings.ENABLE_3G = true;

        initializeStaticResources();
    }
    
    /**
     * Initializes the static resources for the services.
     * @return False if any exception occurred.
     */
    public static boolean initializeStaticResources() {
        LocalDateTime start = LocalDateTime.now();
        if (Settings.ENABLE_1G) {
            try {
                LocalDateTime loop = LocalDateTime.now();
                EnglishDistribution englishDistribution = EnglishCache.getEnglishDistribution(1);
                Settings.getLogger().log(Level.FINE,
                        "Cache EnglishDistribution (ngrams: 1) in " + DateHelper.humanReadableDifference(loop));
            } catch (Exception ex) {
                Settings.getLogger().log(Level.SEVERE, "Error while loading static resources", ex);
                return false;
            }
        }
        if (Settings.ENABLE_2G) {
            try {
                LocalDateTime loop = LocalDateTime.now();
                EnglishDistribution englishDistribution = EnglishCache.getEnglishDistribution(2);
                Settings.getLogger().log(Level.FINE,
                        "Cache EnglishDistribution (ngrams: 2) in " + DateHelper.humanReadableDifference(loop));
            } catch (Exception ex) {
                Settings.getLogger().log(Level.SEVERE, "Error while loading static resources", ex);
                return false;
            }
        }
        if (Settings.ENABLE_3G) {
            try {
                LocalDateTime loop = LocalDateTime.now();
                EnglishDistribution englishDistribution = EnglishCache.getEnglishDistribution(3);
                Settings.getLogger().log(Level.FINE,
                        "Cache EnglishDistribution (ngrams: 3) in " + DateHelper.humanReadableDifference(loop));
            } catch (Exception ex) {
                Settings.getLogger().log(Level.SEVERE, "Error while loading static resources", ex);
                return false;
            }
        }

        try {
            binaryClassifier = new ClassifiersHelper("binary");
            binaryClassifier.initResources();
        } catch (Exception ex) {
            Settings.getLogger().log(Level.SEVERE, "Error while loading binary classifier", ex);
            return false;
        }

        Settings.getLogger().log(Level.INFO, "Loading static resources in " + DateHelper.humanReadableDifference(start));
        return true;
    }

    /**
     * Classes to be published in the JAX-RS application. To subscribe use the setter method.
     */
    private static HashSet<Class<?>> pages = new HashSet<>();
    
    /**
     * Add a page to the JAX-RS application.
     * @param page page to be added.
     */
    public static void addPage(Class<?> page) {
        DGAApplication.pages.add(page);
    }
    
    /**
     * The method returns a non-empty collection with classes, that must be included in the published JAX-RS application.
     * @return a non-empty collection with classes, that must be included in the published JAX-RS application.
     */
    @Override
    public Set<Class<?>> getClasses() {
        return pages;
    }
}
