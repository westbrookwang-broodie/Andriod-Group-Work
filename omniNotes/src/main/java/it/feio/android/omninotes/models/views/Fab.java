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

package it.feio.android.omninotes.models.views;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static androidx.core.view.ViewCompat.animate;
import static it.feio.android.omninotes.utils.ConstantsBase.FAB_ANIMATION_TIME;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.recyclerview.widget.RecyclerView;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.listeners.OnFabItemClickedListener;

public class Fab {

  private final FloatingActionsMenu floatingActionsMenu;
  private final RecyclerView recyclerView;
  private final boolean expandOnLongClick;
  private boolean fabAllowed;
  private boolean fabHidden;
  private boolean fabExpanded;
  private View overlay;
  private OnFabItemClickedListener onFabItemClickedListener;


  public Fab(View fabView, RecyclerView recyclerView, boolean expandOnLongClick) {
    this.floatingActionsMenu = (FloatingActionsMenu) fabView;
    this.recyclerView = recyclerView;
    this.expandOnLongClick = expandOnLongClick;
    init();
  }

  private void init() {
    this.fabHidden = true;
    this.fabExpanded = false;

    AddFloatingActionButton fabAddButton = floatingActionsMenu
        .findViewById(R.id.fab_expand_menu_button);
    fabAddButton.setOnClickListener(v -> {
      if (!isExpanded() && expandOnLongClick) {
        performAction(v);
      } else {
        performToggle();
      }
    });
    fabAddButton.setOnLongClickListener(v -> {
      if (!expandOnLongClick) {
        performAction(v);
      } else {
        performToggle();
      }
      return true;
    });
    recyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0) {
              hideFab();
            } else if (dy < 0) {
              floatingActionsMenu.collapse();
              showFab();
            } else {
              LogDelegate.d("No Vertical Scrolled");
            }
          }
        });

    floatingActionsMenu.findViewById(R.id.fab_checklist).setOnClickListener(onClickListener);
    floatingActionsMenu.findViewById(R.id.fab_camera).setOnClickListener(onClickListener);

    if (!expandOnLongClick) {
      View noteBtn = floatingActionsMenu.findViewById(R.id.fab_note);
      noteBtn.setVisibility(View.VISIBLE);
      noteBtn.setOnClickListener(onClickListener);
    }
  }

  private final View.OnClickListener onClickListener = v -> onFabItemClickedListener.onFabItemClick(v.getId());

  public void performToggle() {
    fabExpanded = !fabExpanded;
    floatingActionsMenu.toggle();
  }

  private void performAction(View v) {
    if (fabExpanded) {
      floatingActionsMenu.toggle();
      fabExpanded = false;
    } else {
      onFabItemClickedListener.onFabItemClick(v.getId());
    }
  }

  public void showFab() {
    if (floatingActionsMenu != null && fabAllowed && fabHidden) {
      animateFab(0, View.VISIBLE, View.VISIBLE);
      fabHidden = false;
    }
  }

  public void hideFab() {
    if (floatingActionsMenu != null && !fabHidden) {
      floatingActionsMenu.collapse();
      animateFab(floatingActionsMenu.getHeight() + getMarginBottom(floatingActionsMenu),
          View.VISIBLE, View.INVISIBLE);
      fabHidden = true;
      fabExpanded = false;
    }
  }

  private void animateFab(int translationY, final int visibilityBefore, final int visibilityAfter) {
    animate(floatingActionsMenu).setInterpolator(new AccelerateDecelerateInterpolator())
        .setDuration(FAB_ANIMATION_TIME)
        .translationY(translationY)
        .setListener(new ViewPropertyAnimatorListener() {
          @Override
          public void onAnimationStart(View view) {
            floatingActionsMenu.setVisibility(visibilityBefore);
          }

          @Override
          public void onAnimationEnd(View view) {
            floatingActionsMenu.setVisibility(visibilityAfter);
          }

          @Override
          public void onAnimationCancel(View view) {
            // Nothing to do
          }
        });
  }

  public void setAllowed(boolean allowed) {
    fabAllowed = allowed;
  }

  private int getMarginBottom(View view) {
    int marginBottom = 0;
    final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
      marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
    }
    return marginBottom;
  }

  public void setOnFabItemClickedListener(OnFabItemClickedListener onFabItemClickedListener) {
    this.onFabItemClickedListener = onFabItemClickedListener;
  }

  public void setOverlay(View overlay) {
    this.overlay = overlay;
    this.overlay.setOnClickListener(v -> performToggle());
  }

  public void setOverlay(int colorResurce) {
    View overlayView = new View(OmniNotes.getAppContext());
    overlayView.setBackgroundResource(colorResurce);
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT,
        MATCH_PARENT);
    overlayView.setLayoutParams(params);
    overlayView.setVisibility(View.GONE);
    overlayView.setOnClickListener(v -> performToggle());
    ViewGroup parent = ((ViewGroup) floatingActionsMenu.getParent());
    parent.addView(overlayView, parent.indexOfChild(floatingActionsMenu));
    this.overlay = overlayView;
  }

  public boolean isExpanded() {
    return fabExpanded;
  }
}
