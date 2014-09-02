package com.ggstudios.lolcraft;

import android.content.Context;

public class LibraryManager {
	private static LibraryManager instance;
	
	private ChampionLibrary championLibrary;
	private ItemLibrary itemLibrary;
	private Context context;
	
	public static void initInstance(Context context) {
		instance = new LibraryManager(context);
	}
	
	public static LibraryManager getInstance() {
		return instance;
	}
	
	private LibraryManager(Context context){
		championLibrary = new ChampionLibrary(context);
		itemLibrary = new ItemLibrary();
		
		this.context = context;
	}
	
	public ChampionLibrary getChampionLibrary() {
		return championLibrary;
	}
	
	public ItemLibrary getItemLibrary() {
		return itemLibrary;
	}
	
}
