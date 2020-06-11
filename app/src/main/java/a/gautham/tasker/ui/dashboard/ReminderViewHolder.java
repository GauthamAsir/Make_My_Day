package a.gautham.tasker.ui.dashboard;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import a.gautham.tasker.R;

public class ReminderViewHolder extends RecyclerView.ViewHolder {

    public ImageView letterAvatar, notification_icon;
    public TextView title, reminderDateTime, type, delete;

    public ReminderViewHolder(@NonNull View itemView) {
        super(itemView);

        letterAvatar = itemView.findViewById(R.id.letterAvatar);
        title = itemView.findViewById(R.id.title);
        reminderDateTime = itemView.findViewById(R.id.reminderDateTime);
        type = itemView.findViewById(R.id.type);
        delete = itemView.findViewById(R.id.delete);
        notification_icon = itemView.findViewById(R.id.notification_icon);

    }
}
