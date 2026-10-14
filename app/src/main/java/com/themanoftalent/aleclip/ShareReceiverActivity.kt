package com.themanoftalent.aleclip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.themanoftalent.aleclip.model.ClipType
import com.themanoftalent.aleclip.service.StorageService

class ShareReceiverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                val type = if (text.startsWith("http://") || text.startsWith("https://")) {
                    ClipType.LINK
                } else if (text.startsWith("#") && (text.length == 7 || text.length == 9)) {
                    ClipType.COLOR
                } else {
                    ClipType.TEXT
                }

                StorageService.getInstance(this).addClip(type = type, content = text)
                Toast.makeText(this, "Saved to AleClip!", Toast.LENGTH_SHORT).show()
            }
        }

        finish()
    }
}
