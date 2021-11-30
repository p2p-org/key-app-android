package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ConfirmedTransaction {

    public static class Header {

        @SerializedName("numReadonlySignedAccounts")
        private long numReadonlySignedAccounts;
        @SerializedName("numReadonlyUnsignedAccounts")
        private long numReadonlyUnsignedAccounts;
        @SerializedName("numRequiredSignatures")
        private long numRequiredSignatures;

        public long getNumReadonlySignedAccounts() {
            return numReadonlySignedAccounts;
        }

        public long getNumReadonlyUnsignedAccounts() {
            return numReadonlyUnsignedAccounts;
        }

        public long getNumRequiredSignatures() {
            return numRequiredSignatures;
        }

    }

    public static class Instruction {

        @SerializedName("accounts")
        private List<Long> accounts = null;
        @SerializedName("data")
        private String data;
        @SerializedName("programIdIndex")
        private long programIdIndex;

        public List<Long> getAccounts() {
            return accounts;
        }

        public String getData() {
            return data;
        }

        public long getProgramIdIndex() {
            return programIdIndex;
        }

    }

    public static class Message {

        @SerializedName("accountKeys")
        private List<String> accountKeys = null;
        @SerializedName("header")
        private Header header;
        @SerializedName("instructions")
        private List<Instruction> instructions = null;
        @SerializedName("recentBlockhash")
        private String recentBlockhash;

        public List<String> getAccountKeys() {
            return accountKeys;
        }

        public Header getHeader() {
            return header;
        }

        public List<Instruction> getInstructions() {
            return instructions;
        }

        public String getRecentBlockhash() {
            return recentBlockhash;
        }

    }

    public static class Status {

        @SerializedName("Ok")
        private Object ok;

        public Object getOk() {
            return ok;
        }

    }

    public static class Meta {

        @SerializedName("err")
        private Object err;
        @SerializedName("fee")
        private long fee;
        @SerializedName("innerInstructions")
        private List<Object> innerInstructions = null;
        @SerializedName("postBalances")
        private List<Long> postBalances = null;
        @SerializedName("preBalances")
        private List<Long> preBalances = null;
        @SerializedName("status")
        private Status status;

        public Object getErr() {
            return err;
        }

        public long getFee() {
            return fee;
        }

        public List<Object> getInnerInstructions() {
            return innerInstructions;
        }

        public List<Long> getPostBalances() {
            return postBalances;
        }

        public List<Long> getPreBalances() {
            return preBalances;
        }

        public Status getStatus() {
            return status;
        }

    }

    public static class Transaction {

        @SerializedName("message")
        private Message message;
        @SerializedName("signatures")
        private List<String> signatures = null;

        public Message getMessage() {
            return message;
        }

        public List<String> getSignatures() {
            return signatures;
        }

    }

    @SerializedName("meta")
    private Meta meta;
    @SerializedName("slot")
    private long slot;
    @SerializedName("transaction")
    private Transaction transaction;

    public Meta getMeta() {
        return meta;
    }

    public long getSlot() {
        return slot;
    }

    public Transaction getTransaction() {
        return transaction;
    }

}
