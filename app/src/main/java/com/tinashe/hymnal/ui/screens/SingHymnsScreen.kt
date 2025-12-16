package com.tinashe.hymnal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingHymnsScreen(
    hymnNumber: Int = 1,
    collectionId: Int = -1
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hymn") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(com.tinashe.hymnal.R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Hymn #$hymnNumber",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

