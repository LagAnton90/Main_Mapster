package server;

/**
 * Room class that stores information about a room
 * Created by Gustav on 2016-04-08.
 */
public class Room {

	private int floor;
	private String name, coor, path;

	public Room(String name, int floor, String coor, String path) {
		this.floor = floor;
		this.name = name;
		this.coor = coor;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public int getFloor() {
		return floor;
	}

	public String getCoor() {
		return coor;
	}

	public String getPath() {
		return path;
	}
}