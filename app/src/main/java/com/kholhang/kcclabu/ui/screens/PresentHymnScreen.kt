package com.kholhang.kcclabu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kholhang.kcclabu.R

@Composable
fun PresentHymnScreen(
    hymn: com.kholhang.kcclabu.data.model.Hymn?,
    onExit: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = hymn?.title ?: stringResource(R.string.unknown),
            modifier = Modifier.padding(16.dp)
        )
        
        FloatingActionButton(
            onClick = onExit,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.kholhang.kcclabu.R.drawable.ic_fullscreen_exit_white),
                contentDescription = stringResource(R.string.content_desc_exit)
            )
        }
    }
}

