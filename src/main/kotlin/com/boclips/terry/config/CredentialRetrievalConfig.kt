package com.boclips.terry.config

import com.boclips.terry.infrastructure.outgoing.rawcredentials.CloudStorageRetriever
import com.boclips.terry.infrastructure.outgoing.securecredentials.SafenoteRetriever
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import com.boclips.terry.infrastructure.outgoing.rawcredentials.Retriever as RawRetriever
import com.boclips.terry.infrastructure.outgoing.securecredentials.Retriever as SecureRetriever

@Profile("!test")
@Configuration
class CredentialRetrievalConfig {
    fun rawRetriever(bucketName: String): RawRetriever =
        CloudStorageRetriever(
            storage = StorageOptions.getDefaultInstance().service,
            bucketName = bucketName
        )

    @Bean
    fun secureRetriever(storageProperties: StorageProperties): SecureRetriever =
        SafenoteRetriever(rawRetriever(storageProperties.bucketName))
}