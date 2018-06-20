
function executeCallback (id, error, value) {
    Trust.executeCallback(id, error, value)
}

Trust.init({
    rpcURL,
    wssURL,
    getAccounts: function (cb) { cb(null, [addressHex]) },
    processTransaction: function (tx, cb){
        console.log('signing a transaction', tx)
        const { data } = tx
        const { id = 8888 } = tx
        Trust.addCallback(id, cb)
        appHybrid.signTransaction(JSON.stringify(data))
        webkit.messageHandlers.signTransaction.postMessage({"name": "signTransaction", "object": { data }, id: id})
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
    sendTransaction: function (tx, cb) {
        const { id = 8888 } = tx
        console.log("send a transaction", tx)
        Trust.addCallback(id, cb)
        appHybrid.sendTransaction(JSON.stringify(tx))
        webkit.messageHandlers.signTypedMessage.postMessage({"name": "signTypedMessage", "object": tx , id: id})
    }
}, {
    address: addressHex,
    networkVersion: chainID
})

web3.setProvider = function () {
    console.debug('Trust Wallet - overrode web3.setProvider')
}

web3.eth.defaultAccount = addressHex

web3.currentProvider.isMetaMask = true;

var handle = web3.handleRequest;
web3.handleRequest = function(payload, next, end) {
     switch(payload.method) {
         case 'cita_sendTransaction':
            console.log("cita")
            break;
         default:
            handle(payload, next, end);
            break;
     }
}

web3.version.getNetwork = function(cb) {
    cb(null, chainID)
}
web3.eth.getCoinbase = function(cb) {
    return cb(null, addressHex)
}
