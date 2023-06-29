package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.core.crypto.Base64Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message {
    private static final int RECENT_BLOCK_HASH_LENGTH = 32;

    private MessageHeader messageHeader;
    private String recentBlockhash;
    private final AccountKeysList accountKeys;
    private final List<TransactionInstruction> instructions;
    private PublicKey feePayer;
    private final List<String> programIds;

    public Message(
            MessageHeader header,
            AccountKeysList accountKeys,
            String recentBlockhash,
            List<CompiledInstruction> instructions
    ) {
        this.programIds = new ArrayList<String>();
        this.messageHeader = header;
        this.accountKeys = accountKeys;
        this.instructions = mapToInternalInstructions(accountKeys.getList(), instructions);
        this.recentBlockhash = recentBlockhash;
    }

    public Message() {
        this.programIds = new ArrayList<String>();
        this.accountKeys = new AccountKeysList();
        this.instructions = new ArrayList<TransactionInstruction>();
    }

    public Message addInstruction(TransactionInstruction instruction) {
        accountKeys.addAll(instruction.getKeys());
        instructions.add(instruction);

        if (!programIds.contains(instruction.getProgramId().toBase58())) {
            programIds.add(instruction.getProgramId().toBase58());
        }

        return this;
    }

    public List<TransactionInstruction> getInstructions() {
        return instructions;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        this.recentBlockhash = recentBlockhash;
    }

    public String getRecentBlockHash() {
        return recentBlockhash;
    }

    public byte[] serialize() {

        if (recentBlockhash == null) {
            throw new IllegalArgumentException("recentBlockhash required");
        }

        if (instructions.size() == 0) {
            throw new IllegalArgumentException("No instructions provided");
        }

        if (feePayer == null) {
            throw new IllegalArgumentException("Fee payer not found");
        }

        messageHeader = new MessageHeader();

        for (String programId : programIds) {
            accountKeys.add(new AccountMeta(new PublicKey(programId), false, false));
        }
        List<AccountMeta> keysList = getAccountKeys();
        int accountKeysSize = keysList.size();

        byte[] accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize);

        int compiledInstructionsLength = 0;
        List<CompiledInstruction> compiledInstructions = new ArrayList<CompiledInstruction>();

        for (TransactionInstruction instruction : instructions) {
            int keysSize = instruction.getKeys().size();

            byte[] keyIndices = new byte[keysSize];
            for (int i = 0; i < keysSize; i++) {
                keyIndices[i] = (byte) AccountMeta.Companion.findAccountIndex(
                        keysList,
                        instruction.getKeys().get(i).getPublicKey()
                );
            }

            CompiledInstruction compiledInstruction = new CompiledInstruction();
            compiledInstruction.programIdIndex = (byte) AccountMeta.Companion.findAccountIndex(
                    keysList,
                    instruction.getProgramId()
            );
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
            compiledInstruction.keyIndices = keyIndices;
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.getData().length);
            compiledInstruction.data = instruction.getData();

            compiledInstructions.add(compiledInstruction);

            compiledInstructionsLength += compiledInstruction.getLength();
        }

        byte[] instructionsLength = ShortvecEncoding.encodeLength(compiledInstructions.size());

        int bufferSize = MessageHeader.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.length
                + (accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH) + instructionsLength.length
                + compiledInstructionsLength;

        ByteBuffer out = ByteBuffer.allocate(bufferSize);

        ByteBuffer accountKeysBuff = ByteBuffer.allocate(accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH);
        for (AccountMeta accountMeta : keysList) {
            accountKeysBuff.put(accountMeta.getPublicKey().asByteArray());

            if (accountMeta.isSigner()) {
                messageHeader.numRequiredSignatures += 1;
                if (!accountMeta.isWritable()) {
                    messageHeader.numReadonlySignedAccounts += 1;
                }
            } else {
                if (!accountMeta.isWritable()) {
                    messageHeader.numReadonlyUnsignedAccounts += 1;
                }
            }
        }

        out.put(messageHeader.toByteArray());

        out.put(accountAddressesLength);
        out.put(accountKeysBuff.array());

        out.put(Base58.decode(recentBlockhash));

        out.put(instructionsLength);
        for (CompiledInstruction compiledInstruction : compiledInstructions) {
            out.put(compiledInstruction.programIdIndex);
            out.put(compiledInstruction.keyIndicesCount);
            out.put(compiledInstruction.keyIndices);
            out.put(compiledInstruction.dataLength);
            out.put(compiledInstruction.data);
        }

        return out.array();
    }

    public static Message deserialize(byte[] messageInBytes) {
        int cursor = 0;
        byte numRequiredSignatures = messageInBytes[cursor++];
        byte numReadonlySignedAccounts = messageInBytes[cursor++];
        byte numReadonlyUnsignedAccounts = messageInBytes[cursor++];

        List<PublicKey> accountKeys = new ArrayList<>();

        byte[] updatedByteArray = Arrays.copyOfRange(messageInBytes, cursor, messageInBytes.length);
        int accountKeysCount = ShortvecEncoding.decodeLength(updatedByteArray);

        for (int i = 0; i < accountKeysCount; i++) {
            byte[] account = Arrays.copyOfRange(updatedByteArray, 0, PublicKey.PUBLIC_KEY_LENGTH);
            updatedByteArray = Arrays.copyOfRange(updatedByteArray, PublicKey.PUBLIC_KEY_LENGTH, updatedByteArray.length);
            accountKeys.add(new PublicKey(account));
        }

        byte[] recentBlockhashBytes = Arrays.copyOfRange(updatedByteArray, 0, PublicKey.PUBLIC_KEY_LENGTH);
        updatedByteArray = Arrays.copyOfRange(updatedByteArray, PublicKey.PUBLIC_KEY_LENGTH, updatedByteArray.length);

        int instructionsCount = ShortvecEncoding.decodeLength(updatedByteArray);
        List<CompiledInstruction> instructions = new ArrayList<>();

        for (int i = 0; i < instructionsCount; i++) {
            byte programIndex = updatedByteArray[0];

            int accountCount = ShortvecEncoding.decodeLength(updatedByteArray);
            byte[] accounts = Arrays.copyOfRange(updatedByteArray, 0, accountCount);
            updatedByteArray = Arrays.copyOfRange(updatedByteArray, accountCount, updatedByteArray.length);

            int dataLength = ShortvecEncoding.decodeLength(updatedByteArray);
            byte[] dataSlice = Arrays.copyOfRange(updatedByteArray, 0, dataLength);
            updatedByteArray = Arrays.copyOfRange(updatedByteArray, dataLength, updatedByteArray.length);

            CompiledInstruction compiledInstruction = new CompiledInstruction();
            compiledInstruction.programIdIndex = programIndex;
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(accountCount);
            compiledInstruction.keyIndices = accounts;
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(dataLength);
            compiledInstruction.data = dataSlice;
            instructions.add(compiledInstruction);
        }

        MessageHeader header = new MessageHeader(
                numRequiredSignatures,
                numReadonlySignedAccounts,
                numReadonlyUnsignedAccounts
        );

        List<AccountMeta> accountMetas = new ArrayList<>();
        for (int i = 0; i < accountKeys.size(); i++) {
            AccountMeta account = new AccountMeta(accountKeys.get(i), false, false);
            accountMetas.add(account);
        }
        AccountKeysList keysList = new AccountKeysList();
        keysList.addAll(accountMetas);
        return new Message(header, keysList, Base64Utils.INSTANCE.encode(recentBlockhashBytes), instructions);
    }

    private List<TransactionInstruction> mapToInternalInstructions(
            List<AccountMeta> accountKeys,
            List<CompiledInstruction> compiledInstructions
    ) {
        List<TransactionInstruction> instructions = new ArrayList<>();

        for (int i = 0; i < compiledInstructions.size(); i++) {
            CompiledInstruction compiledInstruction = compiledInstructions.get(i);
            int programIdIndex = compiledInstruction.programIdIndex;
            PublicKey programId = accountKeys.get(programIdIndex).getPublicKey();
            TransactionInstruction instruction = new TransactionInstruction(programId, accountKeys, compiledInstruction.data);
            instructions.add(instruction);
        }

        return instructions;
    }

    protected void setFeePayer(PublicKey feePayer) {
        this.feePayer = feePayer;
    }

    public boolean isAccountSigner(int index) {
        return index < messageHeader.numRequiredSignatures;
    }

    public boolean isAccountWritable(int index) {
        byte numRequiredSignatures = messageHeader.numRequiredSignatures;
        int size = accountKeys.size();
        return (index < numRequiredSignatures - messageHeader.numReadonlySignedAccounts) ||
                (index >= numRequiredSignatures && index < size - messageHeader.numReadonlyUnsignedAccounts);
    }

    public int getNumRequiredSignatures() {
        return messageHeader.numRequiredSignatures;
    }

    public List<AccountMeta> getAccountKeys() {
        List<AccountMeta> keysList = accountKeys.getList();
        int feePayerIndex = AccountMeta.Companion.findAccountIndex(keysList, feePayer);

        List<AccountMeta> newList = new ArrayList<AccountMeta>();

        if (feePayerIndex != -1) {
            AccountMeta feePayerMeta = keysList.get(feePayerIndex);
            newList.add(new AccountMeta(feePayerMeta.getPublicKey(), true, true));
            keysList.remove(feePayerIndex);
        } else {
            newList.add(new AccountMeta(feePayer, true, true));
        }
        newList.addAll(keysList);

        return newList;
    }

    private static class MessageHeader {

        static final int HEADER_LENGTH = 3;

        byte numRequiredSignatures = 0;
        byte numReadonlySignedAccounts = 0;
        byte numReadonlyUnsignedAccounts = 0;

        public MessageHeader() {

        }

        public MessageHeader(
                byte numRequiredSignatures,
                byte numReadonlySignedAccounts,
                byte numReadonlyUnsignedAccounts
        ) {
            this.numRequiredSignatures = numRequiredSignatures;
            this.numReadonlySignedAccounts = numReadonlySignedAccounts;
            this.numReadonlyUnsignedAccounts = numReadonlyUnsignedAccounts;
        }

        byte[] toByteArray() {
            return new byte[]{numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts};
        }
    }

    private static class CompiledInstruction {
        byte programIdIndex;
        byte[] keyIndicesCount;
        byte[] keyIndices;
        byte[] dataLength;
        byte[] data;

        int getLength() {
            // 1 = programIdIndex length
            return 1 + keyIndicesCount.length + keyIndices.length + dataLength.length + data.length;
        }
    }
}
