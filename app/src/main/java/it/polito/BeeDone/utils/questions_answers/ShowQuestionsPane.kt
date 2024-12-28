package it.polito.BeeDone.utils.questions_answers

import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.CreateTextFieldNoError
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Shows the list of questions associated to the selected task
 * currentTaskQuestions = selectedTask!!.taskQuestions
 * taskQuestions = mutableStateOf(mutableListOf<TaskQuestion>), we need this because, if we only used currentTaskQuestions, the list of questions on display would'nt be updated when a new task is added
 */
@Composable
fun ShowQuestions(
    selectedTask: Task,
    questions: MutableList<Question>,
    setQuestions: (String, Task, FirebaseFirestore) -> Unit,
    questionValue: String,
    setQuestion: (String) -> Unit,
    showUserInformationPane: (String) -> Unit,
    navigate: (String) -> Unit,
    db: FirebaseFirestore
) {
    val state = rememberScrollState()               //Needed for the scroll

    Scaffold(
        bottomBar = {
            //Add a new question
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Color(
                                250, 250, 250
                            )
                        )   //Same color as the background
                        .fillMaxWidth()
                        .padding(bottom = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CreateTextFieldNoError(
                        value = questionValue,
                        setValue = setQuestion,
                        label = "Write your question here.",
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        maxLines = 3
                    )
                    IconButton(onClick = {
                        if (questionValue.isNotBlank()) {                   //If the question is blank, nothing happens
                            setQuestions(questionValue, selectedTask, db)       //When we add a new question, the user must scroll down or up to see it
                            setQuestion("")                                 //Reset the question
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    ) {innerPadding ->
        Row(
            Modifier
                .verticalScroll(state)
                .padding(innerPadding)
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                var user by remember { mutableStateOf(User()) }
                //Show existing questions
                for (q in questions) {
                    var userLoaded by remember { mutableStateOf(false) }

                    LaunchedEffect(q) {
                        db.collection("Users").document(q.user)
                            .get()
                            .addOnSuccessListener {
                                    doc ->
                                user = doc.toObject(User::class.java)!!

                                userLoaded = true
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firestore", "Error getting team data", exception)
                            }
                    }

                    Row(
                        modifier = Modifier
                            .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if(userLoaded) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = {
                                        showUserInformationPane(q.user)
                                    })
                            ) {
                                CreateImage(
                                    photo = if(user.userImage == null) null else user.userImage!!.toUri(),
                                    name = "${user.userFirstName} ${user.userLastName}",
                                    size = 30
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier.weight(10f)
                        ) {
                            Text(
                                text = q.user,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.clickable(onClick = {
                                    showUserInformationPane(q.user)
                                })
                            )

                            Text(
                                text = q.date, color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = q.text, fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.clickable(onClick = {
                                    navigate(q.questionId)
                                })
                            ) {
                                Icon(Icons.Default.Email, "Messages")
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${q.answers.size} answers", fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}