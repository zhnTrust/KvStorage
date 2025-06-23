package com.example.asynckv

import KVDataMigration
import KVStorage
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

class DataStoreMigration(produce: () -> DataStore<Preferences>) : KVDataMigration {
    private val dataStore by lazy(produce)

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