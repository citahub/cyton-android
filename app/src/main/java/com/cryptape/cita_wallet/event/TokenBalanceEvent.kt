package com.cryptape.cita_wallet.event

import com.cryptape.cita_wallet.item.Token

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class TokenBalanceEvent(var item: Token, var address: String)