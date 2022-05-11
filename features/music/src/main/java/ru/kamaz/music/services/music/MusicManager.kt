package ru.kamaz.music.services.music

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import ru.kamaz.music.data.MediaManager
import ru.kamaz.music.services.MusicService
import ru.kamaz.music_api.models.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicManager
@Inject constructor( val context: Context,
                    val dataManager: MediaManager,
                  )
    : MusicManagerI, MediaPlayer.OnCompletionListener {

    @Inject
    lateinit var mediaPlayer: MediaPlayer
    private var tracks = ArrayList<Track>()
    private var currentTrackPosition = 0
    private var resumePosition = 0

    private var musicService: MusicService? = null
    override fun setService(service: MusicService) {
        this.musicService = service
    }

    override fun playTrack() {

        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(tracks[currentTrackPosition].data)
        mediaPlayer.prepare()
        mediaPlayer.start()
        buildNotification()
    }

    override fun pauseTrack() {
        TODO("Not yet implemented")
    }

    override fun resumeTrack() {
        TODO("Not yet implemented")
    }

    override fun resumePause() {
        TODO("Not yet implemented")
    }

    override fun updateTracks(tracks: ArrayList<Track>, currentTrackPosition: Int) {
        this.tracks = tracks
        this.currentTrackPosition = currentTrackPosition
    }

    override fun getCurrentTrack(): Track {
        TODO("Not yet implemented")
    }

    override fun previousTrack() {
        TODO("Not yet implemented")
    }

    override fun nextTrack() {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        TODO("Not yet implemented")
    }

    override fun buildNotification() {

    }

    override fun closeMusicPlayer() {
        TODO("Not yet implemented")
    }

    override fun getTracksSize(): Int {
        TODO("Not yet implemented")
    }

    override fun onCompletion(mp: MediaPlayer?) {
        TODO("Not yet implemented")
    }


}