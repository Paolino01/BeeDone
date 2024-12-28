package it.polito.BeeDone.task

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.utils.lightBlue
import it.polito.BeeDone.utils.myShape
import java.text.SimpleDateFormat
import java.util.Date

//variables to handle attachment loading errors
var mediaError by mutableStateOf("")
    private set

var linkError by mutableStateOf("")
    private set

var documentError by mutableStateOf("")
    private set

//variable for image description
var mediaDescr by mutableStateOf("")
    private set

/**
 * allows you to manage and view the attachments of a specific task
 * attachments are images, links and documents
 **/
@SuppressLint("SimpleDateFormat")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowTaskAttachmentsPane(
    taskLinkValue: String,
    setLink: (String) -> Unit,
    taskMediaListValue: SnapshotStateList<Media>,
    setMediaList: (Media, Task, FirebaseFirestore) -> Unit,
    taskLinkListValue: MutableList<String>,
    setLinkList: (String, Task, FirebaseFirestore) -> Unit,
    taskDocumentListValue: SnapshotStateList<Document>,
    setDocumentList: (Document, Task, FirebaseFirestore) -> Unit,
    selectedTask: Task,
    db: FirebaseFirestore
) {

    //default icon values
    var isMedia by remember { mutableStateOf(true) }
    var isLinks by remember { mutableStateOf(false) }
    var isDocuments by remember { mutableStateOf(false) }

    //popup to insert attachments
    var showPopUp by remember { mutableStateOf(false) }

    ///variables to select images and documents from your phone
    var mediaUri by remember {
        mutableStateOf<Uri?>(Uri.EMPTY)
    }
    var documentUri by remember {
        mutableStateOf<Uri?>(Uri.EMPTY)
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            mediaUri = uri
        }
    )

    val context = LocalContext.current

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            documentUri = uri
        }
    )
    ///


    //the box is made up of the row of buttons,
    //the contents of the page and the button for adding attachments
    Box(
        Modifier
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd){

        /*row of buttons
         they allow me to switch between attachments */
        Row(modifier = Modifier.fillMaxHeight()) {
            Spacer(modifier = Modifier.height(10.dp)) //to have some space from the top bar
                FloatingActionButton(
                    onClick = {
                        isMedia = true
                        isLinks = false
                        isDocuments = false
                    },
                    shape = ButtonDefaults.shape,
                    containerColor = if(isMedia) lightBlue else Color.White,
                    modifier =
                    if(isMedia)
                        Modifier
                            .padding(10.dp)
                            .weight(1f)
                            .height(40.dp)
                    else
                        Modifier
                            .padding(10.dp)
                            .weight(1f)
                            .height(40.dp)
                            .border(3.dp, lightBlue, ButtonDefaults.shape)
                ) {
                    Text(text = "Media", modifier = Modifier.padding(19.dp, 0.dp))
                }
            Spacer(modifier = Modifier.weight(0.05f))
                FloatingActionButton(
                    onClick = {
                        isMedia = false
                        isLinks = true
                        isDocuments = false
                    },
                    shape = ButtonDefaults.shape,
                    containerColor = if(isLinks) lightBlue else Color.White,
                    modifier =
                    if(isLinks)
                        Modifier
                            .padding(10.dp)
                            .weight(1f)
                            .height(40.dp)
                    else
                        Modifier
                            .padding(10.dp)
                            .weight(1f)
                            .height(40.dp)
                            .border(3.dp, lightBlue, ButtonDefaults.shape)
                ) {
                    Text(text = "Links", modifier = Modifier.padding(19.dp, 0.dp))
                }
            Spacer(modifier = Modifier.weight(0.05f))
                FloatingActionButton(
                    onClick = {
                        isMedia = false
                        isLinks = false
                        isDocuments = true
                    },
                    shape = ButtonDefaults.shape,
                    containerColor = if(isDocuments) lightBlue else Color.White,
                    modifier =
                    if(isDocuments)
                        Modifier
                            .padding(10.dp)
                            .weight(1f)
                            .height(40.dp)
                    else
                        Modifier
                            .padding(10.dp)
                            .weight(1f)
                            .height(40.dp)
                            .border(3.dp, lightBlue, ButtonDefaults.shape)
                ) {
                    Text(text = "Documents", modifier = Modifier.padding(7.dp, 0.dp))
                }
        }

        ////code to show attachments
        if (isMedia) {
            LazyColumn(
                Modifier
                    .fillMaxHeight()
                    .padding(top = 75.dp), //the padding is used to avoid overwriting the buttons with media
                verticalArrangement = Arrangement.Top
            ) {
                items(taskMediaListValue) { media ->
                    // preview of the selected image
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                            //.clickable {
                            //    val intent = Intent(Intent.ACTION_VIEW).apply {
                            //        setDataAndType(media, "image/*")
                            //        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            //    }
                            //    context.startActivity(intent)
                            //},
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(modifier = Modifier.weight(1f)){
                            AsyncImage(
                                model = media.image,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentDescription = "Task Image",
                                contentScale = ContentScale.Fit,
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.05f))
                        Column(modifier = Modifier.weight(1f)){
                            Text(text = "Date: "+media.date + "\nDescription: " + media.mediaDescription)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
                }
            }
        }
        if (isLinks) {
            LazyColumn(
                Modifier
                    .fillMaxHeight()
                    .padding(top = 75.dp), //the padding is used to avoid overwriting the buttons with links
                verticalArrangement = Arrangement.Top
            ) {
                items(taskLinkListValue) { link ->
                    var uri: String
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = link,
                        modifier = Modifier.clickable {
                            //IMPORTANT: the app will crash if we try to open a link that doesn't start with "https://",
                            //so we check the link before opening it: if it doesn't contain "www." or "https://", we add them
                            uri = link
                            if(!link.startsWith("www.") && (!link.startsWith("https://") && !link.startsWith("http://"))) {
                                uri = "www.$uri"
                            }
                            if(!link.startsWith("https://") && !link.startsWith("http://")) {
                                uri = "https://$uri"
                            }

                            val urlIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(uri)
                            )
                            context.startActivity(urlIntent)
                        },
                        color = lightBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
                }
            }
        }
        if (isDocuments) {
            LazyColumn(
                Modifier
                    .fillMaxHeight()
                    .padding(top = 75.dp), //the padding is used to avoid overwriting the buttons with documents
                verticalArrangement = Arrangement.Top
            ) {
                items(taskDocumentListValue) { doc ->
                    Spacer(modifier = Modifier.height(6.dp))

                    /*the documentName variable gets the document name from the doc URI using
                    contentResolver().query(uri, null, null, null, null), which queries the
                    content pointed to by the URI. Next, the file name is retrieved from the
                    DISPLAY_NAME column of the cursor. If the document URI is null or the
                    document name is not available, a default text is displayed */
                    val documentName = doc.document.let { document ->
                        LocalContext.current.contentResolver.query(document.toUri(), null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            cursor.getString(nameIndex)
                        }
                    } ?: "Document Name Unavailable"
                    //

                    Text(
                        text = documentName,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(doc.document.toUri(), "application/*")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        },
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Date: "+doc.date,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(Modifier.padding(20.dp, 0.dp), thickness = Dp.Hairline, color = Color.Gray)
                }
            }
        }
        ////

        //button to add
        FloatingActionButton(
            onClick = { showPopUp = !showPopUp },
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
                contentDescription = "add attachment",
                Modifier.size(30.dp)
            )
        }


        /* popup management when I want to insert attachments */
        if(isMedia){
            if(showPopUp) {
                Column {
                    val dialogWidth = 410.dp / (1.3F)
                    val dialogHeight = 330.dp

                    if (showPopUp) {
                        Dialog(onDismissRequest = {
                            /*if I select an image but then exit the popup,
                            **when it reappears the image selected before is still there,
                            **I do this to remove it */
                            mediaUri = Uri.EMPTY

                            mediaDescr = ""
                            mediaError = "" //reset the popup after an error
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
                                        text = "Add image",
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(10.dp, 1.dp),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Left
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    FloatingActionButton(
                                        onClick = {
                                            mediaPicker.launch("image/*")
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(15.dp),
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .height(50.dp)
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
                                    ) {
                                        val buttonText: String
                                        if(mediaUri != null && mediaUri != Uri.EMPTY) {
                                            buttonText = mediaUri?.let { uri ->
                                                LocalContext.current.contentResolver.query(
                                                    uri,
                                                    null,
                                                    null,
                                                    null,
                                                    null
                                                )?.use { cursor ->
                                                    val nameIndex =
                                                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                                    cursor.moveToFirst()
                                                    cursor.getString(nameIndex)
                                                }
                                            } ?: "Select image"
                                        }
                                        else {
                                            buttonText = "Select image"
                                        }
                                        Text(
                                            text = buttonText,
                                                color = Color.DarkGray
                                            )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))

                                    if (mediaError.isNotBlank()) {
                                        Text(mediaError, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Right)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))

                                    OutlinedTextField(
                                        value = mediaDescr,
                                        onValueChange = {
                                            mediaDescr = it
                                        },
                                        label = { Text("Media description") },
                                        isError = false,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        shape = myShape,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = lightBlue,
                                            focusedLabelColor = lightBlue,
                                            focusedTextColor = Color.DarkGray
                                        ),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(13.dp))

                                    FloatingActionButton(
                                        onClick = {
                                            if (mediaUri != null && mediaUri != Uri.EMPTY) {
                                                setMediaList(Media(mediaUri!!.toString(), mediaDescr, SimpleDateFormat("dd/MM/yyyy").format(Date())), selectedTask, db)
                                                mediaUri = Uri.EMPTY
                                                mediaDescr = ""
                                                showPopUp = false
                                            } else {
                                                mediaError = "The field cannot be empty"
                                            }
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                            .height(30.dp)
                                            .width(120.dp)

                                    ) {
                                        Text(text = "Upload", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    FloatingActionButton(
                                        onClick = {
                                            mediaUri = Uri.EMPTY //so the previously selected image does not remain
                                            mediaDescr = ""
                                            mediaError = ""
                                            showPopUp = false
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                            .height(30.dp)
                                            .width(120.dp)
                                    ) {
                                        Text(text = "Cancel", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        if(isLinks){
            if(showPopUp) {
                Column {
                    val dialogWidth = 400.dp / (1.3F)
                    val dialogHeight = 260.dp

                    if (showPopUp) {
                        Dialog(onDismissRequest = {
                            //the fields are reset when clicked out of the popup
                            setLink("")
                            linkError = ""
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
                                        text = "Add link",
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(10.dp, 1.dp),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Left
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = taskLinkValue,
                                        onValueChange = {
                                            setLink(it)
                                        },
                                        label = { Text("https://example.com") },
                                        isError = linkError.isNotBlank(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        shape = myShape,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = lightBlue,
                                            focusedLabelColor = lightBlue,
                                            focusedTextColor = Color.DarkGray
                                        ),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))

                                    if (linkError.isNotBlank()) {
                                        Text(linkError, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Right)
                                    }

                                    Spacer(modifier = Modifier.height(13.dp))

                                    FloatingActionButton(
                                        onClick = {
                                            if (taskLinkValue.isNotEmpty()) {
                                                setLinkList(taskLinkValue, selectedTask, db)
                                                setLink("") // Reset text field after adding link
                                                showPopUp = false
                                            } else {
                                                linkError = "The field cannot be empty"
                                            }
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                            .height(30.dp)
                                            .width(120.dp)
                                    ) {
                                        Text(text = "Confirm", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    FloatingActionButton(
                                        onClick = {
                                            setLink("")
                                            linkError = ""
                                            showPopUp = false
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                            .height(30.dp)
                                            .width(120.dp)
                                    ) {
                                        Text(text = "Cancel", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(15.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        if(isDocuments){
            if(showPopUp) {
                Column {
                    val dialogWidth = 400.dp / (1.3F)
                    val dialogHeight = 500.dp / 2

                    if (showPopUp) {
                        Dialog(onDismissRequest = {
                            documentUri = Uri.EMPTY
                            documentError = ""
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
                                        text = "Add document",
                                        color = Color.Black,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(10.dp, 1.dp),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Left
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    FloatingActionButton(
                                        onClick = {
                                             documentPicker.launch(arrayOf("application/pdf", "application/msword", "application/vnd.ms-excel"))
                                        },
                                        shape = RoundedCornerShape(15.dp),
                                        containerColor = Color.White,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .height(50.dp)
                                            .fillMaxWidth()
                                            .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
                                    ) {
                                        val buttonText: String
                                        if(documentUri != null && documentUri != Uri.EMPTY) {
                                            buttonText = documentUri?.let { uri ->
                                                LocalContext.current.contentResolver.query(
                                                    uri,
                                                    null,
                                                    null,
                                                    null,
                                                    null
                                                )?.use { cursor ->
                                                    val nameIndex =
                                                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                                    cursor.moveToFirst()
                                                    cursor.getString(nameIndex)
                                                }
                                            } ?: "Select file"
                                        }
                                        else {
                                            buttonText = "Select file"
                                        }
                                        Text(
                                            text = buttonText,
                                            color = Color.DarkGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    if (documentError.isNotBlank()) {
                                        Text(documentError, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Right)
                                    }

                                    Spacer(modifier = Modifier.height(13.dp))

                                    FloatingActionButton(
                                        onClick = {
                                            if (documentUri != null && documentUri != Uri.EMPTY) {
                                                setDocumentList(Document(documentUri!!.toString(), SimpleDateFormat("dd/MM/yyyy").format(Date())), selectedTask, db)
                                                documentUri = Uri.EMPTY
                                                showPopUp = false
                                            } else {
                                                documentError = "The field cannot be empty"
                                            }
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                            .height(30.dp)
                                            .width(120.dp)
                                    ) {
                                        Text(text = "Upload", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    FloatingActionButton(
                                        onClick = {
                                            documentUri = Uri.EMPTY
                                            documentError = ""
                                            showPopUp = false
                                        },
                                        containerColor = Color.White,
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                            .height(30.dp)
                                            .width(120.dp)
                                    ) {
                                        Text(text = "Cancel", color = Color.Black)
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
}