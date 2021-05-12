package es.um.dga.features.utils; /**
 * Project: DomainNameProfiler Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * es.um.dga.Settings actual file. Always excluded from repository.
 */
public class Settings {
    /**
     * Drop collections.
     */
    public static final boolean DROP_COLLECTIONS = false;

    /**
     * Exit code - Success
     */
    public static final int EXIT_CODE_OK = 0;

    /**
     * Exit code - Parsing Error.
     */
    public static final int EXIT_CODE_PARSING_ERROR = -1;

    /**
     * Exit code - Connection Error.
     */
    public static final int EXIT_CODE_CONNECTION_ERROR = -2;

    /**
     * Exit code - Generic IO Exception Error.
     */
    public static final int EXIT_CODE_IOEXCEPTION = -3;

    /**
     * Default value for features.
     */
    public static final Object DEFAULT_VALUE = null;

    /**
     * Default value for features.
     */
    public static final String DEFAULT_STRING_VALUE = null;

    /**
     * Default value for features.
     */
    public static final Double DEFAULT_DOUBLE_VALUE = Double.NaN;

    /**
     * Default value for features.
     */
    public static final List DEFAULT_LIST_VALUE = Collections.singletonList(null);

    /**
     * Logger Context.
     */
    private static final String LOGGER_CONTEXT = "es.um.dga";

    /**
     * The analysis will only be reported in output and not stored.
     */
    public static boolean REPORT_ONLY = true;

    /**
     * Enables the basic features calculation.
     */
    public static boolean ENABLE_BASE = false;

    /**
     * Enables the 1gram features calculation.
     */
    public static boolean ENABLE_1G = false;

    /**
     * Enables the 2gram features calculation.
     */
    public static boolean ENABLE_2G = false;

    /**
     * Enables the 3gram features calculation.
     */
    public static boolean ENABLE_3G = false;

    /**
     * Project Root Folder
     */
    protected static String DATA_ABSOLUTE_PATH;

    /**
     * Logger object.
     */
    private transient static Logger LOGGER;

    public static String getFeatureCapabilities() {
        return (ENABLE_BASE ? ".B" : "") + (ENABLE_1G ? ".1G" : "") + (ENABLE_2G ? ".2G" : "") + (ENABLE_3G ? ".3G" : "");
    }

    /**
     * Default log formatter.
     */
    private static Formatter LOG_FORMATTER = new Formatter() {
        @Override
        public String format(LogRecord arg0) {
            StringBuilder b = new StringBuilder();
            b.append(new Date());
            b.append(" T:[");
            b.append(Thread.currentThread().getName().substring(Thread.currentThread().getName().lastIndexOf("-") + 1));
            b.append("[");
            b.append(arg0.getLevel());
            b.append("] ");
            //b.append(arg0.getSourceClassName().substring(arg0.getSourceClassName().lastIndexOf('.') + 1));
            //b.append("#");
            //b.append(arg0.getSourceMethodName());
            b.append("\t ");
            b.append(arg0.getMessage());
            b.append(System.getProperty("line.separator"));
            return b.toString();
        }
    };

    /**
     * Console handler.
     */
    private static final Handler LOG_CONSOLE_HANDLER = new Handler() {
        @Override
        public void publish(LogRecord record) {

            if (this.getFormatter() == null) {
                this.setFormatter(Settings.LOG_FORMATTER);
            }

            try {
                String message = this.getFormatter().format(record);
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    System.err.write(message.getBytes());

                    if (record.getThrown() != null) {
                        // Have an associated exception.
                        record.getThrown().printStackTrace();
                    }
                } else {
                    System.out.write(message.getBytes());
                }
            } catch (Exception exception) {
                this.reportError(null, exception, ErrorManager.FORMAT_FAILURE);
            }

        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    };

    /**
     * Global log level.
     */
    private static Level LOG_LEVEL = Level.INFO;
    //private static Config configuration;

    /**
     * Random generator.
     */
    public static Random RANDOM_GENERATOR = new SecureRandom();

    /**
     * Set root folder
     *
     * @param dataAbsolutePath Absolute root folder.
     */
    public static void setDataAbsolutePath(String dataAbsolutePath) {
        DATA_ABSOLUTE_PATH = dataAbsolutePath;
    }

    /**
     * Gets the 'AbsoluteProjectPath' property value. Ensures that the path ends with a trailer slash.
     *
     * @return value of AbsoluteProjectPath
     */
    public static String getAbsoluteProjectPath() {
        if (DATA_ABSOLUTE_PATH.endsWith("\\") || DATA_ABSOLUTE_PATH.endsWith("/")) {
            return DATA_ABSOLUTE_PATH;
        } else {
            return DATA_ABSOLUTE_PATH + "/";
        }
    }

    //private static void loadConfiguration(String root) {
        /*
        synchronized (Locks.CONFIGURATION) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                configuration = (Config) jaxbUnmarshaller.unmarshal(MongoDBHelper.class.getResource("/config.xml"));
            }
            catch (JAXBException ex) {
                Settings.getLogger().log(Level.SEVERE, "Error while loading configuration file.", ex);
                System.exit(Settings.EXIT_CODE_CONNECTION_ERROR);
            }
        }*/

    //}

    /**
     * Returns the application logger.
     *
     * @return Application Logger
     */
    public static Logger getLogger() {
        synchronized (Locks.LOGGER) {
            if (LOGGER != null) {
                return LOGGER;
            } else {
                LOGGER = Logger.getLogger(Settings.LOGGER_CONTEXT);
                LOGGER.setLevel(Settings.LOG_LEVEL);
                LOGGER.setUseParentHandlers(false);
                LOGGER.addHandler(LOG_CONSOLE_HANDLER);
                return LOGGER;
            }
        }
    }

    /**
     * Gets the 'LogFolder' property value.
     *
     * @return value of LogFolder##
     */
    public static String getLogFolder() {
        return getAbsoluteProjectPath() + "Log/";
    }

    /**
     * Gets a value indicating whether the property 'DROP_COLLECTIONS' is true.
     *
     * @return a value indicating whether the property 'DROP_COLLECTIONS' is true.
     */
    public static boolean isDropCollections() {
        return DROP_COLLECTIONS;
    }

    /**
     * Gets a value indicating whether the property 'REPORT_ONLY' is true.
     *
     * @return a value indicating whether the property 'REPORT_ONLY' is true.
     */
    public static boolean isReportOnly() {
        return REPORT_ONLY;
    }

    /**
     * Gets the lOG_LEVEL variable.
     *
     * @return the lOG_LEVEL
     */
    public static Level getLogLevel() {
        return LOG_LEVEL;
    }

    /**
     * Sets the log level value.
     *
     * @param lvl the log level to set
     */
    public static void setLogLevel(Level lvl) {
        Settings.LOG_LEVEL = lvl;

        for (Handler h : getLogger().getHandlers()) {
            h.setLevel(Settings.LOG_LEVEL);
        }
        
        /*ch.qos.logback.classic.Level mongoLVL;
        if (Settings.LOG_LEVEL.equals(Level.FINEST)) {
            mongoLVL = ch.qos.logback.classic.Level.DEBUG;
        }
        else if (Settings.LOG_LEVEL.equals(Level.FINER)) {
            mongoLVL = ch.qos.logback.classic.Level.INFO;
        }
        else if (Settings.LOG_LEVEL.equals(Level.FINE)) {
            mongoLVL = ch.qos.logback.classic.Level.WARN;
        }
        else if (Settings.LOG_LEVEL.equals(Level.INFO) || Settings.LOG_LEVEL.equals(Level.WARNING)) {
            mongoLVL = ch.qos.logback.classic.Level.ERROR;
        }
        else {
            mongoLVL = ch.qos.logback.classic.Level.OFF;
        }
        
        try {
            ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory
                    .getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
            rootLogger.setLevel(mongoLVL);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    /**
     * Gets the requested language folder.
     *
     * @param absolute Gets the absolute path instead of the relative ones.
     * @param language Installed language.
     *
     * @return Absolute path for the language data directory.
     */
    public static String getLanguageDataDirectory(String language) {
        return Settings.getDataDirectory() + "language/" + language + "/";
    }
    
    private static String getDataDirectory() {
        return "/home/mattia/DGA-RESTful/data/";
    }
    
    /**
     * Gets the requested domains folder.
     *
     * @param absolute       Gets the absolute path instead of the relative ones.
     * @param malwareVariant Malware variant code.
     *
     * @return Absolute path for the domains data directory.
     */
    public static String getFQDNDataDirectory(String malwareVariant) {
        return "/home/mattia/UMU-DGA-Data/data/" + malwareVariant + "/";
    }

    /**
     * Gets the requested classifier folder.
     *
     * @param absolute Gets the absolute path instead of the relative ones.
     * @param mode     Classifier type.
     *
     * @return Absolute path for the classifier data directory.
     */
    public static String getClassifierDataDirectory(
            String mode) {
        return Settings.getDataDirectory() + mode + "/data/";
    }

    /**
     * Gets the requested classifier folder.
     *
     * @param absolute Gets the absolute path instead of the relative ones.
     * @param mode     Classifier type.
     *
     * @return Absolute path for the classifier models directory.
     */
    public static String getClassifierModelDirectory(String mode) {
        return Settings.getDataDirectory() + mode + "/models/";
    }
}
