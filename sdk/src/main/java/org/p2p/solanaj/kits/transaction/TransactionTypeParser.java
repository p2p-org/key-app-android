package org.p2p.solanaj.kits.transaction;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.programs.TokenSwapProgram;

import java.util.ArrayList;
import java.util.List;

public class TransactionTypeParser {

    public static List<TransactionDetails> parse(String signature, ConfirmedTransactionParsed transaction) {
        List<TransactionDetails> details = new ArrayList<>();

        for (ConfirmedTransactionParsed.InstructionParsed parsedInstruction : transaction.getTransaction().getMessage().getInstructions()) {
            ConfirmedTransactionParsed.Parsed parsedInfo = parsedInstruction.getParsed();
            if (parsedInfo != null) {
                switch (parsedInfo.getType()) {
                    case "transferChecked":
                        details.add(new TransferDetails(signature, transaction.getBlockTime(), parsedInfo.getType(),
                                parsedInfo.getInfo()));
                        break;
                    case "closeAccount":
                        details.add(new CloseAccountDetails(signature, transaction.getBlockTime(), parsedInfo.getInfo()));
                        break;
                    default:
                        details.add(new UnknownDetails(signature, transaction.getBlockTime(), parsedInfo.getInfo()));
                }
            } else {
                if (SwapDetails.KNOWN_SWAP_PROGRAM_IDS.contains(parsedInstruction.gerProgramId())) {
                    byte[] data = Base58.decode(parsedInstruction.getData());

                    int intstructionIndex = data[0];
                    switch (intstructionIndex) {
                        case TokenSwapProgram.INSTRUCTION_INDEX_SWAP:
                            List<ConfirmedTransactionParsed.InnerInstruction> innerInstructions = transaction.getMeta().getInnerInstructions();
                            List<ConfirmedTransactionParsed.InstructionParsed> instructionParsed = new ArrayList<>();

                            for (ConfirmedTransactionParsed.InnerInstruction instruction : innerInstructions) {
                                for (ConfirmedTransactionParsed.InstructionParsed ip : instruction.getInstructions()) {
                                    if (ip.getParsed().getType().equals("transfer")) {
                                        instructionParsed.add(ip);
                                    }
                                }
                            }

                            if (instructionParsed.size() != 2) {
                                details.add(new UnknownDetails(signature, transaction.getBlockTime(),
                                        parsedInstruction.getData()));
                                break;
                            }

                            String amountA = (String) instructionParsed.get(0).getParsed().getInfo().get("amount");
                            String userSource = (String) instructionParsed.get(0).getParsed().getInfo().get("source");
                            String amountB = (String) instructionParsed.get(1).getParsed().getInfo().get("amount");
                            String poolDestination = (String) instructionParsed.get(1).getParsed().getInfo()
                                    .get("source");

                            int userSourceKeyIndex = parsedInstruction.getAccounts().indexOf(userSource);
                            int poolDestinationKeyIndex = parsedInstruction.getAccounts().indexOf(poolDestination);

                            List<ConfirmedTransactionParsed.PostTokenBalance> postTokenBalances = transaction.getMeta().getPostTokenBalances();
                            String mintA = "";
                            String mintB = "";

                            for (ConfirmedTransactionParsed.PostTokenBalance balance : postTokenBalances) {
                                if (balance.getAccountIndex() == userSourceKeyIndex) {
                                    mintA = balance.getMint();
                                }
                                if (balance.getAccountIndex() == poolDestinationKeyIndex) {
                                    mintB = balance.getMint();
                                }
                            }

                            details.add(new SwapDetails(signature, transaction.getBlockTime(), amountA, amountB, mintA,
                                    mintB));
                            break;

                    }

                } else {
                    details.add(new UnknownDetails(signature, transaction.getBlockTime(), parsedInstruction.getData()));
                }
            }
        }

        return details;
    }
}