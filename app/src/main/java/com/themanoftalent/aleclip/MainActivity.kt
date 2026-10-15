package com.themanoftalent.aleclip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.themanoftalent.aleclip.service.ClipboardService
import com.themanoftalent.aleclip.ui.screens.MainScreen
import com.themanoftalent.aleclip.ui.theme.AleClipTheme
import com.themanoftalent.aleclip.ui.viewmodel.ClipViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ClipViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AleClipTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Auto-capture clipboard when coming to foreground
        ClipboardService.ingestClipboard(this)
    }
}
