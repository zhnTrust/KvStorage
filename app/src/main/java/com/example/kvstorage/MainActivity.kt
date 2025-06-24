package com.example.kvstorage

import GsonSerializer
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.asynckv.AesKVEncryptor
import com.example.asynckv.KVStorageFactory
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
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
        val mmkvStorage = KVStorageFactory.create(this, KVStorageFactory.StorageType.MMKV)
        val spStorage =
            KVStorageFactory.create(this, KVStorageFactory.StorageType.SHARED_PREFERENCES)
        val dtStorage = KVStorageFactory.create(
            this@MainActivity,
            KVStorageFactory.StorageType.DATASTORE
        )
        val encryptor = AesKVEncryptor(
            passWord = "pwd123",
        )
        mmkvStorage.enableEncryption(encryptor)
        spStorage.enableEncryption(encryptor)
        dtStorage.enableEncryption(encryptor)

        val kvStorage = mmkvStorage

        //这个封装获取不到
//        MainLocalKvService.username.asLiveData().observe(this) {
//            print("delegate liveData User changed: $it")
//        }
        kvStorage.observe<String>("username").asLiveData().observe(this) {
            print("liveData User changed: $it")
        }
        // 启用加密

        // 基本使用
        lifecycleScope.launch {

            // 监听变化
//            launch {
//                kvStorage.observe("user").collect { user ->
//                    print("User changed: $user")
//                }
//            }

            // 存储数据
            kvStorage.putString("username", "john_doe")
            kvStorage.putInt("age", 20)
            kvStorage.putFloat("score", 99.9f)
            kvStorage.putLong("height", 185L)
            kvStorage.putBoolean("isBoy", true)
            kvStorage.putStringSet("interest", setOf("GYM", "Football"))

            // 读取数据
            val username = kvStorage.getString("username")
            val age = kvStorage.getInt("age")
            val score = kvStorage.getFloat("score")
            val height = kvStorage.getLong("height")
            val isBoy = kvStorage.getBoolean("isBoy")
            val interest = kvStorage.getStringSet("interest")
            print("username: $username")
            print("age: $age")
            print("score: $score")
            print("height: $height")
            print("isBoy: $isBoy")
            print("interest: $interest")

            // 对象存储
            val user = User("John", "Doe", 30)
            kvStorage.putObject("user", user, GsonSerializer(User::class.java))
            delay(1500)
            kvStorage.putString("username", "zhn")

            // 批量操作
            kvStorage.putAll(
                mapOf(
                    "key1" to "value1",
                    "key2" to 42,
                    "key3" to true
                )
            )

            // 数据迁移
            spStorage.migrateFrom(kvStorage)
            mmkvStorage.migrateFrom(kvStorage)
            dtStorage.migrateFrom(kvStorage)

            print("spStorage: ${spStorage.getAll()}")
            print("mmkvStorage: ${mmkvStorage.getAll()}")
            print("dtStorage: ${dtStorage.getAll()}")
//            print("${dtStorage.getFloat("username")}")
        }
    }

    private fun delegateDemo() {

        MainLocalKvService.username.getValueForMain(lifecycleScope) {
            print("getValueForMain: $it")
        }

        lifecycleScope.launch {
            MainLocalKvService.username.setValue("zhn")
            val name = MainLocalKvService.username.getValue()
            print("name: $name")
            delay(1500)
            MainLocalKvService.username.setValue("trust")
        }
    }

    private fun print(msg: String) {
        Log.d(TAG, msg)
    }
}

data class User(var firstName: String, val secondName: String, val age: Int)