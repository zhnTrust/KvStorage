package com.zhn.asynckv

import com.zhn.asynckv.crypto.KVEncryptor
import com.zhn.asynckv.KVDataMigration
import com.zhn.asynckv.serialize.ObjectSerializer
import kotlinx.coroutines.flow.Flow

/**
 * Created by zhn on 2025/6/18.
 *
 * KV存储核心接口
 */
interface KVStorage {
    // 基础操作
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String, defaultValue: String = ""): String
    suspend fun putStringSet(key: String, value: Set<String>)
    suspend fun getStringSet(key: String, defaultValue: Set<String> = setOf()): Set<String>
    suspend fun putInt(key: String, value: Int)
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    suspend fun putLong(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long
    suspend fun putFloat(key: String, value: Float)
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    // 对象操作
    suspend fun <T> putObject(key: String, value: T, serializer: ObjectSerializer<T>)
    suspend fun <T> getObject(key: String, serializer: ObjectSerializer<T>): T?

    // 批量操作
    suspend fun getAllKeys(): List<String>
    suspend fun putAll(values: Map<String, Any>)
    suspend fun getAll(keys: List<String>? = null): Map<String, Any>

    // 删除操作
    suspend fun remove(key: String)
    suspend fun removeAll(keys: List<String>)
    suspend fun clear()

    // 监听
    fun <T> observe(key: String): Flow<T?>
    fun observeAll(): Flow<Map<String, Any?>>

    // 迁移
    suspend fun migrateFrom(others: KVStorage)
    suspend fun migrateFrom(kvDataMigration: List<KVDataMigration>? = null)

    // 加密
    fun enableEncryption(encryptor: KVEncryptor?)
}





