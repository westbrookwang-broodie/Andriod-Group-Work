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

package it.feio.android.omninotes.models.misc;


import static java.lang.Boolean.FALSE;

import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.DynamicNavigationReadyEvent;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Note;
import java.util.List;
import lombok.Getter;


@Getter
public class DynamicNavigationLookupTable {

  private static DynamicNavigationLookupTable instance;
  private int archived;
  private int trashed;
  private int uncategorized;
  private int reminders;


  private DynamicNavigationLookupTable() {
    EventBus.getDefault().register(this);
    update();
  }


  public static DynamicNavigationLookupTable getInstance() {
    if (instance == null) {
      instance = new DynamicNavigationLookupTable();
    }
    return instance;
  }


  public void update() {
    archived = trashed = uncategorized = reminders = 0;
    List<Note> notes = DbHelper.getInstance().getAllNotes(false);
    for (int i = 0; i < notes.size(); i++) {
      if (Boolean.TRUE.equals(notes.get(i).isTrashed())) {
        trashed++;
      } else if (Boolean.TRUE.equals(notes.get(i).isArchived())) {
        archived++;
      } else if (notes.get(i).getAlarm() != null) {
        reminders++;
      }
      if ((notes.get(i).getCategory() == null || notes.get(i).getCategory().getId().equals(0L))
          && FALSE.equals(notes.get(i).isTrashed())) {
        uncategorized++;
      }
    }
    EventBus.getDefault().post(new DynamicNavigationReadyEvent());
    LogDelegate.d("Dynamic menu finished counting items");
  }

  public void onEventAsync(NotesUpdatedEvent event) {
    update();
  }

}
