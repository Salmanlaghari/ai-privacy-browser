package com.aibrowser.app.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Orchestrates Google Play Billing client integrations.
 * Validates, restores, and caches premium subscription status locally.
 */
class PremiumBillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val prefs = context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    companion object {
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_YEARLY = "premium_yearly"
        const val PRODUCT_LIFETIME = "premium_lifetime"
    }

    init {
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing: Setup completed successfully.")
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing: Service disconnected. Retrying...")
            }
        })
    }

    /**
     * Checks SharedPreferences cache to verify if the user has premium status.
     */
    fun isPremiumUser(): Boolean {
        return prefs.getBoolean("is_premium", false)
    }

    /**
     * Triggers the Google Play purchase flow for a given product ID.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String, isSubscription: Boolean) {
        val productType = if (isSubscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
            } else {
                Timber.e("Billing: Failed to fetch product details. Code: %d", billingResult.responseCode)
            }
        }
    }

    /**
     * Restores purchases by querying active subscriptions and in-app purchases.
     */
    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            var premiumRestored = false
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        premiumRestored = true
                        handlePurchaseVerification(purchase)
                    }
                }
            }

            // Also query one-time lifetime purchases
            val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in inAppPurchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            premiumRestored = true
                            handlePurchaseVerification(purchase)
                        }
                    }
                }
                prefs.edit().putBoolean("is_premium", premiumRestored).apply()
                onComplete(premiumRestored)
            }
        }
    }

    private fun queryPurchases() {
        restorePurchases { success ->
            Timber.d("Billing: Purchases query finished. Premium active: %b", success)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchaseVerification(purchase)
            }
            prefs.edit().putBoolean("is_premium", true).apply()
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.i("Billing: Purchase flow cancelled by user.")
        } else {
            Timber.e("Billing: Purchase flow error. Code: %d", billingResult.responseCode)
        }
    }

    private fun handlePurchaseVerification(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.d("Billing: Purchase acknowledged successfully.")
                    }
                }
            }
        }
    }
}
