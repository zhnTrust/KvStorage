package com.zhn.asynckv

import kotlinx.coroutines.CoroutineScope

/**
 * Created by zhn on 2025/6/21.
 *
 * 相关扩展
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


