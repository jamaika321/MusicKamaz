package ru.kamaz.music_api.models

data class ErrorMessage(
    val code: Int,
    val message: String,
    val detail: String
)