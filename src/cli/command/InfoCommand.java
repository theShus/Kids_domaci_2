package cli.command;

import app.AppConfig;
import app.CausalBroadcastShared;

public class InfoCommand implements CLICommand {

    @Override
    public String commandName() {
        return "info";
    }

    @Override
    public void execute(String args) {
//        AppConfig.timestampedStandardPrint("My info: " + AppConfig.myServentInfo);
//        AppConfig.timestampedStandardPrint("Neighbors:");
//        String neighbors = "";
//        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
//            neighbors += neighbor + " ";
//        }
//
//        AppConfig.timestampedStandardPrint(neighbors);
//
        AppConfig.timestampedStandardPrint("CLOCK: " + CausalBroadcastShared.getVectorClock().toString());
        AppConfig.timestampedStandardPrint("BITCAKES: " + CausalBroadcastShared.getSnapshotCollector().getBitcakeManager().getCurrentBitcakeAmount());

    }

}
