package com.example.kvstorage

import GsonSerializer
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.asynckv.KVStorageFactory
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MMKV.initialize(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tv_hello).setOnClickListener {
            demo()
        }
    }


    fun demo() {
        // 创建存储实例
        val kvStorage = KVStorageFactory.create(this, KVStorageFactory.StorageType.MMKV)

        // 启用加密
//        val encryptor = AesKVEncryptor(
//            passWord = "123".toByteArray(),
//        )
//        kvStorage.enableEncryption(encryptor)

        // 基本使用
        lifecycleScope.launch {
            // 存储数据
            kvStorage.putString("username", "john_doe")
            kvStorage.putInt("age", 30)
            kvStorage.putFloat("score", 99.9f)

            // 读取数据
            val username = kvStorage.getString("username")
            val age = kvStorage.getInt("age")
            val score = kvStorage.getFloat("score")
            print("username: $username")
            print("age: $age")
            print("score: $score")

            // 对象存储
            val user = User("John", "Doe", 30)
            kvStorage.putObject("user", user, GsonSerializer(User::class.java))

            // 监听变化
            launch {
                kvStorage.observe("user").collect { user ->
                    print("User changed: $user")
                }
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
            val spStorage = KVStorageFactory.create(
                this@MainActivity,
                KVStorageFactory.StorageType.SHARED_PREFERENCES
            )
            val dtStorage = KVStorageFactory.create(
                this@MainActivity,
                KVStorageFactory.StorageType.DATASTORE
            )
            spStorage.migrateFrom(kvStorage)
            dtStorage.migrateFrom(kvStorage)

            print("kvStorage: ${kvStorage.getAll(kvStorage.getAllKeys())}")
            print("newStorage: ${spStorage.getAll(spStorage.getAllKeys())}")
            print("dtStorage: ${dtStorage.getAll(dtStorage.getAllKeys())}")
        }
    }

    private fun print(msg: String) {
        Log.d(TAG, msg)
    }
}

data class User(val firstName: String, val secondName: String, val age: Int)