package it.polito.BeeDone.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.utils.CreateDropdownProfiles
import it.polito.BeeDone.utils.CreateDropdownRepeat
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.DatePickerWithDialog
import it.polito.BeeDone.utils.PresentOrFutureSelectableDates
import it.polito.BeeDone.utils.rememberImeState

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun EditTaskPane(
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
    db: FirebaseFirestore
) {
    val isImeVisible by rememberImeState()

    BoxWithConstraints {
        if (this.maxHeight > this.maxWidth) {
            //VERTICAL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                        .padding(start = 40.dp, top = 3.dp, end = 40.dp, bottom = 3.dp)
                        .imePadding()
                }
            ) {
                //Task Title
                CreateTextFieldError(
                    taskTitleValue,
                    taskTitleError,
                    setTaskTitle,
                    "Task Title *",
                    KeyboardType.Text
                )

                //Task Description
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
                )    //Deadline should be in the future

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

                //Group
                CreateTextFieldNoError(
                    value = taskTeamValue.teamName,
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

                CreateDropdownProfiles(
                    taskTeamValue,
                    taskUsersValue,
                    setTaskUsers,
                    deleteTaskUsers,
                    db
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            //HORIZONTAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    //Task Title
                    CreateTextFieldError(
                        taskTitleValue,
                        taskTitleError,
                        setTaskTitle,
                        "Task Title *",
                        KeyboardType.Text
                    )

                    //Task Description
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
/*
                    //Users
                    CreateTaskUsersField(
                        taskUsersValue,
                        setTaskUsers,
                        taskUserValue,
                        setTaskUser
                    )

 */

                    //Repeat
                    CreateDropdownRepeat(taskRepeatValue, setTaskRepeat)

                    //Group
                    CreateTextFieldNoError(
                        value = taskTeamValue.teamName,
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

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}