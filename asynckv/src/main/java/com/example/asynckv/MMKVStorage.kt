import android.content.Context
import com.tencent.mmkv.MMKV
import kotlin.reflect.KClass

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
        val typeWrapKey = key.typeWrapKey(value)
        when (value) {
            is Int -> mmkv.encode(typeWrapKey, value)
            is Long -> mmkv.encode(typeWrapKey, value)
            is Float -> mmkv.encode(typeWrapKey, value)
            is Boolean -> mmkv.encode(typeWrapKey, value)
            is String -> mmkv.encode(typeWrapKey, value)
            is ByteArray -> mmkv.encode(typeWrapKey, value)
            is Set<*> -> mmkv.putStringSet(typeWrapKey, value as? Set<String>)
            else -> {
                //不支持的类型
                error("not support type ${value::class.java}")
            }
        }
    }


    private fun String.typeWrapKey(value: Any) = typeWrapKey(value::class)
    private fun String.typeWrapKey(value: KClass<*>): String {
        val typeSuffix = "@${value.simpleName}"
        return if (this.contains(typeSuffix)) {
            this
        } else {
            this + typeSuffix
        }

    }

    private val String.keyAndType
        get() = run {
            substringBefore("@") to substringAfter("@")
        }


    override suspend fun <T : Any> performGet(
        key: String,
        defaultValue: T,
        clazz: KClass<T>
    ): T? {
        val typeWrapKey = key.typeWrapKey(clazz)
        val value = when (defaultValue) {
            Int::class -> mmkv.getInt(typeWrapKey, defaultValue as Int)
            Long::class -> mmkv.getLong(typeWrapKey, defaultValue as Long)
            Float::class -> mmkv.getFloat(typeWrapKey, defaultValue as Float)
            Boolean::class -> mmkv.getBoolean(typeWrapKey, defaultValue as Boolean)
            String::class -> mmkv.getString(typeWrapKey, null)
            ByteArray::class -> mmkv.getBytes(typeWrapKey, null)
            Set::class -> mmkv.getStringSet(typeWrapKey, null)
            else -> {
                //不支持的类型
                error("not support type ${clazz.java}")
            }
        }
        return value as? T
    }

    override suspend fun performRemove(key: String) {
        mmkv.removeValueForKey(key)
    }

    override suspend fun performClear() {
        mmkv.clearAll()
    }

    override suspend fun performGetAll(): Map<String, Any> {
        return getOriginAllKeys()
            .associateWith { typeWrapKey ->
                val (_, type) = typeWrapKey.keyAndType
                when (type) {
                    Int::class.simpleName -> mmkv.getInt(typeWrapKey, 0)
                    Long::class.simpleName -> mmkv.getLong(typeWrapKey, 0L)
                    Float::class.simpleName -> mmkv.getFloat(typeWrapKey, 0F)
                    Boolean::class.simpleName -> mmkv.getBoolean(typeWrapKey, false)
                    String::class.simpleName -> mmkv.getString(typeWrapKey, null)
                    ByteArray::class.simpleName -> mmkv.getBytes(typeWrapKey, null)
                    else -> {
                        //不支持的类型
                        error("not support type $type")
                    }
                }
            }
            .mapKeys { it.key.keyAndType.first }
            .filterValues { it != null } as Map<String, Any>
    }


    private fun getOriginAllKeys(): List<String> {
        return mmkv.allKeys()?.toList() ?: emptyList()
    }

    override suspend fun getAllKeys(): List<String> {
        return getOriginAllKeys().map { it.keyAndType.first }
    }
}