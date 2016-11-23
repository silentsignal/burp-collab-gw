package burp;

import java.util.*;
import java.io.*;
import java.net.*;

import org.msgpack.core.*;
import org.msgpack.value.*;

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
			OutputStream os = s.getOutputStream();
			final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(s.getInputStream());
			while (unpacker.hasNext()) {
				final Value v = unpacker.unpackValue();
				final MessagePacker mbp = MessagePack.newDefaultPacker(os);
				switch (v.getValueType()) {
					case INTEGER:
						switch (v.asIntegerValue().toInt()) {
							case 0: mbp.packString(ccc.generatePayload(false)); break;
							case 1: mbp.packString(ccc.generatePayload(true));  break;
							case 2: mbp.packString(ccc.getCollaboratorServerLocation()); break;
						}
						mbp.flush();
						break;
					case STRING:
						handleQuery(mbp, v.asStringValue().asString());
						break;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void handleQuery(MessagePacker mbp, String payload) throws IOException {
		final List<IBurpCollaboratorInteraction> bci =
			ccc.fetchCollaboratorInteractionsFor(payload);
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
		mbp.flush();
	}
}
