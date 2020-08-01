package com.example.kaido.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.database.NoteDB;
import com.example.kaido.noteapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteContent;
    private TextView txtDateTime, txtLinkURL;
    private ImageView imgDone, imgNote;
    private String selectedColor, selectedImagePath;
    private View viewSubtitleIndicator;

    private LinearLayout layoutLinkURL, layoutDeleteNote;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog alertDialog;
    private AlertDialog alertDialogDeleteNote;

    private Note selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ImageView imgBack = findViewById(R.id.imageBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteSubtitle = findViewById(R.id.inputTextSubNote);
        inputNoteContent = findViewById(R.id.inputNoteContent);
        txtDateTime = findViewById(R.id.textDateTime);
        imgDone = findViewById(R.id.imageDone);
        imgNote = findViewById(R.id.imageNote);
        viewSubtitleIndicator = findViewById(R.id.viewSubTitleIndicator);
        layoutLinkURL = findViewById(R.id.layoutLinkURL);
        txtLinkURL = findViewById(R.id.textLinkURL);

        txtDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );

        imgDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
        selectedColor = "#333333";
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdateNote", false)) {
            selectedNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        findViewById(R.id.imageDeleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgNote.setImageBitmap(null);
                imgNote.setVisibility(View.GONE);
                findViewById(R.id.imageDeleteImage).setVisibility(View.GONE);
                selectedImagePath="";
            }
        });

        findViewById(R.id.imageDeleteLinkWeb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtLinkURL.setText(null);
                layoutLinkURL.setVisibility(View.GONE);
            }
        });
        initMiscellaneous();
        setSubtitleIndicator();
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(selectedNote.getTitle());
        inputNoteSubtitle.setText(selectedNote.getSubtitle());
        txtDateTime.setText(selectedNote.getDateTime());
        inputNoteContent.setText(selectedNote.getNoteContent());
        if(selectedNote.getImagePath() != null && !selectedNote.getImagePath().trim().isEmpty()) {
            imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedNote.getImagePath()));
            imgNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);
            selectedImagePath = selectedNote.getImagePath();
        }

        if(selectedNote.getUrl() != null && !selectedNote.getUrl().trim().isEmpty()) {
            txtLinkURL.setText(selectedNote.getUrl());
            layoutLinkURL.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteLinkWeb).setVisibility(View.VISIBLE);
        }
    }


    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteContent.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note content can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteContent(inputNoteContent.getText().toString());
        note.setDateTime(txtDateTime.getText().toString());
        note.setColor(selectedColor);
        note.setImagePath(selectedImagePath);
        if(layoutLinkURL.getVisibility() == View.VISIBLE) {
            note.setUrl(txtLinkURL.getText().toString());
        }

        if(selectedNote != null) {
            note.setId(selectedNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDB.getNoteDB(getApplicationContext()).noteDAO().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final ImageView imgColor1 = layoutMiscellaneous.findViewById(R.id.imgColor1);
        final ImageView imgColor2 = layoutMiscellaneous.findViewById(R.id.imgColor2);
        final ImageView imgColor3 = layoutMiscellaneous.findViewById(R.id.imgColor3);
        final ImageView imgColor4 = layoutMiscellaneous.findViewById(R.id.imgColor4);
        final ImageView imgColor5 = layoutMiscellaneous.findViewById(R.id.imgColor5);

        layoutMiscellaneous.findViewById(R.id.imgColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = "#333333";
                imgColor1.setImageResource(R.drawable.ic_done);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.imgColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = "#F4BF51";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(R.drawable.ic_done);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.imgColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = "#DF3C37";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(R.drawable.ic_done);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.imgColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = "#2437B8";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(R.drawable.ic_done);
                imgColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.imgColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedColor = "#000000";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicator();
            }
        });

        if(selectedNote != null && selectedNote.getColor() != null && !selectedNote.getColor().trim().isEmpty()) {
            switch (selectedNote.getColor()) {
                case "#F4BF51":
                    layoutMiscellaneous.findViewById(R.id.imgColor2).performClick();
                    break;
                case "#DF3C37" :
                    layoutMiscellaneous.findViewById(R.id.imgColor3).performClick();
                    break;
                case "#2437B8" :
                    layoutMiscellaneous.findViewById(R.id.imgColor4).performClick();
                    break;
                case "#000000" :
                    layoutMiscellaneous.findViewById(R.id.imgColor5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });
        layoutMiscellaneous.findViewById(R.id.layoutAddURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        if (selectedNote != null) {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }



    private void setSubtitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(CreateNoteActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imgNote.setImageBitmap(bitmap);
                    imgNote.setVisibility(View.VISIBLE);
                    selectedImagePath = getImagePath(selectedImage);
                    findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);

                } catch (Exception ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
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

    private void showAddURLDialog() {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layoutAddURLDialog));
            builder.setView(view);

            alertDialog = builder.create();
            if(alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputLinkURL);
            inputURL.requestFocus();
            view.findViewById(R.id.textAddURL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString().trim()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "URL Not Available", Toast.LENGTH_SHORT).show();
                    } else {
                        txtLinkURL.setText(inputURL.getText().toString());
                        layoutLinkURL.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog.show();
    }

    private void showDeleteNoteDialog() {
        if(alertDialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note_dialog,(ViewGroup) findViewById(R.id.layoutDeleteNoteDialog));
            builder.setView(view);
            alertDialogDeleteNote = builder.create();

            if(alertDialogDeleteNote.getWindow() != null) {
                alertDialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDB.getNoteDB(getApplicationContext()).noteDAO().deleteNote(selectedNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textCancelDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogDeleteNote.dismiss();
                }
            });
        }
        alertDialogDeleteNote.show();
    }
}