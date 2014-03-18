package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_AUTHOR;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.FAV;

import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.InThePoche.R;

public class ReadArticle extends SherlockActivity {
	TextView txtTitre;
	TextView txtContent;
	TextView txtAuthor;
	// Button btnMarkRead;
	SQLiteDatabase database;
	String id = "";
	ScrollView view;

	private String articleUrl;
	private Menu menu;
	private boolean isRead;
	private boolean isFav;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.article);
		view = (ScrollView) findViewById(R.id.scroll);
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(
				getApplicationContext());
		database = helper.getWritableDatabase();
		String[] getStrColumns = new String[] { ARTICLE_URL, MY_ID,
				ARTICLE_TITLE, ARTICLE_CONTENT, ARCHIVE, ARTICLE_AUTHOR, FAV };
		Bundle data = getIntent().getExtras();
		if (data != null) {
			id = data.getString("id");
		}
		Cursor ac = database.query(ARTICLE_TABLE, getStrColumns, MY_ID + "="
				+ id, null, null, null, null);
		ac.moveToFirst();
		txtTitre = (TextView) findViewById(R.id.txtTitre);
		txtTitre.setText(ac.getString(2));
		txtContent = (TextView) findViewById(R.id.txtContent);
		txtContent.setText(ac.getString(3));

		txtAuthor = (TextView) findViewById(R.id.txtAuthor);
		txtAuthor.setText(ac.getString(0));
		articleUrl = ac.getString(0);

		// btnMarkRead = (Button) findViewById(R.id.btnMarkRead);
		// btnMarkRead.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// ContentValues values = new ContentValues();
		// values.put(ARCHIVE, 1);
		// database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);
		// finish();
		// }
		// });
		findOutIfIsRead(ac.getInt(4));
		findOutIfIsFav(ac.getInt(6));
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		ContentValues values = new ContentValues();
		values.put("read_at", view.getScrollY());
		database.update(ARTICLE_TABLE, values, ARTICLE_ID + "=" + id, null);
		System.out.println(view.getScrollY());
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.option_read, menu);
		this.menu = menu;
		setReadStateIcon();
		setFavStateIcon();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.read:
			toggleMarkAsRead();
			return true;
		case R.id.fav:
			toggleFav();
			return true;
		case R.id.settings:
			startActivity(new Intent(getBaseContext(), Settings.class));
			return true;
		case R.id.share:
			shareUrl();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void shareUrl() {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		viewIntent.setData(Uri.parse(articleUrl));

		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_TEXT, articleUrl);

		Intent intentChooser = createIntentChooserForTwoIntents(viewIntent, sendIntent, getString(R.string.share_title));

		startActivity(intentChooser);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}

	private void setReadStateIcon() {
		MenuItem item = menu.findItem(R.id.read);

		if (isRead) {
			item.setIcon(R.drawable.ic_action_undo);
			item.setTitle(getString(R.string.unread_title));
		} else {
			item.setIcon(R.drawable.ic_action_accept);
			item.setTitle(getString(R.string.read_title));
		}
	}

	private void setFavStateIcon() {
		MenuItem item = menu.findItem(R.id.fav);

		if (isFav)
			item.setIcon(R.drawable.ic_action_important);
		else
			item.setIcon(R.drawable.ic_action_not_important);
	}

	private void findOutIfIsRead(int read) {

		isRead = read == 1 ? true : false;
	}

	private void findOutIfIsFav(int fav) {

		isFav = fav == 1 ? true : false;
	}

	private void toggleMarkAsRead() {
		int value = isRead ? 0 : 1;

		ContentValues values = new ContentValues();
		values.put(ARCHIVE, value);
		database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);

		if (isRead) {
			showToast(getString(R.string.marked_as_unread));
			isRead = false;
		} else {
			showToast(getString(R.string.marked_as_read));
			finish();
		}
		setReadStateIcon();
	}

	private void toggleFav() {
		int value = isFav ? 0 : 1;

		ContentValues values = new ContentValues();
		values.put(FAV, value);
		database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);

		if (isFav) {
			showToast(getString(R.string.marked_as_not_fav));
			isFav = false;
		} else {
			showToast(getString(R.string.marked_as_fav));
			isFav = true;
		}
		setFavStateIcon();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(ReadArticle.this, toast, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	private Intent createIntentChooserForTwoIntents(Intent first, Intent second, String title) {

		PackageManager pm = getPackageManager();

		Intent chooser = Intent.createChooser(second, title);

		List<ResolveInfo> resInfo = pm.queryIntentActivities(first, 0);
		Intent[] extraIntents = new Intent[resInfo.size()];
		for (int i = 0; i < resInfo.size(); i++) {
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.packageName;
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(packageName,
					ri.activityInfo.name));
			intent.setAction(first.getAction());
			first.setData(first.getData());
			extraIntents[i] = intent;
		}

		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

		return chooser;
	}
}
