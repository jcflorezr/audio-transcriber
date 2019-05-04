package net.jcflorezr.transcript

import mu.KotlinLogging
import net.jcflorezr.cloud.BucketClient
import net.jcflorezr.cloud.CloudSpeechClient
import net.jcflorezr.dao.AudioTranscriptDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioTranscript
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

interface AudioTranscriber {
    fun transcriptAudio(audioClipInfo: AudioClipInfo)
}

@Service
class AudioTranscriberImpl : AudioTranscriber {

    @Autowired
    private lateinit var audioTranscriptDao: AudioTranscriptDao
    @Autowired
    private lateinit var bucketClient: BucketClient
    @Autowired
    private lateinit var speechClient: CloudSpeechClient

    private val logger = KotlinLogging.logger { }

    override fun transcriptAudio(audioClipInfo: AudioClipInfo) {
        var audioToTranscript: File? = null
        try {
            logger.info {
                "[${audioClipInfo.transactionId}][${audioClipInfo.audioFileName}] " +
                "Transcription for Audio Clip '${audioClipInfo.audioClipName}' has started."
            }
            audioToTranscript = bucketClient.downloadSourceFileFromBucket(audioClipInfo)
            val audioTranscripts = speechClient.getAudioTranscripts(audioFile = audioToTranscript, audioClipInfo = audioClipInfo)
            audioTranscriptDao.saveAudioTranscript(
                audioTranscript = AudioTranscript(audioClipInfo, audioTranscripts),
                transactionId = audioClipInfo.transactionId
            )
        } finally {
            audioToTranscript?.delete()
        }
    }
}