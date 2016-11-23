package burp;

import java.util.*;
import java.io.*;
import java.net.*;

import org.msgpack.core.*;

public class ClientHandler extends Thread {
	private final Socket s;
	private final IBurpCollaboratorClientContext ccc;
	private final IExtensionHelpers helpers;

	public ClientHandler(Socket s, IBurpCollaboratorClientContext ccc,
			IExtensionHelpers helpers) {
		this.s = s;
		this.ccc = ccc;
		this.helpers = helpers;
	}

	@Override
	public void run() {
		try {
			InputStream is = s.getInputStream();
			OutputStream os = s.getOutputStream();
			while (true) {
				int cmd = is.read();
				System.err.println("cmd = " + cmd);
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

	private void handleQuery(OutputStream os, String payload) throws IOException {
		final List<IBurpCollaboratorInteraction> bci =
			ccc.fetchCollaboratorInteractionsFor(payload);
		final MessagePacker mbp = MessagePack.newDefaultPacker(os);
		mbp.packArrayHeader(bci.size());
		for (IBurpCollaboratorInteraction interaction : bci) {
			final Map<String, String> props = interaction.getProperties();
			mbp.packMapHeader(props.size() - (props.containsKey("interaction_id") ? 1 : 0));
			for (final Map.Entry<String, String> entry : props.entrySet()) {
				final String k = entry.getKey();
				if (k.equals("interaction_id")) continue;
				mbp.packString(k);
				final String v = entry.getValue();
				if (k.equals("request") || k.equals("response") || k.equals("raw_query")) {
					final byte[] buf = helpers.base64Decode(v);
					mbp.packBinaryHeader(buf.length);
					mbp.writePayload(buf);
				} else if (k.equals("client_ip")) {
					final byte[] buf = InetAddress.getByName(v).getAddress();
					mbp.packBinaryHeader(buf.length);
					mbp.writePayload(buf);
				} else {
					mbp.packString(v);
				}
			}
		}
		mbp.close();
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
