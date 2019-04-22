package net.jcflorezr.model

import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

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

@Document
@CompoundIndexes(
    CompoundIndex(
        name = "email_age",
        def = "{'clipTime.hours' : 1, 'clipTime.minutes': 1, 'clipTime.seconds': 1, 'clipTime.tenthsOfSecond': 1}"))
data class AudioTranscript(
    @Indexed(direction = IndexDirection.ASCENDING)
    val audioFileName: String,
    @DBRef
    val clipTime: ClipTime,
    val clipName: String,
    val initialPositionInSeconds: Float,
    val endPositionInSeconds: Float,
    @DBRef
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
            clipName = audioClipInfo.audioClipName,
            initialPositionInSeconds = audioClipInfo.initialPositionInSeconds,
            endPositionInSeconds = audioClipInfo.endPositionInSeconds,
            transcripts = transcripts
        )
}

@Document
data class ClipTime(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int
)

@Document
data class Transcript(
    val transcript: String,
    val confidence: Float
)