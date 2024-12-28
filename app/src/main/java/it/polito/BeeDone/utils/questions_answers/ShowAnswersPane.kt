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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import it.polito.BeeDone.utils.CreateImage
import it.polito.BeeDone.utils.CreateTextFieldNoError

@Composable
fun ShowAnswers(
    answers: MutableList<Answer>,
    setAnswers: (String, Question, FirebaseFirestore) -> Unit,
    assignAnswers: (MutableList<String>, FirebaseFirestore) -> Unit,
    answerValue: String,
    setAnswer: (String) -> Unit,
    selectedQuestion: Question,
    showUserInformationPane: (String) -> Unit,
    db: FirebaseFirestore
) {
    val state = rememberScrollState()               //Needed for the scroll

    var answersLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedQuestion, db) {
        assignAnswers(selectedQuestion.answers, db)
        answersLoaded = true
    }

    Scaffold(
        bottomBar = {
            //Add a new answer
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(250, 250, 250))   //Same color as the background
                        .fillMaxWidth()
                        .padding(bottom = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CreateTextFieldNoError(
                        value = answerValue,
                        setValue = setAnswer,
                        label = "Write your reply here.",
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            if (answerValue.isNotBlank()) {                     //If the answer is blank, nothing happens
                                setAnswers(answerValue, selectedQuestion, db)   //When we add a new answer, the user must scroll down to see it
                                //When a new answer is added to the TaskAnswers List, it is also added to selectedQuestion, since TakAnswers is a reference to selectedQuestion
                                setAnswer("")                                   //Reset the task answer
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    ) {innerPadding ->
        var questionUserLoaded by remember { mutableStateOf(false) }
        var questionUser by remember { mutableStateOf(User()) }

        LaunchedEffect(questionUser) {
            db.collection("Users").document(selectedQuestion.user)
                .get()
                .addOnSuccessListener {
                        doc ->
                    questionUser = doc.toObject(User::class.java)!!
                    questionUserLoaded = true
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting team data", exception)
                }
        }

        Row(
            Modifier
                .verticalScroll(state)
                .padding(innerPadding)
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                //Show selected question
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = {
                                showUserInformationPane(selectedQuestion.user)
                            })
                    ) {
                        if(questionUserLoaded) {
                            CreateImage(
                                photo = if(questionUser.userImage == null) null else questionUser.userImage!!.toUri(),
                                name = "${questionUser.userFirstName} ${questionUser.userLastName}",
                                size = 30
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(10f)
                    ) {
                        Text(
                            text = selectedQuestion.user,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.clickable(onClick = {
                                showUserInformationPane(selectedQuestion.user)
                            })
                        )

                        Text(
                            text = selectedQuestion.date,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = selectedQuestion.text,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            Icon(Icons.Filled.Email, "Messages")
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${selectedQuestion.answers.size} answers",
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if(answersLoaded) {
                            if (answers.size == 0) {
                                Text(
                                    text = "no answers",
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            } else {
                                for (a in answers) {
                                    var answerUserLoaded by remember { mutableStateOf(false) }
                                    var answerUser by remember { mutableStateOf(User()) }

                                    LaunchedEffect(answerUser) {
                                        db.collection("Users").document(a.user)
                                            .get()
                                            .addOnSuccessListener { doc ->
                                                answerUser = doc.toObject(User::class.java)!!
                                                answerUserLoaded = true
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("Firestore", "Error getting team data", exception)
                                            }
                                    }

                                    Row {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(onClick = {
                                                    showUserInformationPane(a.user)
                                                })
                                        ) {
                                            Spacer(modifier = Modifier.height(10.dp))

                                            if(answerUserLoaded) {
                                                CreateImage(
                                                    photo = if(answerUser.userImage == null) null else answerUser.userImage!!.toUri(),
                                                    name = "${answerUser.userFirstName} ${answerUser.userLastName}",
                                                    size = 30
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .weight(10f)
                                                .padding(horizontal = 10.dp)
                                        ) {
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = a.text,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            CircularProgressIndicator()
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}