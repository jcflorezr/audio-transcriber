package net.jcflorezr.config

import net.jcflorezr.cloud.BucketClient
import net.jcflorezr.cloud.CloudSpeechClient
import net.jcflorezr.dao.AudioTranscriptDao
import net.jcflorezr.dao.AudioTranscriptDaoImpl
import net.jcflorezr.transcript.AudioTranscriber
import net.jcflorezr.transcript.AudioTranscriberImpl
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource(value = ["classpath:config/files-config.properties"])
@Import(value = [TestMongoConfig::class])
class TestRootConfig {

    @Bean @Profile("test") fun audioTranscriber(): AudioTranscriber = AudioTranscriberImpl()

    @Bean @Profile("test") fun audioTranscriptDao(): AudioTranscriptDao = AudioTranscriptDaoImpl()

    @Bean @Profile("test") fun bucketClient(): BucketClient = mock(BucketClient::class.java)

    @Bean @Profile("test") fun cloudSpeechClient(): CloudSpeechClient = mock(CloudSpeechClient::class.java)
}