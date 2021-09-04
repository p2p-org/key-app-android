package org.p2p.solanaj.kits.transaction;

import com.google.gson.annotations.SerializedName;

import org.p2p.solanaj.model.types.RpcResultObject;

import java.util.List;
import java.util.Map;

public class ConfirmedTransactionParsed extends RpcResultObject {

    public static class AccountKeysParsed {
        @SerializedName("pubkey")
        private String pubkey;
        @SerializedName("signer")
        private boolean signer;
        @SerializedName("writable")
        private boolean writable;

        public String getPubkey() {
            return pubkey;
        }

    }

    public static class Parsed {
        @SerializedName("info")
        private Map<String, Object> info;
        @SerializedName("type")
        private String type;

        public Map<String, Object> getInfo() {
            return info;
        }

        public String getType() {
            return type;
        }

    }

    public static class InstructionParsed {
        @SerializedName("parsed")
        private Parsed parsed;
        @SerializedName("program")
        private String program;
        @SerializedName("programId")
        private String programId;
        @SerializedName("accounts")
        private List<String> accounts;
        @SerializedName("data")
        private String data;

        public Parsed getParsed() {
            return parsed;
        }

        public String gerProgram() {
            return program;
        }

        public String gerProgramId() {
            return programId;
        }

        public List<String> getAccounts() {
            return accounts;
        }

        public String getData() {
            return data;
        }

    }

    public static class MessageParsed {
        @SerializedName("accountKeys")
        private List<AccountKeysParsed> accountKeys;
        @SerializedName("instructions")
        private List<InstructionParsed> instructions;
        @SerializedName("recentBlockhash")
        private String recentBlockhash;

        public List<AccountKeysParsed> getAccountKeys() {
            return accountKeys;
        }

        public List<InstructionParsed> getInstructions() {
            return instructions;
        }

        public String gerRecentBlockhash() {
            return recentBlockhash;
        }

    }

    public static class TransactionParsed {
        @SerializedName("message")
        private MessageParsed message;
        @SerializedName("signatures")
        private List<String> signatures;

        public MessageParsed getMessage() {
            return message;
        }

        public List<String> getSignatures() {
            return signatures;
        }

    }

    public static class InnerInstruction {
        @SerializedName("index")
        private int index;

        @SerializedName("instructions")
        private List<InstructionParsed> instructions;

        public List<InstructionParsed> getInstructions() {
            return instructions;
        }

    }

    public static class PostTokenBalance {
        @SerializedName("accountIndex")
        private int accountIndex;
        @SerializedName("mint")
        private String mint;

        public int getAccountIndex() {
            return accountIndex;
        }

        public String getMint() {
            return mint;
        }

    }

    public static class Meta {
        @SerializedName("fee")
        private long fee;
        @SerializedName("innerInstructions")
        private List<InnerInstruction> innerInstructions;
        @SerializedName("postTokenBalances")
        private List<PostTokenBalance> postTokenBalances;

        public List<InnerInstruction> getInnerInstructions() {
            return innerInstructions;
        }

        public List<PostTokenBalance> getPostTokenBalances() {
            return postTokenBalances;
        }

        public long getFee() {
            return fee;
        }
    }

    @SerializedName("blockTime")
    private long blockTime;

    @SerializedName("slot")
    private int slot;

    @SerializedName("transaction")
    private TransactionParsed transaction;

    @SerializedName("meta")
    private Meta meta;

    public TransactionParsed getTransaction() {
        return transaction;
    }

    public int getSlot() {
        return slot;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public Meta getMeta() {
        return meta;
    }

}