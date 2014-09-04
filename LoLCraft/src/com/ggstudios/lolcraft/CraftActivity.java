package com.ggstudios.lolcraft;

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
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ggstudios.animation.ResizeAnimation;
import com.ggstudios.lolcraft.ItemPickerDialogFragment.ItemPickerDialogListener;
import com.ggstudios.lolcraft.RunePickerDialogFragment.RunePickerDialogListener;
import com.ggstudios.lolcraft.SplashFetcher.OnDrawableRetrievedListener;
import com.ggstudios.utils.DebugLog;
import com.ggstudios.utils.Utils;
import com.ggstudios.views.LockableScrollView;
import com.ggstudios.views.TabIndicator;
import com.ggstudios.views.TabIndicator.TabItem;

public class CraftActivity extends SherlockFragmentActivity implements ItemPickerDialogListener, RunePickerDialogListener {
	private static final String TAG = "CraftActivity";

	public static final String EXTRA_CHAMPION_ID = "champId";

	private static final int PARALLAX_WIDTH_DP = 30;
	private static final int RESIZE_DURATION = 200;

	private ChampionInfo info;

	private ImageView portrait;
	private ImageView splash;
	private TextView name;
	private TextView title;
	private ViewPager pager;
	private TabIndicator tabIndicator;
	private LockableScrollView splashScroll;
	
	private LinearLayout champInfoPanel;

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
		splashScroll = (LockableScrollView) findViewById(R.id.splashScrollView);
		champInfoPanel = (LinearLayout) findViewById(R.id.champInfoPanel);
		
		splashScroll.setScrollingEnabled(false);

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

		final int parallaxW = (int) Utils.convertDpToPixel(PARALLAX_WIDTH_DP, this);
		final int parallaxPer = parallaxW / adapter.getCount();

		tabIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				splashScroll.scrollTo((int) (parallaxPer * position + parallaxPer * positionOffset), splashScroll.getScrollY());
			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub

			}

		});
		
		portrait.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Animation ani = new ResizeAnimation(champInfoPanel, 0, champInfoPanel.getWidth(),
						0, champInfoPanel.getHeight());
				ani.setDuration(RESIZE_DURATION);
				champInfoPanel.startAnimation(ani);
				champInfoPanel.getLayoutParams().width = 0;
				champInfoPanel.requestLayout();
				champInfoPanel.setVisibility(View.VISIBLE);
				
			}
			
		});

		if (info.icon != null) {
			portrait.setImageDrawable(info.icon);
		}
		name.setText(info.name);
		title.setText(info.title);

		SplashFetcher.getInstance().fetchChampionSplash(info.key, new OnDrawableRetrievedListener() {

			@Override
			public void onDrawableRetrieved(Drawable d) {
				splash.setImageDrawable(d);

				if (splash.getWidth() == 0) {
					splash.post(new Runnable() {

						@Override
						public void run() {
							setUpSplash(parallaxW);
						}

					});
				} else {
					setUpSplash(parallaxW);
				}
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

	private void setUpSplash(int parallaxW) {
		splash.getLayoutParams().width = splashScroll.getWidth() + parallaxW;
		splashScroll.requestLayout();

		int w = MeasureSpec.makeMeasureSpec(splash.getLayoutParams().width, MeasureSpec.EXACTLY);
		int h = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE, MeasureSpec.UNSPECIFIED);
		splash.measure(w, h);

		int y = (splash.getMeasuredHeight() - splashScroll.getHeight()) / 2;
		
		DebugLog.d(TAG, "y:" + y);

		if (y > 0) {
			splashScroll.scrollTo(0, y);
		} else {
			splashScroll.post(new Runnable() {

				@Override
				public void run() {
					int y = (splash.getMeasuredHeight() - splashScroll.getLayoutParams().height) / 2;
					splashScroll.scrollTo(0, y);
				}

			});
		}
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
		if (build.canAdd(rune)) 
			build.addRune(rune);
	}
}
