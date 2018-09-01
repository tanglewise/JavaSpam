package twise;

import jota.IotaAPI;
import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.GetAttachToTangleResponse;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.dto.response.SendTransferResponse;
import jota.error.ArgumentException;
import jota.model.Input;
import jota.model.Transaction;
import jota.model.Transfer;
import jota.pow.ICurl;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;

public class Main {

	public static final ThreadGroup SUPER_THREAD = new ThreadGroup( "Super-Thread" );

    public static void main(String[] args) {
    	try {
		spamTransactions();
	} catch (ArgumentException e) {
		System.out.println(e.getMessage());
	}
    }

    public static void spamTransactions() throws ArgumentException {
    	GoLocalPow local_pow_manager = new GoLocalPow();
    	local_pow_manager.init();
		IotaAPI api = new IotaAPI.Builder()
				.protocol("http")
				.host("104.154.209.111")
				.port("14265")
				.localPoW(local_pow_manager)
				.build();
		GetNodeInfoResponse response = api.getNodeInfo();
		System.out.println(response.toString());

		List<Transfer> transfers = new ArrayList<>();
		String message = "";
		String address = "999999999999999999999999999999999999999999999999999999999999999999999999999999999";
		String tag = "999999999999999999999999999";
		String seed = "SSFM9SBA9GIREICDJU9FVUAKZUU9KLXUIFIYMAJQHUKCKRXWFJKDJG9DCHSOIEGH9";
		int security_level = 1;
		int depth = 4;
		int min_weight_magnitude = 14;
		transfers.add(new Transfer(address, 0, StringUtils.rightPad(message, 2187, '9'), tag));

		System.out.println("Starting spam...");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long start = timestamp.getTime();
		int num_transactions = 0;
		while (true) {
			try {
				SendTransferResponse transfer_response = api.sendTransfer(seed, security_level, depth, min_weight_magnitude, transfers, null, null, false, false);
				if (transfer_response.getSuccessfully()[0]) {
					System.out.println(transfer_response.getTransactions().get(0).getHash() + " attached at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
					num_transactions++;
					Timestamp current_timestamp = new Timestamp(System.currentTimeMillis());
					long current_time = current_timestamp.getTime();
					double elapsed_time = (double) (current_time - start) / 1000;
					double tps = (double) num_transactions / elapsed_time;
					System.out.println("Time elapsed: " + String.valueOf(elapsed_time) + ", number of transactions: " + String.valueOf(num_transactions) + ", TPS: " + String.valueOf(tps));
				} else {
					System.out.println("Transfer failed ! " + transfer_response.toString());
				}
			} catch (ArgumentException e) {
				System.out.println("Error sending tx: " + e.getMessage());
			}
		}
    }
}
