package org.nervos.neuron.constant

/**
 * Created by BaojunCZ on 2018/11/8.
 */
object SensorDataCons {
    val TRACK_SCAN_QR = "scanQRcode"
    val TAG_SCAN_TYPE = "scan_type"
    val TAG_SCAN_RESULT = "scan_result"

    val TRACK_CREATE_WALLET = "createWallet"
    val TAG_CREATE_WALLET_ADDRESS = "create_address"

    val TRACK_INPUT_WALLET = "inputWallet"
    val TAG_INPUT_WALLET_TYPE = "input_type"
    val TAG_INPUT_WALLET_RESULT = "input_result"
    val TAG_INPUT_WALLET_ADDRESS = "input_address"

    val TRACK_POSSESS_MONEY = "possess_money"
    val TAG_POSSESS_MONEY_CHAIN = "currency_chain"
    val TAG_POSSESS_MONEY_TYPE = "currency_type"
    val TAG_POSSESS_MONEY_NUMBER = "currency_number"

    val TRACK_TRANSFER_ACCOUNTS = "transfer_accounts"
    val TAG_TRANSFER_TARGET_CURRENCY = "target_currency"
    val TAG_TRANSFER_TARGET_CURRENCY_NUMBER = "target_currency_number"
    val TAG_TRANSFER_RECEIVE_ADDRESS = "receive_address"
    val TAG_TRANSFER_OUTCOME_ADDRESS = "outcome_address"
    val TAG_TRANSFER_TRANSFER_TYPE = "transfer_type"
    val TAG_TRANSFER_TARGET_CHAIN = "target_chain"
}