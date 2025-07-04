package com.zhn.asynckv

import androidx.lifecycle.asLiveData
import com.zhn.asynckv.serialize.ObjectSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by zhn on 2025/6/18.
 *
 */
abstract class KvStorageDelegation(
    private val defaultScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val userId: (() -> String)? = null,
    private val storage: KVStorage,
) {
    private val scopedKv
        get() = storage.withScope(defaultScope)

    fun getAll(callback: (Map<String, *>) -> Unit) {
        scopedKv.getAll(callback)
    }

    fun clearAll() {
        scopedKv.clearAll()
    }

    fun putAll(data: Map<String, Any>) {
        scopedKv.putAll(data)
    }

    open inner class Key<T : Any>(
        open val key: String,
        val default: T
    ) {

        suspend fun getValue() = storage.getTyped(key, default)

        suspend fun putValue(value: T) {
            storage.putTyped(key, value)
        }

        fun getvalue(callback: (T) -> Unit) {
            scopedKv.getValue(key, default, callback)
        }

        fun getValueForMain(
            scope: CoroutineScope = defaultScope,
            onReceive: (T) -> Unit
        ) {
            scope.launch {
                val value = getValue()
                withContext(Dispatchers.Main.immediate) {
                    onReceive.invoke(value)
                }
            }
        }

        fun setValue(value: T) {
            scopedKv.putValue(key, value)
        }

        fun asFlow() = storage.observe<T>(key)
        fun asLiveData() = storage.observe<T>(key).asLiveData()
    }

    open inner class ObjKey<T : Any>(
        open val key: String,
        val serializer: ObjectSerializer<T>
    ) {

        suspend fun getValue() = storage.getObject(key, serializer)

        suspend fun putValue(value: T) {
            storage.putObject(key, value, serializer)
        }

        fun getValueForMain(
            scope: CoroutineScope = defaultScope,
            onReceive: suspend (T?) -> Unit
        ) {
            scope.launch {
                val value = getValue()
                withContext(Dispatchers.Main.immediate) {
                    onReceive.invoke(value)
                }
            }
        }

        fun asFlow() = storage.observe<T>(key)
        fun asLiveData() = storage.observe<T>(key).asLiveData()

        fun setValue(value: T) {
            scopedKv.putValue(key, value)
        }
    }

    inner class UserKey<T : Any>(
        key: String,
        default: T
    ) : Key<T>(key, default) {

        override val key: String = key
            get() {
                return field + requireNotNull(
                    userId,
                    { "[PrefUserKey] should use after [TypeDelegationPrefs.userId] imps" }).invoke()
            }
    }

    inner class UserObjKey<T : Any>(
        key: String,
        serializer: ObjectSerializer<T>
    ) : ObjKey<T>(key, serializer) {

        override val key: String = key
            get() {
                return field + requireNotNull(
                    userId,
                    { "[PrefUserKey] should use after [TypeDelegationPrefs.userId] imps" }).invoke()
            }
    }
}