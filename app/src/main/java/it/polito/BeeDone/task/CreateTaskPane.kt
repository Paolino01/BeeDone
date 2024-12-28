package it.polito.BeeDone.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.utils.CreateDropdownProfiles
import it.polito.BeeDone.utils.CreateDropdownRepeat
import it.polito.BeeDone.utils.CreateDropdownTeams
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.DatePickerWithDialog
import it.polito.BeeDone.utils.PresentOrFutureSelectableDates
import it.polito.BeeDone.utils.rememberImeState

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun CreateTaskPane(
    taskTitleValue: String,
    taskTitleError: String,
    setTaskTitle: (String) -> Unit,

    taskDescriptionValue: String,
    setTaskDescription: (String) -> Unit,

    taskDeadlineValue: String,
    taskDeadlineError: String,
    setTaskDeadline: (String) -> Unit,

    taskTagValue: String,
    setTaskTag: (String) -> Unit,

    taskCategoryValue: String,
    taskCategoryError: String,
    setTaskCategory: (String) -> Unit,

    taskUsersValue: MutableList<User>,
    setTaskUsers: (User) -> Unit,
    deleteTaskUsers: (User) -> Unit,

    taskRepeatValue: String,
    setTaskRepeat: (String) -> Unit,

    taskTeamValue: Team,
    taskTeamError: String,
    setTaskTeam: (String, FirebaseFirestore) -> Unit,

    allTeams: MutableList<Team> = mutableListOf(),
    selectedTeam: Team?,
    createTaskPaneFromTeam: (String) -> Unit,

    db: FirebaseFirestore
) {

    val isImeVisible by rememberImeState()
    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {
            //VERTICAL
            Column(
                modifier =
                if (isImeVisible) {
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(40.dp, 3.dp, 40.dp, 220.dp)
                } else {
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(40.dp, 3.dp, 40.dp, 3.dp)
                }
            ) {
                //title
                CreateTextFieldError(
                    taskTitleValue,
                    taskTitleError,
                    setTaskTitle,
                    "Title *",
                    KeyboardType.Text
                )

                //description
                CreateTextFieldNoError(
                    value = taskDescriptionValue,
                    setValue = setTaskDescription,
                    label = "Description",
                    keyboardType = KeyboardType.Text
                )

                //Task Deadline
                DatePickerWithDialog(
                    taskDeadlineValue,
                    taskDeadlineError,
                    setTaskDeadline,
                    "Task Deadline *",
                    PresentOrFutureSelectableDates
                )

                //Tag
                CreateTextFieldNoError(
                    value = taskTagValue,
                    setValue = setTaskTag,
                    label = "Tag",
                    keyboardType = KeyboardType.Text
                )

                //Category
                CreateTextFieldError(
                    taskCategoryValue,
                    taskCategoryError,
                    setTaskCategory,
                    "Category *",
                    KeyboardType.Text
                )


                //Repeat
                CreateDropdownRepeat(taskRepeatValue, setTaskRepeat)

                if (selectedTeam == null) {
                    //TeamTask
                    CreateDropdownTeams(taskTeamValue, taskTeamError, setTaskTeam, allTeams, createTaskPaneFromTeam, db)
                } else {
                    LaunchedEffect(selectedTeam) {
                        setTaskTeam(selectedTeam.teamId, db)
                    }

                    CreateTextFieldNoError(
                    value = selectedTeam.teamName,
                    setValue = setTaskTag,
                    label = "Team",
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.LightGray,
                        focusedTextColor = Color.LightGray,
                        unfocusedTextColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        unfocusedLabelColor = Color.LightGray,
                    ),
                    readOnly = true
                )
                }

                if (selectedTeam!=null) {
                    CreateDropdownProfiles(
                        taskTeamValue,
                        taskUsersValue,
                        setTaskUsers,
                        deleteTaskUsers,
                        db
                    )
                }else{
                    Text(text = "Select a team to select a user")
                }

                Spacer(modifier = Modifier.height(16.dp))


            }
        } else {
            Column(
                modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(40.dp, 3.dp, 40.dp, 20.dp)
            ) {
                //title
                CreateTextFieldError(
                    taskTitleValue,
                    taskTitleError,
                    setTaskTitle,
                    "Title *",
                    KeyboardType.Text
                )

                //description
                CreateTextFieldNoError(
                    value = taskDescriptionValue,
                    setValue = setTaskDescription,
                    label = "Description",
                    keyboardType = KeyboardType.Text
                )

                //Task Deadline
                DatePickerWithDialog(
                    taskDeadlineValue,
                    taskDeadlineError,
                    setTaskDeadline,
                    "Task Deadline *",
                    PresentOrFutureSelectableDates
                )

                //Tag
                CreateTextFieldNoError(
                    value = taskTagValue,
                    setValue = setTaskTag,
                    label = "Tag",
                    keyboardType = KeyboardType.Text
                )

                //Category
                CreateTextFieldError(
                    taskCategoryValue,
                    taskCategoryError,
                    setTaskCategory,
                    "Category *",
                    KeyboardType.Text
                )

                //Repeat
                CreateDropdownRepeat(taskRepeatValue, setTaskRepeat)

                if (selectedTeam == null) {
                    //TeamTask
                    CreateDropdownTeams(taskTeamValue, taskTeamError, setTaskTeam, allTeams, createTaskPaneFromTeam, db)
                } else {
                    LaunchedEffect(selectedTeam) {
                        setTaskTeam(selectedTeam.teamId, db)
                    }
                    CreateTextFieldNoError(
                        value = selectedTeam.teamName,
                        setValue = setTaskTag,
                        label = "Team",
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            focusedLabelColor = Color.LightGray,
                            focusedTextColor = Color.LightGray,
                            unfocusedTextColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedLabelColor = Color.LightGray,
                        ),
                        readOnly = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTeam!=null) {
                    CreateDropdownProfiles(
                        taskTeamValue,
                        taskUsersValue,
                        setTaskUsers,
                        deleteTaskUsers,
                        db
                    )
                }else{
                    Text(text = "Select a team to select a user")
                }

                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }


}

