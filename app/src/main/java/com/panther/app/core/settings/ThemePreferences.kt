package com.panther.app.core.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import androidx.datastore.preferences.core.emptyPreferences

private val Context.themeDataStore by preferencesDataStore(name = "settings")
enum class ThemeChoice { DARK, LIGHT, SYSTEM }
@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val key = stringPreferencesKey("theme")
    val choice = context.themeDataStore.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { prefs ->
        ThemeChoice.entries.firstOrNull { it.name == prefs[key] } ?: ThemeChoice.DARK
    }
    suspend fun set(choice: ThemeChoice) { context.themeDataStore.edit { it[key] = choice.name } }
}
