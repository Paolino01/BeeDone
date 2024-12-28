package it.polito.BeeDone.profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import it.polito.BeeDone.R
import it.polito.BeeDone.utils.lightBlue
import it.polito.BeeDone.utils.myShape

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun PasswordPane(
    passwordValue: String,
    passwordConfirmation: String,
    passwordError: String,
    setPassword: (String) -> Unit,
    setConfirmationPassword: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = passwordValue,
            onValueChange = setPassword,
            label = { Text("New password *") },
            isError = passwordError.isNotBlank(),
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
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordConfirmation,
            onValueChange = setConfirmationPassword,
            label = { Text("Confirm new password *") },
            isError = passwordError.isNotBlank(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            )
        )

        if (passwordError.isNotBlank()) {
            Text(passwordError, color = MaterialTheme.colorScheme.error)
        }
    }
}