package org.p2p.solanaj.kits;

import java.util.List;

import com.squareup.moshi.Json;

import org.p2p.solanaj.rpc.types.RpcResultObject;

public class TokenAccounts extends RpcResultObject {

    public static class Account {
        @Json(name = "account")
        private AccountInfoParsed account;
        @Json(name = "pubkey")
        private String pubkey;

        public AccountInfoParsed getAccount() {
            return account;
        }

        public String getPubkey() {
            return pubkey;
        }
    }

    @Json(name = "value")
    private List<Account> value;

    public List<Account> getAccounts() {
        return value;
    }
}