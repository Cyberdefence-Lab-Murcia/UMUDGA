/**
 * Project: DomainNameProfiler
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage.mongodb;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.bson.Document;
import org.bson.types.ObjectId;

import es.um.dga.features.utils.DateHelper;
import es.um.dga.features.utils.Locks;
import es.um.dga.features.utils.Settings;
import es.um.dga.features.utils.config.Collection;
import es.um.dga.features.utils.config.Database;
import es.um.dga.features.utils.config.Host;
import es.um.dga.features.utils.config.Mongodb;

/**
 * MongoDB Helper.
 */
public class MongoDBHelper {
    
    /**
     * Database Map.
     */
    private static ConcurrentMap<String, MongoDatabase> databaseMap = new ConcurrentHashMap<>();
    
    /**
     * Collection Map.
     */
    private static ConcurrentMap<String, CollectionHelper> collectionMap = new ConcurrentHashMap<>();
    
    public static boolean testConnection() {
        
        synchronized (Locks.LOAD_MONGO_CONFIGURATION) {
            if (databaseMap.isEmpty()) {
                loadConfiguration();
            }
        }
        
        for (MongoDatabase mongoDatabase : databaseMap.values()) {
            try {
                mongoDatabase.runCommand(new Document("serverStatus", 1));
            }
            catch (Exception ex) {
                return false;
            }
        }
        return true;
    }
    
    private synchronized static void loadConfiguration() {
        LocalDateTime start = LocalDateTime.now();
        Mongodb dbConfig = Settings.getMongoDBConfiguration();
        for (Host host : dbConfig.getHost()) {
            
            ServerAddress hostAddress = new ServerAddress(host.getUrl(), host.getPort());
            MongoClientOptions options = MongoClientOptions.builder().sslEnabled(host.isSsl()).build();
            
            for (Database database : host.getDatabase()) {
                MongoClient client;
                if (database.getUser() != null) {
                    MongoCredential credential = MongoCredential
                            .createCredential(database.getUser().getId(), database.getId(),
                                    database.getUser().getPassword().toCharArray());
                    client = new MongoClient(hostAddress, credential, options);
                }
                else {
                    client = new MongoClient(hostAddress, options);
                }
                
                MongoDatabase mongoDatabase = client.getDatabase(database.getId());
                databaseMap.put(mongoDatabase.getName(), mongoDatabase);
                
                String uniqueIndex = null;
                if (database.getId().equalsIgnoreCase("FQDN")) {
                    uniqueIndex = "domain";
                }
                for (Collection collection : database.getCollections().getCollection()) {
                    MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection.getName());
                    CollectionHelper collectionHelper = new CollectionHelper(mongoCollection, collection.isUpsert(),
                            collection.isOrdered(), collection.isBypassDocumentValidation(), uniqueIndex);
                    collectionMap.put(collection.getName(), collectionHelper);
                }
            }
        }
        Settings.getLogger().log(Level.INFO,
                "Load configuration in " + DateHelper.humanReadableDifference(start, LocalDateTime.now()));
    }
    
    public static Set<CollectionHelper> getCollections(String database) throws NoSuchElementException {
        MongoDatabase mongoDatabase = getDatabase(database);
        Set<CollectionHelper> result = new HashSet<>();
        mongoDatabase.listCollectionNames().forEach((Block<? super String>) s -> {
            try {
                result.add(getCollectionHelper(s));
            }
            catch (Exception ex) {
                // Continue;
                
            }
        });
        return result;
    }
    
    /**
     * Gets the MongoDB requested.
     *
     * @param name MongoDB Name.
     *
     * @return MongoDB Object.
     *
     * @throws java.util.NoSuchElementException This database has not been defined in the configuration.
     */
    public static MongoDatabase getDatabase(String name) throws NoSuchElementException {
        try {
            
            synchronized (Locks.LOAD_MONGO_CONFIGURATION) {
                if (databaseMap.isEmpty()) {
                    loadConfiguration();
                }
            }
            if (databaseMap.containsKey(name)) {
                return databaseMap.get(name);
            }
            throw new NoSuchElementException("The requested database has not been defined in the configuration file.");
            
        }
        catch (NullPointerException ex) {
            throw new NoSuchElementException("The requested database has not been defined in the configuration file.");
        }
    }
    
    /**
     * Gets the connection helper for any configured MongoCollection.
     *
     * @param name Collection Name
     *
     * @return Collection Helper.
     *
     * @throws java.util.NoSuchElementException Whenever the collection isn't defined in the configuration file.
     */
    public static CollectionHelper getCollectionHelper(String name) {
        
        synchronized (Locks.LOAD_MONGO_CONFIGURATION) {
            if (collectionMap.isEmpty()) {
                loadConfiguration();
            }
        }
        
        if (collectionMap.containsKey(name)) {
            return collectionMap.get(name);
        }
        else {
            Settings.getLogger().log(Level.SEVERE,
                    "The requested collection '" + name + "' has not been defined in the " + "configuration file"
                            + ".");
            System.exit(-1);
            throw new NoSuchElementException(
                    "The requested collection has not been defined in the configuration file.");
        }
        
    }
    
    /**
     * Send the last batch of commits and close all the connection helpers file.
     *
     * @see CollectionHelper#sendAndClose()
     */
    public static void closeAllCollectionHelpers() {
        collectionMap.values().forEach(CollectionHelper::sendAndClose);
        collectionMap = new ConcurrentHashMap<>();
        databaseMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets the connection for any configured MongoCollection.
     *
     * @param name Collection Name
     *
     * @return Collection.
     *
     * @throws java.util.NoSuchElementException Whenever the collection isn't defined in the configuration file.
     */
    public static MongoCollection<Document> getCollection(String name) throws NoSuchElementException {
        try {
            
            synchronized (Locks.LOAD_MONGO_CONFIGURATION) {
                if (collectionMap.isEmpty()) {
                    loadConfiguration();
                }
            }
            if (collectionMap.containsKey(name)) {
                return collectionMap.get(name).getCollection();
            }
            throw new NoSuchElementException(
                    "The requested collection has not been defined in the configuration file.");
            
        }
        catch (NullPointerException ex) {
            throw new NoSuchElementException(
                    "The requested collection has not been defined in the configuration file.");
        }
    }
    
    /**
     * Gets the 'databaseMap' property value.
     *
     * @return value of databaseMap##
     */
    public static ConcurrentMap<String, MongoDatabase> getDatabaseMap() {
        return databaseMap;
    }
    
    /**
     * Gets the 'collectionMap' property value.
     *
     * @return value of collectionMap##
     */
    public static ConcurrentMap<String, CollectionHelper> getCollectionMap() {
        return collectionMap;
    }
    
    /**
     * Deletes all the static objects and calls the Garbage Collector.
     */
    public static void reset() {
        collectionMap = new ConcurrentHashMap<>();
        databaseMap = new ConcurrentHashMap<>();
        System.gc();
    }
    
    /**
     * Gets a new fully random ObjectID.
     * Generated with {@link Settings#RANDOM_GENERATOR}
     * @return 12 bits random objectid.
     */
    public static ObjectId newObjectID() {
        try {
            byte[] resBuf = new byte[12];
            Settings.RANDOM_GENERATOR.nextBytes(resBuf);
            return new ObjectId(resBuf);
        }
        catch (Exception ex) {
            Settings.getLogger().log(Level.SEVERE, "An exception occurred while generating the ObjectID.", ex);
            return new ObjectId();
        }
    }
}
