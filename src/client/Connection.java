package client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import packets.Packet;
import packets.TextPacket;

public abstract class Connection {
	protected OutputStream out;
	protected InputStream in;

	Queue<TextPacket> output = new LinkedList<TextPacket>();

	ConnectionReader readThread;
	ConnectionWriter writeThread;

	public void run() {
		readThread = new ConnectionReader();
		writeThread = new ConnectionWriter();

		readThread.start();
		writeThread.start();

		try {
			writeThread.join();
		} catch (InterruptedException e) {
		}

		readThread.quit();
	}

	public void writePacket(Packet packet) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataout = new DataOutputStream(baos);
		packet.writeToOutput(dataout);
		byte[] packetBytes = baos.toByteArray();
		out.write(packetBytes);
		out.flush();
	}

	abstract public void connect(String ip, int port);

	public void send(String text) {
		Client.log(">>", text);

		TextPacket packet = new TextPacket();
		packet.text = text + "\r\n";
		synchronized (output) {
			output.add(packet);
		}
	}

	public void sendPrivate(String user, String text) {
		send(String.format("PRIVMSG %s :%s", user, text));
	}

	public void quit() {
		send("QUIT Connection closed");
		writeThread.quit();
	}

	class ConnectionReader extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				try {
					int bytesAvailable = in.available();
					if (bytesAvailable > 0) {
						byte[] packetBytes = new byte[bytesAvailable];
						in.read(packetBytes, 0, packetBytes.length);
						TextPacket textPacket = new TextPacket();
						textPacket.parseFromInput(new DataInputStream(
								new ByteArrayInputStream(packetBytes)));
						Client.getClient().respond(textPacket);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void quit() {
			this.run = false;
		}
	}

	class ConnectionWriter extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				try {
					synchronized (output) {
						while (output.size() > 0) {
							writePacket(output.remove());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void quit() {
			this.run = false;
		}
	}
}