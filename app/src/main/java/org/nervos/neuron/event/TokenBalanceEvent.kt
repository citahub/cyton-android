package org.nervos.neuron.event

import org.nervos.neuron.item.WalletTokenLoadItem

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class TokenBalanceEvent(var item: WalletTokenLoadItem, var address: String)