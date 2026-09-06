package com.shn.music

import android.app.Application
import com.shn.music.core.common.di.SHNAppContainer

class SHNMusicApplication : Application() {

    lateinit var container: SHNAppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = SHNAppContainer(this)
    }
}
