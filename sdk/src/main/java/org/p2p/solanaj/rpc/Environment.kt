package org.p2p.solanaj.rpc

enum class Environment(val endpoint: String) {
    MAINNET("https://api.mainnet-beta.solana.com/"),
    DATAHUB("https://solana--mainnet--rpc.datahub.figment.io/"),
    PROJECT_SERUM("https://solana-api.projectserum.com/");
}