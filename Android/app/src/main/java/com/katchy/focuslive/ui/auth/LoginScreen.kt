package com.katchy.focuslive.ui.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.katchy.focuslive.R
import com.katchy.focuslive.ui.theme.AntiPrimary
import com.katchy.focuslive.ui.theme.AntiTextPrimary
import com.katchy.focuslive.ui.theme.AntiTextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by viewModel.user.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(user) {
        if (user != null) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo or Brand
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_app_logo_vector),
                contentDescription = "Logo",
                tint = Color.Black,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Bienvenido a Brish",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AntiTextPrimary
            )
            
            Text(
                text = "Tu sistema operativo de vida.",
                style = MaterialTheme.typography.bodyLarge,
                color = AntiTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (loading) {
                CircularProgressIndicator(color = AntiPrimary)
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            signInWithGoogle(context, viewModel)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Continuar con Google", fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { viewModel.signInAnonymously() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Continuar como Invitado", 
                        color = AntiTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private suspend fun signInWithGoogle(context: Context, viewModel: AuthViewModel) {
    val webClientId = context.getString(R.string.default_web_client_id)
    try {
        val credentialManager = CredentialManager.create(context)
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        
        val credential = result.credential
        val firebaseCredential = when {
            credential is GoogleIdTokenCredential -> {
                GoogleAuthProvider.getCredential(credential.idToken, null)
            }
            credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                 val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                 GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            }
            else -> {
                Log.e("LoginScreen", "Unexpected credential type: ${credential::class.java.name}")
                return
            }
        }
        viewModel.signIn(firebaseCredential)
    } catch (e: Exception) {
        Log.e("LoginScreen", "Login Error", e)
        if (e is GetCredentialException) {
             Toast.makeText(context, "Error de credenciales: ${e.message} (ClientID: $webClientId)", Toast.LENGTH_LONG).show()
        } else {
             Toast.makeText(context, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
