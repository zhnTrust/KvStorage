package com.example.kvstorage

import GsonSerializer
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.asynckv.AesKVEncryptor
import com.example.asynckv.KVStorageFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    fun demo() {
        // 创建存储实例
        val kvStorage = KVStorageFactory.create(this, KVStorageFactory.StorageType.MMKV)

        // 启用加密
        val encryptor = AesKVEncryptor(
            key = "16byteskey1234567".toByteArray(),
            iv = "16bytesiv12345678".toByteArray()
        )
        kvStorage.enableEncryption(encryptor)

        // 基本使用
        lifecycleScope.launch {
            // 存储数据
            kvStorage.putString("username", "john_doe")
            kvStorage.putInt("age", 30)

            // 读取数据
            val username = kvStorage.getString("username")
            val age = kvStorage.getInt("age")

            // 对象存储
            val user = User("John", "Doe", 30)
            kvStorage.putObject("user", user, GsonSerializer(User::class.java))

            // 监听变化
            kvStorage.observe("user").collect { user ->
                println("User changed: $user")
            }

            // 批量操作
            kvStorage.putAll(
                mapOf(
                    "key1" to "value1",
                    "key2" to 42,
                    "key3" to true
                )
            )

            // 数据迁移
            val oldStorage = KVStorageFactory.create(
                this@MainActivity,
                KVStorageFactory.StorageType.SHARED_PREFERENCES
            )
            kvStorage.migrateFrom(oldStorage)
        }
    }
}

data class User(val firstName: String, val secondName: String, val age: Int)