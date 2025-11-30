package com.cognizant.paymentservice.model;

public class WalletTopUpResponse {
    private WalletTopUpResponseTransaction transaction;
    private Wallet wallet;

    public WalletTopUpResponse() {}

    public WalletTopUpResponse(WalletTopUpResponseTransaction transaction, Wallet wallet) {
        this.transaction = transaction;
        this.wallet = wallet;
    }

    public WalletTopUpResponseTransaction getTransaction() { return transaction; }
    public void setTransaction(WalletTopUpResponseTransaction transaction) { this.transaction = transaction; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
}
