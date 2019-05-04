package net.jcflorezr.broker

import mu.KotlinLogging
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.transcript.AudioTranscriber
import net.jcflorezr.util.JsonUtils
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Properties
import javax.annotation.PostConstruct

interface AudioTranscriberConsumer {
    fun receiveAudioClips()
}

@Service
@PropertySource(value = ["classpath:config/kafka.properties"])
class AudioTranscriberConsumerImpl : AudioTranscriberConsumer {

    @Autowired
    private lateinit var audioTranscriber: AudioTranscriber

    private val logger = KotlinLogging.logger { }
    private val thisClass: Class<AudioTranscriberConsumerImpl> = this.javaClass
    private lateinit var tempDirectoryPath: String

    @Value("\${kafka-brokers}")
    private lateinit var kafkaBrokers: String
    @Value("\${group-id}")
    private lateinit var groupId: String
    @Value("\${topic-name}")
    private lateinit var topicName: String
    @Value("\${max-poll-records}")
    private lateinit var maxPollRecords: String
    @Value("\${max-no-message-found-count}")
    private lateinit var maxNoMessageFoundCount: String
    @Value("\${offset-reset}")
    private lateinit var offsetReset: String

    @PostConstruct
    fun init() {
        tempDirectoryPath = thisClass.getResource("/temp-converted-files").path
    }

    override fun receiveAudioClips() {
        // TODO: correct the log message
        logger.info { "[1][audio-clip] Sending generated audio clip to message broker." }
        val consumer = createConsumer()
        var noMessageFound = 0
        while (true) {
            val consumerRecords = consumer.poll(Duration.ofMillis(1000))
            // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
            if (consumerRecords.count() == 0) {
                noMessageFound++
                // If no message found count is reached to threshold exit loop.
                if (noMessageFound > maxNoMessageFoundCount.toInt()) { break } else { continue }
            }
            consumerRecords.forEach { record ->
                val audioClipInfo = record.value()
                logger.info {
                    "[${audioClipInfo.transactionId}][${audioClipInfo.audioFileName}]" +
                    "Audio Clip '${audioClipInfo.audioClipName}' has arrived."
                }
                audioTranscriber.transcriptAudio(audioClipInfo)
            }
            consumer.commitAsync()
        }
        consumer.close()
    }

    private fun createConsumer(): Consumer<String, AudioClipInfo> {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBrokers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId.toInt()
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = AudioClipInfoDeserializer::class.java.name
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = maxPollRecords.toInt()
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = offsetReset
        val consumer = KafkaConsumer<String, AudioClipInfo>(props)
        consumer.subscribe(listOf(topicName))
        return consumer
    }
}

class AudioClipInfoDeserializer : Deserializer<AudioClipInfo> {

    private val logger = KotlinLogging.logger { }

    override fun configure(configs: Map<String, *>, isKey: Boolean) {}

    override fun deserialize(topic: String, data: ByteArray): AudioClipInfo {
        var audioClipInfo: AudioClipInfo? = null
        val objectMapper = JsonUtils.MAPPER
        try {
            audioClipInfo = objectMapper.readValue(data, AudioClipInfo::class.java)
        } catch (exception: Exception) {
            logger.error { "Error in deserializing bytes $exception" }
        }

        return audioClipInfo!!
    }

    override fun close() {}
}