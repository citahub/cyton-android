
const addressHex = "%1$s";
const rpcURL = "%2$s";
const chainID = "%3$s";

function executeCallback (id, error, value) {
  Cyton.executeCallback(id, error, value)
}
function onSignSuccessful(id, value) {
  console.log("onSignSuccessful", value)
  Cyton.executeCallback(id, null, value)
}
function onSignError(id, error) {
  Cyton.executeCallback(id, error, null)
}
window.Cyton.init(rpcURL, {
  getAccounts: function (cb) { cb(null, [addressHex]) },
  processTransaction: function (tx, cb){
    console.log('signing a transaction', tx)
    const { id = 8888 } = tx
    Cyton.addCallback(id, cb)

    var data = tx.data || null;
    var nonce = tx.nonce || -1;
    var chainId = tx.chainId || -1;
    var version = tx.version || 0;
    var value = tx.value || null;
    var chainType = tx.chainType || null;

    if (tx.chainType == "ETH") {
        var gasLimit = tx.gasLimit || tx.gas || null;
        var gasPrice = tx.gasPrice || null;
        chainId = -1;
        cytonSign.signTransaction(id, tx.to || null, value, nonce, gasLimit, gasPrice,
                        data, chainId, version, chainType);
    } else {
        var quota = tx.quota || null;
        var validUntilBlock = tx.validUntilBlock || 0;
        cytonSign.signTransaction(id, tx.to || null, value, nonce, quota, validUntilBlock,
                        data, chainId, version, chainType);
    }
  },
  signMessage: function (msgParams, cb) {
    console.log('signMessage', msgParams)
    const { data, chainType } = msgParams
    const { id = 8888 } = msgParams
    Cyton.addCallback(id, cb)
    cytonSign.signMessage(id, data, chainType);
  },
  signPersonalMessage: function (msgParams, cb) {
    console.log('signPersonalMessage', msgParams)
    const { data, chainType } = msgParams
    const { id = 8888 } = msgParams
    Cyton.addCallback(id, cb)
    cytonSign.signPersonalMessage(id, data, chainType);
  },
  signTypedMessage: function (msgParams, cb) {
    console.log('signTypedMessage ', msgParams)
    const { data } = msgParams
    const { id = 8888 } = msgParams
    Cyton.addCallback(id, cb)
    cytonSign.signTypedMessage(id, JSON.stringify(data))
  }
}, {
    address: addressHex,
    networkVersion: chainID
})
window.web3.setProvider = function () {
  console.debug('Cyton Wallet - overrode web3.setProvider')
}

window.web3.version.getNetwork = function(cb) {
    cb(null, chainID)
}
window.web3.eth.getCoinbase = function(cb) {
    return cb(null, addressHex)
}
window.web3.eth.defaultAccount = addressHex

window.isNervosReady = true
window.isMetaMask = true