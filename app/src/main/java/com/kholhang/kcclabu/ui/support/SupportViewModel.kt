package com.kholhang.kcclabu.ui.support

import android.app.Activity
import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.PurchasesResponseListener
import com.kholhang.kcclabu.BuildConfig
import com.kholhang.kcclabu.R
import com.kholhang.kcclabu.data.model.constants.Status
import com.kholhang.kcclabu.extensions.arch.SingleLiveEvent
import com.kholhang.kcclabu.extensions.arch.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class SupportViewModel @Inject constructor() :
    ViewModel(),
    PurchasesUpdatedListener,
    BillingClientStateListener {
    
    // Helper function to get product ID from ProductDetails
    private fun ProductDetails.getProductId(): String = productId

    private val mutablePurchaseResult = SingleLiveEvent<Pair<Status, @StringRes Int?>>()
    val purchaseResultLiveData: LiveData<Pair<Status, Int?>> = mutablePurchaseResult.asLiveData()

    private val mutableDeepLinkUrl = SingleLiveEvent<String>()
    val deepLinkLiveData: LiveData<String> = mutableDeepLinkUrl.asLiveData()

    private val mutableInAppProducts = MutableLiveData<List<ProductDetails>>()
    val inAppProductsLiveData: LiveData<List<ProductDetails>> = mutableInAppProducts.asLiveData()

    private val mutableSubscriptions = MutableLiveData<List<ProductDetails>>()
    val subscriptionsLiveData: LiveData<List<ProductDetails>> = mutableSubscriptions.asLiveData()

    private var billingClient: BillingClient? = null
    private var purchaseHistory = emptyList<Purchase>()

    fun loadData(activity: Activity) {
        if (billingClient?.isReady == true) {
            return
        }

        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .build()

        billingClient?.startConnection(this)
    }

    fun viewResumed() {
        queryPurchases()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                handlePurchase(purchase)
                mutablePurchaseResult.postValue(Status.SUCCESS to R.string.success_purchase)
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            mutablePurchaseResult.postValue(Status.ERROR to R.string.error_item_already_owned)
        } else {
            mutablePurchaseResult.postValue(Status.ERROR to null)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                viewModelScope.launch(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(params)
                }
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.e("Billing disconnected")
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        Timber.i("Billing SetupFinished: ${result.responseCode}")

        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            queryOneTimeDonations()
            queryRecurringDonations()
            queryPurchases()
        } else {
            Timber.e(result.debugMessage)
            mutablePurchaseResult.postValue(Status.ERROR to R.string.error_billing_client_unavailable)
        }
    }

    private fun queryOneTimeDonations() {
        val productList = inAppDonations.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            billingClient?.queryProductDetails(params)?.let { result ->
                val products = if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    (result.productDetailsList ?: emptyList()).sortedBy { product ->
                        // Get price from one-time purchase offer details
                        product.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
                    }
            } else {
                    Timber.e(result.billingResult.debugMessage)
                emptyList()
            }
            mutableInAppProducts.postValue(products)
            }
        }
    }

    private fun queryRecurringDonations() {
        val productList = subscriptions.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            billingClient?.queryProductDetails(params)?.let { result ->
                val subs = if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    result.productDetailsList ?: emptyList()
            } else {
                    Timber.e(result.billingResult.debugMessage)
                emptyList()
            }
            mutableSubscriptions.postValue(subs)
            }
        }
    }

    private fun queryPurchases() {
        viewModelScope.launch(Dispatchers.IO) {
            val allPurchases = mutableListOf<Purchase>()
            
            val listener = PurchasesResponseListener { result, purchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Get purchases list - property name may vary by version
                    val purchaseList = try {
                        purchases.javaClass.getMethod("getPurchasesList").invoke(purchases) as? List<Purchase>
                            ?: purchases.javaClass.getField("purchasesList").get(purchases) as? List<Purchase>
                            ?: emptyList()
                    } catch (e: Exception) {
                        Timber.e(e, "Error getting purchases list")
                        emptyList<Purchase>()
                    }
                    Timber.i("Purchases: $purchaseList")
                    allPurchases.addAll(purchaseList)
                    purchaseHistory = allPurchases
                }
            }
            
            // Query in-app purchases
            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            billingClient?.queryPurchasesAsync(inAppParams, listener)

            // Query subscriptions
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            billingClient?.queryPurchasesAsync(subsParams, listener)
        }
    }

    fun initiatePurchase(product: ProductDetails, activity: Activity) {
        val productId = product.getProductId()
        if (purchaseHistory.find { it.products.contains(productId) } != null) {
            // Check if it's a subscription
            if (product.productType == BillingClient.ProductType.SUBS) {
                val url = "$SUBS_BASE_URL?$productId&package=${BuildConfig.APPLICATION_ID}"
                mutableDeepLinkUrl.postValue(url)
            } else {
                mutablePurchaseResult.postValue(Status.ERROR to R.string.error_item_already_owned)
            }
            return
        }
        
        // Build billing flow params based on product type
        val productDetailsParamsList = when (product.productType) {
            BillingClient.ProductType.INAPP -> {
                // For one-time purchases, use oneTimePurchaseOfferDetails
                product.oneTimePurchaseOfferDetails?.let { offerDetails ->
                    // Get offer token - it's a property in the API
                    val offerToken = try {
                        offerDetails.javaClass.getMethod("getOfferToken").invoke(offerDetails) as? String
                            ?: offerDetails.javaClass.getField("offerToken").get(offerDetails) as? String
                            ?: ""
                    } catch (e: Exception) {
                        Timber.e(e, "Error getting offer token")
                        ""
                    }
                    if (offerToken.isNotEmpty()) {
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    } else {
                        emptyList()
                    }
                } ?: emptyList()
            }
            BillingClient.ProductType.SUBS -> {
                // For subscriptions, use subscriptionOfferDetails
                product.subscriptionOfferDetails?.mapNotNull { offerDetails ->
                    // Get offer token - it's a property in the API
                    val offerToken = try {
                        offerDetails.javaClass.getMethod("getOfferToken").invoke(offerDetails) as? String
                            ?: offerDetails.javaClass.getField("offerToken").get(offerDetails) as? String
                            ?: ""
                    } catch (e: Exception) {
                        Timber.e(e, "Error getting offer token")
                        ""
                    }
                    if (offerToken.isNotEmpty()) {
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(offerToken)
                            .build()
                    } else {
                        null
                    }
                } ?: emptyList()
            }
            else -> emptyList()
        }
        
        if (productDetailsParamsList.isEmpty()) {
            Timber.e("No offer details available for product: $productId")
            mutablePurchaseResult.postValue(Status.ERROR to null)
            return
        }
        
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val result = billingClient?.launchBillingFlow(activity, flowParams)

        Timber.i("Billing Response: ${result?.responseCode}")
        Timber.i("Billing Response message: ${result?.debugMessage}")
    }

    companion object {
        private const val SUBS_BASE_URL = "https://play.google.com/store/account/subscriptions"
        private val inAppDonations = listOf(
            "donate_1",
            "donate_5",
            "donate_10",
            "donate_25",
            "donate_50",
            "donate_100"
        )
        private val subscriptions = listOf(
            "subscription_1",
            "subscription_3"
        )
    }
}
