package com.example.kaido.noteapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.adapter.NoteAdapter;
import com.example.kaido.noteapp.database.NoteDB;
import com.example.kaido.noteapp.entities.Note;
import com.example.kaido.noteapp.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_GET_NOTES = 2;
    public static final int REQUEST_CODE_UPDATE_NOTE = 3;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;

    private int noteSelectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ImageView imgAddNote = findViewById(R.id.imageAddNoteMain);
        imgAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });
        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_GET_NOTES, false);
    }

    private void getNotes(final int requsetCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDB.getNoteDB(getApplicationContext()).noteDAO().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requsetCode == REQUEST_CODE_GET_NOTES) {
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                } else if (requsetCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                } else if (requsetCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteSelectedPosition);
                    if(isNoteDeleted) {
                        noteAdapter.notifyItemRemoved(noteSelectedPosition);
                    } else {
                        noteList.add(noteSelectedPosition, notes.get(noteSelectedPosition));
                        noteAdapter.notifyItemChanged(noteSelectedPosition);
                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

    @Override
    public void onClickedNote(Note note, int positon) {
        noteSelectedPosition = positon;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdateNote", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
}