package com.kholhang.kcclabu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kholhang.kcclabu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingHymnsScreen(
    hymnNumber: Int = 1,
    collectionId: Int = -1
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_hymn)) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(com.kholhang.kcclabu.R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.content_desc_back)
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
                text = stringResource(R.string.hymn_number, hymnNumber),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

