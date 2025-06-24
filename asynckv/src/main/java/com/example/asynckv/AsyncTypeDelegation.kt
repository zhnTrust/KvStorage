package com.example.asynckv

import KVStorage
import ObjectSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by zhn on 2025/6/18.
 */
abstract class AsyncTypeDelegation(
    private val defaultScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val userId: (() -> String)? = null,
    private val storage: () -> KVStorage,
) {
    private val delegate
        get() = storage()

    suspend fun getAll() = delegate.getAll()

    suspend fun clearAll() {
        delegate.clear()
    }

    suspend fun putAll(data: Map<String, Any>) {
        delegate.putAll(data)
    }

    open inner class PrefKey<T : Any>(
        open val key: String,
        val default: T
    ) {

        suspend fun getValue() = delegate.getTyped(key, default)

        suspend fun setValue(value: T) {
            delegate.putTyped(key, value)
        }

        fun getValueForMain(
            scope: CoroutineScope = defaultScope,
            onReceive: suspend (T) -> Unit
        ) {
            scope.launch {
                val value = getValue()
                withContext(Dispatchers.Main.immediate) {
                    onReceive.invoke(value)
                }
            }
        }

//        fun asFlow() = delegate.observe<T>(key)
//        fun asLiveData() = delegate.observe<T>(key).asLiveData()

        fun apply(value: T) {
            defaultScope.launch { setValue(value) }
        }
    }

    open inner class PrefObjKey<T : Any>(
        open val key: String,
        val serializer: ObjectSerializer<T>
    ) {

        suspend fun getValue() = delegate.getObject(key, serializer)

        suspend fun setValue(value: T) {
            delegate.putObject(key, value, serializer)
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

//        fun asFlow() = delegate.observe<T>(key)
//        fun asLiveData() = delegate.observe<T>(key).asLiveData()

        fun apply(value: T) {
            defaultScope.launch { setValue(value) }
        }
    }

    inner class PrefUserKey<T : Any>(
        key: String,
        default: T
    ) : PrefKey<T>(key, default) {

        override val key: String = key
            get() {
                return field + requireNotNull(
                    userId,
                    { "[PrefUserKey] should use after [TypeDelegationPrefs.userId] imps" }).invoke()
            }
    }

    inner class PrefUserObjKey<T : Any>(
        key: String,
        serializer: ObjectSerializer<T>
    ) : PrefObjKey<T>(key, serializer) {

        override val key: String = key
            get() {
                return field + requireNotNull(
                    userId,
                    { "[PrefUserKey] should use after [TypeDelegationPrefs.userId] imps" }).invoke()
            }
    }
}
