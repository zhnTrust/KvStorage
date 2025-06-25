package com.zhn.asynckv.serialize

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Created by zhn on 2025/6/24.
 *
 * Gson序列化实现
 */
class GsonSerializer<T>(private val clazz: Class<T>) : ObjectSerializer<T> {
    private val gson = Gson()

    override fun serialize(obj: T): String? {
        return gson.toJson(obj)
    }

    override fun deserialize(data: String): T? {
        return try {
            gson.fromJson(data, clazz)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }
}