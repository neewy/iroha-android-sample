package com.example.myapplication;

import java.security.KeyPair;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.DatatypeConverter;

import iroha.protocol.QryResponses;
import iroha.protocol.Queries;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;

public class IrohaHandler {

    public static final String coinId = "coin#test";

    private static IrohaHandler instance;
    private IrohaAPI api;

    public static synchronized IrohaHandler getInstance() {
        if (instance == null) {
            instance = new IrohaHandler();
        }
        return instance;
    }

    private IrohaHandler() {
        MyAccount.getInstance();
        api = new IrohaAPI("10.211.38.47", 50051);
    }

    void sendAsset(String to, String message, String amount) {
        TransactionOuterClass.Transaction tx = Transaction.builder(MyAccount.accountId)
                .transferAsset(MyAccount.accountId, to, coinId, message, amount)
                .sign(MyAccount.myKeypair).build();
        sendTransaction(tx);
    }

    void addAsset(String amount) {
        TransactionOuterClass.Transaction tx = Transaction.builder(MyAccount.accountId)
                .addAssetQuantity(coinId, amount)
                .sign(MyAccount.myKeypair).build();
        sendTransaction(tx);
    }


    private void sendTransaction(TransactionOuterClass.Transaction tx) {
        TransactionStatusObserver observer = TransactionStatusObserver.builder()
                // executed when stateless or stateful validation is failed
                .onTransactionFailed(t -> System.out.println(String.format(
                        "transaction %s failed with msg: %s",
                        t.getTxHash(),
                        t.getErrOrCmdName()
                )))
                // executed when got any exception in handlers or grpc
                .onError(e -> System.out.println("Failed with exception: " + e))
                // executed when we receive "committed" status
                .onTransactionCommitted((t) -> System.out.println("Committed :)"))
                // executed when transfer is complete (failed or succeed) and observable is closed
                .onComplete(() -> System.out.println("Complete"))
                .build();
        api.transaction(tx).subscribe(observer);
    }

    int getBalance() {
        // build protobuf query, sign it
        Queries.Query q = Query.builder(MyAccount.accountId, 1)
                .getAccountAssets(MyAccount.accountId)
                .buildSigned(MyAccount.myKeypair);

        // execute query, get response
        QryResponses.QueryResponse res = api.query(q);

        // get list of assets from our response
        List<QryResponses.AccountAsset> assets = res.getAccountAssetsResponse().getAccountAssetsList();

        // find usd asset
        Optional<QryResponses.AccountAsset> assetOptional = assets
                .stream()
                .filter(a -> a.getAssetId().equals(coinId))
                .findFirst();

        // numbers are hopefully small, so we use int here for simplicity
        return assetOptional
                .map(a -> Integer.parseInt(a.getBalance()))
                .orElse(0);
    }

    public static class MyAccount {

        private static MyAccount instance;


        static final String accountName = "admin";
        static final String accountDomain = "test";
        static final String accountId = String.format("%s@%s", accountName, accountDomain);
        static KeyPair myKeypair;

        public static synchronized MyAccount getInstance() {
            if (instance == null) {
                instance = new MyAccount();
            }
            return instance;
        }

        private MyAccount() {
            byte[] privateKey = DatatypeConverter.parseHexBinary("f101537e319568c765b2cc89698325604991dca57b9716b58016b253506cab70");
            byte[] publicKey = DatatypeConverter.parseHexBinary("313a07e6384776ed95447710d15e59148473ccfc052a681317a72a69f2a49910");
            myKeypair = Ed25519Sha3.keyPairFromBytes(privateKey, publicKey);
        }
    }
}
