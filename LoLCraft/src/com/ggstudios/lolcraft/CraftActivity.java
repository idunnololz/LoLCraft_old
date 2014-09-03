package com.ggstudios.lolcraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ggstudios.lolcraft.ItemPickerDialogFragment.ItemPickerDialogListener;
import com.ggstudios.lolcraft.RunePickerDialogFragment.RunePickerDialogListener;
import com.ggstudios.lolcraft.SplashFetcher.OnDrawableRetrievedListener;
import com.ggstudios.utils.DebugLog;
import com.ggstudios.utils.Utils;
import com.ggstudios.views.TabIndicator;
import com.ggstudios.views.TabIndicator.TabItem;

public class CraftActivity extends SherlockFragmentActivity implements ItemPickerDialogListener, RunePickerDialogListener {
	private static final String TAG = "CraftActivity";

	public static final String EXTRA_CHAMPION_ID = "champId";

	private ChampionInfo info;

	private ImageView portrait;
	private ImageView splash;
	private TextView name;
	private TextView title;
	private ViewPager pager;
	private TabIndicator tabIndicator;
	
	private Build build;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DebugLog.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_craft);

		int champId = getIntent().getExtras().getInt(EXTRA_CHAMPION_ID);
		info = LibraryManager.getInstance().getChampionLibrary().getChampionInfo(champId);

		build = new Build();
		build.setChampion(info);
		StateManager.getInstance().setActiveBuild(build);
		
		portrait = (ImageView) findViewById(R.id.portrait);
		splash = (ImageView) findViewById(R.id.splash);
		name = (TextView) findViewById(R.id.name);
		title = (TextView) findViewById(R.id.title);
		pager = (ViewPager) findViewById(R.id.pager);
		tabIndicator = (TabIndicator) findViewById(R.id.tab_indicator);
		
		Bundle args = new Bundle();
		args.putInt(EXTRA_CHAMPION_ID, champId);
		
		// construct the tabs...
		List<TabItem> tabs = new ArrayList<TabIndicator.TabItem>();
		tabs.add(new TabItem("Basic", CraftBasicFragment.class.getName(), args));
		tabs.add(new TabItem("Skills", CraftSkillsFragment.class.getName(), args));
		tabs.add(new TabItem("Summary", CraftSummaryFragment.class.getName(), args));
		TabAdapter adapter = new TabAdapter(this, getSupportFragmentManager(), tabs);
		pager.setAdapter(adapter);
		tabIndicator.setAdapter(pager);
		
		pager.setPageMargin((int) Utils.convertDpToPixel(20, this));
		pager.setPageMarginDrawable(new ColorDrawable(Color.LTGRAY));
		
		if (info.icon != null) {
			portrait.setImageDrawable(info.icon);
		}
		name.setText(info.name);
		title.setText(info.title);

		SplashFetcher.getInstance().fetchChampionSplash(info.key, new OnDrawableRetrievedListener() {

			@Override
			public void onDrawableRetrieved(Drawable d) {
				splash.setImageDrawable(d);
			}

		});
		
		new AsyncTask<ChampionInfo, Void, ChampionInfo>() {

			@Override
			protected ChampionInfo doInBackground(ChampionInfo... params) {
				ChampionInfo info = params[0];
				LibraryUtils.completeChampionInfo(CraftActivity.this, info);
				return null;
			}
			
		}.execute(info);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class TabAdapter extends FragmentPagerAdapter implements TabIndicator.TabAdapter {
		private List<TabItem> items;
		private Context context;
		
        public TabAdapter(Context con, FragmentManager fm, List<TabItem> items) {
            super(fm);
            
            this.items = items;
            this.context = con;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Fragment getItem(int position) {
        	TabItem i = items.get(position);
            return Fragment.instantiate(context, i.getClassName(), i.getArguments());
        }

		@Override
		public TabItem getTab(int position) {
			return items.get(position);
		}
    }

	@Override
	public void onItemPicked(ItemInfo item) {
		build.addItem(item);
	}

	@Override
	public void onRunePicked(RuneInfo rune) {
		build.addRune(rune);
	}
}
