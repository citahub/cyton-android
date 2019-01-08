package org.nervos.neuron.event

import org.nervos.neuron.item.Token

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class TokenBalanceEvent(var item: Token, var address: String)