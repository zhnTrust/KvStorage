package com.example.kvstorage

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.tencent.mmkv.MMKV
import com.zhn.asynckv.crypto.AesKVEncryptor
import com.zhn.asynckv.serialize.GsonSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val tvText
        get() = findViewById<TextView>(R.id.tv_hello) as TextView

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

        tvText.setOnClickListener {
            basicDemo()
        }
    }


    fun basicDemo() {
        // 创建存储实例
        val mmkvStorage = KVStorageFactory.create(this, KVStorageFactory.StorageType.MMKV)
        val spStorage =
            KVStorageFactory.create(this, KVStorageFactory.StorageType.SHARED_PREFERENCES)
        val dtStorage = KVStorageFactory.create(
            this@MainActivity,
            KVStorageFactory.StorageType.DATASTORE
        )

        // 启用加密
        val encryptor = AesKVEncryptor(
            passWord = "pwd123",
        )
        mmkvStorage.enableEncryption(encryptor)
        spStorage.enableEncryption(encryptor)
        dtStorage.enableEncryption(encryptor)

        val kvStorage = mmkvStorage

        //实现可获取到
        kvStorage.observe<String>("user").asLiveData().observe(this) {
            print("liveData User changed: $it")
        }


//        val scope=lifecycleScope
        val scope = CoroutineScope(Dispatchers.IO)
        // 基本使用
        scope.launch {
            // 监听变化
            launch {
                kvStorage.observe<String>("user").collect { user ->
                    print("flow User changed: $user")
                }
            }

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

            // 变更
            val user = User("John", "Doe", 30)
            kvStorage.putObject("user", user, GsonSerializer(User::class.java))

            // 批量操作
            kvStorage.putAll(
                mapOf(
                    "key1" to "value1",
                    "key2" to 42,
                    "key3" to true
                )
            )

            // 数据迁移
            spStorage.migrateFrom(listOf(kvStorage))
            mmkvStorage.migrateFrom(listOf(kvStorage))
            dtStorage.migrateFrom(listOf(kvStorage))

            print("spStorage: ${spStorage.getAll()}")
            print("mmkvStorage: ${mmkvStorage.getAll()}")
            print("dtStorage: ${dtStorage.getAll()}")
        }
    }

    /**
     * 代理使用
     */
    private fun delegateDemo() {

        MainLocalKvService.username.getvalue {
            print("getvalue: $it")
        }
        MainLocalKvService.username.getValueForMain(lifecycleScope) {
            print("getValueForMain: $it")
        }

        MainLocalKvService.username.asLiveData().observe(this) {
            print("liveData observe changed: $it")
        }
        lifecycleScope.launch {
            MainLocalKvService.username.asFlow()
                .map {
                    it + "_wenext"
                }.collect {
                    print("flow observe changed: $it")
                }
        }
        //赋值
        MainLocalKvService.username.setValue("john_doe")
        MainLocalKvService.age.setValue(20)
        MainLocalKvService.height.setValue(185)
        MainLocalKvService.score.setValue(99.9f)
        MainLocalKvService.isBoy.setValue(true)
        MainLocalKvService.interest.setValue(setOf("GYM", "Football"))

        val printName = suspend {
            val name = MainLocalKvService.username.getValue()
            print("suspend getName: $name")
        }
        lifecycleScope.launch {
            printName()
            MainLocalKvService.username.putValue("zhn")
            printName()
            delay(1500)
            MainLocalKvService.username.putValue("trust")
            printName()
        }
    }


    private fun print(msg: String) {
        Log.d(TAG, "thread:{${Thread.currentThread()}}, " + msg)
    }
}

data class User(var firstName: String, val secondName: String, val age: Int)