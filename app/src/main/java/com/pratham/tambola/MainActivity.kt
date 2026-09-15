package com.pratham.tambola

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pratham.tambola.presentation.TambolaApp
import com.pratham.tambola.presentation.TambolaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: TambolaViewModel = viewModel(factory = TambolaViewModel.factory(application as TambolaApplication))
            TambolaApp(model)
        }
    }
}
