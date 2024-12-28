package it.polito.BeeDone.team

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import it.polito.BeeDone.R
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.teamViewModel
import it.polito.BeeDone.utils.CreateTextFieldError
import it.polito.BeeDone.utils.lightBlue
import it.polito.BeeDone.utils.removeMemberAndRelatedData
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.function.Predicate

fun getQrCodeBitmap(link: String): Bitmap {
    val size = 512 //pixels
    val hints = hashMapOf<EncodeHintType, Int>().also {
        it[EncodeHintType.MARGIN] = 1
    } // Make the QR code buffer border narrower
    val bits = QRCodeWriter().encode(link, BarcodeFormat.QR_CODE, size, size, hints)
    return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
        for (x in 0 until size) {
            for (y in 0 until size) {
                it.setPixel(x, y, if (bits[x, y]) lightBlue.toArgb() else Color.White.toArgb())
            }
        }
    }
}

@Composable
fun ShareTeamPane(allUsers: MutableList<User>, selectedTeam: Team, db: FirebaseFirestore) {

    var userInvited by remember { mutableStateOf("") }
    var userInvitedError by remember { mutableStateOf("") }
    var userInvitedCorrectly by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val inviteLink = "https://www.beedone.com/invite/${selectedTeam.teamId}"

    fun setUserInvited(newUserInvited: String) {
        userInvited = newUserInvited
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(35.dp, 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier.size(250.dp)
        ) {
            AsyncImage(
                model = getQrCodeBitmap(inviteLink),
                contentDescription = "Invitation QR",
                modifier = Modifier.fillMaxSize(1f)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Max)
            ) {
                Text(
                    text = inviteLink,
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(15.dp))
                        .padding(8.dp),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic
                )
            }
            Column {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(inviteLink)) }) {
                    Icon(
                        painter = painterResource(R.drawable.copy_icon), contentDescription = "Copy"
                    )
                }
            }
        }

        CreateTextFieldError(
            value = userInvited,
            error = userInvitedError,
            setValue = { newUser -> setUserInvited(newUser) },
            keyboardType = KeyboardType.Text,
            label = "New User",
            placeholder = "@username"
        )

        Text(text = userInvitedCorrectly, color = Color(36, 133, 62))
        val coroutineScope = rememberCoroutineScope()

        FloatingActionButton(shape = RoundedCornerShape(25.dp),
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .border(2.dp, lightBlue, RoundedCornerShape(25.dp)),
            onClick = {
                coroutineScope.launch {
                    try {
                        val userDoc = db.collection("Users").document(userInvited).get().await()

                        // user not found
                        if (!userDoc.exists()) {
                            userInvitedError = "User Doesn't Exist"
                            userInvitedCorrectly = ""
                        } else {
                            val userToAdd = userDoc.toObject(User::class.java)!!

                            val teamMembers = selectedTeam.teamMembers.map { memberRef ->
                                db.collection("TeamMembers").document(memberRef).get().await()
                            }

                            val isUserAlreadyInTeam = teamMembers.any { memberDoc ->
                                val teamMember = memberDoc.toObject(TeamMember::class.java)
                                teamMember?.user == userToAdd.userNickname
                            }

                            if (isUserAlreadyInTeam) {
                                userInvitedError = "User already in the group!"
                                userInvitedCorrectly = ""
                            } else {
                                // user invited correctly
                                val userInTeamRef = db.collection("UserInTeam")
                                    .add(UserInTeam(selectedTeam.teamId, false)).await()
                                val userTeamsList =
                                    db.collection("Users").document(userToAdd.userNickname).get()
                                        .await().get("userTeams") as MutableList<String>
                                userTeamsList.add(userInTeamRef.id)
                                db.collection("Users").document(userToAdd.userNickname)
                                    .update("userTeams", userTeamsList)

                                val teamMemberRef = db.collection("TeamMembers")
                                    .add(TeamMember(userToAdd.userNickname, "Invited", 0)).await()
                                val teamMembersList =
                                    db.collection("Team").document(selectedTeam.teamId).get()
                                        .await().get("teamMembers") as MutableList<String>
                                teamMembersList.add(teamMemberRef.id)
                                db.collection("Team").document(selectedTeam.teamId)
                                    .update("teamMembers", teamMembersList)

                                userInvitedError = ""
                                userInvitedCorrectly = "User invited correctly!"
                            }
                        }
                    } catch (e: Exception) {
                        // handle the error appropriately
                        userInvitedError = "An error occurred: ${e.message}"
                        userInvitedCorrectly = ""
                    }
                }
            }) {
            Text(text = "Invite User")
        }
    }
}

@Composable
fun InviteUser(
    allTeams: MutableList<Team>,
    teamId: String,
    userToAdd: User,
    showTeamDetailsPane: (String) -> Unit,
    teamListPane: () -> Unit,
    acceptInvitationPane: (String) -> Unit,
    db: FirebaseFirestore
) {

    val teamToInvite = allTeams.find { it.teamId == teamId }

    var teamMembers = mutableListOf<TeamMember>()

    for (teamMemberRef in teamToInvite!!.teamMembers) {
        db.collection("TeamMembers").document(teamMemberRef).get()
            .addOnSuccessListener { teamMemberDoc ->
                teamMembers.add(teamMemberDoc.toObject(TeamMember::class.java)!!)
            }
    }

    if (teamToInvite != null) {

        //not in the team
        if (!teamMembers.map(TeamMember::user).contains(loggedUser.userNickname)) {
            //todo fare la add al db del nuovo membro
            //teamToInvite.teamUsers.add(TeamMember(userToAdd, Role.Invited, 0))
            loggedUser.userTeams.add(teamToInvite.teamId)
            acceptInvitationPane(teamId)
        }
        //already invited in the team
        else if (teamMembers.any { it.user == loggedUser.userNickname && it.role == "Invited" }) {
            acceptInvitationPane(teamId)
        }
        //already joined the team
        else {
            showTeamDetailsPane(teamToInvite.teamId)
        }
    }
    //Wrong id -> just show the team list
    else {
        teamListPane()
    }
}

@Composable
fun InvitedPane(
    showTeamDetailsPane: (String) -> Unit,
    navigateBack: () -> Unit,
    teamListPane: () -> Unit,
    selectedTeam: Team,
    db: FirebaseFirestore
) {
    var hours by remember { mutableStateOf(0) }
    var hoursError by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var idDelTeam by remember { mutableStateOf("") }

    LaunchedEffect(loggedUser, selectedTeam) {
        idDelTeam=selectedTeam.getIdUserInTeam(loggedUser)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                textAlign = TextAlign.Center,
                text = "YOU HAVE BEEN INVITED TO JOIN \"${selectedTeam.teamName}\"",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                fontSize = 16.sp,
                text = "If you want to join the team, enter the number of hours per week you want to dedicate to this team and click ACCEPT"
            )

            Spacer(modifier = Modifier.height(20.dp))

            CreateTextFieldError(
                value = hours.toString(),
                error = hoursError,
                setValue = { newVal ->
                    hours = newVal.toIntOrNull() ?: 0
                },
                label = "Number of hours",
                keyboardType = KeyboardType.Number,
            )

            Spacer(modifier = Modifier.height(20.dp))

            FloatingActionButton(
                shape = RoundedCornerShape(25.dp),
                containerColor = Color.White,
                modifier = Modifier.border(2.dp, lightBlue, RoundedCornerShape(25.dp)),
                onClick = {
                    if (hours < 0) {
                        hoursError = "Hours cannot be less than 0"
                    } else {
                        coroutineScope.launch {
                            isLoading = true
                            handleAcceptClick(
                                db,
                                selectedTeam,
                                hours,
                                navigateBack,
                                showTeamDetailsPane
                            )
                            isLoading = false
                        }
                    }
                }
            ) {
                Text(
                    modifier = Modifier.padding(25.dp, 0.dp),
                    text = "ACCEPT"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FloatingActionButton(
                shape = RoundedCornerShape(25.dp),
                containerColor = Color.White,
                modifier = Modifier.border(2.dp, lightBlue, RoundedCornerShape(25.dp)),
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        handleRejectClick(db, selectedTeam, teamListPane, idDelTeam)
                        isLoading = false
                    }
                }
            ) {
                Text(
                    modifier = Modifier.padding(25.dp, 0.dp),
                    text = "REJECT"
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

suspend fun handleAcceptClick(
    db: FirebaseFirestore,
    selectedTeam: Team,
    hours: Int,
    navigateBack: () -> Unit,
    showTeamDetailsPane: (String) -> Unit
) {
    try {
        var updated = false
        for (teamMemberRef in selectedTeam.teamMembers) {
            val teamMemberDoc = db.collection("TeamMembers").document(teamMemberRef).get().await()
            val teamMember = teamMemberDoc.toObject(TeamMember::class.java)
            if (teamMember?.user == loggedUser.userNickname && teamMember.role == "Invited") {
                db.collection("TeamMembers").document(teamMemberRef)
                    .update("role", "Participant").await()
                db.collection("TeamMembers").document(teamMemberRef)
                    .update("hours", hours).await()
                selectedTeam.teamMembers.remove(teamMemberRef)
                updated = true
            }
        }
        if (updated) {
            navigateBack()
            showTeamDetailsPane(selectedTeam.teamId)
        }
    } catch (e: Exception) {
        // handle the error appropriately
        Log.e("ShareTeamError", e.toString())

    }
}


suspend fun handleRejectClick(
    db: FirebaseFirestore,
    selectedTeam: Team,
    teamListPane: () -> Unit,
    idDelTeam: String
) {

    val teamMembersRefs = selectedTeam.teamMembers.toList()
    for (teamMemberRef in teamMembersRefs) {
        db.collection("TeamMembers").document(teamMemberRef).get()
            .addOnSuccessListener { teamMemberDoc ->

                val teamMember = teamMemberDoc.toObject(TeamMember::class.java)
                if (teamMember?.user == loggedUser.userNickname && teamMember.role == "Invited") {
                    removeMemberAndRelatedData(
                        db = db,
                        teamId = selectedTeam.teamId,
                        teamMembersId = teamMemberDoc.id,
                        userNickname = loggedUser.userNickname,
                        onSuccess = {
                            teamViewModel.allTeams.removeIf(Predicate { t -> t.teamId==selectedTeam.teamId })
                            teamViewModel.showingTeams.removeIf( Predicate { t->t.teamId==selectedTeam.teamId })
                            Log.d("Firestore" , "teamViewModel.allTeams : ${teamViewModel.allTeams.toString()}")
                            loggedUser.userTeams.remove(idDelTeam)
                            teamListPane()
                                    },
                        onFailure = {},
                        clearTeamInformation = {}

                    )
                }
            }
    }

    teamListPane()

}
