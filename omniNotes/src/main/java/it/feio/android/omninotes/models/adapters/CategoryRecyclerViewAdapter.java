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
package it.feio.android.omninotes.models.adapters;

import static android.graphics.Color.parseColor;
import static android.view.LayoutInflater.from;
import static it.feio.android.omninotes.databinding.DrawerListItemBinding.inflate;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_NAVIGATION;
import static java.lang.Integer.parseInt;

import android.app.Activity;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.adapters.category.CategoryViewHolder;
import java.util.List;


public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

  private final Activity mActivity;
  private final List<Category> categories;
  private final String navigationTmp;

  public CategoryRecyclerViewAdapter(Activity mActivity, List<Category> categories) {
    this(mActivity, categories, null);
  }

  public CategoryRecyclerViewAdapter(Activity mActivity, List<Category> categories,
      String navigationTmp) {
    this.mActivity = mActivity;
    this.categories = categories;
    this.navigationTmp = navigationTmp;
  }

  @Override
  public int getItemCount() {
    return categories.size();
  }

  @NonNull
  @Override
  public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new CategoryViewHolder(inflate(from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
    var category = categories.get(position);

    holder.txtTitle.setText(category.getName());

    if (isSelected(position)) {
      holder.txtTitle.setTypeface(null, Typeface.BOLD);
      holder.txtTitle.setTextColor(parseInt(category.getColor()));
    } else {
      holder.txtTitle.setTypeface(null, Typeface.NORMAL);
      holder.txtTitle.setTextColor(mActivity.getResources().getColor(R.color.drawer_text));
    }

    // Set the results into ImageView checking if an icon is present before
    if (category.getColor() != null && category.getColor().length() > 0) {
      var img = mActivity.getResources().getDrawable(R.drawable.ic_folder_special_black_24dp);
      var cf = new LightingColorFilter(parseColor("#000000"), parseInt(category.getColor()));
      img.mutate().setColorFilter(cf);
      holder.imgIcon.setImageDrawable(img);
      holder.imgIcon.setPadding(4, 4, 4, 4);
    }
    showCategoryCounter(holder, category);
  }

  private void showCategoryCounter(@NonNull CategoryViewHolder holder, Category category) {
    if (Prefs.getBoolean("settings_show_category_count", true)) {
      holder.count.setText(String.valueOf(category.getCount()));
      holder.count.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  private boolean isSelected(int position) {
    var navigationListCodes = mActivity.getResources()
        .getStringArray(R.array.navigation_list_codes);

    // Managing temporary navigation indicator when coming from a widget
    var navigationTmpLocal = MainActivity.class.isAssignableFrom(mActivity.getClass())
        ? ((MainActivity) mActivity).getNavigationTmp()
        : null;
    navigationTmpLocal = this.navigationTmp != null ? this.navigationTmp : navigationTmpLocal;

    var navigation = navigationTmp != null
        ? navigationTmpLocal
        : Prefs.getString(PREF_NAVIGATION, navigationListCodes[0]);

    return navigation.equals(String.valueOf(categories.get(position).getId()));
  }

}