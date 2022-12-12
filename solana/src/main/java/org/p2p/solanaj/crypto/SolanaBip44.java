package org.p2p.solanaj.crypto;

/**
 * Utility class for Solana BIP-44 paths
 */
public class SolanaBip44 {
    private final HdKeyGenerator hdKeyGenerator = new HdKeyGenerator();

    private final SolanaCoin solanaCoin;
    private final long PURPOSE;
    private final long TYPE;
    private final long ACCOUNT;
    private final int CHANGE;

    public SolanaBip44(int walletIndex) {
        this.solanaCoin = new SolanaCoin();
        this.PURPOSE = solanaCoin.getPurpose();
        this.TYPE = solanaCoin.getCoinType();
        if (walletIndex != -1) {
            this.ACCOUNT = walletIndex;
        } else {
            this.ACCOUNT = 0;
        }
        this.CHANGE = 0;
    }


    /**
     * Get a root account address for a given seed using bip44 to match sollet implementation
     *
     * @param seed          seed
     * @param derivableType bip44 derivableType
     * @return PrivateKey
     */
    public byte[] getPrivateKeyFromSeed(byte[] seed, DerivationPath derivableType) {
        switch (derivableType) {
            case BIP44:
                return getPrivateKeyFromBip44Seed(seed);
            case BIP44CHANGE:
                return getPrivateKeyFromBip44SeedWithChange(seed);
            default:
                throw new RuntimeException("DerivableType not supported");
        }
    }

    private byte[] getPrivateKeyFromBip44SeedWithChange(byte[] seed) {
        HdAddress masterAddress = hdKeyGenerator.getAddressFromSeed(seed, solanaCoin);
        HdAddress purposeAddress = hdKeyGenerator.getAddress(masterAddress, PURPOSE, solanaCoin.getAlwaysHardened()); // 44H
        HdAddress coinTypeAddress = hdKeyGenerator.getAddress(purposeAddress, TYPE, solanaCoin.getAlwaysHardened()); // 501H
        HdAddress accountAddress = hdKeyGenerator.getAddress(coinTypeAddress, ACCOUNT, solanaCoin.getAlwaysHardened()); //0H
        HdAddress changeAddress = hdKeyGenerator.getAddress(accountAddress, CHANGE, solanaCoin.getAlwaysHardened()); //0H
        return changeAddress.getPrivateKey().getPrivateKey();
    }

    private byte[] getPrivateKeyFromBip44Seed(byte[] seed) {
        HdAddress masterAddress = hdKeyGenerator.getAddressFromSeed(seed, solanaCoin);
        HdAddress purposeAddress = hdKeyGenerator.getAddress(masterAddress, PURPOSE, solanaCoin.getAlwaysHardened()); // 44H
        HdAddress coinTypeAddress = hdKeyGenerator.getAddress(purposeAddress, TYPE, solanaCoin.getAlwaysHardened()); // 501H
        HdAddress accountAddress = hdKeyGenerator.getAddress(coinTypeAddress, ACCOUNT, solanaCoin.getAlwaysHardened()); //0H
        return accountAddress.getPrivateKey().getPrivateKey();
    }
} 