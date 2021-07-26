package org.p2p.solanaj.kits.transaction;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.programs.SystemProgram;
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
                        TransferDetails transferDetails = new TransferDetails(
                                signature,
                                transaction.getBlockTime(),
                                transaction.getSlot(),
                                transaction.getMeta().getFee(),
                                parsedInfo.getType(),
                                parsedInfo.getInfo()
                        );
                        details.add(transferDetails);
                        break;
                    case "closeAccount":
                        if (parsedInstruction.gerProgramId().equals(SystemProgram.INSTANCE.getSPL_TOKEN_PROGRAM_ID().toBase58())) {
                            CloseAccountDetails closeDetails = new CloseAccountDetails(
                                    signature, transaction.getBlockTime(), transaction.getSlot(), parsedInfo.getInfo()
                            );
                            details.add(closeDetails);
                        } else {
                            parseSwapDetails(signature, transaction, details, parsedInstruction);
                        }
                        break;
                    default:
                        details.add(new UnknownDetails(signature, transaction.getBlockTime(), transaction.getSlot(), parsedInfo.getInfo()));
                }
            } else {
                parseSwapDetails(signature, transaction, details, parsedInstruction);
            }
        }

        return details;
    }

    private static void parseSwapDetails(
            String signature,
            ConfirmedTransactionParsed transaction,
            List<TransactionDetails> details,
            ConfirmedTransactionParsed.InstructionParsed parsedInstruction
    ) {
        if (SwapDetails.KNOWN_SWAP_PROGRAM_IDS.contains(parsedInstruction.gerProgramId())) {
            byte[] data = Base58.decode(parsedInstruction.getData());

            int intstructionIndex = data[0];
            if (intstructionIndex == TokenSwapProgram.INSTRUCTION_INDEX_SWAP) {
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
                String destination = (String) instructionParsed.get(secondIndex).getParsed().getInfo().get("destination");

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

                SwapDetails swapDetails = new SwapDetails(
                        signature,
                        transaction.getBlockTime(),
                        transaction.getSlot(),
                        transaction.getMeta().getFee(),
                        destination,
                        amountA,
                        amountB,
                        mintA,
                        mintB
                );
                details.add(swapDetails);
            }

        } else {
            details.add(new UnknownDetails(signature, transaction.getBlockTime(), transaction.getSlot(), parsedInstruction.getData()));
        }
    }
}