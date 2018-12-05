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

    fun getChainNameList(): List<String> {
        var chainList = DBWalletUtil.getCurrentWallet(context).chainItems
        var list = mutableListOf<String>()
        chainList.forEach {
            list.add(it.name)
        }
        list.add(1, ConstantUtil.ETH_RINKEBY_NAME)
        list.add(context.resources.getString(R.string.appchain_native_token))
        return list
    }

    fun getChainList(): List<ChainItem> {
        var list = DBWalletUtil.getCurrentWallet(context).chainItems
        var item = ChainItem(ConstantUtil.ETHEREUM_RINKEBY_ID, ConstantUtil.ETH_RINKEBY_NAME, ConstantUtil.ETH, ConstantUtil.ETH)
        list.add(1, item)
        return list
    }

    //load erc20 by contract address
    fun loadErc20(address: String, contractAddress: String, chainItem: ChainItem): Observable<TokenItem> {
        return Observable.fromCallable {
            if (!AddressUtil.isAddressValid(contractAddress)) {
                throw Throwable(context.resources.getString(R.string.contract_address_error))
            } else if (!checkRepetitionContract(contractAddress)) {
                throw Throwable(context.resources.getString(R.string.exist_erc20_token))
            }
        }.flatMap {
            val tokenItem = when (chainItem.chainId) {
                ConstantUtil.ETHEREUM_MAIN_ID -> {
                    EthRpcService.initNodeUrl(EtherUtil.getEthNodeUrl(chainItem.chainId))
                    EthRpcService.getTokenInfo(contractAddress, address)
                }
                ConstantUtil.ETHEREUM_RINKEBY_ID -> {
                    EthRpcService.initNodeUrl(EtherUtil.getEthNodeUrl(chainItem.chainId))
                    EthRpcService.getTokenInfo(contractAddress, address)
                }
                else -> AppChainRpcService.getErc20TokenInfo(contractAddress)
            }
            when {
                tokenItem == null -> throw Throwable(context.resources.getString(R.string.contract_address_error))
                TextUtils.isEmpty(tokenItem.symbol) -> throw Throwable(context.resources.getString(R.string.input_token_info))
                else -> {
                    tokenItem.chainId = chainItem.chainId
                    tokenItem.chainName = chainItem.name
                    Observable.just(tokenItem)
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

    private fun checkRepetitionContract(contractAddress: String): Boolean {
        val customList = DBWalletUtil.getCurrentWallet(context).tokenItems
        if (customList != null && customList.size > 0) {
            for (item in customList) {
                if (item.contractAddress.equals(contractAddress, ignoreCase = true)) return false
            }
        }
        return true
    }
}