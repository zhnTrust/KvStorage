package com.zhn.asynckv

import com.zhn.asynckv.crypto.KVEncryptor
import com.zhn.asynckv.serialize.ObjectSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

/**
 * Created by zhn on 2025/6/24.
 *
 * 实现KVStorage的基类,抽象封装
 */
abstract class BaseKVStorage(private val initMigrations: List<KVDataMigration>? = null) : KVStorage {
    private val mutex = Mutex()
    private var isInit = false
    private val changeFlow = MutableStateFlow<Pair<String, Any?>>(Pair("", null))
    private var encryptor: KVEncryptor? = null

    override fun enableEncryption(encryptor: KVEncryptor?) {
        this.encryptor = encryptor
    }

    private val Any.encryptValue
        get() = run {
            if (encryptor != null && this is String) {
                encryptor!!.encrypt(this) ?: this
            } else {
                this
            }
        }

    private val Any.decryptValue
        get() = run {
            if (encryptor != null && this is String) {
                encryptor!!.decrypt(this) ?: this
            } else {
                this
            }
        }

    // 需要子类实现的抽象方法
    protected abstract suspend fun performPut(key: String, value: Any)
    protected abstract suspend fun <T : Any> performGet(
        key: String,
        defaultValue: T,
        clazz: KClass<T>
    ): T

    protected abstract suspend fun performRemove(key: String)
    protected abstract suspend fun performClear()
    protected abstract suspend fun performGetAll(): Map<String, Any>

    override suspend fun putString(key: String, value: String) = putValue(key, value)
    override suspend fun getString(key: String, defaultValue: String): String {
        return getValue<String>(key, defaultValue) as? String ?: defaultValue
    }

    override suspend fun putStringSet(key: String, value: Set<String>) = putValue(key, value)
    override suspend fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        return getValue<Set<String>>(key, defaultValue) as? Set<String> ?: defaultValue
    }

    override suspend fun putInt(key: String, value: Int) = putValue(key, value)
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return getValue<Int>(key, defaultValue) as? Int ?: defaultValue
    }

    override suspend fun putLong(key: String, value: Long) = putValue(key, value)
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return getValue<Long>(key, defaultValue) as? Long ?: defaultValue
    }

    override suspend fun putFloat(key: String, value: Float) = putValue(key, value)
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return getValue<Float>(key, defaultValue) as? Float ?: defaultValue
    }

    override suspend fun putBoolean(key: String, value: Boolean) = putValue(key, value)
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getValue<Boolean>(key, defaultValue) as? Boolean ?: defaultValue
    }

    override suspend fun <T> putObject(key: String, value: T, serializer: ObjectSerializer<T>) {
        val serialized = serializer.serialize(value) ?: value.toString()
        putString(key, serialized)
    }

    override suspend fun <T> getObject(key: String, serializer: ObjectSerializer<T>): T? {
        val serialized = getString(key)
        return if (serialized.isNotEmpty()) {
            serializer.deserialize(serialized)
        } else {
            null
        }
    }

    override suspend fun putAll(values: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            tryInitMigrations()
            values.forEach { (key, value) ->
                performPut(key, value.encryptValue)
                notifyChange(key, value)
            }
        }
    }

    override suspend fun getAll(keys: List<String>?): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            tryInitMigrations()
            if (keys.isNullOrEmpty()) {
                performGetAll()
            } else {
                performGetAll().filter { it.key in keys }
            }.mapValues {
                it.value.decryptValue
            }
        }
    }

    override suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            tryInitMigrations()
            performRemove(key)
            notifyChange(key, null)
        }
    }

    override suspend fun removeAll(keys: List<String>) {
        withContext(Dispatchers.IO) {
            keys.forEach { key ->
                performRemove(key)
                notifyChange(key, null)
            }
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            tryInitMigrations()
            performClear()
            notifyChange("", null) // 空key表示全部清除
        }
    }

    override fun <T> observe(key: String): Flow<T?> {
        return changeFlow
            .filter { it.first == key }
            .map {
                val value = it.second
                if (value != null) {
                    value as? T
                } else {
                    null
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun observeAll(): Flow<Map<String, Any?>> {
        return changeFlow
            .map {
                if (it.first.isEmpty()) emptyMap()
                else mapOf(it.first to it.second)
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun migrateFrom(other: KVStorage) {
        withContext(Dispatchers.IO) {
            putAll(other.getAll())
        }
    }

    override suspend fun migrateFrom(migrations: List<KVDataMigration>?) {
        withContext(Dispatchers.IO) {
            migrations?.forEach { migration ->
                if (migration.shouldMigrate(this@BaseKVStorage)) {
                    migration.migrate(this@BaseKVStorage)
                    migration.cleanUp()
                }
            }
        }
    }

    override suspend fun getAllKeys(): List<String> {
        // 默认实现，子类可以覆盖
        return emptyList()
    }

    private suspend fun putValue(key: String, value: Any) {
        withContext(Dispatchers.IO) {
            tryInitMigrations()
            performPut(key, value.encryptValue)
            notifyChange(key, value)
        }
    }

    private suspend inline fun <reified T : Any> getValue(key: String, defaultValue: T): T? {
        return withContext(Dispatchers.IO) {
            tryInitMigrations()
            performGet<T>(key, defaultValue, T::class).decryptValue as T
        }
    }

    protected fun notifyChange(key: String, value: Any?) {
        changeFlow.tryEmit(Pair(key, value))
    }

    private suspend fun tryInitMigrations() {
        if (isInit) return
        mutex.withLock(this) {
            if (!isInit) {
                migrateFrom(initMigrations)
                isInit = true
            }
        }
    }
}