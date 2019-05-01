package net.jcflorezr.config

import net.jcflorezr.dao.AudioTranscriptDao
import net.jcflorezr.dao.AudioTranscriptDaoImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Import(value = [TestMongoConfig::class])
class TestAudioTranscriptDaoConfig {

    @Profile("test") @Bean fun sourceFileDao(): AudioTranscriptDao = AudioTranscriptDaoImpl()
}