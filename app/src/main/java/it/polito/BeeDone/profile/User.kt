package it.polito.BeeDone.profile

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.polito.BeeDone.task.Task
import it.polito.BeeDone.taskViewModel
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.team.UserInTeam
import it.polito.BeeDone.teamViewModel

class User(
    var userImage: String?,
    var userFirstName: String,
    var userLastName: String,
    var userNickname: String,
    var userMail: String,
    var userLocation: String,
    var userDescription: String,
    var userBirthDate: String,
    var userStatus: String,
    var userTeams: MutableList<String>,          //For every team the user is in, save if he has any messages to read (true) or not (false)
    var userTasks: MutableList<String>,
    var userChat: MutableList<String>,
    var password: String
) {
    constructor() : this(
        null, "", "",
        "", "", "", "",
        "", "", mutableStateListOf(), mutableListOf(),
        mutableStateListOf(),
        ""
    )

    public fun deleteTeam(team: Team){


        userTeams.removeIf { it == team.teamId }

        /*
        if(userTeams.map { it.first }.contains(team)) {
            val teamToRemove: Pair<Team, Boolean> = userTeams.find { it.first == team }!!
            userTeams.remove(teamToRemove)
        }
         */

    }

    public fun deleteTask(task: String) {
        userTasks.remove(task)
    }
}

/**
 * Since we don't have yet an actual team creation, we created 4 teams manually for
 * test purpose. We also added 3 users to test all the functionalities.
 * The logged user is by default john smith
 */

/** Temporary profile*/
var loggedUser: User = User()


class UserViewModel() : ViewModel()

{

    var allUsers = mutableStateListOf<User>()

    fun addUser(u: User){
        allUsers.add(u)
    }

    //PROFILE PICTURE
    var photoValue: Uri? by mutableStateOf(null)
        private set

    fun setPhoto(i: Uri?) {
        photoValue = i
    }

    //FIRST NAME
    var firstNameValue by mutableStateOf("")
        private set
    var firstNameError by mutableStateOf("")
        private set

    fun setFirstName(n: String) {
        firstNameValue =
            n.replaceFirstChar { it.uppercase() } //returns the string with only the initial capital letter
    }

    private fun checkFirstName() {
        if (firstNameValue.isBlank()) {
            firstNameError = "First name cannot be blank"
        } else {
            firstNameError = ""
        }
    }

    //LAST NAME
    var lastNameValue by mutableStateOf("")
        private set
    var lastNameError by mutableStateOf("")
        private set

    fun setLastName(n: String) {
        lastNameValue = n.replaceFirstChar { it.uppercase() }
    }

    private fun checkLastName() {
        if (lastNameValue.isBlank()) {
            lastNameError = "Last name cannot be blank"
        } else {
            lastNameError = ""
        }
    }

    //NICKNAME
    var nicknameValue by mutableStateOf("")
        private set
    var nicknameError by mutableStateOf("")
        private set

    fun setNickname(n: String) {
        nicknameValue = n
    }

    private fun checkNickname() {
        if (nicknameValue.isBlank()) {
            nicknameError = "Nickname cannot be blank"
        } else if (nicknameValue.contains(' ')) {
            nicknameError = "Nickname cannot contain spaces"
        } else if(!nicknameValue.startsWith('@')) {
            nicknameError = "Nickname must start with @"
        } else if(nicknameValue.filter { it == '@' }.length != 1) {
            nicknameError = "Nickname can contain only one @"
        } else if(allUsers.filter{ u -> u.userNickname==nicknameValue }.isNotEmpty()){
            nicknameError = "Nickname already exists"
        }
        else{
            nicknameError = ""
        }
    }

    //MAIL
    var mailValue by mutableStateOf("")
        private set
    var mailError by mutableStateOf("")
        private set

    fun setMail(m: String) {
        mailValue = m
    }

    private fun checkMail() {
        if (mailValue.isBlank()) {
            mailError = "Email cannot be blank"
        } else if (!mailValue.contains('@')) {
            mailError = "Invalid email address"
        } else {
            mailError = ""
        }
    }

    var confirmationCodeValue by mutableStateOf("")
        private set
    var confirmationCodeError by mutableStateOf("")
        private set

    fun setConfirmationCode(c: String) {
        confirmationCodeValue = c
    }

    fun openConfirmMailPane(confirmMailPane: () -> Unit) {
        checkMail()
        if (mailError.isBlank()) {
            confirmMailPane()
        }
    }

    fun checkConfirmationCode() {
        if (confirmationCodeValue.isBlank()) {
            confirmationCodeError = "Confirmation code cannot be blank"
        } else {
            if (confirmationCodeValue.length != 4) {
                confirmationCodeError = "Confirmation code must have 4 characters"
            } else {
                confirmationCodeError = ""
            }
        }
    }

    fun validateConfirmationCode(editUserInformationPane: () -> Unit) {
        checkConfirmationCode()
        if (confirmationCodeError.isBlank()) {
            oldMailValue = mailValue
            confirmationCodeValue = ""
            editUserInformationPane()
        }
    }

    //LOCATION
    var locationValue by mutableStateOf("")
        private set
    var locationError by mutableStateOf("")
        private set

    fun setLocation(l: String) {
        locationValue = l.replaceFirstChar { it.uppercase() }
    }

    private fun checkLocation() {
        if (locationValue.isBlank()) {
            locationError = "Location cannot be blank"
        } else {
            locationError = ""
        }
    }

    private fun checkPassword() {
        if (passwordValue.isBlank()) {
            passwordError = "You must set a password"
        } else {
            passwordError = ""
        }
    }

    //DESCRIPTION
    var descriptionValue by mutableStateOf("")
        private set

    fun setDescription(d: String) {
        descriptionValue = d
    }

    //BIRTH DATE
    var birthDateValue by mutableStateOf("")
        private set
    var birthDateError by mutableStateOf("")
        private set

    fun setBirthDate(b: String) {
        birthDateValue = b
    }

    private fun checkBirthDate() {
        if (birthDateValue.isBlank()) {
            birthDateError = "Birth date cannot be blank"
        } else {
            birthDateError = ""
        }
    }

    //STATUS
    var statusValue by mutableStateOf("")
        private set

    fun setStatus(s: String) {
        statusValue = s
    }

    //PASSWORD
    var passwordValue by mutableStateOf("")
        private set
    var passwordConfirmation by mutableStateOf("")
        private set
    var passwordError by mutableStateOf("")
        private set

    fun setPassword(p: String) {
        passwordValue = p
    }

    fun getPasswordError() : Int {
        return passwordError.length;
    }

    fun setConfirmationPassword(p: String) {
        passwordConfirmation = p
    }

    fun changingPassword(editPasswordPane: () -> Unit) {
        editPasswordPane()  // switch to editing state
    }

    fun validatePassword(navigateBack: () -> Unit) {
        checkNewPassword()
        if (passwordError.isBlank()) {
            val db = Firebase.firestore
            if(loggedUser.userNickname != "") {     //If loggedUser.userNickname == "", we are creating a new user. So, we don't set the password in the DB here
                db.collection("Users").document(loggedUser.userNickname)
                    .update("userPassword", passwordValue)
            }

            /*
            I make sure that when I return to the password page the fields are
            always empty (and do not contain the previously entered value)
            */
            //passwordValue = ""
            passwordConfirmation = ""
            navigateBack()
        }
    }

    private fun checkNewPassword() {
        if (passwordValue.isBlank()) {
            passwordError = "Password cannot be blank"
        } else if (passwordValue.length < 8) {
            passwordError = "Password should be at least 8 character long"

        } else if (passwordValue != passwordConfirmation) {
            passwordError = "Password should match"
        } else {
            passwordError = ""
        }
    }

    private fun checkLoginPassword() {
        if(passwordValue.isBlank()) {
            passwordError = "Password cannot be blank"
        }
        else {
            passwordError = ""
        }
    }

    //MESSAGE
    var userMessageValue by mutableStateOf("")
        private set
    fun setUserMessage(n: String) {
        userMessageValue = n
    }

    private var oldPhotoValue: Uri? = photoValue
    private lateinit var oldFirstNameValue: String
    private lateinit var oldLastNameValue: String
    private lateinit var oldNicknameValue: String
    private var oldMailValue: String = ""
    private lateinit var oldLocationValue: String
    private lateinit var oldDescriptionValue: String
    private lateinit var oldBirthDateValue: String
    private lateinit var oldStatusValue: String


    //validate the new information after edit user information
    fun validate(showUserInformationPane: (String) -> Unit, navigateBack: (() -> Unit)? = null) {
        checkFirstName()
        checkLastName()
        checkMail()
        checkBirthDate()
        checkLocation()

        //the user has changed the email and it needs to be revalidated
        if (mailValue != oldMailValue) {
            mailError = "Confirm mail"
        }

        if (lastNameError.isBlank() && firstNameError.isBlank() && mailError.isBlank() && nicknameError.isBlank() && birthDateError.isBlank() && locationError.isBlank()) {
            //Updates the information of the currently logged user (updates everything except for password)

            val db= Firebase.firestore
            db.collection("Users").document(nicknameValue).update("userBirthDate", birthDateValue)
            db.collection("Users").document(nicknameValue).update("userDescription", descriptionValue)
            db.collection("Users").document(nicknameValue).update("userFirstName", firstNameValue)
            db.collection("Users").document(nicknameValue).update("userImage", photoValue.toString())
            db.collection("Users").document(nicknameValue).update("userLastName", lastNameValue)
            db.collection("Users").document(nicknameValue).update("userLocation", locationValue)
            db.collection("Users").document(nicknameValue).update("userMail", mailValue)
            db.collection("Users").document(nicknameValue).update("userStatus", statusValue)

            oldPhotoValue = photoValue
            oldFirstNameValue = firstNameValue
            oldLastNameValue = lastNameValue
            oldNicknameValue = nicknameValue
            oldMailValue = mailValue
            oldLocationValue = locationValue
            oldDescriptionValue = descriptionValue
            oldBirthDateValue = birthDateValue
            oldStatusValue = statusValue

            loggedUser.userImage = photoValue.toString()
            loggedUser.userFirstName = firstNameValue
            loggedUser.userLastName = lastNameValue
            loggedUser.userNickname = nicknameValue
            loggedUser.userMail = mailValue
            loggedUser.userLocation = locationValue
            loggedUser.userDescription = descriptionValue
            loggedUser.userBirthDate = birthDateValue
            loggedUser.userStatus = statusValue


            if(navigateBack != null) {
                navigateBack()
            }
            showUserInformationPane(loggedUser.userNickname)
        }
    }


    //on a graphic level it is < in the top bar
    fun exitWithoutUserInformationUpdate(navigateBack: () -> Unit) {
        revertUserInformationChanges()

        navigateBack()
    }

    fun revertUserInformationChanges() {
        setPhoto(oldPhotoValue)
        setFirstName(oldFirstNameValue)
        setLastName(oldLastNameValue)
        setNickname(oldNicknameValue)
        setMail(oldMailValue)
        setLocation(oldLocationValue)
        setDescription(oldDescriptionValue)
        setBirthDate(oldBirthDateValue)
        setStatus(oldStatusValue)

        //Deletes all the error messages
        firstNameError = ""
        lastNameError = ""
        nicknameError = ""
        mailError = ""
        locationError = ""
        birthDateError = ""
    }

    fun clearUserInformation() {
        setPhoto(null)
        setFirstName("")
        setLastName("")
        setNickname("")
        setMail("")
        setLocation("")
        setDescription("")
        setBirthDate("")
        setStatus("")
        setPassword("")
        setUserLogin("")

        oldPhotoValue = null
        oldFirstNameValue = ""
        oldLastNameValue = ""
        oldNicknameValue = ""
        oldMailValue = ""
        oldLocationValue = ""
        oldDescriptionValue = ""
        oldBirthDateValue = ""
        oldStatusValue = ""

        firstNameError = ""
        lastNameError = ""
        nicknameError = ""
        mailError = ""
        locationError = ""
        birthDateError = ""
    }

    fun exitWithoutCodeUpdate(editUserInformationPane: () -> Unit) {
        setConfirmationCode("")
        confirmationCodeError = ""
        editUserInformationPane()
    }

    fun exitWithoutPasswordUpdate(editUserInformationPane: () -> Unit) {
        setPassword("")
        passwordError = ""
        setConfirmationPassword("")
        editUserInformationPane()
    }

    fun setProfileInformation(selectedUser: User) {
        setFirstName(selectedUser.userFirstName)
        setLastName(selectedUser.userLastName)
        setMail(selectedUser.userMail)
        setBirthDate(selectedUser.userBirthDate)
        setDescription(selectedUser.userDescription)
        setLocation(selectedUser.userLocation)
        setNickname(selectedUser.userNickname)
        setPhoto(if(selectedUser.userImage == null) null else selectedUser.userImage!!.toUri())
        setStatus(selectedUser.userStatus)
    }

    fun setOldProfileInformation() {
        oldPhotoValue = photoValue
        oldFirstNameValue = firstNameValue
        oldLastNameValue = lastNameValue
        oldNicknameValue = nicknameValue
        oldMailValue = mailValue
        oldLocationValue = locationValue
        oldDescriptionValue = descriptionValue
        oldBirthDateValue = birthDateValue
        oldStatusValue = statusValue
    }

    fun validateLogin(sharedPreferences: SharedPreferences, homePane:() -> Unit) {
        checkUserLogin()
        checkLoginPassword()
        val db=Firebase.firestore
        val users = db.collection("Users")

        if(userLoginError.isBlank() && passwordError.isBlank()) {

            users.document(userLoginValue).get()
                .addOnSuccessListener {
                    d -> if (d!=null){
                        if (d.get("userPassword")==passwordValue){
                            authError = ""
                            loggedUser.userImage = d.get("userImage").toString()
                            loggedUser.userNickname=d.get("userNickname").toString()
                            loggedUser.userStatus=d.get("userStatus").toString()
                            loggedUser.userFirstName=d.get("userFirstName").toString()
                            loggedUser.userLastName=d.get("userLastName").toString()
                            loggedUser.userBirthDate=d.get("userBirthDate").toString()
                            loggedUser.userDescription=d.get("userDescription").toString()
                            loggedUser.userLocation=d.get("userLocation").toString()
                            loggedUser.userMail=d.get("userMail").toString()
                            loggedUser.userStatus=d.get("userStatus").toString()
                            loggedUser.userChat = d.get("userChat") as MutableList<String>
                            loggedUser.userTeams = d.get("userTeams") as MutableList<String>
                            loggedUser.userTasks = d.get("userTasks") as MutableList<String>

                            //Retrieve all the teams of the loggedUser and populate the list allTeams
                            var userInTeam: UserInTeam?
                            teamViewModel.allTeams = mutableStateListOf()

                            if(loggedUser.userTeams.size != 0) {
                                for (userInTeamRef in loggedUser.userTeams) {
                                    db.collection("UserInTeam").document(userInTeamRef)
                                        .addSnapshotListener { userInTeamDoc, err1 ->
                                            if (err1 == null) {
                                                if (userInTeamDoc != null) {
                                                    userInTeam =
                                                        userInTeamDoc.toObject(UserInTeam::class.java)
                                                    if (userInTeam != null) {
                                                        db.collection("Team")
                                                            .document(userInTeam!!.first)
                                                            .addSnapshotListener { teamDoc, err2 ->
                                                                if (err2 == null) {
                                                                    if (teamDoc != null) {
                                                                        val team =
                                                                            teamDoc.toObject(Team::class.java)
                                                                        if (team != null) {
                                                                            if(!(teamViewModel.allTeams.map {it.teamId}.contains(team.teamId))) {
                                                                                teamViewModel.allTeams.add(
                                                                                    team
                                                                                )
                                                                            }
                                                                        } else {
                                                                            Log.e(
                                                                                TAG,
                                                                                "Team object is null"
                                                                            )
                                                                        }
                                                                    } else {
                                                                        Log.e(
                                                                            TAG,
                                                                            "teamDoc is null"
                                                                        )
                                                                    }
                                                                } else {
                                                                    Log.e(TAG, err2.toString())
                                                                }
                                                            }
                                                    } else {
                                                        Log.e(TAG, "UserInTeam object is null")
                                                    }
                                                } else {
                                                    Log.e(TAG, "userInTeamDoc is null")
                                                }
                                            } else {
                                                Log.e(TAG, err1.toString())
                                            }
                                        }
                                }
                            }

                            //Retrieve all the tasks of the loggedUser and populate the list allTasks
                            taskViewModel.allTasks = mutableStateListOf()

                            if(loggedUser.userTasks.size != 0) {
                                db.collection("Tasks").whereIn("taskId", loggedUser.userTasks)
                                    .addSnapshotListener { taskDocs, e ->
                                        if (e == null) {
                                            taskViewModel.allTasks.clear()
                                            for (taskDoc in taskDocs!!) {
                                                taskViewModel.allTasks.add(taskDoc.toObject(Task::class.java))
                                            }
                                        } else {
                                            Log.e(TAG, e.toString())
                                        }
                                    }
                            }

                            saveData(sharedPreferences)
                            homePane()
                        }else{
                            //passworrd errata
                            authError = "Incorrent nickname and/or password"
                        }
                    }else{
                        authError = "Incorrent nickname and/or password"

                    //nickname non esistente

                    }
                }.addOnFailureListener {e ->
                    //Failure
                    authError = "Failure: $e"
                }
        }
    }

    fun validateLoginWithGoogle(sharedPreferences: SharedPreferences, homePane:() -> Unit) {
        val db=Firebase.firestore
        val users = db.collection("Users")

        users.whereEqualTo("userMail", userLoginValue).get().addOnSuccessListener { d ->
            if(d.documents.size == 1) {
                authError = ""
                loggedUser.userImage = d.documents[0].get("userImage").toString()
                loggedUser.userNickname=d.documents[0].get("userNickname").toString()
                loggedUser.userStatus=d.documents[0].get("userStatus").toString()
                loggedUser.userFirstName=d.documents[0].get("userFirstName").toString()
                loggedUser.userLastName=d.documents[0].get("userLastName").toString()
                loggedUser.userBirthDate=d.documents[0].get("userBirthDate").toString()
                loggedUser.userDescription=d.documents[0].get("userDescription").toString()
                loggedUser.userLocation=d.documents[0].get("userLocation").toString()
                loggedUser.userMail=d.documents[0].get("userMail").toString()
                loggedUser.userChat = d.documents[0].get("userChat") as MutableList<String>
                loggedUser.userTeams = d.documents[0].get("userTeams") as MutableList<String>
                loggedUser.userTasks = d.documents[0].get("userTasks") as MutableList<String>

                ///Retrieve all the teams of the loggedUser and populate the list allTeams
                var userInTeam: UserInTeam?
                teamViewModel.allTeams = mutableStateListOf()

                if(loggedUser.userTeams.size != 0) {
                    for (userInTeamRef in loggedUser.userTeams) {
                        db.collection("UserInTeam").document(userInTeamRef)
                            .addSnapshotListener { userInTeamDoc, err1 ->
                                if (err1 == null) {
                                    if (userInTeamDoc != null) {
                                        userInTeam =
                                            userInTeamDoc.toObject(UserInTeam::class.java)
                                        if (userInTeam != null) {
                                            db.collection("Team")
                                                .document(userInTeam!!.first)
                                                .addSnapshotListener { teamDoc, err2 ->
                                                    if (err2 == null) {
                                                        if (teamDoc != null) {
                                                            val team =
                                                                teamDoc.toObject(Team::class.java)
                                                            if (team != null) {
                                                                if(!(teamViewModel.allTeams.map {it.teamId}.contains(team.teamId))) {
                                                                    teamViewModel.allTeams.add(
                                                                        team
                                                                    )
                                                                }
                                                            } else {
                                                                Log.e(
                                                                    TAG,
                                                                    "Team object is null"
                                                                )
                                                            }
                                                        } else {
                                                            Log.e(
                                                                TAG,
                                                                "teamDoc is null"
                                                            )
                                                        }
                                                    } else {
                                                        Log.e(TAG, err2.toString())
                                                    }
                                                }
                                        } else {
                                            Log.e(TAG, "UserInTeam object is null")
                                        }
                                    } else {
                                        Log.e(TAG, "userInTeamDoc is null")
                                    }
                                } else {
                                    Log.e(TAG, err1.toString())
                                }
                            }
                    }
                }

                //Retrieve all the tasks of the loggedUser and populate the list allTasks
                taskViewModel.allTasks = mutableStateListOf()

                if(loggedUser.userTasks.size != 0) {
                    db.collection("Tasks").whereIn("taskId", loggedUser.userTasks)
                        .addSnapshotListener { taskDocs, e ->
                            if (e == null) {
                                taskViewModel.allTasks.clear()
                                for (taskDoc in taskDocs!!) {
                                    taskViewModel.allTasks.add(taskDoc.toObject(Task::class.java))
                                }
                            } else {
                                Log.e(TAG, e.toString())
                            }
                        }
                }

                saveData(sharedPreferences)
                homePane()
            }
            else {
                authError = "User not found"
            }
        }
    }

    fun saveData(sharedPreferences: SharedPreferences) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        //Set nickname and pwd value with key in Shared Preferences
        editor.putString("nickname", loggedUser.userNickname)
        editor.putString("pwd", passwordValue)
        editor.putBoolean("animation", true)

        editor.apply()  //Apply changes to SharedPreferences
    }

    fun removeData(sharedPreferences: SharedPreferences) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        //Set nickname and pwd value with key in Shared Preferences
        editor.putString("nickname", "")
        editor.putString("pwd", "")

        editor.apply()  //Apply changes to SharedPreferences
    }

    //Variable needed in the login pane. The user can insert his nickname or his email
    var userLoginValue by mutableStateOf("")
        private set
    var userLoginError by mutableStateOf("")
        private set
    var authError by mutableStateOf("")
        private set
    fun setUserLogin(n: String) {
        userLoginValue = n
    }
    fun checkUserLogin() {
        if(userLoginValue.isBlank()) {
            userLoginError = "Nickname/Email cannot be blank"
        }
        else {
            userLoginError = ""
        }
    }

    //validation afterSignup
    fun validateSignUp(
        showHomePane: () -> Unit,
        navigateBack: (() -> Unit)? = null,
        sharedPreferences: SharedPreferences
    ) {
        checkFirstName()
        checkLastName()
        checkMail()
        checkNickname()
        checkBirthDate()
        checkLocation()
        checkPassword()

        //the user has changed the email and it needs to be revalidated
        if (oldMailValue.isEmpty()){
            mailError = "Confirm mail"
        }
        else if (mailValue != oldMailValue) {
            mailError = "Confirm mail"
        }

        if (passwordValue.isNotEmpty() && lastNameError.isBlank() && firstNameError.isBlank() && mailError.isBlank() && nicknameError.isBlank() && birthDateError.isBlank() && locationError.isBlank()) {

            val u = hashMapOf(
                "userImage" to photoValue,
                "userFirstName" to firstNameValue,
                "userLastName" to lastNameValue,
                "userNickname" to nicknameValue,
                "userMail" to mailValue,
                "userLocation" to locationValue,
                "userDescription" to descriptionValue,
                "userBirthDate" to birthDateValue,
                "userStatus" to statusValue,
                "userPassword" to passwordValue,
                "userChat" to mutableListOf<String>(),
                "userTasks" to mutableListOf<String>(),
                "userTeams" to mutableListOf<String>()
            )

            val db= Firebase.firestore
            val users = db.collection("Users")
            users.document(nicknameValue).get()
                .addOnSuccessListener {
                    d ->
                    if (d!=null) {
                        if(d.get("userNickname")!=nicknameValue){
                            users.document(nicknameValue).set(u)

                            oldPhotoValue = photoValue
                            oldFirstNameValue = firstNameValue
                            oldLastNameValue = lastNameValue
                            oldNicknameValue = nicknameValue
                            oldMailValue = mailValue
                            oldLocationValue = locationValue
                            oldDescriptionValue = descriptionValue
                            oldBirthDateValue = birthDateValue
                            oldStatusValue = statusValue

                            //Updates the information of the currently logged user
                            loggedUser.userImage = photoValue.toString()
                            loggedUser.userFirstName = firstNameValue
                            loggedUser.userLastName = lastNameValue
                            loggedUser.userNickname = nicknameValue
                            loggedUser.userMail = mailValue
                            loggedUser.userLocation = locationValue
                            loggedUser.userDescription = descriptionValue
                            loggedUser.userBirthDate = birthDateValue
                            loggedUser.userStatus = statusValue
                            loggedUser.userChat = mutableListOf()
                            loggedUser.userTeams = mutableListOf()
                            loggedUser.userTasks = mutableListOf()
                            loggedUser.password= passwordValue

                            teamViewModel.allTeams = mutableStateListOf()
                            taskViewModel.allTasks = mutableStateListOf()
                            addUser(loggedUser)
                        }else{
                            nicknameError = "Nickname already in DB"
                        }
                    }
                }
                .addOnFailureListener {e ->
                    Log.e(TAG, e.toString())
                }

            saveData(sharedPreferences)     //Save Login Data into SharedPreferences
            showHomePane()
        }
    }

    fun validateSignUpWithGoogle(
        showHomePane: () -> Unit,
        navigateBack: (() -> Unit)? = null,
        sharedPreferences: SharedPreferences
    ) {
        checkFirstName()
        checkLastName()
        checkNickname()
        checkBirthDate()
        checkLocation()

        if (lastNameError.isBlank() && firstNameError.isBlank() && nicknameError.isBlank() && birthDateError.isBlank() && locationError.isBlank()) {
            val u = hashMapOf(
                "userImage" to photoValue,
                "userFirstName" to firstNameValue,
                "userLastName" to lastNameValue,
                "userNickname" to nicknameValue,
                "userMail" to mailValue,
                "userLocation" to locationValue,
                "userDescription" to descriptionValue,
                "userBirthDate" to birthDateValue,
                "userStatus" to statusValue,
                "userChat" to mutableListOf<String>(),
                "userTasks" to mutableListOf<String>(),
                "userTeams" to mutableListOf<String>()
            )

            val db= Firebase.firestore
            val users = db.collection("Users")
            users.document(nicknameValue).get()
                .addOnSuccessListener {
                        d ->
                    if (d!=null) {
                        if(d.get("userNickname")!=nicknameValue){
                            users.document(nicknameValue).set(u)

                            oldPhotoValue = photoValue
                            oldFirstNameValue = firstNameValue
                            oldLastNameValue = lastNameValue
                            oldNicknameValue = nicknameValue
                            oldMailValue = mailValue
                            oldLocationValue = locationValue
                            oldDescriptionValue = descriptionValue
                            oldBirthDateValue = birthDateValue
                            oldStatusValue = statusValue

                            //Updates the information of the currently logged user
                            loggedUser.userImage = photoValue.toString()
                            loggedUser.userFirstName = firstNameValue
                            loggedUser.userLastName = lastNameValue
                            loggedUser.userNickname = nicknameValue
                            loggedUser.userMail = mailValue
                            loggedUser.userLocation = locationValue
                            loggedUser.userDescription = descriptionValue
                            loggedUser.userBirthDate = birthDateValue
                            loggedUser.userStatus = statusValue
                            loggedUser.userChat = mutableListOf()
                            loggedUser.userTeams = mutableListOf()
                            loggedUser.userTasks = mutableListOf()

                            teamViewModel.allTeams = mutableStateListOf()
                            taskViewModel.allTasks = mutableStateListOf()
                            addUser(loggedUser)
                        }else{
                            nicknameError = "Nickname already in DB"
                        }
                    }
                }
                .addOnFailureListener {e ->
                    Log.e(TAG, e.toString())
                }

            //saveData(sharedPreferences)     //Save Login Data into SharedPreferences
            showHomePane()
        }
    }
}


