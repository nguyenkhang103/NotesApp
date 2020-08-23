package com.example.kaido.noteapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.entities.Note;
import com.example.kaido.noteapp.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes;
    private NoteListener noteListener;
    private List<Note> noteSource;
    private Timer timer;
    private static Context context;

    public NoteAdapter(List<Note> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
        this.noteSource = notes;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new NoteHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, final int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteListener.onClickedNote(notes.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class NoteHolder extends RecyclerView.ViewHolder {

        TextView txtNoteTitle, txtNoteSubTitle, txtDateTime, txtLinkURL;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            txtNoteTitle = itemView.findViewById(R.id.textNoteTitle);
            txtNoteSubTitle = itemView.findViewById(R.id.textNoteSubtitle);
            txtDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layout_item_note);
            imageNote = itemView.findViewById(R.id.imageImageNote);
            txtLinkURL = itemView.findViewById(R.id.textLinkURL);
        }

        public void setNote(Note note) {
            Typeface typeface;
            //title
            txtNoteTitle.setText(note.getTitle());
            if (note.getTitleColor() != 0) {
                txtNoteTitle.setTextColor(note.getTitleColor());
            } else {
                txtNoteTitle.setTextColor(Color.WHITE);
            }
            if(note.getTitleFontFamily() == null) {
                typeface = ResourcesCompat.getFont(context, R.font.ubuntu_bold);
                txtNoteTitle.setTypeface(typeface);
            }
            switch (note.getTitleFontFamily()) {
                case "Harmonia":
                    if (note.isTitleBold() && note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.harmonia_bold_italic);
                    } else if (note.isTitleBold()) {
                        typeface = ResourcesCompat.getFont(context, R.font.harmonia_bold);
                    } else if (note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.harmonia_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(context, R.font.harmonia_regular);
                    }
                    txtNoteTitle.setTypeface(typeface);
                    break;
                case "OpenSans":
                    if (note.isTitleBold() && note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.opensans_bold_italic);
                    } else if (note.isTitleBold()) {
                        typeface = ResourcesCompat.getFont(context, R.font.opensans_bold);
                    } else if (note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.opensans_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(context, R.font.opensans_regular);
                    }
                    txtNoteTitle.setTypeface(typeface);
                    break;
                case "Roboto":
                    if (note.isTitleBold() && note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_bold_italic);
                    } else if (note.isTitleBold()) {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
                    } else if (note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_regular);
                    }
                    txtNoteTitle.setTypeface(typeface);
                    break;
                default:
                    if (note.isTitleBold() && note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_bold_italic);
                    } else if (note.isTitleBold()) {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_bold);
                    } else if (note.isTitleItalic()) {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_italic);
                    } else {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_regular);
                    }
                    txtNoteTitle.setTypeface(typeface);
                    break;
            }

            if (note.getTitleFontSize() == 15) {
                txtNoteTitle.setTextSize(15);
            } else if (note.getTitleFontSize() == 35) {
                txtNoteTitle.setTextSize(35);
            } else {
                txtNoteTitle.setTextSize(25);
            }
            if (note.isTitleUnderLined()){
                txtNoteTitle.setPaintFlags(txtNoteTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
            if (note.getTitleAlign() == null || note.getTitleAlign().equals("START")) {
                txtNoteTitle.setGravity(Gravity.START);
            } else if (note.getTitleAlign().equals("CENTER")) {
                txtNoteTitle.setGravity(Gravity.CENTER);
            } else {
                txtNoteTitle.setGravity(Gravity.END);
            }

            //subtitle
            if (note.getSubtitle().trim().isEmpty()) {
                txtNoteSubTitle.setVisibility(View.GONE);
            } else {
                txtNoteSubTitle.setText(note.getSubtitle());
            }
            //datetime
            txtDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getImagePath() != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }

            if (note.getUrl() == null || note.getUrl().trim().isEmpty()) {
                txtLinkURL.setVisibility(View.GONE);
            } else {
                txtLinkURL.setText(note.getUrl());
            }
        }

    }

    public void searchNote(final String keyword) {
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (keyword.trim().isEmpty()) {
                            notes = noteSource;
                        } else {
                            List<Note> tmp = new ArrayList<>();
                            for (Note note : noteSource) {
                                if (note.getTitle().trim().toLowerCase().contains(keyword.trim().toLowerCase())
                                        || note.getSubtitle().trim().toLowerCase().contains(keyword.trim().toLowerCase())
                                        || note.getNoteContent().trim().toLowerCase().contains(keyword.trim().toLowerCase())
                                        || note.getDateTime().trim().toLowerCase().contains(keyword.trim().toLowerCase())) {
                                    tmp.add(note);
                                }
                            }
                            notes = tmp;
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                }, 500
        );
    }

    public void timerCancel() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
