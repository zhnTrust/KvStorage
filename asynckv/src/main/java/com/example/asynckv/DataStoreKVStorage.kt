import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreKVStorage(
    private val dataStore: DataStore<Preferences>
) : BaseKVStorage() {
    override suspend fun performPut(key: String, value: Any) {
        dataStore.edit { prefs ->
            val prefKey = stringPreferencesKey(key)
            prefs[prefKey] = value.toString()
        }
    }

    override suspend fun performGet(key: String): Any? {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data
            .map { prefs -> prefs[prefKey] }
            .first()
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

    override suspend fun performGetAll(keys: List<String>): Map<String, Any> {
        return dataStore.data
            .map { prefs ->
                keys.associateWith { key ->
                    val prefKey = stringPreferencesKey(key)
                    prefs[prefKey]
                }.filterValues { it != null }
            }
            .first() as Map<String, Any>
    }
}