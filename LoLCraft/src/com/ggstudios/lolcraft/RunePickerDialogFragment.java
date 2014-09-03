package com.ggstudios.lolcraft;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import com.ggstudios.utils.DebugLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class RunePickerDialogFragment extends DialogFragment {
	private static final String TAG = RunePickerDialogFragment.class.getSimpleName();
	
	private static final int ANIMATION_DURATION = 250;

	private GridView content;
	private List<RuneInfo> runes;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View rootView = inflater.inflate(R.layout.dialog_fragment_item_picker, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(rootView);

		
		content = (GridView) rootView.findViewById(R.id.itemGrid);
		content.setNumColumns(GridView.AUTO_FIT);
		content.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		content.setColumnWidth(getActivity().getResources().getDimensionPixelSize(R.dimen.rune_info_width));

		runes = LibraryManager.getInstance().getRuneLibrary().getAllRuneInfo();

		if (runes == null) {
			initializeItemInfo();
		} else {
			filterAndShowRunes();
		}

		content.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				DebugLog.d(TAG, runes.get(position).rawJson.toString());

				return false;
			}

		});

		content.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				((RunePickerDialogListener)getActivity()).onRunePicked(runes.get(position));
				dismiss();
			}

		});

		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Activity act = getActivity();

		if (!(act instanceof RunePickerDialogListener)) {
			throw new ClassCastException(act.getClass() + " must implement ItemPickerDialogListener");
		}
	}

	private void filterAndShowRunes() {
		content.setAdapter(new RuneInfoAdapter(getActivity(), 
				runes));
	}

	private void initializeItemInfo() {
		new AsyncTask<Void, ItemInfo, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					runes = LibraryUtils.getAllRuneInfo(getActivity());
					LibraryManager.getInstance().getRuneLibrary().initialize(runes);
				} catch (IOException e) {
					DebugLog.e(TAG, e);
				} catch (JSONException e) {
					DebugLog.e(TAG, e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void v) {
				filterAndShowRunes();
			}

		}.execute();
		
		new IconFetcher().execute(getActivity().getAssets());
	}

	private class ViewHolder {
		ImageView icon;
		TextView name;
		TextView desc;
		
		RuneInfo rune;
	}

	public class RuneInfoAdapter extends BaseAdapter {
		private Context context;
		private List<RuneInfo> runes;
		private LayoutInflater inflater;

		public RuneInfoAdapter(Context c, List<RuneInfo> runes) {
			context = c;
			this.runes = runes;

			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return runes.size();
		}

		public Object getItem(int position) {
			return runes.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {  // if it's not recycled, initialize some attributes
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item_rune_info, parent, false);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.txtName);
				holder.desc = (TextView) convertView.findViewById(R.id.txtDesc);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if (holder.rune != null) {
				holder.rune.tag = null;
				holder.rune = null;
			}

			RuneInfo info = runes.get(position);

			holder.name.setText(info.shortName);
			holder.desc.setText(info.shortDesc);

			if (info.icon != null) {
				holder.icon.setImageDrawable(info.icon);
			} else {
				holder.icon.setImageDrawable(new ColorDrawable(Color.GRAY));
				info.tag = holder.icon;
				holder.rune = info;
			}

			return convertView;
		}
	}

	private class IconFetcher extends AsyncTask<AssetManager, RuneInfo, Void> {

		@Override
		protected Void doInBackground(AssetManager... params) {
			AssetManager assets = params[0];

			for (RuneInfo rune : runes) {
				if (rune.icon == null) {
					try {
						rune.icon = Drawable.createFromStream(assets.open("rune/" + rune.iconAssetName), null);
						
						publishProgress(rune);
					} catch (IOException e) {
						DebugLog.e(TAG, e);
					}
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(RuneInfo... p) {
			RuneInfo rune = p[0];
			if (rune.tag != null) {
				ImageView v = (ImageView) rune.tag;

					final TransitionDrawable td =
							new TransitionDrawable(new Drawable[] {
									new ColorDrawable(Color.GRAY),
									rune.icon
							});
					td.setCrossFadeEnabled(true);
					v.setImageDrawable(td);
					td.startTransition(ANIMATION_DURATION);
			}
		}
	}

	public interface RunePickerDialogListener {
		public void onRunePicked(RuneInfo rune);
	}
}
