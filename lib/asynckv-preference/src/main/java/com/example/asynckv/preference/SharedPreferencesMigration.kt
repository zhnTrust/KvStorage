package com.example.asynckv.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.zhn.asynckv.KVDataMigration
import com.zhn.asynckv.KVStorage

/**
 * Created by zhn on 2025/6/25.
 */
class SharedPreferencesMigration(private val preference: SharedPreferences) : KVDataMigration {
    constructor(context: Context, name: String) : this(
        context.getSharedPreferences(
            name,
            Context.MODE_PRIVATE
        )
    )

    override suspend fun shouldMigrate(asyncKv: KVStorage): Boolean {
        return !preference.all.isNullOrEmpty()
    }

    override suspend fun migrate(asyncKv: KVStorage) {
        asyncKv.putAll(preference.all.filter { it.value!=null }.mapValues { it.value!! })
    }

    override suspend fun cleanUp() {
        preference.edit(true) {
            clear()
        }
    }
}