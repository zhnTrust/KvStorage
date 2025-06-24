package com.zhn.asynckv

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by zhn on 2025/6/24.
 */

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