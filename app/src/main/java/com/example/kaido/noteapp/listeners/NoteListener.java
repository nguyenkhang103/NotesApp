package com.example.kaido.noteapp.listeners;

import com.example.kaido.noteapp.entities.Note;

public interface NoteListener {
    void onClickedNote(Note note, int positon);
}
