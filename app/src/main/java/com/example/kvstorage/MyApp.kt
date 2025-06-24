package com.example.kvstorage

import android.app.Application
import android.content.Context

/**
 * Created by zhn on 2025/6/24.
 */
class MyApp: Application() {
    companion object{
        lateinit var app: MyApp
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app=this

    }
}