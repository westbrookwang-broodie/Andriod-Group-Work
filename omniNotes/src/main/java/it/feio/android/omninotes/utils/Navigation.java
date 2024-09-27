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

package it.feio.android.omninotes.utils;

import static it.feio.android.omninotes.utils.ConstantsBase.PREF_NAVIGATION;

import android.content.Context;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Category;
import java.util.ArrayList;
import java.util.Arrays;


public class Navigation {

  private Navigation() {
    // hides public constructor
  }

  public static final int NOTES = 0;
  public static final int ARCHIVE = 1;
  public static final int REMINDERS = 2;
  public static final int TRASH = 3;
  public static final int UNCATEGORIZED = 4;
  public static final int CATEGORY = 5;


  /**
   * Returns actual navigation status
   */
  public static int getNavigation() {
    String[] navigationListCodes = OmniNotes.getAppContext().getResources().getStringArray(
        R.array.navigation_list_codes);
    String navigation = getNavigationText();

    if (navigationListCodes[NOTES].equals(navigation)) {
      return NOTES;
    } else if (navigationListCodes[ARCHIVE].equals(navigation)) {
      return ARCHIVE;
    } else if (navigationListCodes[REMINDERS].equals(navigation)) {
      return REMINDERS;
    } else if (navigationListCodes[TRASH].equals(navigation)) {
      return TRASH;
    } else if (navigationListCodes[UNCATEGORIZED].equals(navigation)) {
      return UNCATEGORIZED;
    } else {
      return CATEGORY;
    }
  }


  public static String getNavigationText() {
    Context mContext = OmniNotes.getAppContext();
    String[] navigationListCodes = mContext.getResources()
        .getStringArray(R.array.navigation_list_codes);
    return Prefs.getString(PREF_NAVIGATION, navigationListCodes[0]);
  }


  /**
   * Retrieves category currently shown
   *
   * @return ID of category or null if current navigation is not a category
   */
  public static Long getCategory() {
    return CATEGORY == getNavigation() ? Long.valueOf(Prefs.getString(PREF_NAVIGATION, "")) : null;
  }


  /**
   * Checks if passed parameters is the actual navigation status
   */
  public static boolean checkNavigation(int navigationToCheck) {
    return checkNavigation(new Integer[]{navigationToCheck});
  }


  public static boolean checkNavigation(Integer[] navigationsToCheck) {
    boolean res = false;
    int navigation = getNavigation();
    for (int navigationToCheck : new ArrayList<>(Arrays.asList(navigationsToCheck))) {
      if (navigation == navigationToCheck) {
        res = true;
        break;
      }
    }
    return res;
  }


  /**
   * Checks if passed parameters is the category user is actually navigating in
   */
  public static boolean checkNavigationCategory(Category categoryToCheck) {
    Context mContext = OmniNotes.getAppContext();
    String[] navigationListCodes = mContext.getResources()
        .getStringArray(R.array.navigation_list_codes);
    String navigation = Prefs.getString(PREF_NAVIGATION, navigationListCodes[0]);
    return (categoryToCheck != null && navigation.equals(String.valueOf(categoryToCheck.getId())));
  }

}
