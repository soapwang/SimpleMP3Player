package com.soapwang.simplemp3player;

public class TrackInfo {
	private String fileName;
	private String title;
	private String artist;
	private String album;
	private String path;
	private int duration;
	
	public TrackInfo(String title, String artist, String album, String path, int duration) {
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.path = path;		
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getTitle() {
		return title;
	}
			
	public String getArtist() {
		return artist;
	}
	
	public String getAlbum() {
		return album;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getDuration() {
		return duration;
	}

}
