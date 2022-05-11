package ru.kamaz.music.services.music

import ru.kamaz.music.services.MusicService
import ru.kamaz.music_api.models.Track

interface MusicManagerI {
    fun setService(service: MusicService)

    fun playTrack()
    fun pauseTrack()
    fun resumeTrack()

    fun resumePause()

    fun updateTracks(tracks: ArrayList<Track>, currentTrackPosition: Int)

    fun getCurrentTrack(): Track

    fun previousTrack()
    fun nextTrack()

    fun isPlaying(): Boolean

    fun setVolume(leftVolume: Float, rightVolume: Float)

    fun buildNotification()
    fun closeMusicPlayer()


    fun getTracksSize(): Int

}