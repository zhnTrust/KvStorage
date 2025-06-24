package com.example.kvstorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.asynckv.datastore.DataStoreKVStorage
import com.example.asynckv.mmkv.MMKVStorage
import com.example.asynckv.preference.SharedPreferencesKVStorage
import com.zhn.asynckv.KVStorage

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
            StorageType.MMKV -> MMKVStorage(name)
            StorageType.DATASTORE -> {
                val dataStore = context.dataStore
                DataStoreKVStorage(dataStore)
            }
        }
    }
}

private val Context.dataStore by preferencesDataStore(name = "default_datastore")

//该写法不能保证唯一
fun Context.createDataStore(name: String): DataStore<Preferences> {
    return preferencesDataStore(name = name).getValue(this, ::dataStore)
}