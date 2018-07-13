package trust.web3;

import trust.web3.item.Transaction;

public interface OnSignTransactionListener {
    void onSignTransaction(Transaction transaction);
}
