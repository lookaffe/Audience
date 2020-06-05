package intersistemi.it.afp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Properties;

import basfp.it.bas3.support.LogAndroid;
import intersistemi.it.afp.BuildConfig;
import intersistemi.it.afp.fragment.ClockFragment;
import intersistemi.it.afp.fragment.FingerFragment;
import intersistemi.it.afp.fragment.HomeFragment;
import intersistemi.it.afp.fragment.LogFragment;
import intersistemi.it.afp.fragment.SettingsFragment;
import intersistemi.it.afp.R;
import intersistemi.it.afp.util.Util;

public class MainActivity extends AppCompatActivity
{

    FragmentManager fm;
    FragmentTransaction ft;
    Fragment fragmentHome=null;
    Fragment fragmentFinger=null;
    Fragment fragmentSettings=null;
    Fragment fragmentClock=null;
    Fragment fragmentLog=null;
    private Util util;
    private static Context mContext;
    private MainActivity mainActivity;
    private final int MY_PERMISSIONS_REQUEST = 1;
    private ProgressDialog dialog;

    // Menù bottom di navigazione all'interno del MainActivity
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        // mostra il Fragment corrispondente al click sul bottone
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    mostraFragment(fragmentHome);
                    return true;
                case R.id.navigation_finger:
                    mostraFragment(fragmentFinger);
                    return true;
                case R.id.navigation_logs:
                    mostraFragment(fragmentLog);
                    return true;
            }
            return false;
        }

    };

    private ProgressBar progressBar;

    public Fragment getFragmentHome() {
        return fragmentHome;
    }

    public FragmentManager getFm() {
        return fm;
    }

    public FragmentTransaction getFt() {
        return ft;
    }


    // metodo per mostrare il fragment voluto
    private void mostraFragment(Fragment fShow){
        ft = fm.beginTransaction();
        for(Fragment f:fm.getFragments())
        {
            if(f.equals(fShow))
                ft.show(f);
            else
                ft.hide(f);

        }
        ft.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // getSupportFragmentManager().putFragment(outState, "fragmentHome", fragmentHome);
        super.onSaveInstanceState(outState);
    }

    public int getValidSampleRates() {
        int r=8000;
        for (int rate : new int[] {8000,11025,16000,22050,44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO , AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                r= rate;
            }
        }
        return r;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // To get preferred buffer size and sampling rate.
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        String rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String size = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        Log.d("sample rate", "Size :" + size + " - Rate :" + rate);

        //Questi settagaggi servono per abilitare i permessi di rete verso l'esterno.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        controllaPermessi(); // pop up di accettazione dei permessi

        mContext = getApplicationContext();

        // inizializza i fragment
        fragmentFinger = FingerFragment.newInstance();
        //fragmentSettings =new SettingsFragment();
        //fragmentClock= new ClockFragment();
        fragmentLog=new LogFragment();
        fragmentHome=new HomeFragment();
        progressBar = new ProgressBar(mContext);




        //(Spinner) findViewById(R.id.progressBar);
        if (savedInstanceState != null) {
            //Restore the fragment's instance
           // fragmentHome = getSupportFragmentManager().getFragment(savedInstanceState, "fragmentHome");
        }
        util= new Util();

        // carica tutti i fragment e nascondi tutti tranne quello di log in
        fm= getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.add(android.R.id.content,fragmentHome,"fragmentHomeTAG"); // fragment di login
        ft.add(android.R.id.content,fragmentFinger);    // fragment di registrazione
        ft.hide(fragmentFinger);
        ft.add(android.R.id.content,fragmentLog);
        ft.hide(fragmentLog);
        ft.commit();

        //fragmentClock.setMenuVisibility(false);
        //fragmentSettings.setMenuVisibility(false);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        util.switchButton(false,(MenuView.ItemView)findViewById(R.id.navigation_finger));
        //util.switchButton(false,(MenuView.ItemView)findViewById(R.id.navigation_clock));
        util.switchButton(false,(MenuView.ItemView)findViewById(R.id.navigation_logs));

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // genera il menù in alto a destra
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public String getProperties(String key){
        Properties properties = new Properties();
        String res ="";
        try{
            properties.load(this.getAssets().open("appascolto.properties"));
            res =  properties.get(key).toString();
            //System.out.println("SERVER_URL: "+properties.get("SERVER_URL"));
        }catch(Exception e){
            res="ERROR";
            e.printStackTrace();
        }

        return res;
    }


    // gestore dei click sul fragment
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId(); // id dell'item del menù premuto

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_logoff || id == R.id.action_quit) {

        FingerFragment fff = (FingerFragment) fragmentFinger; // dichiaro il fragment che mi gestisce il login
        fff.getBtnFine().performClick(); //Fermo l'handler che gestisce il processo automatico temporizzato di analisi audio ed invio dati.

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.home_login); // carico il layout del login
        rl.removeAllViews();

        rl.addView(View.inflate(this, R.layout.fragment_home, null)); // aggiungi la vista con il layout di login

        TextView bn =(TextView) findViewById(R.id.build_number);
        bn.setText("Build no. : "+ BuildConfig.VERSION_NAME+"\nContatti: infoaudience@csa.intersistemi.it");

        HomeFragment f = (HomeFragment) getSupportFragmentManager().findFragmentByTag("fragmentHomeTAG");
        f.logged = false;

        Button btnAccediInHomeFragment= (Button) findViewById(R.id.login);
        btnAccediInHomeFragment.setOnClickListener( f.btnAccediOnClick);

        util.switchButton(false,(MenuView.ItemView)findViewById(R.id.navigation_finger));
        util.switchButton(false,(MenuView.ItemView)findViewById(R.id.navigation_logs));

        fm= getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.hide(fragmentFinger);
        ft.hide(fragmentLog);
        ft.show(fragmentHome);

        ft.commit();

        // esegui il logout
        if (id == R.id.action_logoff) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        // chiudi l'app
        if (id == R.id.action_quit){
            MainActivity.this.finish();
            System.exit(0);
        }
        //return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    // richiede di poter utilizzare microfono etc..
    private void controllaPermessi()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST);
                // MY_PERMISSIONS_REQUEST is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


    }

    // gestisce il risultato dell'accettazione dei permessi
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    LogAndroid.info(Util.TAG,"permessi ok");
                }
                else
                {
                    LogAndroid.info(Util.TAG,"permessi negati");
                   // System.exit(0);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }


    }

    //Need to override to prevent logging out after Android back button is pressed.
    @Override
    public void onBackPressed() {
        //Need to override to prevent logging out after Android back button is pressed.
        //Nothing to do here.
    }
}