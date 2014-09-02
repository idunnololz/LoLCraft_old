package com.ggstudios.lolcraft;

public class StateManager {

	private static StateManager instance;
	
	private Build activeBuild;
	
	private StateManager() {
		
	}
	
	public static void initInstance() {
		instance = new StateManager();
	}
	
	public static StateManager getInstance() {
		return instance;
	}
	
	public Build getActiveBuild() {
		return activeBuild;
	}
	
	public void setActiveBuild(Build build) {
		activeBuild = build;
	}
	
	
}
