package it.polito.BeeDone.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.R
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.task.Subtask
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.task.history.Event
import it.polito.BeeDone.task.setState
import java.text.SimpleDateFormat
import java.util.Date

var subtaskValue by mutableStateOf("")
    private set
var subtaskTitleError by mutableStateOf("")
    private set

/**
Manages the KPIs. Shows a StackedBarChart with information about the
subtasks completed of a specific task
 */
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CreateTaskKPI(subtaskDone: Int, totalSubtask: Int) {

    Text(
        text = "Subtasks completed/Subtasks not completed",
        fontSize = 16.sp,
        fontFamily = FontFamily.SansSerif,
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))
    StackedBar(subtaskDone, totalSubtask)
    Spacer(modifier = Modifier.height(4.dp))
}

@SuppressLint("SimpleDateFormat")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateAddSubtaskSection(
    selectedTask: Task,
    taskSubtasks: MutableList<Subtask>,
    addTaskEventToHistory: (Event) -> Unit,
    addSubtaskToTask: (Subtask) -> Unit,
    db: FirebaseFirestore
) {
    var showPopUp by remember { mutableStateOf(false) } // -> STATE

    Row(
        modifier = Modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Add subtask",
            modifier = Modifier
                .weight(1f)
                .padding(14.dp, 1.dp),
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Left
        )

        FloatingActionButton(
            onClick = {
                showPopUp = !showPopUp
                subtaskTitleError = "" //so if I open the popup after a failed attempt the error message doesn't remain
            },
            containerColor = Color.DarkGray,
            shape = CircleShape,
            modifier = Modifier
                .padding(14.dp, 2.dp)
                .size(14.dp), //padding first so I move it away from the edge and then reduce the size
        ) {
            Icon(Icons.Default.Add, "Add subtask", tint = Color.White)
        }

        //Popup, it opens when the user taps on the add icon
        if(showPopUp) {
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
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Insert data",
                                    color = Color.Black,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(10.dp, 1.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Left
                                )

                                Spacer(modifier = Modifier.height(15.dp))

                                OutlinedTextField(
                                    value = subtaskValue,
                                    onValueChange = {
                                        subtaskValue = it
                                        subtaskTitleError = "" // Reset error when user changes the input
                                    },
                                    label = { Text("Title *") },
                                    isError = subtaskTitleError.isNotBlank(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    shape = myShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = lightBlue,
                                        focusedLabelColor = lightBlue,
                                        focusedTextColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (subtaskTitleError.isNotBlank()) {
                                    Text(subtaskTitleError, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Right)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                FloatingActionButton(
                                    onClick = {
                                        //add subtask
                                        if (subtaskValue.isNotEmpty()) {
                                            val tmpSub=Subtask("", subtaskValue, "Not Completed")

                                            val subtaskToAdd = hashMapOf(
                                                "subtaskState" to "Not Completed",
                                                "subtaskTitle" to subtaskValue
                                            )

                                            db.collection("Subtasks").add(subtaskToAdd).addOnSuccessListener { subtaskRef ->
                                                tmpSub.subtaskId = subtaskRef.id
                                                db.collection("Subtasks").document(subtaskRef.id).update("subtaskId", subtaskRef.id)
                                                selectedTask.taskSubtasks.add(subtaskRef.id)
                                                db.collection("Tasks").document(selectedTask.taskId).update("taskSubtasks", selectedTask.taskSubtasks)
                                            }

                                            addSubtaskToTask(tmpSub)

                                            addTaskEventToHistory(
                                                Event(
                                                    "Subtask added",
                                                    SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                                    selectedTask.taskStatus,
                                                    loggedUser.userNickname,
                                                    taskSubtasks.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                                    taskSubtasks.size.toString(),
                                                    mutableListOf("Added '$subtaskValue' subtask")
                                                )
                                            )

                                            val taskHistoryToAdd = hashMapOf(
                                                "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                                "taskChanges" to mutableListOf("Added '$subtaskValue' subtask"),
                                                "taskDoneSubtasks" to taskSubtasks.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                                "taskStatus" to selectedTask.taskStatus,
                                                "taskTotalSubtasks" to taskSubtasks.size.toString(),
                                                "title" to "Subtask added",
                                                "user" to loggedUser.userNickname
                                            )

                                            db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                                                selectedTask.taskHistory.add(taskHistoryRef.id)
                                                db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                                            }

                                            subtaskValue = "" // Reset text field after adding subtask
                                            showPopUp = false
                                        } else {
                                            subtaskTitleError = "The subtask title cannot be empty"
                                        }
                                    },
                                    containerColor = Color.White,
                                    shape = RoundedCornerShape(30.dp),
                                    modifier = Modifier
                                        .border(2.dp, lightBlue, RoundedCornerShape(30.dp))
                                        .width(120.dp)
                                ) {
                                    Text(text = "Add subtask", color = Color.Black)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * List of subtasks
 * Creates a field that shows the list of subtasks assigned to a task. Via a Dropdown Menu,
 * it's possible to mark as completed or remove subtasks
 */
@SuppressLint("SimpleDateFormat")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateViewSubtasksSection(
    selectedTask: Task,
    taskSubtasks: MutableList<Subtask>,
    addTaskEventToHistory: (Event) -> Unit,
    db: FirebaseFirestore
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    var showPopUp by remember { mutableStateOf(false) }
    var subtaskIndexToRemove by remember { mutableIntStateOf(-1) } //I save the index of the item to remove it from the list
    var titleSubtask by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "List of subtasks",
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(14.dp, 2.dp),
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Left
        )

        FloatingActionButton(
            onClick = {
                setExpanded(!expanded)
            },
            containerColor = Color.DarkGray,
            shape = CircleShape,
            modifier = Modifier
                .padding(14.dp, 1.dp)
                .size(14.dp), //padding first so I move it away from the edge and then reduce the size
        ) {
            if (expanded) {
                Icon(Icons.Default.KeyboardArrowUp, "Hide view subtask", tint = Color.White)
            }else{
                Icon(Icons.Default.KeyboardArrowDown, "View subtask", tint = Color.White)
            }
        }
    }

    if (expanded) {
        for (i in 0..<taskSubtasks.size) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = taskSubtasks[i].subtaskTitle,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(8.dp)
                )

                //I check whether the status of the subtask is completed
                //or not, and show the respective icon
                if (taskSubtasks[i].subtaskState == "Not Completed") {
                    IconButton(onClick = {
                        showPopUp = !showPopUp
                        subtaskIndexToRemove = i
                        titleSubtask = taskSubtasks[i].subtaskTitle
                    }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notcompleted_subtasks),
                            contentDescription = "Subtask options",
                            tint = Color.Blue,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else {
                    IconButton(onClick = {
                        showPopUp = false //If it is completed I don't show the popup
                    }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_completed_subtasks),
                            contentDescription = "Subtask options"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }

    /**
     * Shows a pop-up to remove a subtask from a task or mark it as completed
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
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = titleSubtask,
                                color = Color.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(10.dp, 1.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Left
                            )

                            Button(
                                onClick = {
                                    setState(taskSubtasks[subtaskIndexToRemove])

                                    addTaskEventToHistory(
                                        Event(
                                            "Subtask completed",
                                            SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                            selectedTask.taskStatus,
                                            loggedUser.userNickname,
                                            taskSubtasks.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                            taskSubtasks.size.toString(),
                                            mutableListOf("Completed '$titleSubtask' subtask")
                                        )
                                    )

                                    val taskHistoryToAdd = hashMapOf(
                                        "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                        "taskChanges" to mutableListOf("Completed '$titleSubtask' subtask"),
                                        "taskDoneSubtasks" to taskSubtasks.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                        "taskStatus" to selectedTask.taskStatus,
                                        "taskTotalSubtasks" to taskSubtasks.size.toString(),
                                        "title" to "Subtask completed",
                                        "user" to loggedUser.userNickname
                                    )

                                    db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                                        selectedTask.taskHistory.add(taskHistoryRef.id)
                                        db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                                    }

                                    db.collection("Subtasks").document(taskSubtasks[subtaskIndexToRemove].subtaskId).update("subtaskState", "Completed")

                                    showPopUp = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.border(2.dp, lightBlue, ButtonDefaults.shape)
                            ) {
                                Text(text = "Mark as completed", color = Color.Black)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    db.collection("Subtasks").document(taskSubtasks[subtaskIndexToRemove].subtaskId).get().addOnSuccessListener { d ->
                                        d.reference.delete()
                                    }

                                    addTaskEventToHistory(
                                        Event(
                                            "Subtask removed",
                                            SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                            selectedTask.taskStatus,
                                            loggedUser.userNickname,
                                            taskSubtasks.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                            taskSubtasks.size.toString(),
                                            mutableListOf("Removed '$titleSubtask' subtask")
                                        )
                                    )

                                    val taskHistoryToAdd = hashMapOf(
                                        "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                        "taskChanges" to mutableListOf("Removed '$subtaskValue' subtask"),
                                        "taskDoneSubtasks" to taskSubtasks.filter {s -> s.subtaskState == "Completed"}.size.toString(),
                                        "taskStatus" to selectedTask.taskStatus,
                                        "taskTotalSubtasks" to taskSubtasks.size.toString(),
                                        "title" to "Subtask removed",
                                        "user" to loggedUser.userNickname
                                    )

                                    db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                                        selectedTask.taskHistory.add(taskHistoryRef.id)
                                        db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                                    }

                                    selectedTask.taskSubtasks.remove(taskSubtasks[subtaskIndexToRemove].subtaskId)
                                    taskSubtasks.remove(taskSubtasks[subtaskIndexToRemove])

                                    db.collection("Tasks").document(selectedTask.taskId).update("taskSubtasks", selectedTask.taskSubtasks).addOnSuccessListener { showPopUp = false }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.border(2.dp, Color.Red, ButtonDefaults.shape)
                            ) {
                                Text(text = "Remove from task", color = Color.Black)
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
    // }
    // Spacer(modifier = Modifier.height(30.dp))
}

/*  var isExpanded by remember { mutableStateOf(false) } //Used to decide wether the DropDown is expanded or not
  var showPopUp by remember { mutableStateOf(false) }
  var subtask by remember { mutableStateOf<Subtask?>(null) } //I save the item to remove it from the list
  var titleSubtask by remember { mutableStateOf("") }

  ExposedDropdownMenuBox(
      expanded = isExpanded,
      onExpandedChange = { isExpanded = it }
  ) {
      OutlinedTextField(
          value = "List of subtasks",
          onValueChange = {},
          readOnly = true,
          trailingIcon = {
              Icon(Icons.Default.ArrowDropDown,
                  "View subtasks",
                  tint = Color.White,
                  modifier = Modifier
                      .padding(10.dp, 1.dp)
                      .size(14.dp)
                      .background(Color.DarkGray, CircleShape))
          },
          shape = RoundedCornerShape(13.dp),
          colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.LightGray,
              focusedTextColor = Color.DarkGray,
              unfocusedBorderColor = Color.LightGray,
              unfocusedTextColor = Color.DarkGray),
          modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .height(48.dp)
      )

      DropdownMenu(    //Here I used a DropdownMenu and not ExposedDropdownMenu because otherwise the keyboard will not work
          expanded = isExpanded,
          onDismissRequest = { isExpanded = false },
          modifier = Modifier
              .background(Color.White)
              .padding(10.dp)
              .width(260.dp)
      ) {
          Spacer(modifier = Modifier.height(16.dp))

          //Viene mostrata la lista dei sottotask, ognuno con accanto un'icona
          for (s in taskSubtasks) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier
                      .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
                      .padding(horizontal = 10.dp)
              ) {
                  Text(
                      text = s.subtaskTitle,
                      modifier = Modifier
                          .fillMaxWidth(0.9f)
                          .padding(8.dp)
                  )

                  //I check whether the status of the subtask is completed
                  //or not, and show the respective icon
                  if(s.subtaskState == State.NotCompleted){
                      IconButton(onClick = {
                          showPopUp = !showPopUp
                          subtask = s
                          titleSubtask = s.subtaskTitle
                      }
                      ) {
                          Icon(
                              painter = painterResource(R.drawable.ic_notcompleted_subtasks),
                              contentDescription = "Subtask options",
                              tint = Color.Blue,
                              modifier = Modifier.size(30.dp)
                          )
                      }
                  }else{
                      IconButton(onClick = {
                          showPopUp = false //If it is completed I don't show the popup
                      }
                      ) {
                          Icon(
                              painter = painterResource(R.drawable.ic_completed_subtasks),
                              contentDescription = "Subtask options"
                          )
                      }
                  }
              }
              Spacer(modifier = Modifier.height(5.dp))
          }

          Spacer(modifier = Modifier.height(7.dp))
          Row(horizontalArrangement = Arrangement.End, modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp)
              .height(20.dp)) {
              IconButton(
                  onClick = { isExpanded = false },
                  modifier = Modifier
                      .background(Color.White)
                      .width(46.dp)
              ) {
                  Text(text = "Close")
              }
          }
      }
  }

  Spacer(modifier = Modifier.height(16.dp))

  /**
   * Shows a pop-up to remove a subtask from a task or mark it as completed
   */
  if(showPopUp) {
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
                          Spacer(modifier = Modifier.height(16.dp))
                          Text(
                              text = titleSubtask,
                              color = Color.Black,
                              modifier = Modifier
                                  .weight(1f)
                                  .padding(10.dp, 1.dp),
                              style = MaterialTheme.typography.titleLarge,
                              fontFamily = FontFamily.SansSerif,
                              textAlign = TextAlign.Left
                          )

                          Button(
                              onClick = {
                                  setState(subtask)
                                  showPopUp = false
                                        },
                              colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                          ) {
                              Text(text = "Mark as completed")
                          }

                          Spacer(modifier = Modifier.height(10.dp))

                          Button(
                              onClick = {
                                  taskSubtasks.remove(subtask)
                                  showPopUp = false
                              },
                              colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                          ) {
                              Text(text = "Remove from task")
                          }

                          Spacer(modifier = Modifier.height(30.dp))
                      }
                  }
              }
          }
      }
  } */