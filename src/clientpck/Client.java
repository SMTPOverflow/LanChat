package clientpck;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import util.Debugger;
import util.EncryptedString;
import util.SerializableMessage;

/*
 * Ein Client kann sich mit dem Server schicken und mit anderen Clients (�ber den Server) kommunizieren
 */
public class Client {
	/*
	 * Der Socket des Clients, mit dem er mit dem Server kommuniziert
	 */
	private Socket localClientSocket;

	/*
	 * Der Client wartet auf eventuelle Broadcasts vom Server. Diese Broadcasts sind
	 * in den meisten F�llen die Nachrichten von anderen Clients.
	 */
	private void listenForServerBroadcast() {
		new Thread(new Runnable() { // Startet eine neuen Thread, damit der Client parallel weiterlaufen kann.
			@Override
			public void run() {
				ObjectInputStream ois = null; // Es wird ein ObjectInputStream vom lokalen Socket erzeugt.
				try {
					ois = new ObjectInputStream(localClientSocket.getInputStream());
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					int sleepTimeInS = 2;
					SerializableMessage message = null;
					do {
						try {
							message = (SerializableMessage) ois.readObject();
						} catch (SocketException e) {
							Debugger.println(0, "LOCAL", new Date().toString(),
									"Server unreachable. Trying again in " + sleepTimeInS + " sec");
							Thread.sleep(sleepTimeInS * 1000);
							sleepTimeInS *= 2;
						}
					} while (message == null);
					Debugger.println(0, "REMOTE", new Date().toString(), message.getMessage().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/*
	 * Sendet eine Nachricht an den Server, welcher sie an die anderen Clients
	 * weiterleitet. Eine Nachricht kann entweder ein Beliebiges Objekt sein, muss
	 * aber als SerializableMassage �bergeben werden (um zus�tzliche Metadaten und
	 * Informationen anzuh�ngen)
	 */
	public void sendMessage(SerializableMessage message) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(localClientSocket.getOutputStream());
			oos.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Konstruktor, um eine Client-Instanz zu erzeugen. Es muss ein Socket �bergeben
	 * werden. Beim aufrufen des Konstruktors wird sofort auf Broadcasts von Server
	 * gewartet
	 */
	public Client(Socket localClientSocket) {
		this.localClientSocket = localClientSocket;
		this.listenForServerBroadcast();
	}

	/*
	 * Test-Klasse
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Client client = new Client(new Socket("127.0.0.1", 8888));
		client.sendMessage(new SerializableMessage(new EncryptedString("lol")));
		Thread.sleep(5000);
	}
}