package com.heaton.funnyvote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.heaton.funnyvote.ui.navigation.FunnyVoteNavGraph
import com.heaton.funnyvote.ui.theme.FunnyVoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deepLinkVoteCode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent {
            FunnyVoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FunnyVoteNavGraph(initialVoteCode = deepLinkVoteCode)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val code = when {
            data.scheme == "funnyvote" && data.host == "poll" -> {
                data.lastPathSegment
            }
            data.path?.startsWith("/poll/") == true -> {
                data.lastPathSegment
            }
            data.getQueryParameter("code") != null -> {
                data.getQueryParameter("code")
            }
            else -> null
        }
        val isValid = code != null && code.matches(Regex("^[a-zA-Z0-9_-]{1,64}$"))
        if (isValid) {
            deepLinkVoteCode = code
        }
    }
}
