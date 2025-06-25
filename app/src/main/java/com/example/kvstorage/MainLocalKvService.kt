package com.example.kvstorage

import com.zhn.asynckv.serialize.GsonSerializer
import com.zhn.asynckv.KvStorageTypeDelegation
import com.zhn.asynckv.crypto.AesKVEncryptor

/**
 * Created by zhn on 2025/6/24.
 */
object MainLocalKvService : KvStorageTypeDelegation(
    storage = KVStorageFactory.create(
        MyApp.app,
        KVStorageFactory.StorageType.MMKV
    ).also {
        it.enableEncryption(
            AesKVEncryptor(
                passWord = "pwd123",
            )
        )
    }
) {
    val username = PrefKey("name", "")
    val age = PrefKey("age", 0)
    val height = PrefKey("height", 0L)
    val score = PrefKey("score", 0f)
    val isBoy = PrefKey("isBoy", false)
    val interest = PrefKey("interest", setOf<String>())
    val user = PrefObjKey("user", GsonSerializer(User::class.java))
}