package com.example.kaido.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.database.NoteDB;
import com.example.kaido.noteapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteContent;
    private TextView txtDateTime, txtLinkURL, txtTimeReminder;
    private ImageView imgNote, imageEditText;
    private String selectedColor, selectedImagePath, selectedTextColor;
    private View viewSubtitleIndicator;
    private boolean isBold, isItalic, isUnderline;

    private LinearLayout layoutLinkURL, layoutDeleteNote, layoutTimeReminder;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog alertDialog, alertDialogTimeReminder;
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
        ImageView imgDone = findViewById(R.id.imageDone);
        imgNote = findViewById(R.id.imageNote);
        viewSubtitleIndicator = findViewById(R.id.viewSubTitleIndicator);
        layoutLinkURL = findViewById(R.id.layoutLinkURL);
        layoutTimeReminder = findViewById(R.id.layoutTimeReminder);
        txtLinkURL = findViewById(R.id.textLinkURL);
        txtTimeReminder = findViewById(R.id.textTimeReminder);
        imageEditText = findViewById(R.id.imageEditTextStyle);
        inputNoteTitle.setTextSize(25);

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

        isBold = false;
        isItalic = false;
        isUnderline = false;

        if (getIntent().getBooleanExtra("isViewOrUpdateNote", false)) {
            selectedNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
        findViewById(R.id.imageDeleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgNote.setImageBitmap(null);
                imgNote.setVisibility(View.GONE);
                findViewById(R.id.imageDeleteImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        findViewById(R.id.imageDeleteLinkWeb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtLinkURL.setText(null);
                layoutLinkURL.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imageDeleteTimeReminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtTimeReminder.setText(null);
                layoutTimeReminder.setVisibility(View.GONE);
            }
        });
        if (getIntent().getBooleanExtra("isQuickActionNote", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("images")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imgNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);
                } else if (type.equals("web_link")) {
                    txtLinkURL.setText(getIntent().getStringExtra("url"));
                    layoutLinkURL.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageDeleteLinkWeb).setVisibility(View.VISIBLE);
                } else if (type.equals("time_reminder")) {
                    txtTimeReminder.setText(getIntent().getStringExtra("date_time"));
                    layoutTimeReminder.setVisibility(View.VISIBLE);
                    findViewById(R.id.imageDeleteTimeReminder).setVisibility(View.VISIBLE);
                }
            }
        }
        inputNoteTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                imageEditText.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                imageEditText.setVisibility(View.VISIBLE);
                imageEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initTextStyleLayout(inputNoteTitle);
                    }
                });
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
        if (selectedNote.getImagePath() != null && !selectedNote.getImagePath().trim().isEmpty()) {
            imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedNote.getImagePath()));
            imgNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);
            selectedImagePath = selectedNote.getImagePath();
        }

        if (selectedNote.getUrl() != null && !selectedNote.getUrl().trim().isEmpty()) {
            txtLinkURL.setText(selectedNote.getUrl());
            layoutLinkURL.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteLinkWeb).setVisibility(View.VISIBLE);
        }
        if (selectedNote.getTimeReminder() != null && !selectedNote.getTimeReminder().toString().trim().isEmpty()) {
            txtTimeReminder.setText(selectedNote.getTimeReminder().toString());
            layoutTimeReminder.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteTimeReminder).setVisibility(View.VISIBLE);
        }
    }


    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteContent(inputNoteContent.getText().toString());
        note.setDateTime(txtDateTime.getText().toString());
        note.setColor(selectedColor);
        note.setImagePath(selectedImagePath);


        if (layoutLinkURL.getVisibility() == View.VISIBLE) {
            note.setUrl(txtLinkURL.getText().toString());
        }
        if (layoutTimeReminder.getVisibility() == View.VISIBLE) {
            Date timeRemind = new Date(txtTimeReminder.getText().toString().trim());
            note.setTimeReminder(timeRemind);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            calendar.setTime(timeRemind);
            calendar.set(Calendar.SECOND, 0);
            Intent intent = new Intent(CreateNoteActivity.this, NotifierAlarmActivity.class);
            intent.putExtra("note title", note.getTitle());
            intent.putExtra("time reminder", note.getTimeReminder().toString());
            intent.putExtra("id", note.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(CreateNoteActivity.this, note.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        if (selectedNote != null) {
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

    private void initTextStyleLayout(final EditText editText) {
        final LinearLayout layoutTextStyle = findViewById(R.id.layout_text_style);
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior2 = BottomSheetBehavior.from(layoutTextStyle);
        layoutMiscellaneous.setVisibility(View.GONE);
        layoutTextStyle.setVisibility(View.VISIBLE);
        bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
        layoutTextStyle.findViewById(R.id.imageBackText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutTextStyle.setVisibility(View.GONE);
                layoutMiscellaneous.setVisibility(View.VISIBLE);
                initMiscellaneous();
            }
        });
        final ImageView imgColor1 = layoutTextStyle.findViewById(R.id.imgColor1);
        final ImageView imgColor2 = layoutTextStyle.findViewById(R.id.imgColor2);
        final ImageView imgColor3 = layoutTextStyle.findViewById(R.id.imgColor3);
        final ImageView imgColor4 = layoutTextStyle.findViewById(R.id.imgColor4);
        final ImageView imgColor5 = layoutTextStyle.findViewById(R.id.imgColor5);
        final ImageView imgBold = layoutTextStyle.findViewById(R.id.imageBold);
        final ImageView imgItalic = layoutTextStyle.findViewById(R.id.imageItalic);
        final ImageView imgUnderline = layoutTextStyle.findViewById(R.id.imageUnderline);


        layoutTextStyle.findViewById(R.id.imgColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.WHITE);
                imgColor1.setImageResource(R.drawable.ic_done);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
            }
        });
        layoutTextStyle.findViewById(R.id.imgColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#F4BF51"));
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(R.drawable.ic_done);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
            }
        });

        layoutTextStyle.findViewById(R.id.imgColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#DF3C37"));
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(R.drawable.ic_done);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
            }
        });

        layoutTextStyle.findViewById(R.id.imgColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#2437B8"));
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(R.drawable.ic_done);
                imgColor5.setImageResource(0);
            }
        });

        layoutTextStyle.findViewById(R.id.imgColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#7B7B7B"));
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);
            }
        });

        layoutTextStyle.findViewById(R.id.imageBold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBold = !isBold;
                if (isBold && isItalic) {
                    editText.setTypeface(null, Typeface.BOLD_ITALIC);
                    imgBold.setColorFilter(Color.parseColor("#000000"));
                } else if (isBold) {
                    editText.setTypeface(null, Typeface.BOLD);
                    imgBold.setColorFilter(Color.parseColor("#000000"));
                } else if(isItalic) {
                    editText.setTypeface(null,Typeface.ITALIC);
                    imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                } else {
                    editText.setTypeface(Typeface.DEFAULT);
                    imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                }

            }
        });
        layoutTextStyle.findViewById(R.id.imageItalic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isItalic = !isItalic;
                if (isItalic && isBold) {
                    editText.setTypeface(null, Typeface.BOLD_ITALIC);
                    imgItalic.setColorFilter(Color.parseColor("#000000"));
                } else if (isItalic) {
                    editText.setTypeface(null, Typeface.ITALIC);
                    imgItalic.setColorFilter(Color.parseColor("#000000"));

                } else if (isBold){
                    editText.setTypeface(null,Typeface.BOLD);
                    imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));

                } else {
                    editText.setTypeface(Typeface.DEFAULT);
                    imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
                }

            }
        });
        layoutTextStyle.findViewById(R.id.imageUnderline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUnderline = !isUnderline;
                if (isUnderline) {
                    editText.setPaintFlags(editText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    imgUnderline.setColorFilter(Color.parseColor("#000000"));
                } else {
                    editText.setPaintFlags(0);
                    imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                }

            }
        });
        Spinner spinnerFontSize =  layoutTextStyle.findViewById(R.id.spinnerFontSize);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.font_sizes, R.layout.layout_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.layout_dropdown_spinner_item);
        spinnerFontSize.setAdapter(arrayAdapter);
        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().toString().equals("Small")) {
                    editText.setTextSize(15);
                } else if (adapterView.getSelectedItem().toString().equals("Large")){
                    editText.setTextSize(35);
                } else {
                    editText.setTextSize(25);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

        if (selectedNote != null && selectedNote.getColor() != null && !selectedNote.getColor().trim().isEmpty()) {
            switch (selectedNote.getColor()) {
                case "#F4BF51":
                    layoutMiscellaneous.findViewById(R.id.imgColor2).performClick();
                    break;
                case "#DF3C37":
                    layoutMiscellaneous.findViewById(R.id.imgColor3).performClick();
                    break;
                case "#2437B8":
                    layoutMiscellaneous.findViewById(R.id.imgColor4).performClick();
                    break;
                case "#000000":
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

        layoutMiscellaneous.findViewById(R.id.layoutAddTimeReminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddTimeReminderDialog();
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
            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputLinkURL);
            inputURL.requestFocus();
            view.findViewById(R.id.textAddURL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString().trim()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "URL Not Available", Toast.LENGTH_SHORT).show();
                    } else {
                        txtLinkURL.setText(inputURL.getText().toString());
                        layoutLinkURL.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageDeleteLinkWeb).setVisibility(View.VISIBLE);
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
        if (alertDialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note_dialog, (ViewGroup) findViewById(R.id.layoutDeleteNoteDialog));
            builder.setView(view);
            alertDialogDeleteNote = builder.create();

            if (alertDialogDeleteNote.getWindow() != null) {
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
                            setResult(RESULT_OK, intent);
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

    private void showAddTimeReminderDialog() {
        if (alertDialogTimeReminder == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_time_reminder, (ViewGroup) findViewById(R.id.layoutAddTimeReminderDialog));
            builder.setView(view);
            alertDialogTimeReminder = builder.create();

            if (alertDialogTimeReminder.getWindow() != null) {
                alertDialogTimeReminder.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final TextView textDateAndTime = view.findViewById(R.id.dateAndTime);
            final Calendar newCalender = Calendar.getInstance();
            view.findViewById(R.id.selectDate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(CreateNoteActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {
                            final Calendar newDate = Calendar.getInstance();
                            Calendar newTime = Calendar.getInstance();
                            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                    newDate.set(year, month, day, hour, minute, 0);
                                    Calendar currentDateTime = Calendar.getInstance();
                                    if (newDate.getTimeInMillis() - currentDateTime.getTimeInMillis() > 0) {
                                        textDateAndTime.setText(newDate.getTime().toString());
                                    } else
                                        Toast.makeText(CreateNoteActivity.this, "Invalid time", Toast.LENGTH_SHORT).show();
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
                    SimpleDateFormat sdf3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    Date timeRemind = null;
                    try {
                        timeRemind = sdf3.parse(textDateAndTime.getText().toString().trim());
                        assert timeRemind != null;
                        txtTimeReminder.setText(timeRemind.toString());
                        Log.w("New time", timeRemind + "");
                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                        calendar.setTime(timeRemind);
                        calendar.set(Calendar.SECOND, 0);
                        findViewById(R.id.layoutTimeReminder).setVisibility(View.VISIBLE);
                        alertDialogTimeReminder.dismiss();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialogTimeReminder.dismiss();
                }
            });
        }
        alertDialogTimeReminder.show();
    }

}