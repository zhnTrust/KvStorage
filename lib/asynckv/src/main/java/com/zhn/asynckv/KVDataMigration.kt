package com.zhn.asynckv

/**
 * Created by zhn on 2025/6/24.
 *
 * 数据迁移接口
 */
interface KVDataMigration {
    suspend fun shouldMigrate(asyncKv: KVStorage): Boolean
    suspend fun migrate(asyncKv: KVStorage)
    suspend fun cleanUp()
}