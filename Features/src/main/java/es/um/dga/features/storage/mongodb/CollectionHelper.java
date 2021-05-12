/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.storage.mongodb;

import com.mongodb.InsertOptions;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import es.um.dga.features.utils.DateHelper;
import es.um.dga.features.utils.Settings;

/**
 * MongoDB Collection Helper class.
 */
public class CollectionHelper {
    
    /**
     * CollectionHelper insert list Lock.
     *
     * @see CollectionHelper#addToInsertQueue(org.bson.Document)
     */
    private final Object INSERT_LOCK = new Object();
    /**
     * CollectionHelper updates list Lock.
     *
     * @see CollectionHelper#addToUpdateQueue(org.bson.conversions.Bson, org.bson.Document)
     */
    private final Object UPDATE_LOCK = new Object();
    private final Object indexCollectionLock = new Object();
    private LocalDateTime waitStart;
    private LocalDateTime writeStart;
    private LocalDateTime writeEnd;
    private boolean setIndex = true;
    /**
     * Collection Helper.
     */
    private MongoCollection<Document> mongoCollection;
    /**
     * List of update models for bulk write.
     */
    private List<UpdateOneModel<Document>> updateModelList;
    /**
     * List of insert models for bulk write.
     */
    private List<InsertOneModel<Document>> insertModelList;
    /**
     * Sets the upsert flag.
     *
     * @see com.mongodb.client.model.UpdateOptions#upsert(boolean)
     */
    private boolean upsert = true;
    /**
     * Sets the ordered flag.
     *
     * @see com.mongodb.client.model.BulkWriteOptions#ordered(boolean)
     */
    private boolean ordered = false;
    /**
     * Sets the bypass document validation flag.
     *
     * @see com.mongodb.client.model.UpdateOptions#bypassDocumentValidation(Boolean)
     * @see com.mongodb.client.model.BulkWriteOptions#bypassDocumentValidation(Boolean)
     */
    private boolean bypassDocumentValidation = true;
    /**
     * Chunk Size for batch update.
     */
    private int chunkSize = 5000;
    /**
     * Collection Size.
     */
    private long collectionSize;
    /**
     * Counter to keep track of the amount of data send.
     */
    private int sendCount = 0;
    
    /**
     * Default constructor.
     *
     * @param mongoCollection          MongoDB Collection Object.
     * @param upsert                   See {@link com.mongodb.client.model.UpdateOptions#upsert(boolean)}
     * @param ordered                  See {@link com.mongodb.client.model.BulkWriteOptions#ordered(boolean)}
     * @param bypassDocumentValidation See {@link com.mongodb.client.model.UpdateOptions#bypassDocumentValidation(Boolean)} and {@link com.mongodb.client.model.BulkWriteOptions#bypassDocumentValidation(Boolean)}
     * @param uniqueIndex              If not null, creates the unique index based on that field.
     */
    public CollectionHelper(MongoCollection<Document> mongoCollection, boolean upsert, boolean ordered,
            boolean bypassDocumentValidation, String uniqueIndex) {
        this.mongoCollection = mongoCollection;
        this.upsert = upsert;
        this.ordered = ordered;
        this.bypassDocumentValidation = bypassDocumentValidation;
        
        this.updateModelList = new ArrayList<>();
        this.insertModelList = new ArrayList<>();
        this.collectionSize = mongoCollection.count();
        
        if (uniqueIndex != null) {
            this.createIndex(uniqueIndex);
        }
    }
    
    /**
     * Enforce unique index for this collection.
     *
     * @param uniqueIndex Unique index name.
     */
    private synchronized void createIndex(String uniqueIndex) {
        try {
            boolean foundIndex = false;
            ListIndexesIterable<Document> listIndexes = this.mongoCollection.listIndexes();
            for (Document document : listIndexes) {
                if (StringUtils.containsIgnoreCase(document.get("name").toString(), uniqueIndex)) {
                    Settings.getLogger().log(Level.FINER,
                            this.getCollectionName() + ": Found unique index '" + uniqueIndex + "', skip creation.");
                    foundIndex = true;
                    break;
                }
                // ELSE CONTINUE;
            }
            if (!foundIndex) {
                Document index = new Document(uniqueIndex, 1);
                IndexOptions indexOptions = new IndexOptions().name(uniqueIndex).unique(true);
                Settings.getLogger()
                        .log(Level.FINE, this.getCollectionName() + ": Creating unique index '" + uniqueIndex + "'.");
                this.mongoCollection.createIndex(index, indexOptions);
            }
        }
        catch (com.mongodb.MongoCommandException ex) {
            if (!ex.getMessage().contains("index not found")) {
                Settings.getLogger().log(Level.WARNING,
                        "MongoCommandException while creating the index for the collection '" + this
                                .getCollectionName(), ex);
            }
        }
        catch (Exception ex) {
            Settings.getLogger().log(Level.WARNING,
                    "Exception while creating the index for the collection '" + this.getCollectionName(), ex);
        }
    }
    
    /**
     * Gets the collection name.
     *
     * @return Fully qualified collection name, e.g. DATABASE.COLLECTION.
     */
    public String getCollectionName() {
        return this.mongoCollection.getNamespace().toString();
    }
    
    /**
     * Add the provided document to the update queue.
     *
     * @param filter    Filter document for this specific update.
     * @param updateDoc Updated document. May include "$set" reference.
     */
    public void addToUpdateQueue(Bson filter, Document updateDoc) {
        
        UpdateOptions updateOptions = new UpdateOptions().upsert(this.upsert)
                .bypassDocumentValidation(this.bypassDocumentValidation);
        
        synchronized (this.UPDATE_LOCK) {
            //Prepare list of Updates
            if (this.updateModelList.size() < this.chunkSize) {
                this.updateModelList.add(new UpdateOneModel<Document>(filter, updateDoc, updateOptions));
            }
            else {
                this.sendRequest(false);
            }
        }
    }
    
    /**
     * Sends the request to the database.
     */
    private void sendRequest(boolean isLast) {
        String logMessage = StringUtils.rightPad(this.getMessageHeader(), 40);
        boolean printMessage = false;
        this.waitStart = LocalDateTime.now();
        synchronized (this.UPDATE_LOCK) {
            if (!this.updateModelList.isEmpty()) {
                printMessage = true;
                this.writeStart = LocalDateTime.now();
                
                //Bulk write options
                BulkWriteOptions bulkWriteOptions = new BulkWriteOptions();
                bulkWriteOptions.ordered(this.ordered);
                bulkWriteOptions.bypassDocumentValidation(this.bypassDocumentValidation);
                
                BulkWriteResult bulkWriteResult = null;
                try {
                    //Perform bulk update
                    bulkWriteResult = this.mongoCollection.bulkWrite(this.updateModelList, bulkWriteOptions);
                    logWriteResult(bulkWriteResult);
                }
                catch (MongoBulkWriteException e) {
                    e.printStackTrace();
                    //Handle bulkwrite exception
                    List<BulkWriteError> bulkWriteErrors = e.getWriteErrors();
                    bulkWriteErrors.forEach(bulkWriteError -> {
                        Settings.getLogger().log(Level.WARNING, bulkWriteError.toString());
                    });
                }
                
                // Empty the list.
                this.updateModelList = new ArrayList<>();
                this.writeEnd = LocalDateTime.now();
            }
        }
        
        synchronized (this.INSERT_LOCK) {
            if (!this.insertModelList.isEmpty()) {
                printMessage = true;
                this.writeStart = LocalDateTime.now();
                
                //Bulk write options
                BulkWriteOptions bulkWriteOptions = new BulkWriteOptions();
                bulkWriteOptions.ordered(this.ordered);
                bulkWriteOptions.bypassDocumentValidation(this.bypassDocumentValidation);
                
                BulkWriteResult bulkWriteResult = null;
                try {
                    //Perform bulk update
                    bulkWriteResult = this.mongoCollection.bulkWrite(this.insertModelList, bulkWriteOptions);
                    logWriteResult(bulkWriteResult);
                }
                catch (MongoBulkWriteException e) {
                    //Handle bulkwrite exception
                    List<BulkWriteError> bulkWriteErrors = e.getWriteErrors();
                    bulkWriteErrors.forEach(bulkWriteError -> {
                        if (bulkWriteError.getCode() == 11000) {
                            // Magic number for duplicate key
                            Settings.getLogger().log(Level.WARNING,
                                    "Duplicate domain name. Details: " + bulkWriteError.getMessage());
                        }
                        else {
                            Settings.getLogger()
                                    .log(Level.WARNING, "Unhandled write error. Details: " + bulkWriteError.toString());
                        }
                    });
                }
                
                // Empty the list.
                this.insertModelList = new ArrayList<>();
                this.writeEnd = LocalDateTime.now();
            }
        }
        if (isLast && printMessage) {
            logMessage += "Send " + (isLast ? "FINAL" : "PARTIAL") + " chunk ";
            logMessage += (++this.sendCount * this.chunkSize);
            logMessage += "/";
            logMessage += this.collectionSize;
            logMessage += " (LockWait ";
            logMessage += DateHelper.humanReadableDifference(this.waitStart, this.writeStart);
            logMessage += " | DB write ";
            logMessage += DateHelper.humanReadableDifference(this.writeStart, this.writeEnd);
            logMessage += ")";
        } else {
            logMessage += "Closing remaining connections.";
        }
        Settings.getLogger().log(Level.INFO, logMessage);
    }
    
    /**
     * Get a human readable format for the collection name.
     *
     * @return Human-readable-log-friendly collection name.
     */
    public String getMessageHeader() {
        return "'" + this.mongoCollection.getNamespace() + "'";
    }
    
    private static synchronized void logWriteResult(BulkWriteResult bulkWriteResult) {
        StringBuilder builder = new StringBuilder();
        builder.append("WriteResult: ");
        builder.append("\tI:");
        builder.append(bulkWriteResult.getInsertedCount());
        builder.append("\tD:");
        builder.append(bulkWriteResult.getDeletedCount());
        builder.append("\tU:");
        builder.append(bulkWriteResult.getUpserts().size());
        builder.append("\tM:");
        builder.append(bulkWriteResult.getModifiedCount());
        Settings.getLogger().log(Level.FINER, builder.toString());
    }
    
    /**
     * Add the provided document to the insert queue.
     *
     * @param insertDoc Insert document.
     */
    public void addToInsertQueue(Document insertDoc) {
        InsertOptions insertOptions = new InsertOptions().bypassDocumentValidation(this.bypassDocumentValidation)
                .continueOnError(true);
        
        synchronized (this.INSERT_LOCK) {
            //Prepare list of Updates
            if (this.insertModelList.size() < this.chunkSize) {
                this.insertModelList.add(new InsertOneModel<>(insertDoc));
            }
            else {
                this.sendRequest(false);
            }
        }
    }
    
    /**
     * Sends the last batch and performs final operations.
     */
    public void sendAndClose() {
        System.out.println();
        this.sendRequest(true);
        //TODO: close the connection? IDK if it's possible for collections.
        // Perhaps destroy this class to free resources.
        this.mongoCollection = null;
        this.updateModelList = null;
        this.insertModelList = null;
    }
    
    public MongoCollection<Document> getCollection() {
        return this.mongoCollection;
    }
}
