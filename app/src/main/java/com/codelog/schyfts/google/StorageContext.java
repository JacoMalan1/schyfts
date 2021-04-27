package com.codelog.schyfts.google;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.Reference;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;

public class StorageContext {

    private static StorageContext instance;
    private Storage storage;

    private StorageContext() {
        var stream = getClass().getClassLoader().getResourceAsStream("google/service-account.json");
        try {
            if (stream == null)
                throw new IOException("Couldn't open resource google/service-account.json");
            var creds = GoogleCredentials.fromStream(stream);
            var storage = StorageOptions.newBuilder().setCredentials(creds)
                    .setProjectId(Reference.GOOGLE_CLOUD_PROJECT_ID).build().getService();
            this.storage = storage;
        } catch (IOException e) {
            Logger.getInstance().error("Couldn't load google credentials!");
            Logger.getInstance().exception(e);
        }

    }

    public Storage getStorage() {
        return storage;
    }

    public static StorageContext getInstance() {
        instance = (instance == null) ? new StorageContext() : instance;
        return instance;
    }

}
