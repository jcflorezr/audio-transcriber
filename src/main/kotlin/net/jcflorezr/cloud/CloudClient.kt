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
    }

    override fun getAudioTranscripts(audioFile: File): List<Transcript> {
        SpeechClient.create().use { speechClient ->
            val audioBytes = ByteString.copyFrom(audioFile.readBytes())
            val config = RecognitionConfig.newBuilder().setLanguageCode(COLOMBIAN_SPANISH).build()
            val audio = RecognitionAudio.newBuilder().setContent(audioBytes).build()
            return speechClient.recognize(config, audio).resultsList
                .flatMap { it.alternativesList }
                .map { Transcript(transcript = it.transcript, confidence = it.confidence) }
        }
    }
}

interface BucketClient {
    fun downloadSourceFileFromBucket(audioFileName: String): File
}

@Service
final class BucketClientImpl : BucketClient {

    @Value("\${files-net.jcflorezr.config.bucket-name}")
    private lateinit var bucketName: String
    @Value("\${files-net.jcflorezr.config.bucket-directory}")
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

    override fun downloadSourceFileFromBucket(audioFileName: String): File {
        // TODO: give the right log message
        logger.info { "[1][entry-point] Downloading source audio file: ($audioFileName) from bucket" }
        val blobId = BlobId.of(bucketName, "$bucketDirectory/$audioFileName")
        val blob = bucketInstance.get(blobId)
            ?: throw SourceAudioFileValidationException.audioFileDoesNotExistInBucket(audioFileName)
        val downloadedFilePath = "$tempDirectory/$audioFileName"
        blob.downloadTo(Paths.get(downloadedFilePath))
        return File(downloadedFilePath)
    }
}