package com.example.asynckv

import KVEncryptor
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesKVEncryptor(
    private val passWord: ByteArray,
) : KVEncryptor {
    private val ivSpec by lazy {
        val bytes=ByteArray(16)
        SecureRandom().nextBytes(bytes)
        IvParameterSpec(bytes)
    }
    private val cipher = "AES/CBC/NoPadding"
    private val algorithm = "AES"
    private val keySize = 128

    private fun getKey(): SecretKey {
        return KeyGenerator.getInstance(algorithm).apply {
            init(keySize, SecureRandom(passWord))
        }.generateKey()
    }


    override fun encrypt(data: ByteArray): ByteArray {
        Security.getProviders()
        val cipher = Cipher.getInstance(cipher)
        val key = getKey().encoded
        val keySpec = SecretKeySpec(key, algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(data)
    }

    override fun decrypt(encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(cipher)
        val keySpec = SecretKeySpec(getKey().encoded, algorithm)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(encrypted)
    }
}