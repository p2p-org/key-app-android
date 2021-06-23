package org.p2p.solanaj.kits.transaction;

import java.util.List;
import java.util.Map;

import com.squareup.moshi.Json;

import org.p2p.solanaj.model.types.RpcResultObject;

public class ConfirmedTransactionParsed extends RpcResultObject {

    public static class AccountKeysParsed {
        @Json(name = "pubkey")
        private String pubkey;
        @Json(name = "signer")
        private boolean signer;
        @Json(name = "writable")
        private boolean writable;

        public String getPubkey() {
            return pubkey;
        }

    }

    public static class Parsed {
        @Json(name = "info")
        private Map<String, Object> info;
        @Json(name = "type")
        private String type;

        public Map<String, Object> getInfo() {
            return info;
        }

        public String getType() {
            return type;
        }

    }

    public static class InstructionParsed {
        @Json(name = "parsed")
        private Parsed parsed;
        @Json(name = "program")
        private String program;
        @Json(name = "programId")
        private String programId;
        @Json(name = "accounts")
        private List<String> accounts;
        @Json(name = "data")
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
        @Json(name = "accountKeys")
        private List<AccountKeysParsed> accountKeys;
        @Json(name = "instructions")
        private List<InstructionParsed> instructions;
        @Json(name = "recentBlockhash")
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
        @Json(name = "message")
        private MessageParsed message;
        @Json(name = "signatures")
        private List<String> signatures;

        public MessageParsed getMessage() {
            return message;
        }

        public List<String> getSignatures() {
            return signatures;
        }

    }

    @Json(name = "transaction")
    private TransactionParsed transaction;

    public TransactionParsed getTransaction() {
        return transaction;
    }

}