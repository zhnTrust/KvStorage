package com.zhn.asynckv.crypto

/**
 * Created by zhn on 2025/6/24.
 *
 * 加解密接口
 */
interface KVEncryptor {
    fun encrypt(data: String): String?
    fun decrypt(encrypted: String): String?
}