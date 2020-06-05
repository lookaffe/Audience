package intersistemi.it.afp.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

import basfp.it.bas3.support.LogAndroid;
import intersistemi.it.afp.R;
import intersistemi.it.afp.db.ConfigurationsBaseColumns;
import intersistemi.it.afp.db.DataBaseDbHelper;
import intersistemi.it.afp.util.Util;

public class SettingsFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText upload_url_registration;
    private EditText rec_interval_split;
    private EditText rec_interval_registration;
    private FloatingActionButton btn_salva_configurazioni;

    public SettingsFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2)
    {
        SettingsFragment fragment = new SettingsFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View ll = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView txt=(TextView)ll.findViewById(R.id.txtID);
        txt.setText(getID());

        upload_url_registration = (EditText)ll.findViewById(R.id.upload_url_registration);
        rec_interval_split = (EditText)ll.findViewById(R.id.rec_interval_split);
        rec_interval_registration = (EditText)ll.findViewById(R.id.rec_interval_registration);
        btn_salva_configurazioni = (FloatingActionButton)ll.findViewById(R.id.btn_salva_configurazioni);
        btn_salva_configurazioni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConfiguration();
            }
        });
        getConfiguration(getActivity().getApplicationContext());
        return ll ;
    }

    private void setConfiguration() {
        try {
            DataBaseDbHelper mDbHelper = new DataBaseDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            if (
                    upload_url_registration.getText().toString().length()<=0 ||
                            rec_interval_split.getText().toString().length()<=0 ||
                            rec_interval_registration.getText().toString().length()<=0
                    ) {
                getActivity().runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getActivity().getApplicationContext(), "Riempire tutti i campi obbligatori", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            //Cancella le vecchie configurazioni
            String selection = "";
            String[] selectionArgs = {};
            db.delete(ConfigurationsBaseColumns.ConfigurationsEntry.TABLE_NAME, selection, selectionArgs);

            //Inserisce le nuove configurazioni
            ContentValues values = new ContentValues();
            values.put(ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_UPLOAD_URL_REGISTRATION, upload_url_registration.getText().toString());
            values.put(ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_SPLIT, rec_interval_split.getText().toString());
            values.put(ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_REGISTRATION, rec_interval_registration.getText().toString());
            int newRowId = (int) db.insert(ConfigurationsBaseColumns.ConfigurationsEntry.TABLE_NAME, null, values);

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), "Salvataggio effettuato", Toast.LENGTH_SHORT).show();
                }
            });

            getConfiguration(getActivity().getApplicationContext());
        }
        catch(final Throwable t) {
            getActivity().runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Errore setConfiguration() "+t.getMessage(), Toast.LENGTH_SHORT).show(); //MainActivity.this
                }
            });
            //t.printStackTrace();
        }
    }
    private HashMap getConfiguration(Context context) {
        HashMap configuration = new HashMap();
        try {
            DataBaseDbHelper mDbHelper = new DataBaseDbHelper(context);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] projection = {
                    ConfigurationsBaseColumns.ConfigurationsEntry._ID,
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_UPLOAD_URL_REGISTRATION,
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_SPLIT,
                    ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_REC_INTERVAL_REGISTRATION

            };
            String selection = "";
            String[] selectionArgs = {}; //Where Clause
            String sortOrder = ConfigurationsBaseColumns.ConfigurationsEntry.COLUMN_NAME_UPLOAD_URL_REGISTRATION + " DESC";
            Cursor cursor = db.query(
                    ConfigurationsBaseColumns.ConfigurationsEntry.TABLE_NAME,  // The table to query
                    projection,                                     // The columns to return
                    selection,                                      // The columns for the WHERE clause
                    selectionArgs,                                  // The values for the WHERE clause
                    null,                                           // don't group the rows
                    null,                                           // don't filter by row groups
                    sortOrder                                       // The sort order
            );
            if (cursor.moveToNext()) {
                upload_url_registration.setText(cursor.getString(1));
                rec_interval_split.setText(cursor.getString(2));
                rec_interval_registration.setText(cursor.getString(3));
                System.out.println("Ricaricate "+cursor.getString(1));
                System.out.println("Ricaricate "+cursor.getString(2));
                System.out.println("Ricaricate "+cursor.getString(3));
                configuration.put("COLUMN_NAME_UPLOAD_URL_REGISTRATION",cursor.getString(1));
                configuration.put("COLUMN_NAME_REC_INTERVAL_SPLIT",cursor.getString(2));
                configuration.put("COLUMN_NAME_REC_INTERVAL_REGISTRATION",cursor.getString(3));
            }
        }
        catch(final Throwable t) {
            getActivity().runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Errore getConfiguration() "+t.getMessage(), Toast.LENGTH_SHORT).show(); //MainActivity.this
                }
            });
            //t.printStackTrace();
        }
        return configuration;
    }
    public String getID(){
        String uuid = "";
        try
        {
            uuid = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
           // LogAndroid.info(Util.TAG, "this.getApplicationContext().getContentResolver()");
        }
        catch(Exception e)
        {
            uuid = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
           // LogAndroid.info(Util.TAG, "context.getContentResolver()");
        }

        return uuid;
    }

}
