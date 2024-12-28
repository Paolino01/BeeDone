package it.polito.BeeDone.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.task.history.Event
import it.polito.BeeDone.taskViewModel
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.team.TeamMember
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import com.google.android.gms.tasks.Tasks
import it.polito.BeeDone.team.TeamViewModel
import it.polito.BeeDone.team.UserInTeam
import it.polito.BeeDone.teamViewModel
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.function.Predicate


data class Slice(val value: Float, val color: Color, val text: String)

var timeValue by mutableStateOf("")
    private set

//variable for image description
var timeError by mutableStateOf("")
    private set

/**
Creates StackedBar chart that shows number of completed tasks/number of tasks not completed.
Used for the KPIs
 */
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun StackedBar(done: Int, total: Int) {
    val slices = listOf(
        Slice(value = done.toFloat(), color = Color(0XFF9EB25D), text = done.toString()),
        Slice(
            value = (total - done).toFloat(),
            color = Color(0XFFd3d3d3),
            text = (total - done).toString()
        )
    )

    Row(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
    ) {
        if (total > 0) {
            slices.forEach {

                if (it.value > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(it.value),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(it.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = it.text, color = Color.Black)
                        }
                    }
                }

            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "no elements", color = Color.White)
                }
            }

        }
    }
}

/**
Creates a Text element that displays information
 */
@Composable
fun CreateRowText(contentDescription: String, text: String) {
    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {

        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(20.dp, 0.dp)
        ) {
            Text(
                text = contentDescription,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                fontSize = 17.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Column(
            modifier = Modifier.weight(0.55f)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
}

/**
Same as function above, but there is no HorizontalDivider under the Text element
 */
@Composable
fun CreateLastRowText(contentDescription: String, text: String) {
    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {

        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(20.dp, 0.dp)
        ) {
            Text(
                text = contentDescription,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Column(
            modifier = Modifier.weight(0.55f)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
    Spacer(modifier = Modifier.height(6.dp))
}

/**
 * Creates a Text that displays a list of users. Each of these users can be clicked
 */
@Composable
fun CreateClickableUserText(
    contentDescription: String,
    taskUsers: MutableList<String>,
    showUserInformationPane: (String) -> Unit,
    db: FirebaseFirestore
) {
    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {

        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(20.dp, 0.dp)
        ) {
            Text(
                text = contentDescription,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp
            )
        }
        Column(
            modifier = Modifier.weight(0.55f)
        ) {
            var user by remember { mutableStateOf(User()) }
            for (u in taskUsers) {
                LaunchedEffect(u) {
                    db.collection("Users").document(u)
                        .get()
                        .addOnSuccessListener {
                                doc ->
                            user = doc.toObject(User::class.java)!!
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firestore", "Error getting team data", exception)
                        }
                }

                Row(
                    modifier = Modifier.clickable {
                        showUserInformationPane(u)
                    }
                ) {

                    CreateImage(
                        photo = if(user.userImage == null) null else user.userImage!!.toUri(),
                        name = "${user.userFirstName} ${user.userLastName}",
                        size = 25
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (loggedUser.userNickname != u) u else "@You",
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = lightBlue
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

    }
    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
}

@SuppressLint("UnrememberedMutableState", "StateFlowValueCalledInComposition")
@Composable
fun CreateClickableCreatorText(
    contentDescription: String,
    creator: String,
    showUserInformationPane: (String) -> Unit,
    db: FirebaseFirestore
) {
    Spacer(modifier = Modifier.height(6.dp))
    var userState by remember { mutableStateOf(User()) }

    // Launch a coroutine to fetch user data
    LaunchedEffect(creator) {
        db.collection("Users").document(creator)
            .get()
            .addOnCompleteListener { doc ->
                if (doc.isSuccessful) {
                    userState = doc.result.toObject(User::class.java)!!
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting User data", exception)
            }
    }

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {

        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(20.dp, 0.dp)
        ) {
            Text(
                text = contentDescription,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp
            )
        }
        Column(
            modifier = Modifier.weight(0.55f)
        ) {

            Row(
                modifier = Modifier.clickable {
                    showUserInformationPane(userState.userNickname)
                }
            ) {

                CreateImage(
                    photo =if(userState.userImage == null) null else userState.userImage!!.toUri(),
                    name = "${userState.userFirstName} ${userState.userLastName}",
                    size = 25
                )

                Spacer(modifier = Modifier.width(6.dp))

                Log.d("bbb", "user.value.userNickname: " + userState.userNickname)
                Text(
                    text = if (loggedUser.userNickname != creator) creator else "@You",
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = lightBlue
                )

                Spacer(modifier = Modifier.height(30.dp))
            }

        }

    }
    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
}


/**
 * Creates a Text that displays the team name. It can be clicked
 */
@Composable
fun CreateClickableTeamText(
    contentDescription: String,
    taskTeam: String,
    showTeamDetailsPane: (String) -> Unit,
    allTeams: SnapshotStateList<Team>
) {
    Spacer(modifier = Modifier.height(6.dp))

    var team = Team()
    if(allTeams.size > 0) {
        team = allTeams.find { it.teamId == taskTeam }!!
    }
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {

        Column(
            modifier = Modifier
                .weight(0.4f)
                .padding(20.dp, 0.dp)
        ) {
            Text(
                text = contentDescription,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp
            )
        }
        Column(
            modifier = Modifier.weight(0.55f)
        ) {
            Row(
                modifier = Modifier.clickable {
                    showTeamDetailsPane(taskTeam)
                }
            ) {

                CreateImage(
                    photo = team.teamImage.toUri(),
                    name = team.teamName,
                    size = 25
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = team.teamName,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = lightBlue
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

    }
    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
}


/**
Manages the KPIs. Shows a StackedBarChart and two boxes with information about the tasks completed by the user
 */
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CreateKPI(
    taskDone: Int,
    totalTask: Int,
    taskCompletedExpired: Int,
    taskExpiredNotCompleted: Int
) {
    Text(
        text = "Tasks completed/Tasks not completed",
        fontSize = 18.sp,
        fontFamily = FontFamily.SansSerif,
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))
    StackedBar(taskDone, totalTask)
    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .border(
                    2.dp, Color.LightGray, shape = RoundedCornerShape(15.dp)
                )
                .fillMaxHeight()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(2.dp, 4.dp)
        ) {
            Row {
                Text(
                    text = "Task completed after expiration",
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp, 1.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    minLines = 2
                )
            }
            Row {
                Text(
                    text = taskCompletedExpired.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp, 1.dp),
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.05f))
        Column(
            modifier = Modifier
                .weight(1f)
                .border(
                    2.dp, Color.LightGray, shape = RoundedCornerShape(15.dp)
                )
                .fillMaxHeight()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(2.dp, 4.dp)
        ) {
            Row {
                Text(
                    text = "Tasks expired and not yet completed",
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp, 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    minLines = 2
                )
            }
            Row {
                Text(
                    text = taskExpiredNotCompleted.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp, 0.dp),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
Manages the profile/group image. See Camera.kt for details
 */
@Composable
fun CreateImage(photo: Uri?, name: String, size: Int) {
    if (photo != null && photo.toString() != "null" && !Uri.EMPTY.equals(photo)) {
        SetImage(photo, size)
    } else {
        NoImage(name, size)
    }
}

enum class MyAnchors { L, C, R }

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskRow(showingTasks: MutableList<Task>,
            task: Task,
            showTaskDetailsPane: (String) -> Unit,
            editTaskPane: (String) -> Unit,
            db: FirebaseFirestore,
            allTeams: SnapshotStateList<Team>
) {
    var showPopUp by remember { mutableStateOf(false) }

    //Variables needed to enable the swipe of the TaskRow
    val density = LocalDensity.current
    val distancePx = with(LocalDensity.current) {
        20.dp.toPx()
    }
    val anchors = DraggableAnchors {
        MyAnchors.L at -distancePx
        MyAnchors.C at 0f
        MyAnchors.R at distancePx
    }
    val state = remember {
        AnchoredDraggableState(
            MyAnchors.C,                                                        //Initial position
            anchors,                                                            //Set of possible positions
            positionalThreshold = { distance -> distance*0.7f },                //Minimum amount of movement
            velocityThreshold = {with (density) { 20.dp.toPx() }},              //Speed in dp/s
            animationSpec = tween()
            //animationSpec = tween(1600, easing = FastOutSlowInEasing)
        )
    }

    // Remember a coroutine scope to launch coroutines
    val coroutineScope = rememberCoroutineScope()
    // Function to reset the state to the initial position
    val resetState: () -> Unit = {
        coroutineScope.launch {
            state.animateTo(MyAnchors.C)
        }
    }

    // Trigger popup display when swiped to the right
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == MyAnchors.R) {
            showPopUp = true
        }
    }

    if(state.currentValue == MyAnchors.L) {
        editTaskPane(task.taskId)
    }

    var userTaskDeleted by remember { mutableStateOf(false) }
    var teamTaskDeleted by remember { mutableStateOf(false) }
    var taskDeleted by remember { mutableStateOf(false) }

    if(showPopUp) {
        Column {
            val dialogWidth = 420.dp / (1.3F)
            val dialogHeight = 150.dp

            Dialog(onDismissRequest = {
                showPopUp = false
                resetState() // Reset the state when dialog is dismissed
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
                            text = "Are you sure to delete this task?",
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
                                showingTasks.remove(task)

                                val taskUsers: MutableList<User> = mutableListOf()

                                for (userRef in task.taskUsers) {
                                    db.collection("Users").document(userRef)
                                        .get()
                                        .addOnSuccessListener { doc->
                                            val userTaskList = doc.get("userTasks") as MutableList<String>
                                            userTaskList.remove(task.taskId)
                                            db.collection("Users").document(userRef).update("userTasks", userTaskList)
                                            userTaskDeleted = true
                                        }
                                        .addOnFailureListener {
                                                e ->
                                            Log.e("Firestore", "Error getting user data", e)
                                        }
                                }

                                for (u in taskUsers) {
                                    u.deleteTask(task.taskId)
                                }

                                db.collection("Team").document(task.taskTeam)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        val teamTaskList = doc.get("teamTasks") as MutableList<String>
                                        teamTaskList.remove(task.taskId)
                                        db.collection("Team").document(task.taskTeam).update("teamTasks", teamTaskList)
                                        teamTaskDeleted = true
                                    }
                                    .addOnFailureListener {
                                            e ->
                                        Log.e("Firestore", "Error getting team data", e)
                                    }
                                allTeams.find { it.teamId == task.taskTeam }!!.teamTasks.remove(task.taskId)

                                db.collection("Tasks").document(task.taskId).delete().addOnSuccessListener {
                                    taskDeleted = true
                                }

                                taskViewModel.allTasks.remove(task)

                                if(userTaskDeleted && teamTaskDeleted && taskDeleted) {
                                    showPopUp = false
                                    resetState() // Reset the state after deletion
                                }
                            },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .border(2.dp, Color.Red, RoundedCornerShape(20.dp))
                                .height(35.dp)
                                .width(120.dp)
                        ) {
                            Text(text = "Yes", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        FloatingActionButton(
                            onClick = {
                                showPopUp = false
                                resetState() // Reset the state when "No" is clicked
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

    val currentDate = LocalDate.now()
    Box {
        Icon(
            Icons.Filled.Delete,
            "Delete",
            Modifier
                .align(Alignment.CenterStart)
                .size(45.dp)
                .padding(8.dp)
        )
        Icon(
            Icons.Filled.Edit,
            "Edit",
            Modifier
                .align(Alignment.CenterEnd)
                .size(45.dp)
                .padding(8.dp)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(45.dp)
                .offset(
                    x = state
                        .requireOffset()
                        .roundToInt().dp, y = 0.dp
                )
                .background(Color(250, 250, 250))
                .anchoredDraggable(state, Orientation.Horizontal)
        ) {

            Row(
                Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
                    .padding(10.dp)
                    .clickable(onClick = {

                        showTaskDetailsPane(task.taskId)
                    })
            ) {
                Column(Modifier.weight(1f)) {
                    if(allTeams.size > 0) {
                        Text(
                            text = allTeams.find { it.teamId == task.taskTeam }!!.teamName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.size(10.dp))

                Column(Modifier.weight(3f)) {
                    Text(
                        text = task.taskTitle, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.size(10.dp))

                var mod = Modifier.width(IntrinsicSize.Max)
                val deadline =
                    LocalDate.parse(task.taskDeadline, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                if (deadline.isBefore(currentDate)) {
                    mod = Modifier
                        .width(IntrinsicSize.Max)
                        .background(Color(0xFFEB7856), RoundedCornerShape(10.dp))
                        .padding(5.dp, 1.dp)
                } else if (deadline.isBefore(currentDate.plusDays(5))) {
                    mod = Modifier
                        .width(IntrinsicSize.Max)
                        .background(Color(0xFFF5CA5E), RoundedCornerShape(10.dp))
                        .padding(5.dp, 1.dp)
                }

                Column(modifier = mod) {
                    Text(
                        text = task.taskDeadline, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.size(10.dp))
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun TeamBox(
    showTeamDetailsPane: (String) -> Unit,
    acceptInvitationPane: (String) -> Unit,
    team: Team,
    hasMessagesToRead: Boolean
) {
    val db = Firebase.firestore
    val userList = remember { mutableStateListOf<TeamMember>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(team.teamId) {
        try {
            val teamMembers = team.teamMembers.map { memberRef ->
                db.collection("TeamMembers").document(memberRef).get().await()
            }

            userList.clear()
            teamMembers.forEach { memberDoc ->
                val teamMember = memberDoc.toObject(TeamMember::class.java)
                if (teamMember != null) {
                    userList.add(teamMember)
                }
            }
            loading = false
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching team members", e)
            loading = false
        }
    }

    if (loading) {
        CircularProgressIndicator()
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .border(1.dp, lightBlue, RoundedCornerShape(15.dp))
                .clickable {
                    if (userList.any { it.user == loggedUser.userNickname && it.role == "Invited" }) {
                        acceptInvitationPane(team.teamId)
                    } else {
                        showTeamDetailsPane(team.teamId)
                    }
                }
        ) {
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp)
                ) {
                    CreateImage(team.teamImage.toUri(), team.teamName, 150)
                    Text(text = team.teamName, overflow = TextOverflow.Ellipsis)
                }

                if (hasMessagesToRead) {
                    CreateChatNotificationCircle(150, 5)
                }
            }
        }
    }
}


@Composable
fun EventRow(event: Event, showUserInformationPane: (String) -> Unit) {

    Text(text = event.date)

    val (expanded, setExpanded) = remember {
        mutableStateOf(false)
    }

    val b = if (expanded) "\u25b2" else "\u25bc"

    Column(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
            .padding(10.dp)
    ) {
        Row {
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.size(10.dp))

            Column(
                Modifier
                    .weight(1f)
                    .clickable(onClick = {
                        showUserInformationPane(event.user)
                    })
            ) {
                Text(
                    text = event.user,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = lightBlue,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(Modifier.size(10.dp))

            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    text = event.taskStatus.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.size(10.dp))

            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    text = event.taskDoneSubtasks + "/" + event.taskTotalSubtasks,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


        }

        if (event.taskChanges.isNotEmpty()) {
            Spacer(modifier = Modifier.size(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "List of changes:")

                OutlinedButton(
                    onClick = { setExpanded(!expanded) },
                    colors = ButtonColors(Color.White, lightBlue, Color.Black, Color.Black),
                    modifier = Modifier.size(70.dp, 30.dp)
                ) {
                    Text(text = b)
                }
            }
        }
        if (expanded) {

            for (e in event.taskChanges) {
                if (event.taskChanges.size > 1) {
                    Text(text = "- $e")
                } else {
                    Text(text = e)
                }
            }
        }
    }

    Spacer(Modifier.size(10.dp))
}
@Composable
fun ShowTeamMemberHours(
    teamMember: String,
    db: FirebaseFirestore,
    docId: String
) {
    var hours by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(teamMember) {
        db.collection("TeamMembers").document(docId).get()
            .addOnSuccessListener { doc ->
                if (doc != null) {
                    hours = doc.getLong("hours")?.toInt()
                } else {
                    Log.w("Firestore", "No matching documents found")
                }
                loading = false
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting documents", e)
                loading = false
            }
    }

    if (loading) {
        CircularProgressIndicator()
    } else {
        Text(
            text = hours?.toString() ?: "No hours data",
            maxLines = 1
        )
    }
}


fun removeMemberAndRelatedData(
    db: FirebaseFirestore,
    teamId: String,
    teamMembersId: String,
    userNickname: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit,
    clearTeamInformation: () -> Unit
) {
    Log.d("Firestore", "removeMemberAndRelatedData called with parameters:")
    Log.d("Firestore", "teamId: $teamId")
    Log.d("Firestore", "teamMembersId: $teamMembersId")
    Log.d("Firestore", "userNickname: $userNickname")

    val teamRef = db.collection("Team").document(teamId)
    val teamMemberRef = db.collection("TeamMembers").document(teamMembersId)
    val userRef = db.collection("Users").document(userNickname)

    db.runTransaction { transaction ->
        Log.d("Firestore", "Starting transaction")

        // Step 1: Remove teamMembersId from team.TeamId's TeamMembers list
        val teamSnapshot = transaction.get(teamRef)
        Log.d("Firestore", "Fetched team document: ${teamSnapshot.data}")

        if (!teamSnapshot.exists()) {
            Log.e("Firestore", "Team document does not exist")
            throw Exception("Team document does not exist")
        }

        val teamMembers = teamSnapshot.get("teamMembers") as? List<*>
            ?: throw Exception("TeamMembers field is not a list")
        val teamMembersList = teamMembers.filterIsInstance<String>().toMutableList()

        if (!teamMembersList.remove(teamMembersId)) {
            Log.w("Firestore", "teamMembersId: $teamMembersId not found in team members")
        }

        // Step 2: Find gastrinEliminate
        val userSnapshot = transaction.get(userRef)
        Log.d("Firestore", "Fetched user document: ${userSnapshot.data}")

        if (!userSnapshot.exists()) {
            Log.e("Firestore", "User document does not exist")
            throw Exception("User document does not exist")
        }

        val userTeams = userSnapshot.get("userTeams") as? List<*>
            ?: throw Exception("userTeams field is not a list")
        val userTeamsList = userTeams.filterIsInstance<String>().toMutableList()

        val userInTeamDocs = Tasks.await(
            db.collection("UserInTeam").whereEqualTo("first", teamId).get()

        )
        Log.d("Firestore", "Fetched UserInTeam documents: ${userInTeamDocs.documents.map { it.id }}")
        userInTeamDocs.documents.forEach { doc ->
            Log.d("Firestore", "UserInTeam document: ${doc.id}, data: ${doc.data}")

            if(loggedUser.userNickname==userNickname && loggedUser.userTeams.contains(doc.get("first"))){
                loggedUser.userTeams.remove(doc.get("first"))
            }


        }

        val gastrinEliminate = userInTeamDocs.documents.firstOrNull {
            userTeamsList.contains(it.id)
        }?.id ?: throw Exception("GastrinEliminate not found")
        Log.d("Firestore", "Found gastrinEliminate: $gastrinEliminate")

        // Step 3: Remove gastrinEliminate from userNickname's userTeams list
        if (!userTeamsList.remove(gastrinEliminate)) {
            Log.w("Firestore", "gastrinEliminate: $gastrinEliminate not found in user teams")
        }

        // Step 4: Execute all writes
        transaction.update(teamRef, "teamMembers", teamMembersList)
        transaction.delete(teamMemberRef)
        transaction.update(userRef, "userTeams", userTeamsList)
        val userInTeamRef = db.collection("UserInTeam").document(gastrinEliminate)
        transaction.delete(userInTeamRef)

        Log.d("Firestore", "Transaction steps completed")
        null // Return null to indicate success
    }.addOnSuccessListener {
        Log.d("Firestore", "Transaction successful")
        onSuccess()
        clearTeamInformation()
    }.addOnFailureListener { e ->
        Log.e("Firestore", "Transaction failed", e)
        onFailure(e)
    }
}

suspend fun LeaveTeamAndRelatedData(
    db: FirebaseFirestore,
    selectedTeam: Team,
    teamListPane: () -> Unit,
    idDelTeam : String
) {

    val teamMembersRefs = selectedTeam.teamMembers.toList()
    for (teamMemberRef in teamMembersRefs) {
        db.collection("TeamMembers").document(teamMemberRef).get()
            .addOnSuccessListener { teamMemberDoc ->

                val teamMember = teamMemberDoc.toObject(TeamMember::class.java)
                if (teamMember?.user == loggedUser.userNickname) {
                    removeMemberAndRelatedData(
                        db = db,
                        teamId = selectedTeam.teamId,
                        teamMembersId = teamMemberDoc.id,
                        userNickname = loggedUser.userNickname,
                        onSuccess = {
                            teamViewModel.allTeams.removeIf(Predicate { t -> t.teamId==selectedTeam.teamId })
                            teamViewModel.showingTeams.removeIf( Predicate { t->t.teamId==selectedTeam.teamId })
                            Log.d("Firestore" , "teamViewModel.allTeams : ${teamViewModel.allTeams.toString()}")
                            loggedUser.userTeams.remove(idDelTeam)
                            teamListPane() },
                        onFailure = {},
                        clearTeamInformation = {}

                    )
                }
            }
    }

}







@Composable
fun CreateTeamUsersSection(
    team: Team,
    userChatPane: (String) -> Unit,
    showUserInformationPane: (String) -> Unit,
    teamListPane: () -> Unit
) {
    val db = Firebase.firestore
    var teamMemberList = remember { mutableStateListOf<TeamMember>() }
    var docIdList = remember { mutableStateListOf<String>() }
    var loading by remember { mutableStateOf(true) }

    var memberRole by remember { mutableStateOf("") }

    LaunchedEffect(loggedUser, team) {
        memberRole = team.getRoleTeamUser(loggedUser)
    }

    LaunchedEffect(team.teamId) {
        loading = true
        db.collection("Team").document(team.teamId).get()
            .addOnSuccessListener { teamDoc ->
                if (teamDoc != null) {
                    Log.d("Firestore", "teamDoc data: ${teamDoc.data}")
                    val teamMembers = teamDoc.get("teamMembers")
                    Log.d("Firestore", "teamMembers: $teamMembers")
                    if (teamMembers is List<*>) {
                        val userRefs = teamMembers.filterIsInstance<String>()
                        Log.d("Firestore", "Filtered userRefs: $userRefs")

                        teamMemberList.clear()
                        docIdList.clear()

                        userRefs.forEach { userRef ->
                            db.collection("TeamMembers").document(userRef).get()
                                .addOnSuccessListener { teamMemberDoc ->
                                    val teamMember = teamMemberDoc.toObject(TeamMember::class.java)
                                    if (teamMember != null) {
                                        teamMemberList.add(teamMember)
                                        docIdList.add(teamMemberDoc.id)
                                        Log.d("Firestore", "Added team member: ${teamMember.user}")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firestore", "Error getting user document", exception)
                                }
                        }
                    } else {
                        Log.e("Firestore", "Error: teamMembers is not a List")
                    }
                } else {
                    Log.e("Firestore", "Error: teamDoc is null")
                }
                loading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting team document", exception)
                loading = false
            }

    }

    val (expandedIndex, setExpandedIndex) = remember { mutableIntStateOf(-2) }
    val (showRemovePopup, setShowRemovePopup) = remember { mutableStateOf(false) }
    val (showUpdateTimePopup, setShowUpdateTimePopup) = remember { mutableStateOf(false) }
    val (refreshPane, setRefreshPane) = remember { mutableStateOf(false) }
    var indexToRemove by remember { mutableIntStateOf(-1) }
    var indexToUpdateTime by remember { mutableIntStateOf(-1) }

    if (loading) {
        CircularProgressIndicator()
    } else {
        Column {
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Team Members",
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 17.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            teamMemberList.forEachIndexed { i, teamMember ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    ShowTeamMemberInfo(
                        teamMemberList[i].user,
                        addSpacesToSentence(teamMemberList[i].role),
                        i,
                        showUserInformationPane,
                        expandedIndex,
                        setExpandedIndex,
                        db,
                        docIdList[i]
                    )
                    if (expandedIndex == i) {
                        Row(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp, top = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Weekly hours for this team: ",
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            Column(
                                modifier = Modifier.weight(0.55f)
                            ) {
                                ShowTeamMemberHours(teamMember.user, db, docIdList[i])
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (teamMemberList[i].user != loggedUser.userNickname) {
                                // Send Message
                                TextButton(
                                    onClick = {
                                        userChatPane(teamMemberList[i].user)
                                    },
                                    modifier = Modifier
                                        .background(Color(176, 196, 222), RoundedCornerShape(10.dp))
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "Send Message",
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis,
                                        minLines = 2,
                                        maxLines = 2
                                    )
                                }

                                if ((memberRole == "Admin" || loggedUser.userNickname == team.teamCreator) && teamMemberList[i].user != team.teamCreator) {
                                    // The Remove button is not shown under the profile of the creator of the team (the team creator cannot be removed). Also, it is only shown to the Admins and to the Creator of the team
                                    Spacer(modifier = Modifier.weight(0.05f))

                                    // Edit role
                                    if (teamMemberList[i].role!="Invited") {
                                        TextButton(
                                            onClick = {
                                                val docRef =
                                                    db.collection("TeamMembers").document(docIdList[i])

                                                // Update the "role" field with the value "Participant"
                                                docRef.update("role", "Admin")
                                                    .addOnSuccessListener {
                                                        teamMemberList[i].role = "Admin"
                                                        setRefreshPane(!refreshPane)
                                                        Log.d(
                                                            "Firestore",
                                                            "DocumentSnapshot successfully updated!"
                                                        )
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.w("Firestore", "Error updating document", e)
                                                    }
                                            },
                                            modifier = Modifier
                                                .background(
                                                    Color(176, 196, 222),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .weight(1f)
                                        ) {
                                            Text(
                                                // The button to change a user's role is displayed accordingly to the role of the related user
                                                text = if (teamMemberList[i].role == "Participant") "Promote to admin" else {
                                                    if (teamMemberList[i].role == "Admin") "Demote to participant" else "Promote to participant"
                                                },
                                                color = Color.Black,
                                                textAlign = TextAlign.Center,
                                                overflow = TextOverflow.Ellipsis,
                                                minLines = 2,
                                                maxLines = 2
                                            )
                                        }


                                        Spacer(modifier = Modifier.weight(0.05f))
                                    }

                                    // Remove
                                    TextButton(
                                        onClick = {
                                            indexToRemove = i
                                            setShowRemovePopup(!showRemovePopup)
                                        },
                                        modifier = Modifier
                                            .background(Color(176, 196, 222), RoundedCornerShape(10.dp))
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = "Remove",
                                            color = Color.Black,
                                            textAlign = TextAlign.Center,
                                            overflow = TextOverflow.Ellipsis,
                                            minLines = 2,
                                            maxLines = 2
                                        )
                                    }

                                }
                            } else { // Show different options for the member that corresponds to the logged profile
                                if (teamMemberList[i].role == "Admin" && teamMemberList[i].user != team.teamCreator) {
                                    // Edit role
                                    TextButton(
                                        onClick = {
                                            val docRef = db.collection("TeamMembers").document(docIdList[i])

                                            // Update the "role" field with the value "Participant"
                                            docRef.update("role", "Participant")
                                                .addOnSuccessListener {
                                                    teamMemberList[i].role = "Participant"
                                                    setRefreshPane(!refreshPane)
                                                    Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.w("Firestore", "Error updating document", e)
                                                }
                                        },
                                        modifier = Modifier
                                            .background(Color(176, 196, 222), RoundedCornerShape(10.dp))
                                            .weight(1f)
                                    ) {
                                        Text(
                                            // The button to change a user's role is displayed accordingly to the role of the related user
                                            text = "Demote to participant",
                                            color = Color.Black,
                                            textAlign = TextAlign.Center,
                                            overflow = TextOverflow.Ellipsis,
                                            minLines = 2,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(0.05f))
                                }

                                // Leave team
                                if (teamMemberList[i].user != team.teamCreator) {
                                    TextButton(
                                        onClick = {
                                            indexToRemove = i
                                            setShowRemovePopup(!showRemovePopup)
                                        },
                                        modifier = Modifier
                                            .background(Color(176, 196, 222), RoundedCornerShape(10.dp))
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = "Leave team",
                                            color = Color.Black,
                                            textAlign = TextAlign.Center,
                                            overflow = TextOverflow.Ellipsis,
                                            minLines = 2,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(0.05f))
                                }

                                // Set time
                                TextButton(
                                    onClick = {
                                        indexToUpdateTime = i
                                        setShowUpdateTimePopup(!showUpdateTimePopup)
                                    },
                                    modifier = Modifier
                                        .background(Color(176, 196, 222), RoundedCornerShape(10.dp))
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "Update time",
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis,
                                        minLines = 2,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }


            }
        }

        Spacer(modifier = Modifier.height(5.dp))
    }

    if (showRemovePopup) {
        Column {
            val dialogWidth = 400.dp / (1.3F)
            val dialogHeight = 450.dp / 2

            Dialog(onDismissRequest = {
                setShowRemovePopup(false)
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
                        Button(
                            onClick = {
                                if (teamMemberList[indexToRemove].user == loggedUser.userNickname) {
                                    removeMemberAndRelatedData(
                                        db = db,
                                        teamId = team.teamId,
                                        teamMembersId = docIdList[indexToRemove],
                                        userNickname = loggedUser.userNickname,
                                        onSuccess = {
                                            Log.d("Firestore", "All related data removed successfully")
                                            teamMemberList.removeAt(indexToRemove)
                                            docIdList.removeAt(indexToRemove)
                                            indexToRemove = -1
                                            teamViewModel.allTeams.removeIf(Predicate { t -> t.teamId==team.teamId })
                                            teamViewModel.showingTeams.removeIf( Predicate { t->t.teamId==team.teamId })
                                            loggedUser.userTeams.remove(team.teamId)
                                        },
                                        onFailure = { e ->
                                            Log.e("Firestore", "Failed to remove related data", e)
                                        },
                                        clearTeamInformation = {
                                            //vuoto, vedere 15 righe sotto
                                        }
                                    )
                                    teamListPane
                                } else {
                                    Log.d("ssss", "e no, entra qui")
                                    removeMemberAndRelatedData(
                                        db = db,
                                        teamId = team.teamId,
                                        teamMembersId = docIdList[indexToRemove],
                                        userNickname = teamMemberList[indexToRemove].user,
                                        onSuccess = {
                                            Log.d("Firestore", "All related data removed successfully")
                                            teamMemberList.removeAt(indexToRemove)
                                            docIdList.removeAt(indexToRemove)
                                            indexToRemove = -1
//                                            loggedUser.userTeams.remove(team.teamId)
                                        },
                                        onFailure = { e ->
                                            Log.e("Firestore", "Failed to remove related data", e)
                                        },
                                        clearTeamInformation = {
                                        }
                                    )

                                }

                                setShowRemovePopup(false)

                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier.border(2.dp, Color.Red, RoundedCornerShape(30.dp))
                        ) {
                            Text(
                                text = if (teamMemberList[indexToRemove].user != loggedUser.userNickname) "Remove ${teamMemberList[indexToRemove].user} from team" else "Leave Team",
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                indexToRemove = -1
                                setShowRemovePopup(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier.border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                        ) {
                            Text(text = "Cancel", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (showUpdateTimePopup) {
        Column {
            val dialogWidth = 400.dp / (1.3F)
            val dialogHeight = 550.dp / 2

            Dialog(onDismissRequest = {
                setShowUpdateTimePopup(false)
                timeValue = ""
                timeError = ""
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

                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Change your time partecipation for the team",
                            color = Color.Black,
                            modifier = Modifier
                                .weight(1f)
                                .padding(10.dp, 1.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Left
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = timeValue,
                            onValueChange = {
                                timeValue = it
                                timeError = "" // Reset error when user changes the input
                            },
                            label = { Text("Hours per week") },
                            isError = timeError.isNotBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = myShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = lightBlue,
                                focusedLabelColor = lightBlue,
                                focusedTextColor = Color.DarkGray
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (timeError.isNotBlank()) {
                            Text(
                                timeError,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Right,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (timeValue.isNotEmpty()) {
                                    if (timeValue.matches(Regex("^\\d+\$"))) {
                                        val user = teamMemberList[indexToUpdateTime].user
                                        val idTeamMember = docIdList[indexToUpdateTime]

                                        db.collection("TeamMembers").document(idTeamMember).get()
                                            .addOnSuccessListener { document ->
                                                if (document != null ) {

                                                    document.reference.update("hours", timeValue.toInt())
                                                        .addOnSuccessListener {
                                                            Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                                            timeValue = "" // Reset text field
                                                            timeError = ""
                                                            setShowUpdateTimePopup(false)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.w("Firestore", "Error updating document", e)
                                                            timeError = "Failed to update hours"
                                                        }

                                                } else {
                                                    Log.w("Firestore", "No matching documents found")
                                                    timeError = "No matching user found"
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("Firestore", "Error getting documents", e)
                                                timeError = "Error finding user"
                                            }
                                    } else {
                                        timeError = "Please enter only integers and positive numbers"
                                    }
                                } else {
                                    timeError = "The time field cannot be empty"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                .fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = "Change",
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                indexToUpdateTime = -1
                                timeValue = ""
                                timeError = ""
                                setShowUpdateTimePopup(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier.border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                        ) {
                            Text(text = "Cancel", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun ShowTeamMemberInfo(
    teamMember: String,
    memberRole: String,
    i: Int,
    showUserInformationPane: (String) -> Unit,
    expandedIndex: Int,
    setExpandedIndex: (Int) -> Unit,
    db: FirebaseFirestore,
    doc_id: String
) {
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(teamMember) {
        db.collection("Users").document(teamMember).get()
            .addOnSuccessListener { docUser ->
                user = docUser.toObject(User::class.java)

                if (user != null) {
                    Log.d("Firestore", "User data loaded: ${user!!.userNickname}")
                } else {
                    Log.e("Firestore", "User data is null")
                }
                loading = false
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting user document", exception)
                loading = false
            }
    }

    if (loading) {
        CircularProgressIndicator()
    } else {
        user?.let { userData ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(0.dp, 8.dp)
                        .clickable(onClick = {
                            showUserInformationPane(userData.userNickname)
                        })
                ) {
                    CreateImage(
                        photo = if(userData.userImage == null) null else userData.userImage!!.toUri(),
                        name = "${userData.userFirstName} ${userData.userLastName}",
                        size = 25
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(6.3f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = if (userData.userNickname != loggedUser.userNickname) userData.userNickname else "@You",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(onClick = {
                                showUserInformationPane(userData.userNickname)
                            }),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(3.2f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = memberRole,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        )
                }

                FloatingActionButton(
                    onClick = {
                        if (expandedIndex != i) {
                            setExpandedIndex(i)
                        } else {
                            setExpandedIndex(-2)
                        }
                    },
                    containerColor = Color.DarkGray,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(14.dp)
                ) {
                    if (expandedIndex == i) {
                        Icon(Icons.Default.KeyboardArrowUp, "Hide user options", tint = Color.White)
                    } else {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "View user options",
                            tint = Color.White
                        )
                    }
                }
            }
        } ?: Text(text = "User not found")
    }
}



@Composable
fun ShowCommonTeams(
    teams: MutableList<Team>,
    showTeamDetailsPane: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
            .padding(end = 4.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Teams in common",
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (teams.isEmpty()) {
            Text(
                text = "Nobody.", fontSize = 16.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center
            )
        } else {
            for (i in teams.indices step 3) {
                Row {
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .width(70.dp)
                            .padding(start = 20.dp)
                            .clickable {
                                showTeamDetailsPane(teams[i].teamId)
                            }
                    ) {
                        CreateImage(teams[i].teamImage.toUri(), teams[i].teamName, 70)
                        Text(
                            text = teams[i].teamName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 7.dp, bottom = 2.dp)
                        )
                    }

                    // Check if there is a second team in the current pair
                    if (i + 1 < teams.size) {
                        Column(
                            modifier = Modifier
                                .weight(0.5f)
                                .width(70.dp)
                                .padding(start = 25.dp)
                                .clickable {
                                    showTeamDetailsPane(teams[i + 1].teamId)
                                }
                        ) {
                            CreateImage(
                                teams[i + 1].teamImage.toUri(), teams[i + 1].teamName, 70
                            )
                            Text(
                                text = teams[i + 1].teamName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 7.dp, bottom = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }

                    // Check if there is a third team in the current group
                    if (i + 2 < teams.size) {
                        Column(
                            modifier = Modifier
                                .weight(0.5f)
                                .width(70.dp)
                                .padding(start = 30.dp)
                                .clickable {
                                    showTeamDetailsPane(teams[i + 2].teamId)
                                }
                        ) {
                            CreateImage(
                                teams[i + 2].teamImage.toUri(),
                                teams[i + 2].teamName,
                                70
                            )
                            Text(
                                text = teams[i + 2].teamName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 7.dp, bottom = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChatNotificationCircle(
    xOffset: Int = 0,
    yOffset: Int = 0
) {
    Text(
        modifier = Modifier
            .offset(xOffset.dp, yOffset.dp)
            .drawBehind {
                drawCircle(
                    color = Color(220, 73, 58),
                    radius = 16f
                )
            },
        text = ""
    )
}