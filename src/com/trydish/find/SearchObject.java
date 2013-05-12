package com.trydish.find;

public class SearchObject {
	private final int id;
	private final String name;
	
	public SearchObject(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
}
