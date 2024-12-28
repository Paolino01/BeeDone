package it.polito.BeeDone.home

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.TeamBox
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.BeeDone.activeAnimation
import it.polito.BeeDone.firstAccess
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.team.UserInTeam
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.tasks.await


@SuppressLint("UnusedBoxWithConstraintsScope", "UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePane(
    showTaskDetailsPane: (String) -> Unit,
    showTeamDetailsPane: (String) -> Unit,
    acceptInvitationPane: (String) -> Unit,
    db: FirebaseFirestore,
    sharedPreferences: SharedPreferences,
    allTasks: SnapshotStateList<Task>,
    allTeams: SnapshotStateList<Team>
) {

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    val coroutineScope = rememberCoroutineScope()
    val scrollStateTask = rememberScrollState()
    val scrollStateTeam = rememberScrollState()
    var scrollDirectionTask by remember { mutableIntStateOf(0) }
    var scrollDirectionTeam by remember { mutableIntStateOf(0) }

    var showPopUp by remember { mutableStateOf(true) }

    Column(Modifier.verticalScroll(scrollStateTeam)) {

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(
                text = "Your tasks",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row {
            Text(
                text = "All the tasks you have to complete",
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 10.dp),
                fontSize = 18.sp
            )
        }

        if (loggedUser.userTasks.isNotEmpty()) {

            var taskInProgressFound = false

            for(task in allTasks){
                if (task.taskStatus == "In Progress" || task.taskStatus == "Expired Not Completed")
                    taskInProgressFound = true
            }
            if (!taskInProgressFound){
                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All tasks have been completed.",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                }
            }

            if (taskInProgressFound) {
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (scrollDirectionTask >= 0) "→" else "←",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable {
                                coroutineScope.launch {
                                    if (scrollDirectionTask >= 0) {
                                        scrollStateTask.animateScrollTo(scrollStateTask.maxValue)
                                        scrollDirectionTask = -1
                                    } else {
                                        scrollStateTask.animateScrollTo(0)
                                        scrollDirectionTask = 1
                                    }
                                }
                            },
                        fontSize = 30.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollStateTask)
                        .padding(start = 20.dp)
                ) {
                    for (task in allTasks) {
                        val taskTeam = allTeams.find { it.teamId == task.taskTeam }!!

                        if (task.taskStatus == "In Progress" || task.taskStatus == "Expired Not Completed") {
                            Column(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(end = 20.dp)
                                    .clickable {
                                        showTaskDetailsPane(task.taskId)
                                    }
                            ) {
                                CreateImage(
                                    taskTeam.teamImage.toUri(),
                                    taskTeam.teamName,
                                    70
                                )
                                Text(
                                    text = task.taskTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .align(Alignment.CenterHorizontally),
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "There are no tasks to complete.",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text(
                text = "Your teams",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row {
            Text(
                text = "All the teams you belong to",
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 10.dp),
                fontSize = 18.sp
            )
        }

        if (loggedUser.userTeams.isNotEmpty()) {

            Row(
                horizontalArrangement = Arrangement.Absolute.Right,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "↓",
                    color = Color.Black,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clickable {
                            coroutineScope.launch {
                                scrollStateTeam.animateScrollTo(scrollStateTeam.maxValue)
                                scrollDirectionTeam = -1
                            }
                        },
                    fontSize = 30.sp
                )
            }

            LazyVerticalGrid(
                modifier = Modifier.heightIn(0.dp, screenHeight.dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.Top
            ) {
                items(allTeams) { team ->
                    var userInTeam by remember {
                        mutableStateOf(UserInTeam())
                    }
                    var userInTeamLoaded by remember {
                        mutableStateOf(false)
                    }
                    LaunchedEffect(team) {
                        //val userInTeamId = team.getIdTeamUser(loggedUser)

                        for (uInTeam in loggedUser.userTeams){
                            val userInTeamDoc = db.collection("UserInTeam").document(uInTeam).get().await()
                            val tmpUserInTeam = userInTeamDoc.toObject(UserInTeam::class.java)
                            val id = userInTeamDoc.id
                            if (tmpUserInTeam != null && tmpUserInTeam.first == team.teamId) {
                                db.collection("UserInTeam").document(id).get().addOnSuccessListener {d ->
                                    userInTeam = d.toObject(UserInTeam::class.java)!!
                                    userInTeamLoaded = true
                                }
                                break
                            }
                        }
                    }
                    if(userInTeamLoaded) {
                        TeamBox(showTeamDetailsPane, acceptInvitationPane, team, userInTeam.second)
                    }
                }
            }

            if (loggedUser.userTeams.size > 2) {
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "↑",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable {
                                coroutineScope.launch {
                                    scrollStateTeam.animateScrollTo(0)
                                    scrollDirectionTeam = 1

                                }
                            },
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.height(23.dp))
            }
        } else {
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "You are not part of any team.",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        }
    }

    if(firstAccess) {
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
                                text = "Do you want to turn off animations?",
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
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                    editor.putBoolean("animation", false)
                                    editor.apply()
                                    activeAnimation = false

                                    showPopUp = false
                                    firstAccess = false
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
                                    firstAccess = false
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

}



/* FUNZIONA PROVA loadTeams che dovrebbe caricare i dati dal db
suspend fun loadTeams(db: FirebaseFirestore, userId: String) = suspendCoroutine<Unit> { cont ->
    db.collection("TeamMembers")
        .whereEqualTo("user", userId)
        .get()
        .addOnSuccessListener { teamMembersResult ->
            val teamMemberIds = teamMembersResult.documents.map { it.id }
            Log.d("Firestore", "teamMembers: ${teamMemberIds.toString()}")

            db.collection("Team")
                .whereIn("teamMembers", teamMemberIds)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val team = document.toObject(Team::class.java)
                        Log.d("Firestore", "doc aggiunto ${team.teamId}")
                        teamViewModel.allTeams.add(team)
                        teamViewModel.showingTeams.add(team)
                        loggedUser.userTeams.add(team.teamId)
                    }
                    Log.d("Firestore", "fa qualcosa")
                    cont.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting team documents: ", exception)
                    cont.resumeWithException(exception)
                }
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting team member documents: ", exception)
            cont.resumeWithException(exception)
        }
}
*/

/* PROVA CON SUSPENDABLE COROUTINES IN MODO TALE CHE HOME PANE ASPETTI LOAD TEAMS
@SuppressLint("UnusedBoxWithConstraintsScope", "UnrememberedMutableState",
    "CoroutineCreationDuringComposition"
)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePane(
    showTaskDetailsPane: (String) -> Unit,
    showTeamDetailsPane: (String) -> Unit,
    acceptInvitationPane: (String) -> Unit,
    db: FirebaseFirestore,
    sharedPreferences: SharedPreferences,
    allTasks: SnapshotStateList<Task>,
    allTeams: SnapshotStateList<Team>,
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    val scrollStateTask = rememberScrollState()
    val scrollStateTeam = rememberScrollState()
    var scrollDirectionTask by remember { mutableIntStateOf(0) }
    var scrollDirectionTeam by remember { mutableIntStateOf(0) }
    var showPopUp by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            loadTeams(db, loggedUser.userNickname)
        }
    }

    Column(Modifier.verticalScroll(scrollStateTeam)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(
                text = "Your tasks",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row {
            Text(
                text = "All the tasks you have to complete",
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 10.dp),
                fontSize = 18.sp
            )
        }

        if (loggedUser.taskList.isNotEmpty()) {
            var taskInProgressFound = false

            for (task in allTasks) {
                if (task.taskStatus == "In Progress" || task.taskStatus == "Expired Not Completed")
                    taskInProgressFound = true
            }
            if (!taskInProgressFound) {
                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All tasks have been completed.",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                }
            }

            if (taskInProgressFound) {
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (scrollDirectionTask >= 0) "→" else "←",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable {
                                coroutineScope.launch {
                                    if (scrollDirectionTask >= 0) {
                                        scrollStateTask.animateScrollTo(scrollStateTask.maxValue)
                                        scrollDirectionTask = -1
                                    } else {
                                        scrollStateTask.animateScrollTo(0)
                                        scrollDirectionTask = 1
                                    }
                                }
                            },
                        fontSize = 30.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollStateTask)
                        .padding(start = 20.dp)
                ) {
                    for (task in allTasks) {
                        var teamImage = ""
                        var teamName = ""
                        coroutineScope.launch {
                            val teamDoc = db.collection("Team").document(task.taskTeam).get().await()
                            teamImage = teamDoc.get("teamImage").toString()
                            teamName = teamDoc.get("teamName").toString()
                        }

                        if (task.taskStatus == "In Progress" || task.taskStatus == "Expired Not Completed") {
                            Column(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(end = 20.dp)
                                    .clickable {
                                        showTaskDetailsPane(task.taskId)
                                    }
                            ) {
                                CreateImage(
                                    teamImage.toUri(),
                                    teamName,
                                    70
                                )
                                Text(
                                    text = task.taskTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .align(Alignment.CenterHorizontally),
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "There are no tasks to complete.",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text(
                text = "Your teams",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row {
            Text(
                text = "All the teams you belong to",
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 10.dp),
                fontSize = 18.sp
            )
        }

        if (loggedUser.userTeams.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Absolute.Right,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "↓",
                    color = Color.Black,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clickable {
                            coroutineScope.launch {
                                scrollStateTeam.animateScrollTo(scrollStateTeam.maxValue)
                                scrollDirectionTeam = -1
                            }
                        },
                    fontSize = 30.sp
                )
            }

            LazyVerticalGrid(
                modifier = Modifier.heightIn(0.dp, screenHeight.dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.Top
            ) {
                items(allTeams) { team ->
                    TeamBox(showTeamDetailsPane, acceptInvitationPane, team, false)
                }
            }

            if (loggedUser.userTeams.size > 2) {
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "↑",
                        color = Color.Black,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .clickable {
                                coroutineScope.launch {
                                    scrollStateTeam.animateScrollTo(0)
                                    scrollDirectionTeam = 1
                                }
                            },
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.height(23.dp))
            }
        } else {
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "You are not part of any team.",
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        }
    }

    if (firstAccess) {
        if (showPopUp) {
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
                                text = "Do you want to turn off animations?",
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
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                    editor.putBoolean("animation", false)
                                    editor.apply()
                                    activeAnimation = false

                                    showPopUp = false
                                    firstAccess = false
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
                                    firstAccess = false
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
}
*/
