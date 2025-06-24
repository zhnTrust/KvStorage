package com.example.asynckv.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.zhn.asynckv.BaseKVStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

/**
 * Created by zhn on 2025/6/24.
 *
 * DataStore实现
 */
class DataStoreKVStorage(
    private val dataStore: DataStore<Preferences>
) : BaseKVStorage() {
    override suspend fun performPut(key: String, value: Any) {
        dataStore.edit { prefs ->
            prefs
            when (value) {
                is Int -> prefs[intPreferencesKey(key)] = value
                is Long -> prefs[longPreferencesKey(key)] = value
                is Float -> prefs[floatPreferencesKey(key)] = value
                is Boolean -> prefs[booleanPreferencesKey(key)] = value
                is String -> prefs[stringPreferencesKey(key)] = value
                is Set<*> -> prefs[stringSetPreferencesKey(key)] = value as Set<String>
                is ByteArray -> prefs[byteArrayPreferencesKey(key)] = value
                else -> {
                    //不支持的类型
                    error("not support type ${value::class.simpleName}")
                }
            }
        }
    }

    override suspend fun <T : Any> performGet(
        key: String,
        defaultValue: T,
        kClass: KClass<T>
    ): T {
        val prefKey =
            when (kClass) {
                Int::class -> intPreferencesKey(key)
                Long::class -> longPreferencesKey(key)
                Float::class -> floatPreferencesKey(key)
                Boolean::class -> booleanPreferencesKey(key)
                String::class -> stringPreferencesKey(key)
                ByteArray::class -> byteArrayPreferencesKey(key)
                Set::class -> stringSetPreferencesKey(key)
                else -> {
                    //不支持的类型
                    error("not support type ${kClass.simpleName}")
                }
            }
        val res = dataStore.data
            .map { prefs -> prefs[prefKey] }
            .first() as? T
        return res ?: defaultValue
    }

    override suspend fun performRemove(key: String) {
        dataStore.edit { prefs ->
            val prefKey = stringPreferencesKey(key)
            prefs.remove(prefKey)
        }
    }

    override suspend fun performClear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    override suspend fun performGetAll(): Map<String, Any> {
        return dataStore.data.first().asMap().mapKeys { it.key.name }
    }

    override suspend fun getAllKeys(): List<String> {
        return dataStore.data.first().asMap().map { it.key.name }
    }
}