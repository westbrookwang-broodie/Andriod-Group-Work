/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes;

import static it.feio.android.omninotes.helpers.AppVersionHelper.isAppUpdated;
import static it.feio.android.omninotes.helpers.AppVersionHelper.updateAppVersionInPreferences;
import static it.feio.android.omninotes.helpers.BuildHelper.isDebugBuild;
import static it.feio.android.omninotes.helpers.ChangelogHelper.showChangelog;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_NOTIFICATION_CLICK;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_RESTART_APP;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SEND_AND_EXIT;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_START_APP;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET_TAKE_PHOTO;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_GOOGLE_NOW;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_KEY;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.pixplicity.easyprefs.library.Prefs;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.omninotes.async.UpdateWidgetsTask;
import it.feio.android.omninotes.async.bus.NotificationsGrantedEvent;
import it.feio.android.omninotes.async.bus.PasswordRemovedEvent;
import it.feio.android.omninotes.async.bus.SwitchFragmentEvent;
import it.feio.android.omninotes.async.notes.NoteProcessorDelete;
import it.feio.android.omninotes.databinding.ActivityMainBinding;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.NotesHelper;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.intro.IntroActivity;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.FileProviderHelper;
import it.feio.android.omninotes.utils.PasswordHelper;
import it.feio.android.omninotes.utils.SystemHelper;
import it.feio.android.pixlui.links.UrlCompleter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;


public class MainActivity extends BaseActivity implements
    SharedPreferences.OnSharedPreferenceChangeListener {

  private boolean isPasswordAccepted = false;
  public static final String FRAGMENT_DRAWER_TAG = "fragment_drawer";
  public static final String FRAGMENT_LIST_TAG = "fragment_list";
  public static final String FRAGMENT_DETAIL_TAG = "fragment_detail";
  public static final String FRAGMENT_SKETCH_TAG = "fragment_sketch";
  @Getter @Setter
  private Uri sketchUri;
  boolean prefsChanged = false;
  private FragmentManager mFragmentManager;

  ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.OmniNotesTheme_ApiSpec);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    EventBus.getDefault().register(this);
    Prefs.getPreferences().registerOnSharedPreferenceChangeListener(this);
    new NotificationsHelper(this).askToEnableNotifications(this);

    initUI();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    if (!launchIntroIfRequired() && isAppUpdated(getApplicationContext()) && !isDebugBuild()) {
      showChangelogAndUpdateCurrentVersion();
    }
  }

  private void showChangelogAndUpdateCurrentVersion() {
    showChangelog(this);
    updateAppVersionInPreferences(getApplicationContext());
  }

  private boolean launchIntroIfRequired() {
    if (IntroActivity.mustRun()) {
      startActivity(new Intent(getApplicationContext(), IntroActivity.class));
      return true;
    }
    return false;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (isPasswordAccepted) {
      init();
    } else {
      checkPassword();
    }
  }


  @Override
  protected void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
  }


  private void initUI() {
    setSupportActionBar(binding.toolbar.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
  }


  /**
   * This method starts the bootstrap chain.
   */
  private void checkPassword() {
    if (Prefs.getString(PREF_PASSWORD, null) != null
        && Prefs.getBoolean("settings_password_access", false)) {
      PasswordHelper.requestPassword(this, passwordConfirmed -> {
        switch (passwordConfirmed) {
          case SUCCEED:
            init();
            break;
          case FAIL:
            finish();
            break;
          case RESTORE:
            PasswordHelper.resetPassword(this);
        }
      });
    } else {
      init();
    }
  }


  public void onEvent(PasswordRemovedEvent passwordRemovedEvent) {
    showMessage(R.string.password_successfully_removed, ONStyle.ALERT);
    init();
  }

  public void onEvent(NotificationsGrantedEvent notificationsGrantedEvent) {
    if (!notificationsGrantedEvent.granted) {
      showToast(getString(R.string.denied_notifications_permission), Toast.LENGTH_LONG);
    }
  }

  private void init() {
    isPasswordAccepted = true;

    getFragmentManagerInstance();

    NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManagerInstance()
        .findFragmentById(R.id.navigation_drawer);
    if (mNavigationDrawerFragment == null) {
      FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
      fragmentTransaction.replace(R.id.navigation_drawer, new NavigationDrawerFragment(),
          FRAGMENT_DRAWER_TAG).commit();
    }

    if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG) == null) {
      FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
      fragmentTransaction.add(R.id.fragment_container, new ListFragment(), FRAGMENT_LIST_TAG)
          .commit();
    }

    handleIntents();
  }

  private FragmentManager getFragmentManagerInstance() {
    if (mFragmentManager == null) {
      mFragmentManager = getSupportFragmentManager();
    }
    return mFragmentManager;
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (intent.getAction() == null) {
      intent.setAction(ACTION_START_APP);
    }
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntents();
    LogDelegate.d("onNewIntent");
  }


  public MenuItem getSearchMenuItem() {
    Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      return ((ListFragment) f).getSearchMenuItem();
    } else {
      return null;
    }
  }


  public void editTag(Category tag) {
    Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      ((ListFragment) f).editCategory(tag);
    }
  }

  public void initNotesList(Intent intent) {
    if (intent != null) {
      Fragment searchTagFragment = startSearchView();
      new Handler(getMainLooper()).post(() -> ((ListFragment) searchTagFragment).initNotesList(intent));
    }
  }

  public Fragment startSearchView() {
    FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
    animateTransition(transaction, TRANSITION_HORIZONTAL);
    ListFragment mListFragment = new ListFragment();
    transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
        (FRAGMENT_DETAIL_TAG).commit();
    Bundle args = new Bundle();
    args.putBoolean("setSearchFocus", true);
    mListFragment.setArguments(args);
    return mListFragment;
  }


  public void commitPending() {
    Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      ((ListFragment) f).commitPending();
    }
  }


  /**
   * Checks if allocated fragment is of the required type and then returns it or returns null
   */
  private Fragment checkFragmentInstance(int id, Object instanceClass) {
    Fragment result = null;
    Fragment fragment = getFragmentManagerInstance().findFragmentById(id);
    if (fragment != null && instanceClass.equals(fragment.getClass())) {
      result = fragment;
    }
    return result;
  }

  @Override
  public void onBackPressed() {
    // SketchFragment
    Fragment f = checkFragmentInstance(R.id.fragment_container, SketchFragment.class);
    if (f != null) {
      ((SketchFragment) f).save();

      // Removes forced portrait orientation for this fragment
      setRequestedOrientation(
          ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

      getFragmentManagerInstance().popBackStack();
      return;
    }

    // DetailFragment
    f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
    if (f != null) {
      var detailDragment = (DetailFragment) f;
      detailDragment.goBack = true;
      detailDragment.saveAndExit();
      return;
    }

    // ListFragment
    f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      // Before exiting from app the navigation drawer is opened
      if (Prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null &&
          !getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
        getDrawerLayout().openDrawer(GravityCompat.START);
      } else if (!Prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null
          &&
          getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
        getDrawerLayout().closeDrawer(GravityCompat.START);
      } else {
        if (!((ListFragment) f).closeFab()) {
          isPasswordAccepted = false;
          super.onBackPressed();
        }
      }
      return;
    }
    super.onBackPressed();
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("navigationTmp", navigationTmp);
  }


  @Override
  protected void onPause() {
    super.onPause();
    Crouton.cancelAllCroutons();
  }


  public DrawerLayout getDrawerLayout() {
    return binding.drawerLayout;
  }


  public ActionBarDrawerToggle getDrawerToggle() {
    if (getFragmentManagerInstance().findFragmentById(R.id.navigation_drawer) != null) {
      return ((NavigationDrawerFragment) getFragmentManagerInstance().findFragmentById(
          R.id.navigation_drawer)).mDrawerToggle;
    } else {
      return null;
    }
  }


  /**
   * Finishes multiselection mode started by ListFragment
   */
  public void finishActionMode() {
    ListFragment fragment = (ListFragment) getFragmentManagerInstance()
        .findFragmentByTag(FRAGMENT_LIST_TAG);
    if (fragment != null) {
      fragment.finishActionMode();
    }
  }


  Toolbar getToolbar() {
    return binding.toolbar.toolbar;
  }


  private void handleIntents() {
    Intent i = getIntent();

    if (i.getAction() == null) {
      return;
    }

    if (ACTION_RESTART_APP.equals(i.getAction())) {
      SystemHelper.restartApp();
    }

    if (receivedIntent(i)) {
      Note note = i.getParcelableExtra(INTENT_NOTE);
      if (note == null) {
        note = DbHelper.getInstance().getNote(i.getIntExtra(INTENT_KEY, 0));
      }
      // Checks if the same note is already opened to avoid to open again
      if (note != null && noteAlreadyOpened(note)) {
        return;
      }
      // Empty note instantiation
      if (note == null) {
        note = new Note();
      }
      switchToDetail(note);
      return;
    }

    if (ACTION_SEND_AND_EXIT.equals(i.getAction())) {
      saveAndExit(i);
      return;
    }

    // Tag search
    if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getDataString()
        .startsWith(UrlCompleter.HASHTAG_SCHEME)) {
      switchToList();
      return;
    }

    // Home launcher shortcut widget
    if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getData() != null) {
      Long id = Long.valueOf(Uri.parse(i.getDataString()).getQueryParameter("id"));
      Note note = DbHelper.getInstance().getNote(id);
      if (note == null) {
        showMessage(R.string.note_doesnt_exist, ONStyle.ALERT);
        return;
      }
      switchToDetail(note);
      return;
    }

    // Home launcher "new note" shortcut widget
    if (ACTION_SHORTCUT_WIDGET.equals(i.getAction())) {
      switchToDetail(new Note());
      return;
    }
  }


  /**
   * Used to perform a quick text-only note saving (eg. Tasker+Pushbullet)
   */
  private void saveAndExit(Intent i) {
    Note note = new Note();
    note.setTitle(i.getStringExtra(Intent.EXTRA_SUBJECT));
    note.setContent(i.getStringExtra(Intent.EXTRA_TEXT));
    DbHelper.getInstance().updateNote(note, true);
    showToast(getString(R.string.note_updated), Toast.LENGTH_SHORT);
    finish();
  }


  private boolean receivedIntent(Intent i) {
    return ACTION_SHORTCUT.equals(i.getAction())
        || ACTION_NOTIFICATION_CLICK.equals(i.getAction())
        || ACTION_WIDGET.equals(i.getAction())
        || ACTION_WIDGET_TAKE_PHOTO.equals(i.getAction())
        || ((Intent.ACTION_SEND.equals(i.getAction())
        || Intent.ACTION_SEND_MULTIPLE.equals(i.getAction())
        || INTENT_GOOGLE_NOW.equals(i.getAction())
        || Intent.ACTION_PROCESS_TEXT.equals(i.getAction()))
        && i.getType() != null)
        || i.getAction().contains(ACTION_NOTIFICATION_CLICK);
  }


  private boolean noteAlreadyOpened(Note note) {
    DetailFragment detailFragment = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(
        FRAGMENT_DETAIL_TAG);
    return detailFragment != null && NotesHelper.haveSameId(note, detailFragment.getCurrentNote());
  }


  public void switchToList() {
    FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
    animateTransition(transaction, TRANSITION_HORIZONTAL);
    ListFragment mListFragment = new ListFragment();
    transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
        (FRAGMENT_DETAIL_TAG).commitAllowingStateLoss();
    if (getDrawerToggle() != null) {
      getDrawerToggle().setDrawerIndicatorEnabled(false);
    }
    getFragmentManagerInstance().getFragments();
    EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
  }


  public void switchToDetail(Note note) {
    FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
    animateTransition(transaction, TRANSITION_HORIZONTAL);
    DetailFragment mDetailFragment = new DetailFragment();
    Bundle b = new Bundle();
    b.putParcelable(INTENT_NOTE, note);
    mDetailFragment.setArguments(b);
    if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG) == null) {
      transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
          .addToBackStack(FRAGMENT_LIST_TAG)
          .commitAllowingStateLoss();
    } else {
      getFragmentManagerInstance().popBackStackImmediate();
      transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
          .addToBackStack(FRAGMENT_DETAIL_TAG)
          .commitAllowingStateLoss();
    }
  }


  public void shareNote(Note note) {
    Intent shareIntent = new Intent();
    // Prepare sharing intent with only text
    if (note.getAttachmentsList().isEmpty()) {
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");

      // Intent with single image attachment
    } else if (note.getAttachmentsList().size() == 1) {
      Attachment attachment = note.getAttachmentsList().get(0);
      Uri shareableAttachmentUri = getShareableAttachmentUri(attachment);
      if (shareableAttachmentUri != null) {
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(attachment.getMime_type());
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareableAttachmentUri);
      }
      // Intent with multiple images
    } else if (note.getAttachmentsList().size() > 1) {
      shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
      ArrayList<Uri> uris = new ArrayList<>();
      // A check to decide the mime type of attachments to share is done here
      HashMap<String, Boolean> mimeTypes = new HashMap<>();
      for (Attachment attachment : note.getAttachmentsList()) {
        Uri shareableAttachmentUri = getShareableAttachmentUri(attachment);
        if (shareableAttachmentUri != null) {
          uris.add(shareableAttachmentUri);
          mimeTypes.put(attachment.getMime_type(), true);
        }
      }
      // If many mime types are present a general type is assigned to intent
      if (mimeTypes.size() > 1) {
        shareIntent.setType("*/*");
      } else {
        shareIntent.setType((String) mimeTypes.keySet().toArray()[0]);
      }

      shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    }
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
    shareIntent.putExtra(Intent.EXTRA_TEXT, note.getContent());

    startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_message_chooser)));
  }

  public @Nullable Uri getShareableAttachmentUri(Attachment attachment) {
    try {
      return FileProviderHelper.getShareableUri(attachment);
    } catch (FileNotFoundException e) {
      LogDelegate.e(e.getMessage());
      Toast.makeText(this, R.string.attachment_not_found, Toast.LENGTH_SHORT).show();
      return null;
    }
  }


  /**
   * Single note permanent deletion
   *
   * @param note Note to be deleted
   */
  public void deleteNote(Note note) {
    new NoteProcessorDelete(Collections.singletonList(note)).process();
    BaseActivity.notifyAppWidgets(this);
    LogDelegate.d("Deleted permanently note with ID '" + note.get_id() + "'");
  }


  public void updateWidgets() {
    new UpdateWidgetsTask(getApplicationContext())
        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  public void showMessage(int messageId, Style style) {
    showMessage(getString(messageId), style);
  }


  public void showMessage(String message, Style style) {
    // ViewGroup used to show Crouton keeping compatibility with the new Toolbar
    runOnUiThread(
        () -> Crouton.makeText(this, message, style, binding.croutonHandle.croutonHandle).show());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    prefsChanged = true;
  }

}
