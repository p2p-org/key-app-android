package org.p2p.solanaj.kits.transaction;

import android.util.Log;

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
                    case "transfer":
                    case "transferChecked":
                        details.add(new TransferDetails(signature, transaction.getBlockTime(), parsedInfo.getType(),
                                parsedInfo.getInfo()));
                        break;
                    case "closeAccount":
                        parseSwapDetails(signature, transaction, details, parsedInstruction);
                        break;
                    default:
                        details.add(new UnknownDetails(signature, transaction.getBlockTime(), parsedInfo.getInfo()));
                }
            } else {
                parseSwapDetails(signature, transaction, details, parsedInstruction);
            }
        }

        return details;
    }

    private static void parseSwapDetails(String signature, ConfirmedTransactionParsed transaction, List<TransactionDetails> details, ConfirmedTransactionParsed.InstructionParsed parsedInstruction) {
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

                    int firstIndex = 0;
                    int secondIndex = 1;
                    if (instructionParsed.size() > 2) {
                        firstIndex += 1;
                        secondIndex += 1;
                    }

                    String amountA = (String) instructionParsed.get(firstIndex).getParsed().getInfo().get("amount");
                    String userSource = (String) instructionParsed.get(firstIndex).getParsed().getInfo().get("source");
                    String amountB = (String) instructionParsed.get(secondIndex).getParsed().getInfo().get("amount");
                    String poolDestination = (String) instructionParsed.get(secondIndex).getParsed().getInfo().get("source");

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