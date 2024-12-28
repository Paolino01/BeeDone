package it.polito.BeeDone.utils


import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.team.TeamMember
import it.polito.BeeDone.teamViewModel

var myShape = AbsoluteRoundedCornerShape(
    topLeft = 15.dp,
    topRight = 15.dp,
    bottomLeft = 15.dp,
    bottomRight = 15.dp
)
var lightBlue = Color(157, 189, 255)

/**
Creates the TextFields used when editing the profile information.
The values inserted with these TextFields may have error messages
 */
@Composable
fun CreateTextFieldError(
    value: String,
    error: String,
    setValue: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier.fillMaxWidth(),
    maxLines: Int = Int.MAX_VALUE,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = setValue,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = error.isNotBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        shape = myShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = lightBlue,
            focusedLabelColor = lightBlue,
            focusedTextColor = Color.DarkGray
        ),
        modifier = modifier,
        maxLines = maxLines,
    )

    CreateErrorText(error)
    Spacer(modifier = Modifier.height(16.dp))

}

/**
Creates the TextFields used when editing the profile informations.
 */
@Composable
fun CreateTextFieldNoError(
    modifier: Modifier = Modifier.fillMaxWidth(),
    value: String,
    setValue: (String) -> Unit = {},
    label: String,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = lightBlue,
        focusedLabelColor = lightBlue,
        focusedTextColor = Color.DarkGray
    ),
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    maxLines: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = setValue,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        shape = myShape,
        colors = colors,
        modifier = modifier,
        readOnly = readOnly,
        maxLines = maxLines
    )
    Spacer(modifier = Modifier.height(16.dp))
}

/**
Creates Error TextFields
 */
@Composable
fun CreateErrorText(error: String) {

    if (error.isNotBlank()) {
        Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Right)
    }
}

@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }
    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {

            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true

            imeState.value = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return imeState
}


/*prende tutti gli user del team selezionato, verifica chi è statoa ssegnato al task e chi no;
in base a  questa verifica fa add o delete dell'utente al click;
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateDropdownProfiles(
    teamValue: Team, //team di riferimento
    taskUsersValue: MutableList<User>, //utenti assegnati al task
    setTaskUsers: (User) -> Unit, //per aggiornare utenti assegnati al task
    deleteTaskUsers: (User) -> Unit,
    db: FirebaseFirestore
) {
    var isExpanded by remember {            //Used to decide wether the DropDown is expanded or not
        mutableStateOf(false)
    }

    var showPopUp by remember { mutableStateOf(false) }
    var clickedUser by remember { mutableStateOf<User?>(null) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = userListToString(taskUsersValue.map(User::userNickname)), //addSpacesToSentence(value!!.profileNickname),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Profile") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            shape = myShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .background(Color.White)
                .padding(10.dp)
        ) {
            for (teamMemberRef in teamValue.teamMembers) {
                var loading by remember { mutableStateOf(true) }

                var user by remember {
                    mutableStateOf(User())
                }
                var userRef by remember { mutableStateOf(TeamMember()) }

                LaunchedEffect(teamMemberRef) {
                    db.collection("TeamMembers").document(teamMemberRef).get().addOnSuccessListener { teamMemberDoc ->
                        userRef = teamMemberDoc.toObject(TeamMember::class.java)!!

                        db.collection("Users").document(userRef.user).get().addOnSuccessListener { userDoc ->
                            user = userDoc.toObject(User::class.java)!!
                            loading = false
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error getting documents", e)
                            loading = false
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error getting documents", e)
                        loading = false
                    }
                }

                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (loggedUser != user) user.userNickname else "@You",
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

                        if (taskUsersValue.contains(user)) {
                            IconButton(onClick = {
                                showPopUp = !showPopUp
                                clickedUser = user
                            }
                            ) {
                                Icon(Icons.Outlined.Delete, "Delete")
                            }
                        } else {
                            IconButton(onClick = {
                                showPopUp = !showPopUp
                                clickedUser = user
                            }
                            ) {
                                Icon(Icons.Outlined.AddCircle, "Delete")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    /**
     * Shows a pop-up to confirm whether to remove an user from a task or not
     */
    if (showPopUp) {
        Column {
            val dialogWidth = 400.dp / (1.3F)
            val dialogHeight = 450.dp / 2

            if (showPopUp) {
                Dialog(onDismissRequest = {
                    showPopUp = false
                }) {
                    Card(
                        Modifier
                            .size(dialogWidth, dialogHeight),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (taskUsersValue.contains(clickedUser)) {
                                Button(
                                    onClick = {
                                        deleteTaskUsers(clickedUser!!)
                                        showPopUp = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .border(2.dp, Color.Red, RoundedCornerShape(20.dp))
                                        .height(50.dp)
                                        .width(250.dp)
                                ) {
                                    Text(
                                        text = "Remove ${if (loggedUser != clickedUser) clickedUser!!.userNickname else "@You"} from task",
                                        color = Color.Black
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        setTaskUsers(clickedUser!!)
                                        showPopUp = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                        .height(50.dp)
                                        .width(250.dp)
                                ) {
                                    Text(
                                        text = "Add ${if (loggedUser != clickedUser) clickedUser!!.userNickname else "@You"} to task",
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { showPopUp = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                    .height(50.dp)
                                    .width(250.dp)
                            ) {
                                Text(text = "Cancel", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Converts the list of users to a string that will be shown on screen
 */
fun userListToString(taskUsersValue: List<String>): String {
    var res = ""
    var first = 1

    taskUsersValue.forEach {
        if (first == 1) {
            res = it
            first = 0
        } else {
            res = "$res, $it"
        }
    }

    return res
}


/**
 * Manages the Repeat Dropdown Menu. Creates an ExposedDropDownMenu with its options
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateDropdownRepeat(
    value: String,
    setValue: (String) -> Unit
) {
    var isExpanded by remember {            //Used to decide wether the DropDown is expanded or not
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Repeat") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            shape = myShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("No Repeat") },
                onClick = {
                    setValue("No Repeat")
                    isExpanded = false
                },
            )

            DropdownMenuItem(
                text = { Text("Once per Week") },
                onClick = {
                    setValue("Once Per Week")
                    isExpanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("Once every two Weeks") },
                onClick = {
                    setValue("Once Every Two Weeks")
                    isExpanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("Once per Month") },
                onClick = {
                    setValue("Once Per Month")
                    isExpanded = false
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

/**
 * Converts a string like OncePerWeek into Once Per Week (adds spaces). Needed for the Repeat selection
 */
fun addSpacesToSentence(text: String): String {
    if (text.isBlank())
        return ""
    var newText = ""
    newText += text[0]
    for (i in 1..<text.length) {
        if (text[i].isUpperCase() && text[i - 1] != ' ')
            newText += " "
        newText += text[i]
    }
    return newText.trim()
}

/**
 * Manages the Teams Dropdown Menu. Creates an ExposedDropDownMenu with its options
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateDropdownTeams(
    value: Team?,
    error: String,
    setValue: (String, FirebaseFirestore) -> Unit,
    allTeams: MutableList<Team>,
    createTaskPaneFromTeam: (String) -> Unit,
    db: FirebaseFirestore
) {
    var isExpanded by remember {            //Used to decide wether the DropDown is expanded or not
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = addSpacesToSentence(value!!.teamName),
            isError = error.isNotBlank(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Team *") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            shape = myShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            for (t in allTeams) {
                DropdownMenuItem(
                    text = { Text(t.teamName) },
                    onClick = {
                        setValue(t.teamId, db)
                        //selectedTeam=t
                        isExpanded = false
                        createTaskPaneFromTeam(t.teamId)                //Call the same page, but with a teamId
                    },
                )
            }
        }
    }
    CreateErrorText(error = error)
    Spacer(modifier = Modifier.height(16.dp))
}


/**
 * Manages the Teams Dropdown Menu. Creates an ExposedDropDownMenu with its options
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateDropdownTeamsUser(
    value: User?,
    usersRef: List<String>,
    setValue: (User) -> Unit,
    db: FirebaseFirestore
) {
    var isExpanded by remember {            //Used to decide wether the DropDown is expanded or not
        mutableStateOf(false)
    }

    var users = mutableListOf<TeamMember>()
    for (u in usersRef){
        db.collection("TeamMembers").document(u).get()
            .addOnSuccessListener { d-> users.add(d.toObject(TeamMember::class.java)!!) }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = value?.userNickname ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Select a user to show his stats") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            shape = myShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = lightBlue,
                focusedLabelColor = lightBlue,
                focusedTextColor = Color.DarkGray
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            for (teamMemberRef in users) {
                var user = User()
                db.collection("TeamMembers").document(teamMemberRef.user).get().addOnSuccessListener { userDoc ->
                    user = userDoc.toObject(User::class.java)!!
                }

                DropdownMenuItem(
                    text = { Text(user.userNickname) },
                    onClick = {
                        teamViewModel.setTeUserSelected(user)
                        setValue(user)
                        //selectedUser=u.user
                        isExpanded = false
                    },
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}




