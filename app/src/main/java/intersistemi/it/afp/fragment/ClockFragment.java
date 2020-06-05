package intersistemi.it.afp.fragment;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bas3.it.basfp.broadcast.AlarmReceiverBroadcast;
import intersistemi.it.afp.R;
import intersistemi.it.afp.util.Util;
import intersistemi.it.afp.db.DataBaseDbHelper;
import intersistemi.it.afp.db.UserAlarmBaseColumns;


public class ClockFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Util util;
    ListView alarm_list;



    public ClockFragment()
    {
        // Required empty public constructor
    }


    public static ClockFragment newInstance(String param1, String param2)
    {
        ClockFragment fragment = new ClockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        util= new Util();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View ll = inflater.inflate(R.layout.fragment_clock, container, false);

        alarm_list = (ListView)ll.findViewById(R.id.alarm_list);
        ArrayList<String[]> list = new ArrayList<String[]>();
        MyCustomAdapter myCustomAdapter = new MyCustomAdapter(list, getActivity().getApplicationContext());
        alarm_list.setAdapter(myCustomAdapter);
        FloatingActionButton btn_aggiungi=(FloatingActionButton)ll.findViewById(R.id.btn_aagioungi_alarm);
        btn_aggiungi.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.timepicker_custom);
            dialog.show();
            final Button lunedi = (Button) dialog.findViewById(R.id.lunedi);
            lunedi.setSelected(false);
           // lunedi.setBackgroundColor(Color.LTGRAY);
            lunedi.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(lunedi, ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "lunedi "+lunedi.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Button martedi = (Button) dialog.findViewById(R.id.martedi);
            martedi.setSelected(false);
           // martedi.setBackgroundColor(Color.LTGRAY);
            martedi.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(martedi,ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "martedi "+martedi.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Button mercoledi = (Button) dialog.findViewById(R.id.mercoledi);
            mercoledi.setSelected(false);
          //  mercoledi.setBackgroundColor(Color.LTGRAY);
            mercoledi.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(mercoledi,ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "mercoledi "+mercoledi.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Button giovedi = (Button) dialog.findViewById(R.id.giovedi);
            giovedi.setSelected(false);
         //   giovedi.setBackgroundColor(Color.LTGRAY);
            giovedi.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(giovedi,ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "giovedi "+giovedi.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Button venerdi = (Button) dialog.findViewById(R.id.venerdi);
            venerdi.setSelected(false);
       //     venerdi.setBackgroundColor(Color.LTGRAY);
            venerdi.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(venerdi,ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "venerdi "+venerdi.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Button sabato = (Button) dialog.findViewById(R.id.sabato);
            sabato.setSelected(false);
        //    sabato.setBackgroundColor(Color.LTGRAY);
            sabato.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(sabato,ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "sabato "+sabato.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Button domenica = (Button) dialog.findViewById(R.id.domenica);
            domenica.setSelected(false);
        //    domenica.setBackgroundColor(Color.LTGRAY);
            domenica.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    util.toggleDayOfWeekButton(domenica,ResourcesCompat.getDrawable(getResources(), R.drawable.my_button, null),ResourcesCompat.getDrawable(getResources(), R.drawable.my_button_selected, null));
                    //Toast.makeText(getApplicationContext(), "domenica "+domenica.isSelected(), Toast.LENGTH_SHORT).show();
                }
            });
            final Spinner spinner = (Spinner) dialog.findViewById(R.id.day_of_week);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.day_of_week, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            final TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.time_picker);
            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(Calendar.getInstance().get(Calendar.MINUTE));
            Button setTime = (Button) dialog.findViewById(R.id.set_time);
            setTime.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (lunedi.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.MONDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    if (martedi.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.TUESDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    if (mercoledi.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.WEDNESDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    if (giovedi.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.THURSDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    if (venerdi.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.FRIDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    if (sabato.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.SATURDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    if (domenica.isSelected()) {
                        Calendar calendarTemp = util.nextDayOfWeek(Calendar.SUNDAY);
                        setUserAlarm(
                                calendarTemp.get(Calendar.YEAR),
                                calendarTemp.get(Calendar.MONTH),
                                calendarTemp.get(Calendar.DAY_OF_MONTH),
                                timePicker.getCurrentHour(),
                                timePicker.getCurrentMinute(),
                                0
                        );
                    }
                    dialog.dismiss();
                }
            });
        }





    });
        readUserAlarm();
        return ll;
    }

    private void readUserAlarm() {
        DataBaseDbHelper mDbHelper = new DataBaseDbHelper(getActivity().getBaseContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                UserAlarmBaseColumns.UserAlarmEntry._ID,
                UserAlarmBaseColumns.UserAlarmEntry.COLUMN_NAME_TIME_IN_MILLIS
        };
        String selection = "";
        String[] selectionArgs = { }; //Where Clause
        String sortOrder = UserAlarmBaseColumns.UserAlarmEntry.COLUMN_NAME_TIME_IN_MILLIS + " DESC";
        Cursor cursor = db.query(
                UserAlarmBaseColumns.UserAlarmEntry.TABLE_NAME,         // The table to query
                projection,                                             // The columns to return
                selection,                                              // The columns for the WHERE clause
                selectionArgs,                                          // The values for the WHERE clause
                null,                                                   // don't group the rows
                null,                                                   // don't filter by row groups
                sortOrder                                               // The sort order
        );

        HashMap clockGrouped = new HashMap();
        while (cursor.moveToNext()) {
            String hoursMinute = util.ritornaOraDaMillisecondi(Long.parseLong(cursor.getString(1)));
            //System.out.println("readUserAlarm(Orazio) clockGrouped  millisecond= "+cursor.getString(0)+" "+cursor.getString(1));
           // System.out.println("readUserAlarm(Orazio) clockGrouped complete = "+ util.getDayMonthYearHourMinuteFromMillisecond(Long.parseLong(cursor.getString(1))) );
            //System.out.println("readUserAlarm(Orazio) clockGrouped = "+ hoursMinute );
            if (clockGrouped.get(hoursMinute)==null) {
                ArrayList<String[]> actualValue = new ArrayList<String[]>();
                String[] actualSingleValue = new String[]{ cursor.getString(0), util.dayStringFormat(Long.parseLong(cursor.getString(1))) };
                actualValue.add( actualSingleValue );
                clockGrouped.put(
                        hoursMinute,
                        actualValue
                );
            }
            else {
                ArrayList<String[]> previuousValue = (ArrayList<String[]>)clockGrouped.get(hoursMinute);
                String[] actualSingleValue = new String[]{ cursor.getString(0), util.dayStringFormat(Long.parseLong(cursor.getString(1))) };
                previuousValue.add(actualSingleValue);
                clockGrouped.remove(hoursMinute);
                clockGrouped.put(
                        hoursMinute,
                        previuousValue
                );
            }
        }
        ArrayList<String[]> list = new ArrayList<String[]>();
        Iterator it = clockGrouped.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String hour = (String)pair.getKey();
            ArrayList<String[]> actualValue = (ArrayList<String[]>)pair.getValue();
            String timestamps = "";
            String days = "";
            for (int i=0; i<actualValue.size(); i++) {
                if (i==0) {
                    timestamps += (actualValue.get(i))[0];
                    days += ((actualValue.get(i))[1]).substring(0,3);
                }
                else {
                    timestamps += "#"+(actualValue.get(i))[0];
                    days = ((actualValue.get(i))[1]).substring(0,3)+" - "+days;
                }
            }
            //System.out.println("readUserAlarm(Orazio) millisecond= "+cursor.getString(0)+" "+cursor.getString(1));
            //System.out.println("readUserAlarm(Orazio) complete = "+ util.getDayMonthYearHourMinuteFromMillisecond(Long.parseLong(cursor.getString(1))) );
            //System.out.println("readUserAlarm(Orazio) = "+ util.getHourMinuteFromMillisecond(Long.parseLong(cursor.getString(1))) );
            list.add(
                    new String[]{
                            timestamps,
                            hour,
                            days
                    }
            );
        }
        MyCustomAdapter adapter = new MyCustomAdapter(list, getActivity().getApplicationContext());
        alarm_list.setAdapter(adapter);
    }

    private void setUserAlarm(int year, int month, int day, int hour, int minute, int second) {
        String time_in_millis = util.getTimeInMillis(year,month,day,hour,minute,second);
        //Database managment
        DataBaseDbHelper mDbHelper = new DataBaseDbHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserAlarmBaseColumns.UserAlarmEntry.COLUMN_NAME_TIME_IN_MILLIS, time_in_millis);
        int newRowId = (int)db.insert(UserAlarmBaseColumns.UserAlarmEntry.TABLE_NAME, null, values);
        //Android Managment
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlarmReceiverBroadcast.class);
        PendingIntent pendingintent = PendingIntent.getBroadcast(getActivity(), newRowId, intent, 0);
        //am.set(AlarmManager.RTC_WAKEUP,Long.parseLong(time_in_millis), pendingintent);
        am.setRepeating(AlarmManager.RTC_WAKEUP,Long.parseLong(time_in_millis),am.INTERVAL_DAY * 7,pendingintent);
        //Toast.makeText(this, "Settato allarme il "+day+"/"+month+"/"+year+" alle ore "+hour+":"+minute, Toast.LENGTH_SHORT).show();
       // Toast.makeText(getActivity(), "Settato allarme ogni "+util.dayStringFormat(Long.parseLong(time_in_millis))+" alle ore "+util.ritornaOraDaMillisecondi(Long.parseLong(time_in_millis)), Toast.LENGTH_SHORT).show();
        readUserAlarm();
    }

    private void deleteUserAlarm(String id, String time_in_millis) {
        String[] ids = id.split("#");
        for (int i=0; i<ids.length; i++) {
            //DataBase Managment
            DataBaseDbHelper mDbHelper = new DataBaseDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String selection = UserAlarmBaseColumns.UserAlarmEntry._ID + " = ? ";
            String[] selectionArgs = {ids[i]};
            db.delete(UserAlarmBaseColumns.UserAlarmEntry.TABLE_NAME, selection, selectionArgs);
            //Android Managment
            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            //Intent intent = new Intent(this, AlarmReceiver.class)
            Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiverBroadcast.class);
            PendingIntent pendingintent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), Integer.parseInt(ids[i]), intent, 0);
            am.cancel(pendingintent);
        /*Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        alarmIntent.setData(Uri.parse("custom://"+ids[i]));
        alarmIntent.setAction(String.valueOf(ids[i]));
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent displayIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
        alarmManager.cancel(displayIntent);*/
            //System.out.println("deleteUserAlarm("+ids[i]+"-"+time_in_millis+")");
        }
        readUserAlarm();
    }

    public class MyCustomAdapter extends BaseAdapter implements ListAdapter
    {

        private ArrayList<String[]> list = new ArrayList<String[]>();
        private Context context;

        public MyCustomAdapter(ArrayList<String[]> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int pos) {
            return list.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return 0; //list.get(pos).getId();
            //just return 0 if your list items do not have an Id variable.
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.riga_schedulata, null);
            }
            final String[] alarm = list.get(position);
            TextView listItemId = (TextView)view.findViewById(R.id.list_item_id);
            listItemId.setText(alarm[0]);
            TextView listItemTimeStamp = (TextView)view.findViewById(R.id.hour);
            listItemTimeStamp.setText(alarm[1]);
            TextView gg = (TextView)view.findViewById(R.id.list_days);
            gg.setText(alarm[2]);

            ImageButton deleteBtn = (ImageButton)view.findViewById(R.id.delete_btn);
            deleteBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    list.remove(position);
                    notifyDataSetChanged();
                   /* getActivity().runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "Allarme cancellato "+alarm[0], Toast.LENGTH_SHORT).show();
                        }
                    });*/

                    deleteUserAlarm(alarm[0], "");
                }
            });
            return view;
        }
    }


}
