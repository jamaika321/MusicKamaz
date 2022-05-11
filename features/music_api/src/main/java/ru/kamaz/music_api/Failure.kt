package ru.kamaz.music_api

import ru.kamaz.music_api.models.ErrorMessage
import ru.kamaz.music_api.models.HTTPValidationError
import ru.kamaz.music_api.models.ValidationError

sealed class Failure(
    val errorMessage: ErrorMessage? = null,
    val httpValidationError: HTTPValidationError? = null,
    val validationError: ValidationError? = null
) {
    object Empty : Failure()
    object NetworkConnectionError : Failure()
    object ServerError : Failure()
    class ServerNotFoundError(errorMessage: ErrorMessage?) : Failure(errorMessage = errorMessage)
    class AuthorizationError(errorMessage: ErrorMessage?) : Failure(errorMessage = errorMessage)
    class HttpValidationError(httpValidationError: HTTPValidationError?) : Failure(httpValidationError = httpValidationError)
    class OperationError(errorMessage: ErrorMessage?) : Failure(errorMessage = errorMessage)
    class Forbidden(errorMessage: ErrorMessage?) : Failure(errorMessage = errorMessage)
    object UnknownError : Failure()
}