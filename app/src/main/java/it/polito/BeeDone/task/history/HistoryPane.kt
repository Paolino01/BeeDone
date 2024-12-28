package it.polito.BeeDone.task.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.polito.BeeDone.utils.EventRow
import kotlinx.coroutines.launch

@Composable
fun TaskHistoryPane(
    history: List<Event>,
    showUserInformationPane: (String) -> Unit
) {

    //variables for the flyout button
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showButton by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("↑") }


    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.visibleItemsInfo) {
        showButton = listState.firstVisibleItemIndex > 0
        buttonText = if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size >= listState.layoutInfo.totalItemsCount) "↑" else "↓"
    }

    Box(Modifier.padding(horizontal = 15.dp, vertical = 5.dp)) {
        LazyColumn(
            Modifier
                .fillMaxHeight(), state = listState, verticalArrangement = Arrangement.Top
        ) {
            items(history) { event ->
                EventRow(event = event, showUserInformationPane)
                HorizontalDivider(Modifier.padding(15.dp, 5.dp), thickness = Dp.Hairline, color = Color.Gray)
            }
        }

        // Scroll to top or bottom button
        if (showButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        if (listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size < listState.layoutInfo.totalItemsCount) {
                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                        } else {
                            listState.animateScrollToItem(0)
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
