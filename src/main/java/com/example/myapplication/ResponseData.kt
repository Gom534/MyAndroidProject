package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class PaymentReadyResponse(
    @SerializedName("tid")
    val tid: String,

    @SerializedName("next_redirect_app_url")
    val nextRedirectAppUrl: String,

    @SerializedName("next_redirect_mobile_url")
    val nextRedirectMobileUrl: String,

    @SerializedName("next_redirect_pc_url")
    val nextRedirectPcUrl: String,

    @SerializedName("android_app_scheme")
    val androidAppScheme: String,

    @SerializedName("ios_app_scheme")
    val iosAppScheme: String,

    @SerializedName("created_at")
    val createdAt: String
)

data class PaymentApproveResponse(
    @SerializedName("aid")
    val aid: String,

    @SerializedName("tid")
    val tid: String,

    @SerializedName("cid")
    val cid: String,

    @SerializedName("sid")
    val sid: String,

    @SerializedName("partner_order_id")
    val partnerOrderId: String,

    @SerializedName("partner_user_id")
    val partnerUserId: String,

    @SerializedName("payment_method_type")
    val paymentMethodType: String,

    @SerializedName("amount")
    val amount: Amount,

    @SerializedName("card_info")
    val cardInfo: CardInfo,

    @SerializedName("item_name")
    val itemName: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("approved_at")
    val approvedAt: String
)

data class Amount(
    @SerializedName("total")
    val total: Int,

    @SerializedName("tax_free")
    val taxFree: Int,

    @SerializedName("vat")
    val vat: Int,

    @SerializedName("point")
    val point: Int,

    @SerializedName("discount")
    val discount: Int
)

data class CardInfo(
    @SerializedName("purchase_corp")
    val purchaseCorp: String,

    @SerializedName("purchase_corp_code")
    val purchaseCorpCode: String,

    @SerializedName("issuer_corp")
    val issuerCorp: String,

    @SerializedName("issuer_corp_code")
    val issuerCorpCode: String,

    @SerializedName("kakaopay_purchase_corp")
    val kakaopayPurchaseCorp: String,

    @SerializedName("kakaopay_purchase_corp_code")
    val kakaopayPurchaseCorpCode: String,

    @SerializedName("kakaopay_issuer_corp")
    val kakaopayIssuerCorp: String,

    @SerializedName("kakaopay_issuer_corp_code")
    val kakaopayIssuerCorpCode: String,

    @SerializedName("bin")
    val bin: String,

    @SerializedName("card_type")
    val cardType: String,

    @SerializedName("install_month")
    val installMonth: String,

    @SerializedName("approved_id")
    val approvedId: String,

    @SerializedName("card_mid")
    val cardMid: String,

    @SerializedName("interest_free_install")
    val interestFreeInstall: String,

    @SerializedName("card_item_code")
    val cardItemCode: String
)
