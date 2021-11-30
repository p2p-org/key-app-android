package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

import java.util.AbstractMap;
import java.util.List;

public class ProgramSwapAccount {

    public final class Account {
        @SerializedName("data")
        private List<String> data;
        @SerializedName("executable")
        private boolean executable;
        @SerializedName("lamports")
        private double lamports;
        @SerializedName("owner")
        private String owner;
        @SerializedName("rentEpoch")
        private double rentEpoch;

        @SuppressWarnings({"rawtypes"})
        public Account(Object acc) {
            AbstractMap account = (AbstractMap) acc;

            this.data = (List<String>) account.get("data");
            this.executable = (boolean) account.get("executable");
            this.lamports = (double) account.get("lamports");
            this.owner = (String) account.get("owner");
            this.rentEpoch = (double) account.get("rentEpoch");
        }

        public List<String> getData() {
            return data;
        }

        public boolean isExecutable() {
            return executable;
        }

        public double getLamports() {
            return lamports;
        }

        public String getOwner() {
            return owner;
        }

        public double getRentEpoch() {
            return rentEpoch;
        }

    }

    @SerializedName("account")
    private Account account;
    @SerializedName("pubkey")
    private String pubkey;

    public Account getAccount() {
        return account;
    }

    public String getPubkey() {
        return pubkey;
    }

    public ProgramSwapAccount() {
    }

    @SuppressWarnings({"rawtypes"})
    public ProgramSwapAccount(AbstractMap pa) {
        this.account = (Account) new Account(pa.get("account"));
        this.pubkey = (String) pa.get("pubkey");
    }
}
