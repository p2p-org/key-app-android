package org.p2p.solanaj.kits.transaction;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.programs.TokenSwapProgram;

import java.util.ArrayList;
import java.util.List;

public class TransactionTypeParser {

    public static List<TransactionDetails> parse(ConfirmedTransactionParsed transaction) {
        List<TransactionDetails> details = new ArrayList<>();

        for (ConfirmedTransactionParsed.InstructionParsed parsedInstruction : transaction.getTransaction().getMessage().getInstructions()) {
            ConfirmedTransactionParsed.Parsed parsedInfo = parsedInstruction.getParsed();
            if (parsedInfo != null) {
                switch (parsedInfo.getType()) {
                    case "transferChecked":
                        details.add(new TransferDetails(parsedInfo.getType(), parsedInfo.getInfo()));
                        break;
                    case "closeAccount":
                        details.add(new CloseAccountDetails(parsedInfo.getInfo()));
                        break;
                    default:
                        details.add(new UnknownDetails(parsedInfo.getInfo()));
                }
            } else {
                if (SwapDetails.KNOWN_SWAP_PROGRAM_IDS.contains(parsedInstruction.gerProgramId())) {
                    byte[] data = Base58.decode(parsedInstruction.getData());

                    int intstructionIndex = data[0];
                    switch (intstructionIndex) {
                        case TokenSwapProgram.INSTRUCTION_INDEX_SWAP:
                            details.add(new SwapDetails(parsedInstruction.getData()));
                            break;

                    }

                } else {
                    details.add(new UnknownDetails(parsedInstruction.getData()));
                }
            }
        }

        return details;
    }
}