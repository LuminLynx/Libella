package com.example.foss101.data.repository

import android.content.Context
import android.content.SharedPreferences

interface CompletionCache {
    fun completedUnitIds(): Set<String>
    fun add(unitId: String)
    fun clear()
}

class SharedPrefsCompletionCache(context: Context) : CompletionCache {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE
    )

    override fun completedUnitIds(): Set<String> {
        return prefs.getStringSet(KEY_UNIT_IDS, emptySet())?.toSet() ?: emptySet()
    }

    override fun add(unitId: String) {
        val current = completedUnitIds()
        if (unitId in current) return
        prefs.edit().putStringSet(KEY_UNIT_IDS, current + unitId).apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_UNIT_IDS).apply()
    }

    private companion object {
        const val FILE_NAME = "ai101_completion_cache"
        const val KEY_UNIT_IDS = "completed_unit_ids"
    }
}
