package com.boclips.terry.config

import com.boclips.terry.infrastructure.outgoing.rawcredentials.IamCredentialRotator
import com.boclips.terry.infrastructure.outgoing.rawcredentials.RawCredentialRetriever
import com.boclips.terry.infrastructure.outgoing.securecredentials.SafenoteRetriever
import com.boclips.terry.infrastructure.outgoing.securecredentials.SecureCredentialRetriever
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class CredentialRetrievalConfig {
    fun rawRetriever(bucketName: String): RawCredentialRetriever =
        IamCredentialRotator()

    @Bean
    fun secureRetriever(storageProperties: StorageProperties): SecureCredentialRetriever =
        SafenoteRetriever(rawRetriever(storageProperties.bucketName))
}
