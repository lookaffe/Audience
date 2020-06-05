package intersistemi.it.afp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import basfp.it.bas3.support.LogAndroid;
import intersistemi.it.afp.R;
import intersistemi.it.afp.util.Util;


public class LogFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String LOG_FILENAME = "loguploads.txt";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView textView;
    private StringBuilder text = new StringBuilder();
    private String pathBase = "";
    private static String EXT_PATH, INT_PATH;

    public LogFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogFragment newInstance(String param1, String param2)
    {
        LogFragment fragment = new LogFragment();
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
    public void onResume() {
        super.onResume();
        if (textView!=null)
            textView.invalidate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        final View ll = inflater.inflate(R.layout.fragment_log, container, false);
        EXT_PATH = getArguments().getString("ext_path");
        INT_PATH = getArguments().getString("int_path");
        LogAndroid.info("LogFragment EXT_PAth", EXT_PATH);
        LogAndroid.info("LogFragment INT_PAth", INT_PATH);
        pathBase= EXT_PATH + File.separator + getString(R.string.app_name);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                try {

                    FileReader fr = new FileReader(pathBase+"/"+LOG_FILENAME);
                    reader = new BufferedReader(fr);

                    // do reading, usually loop until end of file reading
                    String mLine;
                    while ((mLine = reader.readLine()) != null) {
                        text.append(mLine);
                        text.append('\n');
                    }
                } catch (IOException e) {
                    //Toast.makeText(getContext(),"Error reading file!",Toast.LENGTH_LONG).show();
                    //e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            //log the exception
                        }
                    }

                    TextView output = (TextView) ll.findViewById(R.id.textView);

                    output.refreshDrawableState();
                    output.setText((CharSequence) text);
                    output.refreshDrawableState();

                }
            }
        });

        return ll;
    }

}
