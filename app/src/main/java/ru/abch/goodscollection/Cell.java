package ru.abch.goodscollection;

public class Cell {
	String id, name, descr, zonein, zonein_descr;
	int type, distance, emptySize;
	public Cell (String id, String name, String descr, int type, int distance, String zonein, String zonein_descr, int emptySize) {
		this.id = id;
		this.name = name;
		this.descr = descr;
		this.type = type;
		this.distance = distance;
		this.zonein = zonein;
		this.zonein_descr = zonein_descr;
		this.emptySize = emptySize;
	}
	public int getDistance(int from) {
		int ret = (from > distance)? from - distance : distance - from;
		return ret;
	}
}
