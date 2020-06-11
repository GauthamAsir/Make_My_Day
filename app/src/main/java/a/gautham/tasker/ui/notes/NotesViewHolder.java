package a.gautham.tasker.ui.notes;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import a.gautham.tasker.R;

public class NotesViewHolder extends RecyclerView.ViewHolder {

    public TextView notes_title, createdDate, editedTag;
    public ImageView reminder_icon, edit_note, delete_note;

    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);

        notes_title = itemView.findViewById(R.id.notes_title);
        createdDate = itemView.findViewById(R.id.createdDate);
        editedTag = itemView.findViewById(R.id.editedTag);
        reminder_icon = itemView.findViewById(R.id.reminder_icon);
        edit_note = itemView.findViewById(R.id.edit_note);
        delete_note = itemView.findViewById(R.id.delete_note);

    }
}
