package com.themanoftalent.aleclip

import android.app.Application
import com.themanoftalent.aleclip.service.DriveSyncService
import com.themanoftalent.aleclip.service.StorageService

class AleClipApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize local on-device storage
        StorageService.getInstance(this)
        // Sync with AleClip for Mac/iPhone via Google Drive (if connected)
        DriveSyncService.init(this)
    }
}
