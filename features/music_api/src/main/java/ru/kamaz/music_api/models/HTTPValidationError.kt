package ru.kamaz.music_api.models

data class HTTPValidationError(
    val detail: List<ValidationError>
)