package org.nervos.neuron.constant

/**
 * Created by BaojunCZ on 2018/11/8.
 */
object SensorDataCons {
    const val TRACK_SCAN_QR = "scanQRcode"
    const val TAG_SCAN_TYPE = "scan_type"
    const val TAG_SCAN_TYPE_KEYSTORE = "3"
    const val TAG_SCAN_TYPE_PRIVATEKEY = "2"
    const val TAG_SCAN_RESULT = "scan_result"

    const val TRACK_CREATE_WALLET = "createWallet"
    const val TAG_CREATE_WALLET_ADDRESS = "create_address"

    const val TRACK_INPUT_WALLET = "inputWallet"
    const val TAG_INPUT_WALLET_TYPE = "input_type"
    const val TAG_INPUT_WALLET_RESULT = "input_result"
    const val TAG_INPUT_WALLET_ADDRESS = "input_address"

    const val TRACK_POSSESS_MONEY = "possess_money"
    const val TAG_POSSESS_MONEY_CHAIN = "currency_chain"
    const val TAG_POSSESS_MONEY_TYPE = "currency_type"
    const val TAG_POSSESS_MONEY_NUMBER = "currency_number"

    const val TRACK_TRANSFER_ACCOUNTS = "transfer_accounts"
    const val TAG_TRANSFER_TARGET_CURRENCY = "target_currency"
    const val TAG_TRANSFER_TARGET_CURRENCY_NUMBER = "target_currency_number"
    const val TAG_TRANSFER_RECEIVE_ADDRESS = "receive_address"
    const val TAG_TRANSFER_OUTCOME_ADDRESS = "outcome_address"
    const val TAG_TRANSFER_TRANSFER_TYPE = "transfer_type"
    const val TAG_TRANSFER_TARGET_CHAIN = "target_chain"
}