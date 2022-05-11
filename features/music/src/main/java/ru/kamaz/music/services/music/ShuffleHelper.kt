package ru.kamaz.music.services.music

import ru.kamaz.music_api.models.Track

object ShuffleHelper {

    fun makeShuffleList(listToShuffle: MutableList<Track>, current: Int) {
        if (listToShuffle.isEmpty()) return
        if (current >= 0) {
            val song = listToShuffle.removeAt(current)
            listToShuffle.shuffle()
            listToShuffle.add(0, song)
        } else {
            listToShuffle.shuffle()
        }
    }
}