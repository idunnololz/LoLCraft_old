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
	private ListView statList;
	
	private Build build;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		build = StateManager.getInstance().getActiveBuild();
		
		View rootView = inflater.inflate(R.layout.fragment_craft_summary, container, false);
		
		statList = (ListView) rootView.findViewById(R.id.statList);
		
		statList.setAdapter(new StatAdapter(getActivity(), build.getRawStats()));
		
		return rootView;
	}
	
	private static class ViewHolder {
		TextView txtKey;
		TextView txtValue;
	}
	
	private static class StatAdapter extends BaseAdapter {

		LayoutInflater inflater;
		double[] stats;
		
		public StatAdapter(Context context, double[] stats) {
			this.stats = stats;
			
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return stats.length;
		}

		@Override
		public Object getItem(int position) {
			return stats[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item_raw_stat, parent, false);
				holder.txtKey = (TextView) convertView.findViewById(R.id.txtKey);
				holder.txtValue = (TextView) convertView.findViewById(R.id.txtValue);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.txtKey.setText(position + "");
			holder.txtValue.setText(stats[position] + "");
			
			return convertView;
		}
		
	}
}
