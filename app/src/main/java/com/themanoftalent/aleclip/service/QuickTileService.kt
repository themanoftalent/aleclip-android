package com.themanoftalent.aleclip.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.themanoftalent.aleclip.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val item = ClipboardService.ingestClipboard(applicationContext)
        if (item != null) {
            Toast.makeText(applicationContext, "Saved to AleClip!", Toast.LENGTH_SHORT).show()
        } else {
            // Launch main app
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivityAndCollapse(intent)
        }
    }
}
