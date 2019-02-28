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
    private static IrohaAPI api;
    public static MyAccount account;

    public static synchronized IrohaHandler getInstance() {
        if (instance == null) {
            instance = new IrohaHandler();
        }
        return instance;
    }

    public static void terminateChannel(){
        if (api != null) {
            api.getChannel().shutdownNow();
        }
    }

    private IrohaHandler() {
    }

    public static void setApi(IrohaAPI api_passed) {
        api = api_passed;
    }

    public static void setAccount(IrohaSettingsMessage message) {
        account = new MyAccount(message.accountId, message.privateKey, message.publicKey);
    }

    void sendAsset(String to, String message, String amount) {
        TransactionOuterClass.Transaction tx = Transaction.builder(account.accountId)
                .transferAsset(account.accountId, to, coinId, message, amount)
                .sign(account.myKeypair).build();
        sendTransaction(tx);
    }

    void addAsset(String amount) {
        TransactionOuterClass.Transaction tx = Transaction.builder(account.accountId)
                .addAssetQuantity(coinId, amount)
                .sign(account.myKeypair).build();
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
        Queries.Query q = Query.builder(account.accountId, 1)
                .getAccountAssets(account.accountId)
                .buildSigned(account.myKeypair);

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

        String accountId;
        KeyPair myKeypair;

        public MyAccount(String accountId, String privateKeyHex, String publicKeyHex) {
            this.accountId = accountId;
            byte[] privateKey = DatatypeConverter.parseHexBinary(privateKeyHex);
            byte[] publicKey = DatatypeConverter.parseHexBinary(publicKeyHex);
            myKeypair = Ed25519Sha3.keyPairFromBytes(privateKey, publicKey);
        }
    }
}
