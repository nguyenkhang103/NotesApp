package com.example.kaido.noteapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kaido.noteapp.R;
import com.example.kaido.noteapp.entities.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private List<Note> notes;

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        holder.setNote(notes.get(position));
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

        TextView txtNoteTitle, txtNoteSubTitle, txtDateTime;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            txtNoteTitle = itemView.findViewById(R.id.textNoteTitle);
            txtNoteSubTitle = itemView.findViewById(R.id.textNoteSubtitle);
            txtDateTime = itemView.findViewById(R.id.textDateTime);
        }
        public void setNote(Note note) {
            txtNoteTitle.setText(note.getTitle());
            if(note.getSubtitle().toString().trim().isEmpty()) {
                txtNoteSubTitle.setVisibility(View.GONE);
            } else {
                txtNoteSubTitle.setText(note.getSubtitle());
            }
            txtDateTime.setText(note.getDateTime());
        }
    }
}
