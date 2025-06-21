import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesKVStorage(
    private val context: Context,
    private val name: String = "default_kv_storage"
) : BaseKVStorage() {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    override suspend fun performPut(key: String, value: Any) {
        prefs.edit(commit = true) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value.toString())
            }
        }
    }

    override suspend fun performGet(key: String): Any? {
        return if (prefs.contains(key)) {
            prefs.all[key]
        } else {
            null
        }
    }

    override suspend fun performRemove(key: String) {
        prefs.edit(commit = true) {
            remove(key)
        }
    }

    override suspend fun performClear() {
        prefs.edit(commit = true) {
            clear()
        }
    }

    override suspend fun performGetAll(keys: List<String>): Map<String, Any> {
        return prefs.all.filterKeys { it in keys }.mapValues { it.value!! }
    }

    override suspend fun getAllKeys(): List<String> {
        return prefs.all.keys.toList()
    }
}