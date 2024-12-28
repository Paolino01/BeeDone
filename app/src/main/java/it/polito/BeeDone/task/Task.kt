package it.polito.BeeDone.task

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.profile.User
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.task.history.Event
import it.polito.BeeDone.utils.questions_answers.Answer
import it.polito.BeeDone.utils.questions_answers.Question
import it.polito.BeeDone.task.timer.TaskTimer
import it.polito.BeeDone.taskViewModel
import it.polito.BeeDone.team.Team
import it.polito.BeeDone.teamViewModel
import it.polito.BeeDone.utils.addSpacesToSentence
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

private val taskCount: AtomicInteger = AtomicInteger(1)


var taskStatus = listOf("Pending", "In Progress", "Completed", "Expired Not Completed", "Expired Completed")

class Task(
    var taskId: String,
    var taskTitle: String,
    var taskDescription: String,
    var taskDeadline: String,
    var taskTag: String,
    var taskCategory: String,
    var taskUsers: MutableList<String>,
    var taskRepeat: String,
    var taskSubtasks: MutableList<String>,
    var taskTeam: String,
    var taskHistory: MutableList<String>,
    var taskQuestions: MutableList<String>,
    var taskStatus: String,
    var taskTimerHistory: MutableList<String>,
    var taskMediaList: MutableList<String>,
    var taskLinkList: MutableList<String>,
    var taskDocumentList: MutableList<String>,
    var taskCreator: String,
    var taskCreationDate: String
) {
    @RequiresApi(Build.VERSION_CODES.O)
    constructor() : this(
        taskId = "",
        taskTitle = "",
        taskDescription = "",
        taskDeadline = "",
        taskTag = "",
        taskCategory = "",
        taskUsers = mutableListOf(),
        taskRepeat = "No Repeat",
        taskSubtasks = mutableStateListOf(),
        taskTeam = "",
        taskHistory = mutableListOf(),
        taskQuestions = mutableListOf(),
        taskStatus = "Pending",
        taskTimerHistory = mutableListOf(),
        taskMediaList = mutableStateListOf(),
        taskLinkList = mutableStateListOf(),
        taskDocumentList = mutableStateListOf(),
        loggedUser.userNickname,
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    )

    public fun deleteTask(db: FirebaseFirestore) {
        val taskUsers: MutableList<User> = mutableListOf()
        var taskTeam: Team = Team()

        for (userRef in this.taskUsers) {
            db.collection("Users").document(userRef)
                .get()
                .addOnSuccessListener {
                    doc->
                    taskUsers.add(doc!!.toObject(User::class.java)!!)
                }
                .addOnFailureListener {
                    e ->
                    Log.e("Firestore", "Error getting team data", e)
                }
        }

        for (u in taskUsers) {
            u.deleteTask("Tasks/${this.taskId}")
        }

        db.collection("Team").document(this.taskTeam)
            .get()
            .addOnSuccessListener {
                    doc->
                    taskTeam = doc!!.toObject(Team::class.java)!!
            }
            .addOnFailureListener {
                e ->
                Log.e("Firestore", "Error getting team data", e)
            }
        taskTeam.teamTasks.remove(this.taskId)

        db.collection("Tasks").document(this.taskId).delete()

        taskViewModel.allTasks.remove(this)
    }
}

//this is the task that is currently shown

@SuppressLint("MutableCollectionMutableState")
class TaskViewModel : ViewModel() {

    var allTasks = mutableStateListOf<Task>()
    var showingTasks = mutableStateListOf<Task>()

    //Task Title
    var taskIdValue by mutableStateOf("")
        private set
    var taskIdError by mutableStateOf("")
        private set

    //Task Title
    var taskTitleValue by mutableStateOf("")
        private set
    var taskTitleError by mutableStateOf("")
        private set

    fun setTaskTitle(n: String) {
        taskTitleValue =
            n.replaceFirstChar { it.uppercase() }   //returns the string with only the initial capital letter
    }

    private fun checkTitle() {
        if (taskTitleValue == "") {
            taskTitleError = "Title cannot be empty"
        } else {
            taskTitleError = ""
        }
    }

    //Task Description
    var taskDescriptionValue by mutableStateOf("")
        private set

    fun setTaskDescription(n: String) {
        taskDescriptionValue = n
    }

    //Task Deadline
    var taskDeadlineValue by mutableStateOf("")
        private set
    var taskDeadlineError by mutableStateOf("")
        private set

    fun setTaskDeadline(n: String) {
        taskDeadlineValue = n
    }

    private fun checkDeadline() {
        if (taskDeadlineValue == "") {
            taskDeadlineError = "Deadline cannot be empty"
        } else {
            taskDeadlineError = ""
        }
    }

    //Task Tag
    var taskTagValue by mutableStateOf("")
        private set

    fun setTaskTag(n: String) {
        taskTagValue = n
    }

    //Task Category
    var taskCategoryValue by mutableStateOf("")
        private set
    var taskCategoryError by mutableStateOf("")
        private set

    fun setTaskCategory(n: String) {
        taskCategoryValue = n
    }

    private fun checkCategory() {
        if (taskCategoryValue == "") {
            taskCategoryError = "Category cannot be empty"
        } else {
            taskCategoryError = ""
        }
    }

    //Task Users
    var taskUsersValue by mutableStateOf(       //List of Users assigned to a certain task
        mutableListOf<User>()
    )
        private set


    fun addTask(newTask: Task) {
        allTasks.add(newTask)
    }

    fun setTaskUsers(n: User) {
        taskUsersValue.add(n)
    }

    fun deleteTaskUsers(n: User) {
        taskUsersValue.remove(n)
    }

    fun assignTaskUsers(n: MutableList<String>, db: FirebaseFirestore) {
        taskUsersValue = mutableListOf()

        for (userRef in n) {
            db.collection("Users").document(userRef)
                .addSnapshotListener() {
                        doc, e ->
                    if(e == null) {
                        taskUsersValue.add(doc!!.toObject(User::class.java)!!)
                    }
                    else {
                        Log.e("Firestore", "Error getting team data", e)
                    }
                }
        }
    }

    //Task Repeat
    var taskRepeatValue by mutableStateOf("No Repeat")
        private set

    fun setTaskRepeat(n: String) {
        taskRepeatValue = n
    }

    //Task subtasks
    //Variable used to insert a new subtask in the list of the subtasks
    var taskSubtaskValue by mutableStateOf("")
        private set

    /*   fun setTaskSubtask(n: String) {
           taskSubtaskValue = n
       } */

    var taskSubtasksValue = mutableListOf<Subtask>()
        private set

    /*   fun setTaskSubtasks(n: String) {
           taskSubtasksValue.add(n)
       } */
    fun assignTaskSubtasks(n: MutableList<String>, db: FirebaseFirestore) {
        taskSubtasksValue = mutableListOf()
        for (subtaskRef in n) {
            db.collection("Subtasks").document(subtaskRef)
                .addSnapshotListener {
                    doc, e ->
                    if(e == null) {
                        taskSubtasksValue.add(doc!!.toObject(Subtask::class.java)!!)
                    }
                    else {
                        Log.e("Firestore", "Error getting Subtask data", e)
                    }
                }
        }
    }

    fun addTaskSubtasks(n: Subtask) {
        taskSubtasksValue.add(n)
    }

    //Task Team
    var taskTeamValue by mutableStateOf(Team())
        private set
    var taskTeamError by mutableStateOf("")
        private set

    fun setTaskTeam(n: String, db: FirebaseFirestore) {
        db.collection("Team").document(n)
            .addSnapshotListener {
                    doc, e ->
                if(e == null) {
                    taskTeamValue = doc!!.toObject(Team::class.java)!!
                }
                else {
                    Log.e("Firestore", "Error getting team data", e)
                }
            }
    }

    fun checkTaskTeam() {
        if(taskTeamValue.teamName == "") {
            taskTeamError = "Team cannot be blank"
        }
        else {
            taskTeamError = ""
        }
    }

    //attachments of a task
    var taskMediaListValue by mutableStateOf(
        mutableStateListOf<Media>()
    )
        private set

    var taskLinkListValue = mutableListOf<String>()

    /*by mutableStateOf(
        mutableStateListOf<String>()
    )*/
        private set

    var taskDocumentListValue by mutableStateOf(
        mutableStateListOf<Document>()
    )
        private set

    var taskLinkValue by mutableStateOf("")
        private set

    fun setTaskMediaList(n: Media, selectedTask: Task, db: FirebaseFirestore) {
        taskMediaListValue.add(n)

        val mediaToAdd = hashMapOf(
            "date" to n.date,
            "image" to n.image,
            "mediaDescription" to n.mediaDescription
        )

        db.collection("Media").add(mediaToAdd).addOnSuccessListener { mediaRef ->
            selectedTask.taskMediaList.add(mediaRef.id)

            db.collection("Tasks").document(selectedTask.taskId).get().addOnSuccessListener { d ->
                val taskMediaList = d.get("taskMediaList") as MutableList<String>
                taskMediaList.add(mediaRef.id)
                db.collection("Tasks").document(selectedTask.taskId).update("taskMediaList", taskMediaList)
            }
        }
    }
    fun assignTaskMediaList(n: MutableList<String>, db: FirebaseFirestore) {
        taskMediaListValue = mutableStateListOf()

        for (mediaRef in n) {
            db.collection("Media").document(mediaRef)
                .addSnapshotListener { doc, e ->
                    if(e == null) {
                        taskMediaListValue.add(doc!!.toObject(Media::class.java)!!)
                    }
                    else {
                        Log.e("Firestore", "Error getting team data", e)
                    }
                }
        }
    }

    fun setTaskLink(n: String) {
        taskLinkValue = n
    }

    fun setTaskLinkList(n: String, selectedTask: Task, db: FirebaseFirestore) {
        taskLinkListValue.add(n)

        db.collection("Tasks").document(selectedTask.taskId).update("taskLinkList", taskLinkListValue)
    }
    fun assignTaskLinkList(n: MutableList<String>) {
        taskLinkListValue = n
    }

    fun setTaskDocumentList(n: Document, selectedTask: Task, db: FirebaseFirestore) {
        taskDocumentListValue.add(n)

        val documentToAdd = hashMapOf(
            "date" to n.date,
            "document" to n.document
        )

        db.collection("Document").add(documentToAdd).addOnSuccessListener { documentRef ->
            selectedTask.taskDocumentList.add(documentRef.id)

            db.collection("Tasks").document(selectedTask.taskId).get().addOnSuccessListener { d ->
                val taskDocumentList = d.get("taskDocumentList") as MutableList<String>
                taskDocumentList.add(documentRef.id)
                db.collection("Tasks").document(selectedTask.taskId).update("taskDocumentList", taskDocumentList)
            }
        }
    }
    fun assignTaskDocumentList(n: MutableList<String>, db: FirebaseFirestore) {
        taskDocumentListValue = mutableStateListOf()

        for (documentRef in n) {
            db.collection("Document").document(documentRef)
                .addSnapshotListener { doc, e ->
                    if(e == null) {
                        taskDocumentListValue.add(doc!!.toObject(Document::class.java)!!)
                    }
                    else {
                        Log.e("Firestore", "Error getting team data", e)
                    }
                }
        }

    }

    //Task Questions
    //Single instance of Task Question
    var taskQuestionValue by mutableStateOf("")       //Text of a new question
        private set

    fun setTaskQuestion(n: String) {
        taskQuestionValue = n
    }

    //List of Task Questions
    var taskQuestionsValue by mutableStateOf(               //List of all questions related to a certain task
        mutableListOf<Question>()
    )
        private set

    @SuppressLint("SimpleDateFormat")
    fun setTaskQuestions(n: String, selectedTask: Task, db: FirebaseFirestore) {
        val questionToAdd = hashMapOf(
            "questionId" to "",
            "text" to n,
            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
            "user" to loggedUser.userNickname,
            "answers" to mutableListOf<String>()
        )

        db.collection("Questions").add(questionToAdd).addOnSuccessListener {taskQuestionRef ->
            selectedTask.taskQuestions.add(taskQuestionRef.id)
            db.collection("Questions").document(taskQuestionRef.id).update("questionId", taskQuestionRef.id)
            db.collection("Tasks").document(selectedTask.taskId).update("taskQuestions", selectedTask.taskQuestions)

            val q = Question(
                taskQuestionRef.id,
                n,
                SimpleDateFormat("dd/MM/yyyy").format(Date()),
                loggedUser.userNickname,
                mutableListOf()
            )
            taskQuestionsValue.add(q)
        }
    }

    fun assignTaskQuestions(n: MutableList<String>, db: FirebaseFirestore) {
        taskQuestionsValue = mutableListOf()
        for (questionRef in n) {
            db.collection("Questions").document(questionRef)
                .get()
                .addOnSuccessListener {
                        doc ->
                    taskQuestionsValue.add(doc.toObject(Question::class.java)!!)
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting team data", exception)
                }
        }
    }

    //Task Answers
    //Single instance of Task Answer
    var taskAnswerValue by mutableStateOf("")           //Text of a new answer
        private set

    fun setTaskAnswer(n: String) {
        taskAnswerValue = n
    }

    //List of Task Answers
    var taskAnswersValue by mutableStateOf(                   //List of all answers related to a certain question
        mutableListOf<Answer>()
    )
        private set

    @SuppressLint("SimpleDateFormat")
    fun setTaskAnswers(n: String, selectedQuestion: Question, db: FirebaseFirestore) {
        val answerToAdd = hashMapOf(
            "text" to n,
            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
            "user" to loggedUser.userNickname
        )

        db.collection("Answers").add(answerToAdd).addOnSuccessListener {answerRef ->
            selectedQuestion.answers.add(answerRef.id)
            db.collection("Questions").document(selectedQuestion.questionId).update("answers", selectedQuestion.answers)

            val a = Answer(
                n,
                SimpleDateFormat("dd/MM/yyyy").format(Date()),
                loggedUser.userNickname
            )
            taskAnswersValue.add(a)
        }
    }

    fun assignTaskAnswers(n: MutableList<String>, db: FirebaseFirestore) {
        taskAnswersValue = mutableListOf()
        for (answerRef in n) {
            db.collection("Answers").document(answerRef)
                .get()
                .addOnSuccessListener { doc ->
                    taskAnswersValue.add(doc.toObject(Answer::class.java)!!)
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting team data", exception)
                }
        }
    }

    //Task History
    //Variable used to insert a new user in the list of the users

    var taskHistoryValue by mutableStateOf(
        mutableListOf<Event>()
    )
        private set

    fun addTaskEventToHistory(n: Event) {
        taskHistoryValue.add(n)
    }

    fun assignTaskHistory(n: MutableList<String>, db: FirebaseFirestore) {
        for (eventRef in n) {
            db.collection("TaskHistory").document(eventRef)
                .get()
                .addOnSuccessListener {
                        doc ->
                    taskHistoryValue.add(doc.toObject(Event::class.java)!!)
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting team data", exception)
                }
        }
    }

    var taskStatusValue by mutableStateOf("Pending")

    fun setTaskStatus(n: String) {
        taskStatusValue = n
    }

    var taskTimerTitleValue by mutableStateOf("")
        private set
    fun setTaskTimerTitle(n: String) {
        taskTimerTitleValue = n
    }

    var taskTimerValue by mutableStateOf("0:00:00")
        private set
    fun setTaskTimer(n: String) {
        taskTimerValue = n
    }

    var taskTimerHistory by mutableStateOf<MutableList<TaskTimer>>(mutableListOf())
        private set
    @SuppressLint("SimpleDateFormat")
    fun addTaskTimerHistory(n: Int, selectedTask: Task, db: FirebaseFirestore) {
        taskTimerHistory.add(
            0,
            TaskTimer(
                ticks = n,
                date = SimpleDateFormat("dd/MM/yyyy").format(Date()),
                title = taskTimerTitleValue,
                user = loggedUser.userNickname
            )
        )

        val taskTimerToAdd = hashMapOf(
            "ticks" to n,
            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
            "title" to taskTimerTitleValue,
            "user" to loggedUser.userNickname
        )

        db.collection("TaskTimerHistory").add(taskTimerToAdd).addOnSuccessListener { taskTimerHistoryRef ->
            selectedTask.taskTimerHistory.add(taskTimerHistoryRef.id)
            db.collection("Tasks").document(selectedTask.taskId).get().addOnSuccessListener {d ->
                val taskTimerHistoryList = d.get("taskTimerHistory") as MutableList<String>
                taskTimerHistoryList.add(taskTimerHistoryRef.id)
                db.collection("Tasks").document(selectedTask.taskId).update("taskTimerHistory", taskTimerHistoryList)
            }
        }
    }
    fun assignTaskTimerHistory(n: MutableList<String>, db: FirebaseFirestore) {
        taskTimerHistory = mutableListOf()

        for (taskTimerRef in n) {
            db.collection("TaskTimerHistory").document(taskTimerRef)
                .get()
                .addOnSuccessListener {
                        doc ->
                    taskTimerHistory.add(doc.toObject(TaskTimer::class.java)!!)
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting team data", exception)
                }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setTaskAsCompleted() {
        if(LocalDate.parse(taskDeadlineValue, DateTimeFormatter.ofPattern("dd/MM/uuuu")) < LocalDate.now()){
            taskStatusValue = "Expired Completed"
        }else{
            taskStatusValue = "Completed"
        }
        addTaskEventToHistory(
            Event(
                "Task Completed",
                SimpleDateFormat("dd/MM/yyyy").format(Date()),
                taskStatusValue,
                loggedUser.userNickname,
                taskSubtasksValue.size.toString(),
                taskSubtasksValue.size.toString(),
                mutableListOf()
            )
        )
    }

    private var oldTaskTitleValue: String = taskTitleValue
    private var oldTaskCategoryValue: String = taskCategoryValue
    private var oldTaskDeadlineValue: String = taskDeadlineValue
    private var oldTaskDescriptionValue: String = taskDescriptionValue
    private var oldTaskTagValue: String = taskTagValue
    private var oldTaskUsersValue: MutableList<User> = taskUsersValue.toMutableList()
    private var oldTaskRepeatValue: String = taskRepeatValue
    private var oldTaskSubtasksValue: SnapshotStateList<Subtask> = taskSubtasksValue.toMutableStateList()
    private var oldTaskSubtaskValue: String = taskSubtaskValue
    private var oldTaskTeamValue: Team = taskTeamValue
    private var oldTaskHistoryValue: MutableList<Event> = taskHistoryValue.toMutableList()
    private var oldTaskStatusValue: String = taskStatusValue

    /* information validation */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun validateTaskInformation(
        showTaskDetailsPane: ((String) -> Unit)? = null,               //When I am creating a task, I want to navigate to showTaskDetailsPane
        navigateBack: (() -> Unit)? = null,                         //When I am editing a task, I want to navigate back
        task: Task?,
        db: FirebaseFirestore
    ) {
        var selectedTask: Task
        if(task != null) {
            selectedTask = task
        }
        else {
            selectedTask = Task()
        }

        checkTitle()
        checkDeadline()
        checkCategory()
        checkTaskTeam()

        if (taskTitleError.isBlank() && taskDeadlineError.isBlank() && taskCategoryError.isBlank() && taskTeamError.isBlank()) {

            if (showTaskDetailsPane != null) {          //If showTaskDetailsPane is not null, I am creating a new tsk. Otherwise, I am editing a task, so navigateBack isn't null
                if (taskUsersValue.size>0){
                    taskStatusValue="In Progress"
                }else{
                    taskStatusValue="Pending"
                }
                val tmpTask = Task(
                    "",
                    taskTitleValue,
                    taskDescriptionValue,
                    taskDeadlineValue,
                    taskTagValue,
                    taskCategoryValue,
                    taskUsersValue.map{it.userNickname}.toMutableList(),
                    taskRepeatValue,
                    //taskSubtasksValue.map{it.subtaskId}.toMutableStateList(),
                    selectedTask.taskSubtasks,
                    taskTeamValue.teamId,
                    //taskHistoryValue.map{it.eventId}.toMutableStateList(),
                    selectedTask.taskHistory,
                    mutableListOf<String>(),
                    taskStatusValue,
                    mutableListOf(),
                    mutableStateListOf(),
                    mutableStateListOf(),
                    mutableStateListOf(),
                    loggedUser.userNickname,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                )

                val taskToAdd = hashMapOf(
                    "taskCategory" to taskCategoryValue,
                    "taskCreationDate" to tmpTask.taskCreationDate,
                    "taskCreator" to tmpTask.taskCreator,
                    "taskDeadline" to tmpTask.taskDeadline,
                    "taskDescription" to tmpTask.taskDescription,
                    "taskDocumentList" to tmpTask.taskDocumentList,
                    "taskHistory" to tmpTask.taskHistory,
                    "taskId" to "",
                    "taskLinkList" to tmpTask.taskLinkList,
                    "taskMediaList" to tmpTask.taskMediaList,
                    "taskQuestions" to tmpTask.taskQuestions,
                    "taskRepeat" to tmpTask.taskRepeat,
                    "taskStatus" to tmpTask.taskStatus,
                    "taskSubtasks" to tmpTask.taskSubtasks,
                    "taskTag" to tmpTask.taskTag,
                    "taskTeam" to tmpTask.taskTeam,
                    "taskTimerHistory" to tmpTask.taskTimerHistory,
                    "taskTitle" to tmpTask.taskTitle,
                    "taskUsers" to tmpTask.taskUsers
                )

                //Add task to DB
                db.collection("Tasks").add(taskToAdd).addOnSuccessListener { taskRef ->
                    tmpTask.taskId = taskRef.id

                    db.collection("Tasks").document(taskRef.id)
                        .update("taskId", taskRef.id)

                    db.collection("Team").document(tmpTask.taskTeam).get()
                        .addOnSuccessListener { d ->
                            val teamTaskList = d.get("teamTasks") as MutableList<String>
                            teamTaskList.add(taskRef.id)
                            db.collection("Team").document(tmpTask.taskTeam).update("teamTasks", teamTaskList)
                        }

                    for(userRef in tmpTask.taskUsers) {
                        db.collection("Users").document(userRef).get()
                            .addOnSuccessListener {d ->
                                val userTaskList = d.get("userTasks") as MutableList<String>
                                userTaskList.add(taskRef.id)
                                db.collection("Users").document(userRef).update("userTasks", userTaskList)
                            }
                    }

                    addTask(tmpTask)

                    val str : MutableList<String> = mutableListOf()
                    val taskChangesTmp: String = "Title: $taskTitleValue \n"+
                            "Description: $taskDescriptionValue \n" +
                            "Deadline: $taskDeadlineValue \n" +
                            "Category: $taskCategoryValue \n" +
                            "Repeat: $taskRepeatValue \n" +
                            "Team: ${taskTeamValue.teamName} \n" +
                            "Users: " +
                            taskUsersValue.map { u-> u.userNickname }.toSet().joinToString()

                    str.add(0, taskChangesTmp)

                    /*add creation event*/
                    addTaskEventToHistory(
                        Event(
                            "Task created", SimpleDateFormat("dd/MM/yyyy").format(Date()), taskStatusValue,
                            loggedUser.userNickname, "0", taskSubtasksValue.size.toString(), str
                        )
                    )

                    selectedTask = tmpTask

                    val taskHistoryToAdd = hashMapOf(
                        "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                        "taskChanges" to str,
                        "taskDoneSubtasks" to "0",
                        "taskStatus" to taskStatusValue,
                        "taskTotalSubtasks" to taskSubtasksValue.size.toString(),
                        "title" to "Task Created",
                        "user" to loggedUser.userNickname
                    )

                    db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                        selectedTask.taskHistory.add(taskHistoryRef.id)
                        db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                    }

                    for (u in taskUsersValue){
                        u.userTasks.add("Tasks/${this.taskIdValue}")
                    }

                    loggedUser.userTasks.add(selectedTask.taskId)
                    taskTeamValue.teamTasks.add(selectedTask.taskId)
                    teamViewModel.allTeams.find { it.teamId == selectedTask.taskTeam }!!.teamTasks.add(selectedTask.taskId)
                    setTaskInformation(selectedTask, db)
                    navigateBack!!()
                    navigateBack!!()
                    updateOldTaskInformation()
                    showTaskDetailsPane(selectedTask.taskId)
                }
            } else {
                //Saves edit to a Task.

                selectedTask.taskTitle = taskTitleValue
                selectedTask.taskDescription = taskDescriptionValue
                selectedTask.taskDeadline = taskDeadlineValue
                selectedTask.taskTag = taskTagValue
                selectedTask.taskCategory = taskCategoryValue
                selectedTask.taskUsers = taskUsersValue.map{it.userNickname}.toMutableList()
                selectedTask.taskRepeat = taskRepeatValue
                //selectedTask.taskSubtasks = taskSubtasksValue.map{it.subtaskId}.toMutableStateList()
                selectedTask.taskTeam = taskTeamValue.teamId
                //selectedTask.taskHistory = taskHistoryValue.map { it.eventId }.toMutableStateList()
                selectedTask.taskStatus = taskStatusValue

                if (taskUsersValue.size>0){         //Update task status according to the number of users
                    selectedTask.taskStatus="In Progress"
                }else{
                    selectedTask.taskStatus="Pending"
                }

                db.collection("Tasks").document(selectedTask.taskId).update("taskCategory", selectedTask.taskCategory)
                db.collection("Tasks").document(selectedTask.taskId).update("taskDeadline", selectedTask.taskDeadline)
                db.collection("Tasks").document(selectedTask.taskId).update("taskDescription", selectedTask.taskDescription)
                db.collection("Tasks").document(selectedTask.taskId).update("taskRepeat", selectedTask.taskRepeat)
                db.collection("Tasks").document(selectedTask.taskId).update("taskStatus", selectedTask.taskStatus)
                db.collection("Tasks").document(selectedTask.taskId).update("taskTag", selectedTask.taskTag)
                db.collection("Tasks").document(selectedTask.taskId).update("taskTitle", selectedTask.taskTitle)

                //For every element, I check what was changed, so that I can add it to history
                val taskChangesTmp: MutableList<String> = mutableListOf<String>()
                if (taskTitleValue != oldTaskTitleValue) {
                    taskChangesTmp.add(
                        0, "Changed Title from \"$oldTaskTitleValue to $taskTitleValue\""
                    )
                }
                if (taskDescriptionValue != oldTaskDescriptionValue) {
                    taskChangesTmp.add(
                        0,
                        "Changed Description from \"$oldTaskDescriptionValue\" to \"$taskDescriptionValue\""
                    )
                }
                if (taskTagValue != oldTaskTagValue) {
                    taskChangesTmp.add(
                        0, "Changed Tag from \"$oldTaskTagValue\" to \"$taskTagValue\""
                    )
                }
                if (taskCategoryValue != oldTaskCategoryValue) {
                    taskChangesTmp.add(
                        0,
                        "Changed Category from \"$oldTaskCategoryValue\" to \"$taskCategoryValue\""
                    )
                }
                if (taskUsersValue != oldTaskUsersValue) {
                    var stringUserChanges=""
                    if ((taskUsersValue.map(User::userNickname) subtract oldTaskUsersValue.map(
                            User::userNickname
                        )).isNotEmpty()
                    ){
                        stringUserChanges += "Added users: \n"
                        stringUserChanges += (taskUsersValue.map(User::userNickname) subtract oldTaskUsersValue.map(
                            User::userNickname
                        ).toSet()).joinToString()
                        stringUserChanges+="\n"
                    }

                    if((oldTaskUsersValue.map(User::userNickname) subtract taskUsersValue.map(
                            User::userNickname
                        )).isNotEmpty()){
                        stringUserChanges += "Removed users: \n"
                        stringUserChanges += (oldTaskUsersValue.map(User::userNickname) subtract taskUsersValue.map(
                            User::userNickname
                        ).toSet()).joinToString()
                    }

                    taskChangesTmp.add(
                        0,
                        stringUserChanges
                    )           //Subtract one list from the other to obtain only the users added (taskUsersValue - oldTaskUsersValue) and only the users removedc (oldTaskUsersValue - taskUsersValue)
                }
                if (taskRepeatValue != oldTaskRepeatValue) {
                    taskChangesTmp.add(
                        0,
                        "Changed Repetition from \"${addSpacesToSentence(oldTaskRepeatValue.toString())}\" to \"${
                            addSpacesToSentence(taskRepeatValue.toString())
                        }\""
                    )
                }
                //No check on Subtasks and Team

                addTaskEventToHistory(
                    if(taskUsersValue.size>0) {
                        setTaskStatus("In Progress")

                        val taskHistoryToAdd = hashMapOf(
                            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                            "taskChanges" to taskChangesTmp,
                            "taskDoneSubtasks" to taskSubtasksValue.filter { s -> s.subtaskState == "Completed "}.size.toString(),
                            "taskStatus" to "In Progress",
                            "taskTotalSubtasks" to taskSubtasksValue.size.toString(),
                            "title" to "Task Edited",
                            "user" to loggedUser.userNickname
                        )

                        db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                            selectedTask.taskHistory.add(taskHistoryRef.id)
                            db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                        }

                        Event(
                            "Task edited",
                            SimpleDateFormat("dd/MM/yyyy").format(Date()),
                            "In Progress",
                            loggedUser.userNickname,
                            //selectedTask.taskSubtasks.filter { s -> s.subtaskState == State.Completed }.size.toString(),
                            taskSubtasksValue.filter { s -> s.subtaskState == "Completed" }.size.toString(),
                            taskSubtasksValue.size.toString(),
                            taskChangesTmp
                        )
                    }else{
                        setTaskStatus("Pending")

                        val taskHistoryToAdd = hashMapOf(
                            "date" to SimpleDateFormat("dd/MM/yyyy").format(Date()),
                            "taskChanges" to taskChangesTmp,
                            "taskDoneSubtasks" to taskSubtasksValue.filter { s -> s.subtaskState == "Completed "}.size.toString(),
                            "taskStatus" to "Pending",
                            "taskTotalSubtasks" to taskSubtasksValue.size.toString(),
                            "title" to "Task Edited",
                            "user" to loggedUser.userNickname
                        )

                        db.collection("TaskHistory").add(taskHistoryToAdd).addOnSuccessListener {taskHistoryRef ->
                            selectedTask.taskHistory.add(taskHistoryRef.id)
                            db.collection("Tasks").document(selectedTask.taskId).update("taskHistory", selectedTask.taskHistory)
                        }

                        Event(
                            "Task Edited",
                            SimpleDateFormat("dd/MM/yyyy").format(Date()),
                            "Pending",
                            loggedUser.userNickname,
                            //selectedTask.taskSubtasks.filter { s -> s.subtaskState == State.Completed }.size.toString(),
                            taskSubtasksValue.filter { s -> s.subtaskState == "Completed "}.size.toString(),
                            taskSubtasksValue.size.toString(),
                            taskChangesTmp
                        )
                    }
                )

                for (u in taskUsersValue) {
                    if(!oldTaskUsersValue.contains(u)) {
                        u.userTasks.add("Tasks/${this.taskIdValue}")

                        db.collection("Users").document(u.userNickname).get().addOnSuccessListener { d ->
                            val userTasksList = d.get("userTasks") as MutableList<String>
                            if(!userTasksList.contains(selectedTask.taskId)) {
                                userTasksList.add(selectedTask.taskId)
                                db.collection("Users").document(u.userNickname)
                                    .update("userTasks", userTasksList)
                            }
                        }
                    }
                }

                for (u in oldTaskUsersValue) {
                    if(!taskUsersValue.contains(u)) {
                        u.userTasks.remove("Tasks/${this.taskIdValue}")

                        db.collection("Users").document(u.userNickname).get().addOnSuccessListener { d ->
                            val userTasksList = d.get("userTasks") as MutableList<String>
                            if(userTasksList.contains(selectedTask.taskId)) {
                                userTasksList.remove(selectedTask.taskId)
                                db.collection("Users").document(u.userNickname)
                                    .update("userTasks", userTasksList)
                            }
                        }
                    }
                }

                updateOldTaskInformation()
                navigateBack!!()
            }
        }
    }

    fun updateOldTaskInformation() {
        oldTaskTitleValue = taskTitleValue
        oldTaskRepeatValue = taskRepeatValue
        oldTaskCategoryValue = taskCategoryValue
        oldTaskDeadlineValue = taskDeadlineValue
        oldTaskTagValue = taskTagValue
        oldTaskDescriptionValue = taskDescriptionValue
        oldTaskUsersValue = taskUsersValue.toMutableList()
        oldTaskSubtasksValue = taskSubtasksValue.toMutableStateList()
        oldTaskSubtaskValue = taskSubtaskValue
        oldTaskTeamValue = taskTeamValue
        oldTaskHistoryValue = taskHistoryValue.toMutableList()
        oldTaskStatusValue = taskStatusValue
    }

    //function set the old values, ignoring the changes
    fun noUpdateTaskInformation() {
        //set old values
        taskTitleValue = oldTaskTitleValue
        taskRepeatValue = oldTaskRepeatValue
        taskCategoryValue = oldTaskCategoryValue
        taskDeadlineValue = oldTaskDeadlineValue
        taskTagValue = oldTaskTagValue
        taskDescriptionValue = oldTaskDescriptionValue
        taskUsersValue = oldTaskUsersValue
        taskSubtasksValue = oldTaskSubtasksValue
        taskSubtaskValue = oldTaskSubtaskValue
        taskTeamValue = oldTaskTeamValue
        taskHistoryValue = oldTaskHistoryValue
        taskStatusValue = oldTaskStatusValue

        taskTitleError = ""
        taskDeadlineError = ""
        taskCategoryError = ""
        taskTeamError = ""
    }

    //function clear all values of task
    fun clearTaskInformation() {
        //reset infos
        taskTitleValue = ""
        taskRepeatValue = "No Repeat"
        taskCategoryValue = ""
        taskDeadlineValue = ""
        taskTagValue = ""
        taskDescriptionValue = ""
        taskUsersValue = mutableListOf<User>()
        taskSubtasksValue = mutableStateListOf<Subtask>()
        taskSubtaskValue = ""
        taskTeamValue = Team()
        taskQuestionsValue = mutableListOf<Question>()
        taskQuestionValue = ""
        taskHistoryValue = mutableListOf<Event>()
        taskTimerHistory = mutableListOf()


        taskTitleError = ""
        taskDeadlineError = ""
        taskCategoryError = ""
        taskTeamError = ""
    }

    fun setTaskInformation(selectedTask: Task, db: FirebaseFirestore) {
        setTaskTitle(selectedTask.taskTitle)
        setTaskDescription(selectedTask.taskDescription)
        setTaskDeadline(selectedTask.taskDeadline)
        setTaskTag(selectedTask.taskTag)
        setTaskCategory(selectedTask.taskCategory)
        assignTaskUsers(selectedTask.taskUsers.toMutableList(), db)
        setTaskRepeat(selectedTask.taskRepeat)
        assignTaskSubtasks(selectedTask.taskSubtasks.toMutableList(), db)
        setTaskTeam(selectedTask.taskTeam, db)
        assignTaskQuestions(selectedTask.taskQuestions.toMutableList(), db)
        assignTaskHistory(selectedTask.taskHistory, db)
        setTaskStatus(selectedTask.taskStatus)
        assignTaskTimerHistory(selectedTask.taskTimerHistory.toMutableList(), db)
        assignTaskMediaList(selectedTask.taskMediaList, db)
        assignTaskLinkList(selectedTask.taskLinkList)
        assignTaskDocumentList(selectedTask.taskDocumentList, db)
        setTaskTimerTitle("")
        setTaskTimer("0:00:00")

        oldTaskTitleValue = taskTitleValue
        oldTaskRepeatValue = taskRepeatValue
        oldTaskCategoryValue = taskCategoryValue
        oldTaskDeadlineValue = taskDeadlineValue
        oldTaskTagValue = taskTagValue
        oldTaskDescriptionValue = taskDescriptionValue
        oldTaskUsersValue = taskUsersValue.toMutableList()
        oldTaskSubtasksValue = taskSubtasksValue.toMutableStateList()
        oldTaskSubtaskValue = taskSubtaskValue
        oldTaskTeamValue = taskTeamValue
        oldTaskHistoryValue = taskHistoryValue.toMutableList()
        oldTaskStatusValue = taskStatusValue
    }
}