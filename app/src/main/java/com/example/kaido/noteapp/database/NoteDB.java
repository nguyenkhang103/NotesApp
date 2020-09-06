package com.example.kaido.noteapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.example.kaido.noteapp.dao.NoteDAO;
import com.example.kaido.noteapp.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NoteDB extends RoomDatabase {
    public static NoteDB noteDB;

    public static synchronized NoteDB getNoteDB(Context context) {
        if (noteDB == null) {
            noteDB = Room.databaseBuilder(context, NoteDB.class, "notes_db").fallbackToDestructiveMigration().build();
        }
        return noteDB;
    }

    public abstract NoteDAO noteDAO();
}
