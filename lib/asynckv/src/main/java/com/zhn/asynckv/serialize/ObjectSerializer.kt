package com.zhn.asynckv.serialize

/**
 * Created by zhn on 2025/6/24.
 *
 * 对象序列化接口
 */
interface ObjectSerializer<T> {
    fun serialize(obj: T): String
    fun deserialize(data: String): T
}