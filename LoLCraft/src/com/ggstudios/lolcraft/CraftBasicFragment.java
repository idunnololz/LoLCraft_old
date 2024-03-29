package com.ggstudios.lolcraft;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ggstudios.lolcraft.Build.BuildItem;
import com.ggstudios.lolcraft.Build.BuildObserver;
import com.ggstudios.lolcraft.Build.BuildRune;
import com.ggstudios.lolcraft.ChampionInfo.OnFullyLoadedListener;
import com.ggstudios.utils.DebugLog;
import com.ggstudios.utils.Utils;
import com.ggstudios.views.RearrangeableLinearLayout;
import com.ggstudios.views.RearrangeableLinearLayout.OnEdgeDragListener;
import com.ggstudios.views.RearrangeableLinearLayout.OnItemDragListener;
import com.ggstudios.views.RearrangeableLinearLayout.OnReorderListener;

public class CraftBasicFragment extends SherlockFragment implements BuildObserver {
	private static final String TAG = "CraftBasicFragment";

	public static final String EXTRA_CHAMPION_ID = "champId";

	private static final int MAP_ID_SUMMONERS_RIFT = 1;

	private static final int COLOR_ITEM_BONUS = 0xff0078ff;
	private static final int COLOR_LEVEL_BONUS = 0xff09a818;

	private static final int SCROLL_SPEED_PER_SEC_DP = 5;
	private static final int SEEK_BAR_PADDING_DP = 10;

	private static final int ANIMATION_DURATION = 300;

	private TextView lblPartype;
	private TextView lblPartypeRegen;
	
	private TextView txtHp;
	private TextView txtHpRegen;
	private TextView txtMs;
	private TextView txtMp;
	private TextView txtMpRegen;
	private TextView txtRange;
	private TextView txtAd;
	private TextView txtAs;
	private TextView txtAp;
	private TextView txtAr;
	private TextView txtMr;
	private Button addItem;
	private Button addRunes;
	private Spinner levelSpinner;
	private RearrangeableLinearLayout buildContainer;
	private HorizontalScrollView buildScrollView;
	private SeekBar seekBar;
	private ImageButton btnTrash;
	private LinearLayout runeContainer;
	private HorizontalScrollView runeScrollView;

	private int scrollSpeed;

	private Build build;

	private ChampionInfo champInfo;

	private int level;

	private LayoutInflater inflater;

	private int seekBarPadding;

	private static final DecimalFormat statFormat = new DecimalFormat("###.##");
	private static final DecimalFormat intStatFormat = new DecimalFormat("###");

	private void setStat(TextView tv, double base, double gain, int level, double itemBonus) {
		setStat(tv, base, gain, level, itemBonus, statFormat);
	}

	private void setStat(TextView tv, double base, double gain, int level, double itemBonus, DecimalFormat df) {
		double levelBonus = gain * level;
		double total = base + levelBonus + itemBonus;

		printStat(tv, total, itemBonus, levelBonus, true, df);
	}

	private void setStatAs(TextView tv, double base, double gain, int level, double itemBonus) {
		double levelBonus = gain * level;
		double total = base * (1 + levelBonus + itemBonus);

		printStat(tv, total, itemBonus, levelBonus, true, statFormat);
	}

	private void setLevelessStat(TextView tv, double base, double gain, int level, double itemBonus) {
		double total = base + itemBonus;

		printStat(tv, total, itemBonus, 0, false, statFormat);
	}

	private void setLevelessStat(TextView tv, double base, double gain, int level, double itemBonus, DecimalFormat df) {
		double total = base + itemBonus;

		printStat(tv, total, itemBonus, 0, false, df);
	}

	private void printStat(TextView tv, double total, double itemBonus, double levelBonus, boolean printLevelStat, DecimalFormat df) {
		SpannableStringBuilder span = new SpannableStringBuilder();
		span.append("" + df.format(total));
		int start = span.length();
		int s1 = span.length();
		span.append("(+" + statFormat.format(itemBonus) + ")");
		span.setSpan(new ForegroundColorSpan(COLOR_ITEM_BONUS), s1 + 1, span.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (printLevelStat) {
			s1 = span.length();
			span.append("(+" + statFormat.format(levelBonus) + ")");
			span.setSpan(new ForegroundColorSpan(COLOR_LEVEL_BONUS), s1 + 1, span.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		span.setSpan(new RelativeSizeSpan(0.7f), start, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		tv.setText(span);
	}

	boolean edgeDrag = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.inflater = inflater;

		scrollSpeed = (int) Utils.convertDpToPixel(SCROLL_SPEED_PER_SEC_DP, getActivity());
		seekBarPadding = (int) Utils.convertDpToPixel(SEEK_BAR_PADDING_DP, getActivity());

		build = StateManager.getInstance().getActiveBuild();

		DebugLog.d(TAG, "onCreateView");

		final View rootView = inflater.inflate(R.layout.fragment_craft_basic, container, false);

		final int champId = getArguments().getInt(EXTRA_CHAMPION_ID);
		champInfo = LibraryManager.getInstance().getChampionLibrary().getChampionInfo(champId);

		lblPartype = (TextView) rootView.findViewById(R.id.lblMp);
		lblPartypeRegen = (TextView) rootView.findViewById(R.id.lblMpRegen);
		txtHp = (TextView) rootView.findViewById(R.id.txtHp);
		txtHpRegen = (TextView) rootView.findViewById(R.id.txtHpRegen);
		txtMs = (TextView) rootView.findViewById(R.id.txtMs);
		txtMp = (TextView) rootView.findViewById(R.id.txtMp);
		txtMpRegen = (TextView) rootView.findViewById(R.id.txtMpRegen);
		txtRange = (TextView) rootView.findViewById(R.id.txtRange);
		txtAd = (TextView) rootView.findViewById(R.id.txtAd);
		txtAs = (TextView) rootView.findViewById(R.id.txtAs);
		txtAr = (TextView) rootView.findViewById(R.id.txtAr);
		txtMr = (TextView) rootView.findViewById(R.id.txtMr);
		txtAp = (TextView) rootView.findViewById(R.id.txtAp);
		levelSpinner = (Spinner) rootView.findViewById(R.id.level);
		buildContainer = (RearrangeableLinearLayout) rootView.findViewById(R.id.build);
		buildScrollView = (HorizontalScrollView) rootView.findViewById(R.id.scrollView);
		seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
		btnTrash = (ImageButton) rootView.findViewById(R.id.btnTrash);
		runeContainer = (LinearLayout) rootView.findViewById(R.id.runes);
		runeScrollView = (HorizontalScrollView) rootView.findViewById(R.id.runeScrollView);

		champInfo.onFullyLoaded(new OnFullyLoadedListener() {

			@Override
			public void onFullyLoaded() {
				switch (champInfo.partype) {
				case ChampionInfo.TYPE_ENERGY:
					lblPartype.setText(R.string.stat_energy);
					lblPartypeRegen.setText(R.string.stat_energy_regen);
					break;
				case ChampionInfo.TYPE_BLOODWELL:
					lblPartype.setText(R.string.stat_bloodwell);
					txtMp.setVisibility(View.INVISIBLE);
					lblPartypeRegen.setVisibility(View.INVISIBLE);
					txtMpRegen.setVisibility(View.INVISIBLE);
					break;
				case ChampionInfo.TYPE_UNKNOWN:
					lblPartype.setText(R.string.stat_unknown);
					txtMp.setVisibility(View.INVISIBLE);
					lblPartypeRegen.setVisibility(View.INVISIBLE);
					txtMpRegen.setVisibility(View.INVISIBLE);
					break;
				default:
					break;
				}
			}

		});

		updateStats();
		updateBuild();
		updateRunes();

		buildContainer.setOnEdgeDragListener(new OnEdgeDragListener() {

			@Override
			public void onEdgeDragLeft() {
				DebugLog.d(TAG, "edgeScroll");
				edgeDrag = true;

				new Thread() {
					@Override
					public void run() {
						while (edgeDrag) {
							try {
								sleep(17);
							} catch (InterruptedException e) {}

							buildScrollView.post(new Runnable() {

								@Override
								public void run() {
									int scrollX = buildScrollView.getScrollX() - scrollSpeed;
									if (scrollX > 0) {
										buildContainer.updateTouchX(-scrollSpeed);
									}
									buildScrollView.scrollBy(-scrollSpeed, 0);
								}

							});
						}
					}
				}.start();
			}

			@Override
			public void onEdgeDragRight() {
				edgeDrag = true;

				new Thread() {
					@Override
					public void run() {
						while (edgeDrag) {
							try {
								sleep(17);
							} catch (InterruptedException e) {}

							buildScrollView.post(new Runnable() {

								@Override
								public void run() {
									int scrollX = buildScrollView.getScrollX() + scrollSpeed;
									if (scrollX < buildContainer.getRight() - buildScrollView.getWidth() - buildContainer.getLeft()) {
										buildContainer.updateTouchX(scrollSpeed);
									}
									buildScrollView.scrollBy(scrollSpeed, 0);
								}

							});
						}
					}
				}.start();
			}

			@Override
			public void onEdgeDragCancel() {
				edgeDrag = false;
			}

		});

		buildContainer.setOnReorderListener(new OnReorderListener() {

			@Override
			public void onReorder(View v, int itemOldPosition,
					int itemNewPosition) {

				build.reorder(itemOldPosition, itemNewPosition);
				refreshAllItemViews();
			}

			@Override
			public void onBeginReorder() {
				hideSeekBar();
				showView(btnTrash);
			}

			@Override
			public void onToss(int itemPosition) {
				build.removeItemAt(itemPosition);
				refreshAllItemViews();
			}

			@Override
			public void onEndReorder() {
				if (build.getBuildSize() > 0) {
					showSeekBar();
				}
				hideView(btnTrash);
				TransitionDrawable transition = (TransitionDrawable) btnTrash.getBackground();
				transition.resetTransition();
			}

		});

		buildContainer.setOnItemDragListener(new OnItemDragListener() {

			@Override
			public void onItemDrag(float x, float y) {}

			@Override
			public void onEnterTossZone(View v) {
				TransitionDrawable transition = (TransitionDrawable) btnTrash.getBackground();
				transition.startTransition(ANIMATION_DURATION);
			}

			@Override
			public void onExitTossZone(View v) {
				TransitionDrawable transition = (TransitionDrawable) btnTrash.getBackground();
				transition.reverseTransition(ANIMATION_DURATION);
			}

		});

		buildContainer.post(new Runnable() {

			@Override
			public void run() {
				buildContainer.setMinimumWidth(buildScrollView.getWidth());
			}

		});

		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi") 
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				buildContainer.setEdgeThresholds(buildScrollView.getLeft(), buildScrollView.getRight());
				//...
				//do whatever you want with them
				//...
				//this is an important step not to keep receiving callbacks:
				//we should remove this listener
				//I use the function to remove it based on the api level!

				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else
					rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		addItem = (Button) rootView.findViewById(R.id.addItem);
		addItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putInt(ItemPickerDialogFragment.EXTRA_CHAMPION_ID, champId);
				b.putInt(ItemPickerDialogFragment.EXTRA_MAP_ID, MAP_ID_SUMMONERS_RIFT);
				FragmentManager fm = getActivity().getSupportFragmentManager();
				ItemPickerDialogFragment dialog = new ItemPickerDialogFragment();
				dialog.setArguments(b);
				dialog.show(fm, "dialog");
			}

		});

		addRunes = (Button) rootView.findViewById(R.id.addRunes);
		addRunes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				RunePickerDialogFragment dialog = new RunePickerDialogFragment();
				dialog.show(fm, "dialog");
			}

		});

		levelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				level = position;
				build.setChampionLevel(level);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}

		});

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				if (fromUser) {
					build.setEnabledBuildEnd(progress);
					refreshAllItemViews();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

		});

		seekBar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;

				case MotionEvent.ACTION_UP:
					// Allow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}

				// Handle ListView touch events.
				v.onTouchEvent(event);
				return true;
			}
		});

		return rootView;
	}

	@Override 
	public void onDestroyView() {
		super.onDestroyView();

		final int runeCount = build.getRuneCount();

		for (int i = 0; i < runeCount; i++) {
			build.getRune(i).tag = null;
		}
	}

	private void showView(View v) {
		Animation ani = new AlphaAnimation(0f, 1f);
		ani.setDuration(ANIMATION_DURATION);
		v.setVisibility(View.VISIBLE);
		v.startAnimation(ani);
	}

	private void hideView(final View v) {
		Animation ani = new AlphaAnimation(1f, 0f);
		ani.setDuration(ANIMATION_DURATION);
		ani.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}

		});

		v.startAnimation(ani);
	}

	private void showSeekBar() {
		showView(seekBar);
	}

	private void hideSeekBar() {
		hideView(seekBar);
	}

	@Override
	public void onResume() {
		super.onResume();
		build.registerObserver(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		build.unregisterObserver(this);
	}

	@Override
	public void onBuildChanged(Build build) {
		updateBuild();
	}

	@Override
	public void onItemAdded(Build build, BuildItem item, boolean isNewItem) {
		if (isNewItem) {
			updateBuild(item);
		} else {
			refreshAllItemViews();
		}
	}

	@Override
	public void onRuneAdded(Build build, BuildRune rune) {
		updateRunes(rune);
	}

	@Override
	public void onRuneRemoved(Build build, BuildRune rune) {
		((BuildRuneView) rune.tag).removeSelf();
	}

	@Override
	public void onBuildStatsChanged() {
		updateStats();
	}

	private void updateStats() {
		ChampionInfo info = champInfo;

		setStat(txtHp, 			info.hp, 		info.hpG, 		level, build.getBonusHp(),			intStatFormat);
		setStat(txtHpRegen, 	info.hpRegen, 	info.hpRegenG, 	level, build.getBonusHpRegen());
		setLevelessStat(txtMs, 	info.ms, 		0, 				level, build.getBonusMs(), 	intStatFormat);
		if (info.partype == ChampionInfo.TYPE_MANA) {
			setStat(txtMp, 			info.mp, 		info.mpG, 		level, build.getBonusMp(),			intStatFormat);
			setStat(txtMpRegen, 	info.mpRegen, 	info.mpRegenG, 	level, build.getBonusMpRegen());
		} else if (info.partype == ChampionInfo.TYPE_ENERGY) {
			setStat(txtMp, 			info.mp, 		info.mpG, 		level, build.getBonusEnergy(),		intStatFormat);
			setStat(txtMpRegen, 	info.mpRegen, 	info.mpRegenG, 	level, build.getBonusEnergyRegen());
		}
		setLevelessStat(txtRange, 	info.range, 0, 				level, build.getBonusRange());

		setStat(txtAd, 			info.ad, 		info.adG, 		level, build.getBonusAd(),			intStatFormat);
		setStatAs(txtAs, 		info.as, 		info.asG, 		level, build.getBonusAs());
		setLevelessStat(txtAp, 	0, 				0,		 		level, build.getBonusAp(),			intStatFormat);
		setStat(txtAr, 			info.ar, 		info.arG, 		level, build.getBonusAr(),			intStatFormat);
		setStat(txtMr, 			info.mr, 		info.mrG, 		level, build.getBonusMr(),			intStatFormat);
	}

	private void updateBuild() {
		updateBuild(null);
	}

	private void refreshAllItemViews() {
		final int count = buildContainer.getChildCount();

		for (int i = 0; i < count; i++) {
			View v = buildContainer.getChildAt(i);
			BuildItem item = build.getItem(i);
			ItemViewHolder holder = (ItemViewHolder) v.getTag();

			if (item.group != -1) {
				holder.groupIndicator.setBackgroundColor(Build.getSuggestedColorForGroup(item.group));
			} else {
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					holder.groupIndicator.setBackgroundDrawable(null);
				} else {
					holder.groupIndicator.setBackground(null);
				}
			}

			if (item.count != 1) {
				holder.count.setVisibility(View.VISIBLE);
				holder.count.setText("" + item.count);
			} else {
				holder.count.setVisibility(View.INVISIBLE);
			}

			if (i >= build.getEnabledBuildEnd()) {
				setAlphaForView(v, 0.5f);
			} else {
				setAlphaForView(v, 1f);
			}

			if (item.active) {
				v.setBackgroundColor(Color.GRAY);
			} else {
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					v.setBackgroundDrawable(null);
				} else {
					v.setBackground(null);
				}
			}


		}
	}

	private void setAlphaForView(View v, float alpha) {
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
			animation.setDuration(0);
			animation.setFillAfter(true);
			v.startAnimation(animation);
		} else {
			v.setAlpha(alpha);
		}
	}

	private View getChildView() {
		View v = inflater.inflate(R.layout.item_in_build, buildContainer, false);
		ItemViewHolder holder = new ItemViewHolder();
		holder.groupIndicator = v.findViewById(R.id.groupIndicator);
		holder.count = (TextView) v.findViewById(R.id.count);
		v.setTag(holder);

		return v;
	}

	private void updateBuild(BuildItem item) {
		if (item == null) {
			// update whole build
			buildContainer.removeAllViews();

			final int buildItemCount = build.getBuildSize();

			for (int i = 0; i < buildItemCount; i++) {
				item = build.getItem(i);
				View v = getChildView();
				ImageView icon = (ImageView) v.findViewById(R.id.icon);

				icon.setImageDrawable(item.info.icon);

				buildContainer.addView(v);
			}

			seekBar.setMax(build.getBuildSize());
			seekBar.setProgress(build.getEnabledBuildEnd());
		} else {
			View v = getChildView();
			ImageView icon = (ImageView) v.findViewById(R.id.icon);

			icon.setImageDrawable(item.info.icon);

			buildContainer.addView(v);
			buildScrollView.post(new Runnable() {

				@Override
				public void run() {
					buildScrollView.smoothScrollTo(buildContainer.getWidth(), 0);
				}

			});

			boolean wasFull = seekBar.getProgress() == seekBar.getMax() || build.getBuildSize() == 1;
			seekBar.setMax(build.getBuildSize());

			if (wasFull) {
				seekBar.setProgress(seekBar.getMax());
			}
		}

		if (build.getBuildSize() > 0) {
			seekBar.setVisibility(View.VISIBLE);

			seekBar.post(new Runnable() {

				@Override
				public void run() {
					DebugLog.d(TAG, "Right: " + buildContainer.getChildAt(buildContainer.getChildCount() - 1).getRight());
					seekBar.getLayoutParams().width = buildContainer.getChildAt(buildContainer.getChildCount() - 1).getRight() + seekBarPadding;
					seekBar.requestLayout();
				}

			});
		} else {
			seekBar.setVisibility(View.INVISIBLE);
		}

		refreshAllItemViews();
	}

	private void updateRunes() {
		updateRunes(null);
	}

	private void updateRunes(BuildRune rune) {
		if (rune == null) {
			runeContainer.removeAllViews();

			final int runeCount = build.getRuneCount();

			for (int i = 0; i < runeCount; i++) {
				rune = build.getRune(i);
				BuildRuneView v = (BuildRuneView) inflater.inflate(R.layout.item_rune_in_build, runeContainer, false);
				v.bindBuildRune(rune);

				runeContainer.addView(v);
				rune.tag = v;
			}
		} else {
			BuildRuneView v = (BuildRuneView) inflater.inflate(R.layout.item_rune_in_build, runeContainer, false);
			v.bindBuildRune(rune);

			runeContainer.addView(v);
			runeScrollView.post(new Runnable() {

				@Override
				public void run() {
					runeScrollView.smoothScrollTo(runeContainer.getWidth(), 0);
				}

			});

			rune.tag = v;
		}
	}

	private class ItemViewHolder {
		View groupIndicator;
		TextView count;
	}
}
