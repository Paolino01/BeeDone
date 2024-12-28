package it.polito.BeeDone.team

import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.CreateTextFieldNoError
import it.polito.BeeDone.utils.ImagePicker
import it.polito.BeeDone.utils.rememberImeState

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun EditTeamPane(
    teamPhotoValue: Uri?,
    setTeamPhoto: (Uri?) -> Unit,

    teamNameValue: String,
    teamNameError: String,
    setTeamName: (String) -> Unit,

    teamDescriptionValue: String,
    setTeamDescription: (String) -> Unit,

    teamCategoryValue: String,
    teamCategoryError: String,
    setTeamCategory: (String) -> Unit,
) {
    val isImeVisible by rememberImeState()

    BoxWithConstraints {
        val maxH = this.maxHeight
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
                //Team Picture
                Row(
                    modifier = Modifier
                        .height(maxH / 3)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ImagePicker(teamNameValue, teamPhotoValue, setTeamPhoto)
                }

                //Team Name
                CreateTextFieldError(
                    teamNameValue,
                    teamNameError,
                    setTeamName,
                    "Team Name *",
                    KeyboardType.Text
                )

                //Team Description
                CreateTextFieldNoError(
                    value = teamDescriptionValue,
                    setValue = setTeamDescription,
                    label = "Team Description",
                    keyboardType = KeyboardType.Text
                )

                //Team Category
                CreateTextFieldError(
                    value = teamCategoryValue,
                    error = teamCategoryError,
                    setValue = setTeamCategory,
                    label = "Team Category *",
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            //HORIZONTAL
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                //Profile Picture
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ImagePicker(teamNameValue, teamPhotoValue, setTeamPhoto)
                }

                Column(
                    modifier = Modifier
                        .weight(2f, false)
                        .padding(50.dp, 0.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    //Team Name
                    CreateTextFieldError(
                        teamNameValue,
                        teamNameError,
                        setTeamName,
                        "Team Name *",
                        KeyboardType.Text
                    )

                    //Team Description
                    CreateTextFieldNoError(
                        value = teamDescriptionValue,
                        setValue = setTeamDescription,
                        label = "Team Description",
                        keyboardType = KeyboardType.Text
                    )

                    //Team Category
                    CreateTextFieldError(
                        value = teamCategoryValue,
                        error = teamCategoryError,
                        setValue = setTeamCategory,
                        label = "Team Category *",
                        keyboardType = KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }
}