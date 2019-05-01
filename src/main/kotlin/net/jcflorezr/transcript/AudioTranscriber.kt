package net.jcflorezr.transcript

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

    override fun transcriptAudio(audioClipInfo: AudioClipInfo) {
        var audioToTranscript: File? = null
        try {
            audioToTranscript = bucketClient.downloadSourceFileFromBucket(audioClipInfo)
            val audioTranscripts = speechClient.getAudioTranscripts(audioFile = audioToTranscript)
            audioTranscriptDao.saveAudioTranscript(AudioTranscript(audioClipInfo, audioTranscripts))
        } finally {
            audioToTranscript?.delete()
        }
    }

}