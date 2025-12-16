package com.tinashe.hymnal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tinashe.hymnal.R
import com.tinashe.hymnal.data.model.Hymnal
import com.tinashe.hymnal.ui.hymns.hymnals.HymnalListViewModel
import com.tinashe.hymnal.ui.hymns.hymnals.HymnalListFragment.Companion.SELECTED_HYMNAL_KEY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnalListScreen(
    navController: NavController,
    viewModel: HymnalListViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
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
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (hymnal.offline) {
                Icon(
                    painter = painterResource(R.drawable.ic_offline_pin),
                    contentDescription = "Offline",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
