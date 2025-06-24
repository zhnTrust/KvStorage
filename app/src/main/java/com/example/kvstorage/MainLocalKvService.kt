package com.example.kvstorage

import com.zhn.asynckv.serialize.GsonSerializer
import com.zhn.asynckv.KvStorageTypeDelegation

/**
 * Created by zhn on 2025/6/24.
 */
object MainLocalKvService : KvStorageTypeDelegation(
    storage = KVStorageFactory.create(
        MyApp.app,
        KVStorageFactory.StorageType.MMKV
    )
) {
    val username = PrefKey("name", "")
    val age = PrefKey("age", 0)
    val interest = PrefKey("interest", setOf<String>())
    val user = PrefObjKey("user", GsonSerializer(User::class.java))
}