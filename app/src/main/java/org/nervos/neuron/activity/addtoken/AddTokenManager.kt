package org.nervos.neuron.activity.addtoken

import android.content.Context
import android.text.TextUtils
import org.nervos.appchain.protocol.AppChainj
import org.nervos.appchain.protocol.core.DefaultBlockParameterName
import org.nervos.appchain.protocol.core.methods.response.AppMetaData
import org.nervos.appchain.protocol.http.HttpService
import org.nervos.neuron.R
import org.nervos.neuron.item.ChainItem
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.service.http.AppChainRpcService
import org.nervos.neuron.service.http.EthRpcService
import org.nervos.neuron.util.AddressUtil
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.ether.EtherUtil
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class AddTokenManager(val context: Context) {

    fun getChainNameList(chainList: List<ChainItem>): List<String> {
        var list = mutableListOf<String>()
        chainList.forEach {
            list.add(it.name)
        }
        list.add(context.resources.getString(R.string.appchain_native_token))
        return list
    }

    fun getChainList(): List<ChainItem> {
        var list = DBWalletUtil.getCurrentWallet(context).chainItems
        list.removeAt(0)
        list.add(0, EtherUtil.getEthChainItem())
        return list
    }

    //load erc20 by contract address
    fun loadErc20(address: String, contractAddress: String, chainItem: ChainItem): Observable<TokenItem> {
        return Observable.fromCallable {
            if (!AddressUtil.isAddressValid(contractAddress)) {
                throw Throwable(context.resources.getString(R.string.contract_address_error))
            } else if (!checkRepetitionContract(chainItem.chainId, contractAddress)) {
                throw Throwable(context.resources.getString(R.string.exist_erc20_token))
            }
            val tokenItem = when (EtherUtil.isEther(chainItem.chainId)) {
                true -> {
                    EthRpcService.getTokenInfo(contractAddress, address)
                }
                else -> {
                    AppChainRpcService.getErc20TokenInfo(contractAddress)
                }
            }
            when {
                tokenItem == null -> throw Throwable(context.resources.getString(R.string.contract_address_error))
                TextUtils.isEmpty(tokenItem.symbol) -> throw Throwable(context.resources.getString(R.string.input_token_info))
                else -> {
                    tokenItem.chainId = chainItem.chainId
                    tokenItem.chainName = chainItem.name
                    tokenItem
                }
            }
        }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun loadAppChain(httpProvider: String): Observable<ChainItem> {
        return Observable.fromCallable {
            AppChainj.build(HttpService(httpProvider))
        }.flatMap { service ->
            try {
                Observable.just(service.appMetaData(DefaultBlockParameterName.LATEST).send())
            } catch (e: Exception) {
                throw Throwable(context.resources.getString(R.string.appchain_node_error))
            }
        }.flatMap { metaData: AppMetaData? ->
            if (metaData == null) {
                throw Throwable(context.resources.getString(R.string.appchain_node_error))
            } else {
                var chainItem = ChainItem()
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
        val customList = DBWalletUtil.getCurrentWallet(context).tokenItems
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