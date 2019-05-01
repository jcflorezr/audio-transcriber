package net.jcflorezr.dao

import net.jcflorezr.model.AudioTranscript
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

interface AudioTranscriptDao {
    fun saveAudioTranscript(audioTranscript: AudioTranscript): AudioTranscript
    fun getAudioTranscripts(audioFileName: String): List<AudioTranscript>
    fun getAudioTranscript(audioFileName: String, audioClipName: String): AudioTranscript?
}

@Repository
class AudioTranscriptDaoImpl : AudioTranscriptDao {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun saveAudioTranscript(audioTranscript: AudioTranscript) = mongoTemplate.insert(audioTranscript)

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