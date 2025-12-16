package com.tinashe.hymnal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.tinashe.hymnal.data.model.constants.Status
import com.tinashe.hymnal.ui.hymns.HymnsViewModel
import com.tinashe.hymnal.ui.hymns.hymnals.HymnalListFragment.Companion.SELECTED_HYMNAL_KEY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnsScreen(
    navController: NavController,
    viewModel: HymnsViewModel = hiltViewModel(),
    onHymnClick: (Int) -> Unit = {}
) {
    val status by viewModel.status.collectAsState()
    val hymnsList by viewModel.hymnsList.collectAsState()
    val hymnalTitle by viewModel.hymnalTitle.collectAsState()
    val showHymnalsPrompt by viewModel.showHymnalsPrompt.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Handle hymnal selection from navigation
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<Hymnal?>(SELECTED_HYMNAL_KEY, null)
            ?.collect { hymnal ->
                hymnal?.let { viewModel.hymnalSelected(it) }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchActive) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { 
                                searchQuery = it
                                viewModel.performSearch(it)
                            },
                            onSearchActiveChange = { isSearchActive = it },
                            onClose = { 
                                isSearchActive = false
                                searchQuery = ""
                                viewModel.performSearch(null)
                            }
                        )
                    } else {
                        Text(hymnalTitle)
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.title_search)
                            )
                        }
                        IconButton(onClick = { 
                            navController.navigate("hymnal_list")
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_bookshelf),
                                contentDescription = stringResource(R.string.title_hymnals)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (status) {
            Status.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            Status.ERROR -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.error_un_available),
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { viewModel.hymnalSelected(com.tinashe.hymnal.data.model.Hymnal("", "", "")) }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
            else -> {
                if (hymnsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.error_un_available))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hymnsList) { hymn ->
                            HymnListItem(
                                hymn = hymn,
                                onClick = { onHymnClick(hymn.number) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.hint_search_hymns)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Close"
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HymnListItem(
    hymn: com.tinashe.hymnal.data.model.Hymn,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = hymn.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

