package intersistemi.it.afp.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import basfp.it.bas3.support.HttpRequest;
import intersistemi.it.afp.BuildConfig;
import intersistemi.it.afp.R;
import intersistemi.it.afp.activity.MainActivity;
import intersistemi.it.afp.util.Util;


public class HomeFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private EditText editUser;
    private EditText editPassword;
    private Button btnAccedi = null;
    private View ll;
    public boolean logged;
    private ProgressDialog dialog;
    private TextView userLogged;
    private Util util;
    private String pathBase, LOG_PATH, PATH_TEMP;



    public HomeFragment()
    {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2)
    {
        HomeFragment fragment = new HomeFragment();
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
        util= new Util();

    }


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
    {
        LOG_PATH = getArguments().getString("log_path");
        pathBase= LOG_PATH + File.separator + getString(R.string.app_name);

        Toast mess2 = Toast.makeText(getActivity(), pathBase, Toast.LENGTH_SHORT);
        mess2.setGravity(Gravity.CENTER, 0, 0);
        mess2.show();

        if(logged)
            ll = inflater.inflate(R.layout.fragment_home_logged, container, false);
        else
            ll = inflater.inflate(R.layout.fragment_home, container, false);

        btnAccedi = (Button) ll.findViewById(R.id.login);
        btnAccedi.setOnClickListener(btnAccediOnClick);
        TextView bn =(TextView) ll.findViewById(R.id.build_number);
        bn.setText("Build no. : "+ BuildConfig.VERSION_NAME+"\nContatti: infoaudience@csaresearch.it");

        return ll;
    }

    public View.OnClickListener btnAccediOnClick= new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            editUser =(EditText) ll.findViewById(R.id.user);
            editPassword=(EditText) ll.findViewById(R.id.password);
            if(editUser.getText().length()>0 && editPassword.getText().length()>0)
            {
                try{

                    it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.Builder acb= it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.newBuilder();
                    //nuovi campi per invio info utente
                    acb.setUsername(editUser.getText().toString());
                    acb.setDatainvio(System.currentTimeMillis());
                    acb.setIdperiferica(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));
                    acb.setPassword(editPassword.getText().toString());

                    byte[] message =  acb.build().toByteArray();
                    String login_urlstr = Util.getInstance().getProperty(getActivity(), Util.LOGIN_SERVER_URL_PROPERTY);
                    HttpRequest request = HttpRequest.post(login_urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
                    request.send(message);
                    String result = request.body();

                    logged = true;
                    Date dNow = new Date();
                    SimpleDateFormat ft = new SimpleDateFormat("ddMMyyyy-HHmmss");
                    util.updateLog(editUser.getText().toString()+ " logged at " + ft.format(dNow));
                    new LoginTask().execute(result.toString());


                }catch(final Exception te){

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast mess = Toast.makeText(getActivity(),"Errore di connessione al server di login. Ricontrollare i parametri di rete e riprovare più tardi, grazie." + te, Toast.LENGTH_SHORT);
                            mess.setGravity(Gravity.CENTER,0,0);
                            mess.show();
                        }});
                }
            }
            else
            {
                editUser.setError("inserire un utente");
                editPassword.setError("inserire una password");
                logged=false;
            }

        }
    };

    private class LoginTask extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... jsonResult)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog = ProgressDialog.show(getActivity(),"Attendere", "Accesso in corso... ", false, false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.show();
                }
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return jsonResult[0];
        }



        @Override
        protected void onPostExecute(String user)
        {
            try {
                dialog.hide();

                JSONObject jsonResponse = new JSONObject(user);

                JSONArray data = jsonResponse.getJSONArray("data");
                if (data.getJSONObject(0).getString("esito").equals("FOUND")){

                    MainActivity v = (MainActivity) getActivity();
                    v.setUser(data.getJSONObject(0).getString("nome"));

                    RelativeLayout rl = (RelativeLayout) ll.findViewById(R.id.home_login);
                    rl.removeAllViews();
                    rl.addView(View.inflate(getContext(), R.layout.fragment_home_logged, null));
                    userLogged=(TextView) ll.findViewById(R.id.userLogged);

                    userLogged.setText(data.getJSONObject(0).getString("nome")+" "+data.getJSONObject(0).getString("cognome"));
                    util.switchButton(true,(MenuView.ItemView)getView().getRootView().findViewById(R.id.navigation_finger));
                    //util.switchButton(true,(MenuView.ItemView)getView().getRootView().findViewById(R.id.navigation_clock));
                    util.switchButton(true,(MenuView.ItemView)getView().getRootView().findViewById(R.id.navigation_logs));
                    //util.switchButton(true,(MenuView.ItemView)getView().getRootView().findViewById(R.id.navigation_settings));
                    super.onPostExecute(user);
                }else{
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast mess = Toast.makeText(getActivity(),"Utente non trovato!", Toast.LENGTH_SHORT);
                            mess.setGravity(Gravity.CENTER,0,0);
                            mess.show();
                        }});
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast mess = Toast.makeText(getActivity(),"Errore di connessione al server di login. Ricontrollare i parametri di rete e riprovare più tardi, grazie.", Toast.LENGTH_SHORT);
                        mess.setGravity(Gravity.CENTER,0,0);
                        mess.show();
                    }});
            }


        }

    }

}
