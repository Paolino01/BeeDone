package it.polito.BeeDone.team

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.teamViewModel
import it.polito.BeeDone.utils.CreateDropdownTeamsUser
import it.polito.BeeDone.utils.CreateKPI
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun FeedbackPerformancePane(
    userSelected: User?,
    selectedTeam: Team,
    db: FirebaseFirestore
) {

    var allTasks = mutableStateListOf<Task>()

    for (taskRef in selectedTeam.teamTasks){
        db.collection("Tasks").document(taskRef).get().addOnSuccessListener {
            taskDoc -> allTasks.add(taskDoc.toObject(Task::class.java)!!)
        }
    }


    BoxWithConstraints {
        //VERTICAL
        if (this.maxHeight > this.maxWidth) {               //True if the screen is in portrait mode
            //VERTICAL
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(horizontal = 16.dp),     //Padding is needed in order to leave 16dp from left and right borders
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TEAM TASKS",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))
                if (selectedTeam.teamTasks.size > 0) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                            .padding(horizontal = 10.dp)
                    )
                    {

                        Spacer(modifier = Modifier.height(16.dp))
                        //KPI
                        CreateKPI(
                            allTasks.filter { t -> (t.taskStatus == "Completed" || t.taskStatus == "Expired Completed") }.size,
                            allTasks.size,
                            allTasks.filter { t -> t.taskStatus == "Expired Completed" }.size,
                            allTasks.filter { t ->
                                (t.taskStatus == "Pending" || t.taskStatus == "In Progress" || t.taskStatus == "Expired Not Completed") && LocalDate.parse(
                                    t.taskDeadline,
                                    DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                ) < LocalDate.now()
                            }.size
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        CreateDropdownTeamsUser(
                            teamViewModel.userSelected,
                            selectedTeam.teamMembers,
                            teamViewModel::setTeUserSelected,
                            db
                        )
                    }

                    if (teamViewModel.userSelected != null) {

                        Text(
                            text = "${teamViewModel.userSelected!!.userNickname}'s TASKS",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxSize(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (allTasks.filter { t ->
                                t.taskUsers.contains(
                                    userSelected?.userNickname
                                )
                            }.isNotEmpty()) {

                            Column(
                                modifier = Modifier
                                    .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                    .padding(horizontal = 10.dp)
                            ) {

                                Spacer(modifier = Modifier.height(6.dp))

                                CreateKPI(
                                    allTasks.filter { t ->
                                        ((t.taskStatus == "Completed" || t.taskStatus == "Expired Completed") && t.taskUsers.contains(
                                            userSelected?.userNickname
                                        ))
                                    }.size,
                                    allTasks.filter { t ->
                                        t.taskUsers.contains(
                                            userSelected?.userNickname
                                        )
                                    }.size,
                                    allTasks.filter { t ->
                                        (t.taskStatus == "Expired Completed" && t.taskUsers.contains(
                                            userSelected?.userNickname
                                        ))
                                    }.size,
                                    allTasks.filter { t ->
                                        ((t.taskStatus == "Pending" || t.taskStatus == "In Progress" || t.taskStatus == "Expired Not Completed") && LocalDate.parse(
                                            t.taskDeadline,
                                            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                        ) < LocalDate.now() && t.taskUsers.contains(userSelected?.userNickname))
                                    }.size
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                        } else {
                            Text(
                                text = "The user has no task.",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = "The team has no task.",
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            //HORIZONTAL
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(2.dp)) {
                Row {
                    CreateDropdownTeamsUser(
                        teamViewModel.userSelected,
                        selectedTeam.teamMembers,
                        teamViewModel::setTeUserSelected,
                        db
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),     //Padding is needed in order to leave 16dp from left and right borders
                        horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    Column( modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "TEAM TASKS",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        if (selectedTeam.teamTasks.size > 0) {
                            Column(
                                modifier = Modifier
                                    .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                    .padding(horizontal = 10.dp)
                            )
                            {

                                Spacer(modifier = Modifier.height(16.dp))
                                //KPI
                                CreateKPI(
                                    allTasks.filter { t -> (t.taskStatus == "Completed" || t.taskStatus == "Expired Completed") }.size,
                                    allTasks.size,
                                    allTasks.filter { t -> t.taskStatus == "Expired Completed" }.size,
                                    allTasks.filter { t ->
                                        (t.taskStatus == "Pending" || t.taskStatus == "In Progress" || t.taskStatus == "Expired Not Completed") && LocalDate.parse(
                                            t.taskDeadline,
                                            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                        ) < LocalDate.now()
                                    }.size
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                        } else {
                            Text(
                                text = "The team has no task.",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(4.dp))

                    Column(
                        modifier = Modifier
                        .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (teamViewModel.userSelected != null) {

                            Text(
                                text = "${teamViewModel.userSelected!!.userNickname}'s TASKS",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (allTasks.filter { t ->
                                    t.taskUsers.contains(
                                        userSelected?.userNickname
                                    )
                                }.isNotEmpty()) {

                                Column(
                                    modifier = Modifier
                                        .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                        .padding(horizontal = 10.dp)
                                ) {


                                    Spacer(modifier = Modifier.height(16.dp))


                                    CreateKPI(
                                        allTasks.filter { t ->
                                            ((t.taskStatus == "Completed" || t.taskStatus == "Expired Completed") && t.taskUsers.contains(
                                                userSelected?.userNickname
                                            ))
                                        }.size,
                                        allTasks.filter { t ->
                                            t.taskUsers.contains(
                                                userSelected?.userNickname
                                            )
                                        }.size,
                                        allTasks.filter { t ->
                                            (t.taskStatus == "Expired Completed" && t.taskUsers.contains(
                                                userSelected?.userNickname
                                            ))
                                        }.size,
                                        allTasks.filter { t ->
                                            ((t.taskStatus == "Pending" || t.taskStatus == "In Progress" || t.taskStatus == "Expired Not Completed") && LocalDate.parse(
                                                t.taskDeadline,
                                                DateTimeFormatter.ofPattern("dd/MM/uuuu")
                                            ) < LocalDate.now() && t.taskUsers.contains(userSelected?.userNickname))
                                        }.size
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                }


                            }else{
                                Text(
                                    text = "The user has no task.",
                                    textAlign = TextAlign.Center
                                )
                            }


                        }


                    }


                }
                Spacer(modifier = Modifier.height(16.dp))

            }

        }
    }
}

