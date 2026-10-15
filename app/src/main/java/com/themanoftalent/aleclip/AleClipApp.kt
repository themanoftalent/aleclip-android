package com.themanoftalent.aleclip

import android.app.Application
import com.themanoftalent.aleclip.service.StorageService

class AleClipApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize local on-device storage
        StorageService.getInstance(this)
    }
}
