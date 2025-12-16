package com.tinashe.hymnal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PresentHymnScreen(
    hymn: com.tinashe.hymnal.data.model.Hymn?,
    onExit: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = hymn?.title ?: "Unknown Hymn",
            modifier = Modifier.padding(16.dp)
        )
        
        FloatingActionButton(
            onClick = onExit,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.tinashe.hymnal.R.drawable.ic_fullscreen_exit_white),
                contentDescription = "Exit"
            )
        }
    }
}

