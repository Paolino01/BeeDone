package it.polito.BeeDone.team

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun CreateTeamPane(
    teamNameValue: String,
    teamNameError: String,
    setTeamName: (String) -> Unit,
    teamDescriptionValue: String,
    setTeamDescription: (String) -> Unit,
    teamCategoryValue: String,
    teamCategoryError: String,
    setTeamCategory: (String) -> Unit,
    teamImageValue: Uri?,
    setTeamImage: (Uri?) -> Unit
) {

    val isImeVisible by rememberImeState()
    BoxWithConstraints {
        val maxH = this.maxHeight
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

                //Image value
                Row(
                    modifier = Modifier
                        .height(maxH / 3)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ImagePicker(teamNameValue, teamImageValue, setTeamImage)
                }




                //team name
                CreateTextFieldError(
                    teamNameValue,
                    teamNameError,
                    setTeamName,
                    "Team name *",
                    KeyboardType.Text
                )

                //description
                CreateTextFieldNoError(
                    value = teamDescriptionValue,
                    setValue = setTeamDescription,
                    label = "Description",
                    keyboardType = KeyboardType.Text
                )


                //Category
                CreateTextFieldError(
                    teamCategoryValue,
                    teamCategoryError,
                    setTeamCategory,
                    "Category *",
                    KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(16.dp))

            }
        } else {
            //HORIZONTAL
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                //Image value
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ImagePicker(teamNameValue, teamImageValue, setTeamImage)

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

                    //title
                    CreateTextFieldError(
                        teamNameValue,
                        teamNameError,
                        setTeamName,
                        "Team name *",
                        KeyboardType.Text
                    )

                    //description
                    CreateTextFieldNoError(
                        value = teamDescriptionValue,
                        setValue = setTeamDescription,
                        label = "Description",
                        keyboardType = KeyboardType.Text
                    )

                    //Category
                    CreateTextFieldError(
                        teamCategoryValue,
                        teamCategoryError,
                        setTeamCategory,
                        "Category *",
                        KeyboardType.Text
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }





}