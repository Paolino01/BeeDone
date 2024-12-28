package it.polito.BeeDone.task

import android.os.Build
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.task.history.Event
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.utils.CreateAddSubtaskSection
import it.polito.BeeDone.utils.CreateClickableCreatorText
import it.polito.BeeDone.utils.CreateClickableTeamText
import it.polito.BeeDone.utils.CreateClickableUserText
import it.polito.BeeDone.utils.CreateRowText
import it.polito.BeeDone.utils.CreateTaskKPI
import it.polito.BeeDone.utils.CreateViewSubtasksSection
import it.polito.BeeDone.utils.lightBlue
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date


@Composable
fun ShowTaskDetailsMenu(
    showTaskQuestionsPane: (String) -> Unit,
    showTaskAttachmentsPane: () -> Unit,
    historyPane: () -> Unit,
    selectedTask: Task
) {
    var menuVisible by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuVisible = !menuVisible }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "choice details",
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
                        text = "Details",
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

                Row {
                    Button(
                        onClick = {
                            menuVisible = false
                            showTaskQuestionsPane(selectedTask.taskId)
                        },
                        modifier = Modifier
                            .padding(10.dp, 1.dp)
                            .height(40.dp)
                            .width(180.dp),
                        shape = ButtonDefaults.shape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "\uD83D\uDCAC Q&A",
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            menuVisible = false
                            //open Attachments page
                            showTaskAttachmentsPane()
                        },
                        modifier = Modifier
                            .padding(10.dp, 1.dp)
                            .height(40.dp)
                            .width(180.dp),
                        shape = ButtonDefaults.shape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "\uD83D\uDCC1 Attachments",
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            menuVisible = false
                            historyPane()
                        },
                        modifier = Modifier
                            .padding(10.dp, 1.dp)
                            .height(40.dp)
                            .width(180.dp),
                        shape = ButtonDefaults.shape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "\uD83D\uDD5D History",
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ShowTaskDetailsPane(
    selectedTask: Task,
    addTaskEventToHistory: (Event) -> Unit,
    setTaskCompleted: () -> Unit,
    statusTaskVm: String,
    subtaskListVm: MutableList<Subtask>,    //taskSubtasksValue in GUI
    addSubtaskToTask: (Subtask) -> Unit,    //addTaskSubtasks in GUI
    editTaskPane: (String) -> Unit,
    taskTimerPane: () -> Unit,
    showTeamDetailsPane: (String) -> Unit,
    showUserInformationPane: (String) -> Unit,
    allTeams: SnapshotStateList<Team>,
    db: FirebaseFirestore
) {
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {  //True if the screen is in portrait mode
            //VERTICAL
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(horizontal = 16.dp),     //Padding is needed in order to leave 16dp from left and right borders
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    //Task title
                    CreateRowText(contentDescription = "Title", text = selectedTask.taskTitle)

                    //Description
                    CreateRowText(contentDescription = "Description", text = selectedTask.taskDescription)

                    //Creation date
                    CreateRowText(contentDescription = "Created", text = selectedTask.taskCreationDate)

                    //Deadline
                    CreateRowText(contentDescription = "Deadline", text = selectedTask.taskDeadline)

                    //Status
                    CreateRowText(contentDescription = "Status", text = statusTaskVm.toString())


                    //Task KPI
                    if (subtaskListVm.size > 0) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .padding(horizontal = 10.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CreateTaskKPI(
                                subtaskListVm.filter { it.subtaskState == "Completed" }.size,
                                subtaskListVm.size
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        HorizontalDivider(
                            Modifier.padding(20.dp, 0.dp),
                            thickness = Dp.Hairline,
                            color = Color.Gray
                        )
                    }

                    //Team
                    CreateClickableTeamText(contentDescription = "Team", taskTeam = selectedTask.taskTeam, showTeamDetailsPane, allTeams)

                    //Creator
                    CreateClickableCreatorText(contentDescription = "Created by", creator = selectedTask.taskCreator, showUserInformationPane = showUserInformationPane, db)

                    //User/users
                    CreateClickableUserText(
                        contentDescription = "Users",
                        taskUsers = selectedTask.taskUsers,
                        showUserInformationPane = showUserInformationPane,
                        db
                    )

                    //Category
                    CreateRowText(contentDescription = "Category", text = selectedTask.taskCategory)

                    //Tag
                    CreateRowText(contentDescription = "Tag", text = selectedTask.taskTag)

                    //Repeat
                    CreateRowText(
                        contentDescription = "Repeat", text = selectedTask.taskRepeat.toString()
                    ) //because taskRepeatValue is of type Repeat and I need a string

                    Spacer(modifier = Modifier.height(30.dp))

                    if (statusTaskVm != "Completed" && statusTaskVm != "Expired Completed"
                        && (selectedTask.taskUsers.contains(loggedUser.userNickname) || selectedTask.taskCreator== loggedUser.userNickname))  {
                        //Add subtasks
                        Row(
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp, Color.LightGray, shape = RoundedCornerShape(13.dp)
                                    )
                                    .fillMaxHeight()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .padding(10.dp, 4.dp)
                            ) {
                                CreateAddSubtaskSection(
                                    selectedTask, subtaskListVm, addTaskEventToHistory, addSubtaskToTask, db
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))
                    }

                    //List of subtasks
                    Row(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column {
                            Column(
                                modifier = Modifier
                                    .border(
                                        1.dp, Color.LightGray, shape = RoundedCornerShape(13.dp)
                                    )
                                    .fillMaxHeight()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .padding(10.dp, 4.dp)
                            ) {
                                CreateViewSubtasksSection(
                                    selectedTask, subtaskListVm, addTaskEventToHistory, db
                                )
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                    if (statusTaskVm != "Completed" && statusTaskVm != "Expired Completed"
                        && (selectedTask.taskUsers.contains(loggedUser.userNickname) || selectedTask.taskCreator== loggedUser.userNickname)) {
                        //button section
                        Row(modifier = Modifier.fillMaxHeight()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .padding(2.dp, 4.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        editTaskPane(selectedTask.taskId)  //go to edit task pane
                                    },
                                    shape = RoundedCornerShape(30.dp),
                                    containerColor = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                ) {
                                    Text(
                                        text = "Modify task",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(16.dp, 1.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(0.05f))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .padding(2.dp, 4.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        taskTimerPane()
                                    },
                                    shape = RoundedCornerShape(30.dp),
                                    containerColor = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                ) {
                                    Text(text = "Track working time")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))


                        //button for completed tasks
                        Row(modifier = Modifier.fillMaxHeight()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .padding(2.dp, 4.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        setTaskCompleted()

                                        val taskHistoryToAdd = hashMapOf(
                                            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                            "taskChanges" to mutableListOf<String>(),
                                            "taskDoneSubtasks" to subtaskListVm.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                            "taskStatus" to selectedTask.taskStatus,
                                            "taskTotalSubtasks" to subtaskListVm.size.toString(),
                                            "title" to "Task Completed",
                                            "user" to loggedUser.userNickname
                                        )

                                        db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                                            selectedTask.taskHistory.add(taskHistoryRef.id)
                                            db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                                        }

                                        if(LocalDate.parse(selectedTask.taskDeadline, DateTimeFormatter.ofPattern("dd/MM/uuuu")) < LocalDate.now()) {
                                            selectedTask.taskStatus = "Expired Completed"

                                            db.collection("Tasks").document(selectedTask.taskId).update("taskStatus", "Expired Completed")
                                        }
                                        else {
                                            selectedTask.taskStatus = "Completed"

                                            db.collection("Tasks").document(selectedTask.taskId).update("taskStatus", "Completed")
                                        }
                                        //task.taskStatus = TaskStatus.Completed
                                        //changeScreen(Pane.ShowTaskDetailsPane)
                                    }, //task completed
                                    shape = RoundedCornerShape(30.dp),
                                    containerColor = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                ) {
                                    Text(
                                        text = "Task Completed",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(16.dp, 1.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            //HORIZONTAL
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Column {
                        Spacer(modifier = Modifier.height(10.dp))

                        //Task title
                        CreateRowText(contentDescription = "Title", text = selectedTask.taskTitle)

                        //Description
                        CreateRowText(contentDescription = "Description", text = selectedTask.taskDescription)

                        //Creation date
                        CreateRowText(contentDescription = "Created", text = selectedTask.taskCreationDate)

                        //Deadline
                        CreateRowText(contentDescription = "Deadline", text = selectedTask.taskDeadline)

                        //Status
                        CreateRowText(contentDescription = "Status", text = statusTaskVm.toString())

                        //Task KPI
                        if (selectedTask.taskSubtasks.size > 0) {


                            Column(
                                modifier = Modifier
                                    .border(
                                        1.dp, Color.Gray, RoundedCornerShape(25.dp)
                                    )
                                    .padding(horizontal = 10.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CreateTaskKPI(
                                    subtaskListVm.filter { it.subtaskState == "Completed" }.size,
                                    subtaskListVm.size
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        } else {
                            HorizontalDivider(
                                Modifier.padding(20.dp, 0.dp),
                                thickness = Dp.Hairline,
                                color = Color.Gray
                            )
                        }

                        //Team
                        CreateClickableTeamText(contentDescription = "Team", taskTeam = selectedTask.taskTeam, showTeamDetailsPane, allTeams)

                        //Creator
                        CreateClickableCreatorText(contentDescription = "Created by", creator = selectedTask.taskCreator, showUserInformationPane = showUserInformationPane, db)

                        //User/users
                        CreateClickableUserText(
                            contentDescription = "Users",
                            taskUsers = selectedTask.taskUsers,
                            showUserInformationPane = showUserInformationPane,
                            db
                        )

                        //Category
                        CreateRowText(contentDescription = "Category", text = selectedTask.taskCategory)

                        //Tag
                        CreateRowText(contentDescription = "Tag", text = selectedTask.taskTag)

                        //Repeat
                        CreateRowText(
                            contentDescription = "Repeat", text = selectedTask.taskRepeat.toString()
                        ) //because taskRepeatValue is of type Repeat and I need a string

                        Spacer(modifier = Modifier.height(30.dp))

                        if (statusTaskVm != "Completed" && statusTaskVm != "Expired Completed"
                            && (selectedTask.taskUsers.contains(loggedUser.userNickname) || selectedTask.taskCreator== loggedUser.userNickname))  {
                            //Add subtasks
                            Row(
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp, Color.LightGray, shape = RoundedCornerShape(13.dp)
                                        )
                                        .fillMaxHeight()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .padding(start = 25.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    CreateAddSubtaskSection(
                                        selectedTask, subtaskListVm, addTaskEventToHistory, addSubtaskToTask, db
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))
                        }

                        //List of subtasks
                        Row(
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Column {
                                Column(
                                    modifier = Modifier
                                        .border(
                                            1.dp, Color.LightGray, shape = RoundedCornerShape(13.dp)
                                        )
                                        .fillMaxHeight()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .padding(start = 25.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    CreateViewSubtasksSection(
                                        selectedTask, subtaskListVm, addTaskEventToHistory, db
                                    )
                                }
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }

                        if (statusTaskVm != "Completed" && statusTaskVm != "Expired Completed"
                            && (selectedTask.taskUsers.contains(loggedUser.userNickname) || selectedTask.taskCreator == loggedUser.userNickname)) {
                            //button section
                            Row(modifier = Modifier.fillMaxHeight()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .padding(2.dp, 4.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            editTaskPane(selectedTask.taskId)  //go to edit task pane
                                        },
                                        shape = RoundedCornerShape(30.dp),
                                        containerColor = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                    ) {
                                        Text(
                                            text = "Modify task",
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(16.dp, 1.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.weight(0.05f))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .padding(2.dp, 4.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            taskTimerPane()
                                        },
                                        shape = RoundedCornerShape(30.dp),
                                        containerColor = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                    ) {
                                        Text(text = "Track working time")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))


                            //button for completed tasks
                            Row(modifier = Modifier.fillMaxHeight()) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .padding(2.dp, 4.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            setTaskCompleted()
                                            if(LocalDate.parse(selectedTask.taskDeadline, DateTimeFormatter.ofPattern("dd/MM/uuuu")) < LocalDate.now())
                                                selectedTask.taskStatus= "Expired Completed"
                                            else
                                                selectedTask.taskStatus="Completed"
                                        }, //task completed
                                        shape = RoundedCornerShape(30.dp),
                                        containerColor = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                    ) {
                                        Text(
                                            text = "Task Completed",
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(16.dp, 1.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}