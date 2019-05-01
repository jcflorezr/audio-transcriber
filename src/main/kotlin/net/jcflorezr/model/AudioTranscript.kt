package net.jcflorezr.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

data class AudioClipInfo(
    val audioClipName: String,
    val audioFileName: String,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val initialPositionInSeconds: Float,
    val endPositionInSeconds: Float,
    val transactionId: String
)

@Document(collection = "audioTranscripts")
@CompoundIndexes(
    CompoundIndex(
        name = "audio_transcript_idx",
        def = "{'audioFileName': 1, 'clipTime.hours' : 1, 'clipTime.minutes': 1, 'clipTime.seconds': 1, 'clipTime.tenthsOfSecond': 1}, { unique: true }"
    )
)
data class AudioTranscript(
    @Id val id: String = UUID.randomUUID().toString(),
    val audioFileName: String,
    val clipTime: ClipTime,
    val audioClipName: String,
    val initialPositionInSeconds: Float,
    val endPositionInSeconds: Float,
    val transcripts: List<Transcript>
) {
    constructor(audioClipInfo: AudioClipInfo, transcripts: List<Transcript>) :
        this (
            audioFileName = audioClipInfo.audioFileName,
            clipTime = ClipTime(
                hours = audioClipInfo.hours,
                minutes = audioClipInfo.minutes,
                seconds = audioClipInfo.seconds,
                tenthsOfSecond = audioClipInfo.tenthsOfSecond
            ),
            audioClipName = audioClipInfo.audioClipName,
            initialPositionInSeconds = audioClipInfo.initialPositionInSeconds,
            endPositionInSeconds = audioClipInfo.endPositionInSeconds,
            transcripts = transcripts
        )
}

data class ClipTime(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int
) : Comparable<ClipTime> {

    override fun compareTo(other: ClipTime): Int {
        val hoursComparison = this.hours - other.hours
        return if (hoursComparison != 0) {
            hoursComparison
        } else {
            val minutesComparison = this.minutes - other.minutes
            if (minutesComparison != 0) {
                minutesComparison
            } else {
                val secondsComparison = this.seconds - other.seconds
                if (secondsComparison != 0) {
                    secondsComparison
                } else {
                    this.tenthsOfSecond - other.tenthsOfSecond
                }
            }
        }
    }
}

data class Transcript(
    val transcript: String,
    val confidence: Float
)