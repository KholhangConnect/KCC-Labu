package com.kholhang.kcclabu.ui.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kholhang.kcclabu.BuildConfig
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.utils.Helper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen() {
    val context = LocalContext.current
    val appLink = stringResource(R.string.app_link)
    val appName = stringResource(R.string.app_name)
    val appDeveloper = stringResource(R.string.app_developer)
    val shareMessage = stringResource(R.string.app_share_message, appLink)
    val appSource = stringResource(R.string.app_source)
    val appYouTube = stringResource(R.string.app_youtube)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_info)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 80.dp), // Extra bottom padding to ensure last card is fully visible
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // App Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = appDeveloper,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // About the App Section
        InfoSection(title = stringResource(R.string.about_app)) {
            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // App Features Section
        InfoSection(title = stringResource(R.string.app_features)) {
            FeatureItem(
                icon = painterResource(R.drawable.ic_library_books),
                title = stringResource(R.string.feature_multiple_hymnals),
                description = stringResource(R.string.feature_multiple_hymnals_desc)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureItem(
                icon = painterResource(R.drawable.ic_add),
                title = stringResource(R.string.feature_custom_collections),
                description = stringResource(R.string.feature_custom_collections_desc)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureItem(
                icon = painterResource(R.drawable.ic_search),
                title = stringResource(R.string.feature_search),
                description = stringResource(R.string.feature_search_desc)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureItem(
                icon = painterResource(R.drawable.ic_text_format),
                title = stringResource(R.string.feature_customization),
                description = stringResource(R.string.feature_customization_desc)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureItem(
                icon = painterResource(R.drawable.ic_download),
                title = stringResource(R.string.feature_offline_access),
                description = stringResource(R.string.feature_offline_access_desc)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeatureItem(
                icon = painterResource(R.drawable.ic_text_format),
                title = stringResource(R.string.feature_pinch_zoom),
                description = stringResource(R.string.feature_pinch_zoom_desc)
            )
        }

        // Links Section
        InfoSection(title = stringResource(R.string.links)) {
            InfoLinkButton(
                icon = painterResource(R.drawable.ic_info),
                title = stringResource(R.string.open_in_play_store),
                onClick = { launchWebUrl(context, appLink) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoLinkButton(
                icon = painterResource(R.drawable.ic_share),
                title = stringResource(R.string.share_app),
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                    }
                    context.startActivity(
                        android.content.Intent.createChooser(intent, null)
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoLinkButton(
                icon = painterResource(R.drawable.ic_info),
                title = stringResource(R.string.view_on_github),
                onClick = { launchWebUrl(context, appSource) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoLinkButton(
                icon = painterResource(R.drawable.ic_play_circle),
                title = stringResource(R.string.watch_on_youtube),
                onClick = { launchWebUrl(context, appYouTube) }
            )
        }

        // Support Section
        InfoSection(title = stringResource(R.string.support_or_feedback)) {
            InfoLinkButton(
                icon = painterResource(R.drawable.ic_support),
                title = stringResource(R.string.support_or_feedback),
                onClick = {
                    (context as? android.app.Activity)?.let {
                        Helper.sendFeedback(it)
                    }
                }
            )
        }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoLinkButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun launchWebUrl(context: android.content.Context, url: String) {
    try {
        val builder = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .enableUrlBarHiding()
        val intent = builder.build()
        intent.launchUrl(context, Uri.parse(url))
    } catch (ex: Exception) {
        // Handle error
    }
}
