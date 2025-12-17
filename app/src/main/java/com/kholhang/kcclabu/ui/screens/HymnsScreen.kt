package com.kholhang.kcclabu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.Hymnal
import com.kholhang.kcclabu.data.model.constants.Status
import com.kholhang.kcclabu.ui.hymns.HymnsViewModel

private const val SELECTED_HYMNAL_KEY = "selected_hymnal"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HymnsScreen(
    navController: NavController,
    viewModel: HymnsViewModel = hiltViewModel(),
    onHymnClick: (Int) -> Unit = {},
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
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
                                imageVector = Icons.Filled.Search,
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
                        TextButton(onClick = { viewModel.hymnalSelected(com.kholhang.kcclabu.data.model.Hymnal("", "", "")) }) {
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
                    val listState = rememberLazyListState()
                    
                    // Track scroll state and hide bottom bar when near bottom
                    LaunchedEffect(listState) {
                        snapshotFlow { 
                            val firstVisibleItemIndex = listState.firstVisibleItemIndex
                            val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
                            val totalItems = hymnsList.size
                            val isNearBottom = firstVisibleItemIndex >= totalItems - 2
                            val isScrolling = listState.isScrollInProgress
                            Pair(isNearBottom || (isScrolling && firstVisibleItemIndex >= totalItems - 5), isScrolling)
                        }
                        .distinctUntilChanged()
                        .collect { (shouldHide, isScrolling) ->
                            // Hide bottom bar when near bottom or scrolling down near bottom
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
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.content_desc_close)
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HymnListItem(
    hymn: com.kholhang.kcclabu.data.model.Hymn,
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

