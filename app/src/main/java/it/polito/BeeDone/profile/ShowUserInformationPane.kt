package it.polito.BeeDone.profile

import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.activeAnimation
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.utils.CreateKPI
import it.polito.BeeDone.utils.CreateLastRowText
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.CreateRowText
import it.polito.BeeDone.utils.ShowCommonTeams
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ShowUserMenu(
    loginPane: () -> Unit,
    removeData: (SharedPreferences) -> Unit,
    sharedPreferences: SharedPreferences,
    clearUserInformation: () -> Unit
) {
    var menuVisible by remember { mutableStateOf(false) }
    var showPopUp by remember { mutableStateOf(false) }
    var switchValue by remember { mutableStateOf(activeAnimation) }

    Box {
        IconButton(onClick = { menuVisible = !menuVisible }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "settings",
                modifier = Modifier.size(30.dp)
            )
        }

        DropdownMenu(
            expanded = menuVisible,
            properties = PopupProperties(focusable = true),
            onDismissRequest = { menuVisible = false },
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(
                        text = "Settings",
                        color = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp, 1.dp)
                            .width(180.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Left
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Animations",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // Adds a checked switch that depends on switchChecked and
                    // onCheckedChange which updates switchChecked
                    Switch(
                        checked = switchValue,
                        onCheckedChange = {
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putBoolean("animation", it)
                            editor.apply()
                            switchValue = it
                            activeAnimation = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {

                    Text(text = "Logout",  modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = {
                        menuVisible = false
                        showPopUp = true
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Exit"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if(showPopUp) {
        Column {
            val dialogWidth = 420.dp / (1.3F)
            val dialogHeight = 150.dp

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
                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = "Are you sure you want to logout?",
                            color = Color.Black,
                            modifier = Modifier
                                .weight(1f)
                                .padding(10.dp, 1.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Left
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FloatingActionButton(
                            onClick = {
                                clearUserInformation()
                                removeData(sharedPreferences)
                                loggedUser = User()
                                showPopUp = false
                                loginPane()
                            },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                .height(35.dp)
                                .width(120.dp)
                        ) {
                            Text(text = "Yes", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        FloatingActionButton(
                            onClick = {
                                showPopUp = false
                            },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                .height(35.dp)
                                .width(120.dp)

                        ) {
                            Text(text = "No", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P) // Denotes that the annotated element should only be called on the given API level or higher. Needed for the DatePicker and the Profile Image
@Composable
fun ShowUserInformationPane(
    selectedUser: User,
    photo: Uri?,
    firstName: String,
    lastName: String,
    nickname: String,
    mail: String,
    location: String,
    description: String,
    birthDate: String,
    status: String,
    userChatPane: (String) -> Unit,
    userChatListPane: () -> Unit,
    showTeamDetailsPane: (String) -> Unit,
    allTeams: MutableList<Team>,
    db: FirebaseFirestore
) {
    val coroutineScope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var commonTeams by remember { mutableStateOf<List<Team>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedUser) {
        coroutineScope.launch {
            val tempTasks = mutableListOf<Task>()
            val taskFetchJobs = selectedUser.userTasks.map { taskRef ->
                async {
                    try {
                        val doc = db.collection("Tasks").document(taskRef).get().await()
                        doc.toObject(Task::class.java)?.let { tempTasks.add(it) }
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error getting task data", e)
                    }
                }
            }
            taskFetchJobs.awaitAll()
            tasks = tempTasks

            if (selectedUser.userNickname != loggedUser.userNickname) {
                val tempCommonTeams = mutableListOf<Team>()
                loggedUser.userTeams.forEach { t ->
                    if (selectedUser.userTeams.contains(t)) {
                        allTeams.find { it.teamId == t }?.let { tempCommonTeams.add(it) }
                    }
                }
                commonTeams = tempCommonTeams
            }

            isLoading = false
        }
    }

    BoxWithConstraints {
        val maxH = this.maxHeight
        if (this.maxHeight > this.maxWidth) {
            // VERTICAL
            Box {
                Column(
                    modifier = if (selectedUser.userNickname != loggedUser.userNickname) {
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, bottom = 70.dp)
                    } else {
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState())
                            .padding(horizontal = 16.dp)
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Profile Picture
                    Row(
                        modifier = Modifier
                            .height(maxH / 3)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CreateImage(photo, "$firstName $lastName", 170)
                    }

                    Column(
                        modifier = Modifier
                            .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                            .padding(end = 4.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Full Name
                        CreateRowText(contentDescription = "Full Name", text = "$firstName $lastName")

                        // Nickname
                        CreateRowText(contentDescription = "Nickname", text = nickname)

                        // Mail
                        CreateRowText(contentDescription = "Mail", text = mail)

                        // Location
                        CreateRowText(contentDescription = "Location", text = location)

                        // Description
                        CreateRowText(contentDescription = "Description", text = description)

                        // Birth Date
                        CreateRowText(contentDescription = "Birth Date", text = birthDate)

                        // Status
                        CreateLastRowText(contentDescription = "Status", text = status)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedUser.userNickname != loggedUser.userNickname) {
                        ShowCommonTeams(commonTeams.toMutableStateList(), showTeamDetailsPane)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (tasks.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .padding(horizontal = 10.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            // KPI
                            CreateKPI(
                                tasks.filter { t -> t.taskStatus == "Completed" || t.taskStatus == "Expired Completed" }.size,
                                tasks.size,
                                tasks.filter { t -> t.taskStatus == "Expired Completed" }.size,
                                tasks.filter { t ->
                                    (t.taskStatus == "Pending" || t.taskStatus == "In Progress" || t.taskStatus == "Expired Not Completed") && LocalDate.parse(
                                        t.taskDeadline,
                                        DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                    ) < LocalDate.now()
                                }.size
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        Text(text = "The user has never received a task to do.")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedUser.userNickname != loggedUser.userNickname) {
                    FloatingActionButton(
                        onClick = { userChatPane(selectedUser.userNickname) },
                        shape = CircleShape,
                        containerColor = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .size(70.dp)
                            .align(Alignment.BottomEnd)
                            .border(2.dp, lightBlue, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MailOutline,
                            contentDescription = "Open chat",
                            Modifier.size(30.dp)
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = { userChatListPane() },
                        shape = CircleShape,
                        containerColor = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .size(70.dp)
                            .align(Alignment.BottomEnd)
                            .border(2.dp, lightBlue, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MailOutline,
                            contentDescription = "Open chat list",
                            Modifier.size(30.dp)
                        )
                    }
                }
            }
        } else {
            // HORIZONTAL
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Profile Picture
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CreateImage(photo, "$firstName $lastName", 170)
                    }

                    Column(
                        modifier = if (selectedUser.userNickname != loggedUser.userNickname) {
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .weight(2f)
                                .padding(bottom = 70.dp)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .weight(2f)
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(25.dp))) {
                            // Full Name
                            CreateRowText(contentDescription = "Full Name", text = "$firstName $lastName")

                            // Nickname
                            CreateRowText(contentDescription = "Nickname", text = nickname)

                            // Mail
                            CreateRowText(contentDescription = "Mail", text = mail)

                            // Location
                            CreateRowText(contentDescription = "Location", text = location)

                            // Description
                            CreateRowText(contentDescription = "Description", text = description)

                            // Birth Date
                            CreateRowText(contentDescription = "Birth Date", text = birthDate)

                            // Status
                            CreateLastRowText(contentDescription = "Status", text = status)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedUser.userNickname != loggedUser.userNickname) {
                            ShowCommonTeams(commonTeams.toMutableStateList(), showTeamDetailsPane)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (tasks.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                    .padding(horizontal = 10.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                // KPI
                                CreateKPI(
                                    tasks.filter { t -> t.taskStatus == "Completed" || t.taskStatus == "Expired Completed" }.size,
                                    tasks.size,
                                    tasks.filter { t -> t.taskStatus == "Expired Completed" }.size,
                                    tasks.filter { t ->
                                        (t.taskStatus == "Pending" || t.taskStatus == "In Progress" || t.taskStatus == "Expired Not Completed") && LocalDate.parse(
                                            t.taskDeadline,
                                            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                        ) < LocalDate.now()
                                    }.size
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            Text(text = "The user has never received a task to do.")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        if (selectedUser.userNickname != loggedUser.userNickname) {
            FloatingActionButton(
                onClick = { userChatPane(selectedUser.userNickname) },
                shape = CircleShape,
                containerColor = Color.White,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .size(70.dp)
                    .align(Alignment.BottomEnd)
                    .border(2.dp, lightBlue, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MailOutline,
                    contentDescription = "Open chat",
                    Modifier.size(30.dp)
                )
            }
        } else {
            FloatingActionButton(
                onClick = { userChatListPane() },
                shape = CircleShape,
                containerColor = Color.White,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .size(70.dp)
                    .align(Alignment.BottomEnd)
                    .border(2.dp, lightBlue, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MailOutline,
                    contentDescription = "Open chat list",
                    Modifier.size(30.dp)
                )
            }
        }
    }
}
