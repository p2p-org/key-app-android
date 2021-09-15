package org.p2p.solanaj.kits.renBridge;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Sysvar;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Secp256k1Program;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class RenProgram {
    private static final int ETHEREUM_ADDRESS_BYTES = 20;
    private static final int SIGNATURE_OFFSETS_SERIALIZED_SIZE = 11;
    private static final int SECP256K1_INSTRUCTION_SIZE = 98;

    public static TransactionInstruction mintInstruction(PublicKey account, PublicKey gatewayAccount,
                                                         PublicKey tokenMint, PublicKey recipientTokenAccount, PublicKey mintLogAccount, PublicKey mintAuthority,
                                                         PublicKey programId) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, true, false));
        keys.add(new AccountMeta(gatewayAccount, false, false));
        keys.add(new AccountMeta(tokenMint, false, true));
        keys.add(new AccountMeta(recipientTokenAccount, false, true));
        keys.add(new AccountMeta(mintLogAccount, false, true));
        keys.add(new AccountMeta(mintAuthority, false, false));
        keys.add(new AccountMeta(SystemProgram.INSTANCE.getPROGRAM_ID(), false, false));
        keys.add(new AccountMeta(Sysvar.SYSVAR_INSTRUCTIONS_ADDRESS, false, false));
        keys.add(new AccountMeta(Sysvar.SYSVAR_RENT_ADDRESS, false, false));
        keys.add(new AccountMeta(TokenProgram.PROGRAM_ID, false, false));

        byte[] data = new byte[]{1};

        return new TransactionInstruction(programId, keys, data);
    }

    public static TransactionInstruction createInstructionWithEthAddress2(byte[] ethAddress, byte[] message,
                                                                          byte[] signature, int recoveryID) {
        int dataStart = 1 + SIGNATURE_OFFSETS_SERIALIZED_SIZE;
        int ethAddressOffset = dataStart + 1;
        int signatureOffset = ethAddressOffset + ETHEREUM_ADDRESS_BYTES;
        int messageDataOffset = signatureOffset + signature.length + 1;
        int numSignatures = 1;

        ByteBuffer buffer = ByteBuffer.allocate(SECP256K1_INSTRUCTION_SIZE + message.length);
        buffer.put((byte) numSignatures);
        buffer.put(ByteUtils.uint16ToByteArrayLE(signatureOffset));
        buffer.put((byte) 1);
        buffer.put(ByteUtils.uint16ToByteArrayLE(ethAddressOffset));
        buffer.put((byte) 1);
        buffer.put(ByteUtils.uint16ToByteArrayLE(messageDataOffset));
        buffer.put(ByteUtils.uint16ToByteArrayLE(message.length));
        buffer.put((byte) 1);
        buffer.put((byte) 0);
        buffer.put(ethAddress);
        buffer.put(signature);
        buffer.put((byte) recoveryID);

        buffer.put(message);

        return new TransactionInstruction(Secp256k1Program.PROGRAM_ID, new ArrayList<AccountMeta>(), buffer.array());
    }

    public static TransactionInstruction burnInstruction(PublicKey account, PublicKey source, PublicKey gatewayAccount,
                                                         PublicKey tokenMint, PublicKey burnLogAccountId, byte[] recepient, PublicKey programId) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, true, false));
        keys.add(new AccountMeta(source, false, true));
        keys.add(new AccountMeta(gatewayAccount, false, true));
        keys.add(new AccountMeta(tokenMint, false, true));
        keys.add(new AccountMeta(burnLogAccountId, false, true));
        keys.add(new AccountMeta(SystemProgram.INSTANCE.getPROGRAM_ID(), false, false));
        keys.add(new AccountMeta(Sysvar.SYSVAR_INSTRUCTIONS_ADDRESS, false, false));
        keys.add(new AccountMeta(Sysvar.SYSVAR_RENT_ADDRESS, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + recepient.length);
        buffer.put((byte) 2);
        buffer.put((byte) recepient.length);
        buffer.put(recepient);

        return new TransactionInstruction(programId, keys, buffer.array());
    }

}