package it.polito.BeeDone.task

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.R
import it.polito.BeeDone.activeAnimation
import it.polito.BeeDone.activeAnimation
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.team.UserInTeam
import it.polito.BeeDone.utils.TaskRow
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Shows only the tasks assigned to the logged in user
 * If a task is not assigned to the logged user, it is not shown, but only in "team tasks"
 */
@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PersonalTaskListPane(
    tasks: MutableList<Task>,
    clearTaskInformation: () -> Unit,
    createTaskPane: () -> Unit,
    showTaskDetailsPane: (String) -> Unit,
    editTaskPane: (String) -> Unit,
    db: FirebaseFirestore,
    allTeams: SnapshotStateList<Team>
) {
    //variables for the flyout button
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showButton by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("↑") }


    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.visibleItemsInfo) {
        showButton = listState.firstVisibleItemIndex > 0
        buttonText = if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size >= listState.layoutInfo.totalItemsCount) "↑" else "↓"
    }

    //filter only the logged user
    tasks.removeAll {
        !it.taskUsers.contains(loggedUser.userNickname)
    }

    Spacer(modifier = Modifier.height(10.dp))
    Box(
        Modifier
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        if (tasks.size > 0) {
            if(activeAnimation) {
                // Initialize a state to manage the visibility of each task
                val visibleTasks =
                    remember { mutableStateListOf<Boolean>().apply { repeat(tasks.size) { add(false) } } }

                // LaunchedEffect to trigger the visibility of tasks one by one
                LaunchedEffect(tasks) {
                    tasks.forEachIndexed { index, _ ->
                        kotlinx.coroutines.delay(100) // Delay for each task appearance
                        visibleTasks[index] = true
                    }
                }

                //tasks column
                LazyColumn(
                    Modifier.fillMaxHeight(),
                    state = listState,
                    verticalArrangement = Arrangement.Top
                ) {
                    items(tasks) { task ->
                        val index = tasks.indexOf(task)
                        AnimatedVisibility(
                            visible = visibleTasks[index],
                            enter = fadeIn() + expandVertically()
                        ) {
                            TaskRow(
                                showingTasks = tasks,
                                task = task,
                                showTaskDetailsPane = showTaskDetailsPane,
                                editTaskPane = editTaskPane,
                                db = db,
                                allTeams = allTeams
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }else{
                //tasks column
                LazyColumn(
                    Modifier.fillMaxHeight(),
                    state = listState,
                    verticalArrangement = Arrangement.Top
                ) {
                    items(tasks) { task ->
                        TaskRow(
                            showingTasks = tasks,
                            task = task,
                            showTaskDetailsPane = showTaskDetailsPane,
                            editTaskPane = editTaskPane,
                            db = db,
                            allTeams = allTeams
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }else{
            //no task
            Text(modifier = Modifier.align(Alignment.Center),
                text = "No task to display")
        }

        //create task button
        FloatingActionButton(
            onClick = {
                clearTaskInformation()
                createTaskPane()
            },
            shape = CircleShape,
            containerColor = Color.White,
            modifier = Modifier
                .padding(10.dp)
                .size(70.dp)
                .align(Alignment.BottomEnd)
                .offset(5.dp, 5.dp)
                .border(2.dp, lightBlue, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Create New Task",
                Modifier.size(30.dp)
            )
        }

        // Scroll to top or bottom button
        if (showButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size < listState.layoutInfo.totalItemsCount) {
                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                        } else {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .offset(10.dp, 0.dp)
            ) {
                Text(buttonText, fontSize = 25.sp)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun TaskMenu(
    showingTasks: MutableList<Task>,
    allTasks: MutableList<Task>,
    profileTeams: SnapshotStateList<UserInTeam>,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    var filterText by mutableStateOf("")

    Box(contentAlignment = Alignment.TopEnd) {

        Row {
            //sort icon
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                showSortMenu = !showSortMenu
            }) {
                Icon(
                    painter = painterResource(R.drawable.sorting),
                    contentDescription = "Sorting",
                    modifier = Modifier.size(25.dp)
                )
            }

            //filter icon
            IconButton(onClick = {
                showFilterMenu = !showFilterMenu
            }) {
                Icon(
                    painter = painterResource(R.drawable.filter),
                    contentDescription = "Filter",
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        //SORT MENU
        CascadeDropdownMenu(modifier = Modifier
            .border(1.dp, lightBlue, RoundedCornerShape(20.dp))
            .background(Color.White),
            expanded = showSortMenu,
            shape = RoundedCornerShape(20.dp),
            onDismissRequest = { showSortMenu = false }) {

            //sort by Deadline
            DropdownMenuItem(modifier = Modifier.background(Color.White), childrenHeader = {
                DropdownMenuHeader(Modifier.background(Color.White)) {
                    Text(text = "Sort by Deadline")
                }
            }, text = { Text(text = "Sort by Deadline") }, children = {

                //Sort By Ascending deadline
                DropdownMenuItem(modifier = Modifier
                    .background(Color.White)
                    .border(Dp.Hairline, lightBlue),
                    text = { Text(text = "Ascending") },
                    onClick = {
                        showingTasks.sortBy { it.taskDeadline }

                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ascending),
                            contentDescription = "Ascending",
                            modifier = Modifier.size(22.dp)
                        )
                    })
                //Sort By Descending deadline
                DropdownMenuItem(
                    modifier = Modifier.background(Color.White),
                    text = { Text(text = "Descending") },
                    onClick = { showingTasks.sortByDescending { it.taskDeadline } },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.descending),
                            contentDescription = "Descending",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                )
            })

            //sort by Creation date
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        Text(text = "Sort by Creation Date")
                    }
                },
                text = { Text(text = "Sort by Creation Date") },
                children = {
                    //Sort By Ascending Creation date
                    DropdownMenuItem(modifier = Modifier
                        .background(Color.White)
                        .border(Dp.Hairline, lightBlue),
                        text = { Text(text = "Ascending") },
                        onClick = { showingTasks.sortBy { it.taskCreationDate } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ascending),
                                contentDescription = "Ascending",
                                modifier = Modifier.size(22.dp)
                            )
                        })
                    DropdownMenuItem(modifier = Modifier.background(Color.White),
                        text = { Text(text = "Descending") },
                        onClick = { showingTasks.sortByDescending { it.taskCreationDate } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.descending),
                                contentDescription = "Descending",
                                modifier = Modifier.size(20.dp)
                            )
                        })
                })

            //sort by Title
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        Text(text = "Sort by Title")
                    }
                },
                text = { Text(text = "Sort by Title") },
                children = {
                    //Sort By Ascending Title
                    DropdownMenuItem(modifier = Modifier
                        .background(Color.White)
                        .border(Dp.Hairline, lightBlue),
                        text = { Text(text = "Ascending") },
                        onClick = { showingTasks.sortBy { it.taskTitle } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ascending),
                                contentDescription = "Ascending",
                                modifier = Modifier.size(22.dp)
                            )
                        })
                    DropdownMenuItem(modifier = Modifier.background(Color.White),
                        text = { Text(text = "Descending") },
                        onClick = { showingTasks.sortByDescending { it.taskTitle } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.descending),
                                contentDescription = "Descending",
                                modifier = Modifier.size(20.dp)
                            )
                        })
                })

            //sort by TeamName
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        Text(text = "Sort by Team Name")
                    }
                },
                text = { Text(text = "Sort by Team Name") },
                children = {
                    DropdownMenuItem(
                        modifier = Modifier
                            .background(Color.White)
                            .border(Dp.Hairline, lightBlue),
                        text = { Text(text = "Ascending") },
                        onClick = { showingTasks.sortBy { it.taskTeam } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ascending),
                                contentDescription = "Ascending",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                    )
                    DropdownMenuItem(
                        modifier = Modifier.background(Color.White),

                        text = { Text(text = "Descending") },
                        onClick = { showingTasks.sortByDescending { it.taskTeam } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.descending),
                                contentDescription = "Descending",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                    )
                })
        }

        //FILTER MENU
        CascadeDropdownMenu(
            modifier = Modifier
                .border(1.dp, lightBlue, RoundedCornerShape(20.dp))
                .background(Color.White),
            expanded = showFilterMenu,
            shape = RoundedCornerShape(20.dp),
            onDismissRequest = {
                showFilterMenu = false
                filterText = ""
            },
        ) {

            //Filter by Category
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        filterText = ""
                        Text(text = "Filter by Category")
                    }
                },
                text = { Text(text = "Filter by Category") },
                children = {
                    DropdownMenuItem(
                        modifier = Modifier.background(Color.White),
                        text = {
                            Column {
                                OutlinedTextField(
                                    placeholder = { Text(text = "Insert Category") },
                                    value = filterText,
                                    onValueChange = { newText -> filterText = newText },
                                    maxLines = 1,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = lightBlue,
                                        focusedLabelColor = lightBlue,
                                        focusedTextColor = Color.DarkGray
                                    ),
                                )

                                FloatingActionButton(
                                    onClick = { showingTasks.removeAll { it.taskCategory != filterText } },
                                    containerColor = Color.White,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp, 10.dp)
                                        .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                        .height(30.dp)
                                ) {
                                    Text(text = "Apply Filter")
                                }
                            }
                        },
                        onClick = { showingTasks.removeAll { it.taskCategory != filterText } },
                        interactionSource = MutableInteractionSource()
                    )
                })

            //Filter by Tag
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        filterText = ""
                        Text(text = "Filter by Tag")
                    }
                },
                text = { Text(text = "Filter by Tag") },
                children = {
                    DropdownMenuItem(modifier = Modifier.background(Color.White), text = {
                        Column {
                            OutlinedTextField(
                                placeholder = { Text(text = "Insert Tag") },
                                value = filterText,
                                onValueChange = { newText -> filterText = newText },
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = lightBlue,
                                    focusedLabelColor = lightBlue,
                                    focusedTextColor = Color.DarkGray
                                ),
                            )
                            FloatingActionButton(
                                onClick = { showingTasks.removeAll { it.taskTag != filterText } },
                                containerColor = Color.White,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp, 10.dp)
                                    .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                    .height(30.dp)
                            ) {
                                Text(text = "Apply Filter")
                            }
                        }
                    }, onClick = { showingTasks.removeAll { it.taskTag != filterText } })
                })

            //Filter by User
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        filterText = ""
                        Text(text = "Filter by User")
                    }
                },
                text = { Text(text = "Filter by User") },
                children = {
                    DropdownMenuItem(modifier = Modifier.background(Color.White), text = {
                        Column {

                            OutlinedTextField(
                                placeholder = { Text(text = "Insert User") },
                                value = filterText,
                                onValueChange = { newText -> filterText = newText },
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = lightBlue,
                                    focusedLabelColor = lightBlue,
                                    focusedTextColor = Color.DarkGray
                                ),
                            )
                            FloatingActionButton(
                                onClick = {
                                    showingTasks.removeAll {
                                        !it.taskUsers
                                            .contains(filterText)
                                    }
                                },
                                containerColor = Color.White,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp, 10.dp)
                                    .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                    .height(30.dp)
                            ) {
                                Text(text = "Apply Filter")
                            }
                        }

                    }, onClick = {
                        showingTasks.removeAll {
                            !it.taskUsers.contains(filterText)
                        }
                    })
                })

            //Filter by Team Name
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        Text(text = "Filter by Team Name")
                    }
                },
                text = { Text(text = "Filter by Team Name") },
                children = {
                    profileTeams.forEach { team ->
                        DropdownMenuItem(modifier = Modifier
                            .background(Color.White)
                            .border(Dp.Hairline, lightBlue),
                            text = { Text(text = team.first) },
                            onClick = { showingTasks.removeAll { it.taskTeam != team.first } })
                    }
                }
            )

            //Filter by Status
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        Text(text = "Filter by Status")
                    }
                },
                text = { Text(text = "Filter by Status") },
                children = {
                    taskStatus.forEach { status ->
                        DropdownMenuItem(modifier = Modifier
                            .background(Color.White)
                            .border(Dp.Hairline, lightBlue),
                            text = { Text(text = status.toString()) },
                            onClick = { showingTasks.removeAll {
                                if (status == "Expired Not Completed") {
                                    it.taskStatus != "In Progress" &&
                                            it.taskStatus != "Pending" &&
                                            LocalDate.parse(
                                                it.taskDeadline,
                                                DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                            ) >= LocalDate.now()
                                } else {
                                    it.taskStatus != status
                                }
                            }
                            })
                    }
                }
            )

            //Remove all filters
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                text = { Text("Remove Filters") },
                onClick = {
                    showingTasks.removeAll { true }
                    showingTasks.addAll(allTasks)
                }
            )

        }
    }
}