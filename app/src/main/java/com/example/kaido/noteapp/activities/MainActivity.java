package com.example.kaido.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.adapter.NoteAdapter;
import com.example.kaido.noteapp.database.NoteDB;
import com.example.kaido.noteapp.entities.Note;
import com.example.kaido.noteapp.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NoteListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_GET_NOTES = 2;
    public static final int REQUEST_CODE_UPDATE_NOTE = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;

    private int noteSelectedPosition = -1;

    private AlertDialog alertDialogQuickActionLink;

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

        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        findViewById(R.id.imageImageNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });

        findViewById(R.id.imageWebLinkNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLinkWebDialog();
            }
        });
        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);

        getNotes(REQUEST_CODE_GET_NOTES, false);

        EditText inputSearchNote = findViewById(R.id.editTextSearch);
        inputSearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                noteAdapter.timerCancel();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0) {
                    noteAdapter.searchNote(editable.toString());
                }
            }
        });
    }

    private void showLinkWebDialog() {
        if(alertDialogQuickActionLink == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layoutAddURLDialog));
            builder.setView(view);
            alertDialogQuickActionLink = builder.create();

            if(alertDialogQuickActionLink.getWindow() != null) {
                alertDialogQuickActionLink.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputLinkWeb = view.findViewById(R.id.inputLinkURL);
            inputLinkWeb.requestFocus();
            view.findViewById(R.id.textAddURL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inputLinkWeb.getText().toString().trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputLinkWeb.getText().toString().trim()).matches()) {
                        Toast.makeText(MainActivity.this, "URL Not Available", Toast.LENGTH_SHORT).show();
                    } else {
                        alertDialogQuickActionLink.dismiss();
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isQuickActionNote", true);
                        intent.putExtra("quickActionType", "web_link");
                        intent.putExtra("url", inputLinkWeb.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    }
                }
            });
        }
        alertDialogQuickActionLink.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }

    }

    private String getImagePath(Uri uri) {
        String imagePath;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            imagePath = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            imagePath = cursor.getString(index);
            cursor.close();
        }
        return imagePath;
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
                    if (isNoteDeleted) {
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
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode ==RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        String selectedImagePath = getImagePath(selectedImage);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isQuickActionNote", true);
                        intent.putExtra("quickActionType", "images");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}