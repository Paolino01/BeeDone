package it.polito.BeeDone.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import it.polito.BeeDone.R
import java.io.File
import kotlin.math.min

/*
    When we take a picture with the camera, we first need to create a file to store the image.
    This function creates a file called "selected_image_.jpg" in the "images" folder
 */
class ComposeFileProvider : FileProvider(
    R.xml.filepaths
) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory,
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}

/*
    Show the image (or the placeholder, in case there is no image) and the button that opens
    a popup that lets the user select a new image, take a picture or delete the current image (if present)
 */
@Composable
fun ImagePicker(
    name: String,
    imgUri: Uri?,
    setPhoto: (Uri?) -> Unit,
) {
    var hasImage by remember {
        mutableStateOf(imgUri != null && imgUri.toString() != "null")
    }
    var imageUri by remember {
        mutableStateOf<Uri?>(imgUri)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            hasImage = uri != null
            imageUri = uri
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            hasImage = success
        }
    )

    val context = LocalContext.current

    Box {
        if (hasImage && imageUri != null && imageUri.toString() != "null") {
            SetImage(imageUri, 170)
            setPhoto(imageUri)
        }
        else {
            NoImage(name.trim(), 170)
        }

        var showPopUp by remember { mutableStateOf(false) } // -> STATE
        FloatingActionButton(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
                .absoluteOffset(x = 0.dp, y = 0.dp),
            onClick = {
                showPopUp = !showPopUp
            },
            containerColor = Color.LightGray,
            shape = CircleShape,
        ) {
            Text(
                text = "\uD83D\uDCF7",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        //Popup, it opens when the user taps on the camera icon
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
                                Button(
                                    onClick = {
                                        imagePicker.launch("image/*")
                                        showPopUp = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.border(2.dp, lightBlue, ButtonDefaults.shape)
                                ) {
                                    Text(
                                        text = "Select Image", color = Color.Black
                                    )
                                }
                                Button(
                                    modifier = Modifier.padding(top = 20.dp).border(2.dp, lightBlue, ButtonDefaults.shape),
                                    onClick = {
                                        val uri = ComposeFileProvider.getImageUri(context)
                                        imageUri = uri
                                        cameraLauncher.launch(uri)
                                        showPopUp = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text(
                                        text = "Take photo", color = Color.Black
                                    )
                                }

                                if (hasImage && imageUri != null) { //The "delete" button is shown only when the user has set a profile picture
                                    Button(
                                        modifier = Modifier.padding(top = 20.dp).border(2.dp, lightBlue, ButtonDefaults.shape),
                                        onClick = {
                                            setPhoto(null)
                                            imageUri = null
                                            hasImage = false
                                            showPopUp = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text(
                                            text = "Delete picture", color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
    If there a profile picture, this function displays it
 */
@Composable
fun SetImage(imageUri: Uri?, size: Int) {
    AsyncImage(
        model = imageUri,
        modifier = Modifier
            .border(1.dp, color = Color.LightGray, shape = CircleShape)
            .size(size.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
            .clip(CircleShape),
        contentDescription = "Image",
        contentScale = ContentScale.Fit
    )
    //Spacer(modifier = Modifier.height(16.dp))
}

/*
    This function creates the placeholder that is shown when there is no image.
    It consists of a circle with the initials of the name in the center if the name
    consists of more than one word, otherwise just the name.
    If the name is not set, the write "no image" is shown.
 */
@Composable
fun NoImage(name: String, size: Int) {
    if (name.isBlank()) {
        Text(
            text = "No image",
            modifier = Modifier
                .background(
                    lightBlue,
                    shape = RoundedCornerShape(size.dp)
                )
                .border(1.dp, color = Color.LightGray, shape = CircleShape)
                .size(size.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = (size/5.7).sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )
    } else {
        if(name.contains(" ")){
            var newTeamName = ""
            val words = name.split(" ")
            for (i in 0 until min(words.size, 3)) {
                if (words[i].isNotEmpty()) {
                    newTeamName += words[i][0].uppercaseChar()
                }
            }
            Text(
                text = newTeamName,
                modifier = Modifier
                    .background(
                        lightBlue,
                        shape = RoundedCornerShape(size.dp)
                    )
                    .border(1.dp, color = Color.LightGray, shape = CircleShape)
                    .size(size.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = (size / 3).sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        }else {
            var displayName = ""
            for (i in 0 until min(name.length, 3)) {
                displayName += name[i].uppercaseChar()
            }
            Text(
                text = displayName,
                modifier = Modifier
                    .background(
                        lightBlue,
                        shape = RoundedCornerShape(size.dp)
                    )
                    .border(1.dp, color = Color.LightGray, shape = CircleShape)
                    .size(size.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = (size / 3).sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        }
    }
}