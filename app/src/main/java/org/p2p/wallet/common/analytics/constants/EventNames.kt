package org.p2p.wallet.common.analytics.constants

object EventNames {

    const val BROWSE_TOKEN_LIST_VIEWED = "Token_List_Viewed"
    const val BROWSE_TOKEN_LIST_SCROLLED = "Token_List_Scrolled"
    const val BROWSE_TOKEN_LIST_SEARCHED = "Token_List_Searching"
    const val BROWSE_CURRENCY_LIST_SEARCHING = "Currency_List_Searching"
    const val BROWSE_TOKEN_CHOSEN = "Token_Chosen"
    const val BROWSE_SCREEN_OPENED = "Screen_Opened"
    const val BROWSE_NETWORK_ADDING = "Network_Adding"
    const val BROWSE_NETWORK_CHANGING = "Network_Changing"
    const val BROWSE_NETWORK_SAVED = "Network_Saved"
    const val BROWSE_BANNER_USERNAME_PRESSED = "Banner_Username_Pressed"
    const val BROWSE_BANNER_BACKUP_PRESSED = "Banner_Backup_Pressed"
    const val BROWSE_BANNER_NOTIFICATION_PRESSED = "Banner_Notifications_Pressed"
    const val BROWSE_BANNER_FEEDBACK_PRESSED = "Banner_Feedback_Pressed"

    const val AUTH_VIEWED = "Auth_Viewed"
    const val AUTH_VALIDATED = "Auth_Validated"
    const val AUTH_RESET_INVOKED = "Auth_Reset_Invoked"
    const val AUTH_RESET_VALIDATED = "Auth_Reset_Validated"

    const val ADMIN_APP_OPENED = "App_Opened"
    const val ADMIN_APP_CLOSED = "App_Closed"
    const val ADMIN_PUSH_RECEIVED = "Push_Received"
    const val ADMIN_SNACKBAR_RECEIVED = "Snackbar_Received"
    const val ADMIN_SIGN_OUT = "Sign_Out"
    const val ADMIN_SIGNED_OUT = "Signed_Out"
    const val ADMIN_PIN_CREATED = "Pin_Created"
    const val ADMIN_PIN_REJECTED = "Pin_Rejected"
    const val ADMIN_PASSWORD_CREATED = "Password_Created"
    const val ADMIN_PIN_RESET_INVOKED = "PIN_Reset_Invoked"
    const val ADMIN_PIN_RESET_VALIDATED = "PIN_Reset_Validated"

    const val ONBOARD_SPLASH_VIEWED = "Splash_Viewed"
    const val ONBOARD_SPLASH_SWIPED = "Splash_Swiped"
    const val ONBOARD_SPLASH_RESTORING = "Splash_Restoring"
    const val ONBOARD_SPLASH_CREATED = "Splash_Creating"
    const val ONBOARD_RESTORE_GOOGLE_INVOKED = "Restore_Google_Invoked"
    const val ONBOARD_RESTORE_MANUAL_INVOKED = "Restore_Manual_Invoked"
    const val ONBOARD_CREATE_MANUAL_INVOKED = "Create_Manual_Invoked"
    const val ONBOARD_CREATE_SEED_INVOKED = "Create_Seed_Invoked"
    const val ONBOARD_BACKING_UP_COPYING = "Backing_Up_Copying"
    const val ONBOARD_BACKING_UP_SAVING = "Backing_Up_Saving"
    const val ONBOARD_BACKING_UP_RENEW = "Backing_Up_Renewing"
    const val ONBOARD_BACKUP_MANUALLY = "Backing_Up_Manually"
    const val ONBOARD_BACKUP_ERROR = "Backing_Up_Error"
    const val ONBOARD_BIO_REJECTED = "Bio_Rejected"
    const val ONBOARD_WALLET_CREATED = "Wallet_Created"
    const val ONBOARD_WALLET_RESTORED = "Wallet_Restored"
    const val ONBOARD_MANY_WALLETS_FOUND = "Many_Wallets_Found"
    const val ONBOARD_NO_WALLET_FOUND = "No_Wallet_Found"
    const val ONBOARD_BIO_APPROVED = "Bio_Approved"
    const val ONBOARD_PUSH_REJECTED = "Push_Rejected"
    const val ONBOARD_PUSH_APPROVED = "Push_Approved"
    const val ONBOARD_USERNAME_SKIPPED = "Username_Skipped"
    const val ONBOARD_USERNAME_SAVED = "Username_Saved"
    const val ONBOARD_USERNAME_RESERVED = "Username_Reserved"

    const val BUY_VIEWED = "Buy_Viewed"
    const val BUY_LIST_VIEWED = "Buy_List_Viewed"
    const val BUY_TOKEN_CHOSEN = "Buy_Token_Chosen"
    const val BUY_GOING_BACK = "Buy_Going_Back"
    const val BUY_CONTINUING = "Buy_Continuing"
    const val BUY_CHANGING_PROVIDER = "Buy_Changing_Provider"
    const val BUY_PROVIDER_STEP_VIEWED = "Buy_Provider_Step_Viewed"
    const val BUY_PAYMENT_RESULT_SHOWN = "Buy_Payment_Result_Shown"

    const val HOME_USER_HAS_POSITIVE_BALANCE = "User_Has_Positive_Balance"
    const val HOME_USER_AGGREGATE_BALANCE = "User_Aggregate_Balance"

    const val SEND_VIEWED = "Send_Viewed"
    const val SEND_CHANGING_TOKEN = "Send_Changing_Token"
    const val SEND_CHANGING_CURRENCY = "Send_Changing_Currency"
    const val SEND_GOING_BACK = "Send_Going_Back"
    const val SEND_CHOOSING_RECEIPT = "Send_Choosing_Recipient"
    const val SEND_QR_SCANNING = "Send_QR_Scanning"
    const val SEND_QR_GOING_BACK = "Send_QR_Going_Back"
    const val SEND_PASTING = "Send_Pasting"
    const val SEND_RESOLVED_AUTO = "Send_Resolved_Auto"
    const val SEND_RESOLVED_MANUALLY = "Send_Resolved_Manually"
    const val SEND_REVIEWING = "Send_Reviewing"
    const val SEND_VERIFICATION_INVOKED = "Send_Verification_Invoked"
    const val SEND_PROCESS_SHOWN = "Send_Process_Shown"
    const val SEND_CREATING_ANOTHER = "Send_Creating_Another"
    const val SEND_SHOW_DETAIL_PRESSED = "Send_Show_Details_Pressed"
    const val SEND_SHOWING_DETAILS = "Send_Showing_Details"
    const val SEND_USER_CONFIRMED = "Send_User_Confirmed"
    const val SEND_STARTED = "Send_Started"
    const val SEND_COMPLETED = "Send_Completed"

    const val SWAP_VIEWED = "Swap_Viewed"
    const val SWAP_CHANGING_TOKEN_A = "Swap_Changing_Token_A"
    const val SWAP_CHANGING_TOKEN_B = "Swap_Changing_Token_B"
    const val SWAP_REVERSING = "Swap_Reversing"
    const val SWAP_SHOWING_SETTINGS = "Swap_Showing_Settings"
    const val SWAP_SETTING_SETTINGS = "Swap_Setting_Settings"
    const val SWAP_CHANGING_CURRENCY = "Swap_Changing_Currency"
    const val SWAP_SHOW_DETAILS_PRESSED = "Swap_Show_Details_Pressed"
    const val SWAP_GOING_BACK = "Swap_Going_Back"
    const val SWAP_REVIEWING = "Swap_Reviewing"
    const val SWAP_REVIEWING_HELP_CLOSED = "Swap_Reviewing_Help_Closed"
    const val SWAP_VERIFICATION_INVOKED = "Swap_Verification_Invoked"
    const val SWAP_PROCESS_SHOWN = "Swap_Process_Shown"
    const val SWAP_CREATING_ANOTHER = "Swap_Creating_Another"
    const val SWAP_SHOWING_HISTORY = "Swap_Showing_History"
    const val SWAP_SHOWING_DETAILS = "Swap_Showing_Details"
    const val SWAP_USER_CONFIRMED = "Swap_User_Confirmed"
    const val SWAP_STARTED = "Swap_Started"
    const val SWAP_COMPLETED = "Swap_Completed"

    const val BUY_CURRENCY_CHANGED = "Buy_Currency_Changed"
    const val BUY_COIN_CHANGED = "Buy_Coin_Changed"
    const val BUY_TOTAL_SHOWED = "Buy_Total_Showed"
    const val BUY_CHOSEN_METHOD_PAYMENT = "Buy_Chosen_Method_Payment"
    const val BUY_BUTTON_PRESSED = "Buy_Button_Pressed"
    const val BUY_MOONPAY_WINDOW = "Moonpay_Window"
}
