package intersistemi.it.afp.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.view.menu.MenuView;
import android.widget.Button;

import com.google.protobuf.ByteString;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import basfp.it.bas3.support.LogAndroid;

/**
 * Created by fnardi on 13/03/2017.
 */

public class Util
{

    public static String TAG="baslib";
    public static final String LOGIN_SERVER_URL_PROPERTY = "LOGIN_SERVER_URL";
    public static final String UPLOAD_SERVER_URL_PROPERTY = "UPLOAD_SERVER_URL";
    public static final String LOG_SERVER_URL_PROPERTY = "LOG_SERVER_URL";
    private static final String PROPERTY_FILENAME = "appascolto.properties";
    private static String LOG_PATH, DEVICE_ID;
    private static Util instance= null;
    private Properties properties;


    public static Util getInstance(){
        if (null==instance) {
            instance = new Util();
        }
        return instance;
    }

    public void switchButton(boolean stato,MenuView.ItemView item)
    {
        item.setEnabled(stato);
    }

    public String getProperty(Context context, String key){
        try{
            if (null==this.properties){
                this.properties = new Properties();
                this.properties.load(context.getAssets().open(PROPERTY_FILENAME));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return this.properties.get(key).toString();
    }

    public String getCurrectTimestamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date()).toString();
    }

    public String getRandommIdRegistrazione()
    {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public String ritornaOraDaMillisecondi(long millis)
        {
            Date date = new Date(millis);
            DateFormat formatter = new SimpleDateFormat("HH:mm");
            return formatter.format(date);
        }

    public String dayStringFormat(long msecs) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(msecs));
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.MONDAY:
                return "Lunedì";
            case Calendar.TUESDAY:
                return "Martedì";
            case Calendar.WEDNESDAY:
                return "Mercoledì";
            case Calendar.THURSDAY:
                return "Giovedì";
            case Calendar.FRIDAY:
                return "Venerdì";
            case Calendar.SATURDAY:
                return "Sabato";
            case Calendar.SUNDAY:
                return "Domenica";
        }
        return "Unknown";
    }

    public void toggleDayOfWeekButton(Button button, Drawable unselected, Drawable selected)
    {
        if (button.isSelected())
        {
            button.setSelected(false);
            button.setBackground(unselected);
        }
        else
        {
            button.setSelected(true);
            button.setBackground(selected);
        }
    }

    public Calendar nextDayOfWeek(int dayOfWeek) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        System.out.println("nextDayOfWeek(Orazio) "+df.format( new Date(date.getTimeInMillis()) ));
        return date;
    }


    public String getTimeInMillis(int year, int month, int day, int hour, int minute, int second) {
        System.out.println("Util(Orazio) getTimeInMillis() "+day+"/"+month+"/"+year+" "+hour+":"+minute+":"+second);
        Calendar calendar  = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        /*if(calendar.compareTo(Calendar.getInstance()) == -1) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }*/
        return ""+calendar.getTimeInMillis();
    }

    public void setLogPath (String s){
        LOG_PATH = s;
        LogAndroid.info("LOG_PATH", LOG_PATH);
    }

    public String getLogPath(){
        return LOG_PATH;
    }

    public void setDeviceId (String s){
        DEVICE_ID = s;
        LogAndroid.info("DEVICE_ID", DEVICE_ID);
    }

    public String getDeviceId(){
        return DEVICE_ID;
    }

    public ByteString getLogContent(){
        File logFile = new File(LOG_PATH+"/"+DEVICE_ID+".txt");
        int size = (int) logFile.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(logFile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ByteString.copyFrom(bytes);
    }

    public void updateLog(String text)
    {
        File logFile = new File(LOG_PATH+"/"+DEVICE_ID+".txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
                LogAndroid.info("NEW LOG FILE", String.valueOf(logFile));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteLog() {
        File logFile = new File(LOG_PATH + "/" + DEVICE_ID + ".txt");
        if (logFile.exists()) {
            logFile.delete();
        }
    }

}
