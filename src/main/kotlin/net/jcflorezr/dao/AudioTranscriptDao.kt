package net.jcflorezr.dao

import net.jcflorezr.model.AudioTranscript
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

interface AudioTranscriptDao {
    fun saveAudioClipInfo(audioTranscript: AudioTranscript): AudioTranscript
}

@Repository
class AudioTranscriptDaoImpl : AudioTranscriptDao {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun saveAudioClipInfo(audioTranscript: AudioTranscript) = mongoTemplate.save(audioTranscript)
}