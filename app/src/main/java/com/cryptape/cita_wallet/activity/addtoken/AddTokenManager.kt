package com.cryptape.cita_wallet.activity.addtoken

import android.content.Context
import android.text.TextUtils
import com.cryptape.cita.protocol.CITAj
import com.cryptape.cita.protocol.core.DefaultBlockParameterName
import com.cryptape.cita.protocol.core.methods.response.AppMetaData
import com.cryptape.cita.protocol.http.HttpService
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.item.Chain
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.service.http.CITARpcService
import com.cryptape.cita_wallet.service.http.EthRpcService
import com.cryptape.cita_wallet.util.AddressUtil
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.ether.EtherUtil
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class AddTokenManager(val context: Context) {

    fun getChainNameList(chainList: List<Chain>): List<String> {
        var list = mutableListOf<String>()
        chainList.forEach {
            list.add(it.name)
        }
        list.add(context.resources.getString(R.string.cita_native_token))
        return list
    }

    fun getChainList(): List<Chain> {
        var list = DBWalletUtil.getCurrentWallet(context).chains
        list.removeAt(0)
        list.add(0, EtherUtil.getEthChainItem())
        return list
    }

    //load erc20 by contract address
    fun loadErc20(address: String, contractAddress: String, chain: Chain): Observable<Token> {
        return Observable.fromCallable {
            if (!AddressUtil.isAddressValid(contractAddress)) {
                throw Throwable(context.resources.getString(R.string.contract_address_error))
            } else if (!checkRepetitionContract(chain.chainId, contractAddress)) {
                throw Throwable(context.resources.getString(R.string.exist_erc20_token))
            }
            val tokenItem = when (EtherUtil.isEther(chain.chainId)) {
                true -> {
                    EthRpcService.getTokenInfo(contractAddress, address)
                }
                else -> {
                    CITARpcService.setHttpProvider(chain.httpProvider)
                    CITARpcService.getErc20TokenInfo(contractAddress)
                }
            }
            when {
                tokenItem == null || TextUtils.isEmpty(tokenItem.symbol) -> throw Throwable(context.resources.getString(R.string.contract_address_error))
                else -> {
                    tokenItem.chainId = chain.chainId
                    tokenItem.chainName = chain.name
                    tokenItem
                }
            }
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadCITA(httpProvider: String): Observable<Chain> {
        return Observable.fromCallable {
            CITAj.build(HttpService(httpProvider))
        }.flatMap { service ->
            try {
                Observable.just(service.appMetaData(DefaultBlockParameterName.LATEST).send())
            } catch (e: Exception) {
                throw Throwable(context.resources.getString(R.string.cita_node_error))
            }
        }.flatMap { metaData: AppMetaData? ->
            if (metaData == null) {
                throw Throwable(context.resources.getString(R.string.cita_node_error))
            } else {
                var chainItem = Chain()
                var result = metaData.appMetaDataResult
                chainItem.chainId = result.chainId.toString()
                chainItem.name = result.chainName
                chainItem.httpProvider = httpProvider
                chainItem.tokenAvatar = result.tokenAvatar
                chainItem.tokenSymbol = result.tokenSymbol
                chainItem.tokenName = result.tokenName
                Observable.just(chainItem)
            }
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun checkRepetitionContract(chainId: String, contractAddress: String): Boolean {
        val customList = DBWalletUtil.getCurrentWallet(context).tokens
        if (customList != null && customList.size > 0) {
            for (item in customList) {
                if (item.chainId == chainId && item.contractAddress == contractAddress) {
                    return false
                }
            }
        }
        return true
    }
}