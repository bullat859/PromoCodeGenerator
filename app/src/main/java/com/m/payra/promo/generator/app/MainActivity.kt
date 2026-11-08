package com.m.payra.promo.generator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.m.payra.promo.generator.app.ui.theme.PromoCodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PromoCodeTheme {
                PromoCodeApp()
            }
        }
    }
}
