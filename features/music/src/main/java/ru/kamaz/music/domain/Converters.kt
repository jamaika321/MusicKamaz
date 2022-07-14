package ru.kamaz.music.domain

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    companion object {
        val gson = Gson()

        @TypeConverter
        @JvmStatic
        fun stringToStringObjectList(data: String?): List<String?>? {
            if (data == null) {
                return emptyList<String>()
            }
            val listType = object : TypeToken<List<String?>?>() {}.type
            return Converters.gson.fromJson<List<String>>(data, listType)
        }

        @TypeConverter
        @JvmStatic
        fun stringObjectListToString(someObjects: List<String?>?): String? {
            return gson.toJson(someObjects)
        }
    }
}