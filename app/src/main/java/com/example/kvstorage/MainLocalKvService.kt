package com.example.kvstorage

import com.zhn.asynckv.serialize.GsonSerializer
import com.zhn.asynckv.KvStorageDelegation
import com.zhn.asynckv.crypto.AesKVEncryptor

/**
 * Created by zhn on 2025/6/24.
 */
object MainLocalKvService : KvStorageDelegation(
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
    val username = Key("name", "")
    val age = Key("age", 0)
    val height = Key("height", 0L)
    val score = Key("score", 0f)
    val isBoy = Key("isBoy", false)
    val interest = Key("interest", setOf<String>())
    val user = ObjKey("user", GsonSerializer(User::class.java))
}