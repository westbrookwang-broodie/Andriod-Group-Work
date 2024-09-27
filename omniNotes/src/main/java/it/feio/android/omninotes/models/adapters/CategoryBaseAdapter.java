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

import static it.feio.android.omninotes.databinding.DrawerListItemBinding.inflate;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_NAVIGATION;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.adapters.category.CategoryViewHolder;
import java.util.List;


public class CategoryBaseAdapter extends BaseAdapter {

  private final Activity mActivity;
  private final int layout;
  private final List<Category> categories;
  private final LayoutInflater inflater;
  private final String navigationTmp;

  public CategoryBaseAdapter(Activity mActivity, List<Category> categories) {
    this(mActivity, categories, null);
  }

  public CategoryBaseAdapter(Activity mActivity, List<Category> categories, String navigationTmp) {
    this.mActivity = mActivity;
    this.layout = R.layout.drawer_list_item;
    this.categories = categories;
    this.navigationTmp = navigationTmp;
    inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return categories.size();
  }

  @Override
  public Object getItem(int position) {
    return categories.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    var category = categories.get(position);

    CategoryViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(layout, parent, false);

      holder = new CategoryViewHolder(
          inflate(LayoutInflater.from(parent.getContext()), parent, false));

      holder.imgIcon = convertView.findViewById(R.id.icon);
      holder.txtTitle = convertView.findViewById(R.id.title);
      holder.count = convertView.findViewById(R.id.count);
      convertView.setTag(holder);
    } else {
      holder = (CategoryViewHolder) convertView.getTag();
    }

    // Set the results into TextViews
    holder.txtTitle.setText(category.getName());

    if (isSelected(position)) {
      holder.txtTitle.setTypeface(null, Typeface.BOLD);
      holder.txtTitle.setTextColor(Integer.parseInt(category.getColor()));
    } else {
      holder.txtTitle.setTypeface(null, Typeface.NORMAL);
      holder.txtTitle.setTextColor(mActivity.getResources().getColor(R.color.drawer_text));
    }

    // Set the results into ImageView checking if an icon is present before
    if (category.getColor() != null && category.getColor().length() > 0) {
      var img = mActivity.getResources().getDrawable(R.drawable.ic_folder_special_black_24dp);
      var cf = new LightingColorFilter(Color.parseColor("#000000"),
          Integer.parseInt(category.getColor()));
      img.mutate().setColorFilter(cf);
      holder.imgIcon.setImageDrawable(img);
      int padding = 4;
      holder.imgIcon.setPadding(padding, padding, padding, padding);
    }

    // Sets category count if set in preferences
    if (Prefs.getBoolean("settings_show_category_count", true)) {
      holder.count.setText(String.valueOf(category.getCount()));
      holder.count.setVisibility(View.VISIBLE);
    }

    return convertView;
  }

  private boolean isSelected(int position) {
    // Getting actual navigation selection
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
