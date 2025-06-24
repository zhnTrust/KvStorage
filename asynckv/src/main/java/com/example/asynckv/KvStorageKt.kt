package com.example.asynckv

import KVStorage
import androidx.lifecycle.liveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by zhn on 2025/6/21.
 */

/**
 * 类型安全扩展
 */
suspend fun <T : Any> KVStorage.getTyped(key: String, defaultValue: T): T {
    return when (defaultValue) {
        is String -> getString(key, defaultValue) as T
        is Int -> getInt(key, defaultValue) as T
        is Long -> getLong(key, defaultValue) as T
        is Float -> getFloat(key, defaultValue) as T
        is Boolean -> getBoolean(key, defaultValue) as T
        is Set<*> -> getStringSet(key, defaultValue as Set<String>) as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

suspend fun <T : Any> KVStorage.putTyped(key: String, defaultValue: T) {
    when (defaultValue) {
        is String -> putString(key, defaultValue)
        is Int -> putInt(key, defaultValue)
        is Long -> putLong(key, defaultValue)
        is Float -> putFloat(key, defaultValue)
        is Boolean -> putBoolean(key, defaultValue)
        is Set<*> -> putStringSet(key, defaultValue as Set<String>)
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
    fun getAll(callback: (Map<String, *>) -> Unit)
    fun putValue(key: String, value: Any)
    fun <T : Any> getValue(key: String, defaultValue: T, callback: (T) -> Unit)
    fun clearAll()
    fun putAll(data: Map<String, Any>)
}

class ScopedKVStorageImpl(
    private val delegate: KVStorage,
    private val scope: CoroutineScope
) : ScopedKVStorage {
    override fun getAll(callback: (Map<String, *>) -> Unit) {
        scope.launch {
            val all = delegate.getAll()
            callback(all)
        }
    }

    override fun putValue(key: String, value: Any) {
        scope.launch {
            delegate.putTyped(key, value)
        }
    }

    override fun <T : Any> getValue(key: String, defaultValue: T, callback: (T) -> Unit) {
        scope.launch {
            val value = delegate.getTyped(key, defaultValue)
            callback.invoke(value)
        }
    }

    override fun clearAll() {
        scope.launch { delegate.clear() }
    }

    override fun putAll(data: Map<String, Any>) {
        scope.launch {
            delegate.putAll(data)
        }
    }

}
