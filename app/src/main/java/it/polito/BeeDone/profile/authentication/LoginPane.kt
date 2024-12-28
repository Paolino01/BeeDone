package it.polito.BeeDone.profile.authentication

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import it.polito.BeeDone.R
import it.polito.BeeDone.utils.SetImage
import it.polito.BeeDone.utils.lightBlue
import it.polito.BeeDone.utils.myShape
import it.polito.BeeDone.utils.rememberImeState
import kotlinx.coroutines.launch

val signInWIthGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder("899873757396-mijmj7leepaseegkmoeo9p9gnihotm8s.apps.googleusercontent.com")
    .setNonce(null)
    .build()

val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(true)
    .setServerClientId("899873757396-mijmj7leepaseegkmoeo9p9gnihotm8s.apps.googleusercontent.com")
    .setAutoSelectEnabled(true)
    .setNonce(null)
    .build()

@Composable
fun LoginPane(
    userLoginValue: String,
    userLoginError: String,
    setUserLogin: (String) -> Unit,

    passwordValue: String,
    passwordError: String,
    setPassword: (String) -> Unit,

    authError: String,

    validateLogin: (SharedPreferences, () -> Unit) -> Unit,
    validateLoginWithGoogle: (SharedPreferences, () -> Unit) -> Unit,
    sharedPreferences: SharedPreferences,

    homePane: () -> Unit,
    signUpPane: () -> Unit
) {
    val isImeVisible by rememberImeState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    Column(
        modifier = if (isImeVisible) {
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(40.dp, 3.dp, 40.dp, 290.dp)
        } else {
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(start = 40.dp, top = 3.dp, end = 40.dp, bottom = 3.dp)
                .imePadding()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val logoUri = Uri.parse("android.resource://it.polito.BeeDone/drawable/beedonelogo")
        Spacer(modifier = Modifier.height(16.dp))
        SetImage(imageUri = logoUri, size = 170)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userLoginValue,
            onValueChange = setUserLogin,
            label = { Text("Nickname *") },
            placeholder = { Text("")},
            isError = userLoginError.isNotBlank() || authError.isNotBlank(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            shape = myShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (userLoginError.isNotBlank()) {
            Text(userLoginError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(10.dp))

        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = passwordValue,
            onValueChange = setPassword,
            label = { Text("Password *") },
            isError = passwordError.isNotBlank() || authError.isNotBlank(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),

            /* by default the password is hidden, the user can decide
              whether to show or hide it by clicking on the eye icon
            */
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        painter = if (passwordVisible) painterResource(R.drawable.ic_eye_open)
                        else painterResource(R.drawable.ic_eye_closed),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            shape = myShape,
            //when you click on the OutlinedTextField
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError.isNotBlank()) {
            Text(passwordError, color = MaterialTheme.colorScheme.error)
        }
        if (authError.isNotBlank()) {
            Text(authError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = {
                validateLogin(sharedPreferences, homePane)
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.border(2.dp, lightBlue, RoundedCornerShape(30.dp))
        ) {
            Text(
                text = "Login",
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FloatingActionButton(
            onClick = {
                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWIthGoogleOption)
                    .build()

                coroutineScope.launch {
                    try {
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context,
                        )
                        handleSignIn(result, setUserLogin, validateLoginWithGoogle, sharedPreferences, homePane)
                    } catch (e: GetCredentialException) {
                        Log.e(TAG, e.toString())
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.border(2.dp, lightBlue, RoundedCornerShape(30.dp))
        ) {
            Text(
                text = "Login with Google",
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Don't have an account? "
            )
            Text (
                text = "Sign Up",
                color = lightBlue,
                modifier = Modifier.clickable {
                    setUserLogin("")
                    setPassword("")
                    signUpPane()
                }
            )
        }
    }
}

fun handleSignIn(result: GetCredentialResponse, setUserLogin: (String) -> Unit, validateLoginWithGoogle: (SharedPreferences, () -> Unit) -> Unit, sharedPreferences: SharedPreferences, homePane: () -> Unit) {
    // Handle the successfully returned credential.
    val credential = result.credential

    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    // Use googleIdTokenCredential and extract id to validate and
                    // authenticate on your server.
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)

                    setUserLogin(googleIdTokenCredential.id)
                    validateLoginWithGoogle(sharedPreferences, homePane)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Received an invalid google id token response", e)
                }
            }
            else {
                //Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }

        else -> {
            // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential")
        }
    }
}