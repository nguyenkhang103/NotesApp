package com.example.kaido.noteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
import android.media.ResourceBusyException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
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
    private ImageView imgNote, imageEditText, imageEditTextContent, imgDone;
    private String selectedColor, titleFontFamily, contentFontFamily;
    private String selectedImagePath;
    private int selectedTextColor, selectedContentColor;
    private View viewSubtitleIndicator;
    private boolean isBold, isItalic, isUnderline, isLeft, isCenter, isRight;
    private boolean isBoldContent, isItalicContent, isUnderlineContent, isLeftContent, isCenterContent, isRightContent;

    private LinearLayout layoutLinkURL, layoutDeleteNote, layoutTimeReminder;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private AlertDialog alertDialog, alertDialogTimeReminder;
    private AlertDialog alertDialogDeleteNote;

    private Note selectedNote;
    private int titleTextSize, contentTextSize;
    private String titleAlgin, contentAlign;
    private Date timeReminder;
    private boolean isNotified;

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
        layoutTimeReminder = findViewById(R.id.layoutTimeReminder);
        txtLinkURL = findViewById(R.id.textLinkURL);
        txtTimeReminder = findViewById(R.id.textTimeReminder);
        imageEditText = findViewById(R.id.imageEditTextStyle);
        imageEditTextContent = findViewById(R.id.imageEditTextStyleContent);
        inputNoteTitle.setTextSize(25);
        inputNoteTitle.setGravity(Gravity.START);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_bold);
        inputNoteTitle.setTypeface(typeface);
        inputNoteContent.setTextSize(15);
        inputNoteContent.setGravity(Gravity.START);
        Typeface typefaceContent = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_regular);
        inputNoteContent.setTypeface(typefaceContent);
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
        isLeft = true;
        isCenter = false;
        isRight = false;
        titleAlgin = "START";
        isBoldContent = false;
        isItalicContent = false;
        isUnderlineContent = false;
        isLeftContent = true;
        isCenterContent = false;
        isRightContent = false;
        contentAlign = "START";
        if (getIntent().getBooleanExtra("isViewOrUpdateNote", false)) {
            selectedNote = (Note) getIntent().getSerializableExtra("note");
            isNotified = getIntent().getBooleanExtra("isNotifiedUpdate",false);
            setViewOrUpdateNote();
        }
        findViewById(R.id.imageDeleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgNote.setImageBitmap(null);
                imgNote.setVisibility(View.GONE);
                imgDone.setVisibility(View.VISIBLE);
                findViewById(R.id.imageDeleteImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        findViewById(R.id.imageDeleteLinkWeb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtLinkURL.setText(null);
                imgDone.setVisibility(View.VISIBLE);
                layoutLinkURL.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imageDeleteTimeReminder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtTimeReminder.setText(null);
                imgDone.setVisibility(View.VISIBLE);
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

        inputNoteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                imageEditTextContent.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                imageEditTextContent.setVisibility(View.VISIBLE);
                imageEditTextContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initTextStyleContentLayout(inputNoteContent);
                    }
                });
            }
        });
        initMiscellaneous();
        setSubtitleIndicator();
    }

    private void setViewOrUpdateNote() {

        Typeface typeface, typefaceContent;
        // title
        imgDone.setVisibility(View.GONE);
        inputNoteTitle.setText(selectedNote.getTitle());
        if (selectedNote != null && !selectedNote.getTitle().trim().isEmpty()) {
            imageEditText.setVisibility(View.VISIBLE);
            imageEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initTextStyleLayout(inputNoteTitle);
                    imgDone.setVisibility(View.VISIBLE);
                }
            });

        }
        inputNoteTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().equals(selectedNote.getTitle())) {
                    imgDone.setVisibility(View.VISIBLE);
                }
            }
        });
        inputNoteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().equals(selectedNote.getNoteContent())) {
                    imgDone.setVisibility(View.VISIBLE);
                }
            }
        });
        inputNoteSubtitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().equals(selectedNote.getSubtitle())) {
                    imgDone.setVisibility(View.VISIBLE);
                }
            }
        });

        if (selectedNote != null && selectedNote.getTitleColor() != 0) {
            inputNoteTitle.setTextColor(selectedNote.getTitleColor());
        } else {
            inputNoteTitle.setTextColor(Color.WHITE);
        }

        assert selectedNote != null;
        switch (selectedNote.getTitleFontFamily()) {
            case "Harmonia":
                if (selectedNote.isTitleBold() && selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.harmonia_bold_italic);
                } else if (selectedNote.isTitleBold()) {
                    typeface = ResourcesCompat.getFont(this, R.font.harmonia_bold);
                } else if (selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.harmonia_italic);
                } else {
                    typeface = ResourcesCompat.getFont(this, R.font.harmonia_regular);
                }
                inputNoteTitle.setTypeface(typeface);
                break;
            case "OpenSans":
                if (selectedNote.isTitleBold() && selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.opensans_bold_italic);
                } else if (selectedNote.isTitleBold()) {
                    typeface = ResourcesCompat.getFont(this, R.font.opensans_bold);
                } else if (selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.opensans_italic);
                } else {
                    typeface = ResourcesCompat.getFont(this, R.font.opensans_regular);
                }
                inputNoteTitle.setTypeface(typeface);
                break;
            case "Roboto":
                if (selectedNote.isTitleBold() && selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.roboto_bold_italic);
                } else if (selectedNote.isTitleBold()) {
                    typeface = ResourcesCompat.getFont(this, R.font.roboto_bold);
                } else if (selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.roboto_italic);
                } else {
                    typeface = ResourcesCompat.getFont(this, R.font.roboto_regular);
                }
                inputNoteTitle.setTypeface(typeface);
                break;
            default:
                if (selectedNote.isTitleBold() && selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.ubuntu_bold_italic);
                } else if (selectedNote.isTitleBold()) {
                    typeface = ResourcesCompat.getFont(this, R.font.ubuntu_bold);
                } else if (selectedNote.isTitleItalic()) {
                    typeface = ResourcesCompat.getFont(this, R.font.ubuntu_italic);
                } else {
                    typeface = ResourcesCompat.getFont(this, R.font.ubuntu_regular);
                }
                inputNoteTitle.setTypeface(typeface);
                break;
        }

        if (selectedNote.getTitleFontSize() == 15) {
            inputNoteTitle.setTextSize(15);
        } else if (selectedNote.getTitleFontSize() == 35) {
            inputNoteTitle.setTextSize(35);
        } else {
            inputNoteTitle.setTextSize(25);
        }
        SpannableString content = new SpannableString(selectedNote.getTitle());


        if (selectedNote.isTitleBold() && selectedNote.isTitleItalic()) {
            content.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, content.length(), 0);
        } else if (selectedNote.isTitleBold() && !selectedNote.isTitleItalic()) {
            content.setSpan(new StyleSpan(Typeface.BOLD), 0, content.length(), 0);
        } else {
            content.setSpan(new StyleSpan(Typeface.ITALIC), 0, content.length(), 0);
        }
        inputNoteTitle.setText(content);

        if (selectedNote.isTitleUnderLined()) {
            inputNoteTitle.setPaintFlags(inputNoteTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        if (selectedNote.getTitleAlign() == null || selectedNote.getTitleAlign().equals("START")) {
            inputNoteTitle.setGravity(Gravity.START);
        } else if (selectedNote.getTitleAlign().equals("CENTER")) {
            inputNoteTitle.setGravity(Gravity.CENTER);
        } else {
            inputNoteTitle.setGravity(Gravity.END);
        }
        //subtitle
        inputNoteSubtitle.setText(selectedNote.getSubtitle());
        //datetime
        txtDateTime.setText(selectedNote.getDateTime());

        //content
        inputNoteContent.setText(selectedNote.getNoteContent());
        if (selectedNote != null && !selectedNote.getNoteContent().trim().isEmpty()) {
            imageEditTextContent.setVisibility(View.VISIBLE);
            imageEditTextContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initTextStyleContentLayout(inputNoteContent);
                }
            });
        }

        if (selectedNote != null && selectedNote.getContentColor() != 0) {
            inputNoteContent.setTextColor(selectedNote.getContentColor());
        } else {
            inputNoteContent.setTextColor(Color.WHITE);
        }

        assert selectedNote != null;
        switch (selectedNote.getContentFontFamily()) {
            case "Harmonia":
                if (selectedNote.isContentBold() && selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.harmonia_bold_italic);
                } else if (selectedNote.isContentBold()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.harmonia_bold);
                } else if (selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.harmonia_italic);
                } else {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.harmonia_regular);
                }
                inputNoteTitle.setTypeface(typefaceContent);
                break;
            case "OpenSans":
                if (selectedNote.isContentBold() && selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.opensans_bold_italic);
                } else if (selectedNote.isContentBold()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.opensans_bold);
                } else if (selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.opensans_italic);
                } else {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.opensans_regular);
                }
                inputNoteTitle.setTypeface(typefaceContent);
                break;
            case "Roboto":
                if (selectedNote.isContentBold() && selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.roboto_bold_italic);
                } else if (selectedNote.isContentBold()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.roboto_bold);
                } else if (selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.roboto_italic);
                } else {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.roboto_regular);
                }
                inputNoteTitle.setTypeface(typefaceContent);
                break;
            default:
                if (selectedNote.isContentBold() && selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.ubuntu_bold_italic);
                } else if (selectedNote.isContentBold()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.ubuntu_bold);
                } else if (selectedNote.isContentItalic()) {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.ubuntu_italic);
                } else {
                    typefaceContent = ResourcesCompat.getFont(this, R.font.ubuntu_regular);
                }
                inputNoteTitle.setTypeface(typefaceContent);
                break;
        }

        if (selectedNote.getContentFontSize() == 15) {
            inputNoteContent.setTextSize(15);
        } else if (selectedNote.getContentFontSize() == 35) {
            inputNoteContent.setTextSize(35);
        } else {
            inputNoteContent.setTextSize(25);
        }
        if (!selectedNote.getNoteContent().trim().isEmpty()) {
            SpannableString contentNote = new SpannableString(selectedNote.getNoteContent());
            if (selectedNote.isContentBold() && selectedNote.isContentItalic()) {
                contentNote.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, contentNote.length(), 0);
            } else if (selectedNote.isContentBold() && !selectedNote.isContentItalic()) {
                contentNote.setSpan(new StyleSpan(Typeface.BOLD), 0, contentNote.length(), 0);
            } else if (!selectedNote.isContentBold() && selectedNote.isContentItalic()) {
                contentNote.setSpan(new StyleSpan(Typeface.ITALIC), 0, contentNote.length(), 0);
            } else if (!selectedNote.isContentBold() && !selectedNote.isContentItalic()) {
                contentNote.setSpan(new StyleSpan(Typeface.NORMAL), 0, contentNote.length(),0);
            }
            inputNoteContent.setText(contentNote);
        }


        if (selectedNote.isContentUnderLined()) {
            inputNoteContent.setPaintFlags(inputNoteContent.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        if (selectedNote.getContentAlign() == null || selectedNote.getContentAlign().equals("START")) {
            inputNoteContent.setGravity(Gravity.START);
        } else if (selectedNote.getContentAlign().equals("CENTER")) {
            inputNoteContent.setGravity(Gravity.CENTER);
        } else {
            inputNoteContent.setGravity(Gravity.END);
        }
        //image
        if (selectedNote.getImagePath() != null && !selectedNote.getImagePath().trim().isEmpty()) {
            imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedNote.getImagePath()));
            imgNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteImage).setVisibility(View.VISIBLE);
            selectedImagePath = selectedNote.getImagePath();
        }
        //url
        if (selectedNote.getUrl() != null && !selectedNote.getUrl().trim().isEmpty()) {
            txtLinkURL.setText(selectedNote.getUrl());
            layoutLinkURL.setVisibility(View.VISIBLE);
            findViewById(R.id.imageDeleteLinkWeb).setVisibility(View.VISIBLE);
        }
        //time reminder
        if (selectedNote.getTimeReminder() != null && !selectedNote.getTimeReminder().toString().trim().isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf3 = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a");
            Date timeRemind = new Date(selectedNote.getTimeReminder().toString().trim());
//            txtTimeReminder.setText(sdf3.format(timeRemind));
            layoutTimeReminder.setVisibility(View.VISIBLE);
            String tmp = sdf3.format(timeRemind);
            Log.d("isNotifiedUpdate1",isNotified+"");
            if(isNotified) {
                Log.d("text",txtTimeReminder.getText().toString());
                SpannableStringBuilder ssBuilder = new SpannableStringBuilder(tmp);
                StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
                ssBuilder.setSpan(
                        strikethroughSpan,
                        0,
                        tmp.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                txtTimeReminder.setText(ssBuilder);
                findViewById(R.id.imageDeleteTimeReminder).setVisibility(View.GONE);
            } else {
                txtTimeReminder.setText(tmp);
                findViewById(R.id.imageDeleteTimeReminder).setVisibility(View.VISIBLE);
            }
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
        //title
        note.setTitleColor(selectedTextColor);
        if (titleFontFamily == null || titleFontFamily.trim().isEmpty()) {
            note.setTitleFontFamily("Ubuntu");
        } else {
            note.setTitleFontFamily(titleFontFamily);
        }
        note.setTitleFontSize(titleTextSize);
        note.setTitleBold(isBold);
        note.setTitleItalic(isItalic);
        note.setTitleUnderLined(isUnderline);
        note.setTitleAlign(titleAlgin);
        //content
        note.setContentColor(selectedContentColor);
        if (contentFontFamily == null || contentFontFamily.trim().isEmpty()) {
            note.setContentFontFamily("Ubuntu");
        } else {
            note.setContentFontFamily(contentFontFamily);
        }
        note.setContentFontSize(contentTextSize);
        note.setContentBold(isBoldContent);
        note.setContentItalic(isItalicContent);
        note.setContentUnderLined(isUnderlineContent);
        note.setContentAlign(contentAlign);


        if (layoutLinkURL.getVisibility() == View.VISIBLE) {
            note.setUrl(txtLinkURL.getText().toString());
        }
        if (layoutTimeReminder.getVisibility() == View.VISIBLE) {
            Date timeRemind = new Date(timeReminder.toString());
            Log.d("TIME REMINDER", timeRemind.toString());
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
            layoutTimeReminder.setVisibility(View.GONE);
        }

        if (selectedNote != null) {
            note.setId(selectedNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDB.getNoteDB(getApplicationContext()).noteDAO().insertNote(note);
                Log.d("NOTE", note + "");
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

    private void initTextStyleContentLayout(final EditText editText) {
        final LinearLayout layoutContentStyle = findViewById(R.id.layout_content_style);
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final LinearLayout layoutTextStyle = findViewById(R.id.layout_text_style);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior2 = BottomSheetBehavior.from(layoutContentStyle);
        layoutMiscellaneous.setVisibility(View.GONE);
        layoutTextStyle.setVisibility(View.GONE);
        layoutContentStyle.setVisibility(View.VISIBLE);
        bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
        layoutContentStyle.findViewById(R.id.imageBackText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutContentStyle.setVisibility(View.GONE);
                layoutTextStyle.setVisibility(View.GONE);
                layoutMiscellaneous.setVisibility(View.VISIBLE);
                initMiscellaneous();
            }
        });

        layoutContentStyle.findViewById(R.id.textContentStyle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior2.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        final ImageView imgColor1 = layoutContentStyle.findViewById(R.id.imgColor1);
        final ImageView imgColor2 = layoutContentStyle.findViewById(R.id.imgColor2);
        final ImageView imgColor3 = layoutContentStyle.findViewById(R.id.imgColor3);
        final ImageView imgColor4 = layoutContentStyle.findViewById(R.id.imgColor4);
        final ImageView imgColor5 = layoutContentStyle.findViewById(R.id.imgColor5);
        final ImageView imgBold = layoutContentStyle.findViewById(R.id.imageBold);
        final ImageView imgItalic = layoutContentStyle.findViewById(R.id.imageItalic);
        final ImageView imgUnderline = layoutContentStyle.findViewById(R.id.imageUnderline);
        final ImageView imgAlignLeft = layoutContentStyle.findViewById(R.id.imageAlignLeft);
        final ImageView imgAlignCenter = layoutContentStyle.findViewById(R.id.imageAlignCenter);
        final ImageView imgAlignRight = layoutContentStyle.findViewById(R.id.imageAlignRight);

        if (editText.getGravity() == 8388659) {
            Log.d("START", true + "");
            imgAlignLeft.setColorFilter(Color.parseColor("#000000"));
        }

        layoutContentStyle.findViewById(R.id.imgColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.WHITE);
                selectedContentColor = Color.WHITE;
                imgColor1.setImageResource(R.drawable.ic_done);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
            }
        });
        layoutContentStyle.findViewById(R.id.imgColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#F4BF51"));
                selectedContentColor = Color.parseColor("#F4BF51");
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(R.drawable.ic_done);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
            }
        });

        layoutContentStyle.findViewById(R.id.imgColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#DF3C37"));
                selectedContentColor = Color.parseColor("#DF3C37");
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(R.drawable.ic_done);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);
            }
        });

        layoutContentStyle.findViewById(R.id.imgColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#2437B8"));
                selectedContentColor = Color.parseColor("#2437B8");
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(R.drawable.ic_done);
                imgColor5.setImageResource(0);
            }
        });

        layoutContentStyle.findViewById(R.id.imgColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.parseColor("#7B7B7B"));
                selectedContentColor = Color.parseColor("#7B7B7B");
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);
            }
        });

        if (selectedNote != null && selectedNote.getContentColor() != 0) {
            if (selectedNote.getContentColor() == Color.parseColor("#F4BF51")) {
                layoutContentStyle.findViewById(R.id.imgColor2).performClick();
            } else if (selectedNote.getContentColor() == Color.parseColor("#DF3C37")) {
                layoutContentStyle.findViewById(R.id.imgColor3).performClick();
            } else if (selectedNote.getContentColor() == Color.parseColor("#2437B8")) {
                layoutContentStyle.findViewById(R.id.imgColor4).performClick();
            } else if (selectedNote.getContentColor() == Color.parseColor("#7B7B7B")) {
                layoutContentStyle.findViewById(R.id.imgColor5).performClick();
            }
        }

        if (selectedNote != null) {
            if (selectedNote.getContentAlign() == null || selectedNote.getContentAlign().equals("START")) {
                imgAlignLeft.setColorFilter(Color.parseColor("#000000"));
                imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (selectedNote.getContentAlign().equals("CENTER")) {
                imgAlignCenter.setColorFilter(Color.parseColor("#000000"));
                imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
            } else {
                imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignRight.setColorFilter(Color.parseColor("#000000"));
            }
        }
        layoutContentStyle.findViewById(R.id.imageBold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBold = !isBold;
                if (isBold && isItalic) {
                    editText.setTypeface(null, Typeface.BOLD_ITALIC);
                    imgBold.setColorFilter(Color.parseColor("#000000"));
                } else if (isBold) {
                    editText.setTypeface(null, Typeface.BOLD);
                    imgBold.setColorFilter(Color.parseColor("#000000"));
                } else if (isItalic) {
                    editText.setTypeface(null, Typeface.ITALIC);
                    imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                } else {
                    editText.setTypeface(Typeface.DEFAULT);
                    imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                }

            }
        });
        layoutContentStyle.findViewById(R.id.imageItalic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isItalic = !isItalic;
                if (isItalic && isBold) {
                    editText.setTypeface(null, Typeface.BOLD_ITALIC);
                    imgItalic.setColorFilter(Color.parseColor("#000000"));
                } else if (isItalic) {
                    editText.setTypeface(null, Typeface.ITALIC);
                    imgItalic.setColorFilter(Color.parseColor("#000000"));

                } else if (isBold) {
                    editText.setTypeface(null, Typeface.BOLD);
                    imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));

                } else {
                    editText.setTypeface(Typeface.DEFAULT);
                    imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
                }

            }
        });
        layoutContentStyle.findViewById(R.id.imageUnderline).setOnClickListener(new View.OnClickListener() {
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
        if (selectedNote != null) {
            if (selectedNote.isContentBold() && selectedNote.isContentItalic() && selectedNote.isContentUnderLined()) {
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgItalic.setColorFilter(Color.parseColor("#000000"));
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
            } else if (selectedNote.isContentBold() && selectedNote.isContentItalic() && !selectedNote.isContentUnderLined()) {
                imgItalic.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (selectedNote.isContentBold() && !selectedNote.isContentItalic() && !selectedNote.isContentUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (selectedNote.isContentBold() && !selectedNote.isContentItalic() && selectedNote.isContentUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (!selectedNote.isContentBold() && selectedNote.isContentItalic() && !selectedNote.isContentUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#000000"));
            } else if (!selectedNote.isContentBold() && selectedNote.isContentItalic() && selectedNote.isContentUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#000000"));
            } else if (!selectedNote.isContentBold() && !selectedNote.isContentItalic() && selectedNote.isContentUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            } else {
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            }
        }

        layoutContentStyle.findViewById(R.id.imageAlignLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLeft = !isLeft;
                if (isLeft) {
                    if (isCenter && !isRight) {
                        isCenter = false;
                        imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));

                    } else if (isRight && !isCenter) {
                        isRight = false;
                        imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
                    }
                    editText.setGravity(Gravity.START);
                    imgAlignLeft.setColorFilter(Color.parseColor("#000000"));
                    contentAlign = "START";
                }
            }
        });
        layoutContentStyle.findViewById(R.id.imageAlignCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCenter = !isCenter;
                if (isCenter) {
                    if (isLeft && !isRight) {
                        isLeft = false;
                        imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                    } else if (isRight && !isLeft) {
                        isRight = false;
                        imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
                    }
                    editText.setGravity(Gravity.CENTER);
                    contentAlign = "CENTER";
                    imgAlignCenter.setColorFilter(Color.parseColor("#000000"));
                } else {
                    imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                }
            }
        });
        layoutContentStyle.findViewById(R.id.imageAlignRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRight = !isRight;
                if (isRight) {
                    if (isCenter && !isLeft) {
                        isCenter = false;
                        imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                    } else if (isLeft && !isCenter) {
                        isLeft = false;
                        imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                    }
                    editText.setGravity(Gravity.END);
                    contentAlign = "END";
                    imgAlignRight.setColorFilter(Color.parseColor("#000000"));

                } else {
                    imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
                }
            }
        });
        Spinner spinnerFontSize = layoutContentStyle.findViewById(R.id.spinnerFontSize);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.font_sizes_content, R.layout.layout_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.layout_dropdown_spinner_item);
        spinnerFontSize.setAdapter(arrayAdapter);
        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals("Small")) {
                    editText.setTextSize(15);
                    contentTextSize = 15;
                } else if (adapterView.getSelectedItem().toString().equals("Large")) {
                    editText.setTextSize(35);
                    contentTextSize = 35;
                } else {
                    editText.setTextSize(25);
                    contentTextSize = 25;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (selectedNote != null && selectedNote.getContentFontSize() != 0) {
            if (selectedNote.getContentFontSize() == 15) {
                spinnerFontSize.setSelection(1);
            } else if (selectedNote.getContentFontSize() == 35) {
                spinnerFontSize.setSelection(2);
            } else {
                spinnerFontSize.setSelection(0);
            }
        }

        Spinner spinnerFontFamily = layoutContentStyle.findViewById(R.id.spinnerFontFamily);
        ArrayAdapter<CharSequence> arrayFontFamily = ArrayAdapter.createFromResource(this, R.array.font_family, R.layout.layout_spinner_item);
        arrayFontFamily.setDropDownViewResource(R.layout.layout_dropdown_spinner_item);
        spinnerFontFamily.setAdapter(arrayFontFamily);
        spinnerFontFamily.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Typeface typeface;

                if (adapterView.getSelectedItem().toString().equals("Harmonia")) {
                    if (isBoldContent && isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_bold_italic);
                    } else if (isBoldContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_bold);
                    } else if (isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_regular);
                    }
                    editText.setTypeface(typeface);
                    contentFontFamily = "Harmonia";
                } else if (adapterView.getSelectedItem().toString().equals("OpenSans")) {
                    if (isBoldContent && isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_bold_italic);
                    } else if (isBoldContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_bold);
                    } else if (isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_regular);
                    }
                    editText.setTypeface(typeface);
                    contentFontFamily = "OpenSans";
                } else if (adapterView.getSelectedItem().toString().equals("Roboto")) {
                    if (isBoldContent && isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_bold_italic);
                    } else if (isBoldContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_bold);
                    } else if (isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_regular);
                    }
                    editText.setTypeface(typeface);
                    contentFontFamily = "Roboto";
                } else {
                    if (isBoldContent && isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_bold_italic);
                    } else if (isBoldContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_bold);
                    } else if (isItalicContent) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_regular);
                    }
                    editText.setTypeface(typeface);
                    contentFontFamily = "Ubuntu";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (selectedNote != null && !selectedNote.getContentFontFamily().trim().isEmpty()) {
            switch (selectedNote.getContentFontFamily()) {
                case "Harmonia":
                    spinnerFontFamily.setSelection(1);
                    break;
                case "OpenSans":
                    spinnerFontFamily.setSelection(2);
                    break;
                case "Roboto":
                    spinnerFontFamily.setSelection(3);
                    break;
                default:
                    spinnerFontFamily.setSelection(0);
                    break;
            }
        }
    }

    private void initTextStyleLayout(final EditText editText) {
        final LinearLayout layoutTextStyle = findViewById(R.id.layout_text_style);
        final LinearLayout layoutContentStyle = findViewById(R.id.layout_content_style);
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior2 = BottomSheetBehavior.from(layoutTextStyle);
        layoutMiscellaneous.setVisibility(View.GONE);
        layoutContentStyle.setVisibility(View.GONE);
        layoutTextStyle.setVisibility(View.VISIBLE);
        bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
        layoutTextStyle.findViewById(R.id.imageBackText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutTextStyle.setVisibility(View.GONE);
                layoutContentStyle.setVisibility(View.GONE);
                layoutMiscellaneous.setVisibility(View.VISIBLE);
                initMiscellaneous();
            }
        });
        layoutTextStyle.findViewById(R.id.textTitleStyle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior2.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior2.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
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
        final ImageView imgAlignLeft = layoutTextStyle.findViewById(R.id.imageAlignLeft);
        final ImageView imgAlignCenter = layoutTextStyle.findViewById(R.id.imageAlignCenter);
        final ImageView imgAlignRight = layoutTextStyle.findViewById(R.id.imageAlignRight);

        if (editText.getGravity() == 8388659) {
            Log.d("START", true + "");
            imgAlignLeft.setColorFilter(Color.parseColor("#000000"));
        }

        layoutTextStyle.findViewById(R.id.imgColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setTextColor(Color.WHITE);
                selectedTextColor = Color.WHITE;
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
                selectedTextColor = Color.parseColor("#F4BF51");
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
                selectedTextColor = Color.parseColor("#DF3C37");
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
                selectedTextColor = Color.parseColor("#2437B8");
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
                selectedTextColor = Color.parseColor("#7B7B7B");
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);
            }
        });

        if (selectedNote != null && selectedNote.getTitleColor() != 0) {
            if (selectedNote.getTitleColor() == Color.parseColor("#F4BF51")) {
                layoutTextStyle.findViewById(R.id.imgColor2).performClick();
            } else if (selectedNote.getTitleColor() == Color.parseColor("#DF3C37")) {
                layoutTextStyle.findViewById(R.id.imgColor3).performClick();
            } else if (selectedNote.getTitleColor() == Color.parseColor("#2437B8")) {
                layoutTextStyle.findViewById(R.id.imgColor4).performClick();
            } else if (selectedNote.getTitleColor() == Color.parseColor("#7B7B7B")) {
                layoutTextStyle.findViewById(R.id.imgColor5).performClick();
            }
        }

        if (selectedNote != null) {
            if (selectedNote.getTitleAlign() == null || selectedNote.getTitleAlign().equals("START")) {
                imgAlignLeft.setColorFilter(Color.parseColor("#000000"));
                imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (selectedNote.getTitleAlign().equals("CENTER")) {
                imgAlignCenter.setColorFilter(Color.parseColor("#000000"));
                imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
            } else {
                imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                imgAlignRight.setColorFilter(Color.parseColor("#000000"));
            }
        }
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
                } else if (isItalic) {
                    editText.setTypeface(null, Typeface.ITALIC);
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

                } else if (isBold) {
                    editText.setTypeface(null, Typeface.BOLD);
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
        if (selectedNote != null) {
            if (selectedNote.isTitleBold() && selectedNote.isTitleItalic() && selectedNote.isTitleUnderLined()) {
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgItalic.setColorFilter(Color.parseColor("#000000"));
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
            } else if (selectedNote.isTitleBold() && selectedNote.isTitleItalic() && !selectedNote.isTitleUnderLined()) {
                imgItalic.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (selectedNote.isTitleBold() && !selectedNote.isTitleItalic() && !selectedNote.isTitleUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (selectedNote.isTitleBold() && !selectedNote.isTitleItalic() && selectedNote.isTitleUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#000000"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            } else if (!selectedNote.isTitleBold() && selectedNote.isTitleItalic() && !selectedNote.isTitleUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#000000"));
            } else if (!selectedNote.isTitleBold() && selectedNote.isTitleItalic() && selectedNote.isTitleUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#000000"));
            } else if (!selectedNote.isTitleBold() && !selectedNote.isTitleItalic() && selectedNote.isTitleUnderLined()) {
                imgUnderline.setColorFilter(Color.parseColor("#000000"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            } else {
                imgUnderline.setColorFilter(Color.parseColor("#A4A4A4"));
                imgBold.setColorFilter(Color.parseColor("#A4A4A4"));
                imgItalic.setColorFilter(Color.parseColor("#A4A4A4"));
            }
        }

        layoutTextStyle.findViewById(R.id.imageAlignLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLeft = !isLeft;
                if (isLeft) {
                    if (isCenter && !isRight) {
                        isCenter = false;
                        imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));

                    } else if (isRight && !isCenter) {
                        isRight = false;
                        imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
                    }
                    editText.setGravity(Gravity.START);
                    imgAlignLeft.setColorFilter(Color.parseColor("#000000"));
                    titleAlgin = "START";
                }
            }
        });
        layoutTextStyle.findViewById(R.id.imageAlignCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCenter = !isCenter;
                if (isCenter) {
                    if (isLeft && !isRight) {
                        isLeft = false;
                        imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                    } else if (isRight && !isLeft) {
                        isRight = false;
                        imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
                    }
                    editText.setGravity(Gravity.CENTER);
                    titleAlgin = "CENTER";
                    imgAlignCenter.setColorFilter(Color.parseColor("#000000"));
                } else {
                    imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                }
            }
        });
        layoutTextStyle.findViewById(R.id.imageAlignRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRight = !isRight;
                if (isRight) {
                    if (isCenter && !isLeft) {
                        isCenter = false;
                        imgAlignCenter.setColorFilter(Color.parseColor("#A4A4A4"));
                    } else if (isLeft && !isCenter) {
                        isLeft = false;
                        imgAlignLeft.setColorFilter(Color.parseColor("#A4A4A4"));
                    }
                    editText.setGravity(Gravity.END);
                    titleAlgin = "END";
                    imgAlignRight.setColorFilter(Color.parseColor("#000000"));

                } else {
                    imgAlignRight.setColorFilter(Color.parseColor("#A4A4A4"));
                }
            }
        });
        Spinner spinnerFontSize = layoutTextStyle.findViewById(R.id.spinnerFontSize);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.font_sizes, R.layout.layout_spinner_item);
        arrayAdapter.setDropDownViewResource(R.layout.layout_dropdown_spinner_item);
        spinnerFontSize.setAdapter(arrayAdapter);
        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals("Small")) {
                    editText.setTextSize(15);
                    titleTextSize = 15;
                } else if (adapterView.getSelectedItem().toString().equals("Large")) {
                    editText.setTextSize(35);
                    titleTextSize = 35;
                } else {
                    editText.setTextSize(25);
                    titleTextSize = 25;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (selectedNote != null && selectedNote.getTitleFontSize() != 0) {
            if (selectedNote.getTitleFontSize() == 15) {
                spinnerFontSize.setSelection(1);
            } else if (selectedNote.getTitleFontSize() == 35) {
                spinnerFontSize.setSelection(2);
            } else {
                spinnerFontSize.setSelection(0);
            }
        }

        Spinner spinnerFontFamily = layoutTextStyle.findViewById(R.id.spinnerFontFamily);
        ArrayAdapter<CharSequence> arrayFontFamily = ArrayAdapter.createFromResource(this, R.array.font_family, R.layout.layout_spinner_item);
        arrayFontFamily.setDropDownViewResource(R.layout.layout_dropdown_spinner_item);
        spinnerFontFamily.setAdapter(arrayFontFamily);
        spinnerFontFamily.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Typeface typeface;
                if (adapterView.getSelectedItem().toString().equals("Harmonia")) {
                    if (isBold && isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_bold_italic);
                    } else if (isBold) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_bold);
                    } else if (isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.harmonia_regular);
                    }
                    editText.setTypeface(typeface);
                    titleFontFamily = "Harmonia";
                } else if (adapterView.getSelectedItem().toString().equals("OpenSans")) {
                    if (isBold && isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_bold_italic);
                    } else if (isBold) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_bold);
                    } else if (isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.opensans_regular);
                    }
                    editText.setTypeface(typeface);
                    titleFontFamily = "OpenSans";
                } else if (adapterView.getSelectedItem().toString().equals("Roboto")) {
                    if (isBold && isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_bold_italic);
                    } else if (isBold) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_bold);
                    } else if (isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_regular);
                    }
                    editText.setTypeface(typeface);
                    titleFontFamily = "Roboto";
                } else {
                    if (isBold && isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_bold_italic);
                    } else if (isBold) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_bold);
                    } else if (isItalic) {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.ubuntu_regular);
                    }
                    editText.setTypeface(typeface);
                    titleFontFamily = "Ubuntu";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (selectedNote != null && !selectedNote.getTitleFontFamily().trim().isEmpty()) {
            switch (selectedNote.getTitleFontFamily()) {
                case "Harmonia":
                    spinnerFontFamily.setSelection(1);
                    break;
                case "OpenSans":
                    spinnerFontFamily.setSelection(2);
                    break;
                case "Roboto":
                    spinnerFontFamily.setSelection(3);
                    break;
                default:
                    spinnerFontFamily.setSelection(0);
                    break;
            }
        }
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
                @SuppressLint("SimpleDateFormat")
                @Override
                public void onClick(View view) {
                    SimpleDateFormat sdf3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    Date timeRemind = null;
                    try {
                        timeRemind = sdf3.parse(textDateAndTime.getText().toString());
                        timeReminder = timeRemind;
                        assert timeRemind != null;
                        sdf3 = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a");
                        txtTimeReminder.setText(sdf3.format(timeRemind));
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