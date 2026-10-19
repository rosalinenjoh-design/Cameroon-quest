package com.cameroonquest.app

import android.app.Application
import com.cameroonquest.app.di.AppContainer
import com.cameroonquest.app.di.DefaultAppContainer

/** Singleton scope: Android creates one Application per process, so one [AppContainer] serves the whole app. */
class CameroonQuestApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
