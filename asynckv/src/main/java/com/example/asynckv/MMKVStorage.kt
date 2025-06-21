import android.content.Context
import com.tencent.mmkv.MMKV

class MMKVStorage(
    private val mmkv: MMKV
) : BaseKVStorage() {
    constructor(context: Context, id: String = "default_mmkv") : this(
        MMKV.mmkvWithID(
            id,
            MMKV.SINGLE_PROCESS_MODE,
        )
    )

    override suspend fun performPut(key: String, value: Any) {
        when (value) {
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is ByteArray -> mmkv.encode(key, value)
            else -> mmkv.encode(key, value.toString())
        }
    }

    override suspend fun performGet(key: String): Any? {
        return when {
            mmkv.contains(key) -> {
                val value = mmkv.decodeString(key)
                if (value != null) return value

                val intVal = mmkv.decodeInt(key, Int.MIN_VALUE)
                if (intVal != Int.MIN_VALUE) return intVal

                val longVal = mmkv.decodeLong(key, Long.MIN_VALUE)
                if (longVal != Long.MIN_VALUE) return longVal

                val floatVal = mmkv.decodeFloat(key, Float.NaN)
                if (!floatVal.isNaN()) return floatVal

                mmkv.decodeBool(key, false)
            }

            else -> null
        }
    }

    override suspend fun performRemove(key: String) {
        mmkv.removeValueForKey(key)
    }

    override suspend fun performClear() {
        mmkv.clearAll()
    }

    override suspend fun performGetAll(keys: List<String>): Map<String, Any> {
        return keys.associateWith { performGet(it) }.filterValues { it != null } as Map<String, Any>
    }

    override suspend fun getAllKeys(): List<String> {
        return mmkv.allKeys()?.toList() ?: emptyList()
    }
}