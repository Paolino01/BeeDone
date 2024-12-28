package it.polito.BeeDone.team

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.chat.Message
import it.polito.BeeDone.profile.loggedUser
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

class Team(
    var teamId: String,
    var teamName: String,
    var teamMembers: MutableList<String>,
    var teamTasks: MutableList<String>,
    var teamDescription: String,
    var teamCategory: String,
    var teamCreationDate: String,
    var teamImage: String,
    var teamCreator: String,
    var teamChat: MutableList<String>
) {
    constructor() : this(
        UUID.randomUUID()
            .toString(),       //We use UUID to generate the IDs of the teams. It generates a random ID of 128 bits
        "",
        mutableStateListOf(),
        mutableListOf(),
        "",
        "",
        "",
        "",
        loggedUser.userNickname,
        mutableStateListOf()
    )

    public fun addUser(loggedUser: User, participant: String, hours: Int, db: FirebaseFirestore) {

        db.collection("Users").document(loggedUser.userNickname).get().addOnSuccessListener { d ->
                if (d != null) {
                    Log.d("xxx", "user taken")
                    val userTeamsDb = d.get("userTeams") as MutableList<String>
                    userTeamsDb.add("/Team/$teamId")

                    db.collection("Users").document(loggedUser.userNickname)
                        .update("userTeams", userTeamsDb).addOnSuccessListener {
                            Log.d("xxx", "team added to userTeams")
                        }.addOnFailureListener {
                            Log.d("xxx", "error adding team to userTeams")
                        }

                }
            }.addOnFailureListener { e ->
                Log.d("xxx", "failure taking user teams", e)
            }

        db.collection("TeamMembers").where(
            Filter.and(
                Filter.equalTo("user", loggedUser.userNickname), Filter.equalTo("team", teamId)
            )
        ).get().addOnSuccessListener {

            }.addOnFailureListener { }

        teamMembers.add("User/${loggedUser.userNickname}")
    }

    /*
    public fun addUsers(users: MutableList<String>){
        teamUsers = users.toMutableStateList()
    }
*//*DELETE USER BY THE TEAM
    * Search in teamUsers for a team member whose user is the same as the Profile passed
    * and whose role is Participant or Admin.
    * If it finds such a member, it removes it from the teamUsers list.
    * Moreover it removes the team from the list of teams the user belongs to.
    */
    public fun deleteUser(user: String) {
        val db = Firebase.firestore
        val users = mutableListOf<String>()
        var teamMember: String;
        var userTeams = mutableListOf<String>()


        //taking teamMember to delete
        db.collection("TeamMembers").where(
            Filter.and(
                Filter.equalTo("user", user), Filter.equalTo("team", this.teamId)
            )
        ).get().addOnSuccessListener { d ->
                teamMember = d.documents[0].id

                //taking the new teamMembers
                db.collection("Team").document(this.teamId).get().addOnSuccessListener { doc ->
                        val teamUsers: MutableList<String> =
                            doc.get("teamUsers") as MutableList<String>;

                        teamUsers.remove(teamMember);

                        //taking the list of team of the user
                        db.collection("Team").document(this.teamId).update("teamMembers", teamUsers)
                            .addOnSuccessListener { //teamUsers updated
                                db.collection("Users").document(user).get()
                                    .addOnSuccessListener { docL ->
                                        userTeams = docL.get("UserTeams") as MutableList<String>
                                        userTeams.remove(this.teamId)
                                        //update the list
                                        db.collection("Users").document(user)
                                            .update("userTeams", userTeams)
                                    }

                                // }


                            }.addOnFailureListener { e ->
                                Log.e(TAG, e.toString())
                            }


                    }
            }
    }


    //set time
    fun setTimeTeamUser(user: String, i: Int) {
        var db = Firebase.firestore
        var idTeamMember: String;

        db.collection("TeamMembers").where(
            Filter.and(
                Filter.equalTo("user", user), Filter.equalTo("team", this.teamId)
            )
        ).get().addOnSuccessListener { d ->
            idTeamMember = d.documents[0].id

            db.collection("TeamMembers").document(idTeamMember).update("hours", i)

        }/*
            val member = teamUsers.find { it.user == user}
            member?.let {
                it.timePartecipation = i
            }

     */
    }

    /* get role
Check if the user is part of this Team.
 If it is, I get his role, otherwise I assume it has role = participant
*/
    suspend fun getRoleTeamUser(user: User): String {
        val db = Firebase.firestore
        var memberRole = ""

        for (teamMemberRef in this.teamMembers) {
            val teamMemberDoc = db.collection("TeamMembers").document(teamMemberRef).get().await()
            val tmpTeamMember = teamMemberDoc.toObject(TeamMember::class.java)
            if (tmpTeamMember != null && tmpTeamMember.user == user.userNickname) {
                memberRole = tmpTeamMember.role
                break
            }
        }

        return memberRole
    }

    suspend fun getIdTeamUser(user: User): String {
        val db = Firebase.firestore
        var id = ""

        for (teamMemberRef in this.teamMembers) {
            val teamMemberDoc = db.collection("TeamMembers").document(teamMemberRef).get().await()
            val tmpTeamMember = teamMemberDoc.toObject(TeamMember::class.java)
            val tmpId = teamMemberDoc.id
            if (tmpTeamMember != null && tmpTeamMember.user == user.userNickname) {
                id = tmpId
                break
            }
        }

        return id
    }

    suspend fun getIdUserInTeam(user: User): String {
        val db = Firebase.firestore
        var id = ""

        for (uInTeam in user.userTeams){
            val userInTeamDoc = db.collection("UserInTeam").document(uInTeam).get().await()
            val tmpUserInTeam = userInTeamDoc.toObject(UserInTeam::class.java)
            val tmpId = userInTeamDoc.id
            if (tmpUserInTeam != null && tmpUserInTeam.first == this.teamId) {
                id = tmpId
                break
            }
        }

        return id
    }
}

@SuppressLint("MutableCollectionMutableState")
class TeamViewModel : ViewModel() {
    var allTeams = mutableStateListOf<Team>()
    var showingTeams = mutableStateListOf<Team>()

    fun addTeam(newTeam: Team) {
        allTeams.add(newTeam)
    }

    //Team Name
    var teamNameValue by mutableStateOf("")
        private set
    var teamNameError by mutableStateOf("")
        private set

    fun setTeamName(n: String) {
        teamNameValue = n
    }

    private fun checkName() {
        if (teamNameValue == "") {
            teamNameError = "Name cannot be empty"
        } else {
            teamNameError = ""
        }
    }

    //Team Image
    var teamImageValue: Uri? by mutableStateOf(null)
        private set

    fun setTeamImage(i: Uri?) {
        teamImageValue = i
    }

    //Team Description
    var teamDescriptionValue by mutableStateOf("")
        private set

    fun setTeamDescription(n: String) {
        teamDescriptionValue = n
    }

    //Team Category
    var teamCategoryValue by mutableStateOf("")
        private set
    var teamCategoryError by mutableStateOf("")
        private set

    fun setTeamCategory(n: String) {
        teamCategoryValue = n
    }

    private fun checkCategory() {
        if (teamCategoryValue == "") {
            teamCategoryError = "Category cannot be empty"
        } else {
            teamCategoryError = ""
        }
    }

    //Team Image
    var userSelected: User? by mutableStateOf(null)
        private set

    fun setTeUserSelected(i: User?) {
        userSelected = i
    }

    //Team Chat
    //Single message written by the user
    var teamMessageValue by mutableStateOf("")
        private set

    fun setTeamMessage(n: String) {
        teamMessageValue = n
    }

    //List of Messages sent to the team
    var teamChatValue by mutableStateOf(
        mutableListOf<Message>()
    )
        private set

    @SuppressLint("SimpleDateFormat")
    fun setTeamChat(n: String, selectedTeam: Team) {
     /*   val db = Firebase.firestore
        val message = hashMapOf(
            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
            "message" to n,
            "sender" to loggedUser.userNickname,
            "time" to SimpleDateFormat("hh:mm").format(Date())
        )
        selectedTeam.teamChat.clear()
        //msg added to all messages
        db.collection("Messages").add(message).addOnSuccessListener { documentReference ->
                selectedTeam.teamChat.add(documentReference.toString())
                //pushing on list
                //update db
                db.collection("Team").document(selectedTeam.teamId)
                    .update("teamChat", selectedTeam.teamChat)

                documentReference.id
            }

        //teamChatValue.add(q)
        // selectedTeam.teamChat = selectedTeam.teamChat.add(message)  //Updates selectedTeam and, consequently, the list of teams
    */
    }


    //riceve in input selectedTeam.teamChat
    //teamChat contiene i riferimenti ai messaggi che sono in Messages
    @SuppressLint("SimpleDateFormat")
    fun assignTeamChat(n: MutableList<String>) {
        teamChatValue.clear()

        val addedMessageIds: MutableSet<String> = mutableSetOf()
        val db = Firebase.firestore


            for (l in n) {
                if (!addedMessageIds.contains(l)) {
                    db.collection("Messages").document(l).get().addOnSuccessListener { d ->
                        if (d != null && d.exists()) {
                            val message = d.toObject(Message::class.java)
                            if (message != null) {
                                teamChatValue.add(message)
                                addedMessageIds.add(l)
                                Log.d("assignTeamChat", "Added message: ${l}")
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "Error getting message", e)
                    }
                }
            }

    }


    private var oldTeamNameValue: String = teamNameValue
    private var oldTeamPhotoValue: Uri? = teamImageValue
    private var oldTeamCategoryValue: String = teamCategoryValue
    private var oldTeamDescriptionValue: String = teamDescriptionValue

    /* information validation */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateTeamInformation(
        navController: NavHostController, navigateBack: () -> Unit, team: Team?
    ) {
        val db = Firebase.firestore
        var selectedTeam: Team = team ?: Team()

        checkName()
        checkCategory()

        var listTeam = mutableListOf<String>()

        if (teamNameError.isBlank() && teamCategoryError.isBlank()) {
            if (navController.currentBackStackEntry?.destination?.route == "CreateTeamPane") {
                selectedTeam = Team(
                    "",
                    teamNameValue,
                    mutableListOf<String>(),
                    mutableListOf<String>(),
                    teamDescriptionValue,
                    teamCategoryValue,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
                    teamImageValue.toString(),
                    loggedUser.userNickname,
                    mutableStateListOf()
                )

                val member = hashMapOf(
                    "hours" to 0, "role" to "Admin", "user" to loggedUser.userNickname
                )

                //create team member
                db.collection("TeamMembers").add(member).addOnSuccessListener { teamMembersRef ->

                        selectedTeam.teamMembers.add(teamMembersRef.id)

                    //add team to DB
                    db.collection("Team").add(selectedTeam).addOnSuccessListener { teamRef ->
                        selectedTeam.teamId = teamRef.id

                        db.collection("UserInTeam").add(UserInTeam(teamRef.id, false))
                            .addOnSuccessListener { userInTeamRef ->

                                db.collection("Users").document(loggedUser.userNickname).get()
                                    .addOnSuccessListener { d ->
                                        listTeam = d.get("userTeams") as MutableList<String>
                                        listTeam.add(userInTeamRef.id)

                                        db.collection("Users").document(loggedUser.userNickname)
                                            .update("userTeams", listTeam)
                                    }

                                db.collection("Team").document(teamRef.id)
                                    .update("teamId", teamRef.id)

                            }
                        }
                    }

                allTeams.add(selectedTeam)
                setTeamInformation(selectedTeam)

            } else { //you are coming from EditTeamPane
                //Saves edit to a Team.
                selectedTeam.teamImage = teamImageValue.toString()
                selectedTeam.teamName = teamNameValue
                selectedTeam.teamDescription = teamDescriptionValue
                selectedTeam.teamCategory = teamCategoryValue

                //Updates the information of the selected team
                val db= Firebase.firestore
                db.collection("Team").document(selectedTeam.teamId).update("teamImage", teamImageValue.toString())
                db.collection("Team").document(selectedTeam.teamId).update("teamName", teamNameValue)
                db.collection("Team").document(selectedTeam.teamId).update("teamDescription", teamDescriptionValue)
                db.collection("Team").document(selectedTeam.teamId).update("teamCategory", teamCategoryValue)
            }


            //you have to set old=new
            oldTeamPhotoValue = teamImageValue
            oldTeamNameValue = teamNameValue
            oldTeamDescriptionValue = teamDescriptionValue
            oldTeamCategoryValue = teamCategoryValue

            navigateBack()
        }
    }

    //function set the old values, ignoring the changes
    fun noUpdateTeamInformation() {
        //set old values
        teamImageValue = oldTeamPhotoValue
        teamNameValue = oldTeamNameValue
        teamDescriptionValue = oldTeamDescriptionValue
        teamCategoryValue = oldTeamCategoryValue

        //clear old error values
        teamNameError = ""
        teamCategoryError = ""
    }

    //function clear all values of team
    fun clearTeamInformation() {
        //reset infos
        teamImageValue = null
        teamNameValue = ""
        teamDescriptionValue = ""
        teamCategoryValue = ""


        //reset errors
        teamNameError = ""
        teamCategoryError = ""
    }

    fun setTeamInformation(selectedTeam: Team) {
        //set new values
        setTeamImage(selectedTeam.teamImage.toUri())
        setTeamName(selectedTeam.teamName)
        setTeamDescription(selectedTeam.teamDescription)
        setTeamCategory(selectedTeam.teamCategory)


        assignTeamChat(selectedTeam.teamChat)

        //set old=new
        oldTeamPhotoValue = teamImageValue
        oldTeamNameValue = teamNameValue
        oldTeamDescriptionValue = teamDescriptionValue
        oldTeamCategoryValue = teamCategoryValue
    }
}
