package org.nervos.neuron.view.tokenprofile

import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.Request
import org.nervos.neuron.item.EthErc20TokenInfoItem
import org.nervos.neuron.service.http.HttpService
import org.nervos.neuron.util.AddressUtil
import org.nervos.neuron.util.url.HttpUrls
import org.web3j.crypto.Keys
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by BaojunCZ on 2018/10/9.
 */
class TokenDescribeModel {

    operator fun get(address: String): Observable<EthErc20TokenInfoItem?>? {
        return Observable.fromCallable {
            var mAddress = address
            if (AddressUtil.isAddressValid(address))
                mAddress = Keys.toChecksumAddress(address)
            mAddress
        }
                .subscribeOn(Schedulers.newThread())
                .flatMap {
                    val url = String.format(HttpUrls.TOKEN_DESC, it)
                    val request = Request.Builder().url(url).build()
                    val call = HttpService.getHttpClient().newCall(request)
                    val res = call.execute().body()!!.string()
                    if (!TextUtils.isEmpty(res)) {
                        val item = Gson().fromJson(res, EthErc20TokenInfoItem::class.java)
                        Observable.just(item)
                    } else {
                        Observable.just(null)
                    }
                }.observeOn(AndroidSchedulers.mainThread());
    }
}
