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

package it.feio.android.omninotes.models.adapters.category;

import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.neopixl.pixlui.components.textview.TextView;
import it.feio.android.omninotes.databinding.DrawerListItemBinding;

public class CategoryViewHolder extends ViewHolder {

  public ImageView imgIcon;
  public TextView txtTitle;
  public android.widget.TextView count;

  public CategoryViewHolder(DrawerListItemBinding binding) {
    super(binding.getRoot());
    imgIcon = binding.icon;
    txtTitle = binding.title;
    count = binding.count;
  }
}