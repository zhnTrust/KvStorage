import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

class DataStoreKVStorage(
    private val dataStore: DataStore<Preferences>
) : BaseKVStorage() {
    override suspend fun performPut(key: String, value: Any) {
        dataStore.edit { prefs ->
            val prefKey = stringPreferencesKey(key)
            prefs[prefKey] = value.toString()
        }
    }

    override suspend fun performGet(key: String, kClass: KClass<*>): Any? {
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

    override suspend fun performGetAll(): Map<String, Any> {
        return dataStore.data.first().asMap().mapKeys { it.key.name }
    }

    override suspend fun getAllKeys(): List<String> {
        return dataStore.data.first().asMap().map { it.key.name }
    }
}