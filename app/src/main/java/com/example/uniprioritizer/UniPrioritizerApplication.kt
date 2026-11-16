package com.example.uniprioritizer

import android.app.Application

class UniPrioritizerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
