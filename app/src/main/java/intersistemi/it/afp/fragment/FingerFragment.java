package intersistemi.it.afp.fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;

import bas3.it.basfp.FingerPrint;
import basfp.it.bas3.support.LogAndroid;
import intersistemi.it.afp.R;
import intersistemi.it.afp.activity.MainActivity;
import intersistemi.it.afp.util.Util;


public class FingerFragment extends Fragment
{
    private static final String TAG = "baslib";
    private static String EXT_PATH, INT_PATH;
    private Chronometer c =null;
    private FloatingActionButton btnAvvia = null; // tasto avvia registrazione
    private FloatingActionButton btnFine=null; // tasto stop registrazione
    private FloatingActionButton btnAnnulla=null;
    private FingerPrint riconoscimento;
    final Handler recordingHandler = new Handler();
    private Runnable runnableRecordingTask;

    public FingerFragment()
    {
        // Required empty public constructor
    }

    public static FingerFragment newInstance()
    {
        FingerFragment fragment = new FingerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){

            if(FingerPrint.class.equals(service.service.getClassName())) {

                LogAndroid.info(Util.TAG, "schedulazione attiva");
                return true;
            }
        }
        LogAndroid.info(Util.TAG, "schedulazione in pausa");
        return false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        EXT_PATH = getArguments().getString("ext_path");
        INT_PATH = getArguments().getString("int_path");
        LogAndroid.info("FingerFragment EXT_PAth", EXT_PATH);
        LogAndroid.info("FingerFragment INT_PAth", INT_PATH);
        View ll = inflater.inflate(R.layout.fragment_finger, container, false);
        String pathBase= EXT_PATH + File.separator + getString(R.string.app_name);
        riconoscimento= new FingerPrint(this.getActivity(),pathBase,1,true);
        c = (Chronometer) ll.findViewById(R.id.chronometer);

        btnAvvia = (FloatingActionButton) ll.findViewById(R.id.btnAvviaRegistrazione);
        btnFine = (FloatingActionButton) ll.findViewById(R.id.btnFineRegistrazione);
        btnAnnulla = (FloatingActionButton) ll.findViewById(R.id.btnAnnullaRegistrazione);
        btnFine.setEnabled(false);
        btnAnnulla.setEnabled(false);

        btnAvvia.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LogAndroid.info(TAG, "Avvia Registrazione Click");
                c.setBase(SystemClock.elapsedRealtime());
                c.start();

                Toast toa = Toast.makeText(getActivity(), "Avvio della sessione di ascolto in corso ...", Toast.LENGTH_SHORT);
                toa.setGravity(Gravity.CENTER, 0, 0);
                toa.show();
                runnableRecordingTask = new Runnable() {
                       public void run() {
                           try {
                               MainActivity m = (MainActivity) getActivity();
                               riconoscimento.setUser_name(m.getUserName());
                               riconoscimento.startRec();
                               recordingHandler.postDelayed(this,21000);
                           } catch (Exception e) {
                               //disattivo l'handler ciclico e resetto il timer
                               recordingHandler.removeCallbacks(this);
                               e.printStackTrace();
                           }
                       }
                };

                getActivity().runOnUiThread(runnableRecordingTask);

                btnFine.setEnabled(true);
                btnAvvia.setEnabled(false);
            }
        });

        btnFine.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int optime = (int) (SystemClock.elapsedRealtime() - c.getBase());
                LogAndroid.info(TAG, "Fine Registrazione Click");
                c.setBase(SystemClock.elapsedRealtime());

                c.stop();
                c.setFormat(null);

                //riconoscimento.fineRegistrazione(false);
                try {
                    recordingHandler.removeCallbacks(runnableRecordingTask);
                    riconoscimento.stopRec();
                    //riconoscimento.writeApplicationLog(optime);
                    //riconoscimento.deleteRecordedFiles();

                    Toast mess = Toast.makeText(getActivity(), "Chiusura della sessione di ascolto in corso... Grazie!", Toast.LENGTH_SHORT);
                    mess.setGravity(Gravity.CENTER, 0, 0);
                    mess.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnFine.setEnabled(false);
                btnAvvia.setEnabled(true);
            }
        });

        btnAnnulla.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LogAndroid.info(TAG, "Annulla Registrazione Click");
                c.setBase(SystemClock.elapsedRealtime());
                c.stop();
                c.setFormat(null);
                //  fineRegistrazione(false);
                btnFine.setEnabled(false);
                btnAvvia.setEnabled(true);
            }
        });
        return ll;
    }

    public Handler getRecordingHandler(){
        return this.recordingHandler;
    }

    public Runnable getRunnableRecordingTask() {
        return this.runnableRecordingTask;
    }

    public FloatingActionButton getBtnFine() {
        return btnFine;
    }

}
