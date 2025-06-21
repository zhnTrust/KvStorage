package com.example.asynckv

import KVEncryptor
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesKVEncryptor(
    private val key: ByteArray,
    private val iv: ByteArray
) : KVEncryptor {
    private val transformation = "AES/CBC/PKCS5Padding"
    
    override fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(data)
    }
    
    override fun decrypt(encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(encrypted)
    }
}