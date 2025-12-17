package com.kholhang.kcclabu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.Hymnal
import com.kholhang.kcclabu.ui.hymns.hymnals.HymnalListViewModel

private const val SELECTED_HYMNAL_KEY = "selected_hymnal"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnalListScreen(
    navController: NavController,
    viewModel: HymnalListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    val hymnalList by viewModel.hymnalList.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_hymnals)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.title_info)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (hymnalList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val listState = rememberLazyListState()
            
            // Track scroll state and hide bottom bar when near bottom
            LaunchedEffect(listState) {
                snapshotFlow { 
                    val firstVisibleItemIndex = listState.firstVisibleItemIndex
                    val totalItems = hymnalList.size
                    val isNearBottom = firstVisibleItemIndex >= totalItems - 2
                    val isScrolling = listState.isScrollInProgress
                    isNearBottom || (isScrolling && firstVisibleItemIndex >= totalItems - 5)
                }
                .distinctUntilChanged()
                .collect { shouldHide ->
                    onBottomBarVisibilityChange(!shouldHide)
                }
            }
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp, 
                    bottom = 80.dp // Extra bottom padding to ensure last card is fully visible
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hymnalList) { hymnal ->
                    HymnalListItem(
                        hymnal = hymnal,
                        onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(SELECTED_HYMNAL_KEY, hymnal)
                            navController.popBackStack()
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(stringResource(R.string.title_info)) },
            text = { Text(stringResource(R.string.switch_between_help)) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}

@Composable
fun HymnalListItem(
    hymnal: Hymnal,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hymnal.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hymnal.language,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hymnal.selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.content_desc_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (hymnal.offline) {
                Icon(
                    painter = painterResource(R.drawable.ic_offline_pin),
                    contentDescription = stringResource(R.string.content_desc_offline),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
