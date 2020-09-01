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
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.adapter.NoteAdapter;
import com.example.kaido.noteapp.dao.NoteDAO;
import com.example.kaido.noteapp.database.NoteDB;
import com.example.kaido.noteapp.entities.Note;
import com.example.kaido.noteapp.listeners.NoteListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements NoteListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_GET_NOTES = 2;
    public static final int REQUEST_CODE_UPDATE_NOTE = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;
    public static final int REQUEST_CODE = 123;


    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;

    private int noteSelectedPosition = -1;

    private AlertDialog alertDialogQuickActionLink, alertDialogQuickActionTimeReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ImageView imgAddNote = findViewById(R.id.imageAddNoteMain);
        imgAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) +
                        ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Grant those Permission");
                    builder.setMessage("This app needs some permissions to function properly.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    },
                                    REQUEST_CODE);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    startActivityForResult(
                            new Intent(getApplicationContext(), CreateNoteActivity.class),
                            REQUEST_CODE_ADD_NOTE
                    );
                }
            }
        });

        findViewById(R.id.imageTimeReminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeReminderDialog();

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
        if (alertDialogQuickActionLink == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layoutAddURLDialog));
            builder.setView(view);
            alertDialogQuickActionLink = builder.create();

            if (alertDialogQuickActionLink.getWindow() != null) {
                alertDialogQuickActionLink.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputLinkWeb = view.findViewById(R.id.inputLinkURL);
            inputLinkWeb.requestFocus();
            view.findViewById(R.id.textAddURL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputLinkWeb.getText().toString().trim().isEmpty()) {
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

    private void showTimeReminderDialog() {
        if (alertDialogQuickActionTimeReminder == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_time_reminder, (ViewGroup) findViewById(R.id.layoutAddTimeReminderDialog));
            builder.setView(view);
            alertDialogQuickActionTimeReminder = builder.create();

            if (alertDialogQuickActionTimeReminder.getWindow() != null) {
                alertDialogQuickActionTimeReminder.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final TextView textDateAndTime = view.findViewById(R.id.dateAndTime);
            final Calendar newCalender = Calendar.getInstance();
            view.findViewById(R.id.selectDate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {
                            final Calendar newDate = Calendar.getInstance();
                            Calendar newTime = Calendar.getInstance();
                            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                    newDate.set(year, month, day, hour, minute, 0);
                                    Calendar currentDateTime = Calendar.getInstance();
                                    if (newDate.getTimeInMillis() - currentDateTime.getTimeInMillis() > 0) {
                                        textDateAndTime.setText(newDate.getTime().toString());
                                    } else
                                        Toast.makeText(getApplicationContext(), "Invalid time", Toast.LENGTH_SHORT).show();
                                }
                            }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true);
                            timePickerDialog.show();
                        }
                    }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(Calendar.DAY_OF_MONTH));

                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                    datePickerDialog.show();
                }
            });

            view.findViewById(R.id.textAddTimeReminder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Date timeRemind = new Date(textDateAndTime.getText().toString().trim());
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                    calendar.setTime(timeRemind);
                    calendar.set(Calendar.SECOND, 0);
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isQuickActionNote", true);
                    intent.putExtra("quickActionType", "time_reminder");
                    intent.putExtra("date_time", timeRemind.toString());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    alertDialogQuickActionTimeReminder.dismiss();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogQuickActionTimeReminder.dismiss();
                }
            });
        }
        alertDialogQuickActionTimeReminder.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
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

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDB.getNoteDB(getApplicationContext()).noteDAO().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_GET_NOTES) {
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
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
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
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
    public void onClickedNote(final Note note, int position) {
        noteSelectedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdateNote", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] + grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}