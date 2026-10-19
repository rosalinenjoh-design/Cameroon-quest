package com.cameroonquest.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cameroonquest.app.ui.navigation.QuestApp
import com.cameroonquest.app.ui.theme.CameroonQuestTheme

/** AppCompatActivity so AppCompat can switch the app between English and French on every Android version. */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Every screen has a dark header and a dark kente footer, so system bar icons stay light.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val container = (application as CameroonQuestApp).container
        setContent {
            CameroonQuestTheme {
                QuestApp(container)
            }
        }
    }
}
