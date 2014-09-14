package com.ggstudios.lolcraft;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CraftSummaryFragment extends SherlockFragment {
	private Build build;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		build = StateManager.getInstance().getActiveBuild();
		
		View rootView = inflater.inflate(R.layout.dialog_fragment_stat_summary, container, false);
		
		return rootView;
	}
}
