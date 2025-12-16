package com.tinashe.hymnal.ui.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinashe.hymnal.BuildConfig
import com.tinashe.hymnal.R
import com.tinashe.hymnal.utils.Helper

@Composable
fun InfoScreen() {
    val context = LocalContext.current
    val appLink = stringResource(R.string.app_link)
    val appName = stringResource(R.string.app_name)
    val supportOrFeedback = stringResource(R.string.support_or_feedback)
    val shareThisApp = stringResource(R.string.share_this_app)
    val appSource = stringResource(R.string.app_source)
    val appTwitter = stringResource(R.string.app_twitter)
    val shareMessage = stringResource(R.string.app_share_message, appLink)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(
            onClick = {
                (context as? android.app.Activity)?.let {
                    Helper.sendFeedback(it)
                }
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_support),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(supportOrFeedback)
        }

        TextButton(
            onClick = {
                launchWebUrl(context, appLink)
            }
        ) {
            Text(appName)
        }

        TextButton(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                }
                context.startActivity(
                    android.content.Intent.createChooser(
                        intent,
                        null
                    )
                )
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(shareThisApp)
        }

        TextButton(
            onClick = {
                launchWebUrl(context, appSource)
            }
        ) {
            Text(appSource)
        }

        TextButton(
            onClick = {
                launchWebUrl(context, appTwitter)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.twitter),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(appTwitter)
        }
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
