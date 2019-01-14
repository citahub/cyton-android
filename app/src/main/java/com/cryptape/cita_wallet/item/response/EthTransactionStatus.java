package com.cryptape.cita_wallet.item.response;

/**
 * Created by duanyytop on 2018/12/14.
 */
public class EthTransactionStatus {

    /**
     * status : 1
     * message : OK
     * result : {"status":"0"}
     */

    public String status;
    public String message;
    public Result result;

    public static class Result {

        public int status;   // 0 = Fail, 1 = Pass
    }
}
