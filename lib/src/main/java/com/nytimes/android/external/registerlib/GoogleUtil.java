package com.nytimes.android.external.registerlib;

/**
 * Includes constants, classes, helper methods for StoreFrontGoogle.
 */

public final class GoogleUtil {

    /**
     * Response codes
     */
    public static final int RESULT_OK = 0;                  // - success
    public static final int RESULT_USER_CANCELED = 1;       // - user pressed back or canceled a
                                                            //   dialog
    public static final int RESULT_BILLING_UNAVAILABLE = 3; // - this billing API version is not
                                                            //   supported for the type requested
    public static final int RESULT_ITEM_UNAVAILABLE = 4;    // - requested SKU is not available for
                                                            //   purchase
    public static final int RESULT_DEVELOPER_ERROR = 5;     // - invalid arguments provided to the
                                                            //   API
    public static final int RESULT_ERROR = 6;               // - Fatal error during the API action
    public static final int RESULT_ITEM_ALREADY_OWNED = 7;  // - Failure to purchase since item is
                                                            //   already owned
    public static final int RESULT_ITEM_NOT_OWNED = 8;      // - Failure to consume since item is
                                                            //   not owned

    /**
     * Various string constants
     */
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String BUY_INTENT = "BUY_INTENT";
    public static final String INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";
    public static final String ITEM_ID_LIST = "ITEM_ID_LIST";
    public static final String DETAILS_LIST = "DETAILS_LIST";

    /**
     * InAppBillingService-related constants
     */
    public static final int BILLING_API_VERSION = 5;
    public static final String BILLING_TYPE_SUBSCRIPTION = "subs";
    public static final String BILLING_TYPE_IAP = "inapp";

    private GoogleUtil() {
    }


}
