package com.heaton.funnyvote.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVoteClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    val scope = rememberCoroutineScope()
    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.handleIntent(ProfileIntent.SignInWithGoogle(idToken))
            } else {
                scope.launch { snackbarHostState.showSnackbar("無法取得 Google 授權憑證") }
            }
        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar("Google 登入取消或失敗：${e.localizedMessage ?: "未完成登入"}") }
        }
    }

    val onGoogleSignInClick = {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken("444957524197-l1en4gq9quhhtu8hee8plcnss5kd146h.apps.googleusercontent.com")
            .requestEmail()
            .build()
        val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.handleIntent(ProfileIntent.UploadAvatar(uri))
        }
    }

    val onAvatarClick = {
        photoPickerLauncher.launch(
            androidx.activity.result.PickVisualMediaRequest(
                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onNavigateBack = onNavigateBack,
        onVoteClick = onVoteClick,
        onGoogleSignInClick = onGoogleSignInClick,
        onAvatarClick = onAvatarClick,
        snackbarHostState = snackbarHostState
    )
}
