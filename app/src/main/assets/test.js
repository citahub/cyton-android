
function executeCallback (id, error, value) {
    Trust.executeCallback(id, error, value)
}

Trust.init({
    rpcURL,
    wssURL,
    getAccounts: function (cb) { cb(null, [addressHex]) },
    processTransaction: function (tx, cb){
    console.log('signing a transaction', tx)
    const { id = 8888 } = tx
    Trust.addCallback(id, cb)
    webkit.messageHandlers.signTransaction.postMessage({"name": "signTransaction", "object": tx, id: id})
    },
    signMessage: function (msgParams, cb) {
    const { data } = msgParams
    const { id = 8888 } = msgParams
    console.log("signing a message", msgParams)
    Trust.addCallback(id, cb)
    webkit.messageHandlers.signMessage.postMessage({"name": "signMessage", "object": { data }, id: id})
},
    signPersonalMessage: function (msgParams, cb) {
    const { data } = msgParams
    const { id = 8888 } = msgParams
    console.log("signing a personal message", msgParams)
    Trust.addCallback(id, cb)
    webkit.messageHandlers.signPersonalMessage.postMessage({"name": "signPersonalMessage", "object": { data }, id: id})
    },
    signTypedMessage: function (msgParams, cb) {
    const { data } = msgParams
    const { id = 8888 } = msgParams
    console.log("signing a typed message", msgParams)
    Trust.addCallback(id, cb)
    webkit.messageHandlers.signTypedMessage.postMessage({"name": "signTypedMessage", "object": { data }, id: id})
    }
}, {
    address: addressHex,
    networkVersion: chainID
})

web3.setProvider = function () {
    console.debug('Trust Wallet - overrode web3.setProvider')
}

web3.eth.defaultAccount = addressHex

web3.version.getNetwork = function(cb) {
    cb(null, chainID)
}
web3.eth.getCoinbase = function(cb) {
    return cb(null, addressHex)
}