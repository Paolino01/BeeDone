package it.polito.BeeDone.task.timer

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.R
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnrememberedMutableState")
@Composable
fun ShowTimer(
    task: Task,
    taskTimerTitleValue: String,
    setTaskTimerTitle: (String) -> Unit,
    taskTimerValue: String,
    setTaskTimer: (String) -> Unit,
    taskTimerHistory: MutableList<TaskTimer>,
    addTaskTimerHistory: (Int, Task, FirebaseFirestore) -> Unit,
    showUserInformationPane: (String) -> Unit,
    taskUsersValue: MutableList<User>,
    selectedTask: Task,
    db: FirebaseFirestore
) {
    var taskTimerTitleError by remember { mutableStateOf("") }
    var timerRunning by remember { mutableStateOf(false) }
    var ticks by remember { mutableIntStateOf(0) }
    val state = rememberScrollState()

    var u: User

    Box(
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            Modifier.verticalScroll(state)
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Total Task Time: ",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${(taskTimerHistory.sumOf{t -> t.ticks} / 3600)}:${
                            (taskTimerHistory.sumOf{t -> t.ticks} / 60).toString().padStart(2, '0')
                        }:${(taskTimerHistory.sumOf{t -> t.ticks} % 60).toString().padStart(2, '0')}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                //Show timer history
                for (t in taskTimerHistory) {
                    u = taskUsersValue.find { it.userNickname == t.user }!!     //When we arrive here, taskUsersValue is surely populated with the users of the task

                    Row(
                        modifier = Modifier
                            .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = {
                                    showUserInformationPane(u.userNickname)
                                })
                        ) {
                            CreateImage(
                                photo = if(u.userImage == null) null else u.userImage!!.toUri(),
                                name = "${u.userFirstName} ${u.userLastName}",
                                size = 30
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(10f)
                                .padding(top = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = u.userNickname,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            showUserInformationPane(u.userNickname)
                                        })
                                        .weight(4.2f)
                                )

                                Text(
                                    text = t.title,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(2.5f)
                                )

                                Column (
                                    modifier = Modifier.weight(3.3f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${(t.ticks / 3600)}:${
                                            (t.ticks / 60).toString().padStart(2, '0')
                                        }:${(t.ticks % 60).toString().padStart(2, '0')}",
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = t.date
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Color(
                            250, 250, 250
                        )
                    )   //Same color as the background
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(100.dp),                                     //Fixed height, so that I can have the exact padding at the top of the scrollable column
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    modifier = Modifier.weight(6f),
                    verticalArrangement = Arrangement.Center
                ) {
                    CreateTextFieldError(
                        value = taskTimerTitleValue,
                        error = taskTimerTitleError,
                        setValue = setTaskTimerTitle,
                        label = "I am working on...",
                        keyboardType = KeyboardType.Text,
                        maxLines = 1
                    )
                }

                //Timer text
                Text(
                    text = taskTimerValue,
                    modifier = Modifier
                        .weight(3f)
                        .offset(x = 7.dp),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                IconButton(
                    onClick = {
                        if (!timerRunning) {

                            if (taskTimerTitleValue.isNotBlank()) {
                                taskTimerTitleError = ""
                                timerRunning = true
                            } else {
                                taskTimerTitleError = "This field cannot be blank"
                            }
                        } else {
                            timerRunning = false
                            addTaskTimerHistory(ticks, selectedTask, db)
                            //TODO: update to DB the new timer information
                            //task.taskTimerHistory = taskTimerHistory.map{it.timerId}.toMutableList()
                            setTaskTimer("0:00:00")
                            ticks = 0
                            setTaskTimerTitle("")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .border(4.dp, lightBlue, CircleShape)
                ) {
                    if (!timerRunning) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Start timer",
                            modifier = Modifier.size(50.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.stop_icon),
                            contentDescription = "Stop timer",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                if (timerRunning) {
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1.seconds)
                            ticks++
                            setTaskTimer(
                                "${(ticks / 3600)}:${
                                    (ticks / 60).toString().padStart(2, '0')
                                }:${(ticks % 60).toString().padStart(2, '0')}"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * This function resets all the values of the timer pane. It is called when we exit from this pane
 */
fun exitTimerPane(
    setTaskTimerTitle: (String) -> Unit,
    setTaskTimer: (String) -> Unit
) {
    setTaskTimerTitle("")
    setTaskTimer("0:00:00")
}