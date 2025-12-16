package com.tinashe.hymnal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tinashe.hymnal.R
import com.tinashe.hymnal.ui.collections.CollectionsViewModel
import com.tinashe.hymnal.ui.collections.ViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: CollectionsViewModel = hiltViewModel(),
    onCollectionClick: (Int) -> Unit = {},
    onAddCollection: () -> Unit = {}
) {
    val viewState by viewModel.viewState.collectAsState()
    val collections by viewModel.collections.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
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
                        Text(stringResource(R.string.title_collections))
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
                        IconButton(onClick = onAddCollection) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.new_collection)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = {
                            viewModel.undoDelete()
                            showSnackbar = false
                        }) {
                            Text(stringResource(R.string.title_undo))
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(stringResource(R.string.collection_deleted))
                }
            }
        }
    ) { paddingValues ->
        when (viewState) {
            ViewState.LOADING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            ViewState.NO_RESULTS -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.error_un_available))
                }
            }
            ViewState.HAS_RESULTS -> {
                if (collections.isEmpty()) {
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
                            Icon(
                                painter = painterResource(R.drawable.ic_empty_collections),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(stringResource(R.string.empty_collections))
                            TextButton(onClick = onAddCollection) {
                                Text(stringResource(R.string.empty_collections_add))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = collections,
                            key = { it.collection.collectionId }
                        ) { collection ->
                            CollectionListItem(
                                collection = collection,
                                onClick = {
                                    if (collection.hymns.isEmpty()) {
                                        // Show error
                                    } else {
                                        onCollectionClick(collection.collection.collectionId)
                                    }
                                },
                                onSwipeToDelete = {
                                    viewModel.onIntentToDelete(collections.indexOf(collection))
                                    showSnackbar = true
                                }
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
fun CollectionListItem(
    collection: com.tinashe.hymnal.data.model.collections.CollectionHymns,
    onClick: () -> Unit,
    onSwipeToDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = collection.collection.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (collection.hymns.size) {
                    0 -> stringResource(R.string.hymn_count_zero)
                    1 -> stringResource(R.string.hymn_count_one)
                    else -> stringResource(R.string.hymn_count_other, collection.hymns.size)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
