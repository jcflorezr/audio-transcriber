package net.jcflorezr.dao

import mu.KotlinLogging
import net.jcflorezr.model.AudioTranscript
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

interface AudioTranscriptDao {
    fun saveAudioTranscript(audioTranscript: AudioTranscript)
    fun getAudioTranscripts(audioFileName: String): List<AudioTranscript>
    fun getAudioTranscript(audioFileName: String, audioClipName: String): AudioTranscript?
}

@Repository
class AudioTranscriptDaoImpl : AudioTranscriptDao {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private val logger = KotlinLogging.logger { }

    override fun saveAudioTranscript(audioTranscript: AudioTranscript) {
        try {
            mongoTemplate.insert(audioTranscript)
        } catch (ex: DuplicateKeyException) {
            logger.warn {
                "Audio transcript: $audioTranscript already exists in the Transcript collection. It will not be stored in db."
            }
        }
    }

    override fun getAudioTranscripts(audioFileName: String): List<AudioTranscript> {
        val query = Query()
            .addCriteria(Criteria.where("audioFileName").`is`(audioFileName))
            .with(Sort.by(transcriptsOrder()))
        return mongoTemplate.query(AudioTranscript::class.java).matching(query).all()
    }

    private fun transcriptsOrder() = listOf(
        Sort.Order(Sort.Direction.ASC, "audioFileName"),
        Sort.Order(Sort.Direction.ASC, "clipTime.hours"),
        Sort.Order(Sort.Direction.ASC, "clipTime.minutes"),
        Sort.Order(Sort.Direction.ASC, "clipTime.seconds"),
        Sort.Order(Sort.Direction.ASC, "clipTime.tenthsOfSecond")
    )

    override fun getAudioTranscript(audioFileName: String, audioClipName: String): AudioTranscript? {
        val query = Query().addCriteria(Criteria
            .where("audioFileName").`is`(audioFileName)
            .and("audioClipName").`is`(audioClipName)
        )
        return mongoTemplate.query(AudioTranscript::class.java).matching(query).oneValue()
    }
}