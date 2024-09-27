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

import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_CLICKED_IMAGE;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_IMAGES;
import static it.feio.android.omninotes.utils.ConstantsBase.GALLERY_TITLE;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_VIDEO;
import static it.feio.android.omninotes.utils.StorageHelper.getMimeType;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import it.feio.android.omninotes.databinding.ActivityGalleryBinding;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.listeners.OnViewTouchedListener;
import it.feio.android.omninotes.utils.FileProviderHelper;
import it.feio.android.simplegallery.models.GalleryPagerAdapter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {

  private ActivityGalleryBinding binding;

  private List<Attachment> images;
  OnViewTouchedListener screenTouches = new OnViewTouchedListener() {

    private static final int MOVING_THRESHOLD = 30;
    float x;
    float y;
    private boolean statusPressed = false;


    @Override
    public void onViewTouchOccurred(MotionEvent ev) {
      if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
        x = ev.getX();
        y = ev.getY();
        statusPressed = true;
      }
      if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
        float dx = Math.abs(x - ev.getX());
        float dy = Math.abs(y - ev.getY());
        double dxy = Math.sqrt(dx * dx + dy * dy);
        LogDelegate.v("Moved of " + dxy);
        if (dxy >= MOVING_THRESHOLD) {
          statusPressed = false;
        }
      }
      if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP && statusPressed) {
        click();
        statusPressed = false;
      }
    }


    private void click() {
      Attachment attachment = images.get(binding.fullscreenContent.getCurrentItem());
      if (attachment.getMime_type().equals(MIME_TYPE_VIDEO)) {
        viewMedia();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityGalleryBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    initViews();
    initData();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_gallery, menu);
    return true;
  }

  private void initViews() {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    binding.galleryRoot.setOnViewTouchedListener(screenTouches);

    binding.fullscreenContent.addOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageSelected(int arg0) {
        getSupportActionBar().setSubtitle("(" + (arg0 + 1) + "/" + images.size() + ")");
      }

      @Override
      public void onPageScrolled(int arg0, float arg1, int arg2) {
        // Nothing to do
      }

      @Override
      public void onPageScrollStateChanged(int arg0) {
        // Nothing to do
      }
    });
  }

  /**
   * Initializes data received from note detail screen
   */
  private void initData() {
    String title = getIntent().getStringExtra(GALLERY_TITLE);
    images = getIntent().getParcelableArrayListExtra(GALLERY_IMAGES) != null
        ? getIntent().getParcelableArrayListExtra(GALLERY_IMAGES)
        : Collections.emptyList();
    int clickedImage = getIntent().getIntExtra(GALLERY_CLICKED_IMAGE, 0);

    ArrayList<Uri> imageUris = new ArrayList<>();
    for (Attachment mAttachment : images) {
      imageUris.add(mAttachment.getUri());
    }

    GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this, imageUris);
    binding.fullscreenContent.setOffscreenPageLimit(3);
    binding.fullscreenContent.setAdapter(pagerAdapter);
    binding.fullscreenContent.setCurrentItem(clickedImage);

    getSupportActionBar().setTitle(title);
    getSupportActionBar().setSubtitle("(" + (clickedImage + 1) + "/" + images.size() + ")");

    // If selected attachment is a video it will be immediately played
    if (images.get(clickedImage).getMime_type().equals(MIME_TYPE_VIDEO)) {
      viewMedia();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        break;
      case R.id.menu_gallery_share:
        shareMedia();
        break;
      case R.id.menu_gallery:
        viewMedia();
        break;
      default:
        LogDelegate.e("Wrong element choosen: " + item.getItemId());
    }
    return super.onOptionsItemSelected(item);
  }

  private void viewMedia() {
    Attachment attachment = images.get(binding.fullscreenContent.getCurrentItem());
    Uri shareableAttachmentUri = getShareableAttachmentUri(attachment);
    if (shareableAttachmentUri != null) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.setDataAndType(shareableAttachmentUri, getMimeType(this, attachment.getUri()));
      startActivity(intent);
    }
  }

  private void shareMedia() {
    Attachment attachment = images.get(binding.fullscreenContent.getCurrentItem());
    Uri shareableAttachmentUri = getShareableAttachmentUri(attachment);
    if (shareableAttachmentUri != null) {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType(getMimeType(this, attachment.getUri()));
      intent.putExtra(Intent.EXTRA_STREAM, shareableAttachmentUri);
      startActivity(intent);
    }
  }

    private @Nullable Uri getShareableAttachmentUri(Attachment attachment) {
      try {
        return FileProviderHelper.getShareableUri(attachment);
      } catch (FileNotFoundException e) {
        LogDelegate.e(e.getMessage());
        Toast.makeText(this, R.string.attachment_not_found, Toast.LENGTH_SHORT).show();
        return null;
      }
    }

}
