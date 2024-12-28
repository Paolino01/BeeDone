package it.polito.BeeDone.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.polito.BeeDone.utils.CreateTextFieldError

@Composable
fun ConfirmMailPane(
    confirmationCodeValue: String,
    confirmationCodeError: String,
    setConfirmationCode: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(40.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        CreateTextFieldError(
            confirmationCodeValue,
            confirmationCodeError,
            setConfirmationCode,
            "Confirmation code *",
            KeyboardType.Number
        )
    }
}