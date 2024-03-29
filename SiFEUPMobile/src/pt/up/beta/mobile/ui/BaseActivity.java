package pt.up.beta.mobile.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import pt.up.beta.mobile.R;
import pt.up.beta.mobile.sifeup.AccountUtils;
import pt.up.beta.mobile.ui.personalarea.PersonalAreaActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.KeyEvent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import external.com.google.android.apps.iosched.util.UIUtils;

/**
 * A base activity that defers common functionality across app activities. This
 * class shouldn't be used directly; instead, activities should inherit from
 * {@link BaseSinglePaneActivity} or {@link BaseMultiPaneActivity}.
 */
public abstract class BaseActivity extends SherlockFragmentActivity {
	protected ActionBar actionbar;
	private SlidingMenu slidingMenu;
	private Handler mHandler = new Handler();
	private final static ExceptionParser parser = new ExceptionParser() {
		@Override
		public String getDescription(String threadName, Throwable t) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			return "Id:" + AccountUtils.getActiveUserCode(null) + "\n"
					+ sw.toString() + "\n";
		}
	};

	public void onCreate(Bundle o) {
		super.onCreate(o);
		actionbar = getSupportActionBar();
		if (!UIUtils.isTablet(getApplicationContext())) {
			// customize the SlidingMenu
			/*slidingMenu = new SlidingMenu(getApplicationContext());
			slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
			slidingMenu.setShadowDrawable(R.drawable.shadow);
			slidingMenu.setBehindOffsetRes(R.dimen.actionbar_home_width);
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			slidingMenu.attachToActivity(getApplicationContext(), SlidingMenu.SLIDING_CONTENT);
			slidingMenu.setMenu(R.layout.menu_frame);*/
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
		EasyTracker.getTracker().setExceptionParser(parser);
	}

	@Override
	public void onResume() {
		super.onResume();
		if ( AccountUtils.isInvalidated() ){
			logOut();
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// NOTE: there needs to be a content view set before this is called, so
		// this method should be called in onPostCreate.
		actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT < 5
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			onBackPressed();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goUp();
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.default_menu_items, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.menu_search:
			startSearch(null, false, Bundle.EMPTY, false);
			return true;
		case android.R.id.home:
			if (UIUtils.isTablet(getApplicationContext()))
				goUp();
			else
				slidingMenu.toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void logOut(){
		Intent i = new Intent(this, LauncherActivity.class).putExtra(
				LauncherActivity.LOGOUT_FLAG, true).addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	/**
	 * Invoke "home" action, returning to {@link HomeActivity}.
	 */
	protected void goUp() {
		if (this instanceof PersonalAreaActivity) {
			return;
		}
		final Intent upIntent = new Intent(this, PersonalAreaActivity.class);
		if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
			// This activity is not part of the application's task, so create a
			// new task
			// with a synthesized back stack.
			TaskStackBuilder.create(this).addNextIntent(upIntent)
					.startActivities();
			finish();
		} else {
			// This activity is part of the application's task, so simply
			// navigate up to the hierarchical parent activity.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(upIntent);
				finish();
			} else
				NavUtils.navigateUpTo(this, upIntent);
		}
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	/**
	 * Takes a given intent and either starts a new activity to handle it (the
	 * default behavior), or creates/updates a fragment (in the case of a
	 * multi-pane activity) that can handle the intent.
	 * 
	 * Must be called from the main (UI) thread.
	 * 
	 * @param intent
	 */
	public void openActivityOrFragment(final Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		if (slidingMenu != null) {
			if (slidingMenu.isMenuShowing()) {
				// delay a bit to help prevent jankyness
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						slidingMenu.showContent();
					}
				}, 50);
			}
		}
	}

	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	/**
	 * Converts an intent into a {@link Bundle} suitable for use as fragment
	 * arguments.
	 * 
	 * @param intent
	 * @return the bundle with the argument
	 */
	public static Bundle intentToFragmentArguments(Intent intent) {
		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}

		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable(BaseFragment.URL_INTENT, data);
		}

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}

		return arguments;
	}

	/**
	 * Converts a fragment arguments bundle into an intent.
	 * 
	 * @param arguments
	 * @return the argument in a intent
	 */
	public static Intent fragmentArgumentsToIntent(Bundle arguments) {
		Intent intent = new Intent();
		if (arguments == null) {
			return intent;
		}

		final Uri data = arguments.getParcelable("_uri");
		if (data != null) {
			intent.setData(data);
		}

		intent.putExtras(arguments);
		intent.removeExtra("_uri");
		return intent;
	}

	public void showContent() {
		if (slidingMenu != null)
			slidingMenu.showContent();
	}
}
