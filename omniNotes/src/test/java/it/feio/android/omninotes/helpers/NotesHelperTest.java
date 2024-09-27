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

package it.feio.android.omninotes.helpers;

import static it.feio.android.omninotes.utils.ConstantsBase.MERGED_NOTES_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

import android.os.Build.VERSION_CODES;
import it.feio.android.omninotes.BaseUnitTest;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Note;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.MockedStatic;

public class NotesHelperTest extends BaseUnitTest {

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(NotesHelper.class);
  }

  @Test
  public void haveSameIdShouldFail() {
    var note1 = getNote(1L, "test title", "test content");
    var note2 = getNote(2L, "test title", "test content");

    assertFalse(NotesHelper.haveSameId(note1, note2));
  }

  @Test
  public void haveSameIdShouldSucceed() {
    var note1 = getNote(3L, "test title", "test content");
    var note2 = getNote(3L, "different test title", "different test content");

    assertTrue(NotesHelper.haveSameId(note1, note2));
  }

  @Test
  public void mergingNotesDoesntDuplicateFirstTitle() {
    final String FIRST_NOTE_TITLE = "test title 1";
    var note1 = getNote(4L, FIRST_NOTE_TITLE, "");
    var note2 = getNote(5L, "test title 2", "");
    var mergedNote = NotesHelper.mergeNotes(Arrays.asList(note1, note2), false);

    assertFalse(mergedNote.getContent().contains(FIRST_NOTE_TITLE));
  }

  @Test
  public void mergeNotes() {
    int notesNumber = 3;
    var notes = new ArrayList<Note>();
    for (int i = 0; i < notesNumber; i++) {
      Note note = new Note();
      note.setTitle("Merged note " + i + " title");
      note.setContent("Merged note " + i + " content");
      notes.add(note);
    }
    var mergeNote = NotesHelper.mergeNotes(notes, false);

    assertNotNull(mergeNote);
    assertEquals("Merged note 0 title", mergeNote.getTitle());
    assertTrue(mergeNote.getContent().contains("Merged note 0 content"));
    assertTrue(mergeNote.getContent().contains("Merged note 1 content"));
    assertTrue(mergeNote.getContent().contains("Merged note 2 content"));
    assertEquals(StringUtils.countMatches(mergeNote.getContent(), MERGED_NOTES_SEPARATOR), 2);
  }

  @Test
  public void getNoteInfo() {
    var contextMock = getContextMock();
    try (
        MockedStatic<OmniNotes> omniNotes = mockStatic(OmniNotes.class);
        MockedStatic<BuildHelper> buildVersionHelper = mockStatic(BuildHelper.class);
    ) {
      omniNotes.when(OmniNotes::getAppContext).thenReturn(contextMock);
      buildVersionHelper.when(() -> BuildHelper.isAboveOrEqual(VERSION_CODES.N)).thenReturn(true);

      var info = NotesHelper.getNoteInfo(new Note());

      assertEquals(0, info.getChars());
      assertEquals(0, info.getWords());
      assertEquals(0, info.getChecklistCompletedItemsNumber());
    }
  }

}
