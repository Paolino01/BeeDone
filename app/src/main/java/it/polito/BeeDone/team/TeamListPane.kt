package it.polito.BeeDone.team

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.BeeDone.R
import it.polito.BeeDone.activeAnimation
import it.polito.BeeDone.profile.loggedUser
import it.polito.BeeDone.utils.TeamBox
import it.polito.BeeDone.utils.lightBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import me.saket.cascade.CascadeDropdownMenu


@SuppressLint("UnrememberedMutableState")
@Composable
fun TeamListPane(
    showingTeams: SnapshotStateList<Team>,
    allTeams: SnapshotStateList<Team>,
    clearTeamInformation: () -> Unit,
    createTeamPane: () -> Unit,
    showTeamDetailsPane: (String) -> Unit,
    acceptInvitationPane: (String) -> Unit,
    db: FirebaseFirestore
) {
    var state by remember { mutableIntStateOf(0) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showButton by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("↑") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        showButton = scrollState.value != 0
        buttonText = if (scrollState.value < scrollState.maxValue) "↓" else "↑"
    }

    // Function to load teams asynchronously
    suspend fun loadTeams(condition: (TeamMember) -> Boolean): List<Team> {
        val tempTeams = mutableSetOf<Team>()  // Use a set to avoid duplicates
        for (team in allTeams) {
            for (teamMemberRef in team.teamMembers) {
                try {
                    val teamMemberDoc = db.collection("TeamMembers")
                        .document(teamMemberRef)
                        .get()
                        .await()
                    if (teamMemberDoc.exists()) {
                        val teamMember = teamMemberDoc.toObject(TeamMember::class.java)!!
                        if (condition(teamMember)) {
                            tempTeams.add(team)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("Firebase", "Error fetching team member document", e)
                }
            }
        }
        return tempTeams.toList()
    }

    // Load initial data based on the state
    LaunchedEffect(state) {
        coroutineScope.launch {
            isLoading = true
            val teams = if (state == 0) {
                loadTeams { teamMember ->
                    teamMember.user == loggedUser.userNickname && teamMember.role != "Invited"
                }
            } else {
                loadTeams { teamMember ->
                    teamMember.role == "Invited" && teamMember.user == loggedUser.userNickname
                }
            }

            showingTeams.removeAll { true }
            showingTeams.addAll(teams)
            isLoading = false
        }
    }

    Column {
        TabRow(selectedTabIndex = state) {
            Tab(
                text = { Text(text = "My teams") },
                selected = state == 0,
                onClick = { state = 0 }
            )
            Tab(
                text = { Text(text = "Pending invitation") },
                selected = state == 1,
                onClick = { state = 1 }
            )
        }

        Box(
            Modifier
                .padding(horizontal = 15.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val durationMillis = 1400
                val enterAnimation = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing))
                val exitAnimation = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing))

                if (activeAnimation) {
                    Crossfade(targetState = state, label = "Animation Teams") { currentState ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            AnimatedVisibility(
                                visible = currentState == state,
                                enter = enterAnimation,
                                exit = exitAnimation
                            ) {
                                LazyVerticalGrid(
                                    modifier = Modifier.heightIn(0.dp, screenHeight.dp),
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    items(showingTeams) { team ->
                                        var userInTeam by remember {
                                            mutableStateOf(UserInTeam())
                                        }
                                        var userInTeamLoaded by remember {
                                            mutableStateOf(false)
                                        }
                                        LaunchedEffect(team) {
                                            //val userInTeamId = team.getIdTeamUser(loggedUser)

                                            for (uInTeam in loggedUser.userTeams){
                                                val userInTeamDoc = db.collection("UserInTeam").document(uInTeam).get().await()
                                                val tmpUserInTeam = userInTeamDoc.toObject(UserInTeam::class.java)
                                                val id = userInTeamDoc.id
                                                if (tmpUserInTeam != null && tmpUserInTeam.first == team.teamId) {
                                                    db.collection("UserInTeam").document(id).get().addOnSuccessListener {d ->
                                                        userInTeam = d.toObject(UserInTeam::class.java)!!
                                                        userInTeamLoaded = true
                                                    }
                                                    break
                                                }
                                            }
                                        }
                                        if(userInTeamLoaded) {
                                            TeamBox(showTeamDetailsPane, acceptInvitationPane, team, userInTeam.second)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier.heightIn(0.dp, screenHeight.dp),
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.Top
                        ) {
                            items(showingTeams) { team ->
                                var userInTeam by remember {
                                    mutableStateOf(UserInTeam())
                                }
                                var userInTeamLoaded by remember {
                                    mutableStateOf(false)
                                }
                                LaunchedEffect(team) {
                                    //val userInTeamId = team.getIdTeamUser(loggedUser)

                                    for (uInTeam in loggedUser.userTeams){
                                        val userInTeamDoc = db.collection("UserInTeam").document(uInTeam).get().await()
                                        val tmpUserInTeam = userInTeamDoc.toObject(UserInTeam::class.java)
                                        val id = userInTeamDoc.id
                                        if (tmpUserInTeam != null && tmpUserInTeam.first == team.teamId) {
                                            db.collection("UserInTeam").document(id).get().addOnSuccessListener {d ->
                                                userInTeam = d.toObject(UserInTeam::class.java)!!
                                                userInTeamLoaded = true
                                            }
                                            break
                                        }
                                    }
                                }
                                if(userInTeamLoaded) {
                                    TeamBox(showTeamDetailsPane, acceptInvitationPane, team, userInTeam.second)
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    clearTeamInformation()
                    createTeamPane()
                },
                shape = CircleShape,
                containerColor = Color.White,
                modifier = Modifier
                    .padding(10.dp)
                    .size(70.dp)
                    .align(Alignment.BottomEnd)
                    .offset(5.dp, 0.dp)
                    .border(2.dp, lightBlue, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create New Team",
                    Modifier.size(30.dp)
                )
            }

            if (showButton) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (scrollState.value < scrollState.maxValue) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            } else {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(10.dp, 0.dp)
                ) {
                    Text(buttonText, fontSize = 25.sp)
                }
            }
        }
    }
}





@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun TeamListMenu(
    showingTeams: MutableList<Team>,
    allTeams: MutableList<Team>,
    db: FirebaseFirestore
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    var filterText by mutableStateOf("")

    Row(horizontalArrangement = Arrangement.End) {

        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {
            showSortMenu = !showSortMenu
        }) {
            Icon(
                painter = painterResource(R.drawable.sorting),
                contentDescription = "Sorting",
                modifier = Modifier.size(25.dp)
            )
        }

        IconButton(onClick = {
            showFilterMenu = !showFilterMenu
        }) {
            Icon(
                painter = painterResource(R.drawable.filter),
                contentDescription = "Filter",
                modifier = Modifier.size(25.dp)
            )
        }

        //SORT MENU
        CascadeDropdownMenu(modifier = Modifier
            .border(1.dp, lightBlue, RoundedCornerShape(20.dp))
            .background(Color.White),
            expanded = showSortMenu,
            shape = RoundedCornerShape(20.dp),
            onDismissRequest = { showSortMenu = false }) {

            //sort by Deadline
            DropdownMenuItem(modifier = Modifier.background(Color.White), childrenHeader = {
                DropdownMenuHeader(Modifier.background(Color.White)) {
                    Text(text = "Sort by Team Name")
                }
            }, text = { Text(text = "Sort by Team Name") }, children = {

                //Sort By Ascending deadline
                androidx.compose.material3.DropdownMenuItem(modifier = Modifier
                    .background(Color.White)
                    .border(Dp.Hairline, lightBlue),
                    text = { Text(text = "Ascending") },
                    onClick = {
                        showingTeams.sortBy { it.teamName }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ascending),
                            contentDescription = "Ascending",
                            modifier = Modifier.size(22.dp)
                        )
                    })
                //Sort By Descending deadline
                androidx.compose.material3.DropdownMenuItem(
                    modifier = Modifier.background(Color.White),
                    text = { Text(text = "Descending") },
                    onClick = { showingTeams.sortByDescending { it.teamName } },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.descending),
                            contentDescription = "Descending",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                )
            })

            //sort by Creation date
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        Text(text = "Sort by Creation Date")
                    }
                },
                text = { Text(text = "Sort by Creation Date") },
                children = {
                    //Sort By Ascending Creation date
                    androidx.compose.material3.DropdownMenuItem(modifier = Modifier
                        .background(Color.White)
                        .border(Dp.Hairline, lightBlue),
                        text = { Text(text = "Ascending") },
                        onClick = { showingTeams.sortBy { it.teamCreationDate } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ascending),
                                contentDescription = "Ascending",
                                modifier = Modifier.size(22.dp)
                            )
                        })
                    androidx.compose.material3.DropdownMenuItem(modifier = Modifier.background(
                        Color.White
                    ),
                        text = { Text(text = "Descending") },
                        onClick = { showingTeams.sortByDescending { it.teamCreationDate } },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.descending),
                                contentDescription = "Descending",
                                modifier = Modifier.size(20.dp)
                            )
                        })
                }
            )
        }

        //FILTER MENU
        CascadeDropdownMenu(
            modifier = Modifier
                .border(1.dp, lightBlue, RoundedCornerShape(20.dp))
                .background(Color.White),
            expanded = showFilterMenu,
            shape = RoundedCornerShape(20.dp),
            onDismissRequest = {
                showFilterMenu = false
                filterText = ""
            },
        ) {

            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        filterText = ""
                        Text(text = "Filter by Team Name")
                    }
                },
                text = { Text(text = "Filter by Team Name") },
                children = {
                    androidx.compose.material3.DropdownMenuItem(
                        modifier = Modifier.background(Color.White),
                        text = {
                            Column {
                                OutlinedTextField(
                                    placeholder = { Text(text = "Insert Team Name") },
                                    value = filterText,
                                    onValueChange = { newText -> filterText = newText },
                                    maxLines = 1,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = lightBlue,
                                        focusedLabelColor = lightBlue,
                                        focusedTextColor = Color.DarkGray
                                    ),
                                )

                                FloatingActionButton(
                                    onClick = { showingTeams.removeAll { it.teamName != filterText } },
                                    containerColor = Color.White,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp, 10.dp)
                                        .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                        .height(30.dp)
                                ) {
                                    Text(text = "Apply Filter")
                                }
                            }
                        },
                        onClick = { showingTeams.removeAll { it.teamName != filterText } },
                        interactionSource = MutableInteractionSource()
                    )
                })

            //Filter by Category
            DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                childrenHeader = {
                    DropdownMenuHeader(Modifier.background(Color.White)) {
                        filterText = ""
                        Text(text = "Filter by Category")
                    }
                },
                text = { Text(text = "Filter by Category") },
                children = {
                    androidx.compose.material3.DropdownMenuItem(
                        modifier = Modifier.background(Color.White),
                        text = {
                            Column {
                                OutlinedTextField(
                                    placeholder = { Text(text = "Insert Category") },
                                    value = filterText,
                                    onValueChange = { newText -> filterText = newText },
                                    maxLines = 1,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = lightBlue,
                                        focusedLabelColor = lightBlue,
                                        focusedTextColor = Color.DarkGray
                                    ),
                                )

                                FloatingActionButton(
                                    onClick = { showingTeams.removeAll { it.teamCategory != filterText } },
                                    containerColor = Color.White,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp, 10.dp)
                                        .border(2.dp, lightBlue, RoundedCornerShape(20.dp))
                                        .height(30.dp)
                                ) {
                                    Text(text = "Apply Filter")
                                }
                            }
                        },
                        onClick = { showingTeams.removeAll { it.teamCategory != filterText } },
                        interactionSource = MutableInteractionSource()
                    )
                })

            //Remove all filters
            androidx.compose.material3.DropdownMenuItem(modifier = Modifier
                .background(Color.White)
                .border(Dp.Hairline, lightBlue),
                text = { Text("Remove Filters") },
                onClick = {
                    showingTeams.removeAll { true }
                    showingTeams.addAll(allTeams)
                }
            )
        }
    }
}
