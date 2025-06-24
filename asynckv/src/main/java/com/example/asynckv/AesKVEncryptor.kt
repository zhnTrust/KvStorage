package com.example.asynckv

import KVEncryptor
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AesKVEncryptor(
    private val passWord: String,
) : KVEncryptor {

    private val cipherAlgorith = "AES/ECB/PKCS5Padding"
    private val keyAlgorithm = "AES"
    private val keySize = 128

    private val secretKey by lazy {
        val str = toMakeKey(passWord, keySize, '0')
        SecretKeySpec(str.toByteArray(), keyAlgorithm)
    }

    override fun encrypt(data: String): String? {
        return runCatching {
            val result = Cipher.getInstance(cipherAlgorith).apply {
                init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey.encoded, keyAlgorithm))
            }.doFinal(data.toByteArray())
            Base64.encodeToString(result, Base64.NO_WRAP)
        }.getOrNull()
    }

    override fun decrypt(encrypted: String): String? {
        return runCatching {
            val base64 = Base64.decode(encrypted, Base64.NO_WRAP)
            val result = Cipher.getInstance(cipherAlgorith).apply {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey.encoded, keyAlgorithm))
            }.doFinal(base64)
            String(result)
        }.getOrNull()
    }

    /**
     * 如果 AES 的密钥小于 `length` 的长度，就对秘钥进行补位，保证秘钥安全。
     *
     * @param secretKey 密钥 key
     * @param byteLength    密钥应有的长度（位）
     * @param text      默认补的字符
     * @return 密钥
     */
    private fun toMakeKey(secretKey: String, byteLength: Int, text: Char): String {
        val length = byteLength / 4
        // 获取密钥长度
        var secretKey = secretKey
        val strLen = secretKey.length
        // 判断长度是否小于应有的长度
        if (strLen < length) {
            // 补全位数
            val builder = StringBuilder()
            // 将key添加至builder中
            builder.append(secretKey)
            // 遍历添加默认字符
            for (i in 0..<length - strLen) {
                builder.append(text)
            }
            // 赋值
            secretKey = builder.toString()
        }
        return secretKey
    }
}