package net.jcflorezr.cloud

import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.protobuf.ByteString
import net.jcflorezr.exception.SourceAudioFileValidationException
import net.jcflorezr.model.Transcript
import mu.KotlinLogging
import net.jcflorezr.model.AudioClipInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import javax.annotation.PostConstruct


interface CloudSpeechClient {
    fun getAudioTranscripts(audioFile: File): List<Transcript>
}

@Service
class CloudSpeechClientImpl : CloudSpeechClient {

    companion object {
        private const val COLOMBIAN_SPANISH = "es-CO"
        private const val US_ENGLISH = "en-US"
        private const val UK_ENGLISH = "en-UK"
    }

    override fun getAudioTranscripts(audioFile: File): List<Transcript> {
        SpeechClient.create().use { speechClient ->
            val audioBytes = ByteString.copyFrom(audioFile.readBytes())
            val config = RecognitionConfig.newBuilder().setLanguageCode(UK_ENGLISH).build()
            val audio = RecognitionAudio.newBuilder().setContent(audioBytes).build()
            return speechClient.recognize(config, audio).resultsList
                .flatMap { it.alternativesList }
                .map { Transcript(transcript = it.transcript, confidence = it.confidence) }
        }
    }
}

interface BucketClient {
    fun downloadSourceFileFromBucket(audioClipInfo: AudioClipInfo): File
}

@Service
final class BucketClientImpl : BucketClient {

    @Value("\${files-config.bucket-name}")
    private lateinit var bucketName: String
    @Value("\${files-config.bucket-directory}")
    private lateinit var bucketDirectory: String

    private val thisClass: Class<BucketClientImpl> = this.javaClass
    private lateinit var tempDirectory: String
    private lateinit var bucketInstance: Storage

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() {
        bucketInstance = StorageOptions.getDefaultInstance().service
        tempDirectory = thisClass.getResource("/temp-converted-files").path
    }

    override fun downloadSourceFileFromBucket(audioClipInfo: AudioClipInfo): File {
        // TODO: give the right log message
        val audioClipLocation = audioClipInfo.run { "$bucketDirectory/$audioFileName/$transactionId/$audioClipName" }
        logger.info { "[1][entry-point] Downloading source audio file: ($audioClipLocation) from bucket" }
        val blobId = BlobId.of(bucketName, audioClipLocation)
        val blob = bucketInstance.get(blobId)
            ?: throw SourceAudioFileValidationException.audioFileDoesNotExistInBucket(audioClipLocation)
        val downloadedFilePath = audioClipInfo.run { "$tempDirectory/$audioFileName/$transactionId/$audioClipName" }
        blob.downloadTo(Paths.get(downloadedFilePath))
        return File(downloadedFilePath)
    }
}