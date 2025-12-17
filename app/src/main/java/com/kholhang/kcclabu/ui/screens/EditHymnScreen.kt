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
fun EditHymnScreen(
    hymn: com.kholhang.kcclabu.data.model.Hymn?,
    onSave: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_edit_hymn)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                text = stringResource(
                    R.string.edit_hymn_with_title,
                    hymn?.title ?: stringResource(R.string.unknown)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

