package com.nytimes.android.external.registerlib

/**
 * Includes constants, classes, helper methods for StoreFrontGoogle.
 */

object GoogleUtil {

    //==============================================================================================
    // Response Codes
    //==============================================================================================

    /**
     * Success
     */
    const val RESULT_OK = 0

    /**
     * User pressed back or canceled dialog
     */
    const val RESULT_USER_CANCELED = 1

    /**
     * This billing API version is not supported for the type requested
     */
    const val RESULT_BILLING_UNAVAILABLE = 3

    /**
     * Requested SKU is not available for purchase
     */
    const val RESULT_ITEM_UNAVAILABLE = 4

    /**
     * Invalid arguments provided to the API
     */
    const val RESULT_DEVELOPER_ERROR = 5

    /**
     * Fatal error during the API action
     */
    const val RESULT_ERROR = 6

    /**
     * Failure to purchase since item is  already owned
     */
    const val RESULT_ITEM_ALREADY_OWNED = 7

    /**
     * Failure to consume since item is not owned
     */
    const val RESULT_ITEM_NOT_OWNED = 8

    //==============================================================================================
    // Various string constants
    //==============================================================================================

    const val RESPONSE_CODE = "RESPONSE_CODE"

    const val INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"

    const val INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE"

    const val BUY_INTENT = "BUY_INTENT"

    const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"

    const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"

    const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"

    const val INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN"

    const val ITEM_ID_LIST = "ITEM_ID_LIST"

    const val DETAILS_LIST = "DETAILS_LIST"

    //==============================================================================================
    // InAppBillingService-related constants
    //==============================================================================================

    const val BILLING_API_VERSION = 5

    const val BILLING_TYPE_SUBSCRIPTION = "subs"

    const val BILLING_TYPE_IAP = "inapp"


}
