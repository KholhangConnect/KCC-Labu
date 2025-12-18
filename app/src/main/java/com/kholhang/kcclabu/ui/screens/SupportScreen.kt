package com.kholhang.kcclabu.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.constants.Status
import com.kholhang.kcclabu.ui.support.SupportViewModel
import com.kholhang.kcclabu.utils.Helper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: SupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val purchaseResult by viewModel.purchaseResultLiveData.observeAsState()
    val deepLink by viewModel.deepLinkLiveData.observeAsState()
    val inAppProducts by viewModel.inAppProductsLiveData.observeAsState(emptyList())
    val subscriptions by viewModel.subscriptionsLiveData.observeAsState(emptyList())
    val subscriptionsUrl = stringResource(R.string.subscriptions_url)
    val privacyPolicyUrl = stringResource(R.string.app_privacy_policy)
    val termsUrl = stringResource(R.string.app_terms)

    LaunchedEffect(Unit) {
        if (context is Activity) {
            viewModel.loadData(context)
        }
    }

    LaunchedEffect(purchaseResult) {
        purchaseResult?.let { (status, messageRes) ->
            messageRes?.let {
                // Show dialog - would need AlertDialog state
            }
        }
    }

    LaunchedEffect(deepLink) {
        deepLink?.let {
            // Launch web URL
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_support)) },
                actions = {
                    IconButton(onClick = {
                        // Launch subscriptions URL
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(subscriptionsUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_admin_panel_settings),
                            contentDescription = stringResource(R.string.manage_subscriptions)
                        )
                    }
                    IconButton(onClick = {
                        if (context is Activity) {
                            Helper.sendFeedback(context)
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_help),
                            contentDescription = stringResource(R.string.title_help)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.support_headline),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.support_message),
                style = MaterialTheme.typography.bodyMedium
            )

            if (inAppProducts.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(R.string.one_tine_donation),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        inAppProducts.forEach { product ->
                            val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                            FilterChip(
                                selected = false,
                                onClick = {
                                    if (context is Activity) {
                                        viewModel.initiatePurchase(product, context)
                                    }
                                },
                                label = { Text(price) }
                            )
                        }
                    }
                }
            }

            if (subscriptions.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(R.string.monthly_donations),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        subscriptions.forEach { product ->
                            // Get price from subscription offer details (usually the first/base plan)
                            val offerDetails = product.subscriptionOfferDetails?.firstOrNull()
                            val price = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""
                            FilterChip(
                                selected = false,
                                onClick = {
                                    if (context is Activity) {
                                        viewModel.initiatePurchase(product, context)
                                    }
                                },
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.subscription_period,
                                            price
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.in_app_purchases_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = {
                // Launch privacy policy
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(privacyPolicyUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error
                }
            }) {
                Text(stringResource(R.string.privacy_policy))
            }

            TextButton(onClick = {
                // Launch terms of use
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(termsUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error
                }
            }) {
                Text(stringResource(R.string.terms_of_use))
            }
        }
    }
}

