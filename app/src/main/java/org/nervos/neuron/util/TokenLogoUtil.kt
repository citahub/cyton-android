package org.nervos.neuron.util

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.nervos.neuron.R
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.util.ether.EtherUtil
import org.nervos.neuron.util.url.HttpUrls
import org.web3j.crypto.Keys

/**
 * Created by BaojunCZ on 2018/11/21.
 */
class TokenLogoUtil {
    companion object {
        fun setLogo(tokenItem: TokenItem, context: Context, iv: ImageView) {
            if (EtherUtil.isEther(tokenItem) && !TextUtils.isEmpty(tokenItem.contractAddress)) {
                var address = tokenItem.contractAddress
                if (AddressUtil.isAddressValid(address))
                    address = Keys.toChecksumAddress(address)
                val options = RequestOptions()
                        .error(R.drawable.ether_big)
                        .placeholder(R.drawable.ether_big)
                Glide.with(context)
                        .load(Uri.parse(String.format(HttpUrls.TOKEN_LOGO, address)))
                        .apply(options)
                        .into(iv)

            } else {
                var icon = if (EtherUtil.isEther(tokenItem)) R.drawable.ether_big else R.mipmap.ic_launcher
                val options = RequestOptions()
                        .error(icon)
                        .placeholder(icon)
                Glide.with(context)
                        .load(if (TextUtils.isEmpty(tokenItem.avatar)) "" else Uri.parse(tokenItem.avatar))
                        .apply(options)
                        .into(iv)
            }
        }
    }


}