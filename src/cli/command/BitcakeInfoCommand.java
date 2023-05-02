package cli.command;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;

public class BitcakeInfoCommand implements CLICommand {

	private SnapshotCollector collector;
	
	public BitcakeInfoCommand(SnapshotCollector collector) {
		this.collector = collector;
	}
	
	@Override
	public String commandName() {
		return "bitcake_info";
	}

	@Override
	public void execute(String args) {//todo
		if(!collector.isCollecting())
			collector.startCollecting();
		else
			AppConfig.timestampedErrorPrint("Already collecting");

	}

}
