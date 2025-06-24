package com.example.asynckv.mmkv

import com.tencent.mmkv.MMKV
import com.zhn.asynckv.BaseKVStorage
import kotlin.reflect.KClass

/**
 * Created by zhn on 2025/6/24.
 */
class MMKVStorage(
    private val mmkv: MMKV
) : BaseKVStorage() {
    constructor(id: String = "default_mmkv") : this(
        MMKV.mmkvWithID(
            id,
            MMKV.SINGLE_PROCESS_MODE,
        )
    )

    override suspend fun performPut(key: String, value: Any) {
        when (value) {
            is Int -> mmkv.encode(getTypeKey<Int>(key), value)
            is Long -> mmkv.encode(getTypeKey<Long>(key), value)
            is Float -> mmkv.encode(getTypeKey<Float>(key), value)
            is Boolean -> mmkv.encode(getTypeKey<Boolean>(key), value)
            is String -> mmkv.encode(getTypeKey<String>(key), value)
            is ByteArray -> mmkv.encode(getTypeKey<ByteArray>(key), value)
            is Set<*> -> mmkv.putStringSet(getTypeKey<Set<String>>(key), value as? Set<String>)
            else -> {
                //不支持的类型
                error("not support type ${value::class.java}")
            }
        }
    }


    private inline fun <reified T> getTypeKey(key: String): String {
        val type = "@" + T::class.simpleName
        return if (key.contains(type)) {
            type
        } else {
            key + type
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
    ): T {
        val value = when (clazz) {
            Int::class -> mmkv.getInt(getTypeKey<Int>(key), defaultValue as Int)
            Long::class -> mmkv.getLong(getTypeKey<Long>(key), defaultValue as Long)
            Float::class -> mmkv.getFloat(getTypeKey<Float>(key), defaultValue as Float)
            Boolean::class -> mmkv.getBoolean(getTypeKey<Boolean>(key), defaultValue as Boolean)
            String::class -> mmkv.getString(getTypeKey<String>(key), null)
            ByteArray::class -> mmkv.getBytes(getTypeKey<ByteArray>(key), null)
            Set::class -> mmkv.getStringSet(getTypeKey<Set<String>>(key), null)
            else -> {
                //不支持的类型
                error("not support type ${clazz.java}")
            }
        } as? T
        return value ?: defaultValue
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
                    Set::class.simpleName -> mmkv.getStringSet(typeWrapKey, null)
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