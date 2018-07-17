
const addressHex = "%1$s";
const rpcURL = "%2$s";
const chainID = "%3$s";
function executeCallback (id, error, value) {
  Trust.executeCallback(id, error, value)
}
function onSignSuccessful(id, value) {
  Trust.executeCallback(id, null, value)
}
function onSignError(id, error) {
  Trust.executeCallback(id, error, null)
}
window.Trust.init(rpcURL, {
  getAccounts: function (cb) { cb(null, [addressHex]) },
  processTransaction: function (tx, cb){
    console.log('signing a transaction', tx)
    const { id = 8888 } = tx
    Trust.addCallback(id, cb)

    var data = tx.data || null;
    var nonce = tx.nonce || -1;
    var chainId = tx.chainId || -1;
    var version = tx.version || 0;
    var value = tx.value || null;
    var chainType = tx.chainType || null;

    if (tx.chainType == "ETH") {
        var gasLimit = tx.gasLimit || tx.gas || null;
        var gasPrice = tx.gasPrice || null;
        trust.signTransaction(id, tx.to || null, value, nonce, gasLimit, gasPrice,
                        data, chainId, version, chainType);
    } else {
        var quota = tx.quota || null;
        var validUntilBlock = tx.validUntilBlock || 0;
        trust.signTransaction(id, tx.to || null, value, nonce, quota, validUntilBlock,
                        data, chainId, version, chainType);
    }
  },
  signMessage: function (msgParams, cb) {
    console.log('signMessage', msgParams)
    const { data, chainType } = msgParams
    const { id = 8888 } = msgParams
    Trust.addCallback(id, cb)
    trust.signMessage(id, data, chainType);
  },
  signPersonalMessage: function (msgParams, cb) {
    console.log('signPersonalMessage', msgParams)
    const { data } = msgParams
    const { id = 8888 } = msgParams
    Trust.addCallback(id, cb)
    trust.signPersonalMessage(id, data);
  },
  signTypedMessage: function (msgParams, cb) {
    console.log('signTypedMessage ', msgParams)
    const { data } = msgParams
    const { id = 8888 } = msgParams
    Trust.addCallback(id, cb)
    trust.signTypedMessage(id, JSON.stringify(data))
  }
}, {
    address: addressHex,
    networkVersion: chainID
})
window.web3.setProvider = function () {
  console.debug('Trust Wallet - overrode web3.setProvider')
}
window.web3.eth.defaultAccount = addressHex
window.web3.version.getNetwork = function(cb) {
    cb(null, chainID)
}
window.web3.eth.getCoinbase = function(cb) {
    return cb(null, addressHex)
}
window.isNervosReady = true
window.isMetaMask = true