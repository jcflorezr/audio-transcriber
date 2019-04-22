package net.jcflorezr.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.lang.RuntimeException

@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class AudioTranscriberException(
    val errorCode: String,
    override val message: String
) : RuntimeException()

class SourceAudioFileValidationException(
    message: String,
    errorCode: String
) : AudioTranscriberException(message = message, errorCode = errorCode) {
    companion object {
        fun audioFileDoesNotExistInBucket(audioFileName: String) =
            SourceAudioFileValidationException(
                errorCode = "audio_file_not_found_in_bucket",
                message = "Source audio file '$audioFileName' does not exist in the bucket."
            )
    }
}