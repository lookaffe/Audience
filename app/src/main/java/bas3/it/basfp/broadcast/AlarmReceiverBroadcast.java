package bas3.it.basfp.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



import bas3.it.basfp.FingerPrint;
import intersistemi.it.afp.R;

public class AlarmReceiverBroadcast extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent mServiceIntent = new Intent(context.getApplicationContext(), FingerPrint.class);
		mServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(mServiceIntent);

	/*	final NotificationManager mNotifyManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentTitle("Picture Download")
				.setContentText("Download in progress")
				.setSmallIcon(R.drawable.ic_icon_home);
	// Start a lengthy operation in a background thread
		new Thread(
				new Runnable() {
					@Override
					public void run() {
						int incr;
						// Do the "lengthy" operation 20 times
						for (incr = 0; incr <= 100; incr+=5) {
							// Sets the progress indicator to a max value, the
							// current completion percentage, and "determinate"
							// state
							mBuilder.setProgress(100, incr, false);
							// Displays the progress bar for the first time.
							mNotifyManager.notify(0, mBuilder.build());
							// Sleeps the thread, simulating an operation
							// that takes time
							try {
								// Sleep for 5 seconds
								Thread.sleep(5*1000);
							} catch (InterruptedException e) {
								//Log.d(TAG, "sleep failure");
							}
						}
						// When the loop is finished, updates the notification
						mBuilder.setContentText("Download complete")
								// Removes the progress bar
								.setProgress(0,0,false);
						mNotifyManager.notify(0, mBuilder.build());
					}
				}
// Starts the thread by calling the run() method in its Runnable
		).start();*/

	}
}
