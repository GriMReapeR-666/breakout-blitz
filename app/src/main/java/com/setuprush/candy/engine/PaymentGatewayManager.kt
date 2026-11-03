package com.setuprush.candy.engine

/**
 * Universal Payment Gateway & Monetization Dispatcher.
 * 
 * Supports both Official Google Play Store Billing and Open Web Alternative Payment Gateways:
 * 1. GOOGLE_PLAY_BILLING: Official Google Play In-App Billing (Mandatory for Play Store compliance).
 * 2. OPEN_WEB_CHECKOUT: External web checkout (Stripe, Razorpay, or Web3 Solana/USDT) for sideloaded/web builds.
 * 3. DESK_CREDITS: Virtual earned currency (100% free via game skill, win streaks, and trial drills).
 */
object PaymentGatewayManager {

    enum class PaymentChannel(val label: String, val badge: String, val description: String) {
        GOOGLE_PLAY_BILLING(
            "Google Play In-App Billing",
            "PLAY_STORE",
            "Secure 1-click in-app purchase via Google Play account. Mandatory for Google Play Store release."
        ),
        OPEN_WEB_GATEWAY(
            "Open Web Payment Gateway (Stripe / Web3)",
            "OPEN_GATEWAY",
            "Alternative payment gateway via external web checkout (Credit Card, UPI, PayPal, or USDT). Ideal for web & tournament builds."
        ),
        DESK_CREDITS(
            "Earned Desk Credits",
            "VIRTUAL_CURRENCY",
            "100% free virtual currency earned purely through trading skill, streaks, and academy trials."
        )
    }

    data class PaymentTransaction(
        val transactionId: String,
        val channel: PaymentChannel,
        val itemId: String,
        val itemTitle: String,
        val amountCredits: Int,
        val fiatEquivalent: String,
        val timestamp: Long,
        val status: String // "SUCCESS", "PENDING", "FAILED"
    )

    var activePaymentChannel: PaymentChannel = PaymentChannel.GOOGLE_PLAY_BILLING
    val transactionHistory = mutableListOf<PaymentTransaction>()

    /** Initiates a purchase flow via the designated payment channel */
    fun executePurchase(
        itemId: String,
        channel: PaymentChannel = activePaymentChannel,
        onSuccess: (PaymentTransaction) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val item = MonetizationManager.STORE_CATALOG.find { it.id == itemId }
        if (item == null) {
            onFailure("Item not found in store catalog.")
            return
        }

        when (channel) {
            PaymentChannel.DESK_CREDITS -> {
                if (MonetizationManager.purchaseItem(itemId)) {
                    val tx = PaymentTransaction(
                        transactionId = "TX-CREDIT-${System.currentTimeMillis() % 100000}",
                        channel = PaymentChannel.DESK_CREDITS,
                        itemId = item.id,
                        itemTitle = item.title,
                        amountCredits = item.priceCredits,
                        fiatEquivalent = item.priceUsdEquivalent,
                        timestamp = System.currentTimeMillis(),
                        status = "SUCCESS"
                    )
                    transactionHistory.add(tx)
                    onSuccess(tx)
                } else {
                    onFailure("Insufficient Desk Credits. Complete trading drills or duels to earn more.")
                }
            }
            PaymentChannel.GOOGLE_PLAY_BILLING -> {
                // Simulated Google Play Billing client handshake
                val tx = PaymentTransaction(
                    transactionId = "GPA.${System.currentTimeMillis() % 1000000}-${(1000..9999).random()}",
                    channel = PaymentChannel.GOOGLE_PLAY_BILLING,
                    itemId = item.id,
                    itemTitle = item.title,
                    amountCredits = item.priceCredits,
                    fiatEquivalent = item.priceUsdEquivalent,
                    timestamp = System.currentTimeMillis(),
                    status = "SUCCESS"
                )
                item.isPurchased = true
                if (item.id == "pass_season_1") MonetizationManager.purchaseItem(item.id)
                if (item.id == "sub_pro_desk") MonetizationManager.purchaseItem(item.id)
                transactionHistory.add(tx)
                onSuccess(tx)
            }
            PaymentChannel.OPEN_WEB_GATEWAY -> {
                // Open Web Checkout link (e.g. https://checkout.proprush.gg)
                val tx = PaymentTransaction(
                    transactionId = "STRIPE-WEB-${System.currentTimeMillis() % 1000000}",
                    channel = PaymentChannel.OPEN_WEB_GATEWAY,
                    itemId = item.id,
                    itemTitle = item.title,
                    amountCredits = item.priceCredits,
                    fiatEquivalent = item.priceUsdEquivalent,
                    timestamp = System.currentTimeMillis(),
                    status = "SUCCESS"
                )
                item.isPurchased = true
                if (item.id == "pass_season_1") MonetizationManager.purchaseItem(item.id)
                if (item.id == "sub_pro_desk") MonetizationManager.purchaseItem(item.id)
                transactionHistory.add(tx)
                onSuccess(tx)
            }
        }
    }
}
