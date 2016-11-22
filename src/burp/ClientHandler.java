package burp;

import java.util.*;
import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
	private final Socket s;
	private final IBurpCollaboratorClientContext ccc;

	public ClientHandler(Socket s, IBurpCollaboratorClientContext ccc) {
		this.s = s;
		this.ccc = ccc;
	}

	@Override
	public void run() {
		try {
			InputStream is = s.getInputStream();
			OutputStream os = s.getOutputStream();
			while (true) {
				int cmd = is.read();
				switch (cmd) {
					case 0:
						sendPayload(os);
						break;
					case -1:
						return;
					default:
						handleQuery(os, recvPayload(is, cmd));
						break;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void sendPayload(OutputStream os) throws IOException {
		String s = ccc.generatePayload(true);
		os.write(s.length());
		os.write(s.getBytes());
	}

/*
 * un-base64: request, response, raw_query
 * un-ip: client_ip
 * remove: interaction_id
 */

	private void handleQuery(OutputStream os, String payload) throws IOException {
		final List<IBurpCollaboratorInteraction> bci =
			ccc.fetchCollaboratorInteractionsFor(payload);
		os.write(bci.size());
		for (IBurpCollaboratorInteraction i : bci) {
			System.out.println("---bci---");
			for (Map.Entry<String, String> e : i.getProperties().entrySet()) {
				System.out.println(e.getKey() + " => " + e.getValue());
			}
		}
		System.out.println("---eof---");
	}

	private String recvPayload(InputStream is, int length) throws IOException {
		byte[] buf = new byte[length];
		int pos = 0;
		do {
			int res = is.read(buf, pos, length - pos);
			if (res == -1) return null;
			pos += res;
		} while (pos < length);
		return new String(buf);
	}
}
