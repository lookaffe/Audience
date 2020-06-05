package bas3.it.basfp.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AlarmBootReceiverBroadcast extends BroadcastReceiver{
	Alarm alarm;
	Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		charger();
		planifierAlarm();
	}

    public void charger(){
    	alarm = null;
    	try {
    		ObjectInputStream alarmOIS= new ObjectInputStream(context.openFileInput("alarm.serial"));
    		try {	
				alarm = (Alarm) alarmOIS.readObject(); 
			}
			finally {
				try {
					alarmOIS.close();
				} finally {
					;
				}
			}
		}
    	catch(FileNotFoundException fnfe){
    		alarm = new Alarm();
        	alarm.setActive(true);
        	Time t = new Time();
        	t.hour = 7;
        	t.minute = 30;
        	alarm.setHeure(t);
    	}
    	catch(IOException ioe) {
			ioe.printStackTrace();
		}
    	catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
    }   
    
	private void planifierAlarm() {
		/*AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingintent = PendingIntent.getBroadcast(context, MainActivity.ALARM_ID, intent, 0);
		am.cancel(pendingintent);
		Calendar reveil  = Calendar.getInstance();
		reveil.set(Calendar.HOUR_OF_DAY, alarm.getHeure().hour);
		reveil.set(Calendar.MINUTE, alarm.getHeure().minute);
		if(reveil.compareTo(Calendar.getInstance()) == -1) {
			reveil.add(Calendar.DAY_OF_YEAR, 1);
		}
		Calendar cal = Calendar.getInstance();
		reveil.set(Calendar.SECOND, 0);
		cal.set(Calendar.SECOND, 0);
		long diff = reveil.getTimeInMillis() - cal.getTimeInMillis();
		am.set(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis() + diff, pendingintent);
		Toast.makeText(context, "1 Allarme programmato per il giorno " + reveil.get(Calendar.DAY_OF_MONTH) + " alle ore " + reveil.get(Calendar.HOUR_OF_DAY) + " " + reveil.get(Calendar.MINUTE), Toast.LENGTH_SHORT).show();*/
	}
}
