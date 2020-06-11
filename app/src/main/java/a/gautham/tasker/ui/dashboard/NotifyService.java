package a.gautham.tasker.ui.dashboard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import a.gautham.tasker.Dashboard;
import a.gautham.tasker.R;

public class NotifyService extends Service {

    public NotifyService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Intent notificationIntent = new Intent(getApplicationContext(), Dashboard.class);
        notificationIntent.putExtra("fromNotification", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String channelId = getString(R.string.channel_id);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        mBuilder.setContentTitle("My Notification");
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentText("Notification Listener Service Example");
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new
                    NotificationChannel(channelId, getString(R.string.app_name), importance);
            mBuilder.setChannelId(channelId);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        assert mNotificationManager != null;
        mNotificationManager.notify(12, mBuilder.build());
        throw new UnsupportedOperationException("Not yet implemented");

    }

}
