package com.example.asynckv

import KVStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by zhn on 2025/6/21.
 */

/**
 * 类型安全扩展
 */
suspend inline fun <reified T> KVStorage.getTyped(key: String, defaultValue: T): T {
    return when (T::class) {
        String::class -> getString(key, defaultValue as? String ?: "") as T
        Int::class -> getInt(key, defaultValue as? Int ?: 0) as T
        Long::class -> getLong(key, defaultValue as? Long ?: 0L) as T
        Float::class -> getFloat(key, defaultValue as? Float ?: 0f) as T
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}


/**
 * 协程作用域扩展
 */
fun KVStorage.withScope(scope: CoroutineScope): ScopedKVStorage {
    return ScopedKVStorageImpl(this, scope)
}

interface ScopedKVStorage {
    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String = "", callback: (String) -> Unit)
    // 其他方法...
}

private class ScopedKVStorageImpl(
    private val delegate: KVStorage,
    private val scope: CoroutineScope
) : ScopedKVStorage {
    override fun putString(key: String, value: String) {
        scope.launch {
            delegate.putString(key, value)
        }
    }

    override fun getString(key: String, defaultValue: String, callback: (String) -> Unit) {
        scope.launch {
            val value = delegate.getString(key, defaultValue)
            callback(value)
        }
    }
    // 其他方法实现...
}