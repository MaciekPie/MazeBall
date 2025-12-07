package com.example.mazeball

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LevelStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("levels_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val KEY_LEVELS = "levels"

    fun getLevels(): MutableList<Level> {
        val json = prefs.getString(KEY_LEVELS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Level>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveLevels(levels: List<Level>) {
        val json = gson.toJson(levels)
        prefs.edit().putString(KEY_LEVELS, json).apply()
    }

    fun addLevel(level: Level) {
        val levels = getLevels()
        // jeśli chcesz nadpisywać po id, możesz tu usunąć istniejący o tym samym id
        levels.add(level)
        saveLevels(levels)
    }

    fun getLevelById(id: Int): Level? {
        return getLevels().find { it.id == id }
    }
}
