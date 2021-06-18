package org.p2p.solanaj.kits;

import com.squareup.moshi.Json;

import org.p2p.solanaj.rpc.types.RpcResultObject;

import java.util.List;

public class MultipleAccountsInfo extends RpcResultObject {

    @Json(name = "value")
    private List<AccountInfoParsed> value;

    public List<AccountInfoParsed> getAccountsInfoParsed() {
        return value;
    }

    public void setAccountsInfoParsed(List<AccountInfoParsed> info) {
        value = info;
    }

}