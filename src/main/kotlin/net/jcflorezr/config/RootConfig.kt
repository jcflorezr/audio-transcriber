package net.jcflorezr.config

import net.jcflorezr.broker.AudioTranscriberConsumerImpl
import net.jcflorezr.cloud.BucketClientImpl
import net.jcflorezr.cloud.CloudSpeechClientImpl
import net.jcflorezr.dao.AudioTranscriptDaoImpl
import net.jcflorezr.transcript.AudioTranscriberImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource(value = ["classpath:net.jcflorezr.config/files-config.properties"])
@Import(value = [MongoConfig::class])
class RootConfig {

    @Bean fun audioTranscriberConsumer() = AudioTranscriberConsumerImpl()

    @Bean fun audioTranscriber() = AudioTranscriberImpl()

    @Bean fun audioTranscriptDao() = AudioTranscriptDaoImpl()

    @Bean fun bucketClient() = BucketClientImpl()

    @Bean fun cloudSpeechClient() = CloudSpeechClientImpl()

}