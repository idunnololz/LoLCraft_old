package com.ggstudios.lolcraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import com.ggstudios.lolcraft.LibraryUtils.OnItemLoadListener;
import com.ggstudios.utils.DebugLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ItemPickerDialogFragment extends DialogFragment {
	private static final String TAG = "ItemPickerDialogFragment";
	
	public static final String EXTRA_CHAMPION_ID = "champId";
	public static final String EXTRA_MAP_ID = "mapId";

	private GridView content;
	private List<ItemInfo> items;
	private EditText searchField;
	
	private int champId = -1;
	private int mapId = -1;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);
	  
	  if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
		  setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
		  dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_full_holo_light);
	  } else {
		  setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
	  }
	  
	  return dialog;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.dialog_fragment_item_picker, container, false);

		content = (GridView) rootView.findViewById(R.id.itemGrid);
		
		items = LibraryManager.getInstance()
				.getItemLibrary().getPurchasableItemInfo();
		
		searchField = (EditText) rootView.findViewById(R.id.searchField);
		searchField.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	String str = s.toString();
	        	ListAdapter adapter = content.getAdapter();
	        	if (adapter != null) {
	        		((ItemInfoAdapter) adapter).filter(str);
	        	}
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		
		Bundle args = getArguments();
		if (args != null) {
			champId = args.getInt(EXTRA_CHAMPION_ID, -1);
			mapId = args.getInt(EXTRA_MAP_ID, -1);
			
			DebugLog.d(TAG, "MapId: " + mapId);
		}
		
		if (items == null) {
			initializeItemInfo();
		} else {
			filterAndShowItems();
		}
		
		content.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				DebugLog.d(TAG, ((ItemInfo) parent.getItemAtPosition(position)).rawJson.toString());
				
				return false;
			}
			
		});
		
		content.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				((ItemPickerDialogListener)getActivity()).onItemPicked((ItemInfo) parent.getItemAtPosition(position));
				dismiss();
			}
			
		});
		
		return rootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Activity act = getActivity();
		
		if (!(act instanceof ItemPickerDialogListener)) {
			throw new ClassCastException(act.getClass() + " must implement ItemPickerDialogListener");
		}
	}
	
	private void filterAndShowItems() {
		ChampionLibrary champLib = LibraryManager.getInstance().getChampionLibrary();
		ChampionInfo info = champLib.getChampionInfo(champId);
		
		items = new ArrayList<ItemInfo>();
				
		List<ItemInfo> fullList = LibraryManager.getInstance()
				.getItemLibrary().getPurchasableItemInfo();
		
		for (ItemInfo i : fullList) {
			if (i.notOnMap != null) {
				if (i.notOnMap.contains(mapId)) {
					continue;
				}
			}
			
			if (i.requiredChamp != null) {
				if (champLib.getChampionInfo(i.requiredChamp) == info) {
					items.add(i);
				}
			} else {
				items.add(i);
			}
		}

		content.setAdapter(new ItemInfoAdapter(getActivity(), 
				items));
	}
	
	private void initializeItemInfo() {
		new AsyncTask<Void, ItemInfo, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					LibraryUtils.getAllItemInfo(getActivity(),
							new OnItemLoadListener(){

						@Override
						public void onStartLoadPortrait(final List<ItemInfo> items) {
							final ItemLibrary itemLib = LibraryManager.getInstance().getItemLibrary();
							itemLib.initialize(items);
							
							content.post(new Runnable(){

								@Override
								public void run() {
									filterAndShowItems();
								}
								
							});
						}

						@Override
						public void onPortraitLoad(int position,
								ItemInfo info) {
							publishProgress(info);
						}

						@Override
						public void onCompleteLoadPortrait(List<ItemInfo> items) {}

					});
				} catch (IOException e) {
					DebugLog.e(TAG, e);
				} catch (JSONException e) {
					DebugLog.e(TAG, e);
				}
				return null;
			}

			protected void onProgressUpdate(ItemInfo... progress) {
				ItemInfo info = progress[0];
				
				int start = content.getFirstVisiblePosition();
				for(int i = start, j = content.getLastVisiblePosition(); i <= j; i++) {
					if(info == content.getItemAtPosition(i)){
						View view = content.getChildAt(i - start);
						content.getAdapter().getView(i, view, content);
						break;
					}
				}
			}

		}.execute();
	}

	private class ViewHolder {
		ImageView icon;
		TextView gold;
	}

	public class ItemInfoAdapter extends BaseAdapter {
		private Context context;
		private List<ItemInfo> itemInfoAll;
		private List<ItemInfo> itemInfo;
		private LayoutInflater inflater;

		private Drawable placeHolder;
		
		private String lastQuery;

		public ItemInfoAdapter(Context c, List<ItemInfo> Items) {
			context = c;
			itemInfoAll = Items;
			itemInfo = Items;

			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public void filter(String s) {
			if (s == null || s.length() == 0) {
				itemInfo = itemInfoAll;
			} else {
				List<ItemInfo> last = itemInfo;
				itemInfo = new ArrayList<ItemInfo>();
				
				s = s.toLowerCase(Locale.US);

				if (lastQuery != null && s.startsWith(lastQuery)) {
					for (ItemInfo i : last) {
						if (i.lowerName.contains(s) || i.colloq.contains(s)) {
							itemInfo.add(i);
						}
					}
				} else {
					for (ItemInfo i : itemInfoAll) {
						if (i.lowerName.contains(s) || i.colloq.contains(s)) {
							itemInfo.add(i);
						}
					}
				}
			}
			
			notifyDataSetChanged();
			lastQuery = s;
		}

		public int getCount() {
			return itemInfo.size();
		}

		public Object getItem(int position) {
			return itemInfo.get(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {  // if it's not recycled, initialize some attributes
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item_item_info, parent, false);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.gold = (TextView) convertView.findViewById(R.id.gold);

				placeHolder = holder.icon.getDrawable();

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ItemInfo info = itemInfo.get(position);

			holder.gold.setText("" + info.totalGold);

			if (info.icon != null) {
				holder.icon.setImageDrawable(info.icon);
			} else {
				holder.icon.setImageDrawable(placeHolder);
			}

			return convertView;
		}
	}
	
	public interface ItemPickerDialogListener {
		public void onItemPicked(ItemInfo item);
	}
}
