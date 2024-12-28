package it.polito.BeeDone

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.BeeDone.home.HomePane
import it.polito.BeeDone.profile.ConfirmMailPane
import it.polito.BeeDone.profile.EditUserInformationPane
import it.polito.BeeDone.profile.PasswordPane
import it.polito.BeeDone.profile.ShowUserInformationPane
import it.polito.BeeDone.profile.ShowUserMenu
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.authentication.LoginPane
import it.polito.BeeDone.profile.authentication.SetProfilePane
import it.polito.BeeDone.profile.authentication.SignUpPane
import it.polito.BeeDone.profile.chat.UserChatListPane
import it.polito.BeeDone.profile.chat.UserChatPane
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.task.CreateTaskPane
import it.polito.BeeDone.task.EditTaskPane
import it.polito.BeeDone.task.PersonalTaskListPane
import it.polito.BeeDone.task.ShowTaskAttachmentsPane
import it.polito.BeeDone.task.ShowTaskDetailsMenu
import it.polito.BeeDone.task.ShowTaskDetailsPane
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.task.TaskMenu
import it.polito.BeeDone.task.history.TaskHistoryPane
import it.polito.BeeDone.task.timer.ShowTimer
import it.polito.BeeDone.task.timer.exitTimerPane
import it.polito.BeeDone.team.CreateTeamPane
import it.polito.BeeDone.team.EditTeamPane
import it.polito.BeeDone.team.FeedbackPerformancePane
import it.polito.BeeDone.team.InviteUser
import it.polito.BeeDone.team.InvitedPane
import it.polito.BeeDone.team.ShareTeamPane
import it.polito.BeeDone.team.ShowTeamChatPane
import it.polito.BeeDone.team.ShowTeamDetailsMenu
import it.polito.BeeDone.team.ShowTeamDetailsPane
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.team.TeamListMenu
import it.polito.BeeDone.team.TeamListPane
import it.polito.BeeDone.team.TeamTaskListPane
import it.polito.BeeDone.team.TeamTaskMenu
import it.polito.BeeDone.team.UserInTeam
import it.polito.BeeDone.utils.CreateChatNotificationCircle
import it.polito.BeeDone.utils.TeamBox
import it.polito.BeeDone.utils.lightBlue
import it.polito.BeeDone.utils.questions_answers.Question
import it.polito.BeeDone.utils.questions_answers.ShowAnswers
import it.polito.BeeDone.utils.questions_answers.ShowQuestions
import kotlinx.coroutines.tasks.await

//variable to show a popup on the home page
var firstAccess = false

//variable for manage animations
var activeAnimation = true


@SuppressLint("RestrictedApi", "UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedPreferences: SharedPreferences,
    db: FirebaseFirestore

) {              //At the moment, the main screen is the profile page
    val navController = rememberNavController()
    val actions = Actions(navController)

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentScreen = currentDestination?.route

    val NICK_KEY = "nickname"
    val PWD_KEY = "pwd"

    val startDestination: String = "LoginPane"
    var selectedUser by remember { mutableStateOf(User()) }
    var selectedTask by remember { mutableStateOf(Task()) }
    //userViewModel.setProfileInformation(selectedUser)

    val nickname = sharedPreferences.getString(NICK_KEY, "")
        .toString() //Get nickname and password from SharedPreferences
    val pwd = sharedPreferences.getString(PWD_KEY, "").toString()
    activeAnimation = sharedPreferences.getBoolean("animation", true)


    if (nickname.isNotEmpty() && pwd.isNotEmpty()) { //Otherwise, retrieve the logged user and go to home pane
        var taskFetchCompleted by remember { mutableStateOf(false) }
        var teamFetchCompleted by remember { mutableStateOf(false) }

        LaunchedEffect(nickname, pwd) {
            firstAccess = false
            val db = Firebase.firestore

            val users = db.collection("Users")
            users.document(nickname).get()
                .addOnSuccessListener { d ->
                    if (d != null) {
                        if (d.get("userPassword") == pwd) {
                            loggedUser.userImage = d.get("userImage").toString()
                            loggedUser.userNickname = d.get("userNickname").toString()
                            loggedUser.userStatus = d.get("userStatus").toString()
                            loggedUser.userFirstName = d.get("userFirstName").toString()
                            loggedUser.userLastName = d.get("userLastName").toString()
                            loggedUser.userBirthDate = d.get("userBirthDate").toString()
                            loggedUser.userDescription = d.get("userDescription").toString()
                            loggedUser.userLocation = d.get("userLocation").toString()
                            loggedUser.userMail = d.get("userMail").toString()
                            loggedUser.userStatus = d.get("userStatus").toString()
                            loggedUser.userChat = d.get("userChat") as MutableList<String>
                            loggedUser.userTeams = d.get("userTeams") as MutableList<String>
                            loggedUser.userTasks = d.get("userTasks") as MutableList<String>

                            selectedUser = User(
                                loggedUser.userImage,
                                loggedUser.userFirstName,
                                loggedUser.userLastName,
                                loggedUser.userNickname,
                                loggedUser.userMail,
                                loggedUser.userLocation,
                                loggedUser.userDescription,
                                loggedUser.userBirthDate,
                                loggedUser.userStatus,
                                loggedUser.userTeams,
                                loggedUser.userTasks,
                                loggedUser.userChat,
                                loggedUser.password,
                            )
                            userViewModel.setProfileInformation(selectedUser)
                            userViewModel.setOldProfileInformation()

                            //Retrieve all the teams of the loggedUser and populate the list allTeams
                            var userInTeam: UserInTeam?
                            teamViewModel.allTeams = mutableStateListOf()

                            if (loggedUser.userTeams.size == 0) {
                                teamFetchCompleted = true
                            } else {
                                for (userInTeamRef in loggedUser.userTeams) {
                                    db.collection("UserInTeam").document(userInTeamRef)
                                        .addSnapshotListener { userInTeamDoc, err1 ->
                                            if (err1 == null) {
                                                if (userInTeamDoc != null) {
                                                    userInTeam =
                                                        userInTeamDoc.toObject(UserInTeam::class.java)
                                                    if (userInTeam != null) {
                                                        db.collection("Team")
                                                            .document(userInTeam!!.first)
                                                            .addSnapshotListener { teamDoc, err2 ->
                                                                if (err2 == null) {
                                                                    if (teamDoc != null) {
                                                                        val team =
                                                                            teamDoc.toObject(Team::class.java)
                                                                        if (team != null) {
                                                                            if (!(teamViewModel.allTeams.map { it.teamId }
                                                                                    .contains(team.teamId))) {
                                                                                teamViewModel.allTeams.add(
                                                                                    team
                                                                                )
                                                                            }
                                                                            teamFetchCompleted =
                                                                                true
                                                                        } else {
                                                                            Log.e(
                                                                                TAG,
                                                                                "Team object is null"
                                                                            )
                                                                        }
                                                                    } else {
                                                                        Log.e(
                                                                            TAG,
                                                                            "teamDoc is null"
                                                                        )
                                                                    }
                                                                } else {
                                                                    Log.e(TAG, err2.toString())
                                                                }
                                                            }
                                                    } else {
                                                        Log.e(TAG, "UserInTeam object is null")
                                                    }
                                                } else {
                                                    Log.e(TAG, "userInTeamDoc is null")
                                                }
                                            } else {
                                                Log.e(TAG, err1.toString())
                                            }
                                        }
                                }
                            }


                            //Retrieve all the tasks of the loggedUser and populate the list allTasks
                            taskViewModel.allTasks = mutableStateListOf()

                            if (loggedUser.userTasks.size == 0) {
                                taskFetchCompleted = true
                            } else {
                                db.collection("Tasks").whereIn("taskId", loggedUser.userTasks)
                                    .addSnapshotListener { taskDocs, e ->
                                        if (e == null) {
                                            taskViewModel.allTasks.clear()
                                            for (taskDoc in taskDocs!!) {
                                                taskViewModel.allTasks.add(taskDoc.toObject(Task::class.java))
                                            }
                                            taskFetchCompleted = true
                                        } else {
                                            Log.e(TAG, e.toString())
                                        }
                                    }
                            }
                        } else {
                            //passworrd errata
                        }
                    } else {
                        //nickname non esistente
                    }
                }.addOnFailureListener { exeception ->
                    Log.d("DB exception", exeception.toString())
                }
        }

        if (taskFetchCompleted && teamFetchCompleted && currentScreen == "LoginPane") {
            navController.navigate("HomePane")
        }
    } else {
        // Se non ci sono credenziali memorizzate, mostra il login pane
        LaunchedEffect(Unit) {
            navController.navigate("LoginPane")
        }
    }

    var selectedQuestion: Question? = null
    var selectedTeam: Team? = null

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        //This scaffold is needed for the BottomBar. Inside of the scaffold content, there is the application interface
        topBar = {
            TopAppBar(title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = lightBlue),
                actions = {
                    when (currentScreen) {
                        "EditUserInformationPane" -> {

                            IconButton(onClick = {
                                userViewModel.exitWithoutUserInformationUpdate(
                                    actions.navigateBack
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Profile", color = Color.Black, fontSize = 24.sp)

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = {
                                userViewModel.validate(
                                    showUserInformationPane = actions.showUserInformationPane,
                                    navigateBack = actions.navigateBack
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                        "EditPasswordPane" -> {
                            IconButton(onClick = {
                                userViewModel.exitWithoutPasswordUpdate(actions.navigateBack)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }

                            Text(text = "Set Password", color = Color.Black, fontSize = 24.sp)

                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { userViewModel.validatePassword(actions.navigateBack) }) {
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                        "ShowUserInformationPane/{userNickname}" -> {
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(text = "Profile", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))

                            if (selectedUser.userNickname == loggedUser.userNickname) {
                                //We show the option to edit the profile only if the selectedProfile corresponds to the loggedProfile
                                IconButton(onClick = {
                                    actions.editUserInformationPane()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit"
                                    )
                                }

                                ShowUserMenu(
                                    actions.loginPane,
                                    userViewModel::removeData,
                                    sharedPreferences,
                                    userViewModel::clearUserInformation
                                )
                            }
                        }

                        "ConfirmMailPane" -> {
                            IconButton(onClick = {
                                userViewModel.exitWithoutCodeUpdate(actions.navigateBack)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Profile", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { userViewModel.validateConfirmationCode(actions.navigateBack) }) {
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                        "PersonalTaskListPane" -> {
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(text = "My tasks", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))

                            val userTeams: SnapshotStateList<UserInTeam> = mutableStateListOf()

                            for (pair in selectedUser.userTeams) {
                                val pairRef = db.collection("UserInTeam").document(pair)
                                pairRef.get().addOnSuccessListener { teamDoc ->
                                    val team = teamDoc.toObject(UserInTeam::class.java)
                                    if (team != null) {
                                        userTeams.add(team)
                                    }
                                }.addOnFailureListener { exception ->
                                    Log.e("Firestore", "Error getting team data", exception)
                                }
                            }

                            TaskMenu(
                                taskViewModel.showingTasks,
                                taskViewModel.allTasks,
                                userTeams
                            )
                        }

                        "CreateTaskPane" -> {
                            IconButton(onClick = {
                                taskViewModel.noUpdateTaskInformation()
                                actions.navigateBack()
                            }) //back to my task list
                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Create task", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                taskViewModel.validateTaskInformation(
                                    showTaskDetailsPane = actions.showTaskDetailsPane,
                                    navigateBack = actions.navigateBack,
                                    task = null,
                                    db
                                )
                            }) {             //Saves the information and returns to the ShowTaskDetailsPane
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Create"
                                )
                            }
                        }

                        "CreateTaskPane/{teamId}" -> {
                            IconButton(onClick = {
                                taskViewModel.noUpdateTaskInformation()
                                actions.navigateBack()
                            }) //back to my task list
                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Create task", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                actions.navigateBack
                                taskViewModel.validateTaskInformation(
                                    showTaskDetailsPane = actions.showTaskDetailsPane,
                                    navigateBack = actions.navigateBack,
                                    task = null,
                                    db
                                )
                            }) {             //Saves the information and returns to the ShowTaskDetailsPane
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Create"
                                )
                            }
                        }

                        "ShowTaskDetailsPane/{taskId}" -> {
                            IconButton(onClick = {
                                taskViewModel.showingTasks =
                                    taskViewModel.allTasks.toMutableStateList()
                                actions.navigateBack()    //back to my task list
                            })

                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(
                                text = selectedTask.taskTitle,
                                color = Color.Black,
                                fontSize = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.8f)                  //Limiting the maximum width of the Text, so that the menu icon can be shown
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            ShowTaskDetailsMenu(
                                actions.showTaskQuestionsPane,
                                actions.showTaskAttachmentsPane,
                                actions.historyPane,
                                selectedTask
                            )
                        }

                        "EditTaskPane/{taskId}" -> {
                            IconButton(onClick = {
                                taskViewModel.noUpdateTaskInformation()
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Edit Task", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                taskViewModel.validateTaskInformation(
                                    navigateBack = actions.navigateBack,
                                    task = selectedTask,
                                    db = db
                                )
                            }) {             //Saves the information and returns to the ShowTaskDetailsPane
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                        "TeamTaskListPane/{teamId}" -> {

                            IconButton(onClick = { actions.navigateBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }

                            Text(
                                text = "${selectedTeam!!.teamName} tasks",
                                color = Color.Black,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))



                            TeamTaskMenu(
                                taskViewModel.showingTasks,
                                selectedTeam!!.teamTasks
                            )
                        }


                        "TeamListPane" -> {

                            Spacer(modifier = Modifier.width(20.dp))
                            Text(text = "My Teams", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))

                            val userTeams = mutableListOf<UserInTeam>()

                            for (pair in loggedUser.userTeams) {
                                val pairRef = db.collection("UserInTeam").document(pair)
                                pairRef.get().addOnSuccessListener { teamDoc ->
                                    val team = teamDoc.toObject(UserInTeam::class.java)
                                    if (team != null) {
                                        userTeams.add(team)
                                    }
                                }.addOnFailureListener { exception ->
                                    Log.e("Firestore", "Error getting team data", exception)
                                }
                            }

                            //teamViewModel.showingTeams = userTeams.toMutableList()

                            TeamListMenu(
                                teamViewModel.allTeams,
                                teamViewModel.showingTeams,
                                db
                            )


                        }

                        "ShowTaskQuestionsPane/{taskId}" -> {
                            IconButton(onClick = {
                                taskViewModel.setTaskQuestion("")
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Q&A", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "ShowTaskAnswersPane/{questionId}" -> {
                            IconButton(onClick = {
                                taskViewModel.setTaskAnswer("")
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Q&A", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "ShowTaskAttachmentsPane" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Attachments", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "HistoryPane" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "History", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "TaskTimerPane" -> {
                            IconButton(onClick = {
                                exitTimerPane(
                                    taskViewModel::setTaskTimerTitle, taskViewModel::setTaskTimer
                                )
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(
                                text = selectedTask.taskTitle,
                                color = Color.Black,
                                fontSize = 24.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "ShowTeamDetailsPane/{teamId}" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            })

                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            if (selectedTeam != null) {
                                Text(
                                    text = selectedTeam!!.teamName,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(0.8f)                  //Limiting the maximum width of the Text, so that the menu icon can be shown
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))

                            if (selectedTeam != null) {
                                var userInTeam by remember {
                                    mutableStateOf(UserInTeam())
                                }
                                var userInTeamLoaded by remember {
                                    mutableStateOf(false)
                                }
                                LaunchedEffect(selectedTeam) {
                                    //val userInTeamId = team.getIdTeamUser(loggedUser)

                                    for (uInTeam in loggedUser.userTeams){
                                        val userInTeamDoc = db.collection("UserInTeam").document(uInTeam).get().await()
                                        val tmpUserInTeam = userInTeamDoc.toObject(UserInTeam::class.java)
                                        val id = userInTeamDoc.id
                                        if (tmpUserInTeam != null && tmpUserInTeam.first == selectedTeam!!.teamId) {
                                            db.collection("UserInTeam").document(id).get().addOnSuccessListener {d ->
                                                userInTeam = d.toObject(UserInTeam::class.java)!!
                                                userInTeamLoaded = true
                                            }
                                            break
                                        }
                                    }
                                }
                                if(userInTeamLoaded) {
                                    ShowTeamDetailsMenu(
                                        selectedTeam!!.teamName,
                                        actions.editTeamPane,
                                        actions.shareTeamPane,
                                        actions.feedbackPerformancePane,
                                        actions.showTeamChatPane,
                                        actions.teamListPane,
                                        selectedTeam!!,
                                        taskViewModel.allTasks,
                                        db,
                                        userInTeam.second
                                    )
                                }
                            }


                        }

                        "CreateTeamPane" -> {
                            IconButton(onClick = {
                                teamViewModel.noUpdateTeamInformation()
                                actions.navigateBack()
                            }) //back to my task list
                            {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Create team", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                teamViewModel.validateTeamInformation(
                                    navController, actions.navigateBack, null
                                )
                            }) {             //Saves the information and returns to the ShowTeamDetailsPane
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Create"
                                )
                            }
                        }


                        "EditTeamPane/{teamId}" -> {
                            IconButton(onClick = {
                                teamViewModel.noUpdateTeamInformation()
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Edit Team", color = Color.Black, fontSize = 24.sp)

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = {
                                teamViewModel.validateTeamInformation(
                                    navController, actions.navigateBack, selectedTeam
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                        "ShareTeamPane" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Share Team", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "FeedbackPerformancePane/{teamId}" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(
                                text = "Feedback & Performance",
                                color = Color.Black,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "ShowTeamChatPane/{teamId}" -> {
                            IconButton(onClick = {
                                teamViewModel.setTeamMessage("")
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(text = "Chat", color = Color.Black, fontSize = 24.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "UserChatPane/{userNickname}" -> {
                            IconButton(onClick = {
                                userViewModel.setUserMessage("")
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            TextButton(onClick = {
                                actions.showUserInformationPane(selectedUser.userNickname)
                            }) {
                                if(selectedUser.userNickname != loggedUser.userNickname) {
                                    Text(
                                        text = selectedUser.userNickname,
                                        color = Color.Black,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "UserChatListPane" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(
                                text = "Chat", color = Color.Black, fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "HomePane" -> {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BeeDone", color = Color.Black, fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "AcceptInvitationPane/{teamId}" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                            Text(
                                text = "Accept invitation", color = Color.Black, fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "LoginPane" -> {
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Login", color = Color.Black, fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        "SignUpPane" -> {
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Sign Up", color = Color.Black, fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = {

                                firstAccess = true
                                userViewModel.validateSignUp(
                                    showHomePane = actions.homePane,
                                    navigateBack = actions.navigateBack,
                                    sharedPreferences = sharedPreferences
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                        "SetProfilePane" -> {
                            IconButton(onClick = {
                                actions.navigateBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Set Profile",
                                color = Color.Black,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = {
                                userViewModel.validateSignUpWithGoogle(
                                    showHomePane = actions.homePane,
                                    navigateBack = actions.navigateBack,
                                    sharedPreferences = sharedPreferences
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check, contentDescription = "Save"
                                )
                            }
                        }

                    }
                })
        },

        bottomBar = {
            val navigationBarItemColors = NavigationBarItemColors(
                selectedTextColor = Color(45, 86, 156),
                selectedIconColor = Color(45, 86, 156),
                selectedIndicatorColor = Color(132, 174, 250),
                unselectedIconColor = Color.DarkGray,
                unselectedTextColor = Color.DarkGray,
                disabledIconColor = Color.DarkGray,
                disabledTextColor = Color.DarkGray
            )

            if (currentScreen != "LoginPane" && currentScreen != "SignUpPane" && currentScreen != "SetProfilePane") {      //If i'm in the Login Pane or SignUp Pane, I hide the bottom bar
                NavigationBar(
                    tonalElevation = 0.dp,
                    containerColor = lightBlue, //same color as the top bar
                    modifier = Modifier.height(60.dp),
                ) {

                    NavigationBarItem(colors = navigationBarItemColors, onClick = {
                        actions.homePane()
                    }, icon = {
                        Icon(
                            Icons.Outlined.Home, "Home", Modifier.size(25.dp)
                        )
                    }, label = { Text("Home") }, selected = (currentScreen == "HomePane")
                    )

                    NavigationBarItem(
                        colors = navigationBarItemColors,
                        onClick = {
                            actions.teamListPane()
                        },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ic_notselected_teams),
                                "Teams",
                                Modifier.size(25.dp)
                            )

                        },
                        label = { Text("Teams") },
                        selected = (currentScreen == "TeamTaskListPane/{teamId}" || currentScreen == "ShowTeamDetailsPane/{teamId}" || currentScreen == "TeamListPane" || currentScreen == "ShareTeamPane" || currentScreen == "EditTeamPane/{teamId}" || currentScreen == "ShowTeamChatPane/{teamId}" || currentScreen == "FeedbackPerformancePane/{teamId}" || currentScreen == "AcceptInvitationPane/{teamId}")
                    )

                    NavigationBarItem(
                        colors = navigationBarItemColors,
                        onClick = {
                            taskViewModel.showingTasks = taskViewModel.allTasks.toMutableStateList()
                            actions.personalTaskListPane()
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_notselected_tasks),
                                contentDescription = "Tasks",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        label = { Text("Tasks") },
                        selected = (currentScreen == "PersonalTaskListPane" || currentScreen == "CreateTaskPane" || currentScreen == "CreateTaskPane/{teamId}" || currentScreen == "ShowTaskDetailsPane/{taskId}" || currentScreen == "EditTaskPane/{taskId}" || currentScreen == "ShowTaskAttachmentsPane" || currentScreen == "HistoryPane" || currentScreen == "ShowTaskAnswersPane/{questionId}" || currentScreen == "ShowTaskQuestionsPane/{taskId}" || currentScreen == "TaskTimerPane")
                    )

                    NavigationBarItem(
                        colors = navigationBarItemColors,

                        /* when the user clicks on the profile icon in the bottom bar
                       he returns to the profile page saving the changes he has made */
                        onClick = {
                            selectedUser = User(
                                loggedUser.userImage,
                                loggedUser.userFirstName,
                                loggedUser.userLastName,
                                loggedUser.userNickname,
                                loggedUser.userMail,
                                loggedUser.userLocation,
                                loggedUser.userDescription,
                                loggedUser.userBirthDate,
                                loggedUser.userStatus,
                                loggedUser.userTeams,
                                loggedUser.userTasks,
                                loggedUser.userChat,
                                loggedUser.password,
                            )
                            userViewModel.setProfileInformation(selectedUser)
                            userViewModel.setOldProfileInformation()
                            actions.showUserInformationPane(loggedUser.userNickname)
                        },
                        icon = {
                            Icon(
                                Icons.Filled.AccountCircle, "Profile", Modifier.size(25.dp)
                            )
                        },
                        label = { Text("Profile") },
                        selected = (currentScreen == "ShowUserInformationPane/{userNickname}" || currentScreen == "EditPasswordPane" || currentScreen == "EditUserInformationPane" || currentScreen == "ConfirmMailPane" || currentScreen == "UserChatPane/{userNickname}" || currentScreen == "UserChatListPane")
                    )
                }

            }

        },

        ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,

                ) {
                composable("EditUserInformationPane") {
                    EditUserInformationPane(
                        userViewModel.photoValue,
                        userViewModel::setPhoto,
                        userViewModel.firstNameValue,
                        userViewModel.firstNameError,
                        userViewModel::setFirstName,
                        userViewModel.lastNameValue,
                        userViewModel.lastNameError,
                        userViewModel::setLastName,
                        userViewModel.nicknameValue,
                        userViewModel.nicknameError,
                        userViewModel::setNickname,
                        userViewModel.mailValue,
                        userViewModel.mailError,
                        userViewModel::setMail,
                        userViewModel::openConfirmMailPane,
                        userViewModel.locationValue,
                        userViewModel.locationError,
                        userViewModel::setLocation,
                        userViewModel.descriptionValue,
                        userViewModel::setDescription,
                        userViewModel.birthDateValue,
                        userViewModel.birthDateError,
                        userViewModel::setBirthDate,
                        userViewModel.statusValue,
                        userViewModel::setStatus,
                        userViewModel::changingPassword,
                        actions.confirmMailPane,
                        actions.editPasswordPane
                    )
                }

                composable("EditPasswordPane") {
                    PasswordPane(
                        userViewModel.passwordValue,
                        userViewModel.passwordConfirmation,
                        userViewModel.passwordError,
                        userViewModel::setPassword,
                        userViewModel::setConfirmationPassword
                    )
                }

                composable(
                    "ShowUserInformationPane/{userNickname}",
                    arguments = listOf(navArgument("userNickname") { type = NavType.StringType })
                ) { entry ->
                    val parameterNickname = entry.arguments?.getString("userNickname")

                    if(parameterNickname != loggedUser.userNickname) {
                        db.collection("Users")
                            .document(parameterNickname!!).get()
                            .addOnSuccessListener { d ->
                                if (d != null) {
                                    selectedUser.userImage = d.get("userImage").toString()
                                    selectedUser.userNickname = d.get("userNickname").toString()
                                    selectedUser.userStatus = d.get("userStatus").toString()
                                    selectedUser.userFirstName = d.get("userFirstName").toString()
                                    selectedUser.userLastName = d.get("userLastName").toString()
                                    selectedUser.userBirthDate = d.get("userBirthDate").toString()
                                    selectedUser.userDescription =
                                        d.get("userDescription").toString()
                                    selectedUser.userLocation = d.get("userLocation").toString()
                                    selectedUser.userMail = d.get("userMail").toString()
                                    selectedUser.userStatus = d.get("userStatus").toString()
                                    selectedUser.userChat = d.get("userChat") as MutableList<String>
                                    selectedUser.userTeams =
                                        d.get("userTeams") as MutableList<String>
                                    selectedUser.userTasks =
                                        d.get("userTasks") as MutableList<String>

                                    userViewModel.setProfileInformation(selectedUser)
                                    userViewModel.setOldProfileInformation()
                                }
                            }.addOnFailureListener { exception ->
                                Log.d("DB exception", exception.toString())
                            }
                    }
                    else {
                        selectedUser = User(
                            loggedUser.userImage,
                            loggedUser.userFirstName,
                            loggedUser.userLastName,
                            loggedUser.userNickname,
                            loggedUser.userMail,
                            loggedUser.userLocation,
                            loggedUser.userDescription,
                            loggedUser.userBirthDate,
                            loggedUser.userStatus,
                            loggedUser.userTeams,
                            loggedUser.userTasks,
                            loggedUser.userChat,
                            loggedUser.password,
                        )
                        userViewModel.setProfileInformation(selectedUser)
                        userViewModel.setOldProfileInformation()
                    }

                    ShowUserInformationPane(
                        selectedUser,
                        userViewModel.photoValue,
                        userViewModel.firstNameValue,
                        userViewModel.lastNameValue,
                        userViewModel.nicknameValue,
                        userViewModel.mailValue,
                        userViewModel.locationValue,
                        userViewModel.descriptionValue,
                        userViewModel.birthDateValue,
                        userViewModel.statusValue,
                        actions.userChatPane,
                        actions.userChatListPane,
                        actions.showTeamDetailsPane,
                        teamViewModel.allTeams,
                        db
                    )

                    if (userViewModel.allUsers.map { it.userNickname }
                            .contains(parameterNickname)) {
                        selectedUser = userViewModel.allUsers.find {
                            it.userNickname == parameterNickname
                        }!!
                        userViewModel.setProfileInformation(selectedUser)
                        userViewModel.setOldProfileInformation()
                        ShowUserInformationPane(
                            selectedUser,
                            userViewModel.photoValue,
                            userViewModel.firstNameValue,
                            userViewModel.lastNameValue,
                            userViewModel.nicknameValue,
                            userViewModel.mailValue,
                            userViewModel.locationValue,
                            userViewModel.descriptionValue,
                            userViewModel.birthDateValue,
                            userViewModel.statusValue,
                            actions.userChatPane,
                            actions.userChatListPane,
                            actions.showTeamDetailsPane,
                            teamViewModel.allTeams,
                            db
                        )
                    }
                }

                composable("ConfirmMailPane") {
                    ConfirmMailPane(
                        userViewModel.confirmationCodeValue,
                        userViewModel.confirmationCodeError,
                        userViewModel::setConfirmationCode
                    )
                }

                composable("PersonalTaskListPane") {
                    //taskViewModel.showingTasks = taskViewModel.allTasks.toMutableStateList()
                    PersonalTaskListPane(
                        taskViewModel.showingTasks,
                        taskViewModel::clearTaskInformation,
                        actions.createTaskPane,
                        actions.showTaskDetailsPane,
                        actions.editTaskPane,
                        db,
                        teamViewModel.allTeams
                    )
                }

                composable("CreateTaskPane") {
                    CreateTaskPane(
                        taskTitleValue = taskViewModel.taskTitleValue,
                        taskTitleError = taskViewModel.taskTitleError,
                        setTaskTitle = taskViewModel::setTaskTitle,
                        taskDescriptionValue = taskViewModel.taskDescriptionValue,
                        setTaskDescription = taskViewModel::setTaskDescription,
                        taskDeadlineValue = taskViewModel.taskDeadlineValue,
                        taskDeadlineError = taskViewModel.taskDeadlineError,
                        setTaskDeadline = taskViewModel::setTaskDeadline,
                        taskTagValue = taskViewModel.taskTagValue,
                        setTaskTag = taskViewModel::setTaskTag,
                        taskCategoryValue = taskViewModel.taskCategoryValue,
                        taskCategoryError = taskViewModel.taskCategoryError,
                        setTaskCategory = taskViewModel::setTaskCategory,
                        taskUsersValue = taskViewModel.taskUsersValue,
                        setTaskUsers = taskViewModel::setTaskUsers,
                        deleteTaskUsers = taskViewModel::deleteTaskUsers,
                        taskRepeatValue = taskViewModel.taskRepeatValue,
                        setTaskRepeat = taskViewModel::setTaskRepeat,
                        taskTeamValue = taskViewModel.taskTeamValue,
                        taskTeamError = taskViewModel.taskTeamError,
                        setTaskTeam = taskViewModel::setTaskTeam,
                        selectedTeam = null,
                        createTaskPaneFromTeam = actions.createTaskPaneFromTeam,
                        db = db,
                        allTeams = teamViewModel.allTeams
                    )
                }

                composable(
                    "CreateTaskPane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }
                    if (selectedTeam != null) {
                        CreateTaskPane(
                            taskTitleValue = taskViewModel.taskTitleValue,
                            taskTitleError = taskViewModel.taskTitleError,
                            setTaskTitle = taskViewModel::setTaskTitle,
                            taskDescriptionValue = taskViewModel.taskDescriptionValue,
                            setTaskDescription = taskViewModel::setTaskDescription,
                            taskDeadlineValue = taskViewModel.taskDeadlineValue,
                            taskDeadlineError = taskViewModel.taskDeadlineError,
                            setTaskDeadline = taskViewModel::setTaskDeadline,
                            taskTagValue = taskViewModel.taskTagValue,
                            setTaskTag = taskViewModel::setTaskTag,
                            taskCategoryValue = taskViewModel.taskCategoryValue,
                            taskCategoryError = taskViewModel.taskCategoryError,
                            setTaskCategory = taskViewModel::setTaskCategory,
                            taskUsersValue = taskViewModel.taskUsersValue,
                            setTaskUsers = taskViewModel::setTaskUsers,
                            deleteTaskUsers = taskViewModel::deleteTaskUsers,
                            taskRepeatValue = taskViewModel.taskRepeatValue,
                            setTaskRepeat = taskViewModel::setTaskRepeat,
                            taskTeamValue = taskViewModel.taskTeamValue,
                            taskTeamError = taskViewModel.taskTeamError,
                            setTaskTeam = taskViewModel::setTaskTeam,
                            selectedTeam = selectedTeam,
                            createTaskPaneFromTeam = actions.createTaskPaneFromTeam,
                            db = db,
                            allTeams = teamViewModel.allTeams
                        )
                    }
                }

                composable(
                    "ShowTaskDetailsPane/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    selectedTask =
                        taskViewModel.allTasks.find { it.taskId == entry.arguments?.getString("taskId") }!!
                    if (selectedTask != null) {
                        var taskInformationLoaded by remember { mutableStateOf(false) }
                        LaunchedEffect(selectedTask, db) {
                            taskViewModel.setTaskInformation(selectedTask, db)
                            taskInformationLoaded = true
                        }
                        if (taskInformationLoaded) {
                            ShowTaskDetailsPane(
                                selectedTask = selectedTask,
                                taskViewModel::addTaskEventToHistory, //for subtask hystory
                                taskViewModel::setTaskAsCompleted,
                                taskViewModel.taskStatusValue,
                                taskViewModel.taskSubtasksValue,
                                taskViewModel::addTaskSubtasks,
                                actions.editTaskPane,
                                actions.taskTimerPane,
                                actions.showTeamDetailsPane,
                                actions.showUserInformationPane,
                                teamViewModel.allTeams,
                                db
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }

                composable(
                    "EditTaskPane/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    selectedTask =
                        taskViewModel.allTasks.find { it.taskId == entry.arguments?.getString("taskId") }!!
                    if (selectedTask != null) {
                        var taskInformationLoaded by remember { mutableStateOf(false) }
                        LaunchedEffect(selectedTask, db) {
                            taskViewModel.setTaskInformation(selectedTask, db)
                            taskInformationLoaded = true
                        }
                        if (taskInformationLoaded) {
                            EditTaskPane(
                                taskTitleValue = taskViewModel.taskTitleValue,
                                taskTitleError = taskViewModel.taskTitleError,
                                setTaskTitle = taskViewModel::setTaskTitle,
                                taskDescriptionValue = taskViewModel.taskDescriptionValue,
                                setTaskDescription = taskViewModel::setTaskDescription,
                                taskDeadlineValue = taskViewModel.taskDeadlineValue,
                                taskDeadlineError = taskViewModel.taskDeadlineError,
                                setTaskDeadline = taskViewModel::setTaskDeadline,
                                taskTagValue = taskViewModel.taskTagValue,
                                setTaskTag = taskViewModel::setTaskTag,
                                taskCategoryValue = taskViewModel.taskCategoryValue,
                                taskCategoryError = taskViewModel.taskCategoryError,
                                setTaskCategory = taskViewModel::setTaskCategory,
                                taskUsersValue = taskViewModel.taskUsersValue,
                                setTaskUsers = taskViewModel::setTaskUsers,
                                deleteTaskUsers = taskViewModel::deleteTaskUsers,
                                taskRepeatValue = taskViewModel.taskRepeatValue,
                                setTaskRepeat = taskViewModel::setTaskRepeat,
                                taskTeamValue = taskViewModel.taskTeamValue,
                                db = db
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }

                composable(
                    "ShowTaskQuestionsPane/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    selectedTask =
                        taskViewModel.allTasks.find { it.taskId == entry.arguments?.getString("taskId") }!!
                    if (selectedTask != null) {
                        ShowQuestions(
                            selectedTask = selectedTask!!,
                            questions = taskViewModel.taskQuestionsValue,
                            setQuestions = taskViewModel::setTaskQuestions,
                            questionValue = taskViewModel.taskQuestionValue,
                            setQuestion = taskViewModel::setTaskQuestion,
                            showUserInformationPane = actions.showUserInformationPane,
                            navigate = actions.showTaskAnswersPane,
                            db = db
                        )
                    }
                }

                composable(
                    "ShowTaskAnswersPane/{questionId}",
                    arguments = listOf(navArgument("questionId") { type = NavType.StringType })
                ) { entry ->
                    selectedQuestion = taskViewModel.taskQuestionsValue.find {
                        it.questionId == entry.arguments?.getString("questionId")
                    }
                    if (selectedQuestion != null) {
                        ShowAnswers(
                            answers = taskViewModel.taskAnswersValue,
                            setAnswers = taskViewModel::setTaskAnswers,
                            assignAnswers = taskViewModel::assignTaskAnswers,
                            answerValue = taskViewModel.taskAnswerValue,
                            setAnswer = taskViewModel::setTaskAnswer,
                            selectedQuestion = selectedQuestion!!,
                            showUserInformationPane = actions.showUserInformationPane,
                            db
                        )
                    }
                }

                composable("ShowTaskAttachmentsPane") {
                    if (selectedTask != null) {
                        ShowTaskAttachmentsPane(
                            taskViewModel.taskLinkValue,
                            taskViewModel::setTaskLink,
                            taskViewModel.taskMediaListValue,
                            taskViewModel::setTaskMediaList,
                            taskViewModel.taskLinkListValue,
                            taskViewModel::setTaskLinkList,
                            taskViewModel.taskDocumentListValue,
                            taskViewModel::setTaskDocumentList,
                            selectedTask,
                            db
                        )
                    }
                }

                composable("HistoryPane") {
                    TaskHistoryPane(
                        history = taskViewModel.taskHistoryValue,
                        showUserInformationPane = actions.showUserInformationPane
                    )
                }

                composable("TaskTimerPane") {
                    if (selectedTask != null) {
                        ShowTimer(
                            task = selectedTask,
                            taskTimerTitleValue = taskViewModel.taskTimerTitleValue,
                            setTaskTimerTitle = taskViewModel::setTaskTimerTitle,
                            taskTimerValue = taskViewModel.taskTimerValue,
                            setTaskTimer = taskViewModel::setTaskTimer,
                            taskTimerHistory = taskViewModel.taskTimerHistory,
                            addTaskTimerHistory = taskViewModel::addTaskTimerHistory,
                            showUserInformationPane = actions.showUserInformationPane,
                            taskUsersValue = taskViewModel.taskUsersValue,
                            selectedTask = selectedTask,
                            db = db
                        )
                    }
                }

                composable(
                    "TeamTaskListPane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }
                    if (selectedTeam != null) {

                        var teamTasks = mutableListOf<Task>()

                        for (taskRef in selectedTeam!!.teamTasks) {
                            db.collection("Tasks").document(taskRef).get()
                                .addOnSuccessListener { doc ->
                                    teamTasks.add(doc.toObject(Task::class.java)!!)
                                }
                        }

                        taskViewModel.showingTasks = teamTasks.toMutableStateList()
                        TeamTaskListPane(
                            taskViewModel.showingTasks,
                            taskViewModel::clearTaskInformation,
                            actions.createTaskPaneFromTeam,
                            actions.showTaskDetailsPane,
                            actions.editTaskPane,
                            selectedTeam!!,
                            teamViewModel.allTeams
                        )
                    }
                }

                composable("CreateTeamPane") {
                    CreateTeamPane(
                        teamViewModel.teamNameValue,
                        teamViewModel.teamNameError,
                        teamViewModel::setTeamName,
                        teamViewModel.teamDescriptionValue,
                        teamViewModel::setTeamDescription,
                        teamViewModel.teamCategoryValue,
                        teamViewModel.teamCategoryError,
                        teamViewModel::setTeamCategory,
                        teamViewModel.teamImageValue,
                        teamViewModel::setTeamImage
                    )
                }

                composable("TeamListPane") {

                    TeamListPane(
                        teamViewModel.showingTeams,
                        teamViewModel.allTeams,
                        teamViewModel::clearTeamInformation,
                        actions.createTeamPane,
                        actions.showTeamDetailsPane,
                        actions.acceptInvitationPane,
                        db
                    )
                }

                composable(
                    "ShowTeamDetailsPane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == (entry.arguments!!.getString("teamId")!!) }

                    // teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }

                    if (selectedTeam != null) {
                        teamViewModel.setTeamInformation(selectedTeam!!)

                        ShowTeamDetailsPane(
                            team = selectedTeam!!,
                            teamTaskListPane = actions.teamTaskListPane,
                            userChatPane = actions.userChatPane,
                            showUserInformationPane = actions.showUserInformationPane,
                            actions.teamListPane,
                            db
                        )


                    }


                }

                composable("ShareTeamPane") {
                    if (selectedTeam != null) {

                        ShareTeamPane(userViewModel.allUsers, selectedTeam!!, db)

                    }
                }

                composable(
                    "EditTeamPane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }
                    if (selectedTeam != null) {
                        EditTeamPane(
                            teamPhotoValue = teamViewModel.teamImageValue,
                            setTeamPhoto = teamViewModel::setTeamImage,
                            teamNameValue = teamViewModel.teamNameValue,
                            teamNameError = teamViewModel.teamNameError,
                            setTeamName = teamViewModel::setTeamName,
                            teamDescriptionValue = teamViewModel.teamDescriptionValue,
                            setTeamDescription = teamViewModel::setTeamDescription,
                            teamCategoryValue = teamViewModel.teamCategoryValue,
                            teamCategoryError = teamViewModel.teamCategoryError,
                            setTeamCategory = teamViewModel::setTeamCategory
                        )
                    }
                }

                composable(
                    "FeedbackPerformancePane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }
                    if (selectedTeam != null) {
                        FeedbackPerformancePane(
                            teamViewModel.userSelected,
                            selectedTeam!!,
                            db,
                        )
                    }
                }

                composable(
                    "ShowTeamChatPane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }
                    if (selectedTeam != null) {
                        ShowTeamChatPane(
                            selectedTeam = selectedTeam!!,
                            chat = teamViewModel.teamChatValue,
                            setChat = teamViewModel::setTeamChat,
                            messageValue = teamViewModel.teamMessageValue,
                            setMessage = teamViewModel::setTeamMessage,
                            showUserInformationPane = actions.showUserInformationPane,
                            db = db
                        )
                    }
                }

                composable(
                    "UserChatPane/{userNickname}",
                    arguments = listOf(navArgument("userNickname") { type = NavType.StringType })
                ) { entry ->
                    val parameterNickname = entry.arguments?.getString("userNickname")

                    db.collection("Users")
                        .document(parameterNickname!!).get()
                        .addOnSuccessListener { d ->
                            if (d != null) {

                                selectedUser.userImage = d.get("userImage").toString()
                                selectedUser.userNickname = d.get("userNickname").toString()
                                selectedUser.userStatus = d.get("userStatus").toString()
                                selectedUser.userFirstName = d.get("userFirstName").toString()
                                selectedUser.userLastName = d.get("userLastName").toString()
                                selectedUser.userBirthDate = d.get("userBirthDate").toString()
                                selectedUser.userDescription = d.get("userDescription").toString()
                                selectedUser.userLocation = d.get("userLocation").toString()
                                selectedUser.userMail = d.get("userMail").toString()
                                selectedUser.userStatus = d.get("userStatus").toString()
                                selectedUser.userChat = d.get("userChat") as MutableList<String>
                                selectedUser.userTeams = d.get("userTeams") as MutableList<String>
                                selectedUser.userTasks = d.get("userTasks") as MutableList<String>

                                userViewModel.setProfileInformation(selectedUser)
                                userViewModel.setOldProfileInformation()

                            } else {
                                //nickname non esistente
                            }
                        }.addOnFailureListener { exeception ->
                            Log.d("DB exception", exeception.toString())
                        }

                        if(loggedUser.userNickname != selectedUser.userNickname) {
                            UserChatPane(
                                selectedUser,
                                userViewModel.userMessageValue,
                                userViewModel::setUserMessage,
                                actions.showUserInformationPane,
                                db
                            )
                        }

                }


                composable("UserChatListPane") {
                    UserChatListPane(
                        userChat = loggedUser.userChat,
                        userChatPane = actions.userChatPane,
                        db = db
                    )
                }

                composable("HomePane") {
                    if(loggedUser.userTeams.size == teamViewModel.allTeams.size && loggedUser.userTasks.size == taskViewModel.allTasks.size) {
                        HomePane(
                            actions.showTaskDetailsPane,
                            actions.showTeamDetailsPane,
                            actions.acceptInvitationPane,
                            db,
                            sharedPreferences = sharedPreferences,
                            taskViewModel.allTasks,
                            teamViewModel.allTeams
                        )
                    }
                }


                composable("LoginPane") {
                    LoginPane(
                        userLoginValue = userViewModel.userLoginValue,
                        userLoginError = userViewModel.userLoginError,
                        setUserLogin = userViewModel::setUserLogin,
                        passwordValue = userViewModel.passwordValue,
                        passwordError = userViewModel.passwordError,
                        setPassword = userViewModel::setPassword,
                        authError = userViewModel.authError,
                        validateLogin = userViewModel::validateLogin,
                        validateLoginWithGoogle = userViewModel::validateLoginWithGoogle,
                        sharedPreferences = sharedPreferences,
                        homePane = actions.homePane,
                        signUpPane = actions.signUpPane
                    )
                }


                composable("SignUpPane") {
                    SignUpPane(
                        userViewModel.photoValue,
                        userViewModel::setPhoto,
                        userViewModel.firstNameValue,
                        userViewModel.firstNameError,
                        userViewModel::setFirstName,
                        userViewModel.lastNameValue,
                        userViewModel.lastNameError,
                        userViewModel::setLastName,
                        userViewModel.nicknameValue,
                        userViewModel.nicknameError,
                        userViewModel::setNickname,
                        userViewModel.mailValue,
                        userViewModel.mailError,
                        userViewModel::setMail,
                        userViewModel::openConfirmMailPane,
                        userViewModel.locationValue,
                        userViewModel.locationError,
                        userViewModel::setLocation,
                        userViewModel.descriptionValue,
                        userViewModel::setDescription,
                        userViewModel.birthDateValue,
                        userViewModel.birthDateError,
                        userViewModel::setBirthDate,
                        userViewModel.statusValue,
                        userViewModel::setStatus,
                        userViewModel::changingPassword,
                        actions.confirmMailPane,
                        actions.editPasswordPane,
                        actions.loginPane,
                        actions.setProfilePane,
                        userViewModel::setOldProfileInformation
                    )
                }


                composable("SetProfilePane") {
                    SetProfilePane(
                        userViewModel.photoValue,
                        userViewModel::setPhoto,
                        userViewModel.firstNameValue,
                        userViewModel.firstNameError,
                        userViewModel::setFirstName,
                        userViewModel.lastNameValue,
                        userViewModel.lastNameError,
                        userViewModel::setLastName,
                        userViewModel.nicknameValue,
                        userViewModel.nicknameError,
                        userViewModel::setNickname,
                        userViewModel.mailValue,
                        userViewModel.locationValue,
                        userViewModel.locationError,
                        userViewModel::setLocation,
                        userViewModel.descriptionValue,
                        userViewModel::setDescription,
                        userViewModel.birthDateValue,
                        userViewModel.birthDateError,
                        userViewModel::setBirthDate,
                        userViewModel.statusValue,
                        userViewModel::setStatus,
                    )
                }


                val uri = "https://www.beedone.com"

                composable(
                    route = "/invite/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType }),
                    deepLinks = listOf(NavDeepLink("$uri/invite/{teamId}"))
                ) { navBackStackEntry ->
                    val teamId = navBackStackEntry.arguments?.getString("teamId")

                    if (teamId != null) {
                        InviteUser(
                            teamViewModel.allTeams,
                            teamId,
                            loggedUser,
                            actions.showTeamDetailsPane,
                            actions.homePane,
                            actions.acceptInvitationPane,
                            db
                        )
                    }
                }


                composable(
                    route = "AcceptInvitationPane/{teamId}",
                    arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                ) { entry ->
                    selectedTeam =
                        teamViewModel.allTeams.find { it.teamId == entry.arguments?.getString("teamId") }

                    InvitedPane(
                        actions.showTeamDetailsPane,
                        actions.navigateBack,
                        actions.teamListPane,
                        selectedTeam!!,
                        db
                    )
                }
            }
        }
    }
}


class Actions(
    val navController: NavHostController
) {
    val editUserInformationPane: () -> Unit = {
        navController.navigate("EditUserInformationPane")
    }

    val editPasswordPane: () -> Unit = {
        navController.navigate("EditPasswordPane")
    }

    val showUserInformationPane: (String) -> Unit = { userNickname ->
        navController.navigate("ShowUserInformationPane/${userNickname}")
    }

    val confirmMailPane: () -> Unit = {
        navController.navigate("ConfirmMailPane")
    }

    val personalTaskListPane: () -> Unit = {
        navController.navigate("PersonalTaskListPane")
    }

    val createTaskPane: () -> Unit = {
        navController.navigate("CreateTaskPane")
    }

    val createTaskPaneFromTeam: (String) -> Unit = { teamId ->
        navController.navigate("CreateTaskPane/${teamId}")
    }

    val showTaskDetailsPane: (String) -> Unit = { taskId ->
        navController.navigate("ShowTaskDetailsPane/${taskId}")
    }

    val editTaskPane: (String) -> Unit = { taskId ->
        navController.navigate("EditTaskPane/${taskId}")
    }

    val showTaskQuestionsPane: (String) -> Unit = { taskId ->
        navController.navigate("ShowTaskQuestionsPane/${taskId}")
    }

    val showTaskAnswersPane: (String) -> Unit = { questionId ->
        navController.navigate("ShowTaskAnswersPane/${questionId}")
    }

    val showTaskAttachmentsPane: () -> Unit = {
        navController.navigate("ShowTaskAttachmentsPane")
    }

    val historyPane: () -> Unit = {
        navController.navigate("HistoryPane")
    }

    val taskTimerPane: () -> Unit = {
        navController.navigate("TaskTimerPane")
    }

    val teamTaskListPane: (String) -> Unit = { teamId ->
        navController.navigate("TeamTaskListPane/${teamId}")
    }

    val createTeamPane: () -> Unit = {
        navController.navigate("CreateTeamPane")
    }

    val teamListPane: () -> Unit = {
        navController.navigate("TeamListPane")
    }

    val showTeamDetailsPane: (String) -> Unit = { teamId ->
        navController.navigate("ShowTeamDetailsPane/${teamId}")
    }

    val shareTeamPane: () -> Unit = {
        navController.navigate("ShareTeamPane")
    }

    val editTeamPane: (String) -> Unit = { teamId ->
        navController.navigate("EditTeamPane/${teamId}")
    }

    val feedbackPerformancePane: (String) -> Unit = { teamId ->
        navController.navigate("FeedbackPerformancePane/${teamId}")
    }

    val showTeamChatPane: (String) -> Unit = { teamId ->
        navController.navigate("ShowTeamChatPane/${teamId}")
    }

    val userChatPane: (String) -> Unit = { userNickname ->
        navController.navigate("UserChatPane/${userNickname}")
    }

    val userChatListPane: () -> Unit = {
        navController.navigate("UserChatListPane")
    }

    val homePane: () -> Unit = {
        navController.navigate("HomePane")
    }

    val invitePane: (String) -> Unit = { teamId ->
        navController.navigate("/invite/${teamId}")
    }

    val acceptInvitationPane: (String) -> Unit = { teamId ->
        navController.navigate("AcceptInvitationPane/${teamId}")
    }

    val loginPane: () -> Unit = {
        navController.navigate("LoginPane")
    }

    val signUpPane: () -> Unit = {
        navController.navigate("SignUpPane")
    }

    val setProfilePane: () -> Unit = {
        navController.navigate("SetProfilePane")
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }

    val navigateBackShowDetails: () -> Unit = {
        navController.popBackStack()
        navController.navigate("HomePane")
    }
}

