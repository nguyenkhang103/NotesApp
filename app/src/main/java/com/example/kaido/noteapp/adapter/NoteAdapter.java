package com.example.kaido.noteapp.adapter;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.entities.Note;
import com.example.kaido.noteapp.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes;
    private NoteListener noteListener;

    public NoteAdapter(List<Note> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

    public static class NoteHolder extends RecyclerView.ViewHolder{

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
            txtNoteTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()) {
                txtNoteSubTitle.setVisibility(View.GONE);
            } else {
                txtNoteSubTitle.setText(note.getSubtitle());
            }
            txtDateTime.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(note.getColor() != null) {
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

            if(note.getUrl() == null || note.getUrl().trim().isEmpty()) {
                txtLinkURL.setVisibility(View.GONE);
            } else {
                txtLinkURL.setText(note.getUrl());
            }
        }
    }
}
