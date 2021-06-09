package org.p2p.solanaj.kits;
import java.util.List;

import com.squareup.moshi.Json;

import org.p2p.solanaj.rpc.types.RpcResultObject;

public class MultipleAccountsInfo extends RpcResultObject {

    public static class Info {
        @Json(name = "decimals")
        private long decimals;
        @Json(name = "freezeAuthority")
        private Object freezeAuthority;
        @Json(name = "isInitialized")
        private boolean isInitialized;
        @Json(name = "mintAuthority")
        private String mintAuthority;
        @Json(name = "supply")
        private String supply;

        public long getDecimals() {
            return decimals;
        }

        public Object getFreezeAuthority() {
            return freezeAuthority;
        }

        public boolean isIsInitialized() {
            return isInitialized;
        }

        public String getMintAuthority() {
            return mintAuthority;
        }

        public String getSupply() {
            return supply;
        }

    }

    public static class Parsed {
        @Json(name = "info")
        private Info info;
        @Json(name = "type")
        private String type;

        public Info getInfo() {
            return info;
        }

        public String getType() {
            return type;
        }

    }

    public static class Data {
        @Json(name = "parsed")
        private Parsed parsed;
        @Json(name = "program")
        private String program;
        @Json(name = "space")
        private long space;

        public Parsed getParsed() {
            return parsed;
        }

        public String getProgram() {
            return program;
        }

        public long getSpace() {
            return space;
        }

    }

    public static class AccountInfoParsed {
        @Json(name = "data")
        private Data data;
        @Json(name = "executable")
        private boolean executable;
        @Json(name = "lamports")
        private long lamports;
        @Json(name = "owner")
        private String owner;
        @Json(name = "rentEpoch")
        private long rentEpoch;

        private String address;

        public Data getData() {
            return data;
        }

        public boolean isExecutable() {
            return executable;
        }

        public long getLamports() {
            return lamports;
        }

        public String getOwner() {
            return owner;
        }

        public long getRentEpoch() {
            return rentEpoch;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

    }

    @Json(name = "value")
    private List<AccountInfoParsed> value;

    public List<AccountInfoParsed> getAccountsInfoParsed() {
        return value;
    }

    public void setAccountsInfoParsed(List<AccountInfoParsed> info) {
        value = info;
    }

}