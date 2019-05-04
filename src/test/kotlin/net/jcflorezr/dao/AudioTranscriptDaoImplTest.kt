package net.jcflorezr.dao

import net.jcflorezr.config.TestAudioTranscriptDaoConfig
import net.jcflorezr.model.AudioTranscript
import net.jcflorezr.util.JsonUtils
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.util.UUID

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestAudioTranscriptDaoConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AudioTranscriptDaoImplTest {

    private val thisClass: Class<AudioTranscriptDaoImplTest> = this.javaClass
    private val audioClipsResourcesPath: String

    @Autowired
    private lateinit var audioTranscriptDao: AudioTranscriptDao

    companion object {
        @JvmField
        @ClassRule
        val mongoInitializer = TestMongoInitializer()
        private const val backgroundNoiseLowVolume = "background-noise-low-volume"
        private const val mockTransactionId = "any-transaction-id"
    }

    init {
        audioClipsResourcesPath = thisClass.getResource("/audio-clips").path
    }

    @Test
    fun storeAudioTranscripts() {
        File("$audioClipsResourcesPath/$backgroundNoiseLowVolume/db-objects/").listFiles()
        .map { JsonUtils.MAPPER.readValue(it, AudioTranscript::class.java) }
        .forEach { expectedTranscript ->
            audioTranscriptDao.saveAudioTranscript(audioTranscript = expectedTranscript, transactionId = mockTransactionId)
            val actualTranscript = audioTranscriptDao.getAudioTranscript(
                audioFileName = expectedTranscript.audioFileName,
                audioClipName = expectedTranscript.audioClipName
            )
            assertThat(actualTranscript, Is(equalTo(expectedTranscript)))
        }
        val audioTranscripts = audioTranscriptDao.getAudioTranscripts(audioFileName = "$backgroundNoiseLowVolume.flac")
        assertThat(audioTranscripts.size, Is(equalTo(4)))
        audioTranscripts.forEachIndexed { index, audioTranscript ->
            if (index == audioTranscripts.size - 1) {
                return@forEachIndexed
            }
            assertTrue(audioTranscript.clipTime < audioTranscripts[index + 1].clipTime)
        }
        audioTranscriptDao.dropCollection()
    }

    @Test
    fun storeDuplicateAudioTranscripts() {
        val transcript1 = File("$audioClipsResourcesPath/$backgroundNoiseLowVolume/db-objects/0_2.json")
            .let { JsonUtils.MAPPER.readValue(it, AudioTranscript::class.java) }
        val transcript2 = File("$audioClipsResourcesPath/$backgroundNoiseLowVolume/db-objects/13_2.json")
            .let { JsonUtils.MAPPER.readValue(it, AudioTranscript::class.java) }
        audioTranscriptDao.saveAudioTranscript(audioTranscript = transcript2, transactionId = mockTransactionId)
        audioTranscriptDao.saveAudioTranscript(audioTranscript = transcript1, transactionId = mockTransactionId)
        audioTranscriptDao.saveAudioTranscript(
            audioTranscript = transcript1.copy(id = UUID.randomUUID().toString()), transactionId = mockTransactionId
        )
        audioTranscriptDao.saveAudioTranscript(
            audioTranscript = transcript2.copy(id = UUID.randomUUID().toString()), transactionId = mockTransactionId
        )
        val audioTranscripts = audioTranscriptDao.getAudioTranscripts(audioFileName = "$backgroundNoiseLowVolume.flac")
        assertThat(audioTranscripts.size, Is(equalTo(2)))
        audioTranscripts.forEachIndexed { index, audioTranscript ->
            if (index == audioTranscripts.size - 1) {
                return@forEachIndexed
            }
            assertTrue(audioTranscript.clipTime < audioTranscripts[index + 1].clipTime)
        }
        audioTranscriptDao.dropCollection()
    }
}