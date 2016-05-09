package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Starts a server that receives a room and returns an image (map)
 * Created by Gustav on 2016-04-05.
 */
public class Server implements Runnable {
	private ServerSocket serverSocket;
	private Thread serverThread;
	private Connect connect = new Connect();
	private FileFunctions fh = new FileFunctions();
	private ArrayList<String> recievedBuildings = new ArrayList<String>();


	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
			serverThread = new Thread(this);
			serverThread.start();
		} catch (IOException e) {
		}
	}

	/**
	 * Creates a new thread (ClientListener) for each accepted connection made
	 */
	public void run() {
		System.out.println("Server started...");

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("Client connected! [" + socket.getInetAddress().getHostName() + "]");
				new ClientListener(socket).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * A tread that listens for requests without closing the connection between
	 * requests.
	 */
	private class ClientListener extends Thread {
		private Socket socket;
		private ObjectOutputStream outputStream;
        private DataInputStream inputStream;
		boolean fileExist = true;
		private Room room;
		private String roomString = "", buildingString = "";

		public ClientListener(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Receives room-string from android client, search room via connect class and gets info
		 * from database, returns the path of the file stored on the server.
		 */
		public void run() {
			try {
				System.out.println();
				inputStream = new DataInputStream(socket.getInputStream());
				outputStream = new ObjectOutputStream(socket.getOutputStream());
                String request = inputStream.readUTF();

				if(request.charAt(0) == '&' ) {
					sendCoor(request);
				} else if(request.charAt(0) == '#') {
					connect.whichBuilding(request);


					recievedBuildings = connect.getDistinctFloors();
					int howManyFloors = recievedBuildings.size();
					System.out.println(howManyFloors);
					String afloor = recievedBuildings.get(0);
					System.out.println(afloor);
					outputStream.writeInt(howManyFloors);
					outputStream.flush();
					System.out.print("howManyFloors skickat: " + howManyFloors);

					for (int i = 0; i < recievedBuildings.size(); i++) {
						fh.sendFile(outputStream, recievedBuildings.get(i));
					}
					outputStream.writeObject(connect.getHashMap()); //HASHMAP GETS SENT HERE
					outputStream.flush();
				} else {
					sendMapAndCoor(request);
				}

                outputStream.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Sends both the map and coordinates to the client
		 * @param str
         */
		private void sendMapAndCoor (String str) {
			try {
				System.out.println("Client doesn't have map, sending map and coordinates.");
				fh.splitRoom(str);
				roomString = fh.getRoomString();
				buildingString = fh.getBuildingString();

				System.out.println("Input from client: " + roomString + " " + buildingString);

				fileExist = connect.searchExist(roomString, buildingString);
//				System.out.println("Connectmethod is " + fileExist);

				if(fileExist) {
					System.out.println("Room exists!");
					room = connect.searchedRoom(roomString, buildingString);
					String strImage = room.getPath();

					outputStream.writeBoolean(true);
					System.out.println("Sent boolean");

					//Send file to clientx
					fh.sendFile(outputStream, strImage);
					System.out.println("Picture sent to " + socket.getInetAddress() + "!" + " [" + strImage + "]");

					//Get coordinates
					fh.splitCoor(room.getCoor());
					System.out.println("X: " + fh.getX() + " Y: " + fh.getY());
					outputStream.writeInt(fh.getX());
					outputStream.writeInt(fh.getY());
				} else {
					System.out.println("Room doesn't exist!");
					outputStream.writeBoolean(fileExist);
					System.out.println("Sent boolean");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Sends only the coordinates to the client
		 * @param str
         */
		public void sendCoor (String str) {
			try {
				System.out.println("Client already has map, sending coordinates only");
				fh.splitRoom(str);
				roomString = fh.getRoomString();
				buildingString = fh.getBuildingString();

				System.out.println("Input from client: " + roomString + " " + buildingString);

				fileExist = connect.searchExist(roomString, buildingString);

				if(fileExist) {
					System.out.println("Room exists!");
					room = connect.searchedRoom(roomString, buildingString);

					outputStream.writeBoolean(true);
					System.out.println("Sent boolean");

					//Get coordinates
					fh.splitCoor(room.getCoor());
					System.out.println("X: " + fh.getX() + " Y: " + fh.getY());
					outputStream.writeInt(fh.getX());
					outputStream.writeInt(fh.getY());
				} else {
					System.out.println("Room doesn't exist!");
					outputStream.writeBoolean(fileExist);
					System.out.println("Sent boolean");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}