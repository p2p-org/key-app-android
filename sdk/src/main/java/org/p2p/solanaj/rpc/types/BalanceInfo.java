package org.p2p.solanaj.rpc.types;

public class BalanceInfo {
    String depositAddress;
    long amount;
    String mint;
    String owner;
    int decimals;

    @Override
    public String toString() {
        return "BalanceInfo{" +
                "depositAddress='" + depositAddress + '\'' +
                ", amount=" + amount +
                ", mint='" + mint + '\'' +
                ", owner='" + owner + '\'' +
                ", decimals=" + decimals +
                '}';
    }
}
