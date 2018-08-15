
const addressHex = "%1$s";
const rpcURL = "%2$s";
const chainID = "%3$s";

function executeCallback (id, error, value) {
  Neuron.executeCallback(id, error, value)
}
function onSignSuccessful(id, value) {
  Neuron.executeCallback(id, null, value)
}
function onSignError(id, error) {
  Neuron.executeCallback(id, error, null)
}
window.Neuron.init(rpcURL, {
  getAccounts: function (cb) { cb(null, [addressHex]) },
  processTransaction: function (tx, cb){
    console.log('signing a transaction', tx)
    const { id = 8888 } = tx
    Neuron.addCallback(id, cb)

    var data = tx.data || null;
    var nonce = tx.nonce || -1;
    var chainId = tx.chainId || -1;
    var version = tx.version || 0;
    var value = tx.value || null;
    var chainType = tx.chainType || null;

    if (tx.chainType == "ETH") {
        var gasLimit = tx.gasLimit || tx.gas || null;
        var gasPrice = tx.gasPrice || null;
        neuronSign.signTransaction(id, tx.to || null, value, nonce, gasLimit, gasPrice,
                        data, chainId, version, chainType);
    } else {
        var quota = tx.quota || null;
        var validUntilBlock = tx.validUntilBlock || 0;
        neuronSign.signTransaction(id, tx.to || null, value, nonce, quota, validUntilBlock,
                        data, chainId, version, chainType);
    }
  },
  signMessage: function (msgParams, cb) {
    console.log('signMessage', msgParams)
    const { data, chainType } = msgParams
    const { id = 8888 } = msgParams
    Neuron.addCallback(id, cb)
    neuronSign.signMessage(id, data, chainType);
  },
  signPersonalMessage: function (msgParams, cb) {
    console.log('signPersonalMessage', msgParams)
    const { data, chainType } = msgParams
    const { id = 8888 } = msgParams
    Neuron.addCallback(id, cb)
    neuronSign.signPersonalMessage(id, data, chainType);
  },
  signTypedMessage: function (msgParams, cb) {
    console.log('signTypedMessage ', msgParams)
    const { data } = msgParams
    const { id = 8888 } = msgParams
    Neuron.addCallback(id, cb)
    neuronSign.signTypedMessage(id, JSON.stringify(data))
  }
}, {
    address: addressHex,
    networkVersion: chainID
})
window.web3.setProvider = function () {
  console.debug('Neuron Wallet - overrode web3.setProvider')
}

window.web3.version.getNetwork = function(cb) {
    cb(null, chainID)
}
window.web3.eth.getCoinbase = function(cb) {
    return cb(null, addressHex)
}
window.web3.eth.defaultAccount = addressHex

window.web3.eth.getAccounts = function() {
    return [addressHex];
}

window.isNervosReady = true
window.isMetaMask = true