package org.p2p.wallet.common.analytics.constants

object ScreenNames {

    object OnBoarding {
        const val WALLET_CREATE = "Wallet_Create"
        const val IMPORT_MANUAL = "Import_Manual"
        const val CREATE_MANUAL = "Create_Manual"
        const val PIN_CREATE = "Pin_Create"
        const val PIN_CONFIRM = "Pin_Confirm"
        const val SEED_INFO = "Seed_Info"
        const val SEED_VERIFY = "Seed_Verify"
        const val DERIVATION = "Derivation"
        const val USERNAME_RESERVE = "Username_Reserve"
        const val TERMS_OF_USE = "Terms_Of_Use"
        const val WELCOME_NEW = "Welcome_New"
        const val WELCOME_NEW_USERNAME = "Welcome_New_Username"
        const val WELCOME_BACK = "Welcome_Back"
        const val WELCOME_BACK_USERNAME = "Welcome_Back_Username"
    }

    object Main {
        const val MAIN = "Main_Screen"
        const val MAIN_FEEDBACK = "Main_Feedback"
        const val MAIN_HISTORY = "Main_History"
        const val MAIN_EARN = "Main_Earn"
        const val MAIN_SWAP = "Main_Swap"
    }

    object Token {
        const val TOKEN_SCREEN = "Token_Screen"
        const val TOKEN_RECEIVE = "Token_Receive (ETH_Receive)"
    }

    object Settings {
        const val MAIN = "Settings"
        const val USERNAME_RESERVE = "Settings_Username_Reserve"
        const val USERCARD = "Settings_Usercard"
        const val SEED = "Settings_Seed"
        const val SECURITY = "Settings_Security"
        const val SEED_VERIFY = "Settings_Seed_Verify"
        const val FEES = "Settings_Fees"
        const val NETWORK = "Settings_Network"
        const val PIN_ENTER = "Settings_PIN_Enter"
        const val PIN_CREATE = "Settings_PIN_Create"
        const val PIN_CONFIRM = "Settings_PIN_Confirm"
        const val PIN_RESET = "Settings_PIN_Reset"
        const val ZERO_BALANCES = "Settings_Zero_Balances"
    }

    object Send {
        const val MAIN = "Send_Main"
        const val RECIPIENT_ADDRESS = "Send_Recipient_Address"
        const val QR_CAMERA = "QR_Camera"
        const val QR_GALLERY = "QR_Gallery"
        const val CURRENCY = "Send_Currency"
        const val FEE_CURRENCY = "Send_Fee_Currency"
        const val CONFIRMATION = "Send_Confirmation"
        const val NETWORK = "Send_Network"
        const val PROCESSING = "Send_Processing"
        const val SUCCESS = "Send_Success"
        const val ERROR = "Send_Error"
        const val TRANSACTION_INFO = "Send_Transaction_Info"
    }

    object Swap {
        const val MAIN = "Swap_Main"
        const val CURRENCY_A = "Swap_Currency_A"
        const val CURRENCY_B = "Swap_Currency_B"
        const val SETTINGS = "Swap_Settings"
        const val CONFIRMATION = "Swap_Confirmation"
        const val PROCESSING = "Swap_Processing"
        const val SUCCESS = "Swap_Success"
        const val ERROR = "Swap_Error"
        const val TRANSACTION_INFO = "Swap_Transaction_Info"
    }

    object Buy {
        const val BUY = "Buy_Screen"
        const val SOL = "Buy_SOL"
        const val EXTERNAL = "Buy_External"
    }

    object Receive {
        const val SOLANA = "Receive_Solana"
        const val BITCOIN = "Receive_Bitcoin"
        const val LIST = "Receive_List"
        const val BITCOIN_INFO = "Receive_Bitcoin_Info"
        const val BITCOIN_STATUSES = "Receive_Bitcoin_Statuses"
        const val BITCOIN_STATUS = "Receive_Bitcoin_Status"
        const val NETWORK = "Receive_Network"
        const val TRANSACTION_INFO = "Receive_Transaction_Info"
    }

    object Lock {
        const val SCREEN = "Lock_Screen"
    }
}
