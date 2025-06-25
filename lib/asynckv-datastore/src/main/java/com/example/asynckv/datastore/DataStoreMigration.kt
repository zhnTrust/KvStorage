package com.example.asynckv.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.zhn.asynckv.KVStorage
import com.zhn.asynckv.KVDataMigration
import kotlinx.coroutines.flow.first

/**
 * Created by zhn on 2025/6/24.
 *
 * DataStore迁移实现
 */
class DataStoreMigration(private val dataStore: DataStore<Preferences>) : KVDataMigration {

    suspend fun DataStore<Preferences>.all() = data.first().asMap().mapKeys { it.key.name }

    override suspend fun shouldMigrate(asyncKv: KVStorage): Boolean {
        return !dataStore.all().isEmpty()
    }

    override suspend fun migrate(asyncKv: KVStorage) {
        asyncKv.putAll(dataStore.all())

    }

    override suspend fun cleanUp() {
        dataStore.edit {
            it.clear()
        }
    }
}