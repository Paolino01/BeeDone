package it.polito.BeeDone.profile.authentication

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import it.polito.BeeDone.userViewModel
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.DatePickerWithDialog
import it.polito.BeeDone.utils.ImagePicker
import it.polito.BeeDone.utils.PastOrPresentSelectableDates
import it.polito.BeeDone.utils.lightBlue
import it.polito.BeeDone.utils.rememberImeState
import kotlinx.coroutines.launch


//this is provvisory, i fix this on sunday
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SignUpPane(
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
    mailError: String,
    setMail: (String) -> Unit,
    openConfirmMailPane: (() -> Unit) -> Unit,
    locationValue: String,
    locationError: String,
    setLocation: (String) -> Unit,
    descriptionValue: String,
    setDescription: (String) -> Unit,
    birthDateValue: String,
    birthDateError: String,
    setBirthDate: (String) -> Unit,
    statusValue: String,
    setStatus: (String) -> Unit,
    changingPassword: (() -> Unit) -> Unit,
    confirmMailPane: () -> Unit,
    editPasswordPane: () -> Unit,
    loginPane: () -> Unit,
    setNicknamePane: () -> Unit,
    setOldProfileInformation: () -> Unit
) {
    val isImeVisible by rememberImeState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("899873757396-mijmj7leepaseegkmoeo9p9gnihotm8s.apps.googleusercontent.com")
        .build()

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

                FloatingActionButton(
                    onClick = {
                        val request: GetCredentialRequest = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        coroutineScope.launch {
                            try {
                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = context,
                                )
                                handleSignUp(result, setNicknamePane, setPhoto, setFirstName, setLastName, setMail, setOldProfileInformation)
                            } catch (e: GetCredentialException) {
                                Log.e(TAG, e.toString())
                            }
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.fillMaxWidth(0.5f).border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                ) {
                    Text(
                        text = "Sign Up with Google",
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                //Mail
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CreateTextFieldError(
                            mailValue,
                            mailError,
                            setMail,
                            "email@example.com *",
                            KeyboardType.Email,
                        )
                    }

                    Column(
                        Modifier.padding(start = 5.dp, bottom = 10.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { openConfirmMailPane(confirmMailPane) },
                            containerColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(45.dp)
                                .border(2.dp, lightBlue, CircleShape),
                        ) {
                            Icon(Icons.Default.Check, "Change")
                        }
                    }
                }

                //Birth Date
                DatePickerWithDialog(birthDateValue, birthDateError, setBirthDate, "Birth Date *", PastOrPresentSelectableDates)

                FloatingActionButton(
                    onClick = { changingPassword(editPasswordPane) },
                    shape = RoundedCornerShape(30.dp),
                    containerColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                ) {
                    Text(text = "Set Password")
                }
                if (userViewModel.getPasswordError()>0) {
                    Text(text = "you must set a password", color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Already have an account? "
                    )
                    Text (
                        text = "Sign In",
                        color = lightBlue,
                        modifier = Modifier.clickable {
                            loginPane()
                        }
                    )
                }

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

                    FloatingActionButton(
                        onClick = {
                            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            coroutineScope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        request = request,
                                        context = context,
                                    )
                                    handleSignUp(result, setNicknamePane, setPhoto, setFirstName, setLastName, setMail, setOldProfileInformation)
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
                            text = "Sign Up with Google",
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

                    //Mail
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CreateTextFieldError(
                                mailValue,
                                mailError,
                                setMail,
                                "email@example.com *",
                                KeyboardType.Email
                            )
                        }

                        Column(
                            Modifier.padding(start = 5.dp, bottom = 20.dp)
                        ) {
                            FloatingActionButton(
                                onClick = { openConfirmMailPane(confirmMailPane) },
                                containerColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(45.dp)
                                    .border(2.dp, lightBlue, CircleShape),
                            ) {
                                Icon(Icons.Default.Check, "Change", tint = Color.Black)
                            }
                        }
                    }

                    //Birth Date
                    DatePickerWithDialog(birthDateValue, birthDateError, setBirthDate, "Birth Date *", PastOrPresentSelectableDates)

                    FloatingActionButton(
                        onClick = { changingPassword(editPasswordPane) },
                        shape = RoundedCornerShape(30.dp),
                        containerColor = Color.White,

                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                    ) {
                        Text(text = "Set Password")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Already have an account? "
                        )
                        Text (
                            text = "Sign In",
                            color = lightBlue,
                            modifier = Modifier.clickable {
                                setPhoto(null)
                                setFirstName("")
                                setLastName("")
                                setNickname("")
                                setLocation("")
                                setDescription("")
                                setStatus("")
                                setMail("")
                                setBirthDate("")
                                loginPane()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

fun handleSignUp(
    result: GetCredentialResponse,
    setNicknamePane: () -> Unit,
    setPhoto: (Uri?) -> Unit,
    setFirstName: (String) -> Unit,
    setLastName: (String) -> Unit,
    setMail: (String) -> Unit,
    setOldProfileInformation: () -> Unit
) {
    // Handle the successfully returned credential.
    val credential = result.credential

    when (credential) {

        // Passkey credential
        is PublicKeyCredential -> {
            // Share responseJson such as a GetCredentialResponse on your server to
            // validate and authenticate
            val responseJson = credential.authenticationResponseJson
        }

        // Password credential
        is PasswordCredential -> {
            // Send ID and password to your server to validate and authenticate.
            val username = credential.id
            val password = credential.password
        }

        // GoogleIdToken credential
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    // Use googleIdTokenCredential and extract id to validate and
                    // authenticate on your server.
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)

                    setPhoto(googleIdTokenCredential.profilePictureUri)

                    if(googleIdTokenCredential.givenName != null) {
                        setFirstName(googleIdTokenCredential.givenName!!)
                    }
                    else {
                        setFirstName("")
                    }

                    if(googleIdTokenCredential.familyName != null) {
                        setLastName(googleIdTokenCredential.familyName!!)
                    }
                    else {
                        setLastName("")
                    }

                    setMail(googleIdTokenCredential.id)

                    setOldProfileInformation()
                    setNicknamePane()
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Received an invalid google id token response", e)
                }
            } else {
                // Catch any unrecognized custom credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }

        else -> {
            // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential")
        }
    }
}