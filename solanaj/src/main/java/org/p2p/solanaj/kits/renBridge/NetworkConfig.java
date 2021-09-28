package org.p2p.solanaj.kits.renBridge;

public class NetworkConfig {
    private String chain;
    private boolean isTestnet;
    private String endpoint;
    private String lightNode;
    private String gatewayRegistry;
    private String genesisHash;
    private int p2shPrefix;

    private NetworkConfig(
            String chain,
            boolean isTestnet,
            String endpiont,
            String lightNode,
            String gatewayRegistry,
            String genesisHash,
            int p2shPrefix) {
        this.chain = chain;
        this.isTestnet = isTestnet;
        this.endpoint = endpiont;
        this.lightNode = lightNode;
        this.gatewayRegistry = gatewayRegistry;
        this.genesisHash = genesisHash;
        this.p2shPrefix = p2shPrefix;
    }

    public static NetworkConfig MAINNET() {
        return new NetworkConfig("mainnet", false, "https://ren.rpcpool.com/",
                "https://lightnode-mainnet.herokuapp.com", "REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2",
                "5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d", 0x05);
    }

    public static NetworkConfig DEVNET() {
        return new NetworkConfig("testnet", true, "https://api.devnet.solana.com",
                "https://lightnode-testnet.herokuapp.com/", "REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2",
                "EtWTRABZaYq6iMfeYKouRu166VU2xqa1wcaWoxPkrZBG", 0xc4);
    }

    public String getChain() {
        return chain;
    }

    public boolean isTestnet() {
        return isTestnet;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getLightNode() {
        return lightNode;
    }

    public String getGatewayRegistry() {
        return gatewayRegistry;
    }

    public String getGenesisHash() {
        return genesisHash;
    }

    public int getP2shPrefix() {
        return p2shPrefix;
    }

}