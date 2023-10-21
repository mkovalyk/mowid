package com.kovcom.data.preferences.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.kovcom.data.preferences.LocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


class LocalDataSourceImpl  constructor(
    @Named("LocalDataStore")
    override val coroutineContext: CoroutineContext,
    context: Context,
) : LocalDataSource, CoroutineScope {

    private val Context.dataStoreDelegate by preferencesDataStore("settings")
    private val dataStore: DataStore<Preferences> = context.dataStoreDelegate

    override val testValue: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[TEST_VALUE] ?: false
    }

    override fun setTestValue(value: Boolean) {
        launch {
            dataStore.edit { preferences ->
                preferences[TEST_VALUE] = value
            }
        }
    }

    override val quoteChangeOption: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[QUOTE_CHANGE_OPTION]
        }

    override fun setQuoteChangeOption(value: String?) {
        launch {
            dataStore.edit { preferences ->
                if (value == null) {
                    preferences.remove(QUOTE_CHANGE_OPTION)
                } else {
                    preferences[QUOTE_CHANGE_OPTION] = value
                }
            }
        }
    }

    override val token: String
        get() = runBlocking {
            tokenFlow.first().orEmpty()
        }

    override val tokenFlow: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[TOKEN]
        }

    override fun setToken(value: String?) {
        launch {
            dataStore.edit { preferences ->
                if (value == null) {
                    preferences.remove(TOKEN)
                } else {
                    preferences[TOKEN] = value
                }
            }
        }
    }

    override val frequency: Flow<Long>
        get() = dataStore.data.map { preferences ->
            preferences[FREQUENCY] ?: 0L
        }

    override fun setFrequency(value: Long) {
        launch {
            dataStore.edit { preferences ->
                preferences[FREQUENCY] = value
            }
        }
    }

    override val selectedLocale: Flow<String>
        get() = dataStore.data.map { preferences ->
            val result = preferences[SELECTED_LOCALE] ?: Locale.getDefault().language
            // log 
            Timber.tag("LocalDataSourceImpl").i("selectedLocale: $result")
            result
        }

    override fun setSelectedLocale(value: String) {
        launch {
            dataStore.edit { preferences ->
                preferences[SELECTED_LOCALE] = value
            }
        }
    }

    companion object {

        private val TEST_VALUE = booleanPreferencesKey("TEST_VALUE")
        private val QUOTE_CHANGE_OPTION = stringPreferencesKey("QUOTE_CHANGE_OPTION")
        private val TOKEN = stringPreferencesKey("TOKEN")
        private val FREQUENCY = longPreferencesKey("FREQUENCY")
        private val SELECTED_LOCALE = stringPreferencesKey("SELECTED_LOCALE")
    }

}
