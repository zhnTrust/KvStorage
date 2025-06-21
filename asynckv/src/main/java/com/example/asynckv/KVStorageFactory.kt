package com.example.asynckv

import DataStoreKVStorage
import KVStorage
import MMKVStorage
import SharedPreferencesKVStorage
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object KVStorageFactory {
    enum class StorageType {
        SHARED_PREFERENCES,
        MMKV,
        DATASTORE
    }
    
    fun create(
        context: Context,
        type: StorageType = StorageType.SHARED_PREFERENCES,
        name: String = "default_kv_storage"
    ): KVStorage {
        return when (type) {
            StorageType.SHARED_PREFERENCES -> SharedPreferencesKVStorage(context, name)
            StorageType.MMKV -> MMKVStorage(context, name)
            StorageType.DATASTORE -> {
                val dataStore = context.createDataStore(name)
                DataStoreKVStorage(dataStore)
            }
        }
    }
}

private val Context.dataStore by preferencesDataStore(name = "default_datastore")

fun Context.createDataStore(name: String): DataStore<Preferences> {
    return preferencesDataStore(name = name).getValue(this, ::dataStore)
}