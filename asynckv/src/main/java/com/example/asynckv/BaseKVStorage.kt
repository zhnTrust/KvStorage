import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

abstract class BaseKVStorage : KVStorage {
    private val changeFlow = MutableStateFlow<Pair<String, Any?>>(Pair("", null))
    private var encryptor: KVEncryptor? = null

    override fun enableEncryption(encryptor: KVEncryptor) {
        this.encryptor = encryptor
    }

    // 需要子类实现的抽象方法
    protected abstract suspend fun performPut(key: String, value: Any)
    protected abstract suspend fun performGet(key: String, clazz: KClass<*>): Any?
    protected abstract suspend fun performRemove(key: String)
    protected abstract suspend fun performClear()
    protected abstract suspend fun performGetAll(): Map<String, Any>

    override suspend fun putString(key: String, value: String) = putValue(key, value)
    override suspend fun getString(key: String, defaultValue: String): String {
        return getValue<String>(key) as? String ?: defaultValue
    }

    override suspend fun putInt(key: String, value: Int) = putValue(key, value)
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return getValue<Int>(key) as? Int ?: defaultValue
    }

    override suspend fun putLong(key: String, value: Long) = putValue(key, value)
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return getValue<Long>(key) as? Long ?: defaultValue
    }

    override suspend fun putFloat(key: String, value: Float) = putValue(key, value)
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return getValue<Float>(key) as? Float ?: defaultValue
    }

    override suspend fun putBoolean(key: String, value: Boolean) = putValue(key, value)
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getValue<Boolean>(key) as? Boolean ?: defaultValue
    }

    override suspend fun <T> putObject(key: String, value: T, serializer: ObjectSerializer<T>) {
        val serialized = serializer.serialize(value)
        putString(key, serialized)
    }

    override suspend fun <T> getObject(key: String, serializer: ObjectSerializer<T>): T? {
        val serialized = getString(key)
        return if (serialized.isNotEmpty()) {
            serializer.deserialize(serialized)
        } else {
            null
        }
    }

    override suspend fun putAll(values: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            values.forEach { (key, value) ->
                performPut(key, value)
                notifyChange(key, value)
            }
        }
    }

    override suspend fun getAll(keys: List<String>?): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            if (keys.isNullOrEmpty()) {
                performGetAll()
            } else {
                performGetAll().filter { it.key in keys }
            }
        }
    }

    override suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            performRemove(key)
            notifyChange(key, null)
        }
    }

    override suspend fun removeAll(keys: List<String>) {
        withContext(Dispatchers.IO) {
            keys.forEach { key ->
                performRemove(key)
                notifyChange(key, null)
            }
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            performClear()
            notifyChange("", null) // 空key表示全部清除
        }
    }

    override fun observe(key: String): Flow<Any?> {
        return changeFlow
            .filter { it.first == key || it.first.isEmpty() }
            .map { if (it.first.isEmpty()) null else it.second }
            .flowOn(Dispatchers.Default)
    }

    override fun observeAll(): Flow<Map<String, Any?>> {
        return changeFlow
            .map {
                if (it.first.isEmpty()) emptyMap()
                else mapOf(it.first to it.second)
            }
            .flowOn(Dispatchers.Default)
    }

    override suspend fun migrateFrom(other: KVStorage) {
        withContext(Dispatchers.IO) {
            val allValues = other.getAll(other.getAllKeys())
            putAll(allValues)
        }
    }

    override suspend fun migrateFrom(migrations: List<KVDataMigration>) {
        withContext(Dispatchers.IO) {
            val cleanUps = mutableListOf<suspend () -> Unit>()
            migrations.forEach { migration ->
                if (migration.shouldMigrate(this@BaseKVStorage)) {
                    cleanUps.add { migration.cleanUp() }
                    migration.migrate(this@BaseKVStorage)
                }
            }
            var cleanUpFailure: Throwable? = null

            cleanUps.forEach { cleanUp ->
                try {
                    cleanUp()
                } catch (exception: Throwable) {
                    if (cleanUpFailure == null) {
                        cleanUpFailure = exception
                    } else {
                        cleanUpFailure.addSuppressed(exception)
                    }
                }
            }
        }
    }

    override suspend fun getAllKeys(): List<String> {
        // 默认实现，子类可以覆盖
        return emptyList()
    }

    private suspend fun putValue(key: String, value: Any) {
        withContext(Dispatchers.IO) {
            val crypto = encryptor
            val processedValue = if (crypto != null && value is String) {
                crypto.encrypt(value)
            } else {
                value
            }
            performPut(key, processedValue)
            notifyChange(key, value)
        }
    }

    private suspend inline fun <reified T> getValue(key: String): Any? {
        return withContext(Dispatchers.IO) {
            val value = performGet(key, T::class) ?: return@withContext null
            val crypto = encryptor
            if (crypto != null && value is String) {
                crypto.decrypt(value)
            } else {
                value
            }
        }
    }

    protected fun notifyChange(key: String, value: Any?) {
        changeFlow.tryEmit(Pair(key, value))
    }
}