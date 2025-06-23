import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KClass

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
                is Set<*> -> putStringSet(key,value  as Set<String>)
                else -> {
                    //不支持的类型
                    error("not support type ${value::class.java}")
                }
            }
        }
    }


    override suspend fun <T : Any> performGet(
        key: String,
        defaultValue: T,
        clazz: KClass<T>
    ): T? {
        return when (clazz) {
            String::class -> prefs.getString(key, defaultValue as String)
            Int::class -> prefs.getInt(key, defaultValue as Int)
            Long::class -> prefs.getLong(key, defaultValue as Long)
            Float::class -> prefs.getFloat(key, defaultValue as Float)
            Boolean::class -> prefs.getBoolean(key, defaultValue as Boolean)
            Set::class -> prefs.getStringSet(key, defaultValue as Set<String>)
            else -> {
                //不支持的类型
                error("not support type ${clazz.java}")
            }
        } as? T
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

    override suspend fun performGetAll(): Map<String, Any> {
        return prefs.all.filter { it.value != null }.mapValues { it.value!! }
    }

    override suspend fun getAllKeys(): List<String> {
        return prefs.all.keys.toList()
    }
}