package com.zhn.asynckv

/**
 * Created by zhn on 2025/6/24.
 */
interface ScopedKVStorage {
    fun getAll(callback: (Map<String, *>) -> Unit)
    fun putValue(key: String, value: Any)
    fun <T : Any> getValue(key: String, defaultValue: T, callback: (T) -> Unit)
    fun clearAll()
    fun putAll(data: Map<String, Any>)
}