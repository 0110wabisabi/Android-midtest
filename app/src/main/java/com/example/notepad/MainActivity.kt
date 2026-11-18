package com.example.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.notepad.ui.theme.NotePadTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: NotePadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[NotePadViewModel::class.java]
        setContent {
            val bg by viewModel.appBg.collectAsState()
            NotePadTheme(bg = bg) {
                NotePadApp(viewModel = viewModel)
            }
        }
    }
}
