package org.p2p.solanaj.rpc;

public enum Environment {
    MAINNET("https://api.mainnet-beta.solana.com"),
    DATAHUB("https://solana--mainnet--rpc.datahub.figment.io"),
    PROJECT_SERUM("https://solana-api.projectserum.com");

    private final String endpoint;

    Environment(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
