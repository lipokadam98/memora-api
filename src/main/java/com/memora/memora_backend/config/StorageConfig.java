package com.memora.memora_backend.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    /**
     * Create a storage object to interact with Google Cloud Storage
     * @return the storage object
     */
    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }

}
