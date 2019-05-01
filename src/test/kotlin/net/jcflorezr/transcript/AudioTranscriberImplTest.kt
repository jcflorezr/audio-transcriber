package net.jcflorezr.transcript

import net.jcflorezr.cloud.BucketClient
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.dao.TestMongoInitializer
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.util.JsonUtils
import org.apache.commons.io.FileUtils
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class AudioTranscriberImplTest {

    @Autowired
    private lateinit var bucketClient: BucketClient
    @Autowired
    private lateinit var audioTranscriber: AudioTranscriber

    private val thisClass: Class<AudioTranscriberImplTest> = this.javaClass
    private val tempConvertedFilesPath: String
    private val audioClipsResourcesPath: String

    companion object {
        @JvmField
        @ClassRule
        val mongoInitializer = TestMongoInitializer()
        private const val backgroundNoiseLowVolume = "background-noise-low-volume"
        private const val withApplause = "with-applause"
        private const val strongBackgroundNoise = "strong-background-noise"
    }

    init {
        audioClipsResourcesPath = thisClass.getResource("/audio-clips").path
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    @Test
    fun transcriptBackgroundNoiseLowVolumeAudioClips() {
        transcriptAudioClips(audioFileName = backgroundNoiseLowVolume)
    }

    @Test
    fun transcriptWithApplauseAudioClips() {
        transcriptAudioClips(audioFileName = withApplause)
    }

    @Test
    fun transcriptStrongBackgroundNoiseAudioClips() {
        transcriptAudioClips(audioFileName = strongBackgroundNoise)
    }

    private fun transcriptAudioClips(audioFileName: String) {
        val tempAudioClips = File("$audioClipsResourcesPath/$audioFileName").listFiles()
        .filter { !it.isDirectory }
        .map { audioClip ->
            val tempAudioClip = File("$tempConvertedFilesPath/${audioClip.name}")
            FileUtils.copyFile(audioClip, tempAudioClip)
            tempAudioClip
        }

        File("$audioClipsResourcesPath/$audioFileName/clip-info").listFiles()
        .map { JsonUtils.MAPPER.readValue(it, AudioClipInfo::class.java) }
        .forEach { audioClipInfo ->
            val tempAudioClip = tempAudioClips.find { it.nameWithoutExtension == audioClipInfo.audioClipName }
                ?: throw AssertionError("No temp audio clip was created for ${audioClipInfo.audioClipName}")
            When(bucketClient.downloadSourceFileFromBucket(audioClipInfo))
                .thenReturn(tempAudioClip)
            audioTranscriber.transcriptAudio(audioClipInfo)
        }
    }
}