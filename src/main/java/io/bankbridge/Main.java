package io.bankbridge;

import static spark.Spark.port;

import io.bankbridge.integration.BanksController;


public class Main {

	public static void main(String[] args) throws Exception {

		//Added Option to pass the running port for Integration Test
		int running_port = 8080;
		if (args != null && args.length > 0) {
			running_port = Integer.parseInt(args[0]);
		}
		port(running_port);

		//Created BanksController Purpose is to add filter and all specific to Banks details.
		//In future if accounts or something comes up they will have different filter mechanisms and steps to follow.
		new BanksController();
	}
}