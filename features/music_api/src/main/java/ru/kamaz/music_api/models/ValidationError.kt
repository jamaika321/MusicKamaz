package ru.kamaz.music_api.models

data class ValidationError (
    val loc: List<String>,
    val msg: String,
    val type: String
)