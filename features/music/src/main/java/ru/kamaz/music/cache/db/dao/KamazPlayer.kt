package ru.kamaz.music.cache.db.dao

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import ru.kamaz.music.R


class KamazPlayer internal constructor(private val context: Context?) : Playback,
    MediaPlayer.OnErrorListener, OnCompletionListener {
    private var mCurrentMediaPlayer = MediaPlayer()
    private var mNextMediaPlayer: MediaPlayer? = null
    private var callbacks: Playback.PlaybackCallbacks? = null

    /** @return True if the player is ready to go, false otherwise
     */
    override var isInitialized = false
        private set

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream you want to play
     * @return True if the `player` has been prepared and is ready to play, false otherwise
     */
    override fun setDataSource(path: String): Boolean {
        this.isInitialized = false
        this.isInitialized = setDataSourceImpl(mCurrentMediaPlayer, path)
        if (this.isInitialized) {
            setNextDataSource(null)
        }
        return this.isInitialized
    }

    /**
     * @param player The [MediaPlayer] to use
     * @param path The path of the file, or the http/rtsp URL of the stream you want to play
     * @return True if the `player` has been prepared and is ready to play, false otherwise
     */
    private fun setDataSourceImpl(player: MediaPlayer, path: String): Boolean {
        if (context == null) {
            return false
        }
        try {
            player.reset()
            player.setOnPreparedListener(null)
            if (path.startsWith("content://")) {
                player.setDataSource(context, Uri.parse(path))
            } else {
                player.setDataSource(path)
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.prepare()
        } catch (e: Exception) {
            return false
        }
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
        val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        context.sendBroadcast(intent)
        return true
    }

    /**
     * Set the MediaPlayer to start when this MediaPlayer finishes playback.
     *
     * @param path The path of the file, or the http/rtsp URL of the stream you want to play
     */
    override fun setNextDataSource(path: String?) {
        if (context == null) {
            return
        }
        try {
            mCurrentMediaPlayer.setNextMediaPlayer(null)
        } catch (e: IllegalArgumentException) {
            Log.i(TAG, "Next media player is current one, continuing")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Media player not initialized!")
            return
        }
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer!!.release()
            mNextMediaPlayer = null
        }
        if (path == null) {
            return
        }
       /* if (PreferenceUtil.INSTANCE.isGapLessPlayback()) {
            mNextMediaPlayer = MediaPlayer()
            mNextMediaPlayer!!.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            mNextMediaPlayer!!.audioSessionId = audioSessionId
            if (setDataSourceImpl(mNextMediaPlayer!!, path)) {
                try {
                    mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "setNextDataSource: setNextMediaPlayer()", e)
                    if (mNextMediaPlayer != null) {
                        mNextMediaPlayer!!.release()
                        mNextMediaPlayer = null
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "setNextDataSource: setNextMediaPlayer()", e)
                    if (mNextMediaPlayer != null) {
                        mNextMediaPlayer!!.release()
                        mNextMediaPlayer = null
                    }
                }
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer!!.release()
                    mNextMediaPlayer = null
                }
            }
        }*/
    }

    override fun setCallbacks(callbacks: Playback.PlaybackCallbacks) {
        this.callbacks = callbacks
    }


    override fun start(): Boolean {
        return try {
            mCurrentMediaPlayer.start()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /** Resets the MediaPlayer to its uninitialized state.  */
    override fun stop() {
        mCurrentMediaPlayer.reset()
        this.isInitialized = false
    }

    /** Releases resources associated with this MediaPlayer object.  */
    override fun release() {
        stop()
        mCurrentMediaPlayer.release()
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer!!.release()
        }
    }

    /** Pauses playback. Call start() to resume.  */
    override fun pause(): Boolean {
        return try {
            mCurrentMediaPlayer.pause()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /** Checks whether the MultiPlayer is playing.  */
    override val isPlaying: Boolean
        get() = this.isInitialized && mCurrentMediaPlayer.isPlaying

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    override fun duration(): Int {
        return if (!this.isInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.duration
        } catch (e: IllegalStateException) {
            -1
        }
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    override fun position(): Int {
        return if (!this.isInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.currentPosition
        } catch (e: IllegalStateException) {
            -1
        }
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    override fun seek(whereto: Int): Int {
        return try {
            mCurrentMediaPlayer.seekTo(whereto)
            whereto
        } catch (e: IllegalStateException) {
            -1
        }
    }

    override fun setVolume(vol: Float): Boolean {
        return try {
            mCurrentMediaPlayer.setVolume(vol, vol)
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    override fun setAudioSessionId(sessionId: Int): Boolean {
        return try {
            mCurrentMediaPlayer.audioSessionId = sessionId
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    override val audioSessionId: Int
        get() = mCurrentMediaPlayer.audioSessionId

    /** {@inheritDoc}  */
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        this.isInitialized = false
        mCurrentMediaPlayer.release()
        mCurrentMediaPlayer = MediaPlayer()
        mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        if (context != null) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.add_widget),
                Toast.LENGTH_SHORT
            )
                .show()
        }
        return false
    }

    /** {@inheritDoc}  */
    override fun onCompletion(mp: MediaPlayer) {
        if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
            this.isInitialized = false
            mCurrentMediaPlayer.release()
            mCurrentMediaPlayer = mNextMediaPlayer as MediaPlayer
            this.isInitialized = true
            mNextMediaPlayer = null
            callbacks?.onTrackWentToNext()
        } else {
            callbacks?.onTrackEnded()
        }
    }

    companion object {
        val TAG = KamazPlayer::class.java.simpleName
    }

    /** Constructor of `MultiPlayer`  */
    init {
        mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
    }
}