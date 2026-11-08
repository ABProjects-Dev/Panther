package com.panther.app.core.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "settings")

enum class ThemeChoice { DARK, LIGHT, SYSTEM }

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val key = stringPreferencesKey("theme")
    val choice = context.themeDataStore.data
        .catch { e ->
            if (e is CancellationException) throw e
            if (e !is IOException) {
                runCatching {
                    Log.w("ThemePreferences", "Recovering from preferences read failure: ${e::class.java.simpleName}")
                }
            }
            emit(emptyPreferences())
        }
        .map { prefs ->
            ThemeChoice.entries.firstOrNull { it.name == prefs[key] } ?: ThemeChoice.DARK
        }

    suspend fun set(choice: ThemeChoice) {
        context.themeDataStore.edit { it[key] = choice.name }
    }
}
