package it.polito.BeeDone.profile.authentication

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.DatePickerWithDialog
import it.polito.BeeDone.utils.ImagePicker
import it.polito.BeeDone.utils.PastOrPresentSelectableDates
import it.polito.BeeDone.utils.rememberImeState

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SetProfilePane(
    photoValue: Uri?,
    setPhoto: (Uri?) -> Unit,
    firstNameValue: String,
    firstNameError: String,
    setFirstName: (String) -> Unit,
    lastNameValue: String,
    lastNameError: String,
    setLastName: (String) -> Unit,
    nicknameValue: String,
    nicknameError: String,
    setNickname: (String) -> Unit,
    mailValue: String,
    locationValue: String,
    locationError: String,
    setLocation: (String) -> Unit,
    descriptionValue: String,
    setDescription: (String) -> Unit,
    birthDateValue: String,
    birthDateError: String,
    setBirthDate: (String) -> Unit,
    statusValue: String,
    setStatus: (String) -> Unit
) {
    val isImeVisible by rememberImeState()

    BoxWithConstraints {
        val maxH = this.maxHeight
        if (this.maxHeight > this.maxWidth) {
            //VERTICAL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                if (isImeVisible) {
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(40.dp, 3.dp, 40.dp, 220.dp)
                } else {
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(start = 40.dp, top = 3.dp, end = 40.dp, bottom = 3.dp)
                        .imePadding()
                }
            ) {
                //Profile Picture
                Row(
                    modifier = Modifier
                        .height(maxH / 3)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ImagePicker("$firstNameValue $lastNameValue", photoValue, setPhoto)
                }

                //Mail
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CreateTextFieldNoError(
                            modifier = Modifier.fillMaxWidth(),
                            value = mailValue,
                            label = "email@example.com *",
                            readOnly = true
                        )
                    }
                }

                //First Name
                CreateTextFieldError(
                    firstNameValue,
                    firstNameError,
                    setFirstName,
                    "First Name *",
                    KeyboardType.Text
                )

                //Last Name
                CreateTextFieldError(
                    lastNameValue,
                    lastNameError,
                    setLastName,
                    "Last name *",
                    KeyboardType.Text
                )

                //Nickname
                CreateTextFieldError(
                    nicknameValue,
                    nicknameError,
                    setNickname,
                    "Nickname *",
                    KeyboardType.Text
                )

                //Location
                CreateTextFieldError(
                    locationValue,
                    locationError,
                    setLocation,
                    "Location *",
                    KeyboardType.Text
                )

                //Description
                CreateTextFieldNoError(
                    value = descriptionValue,
                    setValue = setDescription,
                    label = "Description",
                    keyboardType = KeyboardType.Text
                )

                //Status
                CreateTextFieldNoError(
                    value = statusValue,
                    setValue = setStatus,
                    label = "Status",
                    keyboardType = KeyboardType.Text
                )

                //Birth Date
                DatePickerWithDialog(birthDateValue, birthDateError, setBirthDate, "Birth Date *", PastOrPresentSelectableDates)

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            //HORIZONTAL
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                //Profile Picture
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ImagePicker( "$firstNameValue $lastNameValue", photoValue, setPhoto)
                }

                Column(
                    modifier = Modifier
                        .weight(2f, false)
                        .padding(50.dp, 0.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Spacer(modifier = Modifier.height(16.dp))

                    //Mail
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CreateTextFieldNoError(
                                modifier = Modifier.fillMaxWidth(),
                                value = mailValue,
                                label = "email@example.com *",
                                readOnly = true
                            )
                        }
                    }

                    //First Name
                    CreateTextFieldError(
                        firstNameValue,
                        firstNameError,
                        setFirstName,
                        "First Name *",
                        KeyboardType.Text
                    )

                    //Last Name
                    CreateTextFieldError(
                        lastNameValue,
                        lastNameError,
                        setLastName,
                        "Last name *",
                        KeyboardType.Text
                    )

                    //Nickname
                    CreateTextFieldError(
                        nicknameValue,
                        nicknameError,
                        setNickname,
                        "Nickname *",
                        KeyboardType.Text
                    )

                    //Location
                    CreateTextFieldError(
                        locationValue,
                        locationError,
                        setLocation,
                        "Location *",
                        KeyboardType.Text
                    )

                    //Description
                    CreateTextFieldNoError(
                        value = descriptionValue,
                        setValue = setDescription,
                        label = "Description",
                        keyboardType = KeyboardType.Text
                    )

                    //Status
                    CreateTextFieldNoError(
                        value = statusValue,
                        setValue = setStatus,
                        label = "Status",
                        keyboardType = KeyboardType.Text
                    )

                    //Birth Date
                    DatePickerWithDialog(birthDateValue, birthDateError, setBirthDate, "Birth Date *", PastOrPresentSelectableDates)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}