package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transaction {

    public static final int SIGNATURE_LENGTH = 64;
    public static final byte[] DEFAULT_SIGNATURE = new byte[64];

    private final Message message;
    private final List<Signature> signatures;
    private byte[] serializedMessage;
    private PublicKey feePayer;

    public Transaction() {
        this.message = new Message();
        this.signatures = new ArrayList<>();
    }

    public Transaction addInstruction(TransactionInstruction instruction) {
        message.addInstruction(instruction);
        return this;
    }

    public Transaction addInstructions(List<TransactionInstruction> instructions) {
        for (TransactionInstruction instruction : instructions) {
            message.addInstruction(instruction);
        }
        return this;
    }

    public List<TransactionInstruction> getInstructions() {
        return message.getInstructions();
    }

    public void setRecentBlockHash(String recentBlockhash) {
        message.setRecentBlockHash(recentBlockhash);
    }

    public String getRecentBlockHash() {
        return message.getRecentBlockHash();
    }

    public void setFeePayer(PublicKey feePayer) {
        message.setFeePayer(feePayer);
        this.feePayer = feePayer;
    }

    public PublicKey getFeePayer() {
        return feePayer;
    }

    public void sign(Account signer) {
        sign(Arrays.asList(signer));
    }

    public void signWithoutSignatures(PublicKey owner) {

        if (feePayer == null) {
            feePayer = owner;
        }
        message.setFeePayer(feePayer);

        serializedMessage = message.serialize();
    }

    public void sign(List<Account> signers) {

        if (signers.size() == 0) {
            throw new IllegalArgumentException("No signers");
        }

        if (feePayer == null) {
            feePayer = signers.get(0).getPublicKey();
        }
        message.setFeePayer(feePayer);

        serializedMessage = message.serialize();

        for (Account signer : signers) {
            TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getKeypair());
            byte[] signature = signatureProvider.detached(serializedMessage);

            Signature newSignature = new Signature(signer.getPublicKey(), Base58.encode(signature));
            signatures.add(newSignature);
        }
    }

    public List<AccountMeta> getAccountKeys() {
        return message.getAccountKeys();
    }

    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        ByteBuffer out = ByteBuffer
                .allocate(signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length);

        out.put(signaturesLength);

        for (Signature signature : signatures) {
            if (signature.getSignature() != null) {
                byte[] rawSignature = Base58.decode(signature.getSignature());
                out.put(rawSignature);
            }
        }

        out.put(serializedMessage);

        return out.array();
    }

    public Signature getSignature() {
        if (signatures.size() > 0) {
            return signatures.get(0);
        }

        return null;
    }

    public Signature findSignature(PublicKey publicKey) {
        for (Signature signature : signatures) {
            if (signature.getPublicKey().equals(publicKey)) {
                return signature;
            }
        }

        return null;
    }

    public List<Signature> getAllSignatures() {
        return signatures;
    }

    public BigInteger calculateTransactionFee(BigInteger lamportsPerSignatures) {
        message.serialize();
        return BigInteger.valueOf((long) message.getNumRequiredSignatures()).multiply(lamportsPerSignatures);
    }
}
