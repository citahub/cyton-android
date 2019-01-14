package com.cryptape.cita_wallet.util

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.util.ether.EtherUtil
import com.cryptape.cita_wallet.constant.url.HttpUrls
import org.web3j.crypto.Keys

/**
 * Created by BaojunCZ on 2018/11/21.
 */
object TokenLogoUtil {

    fun setLogo(token: Token, context: Context, iv: ImageView) {
        if (EtherUtil.isEther(token) && !TextUtils.isEmpty(token.contractAddress)) {
            var address = token.contractAddress
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
            var icon = if (EtherUtil.isEther(token)) R.drawable.ether_big else R.mipmap.ic_launcher
            val options = RequestOptions()
                    .error(icon)
                    .placeholder(icon)
            Glide.with(context)
                    .load(if (TextUtils.isEmpty(token.avatar)) "" else Uri.parse(token.avatar))
                    .apply(options)
                    .into(iv)
        }
    }


}