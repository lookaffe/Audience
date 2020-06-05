package bas3.it.basfp;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import basfp.it.bas3.support.HttpRequest;
import basfp.it.bas3.support.LogAndroid;
import basfp.it.bas3.support.Utility;
import edu.gvsu.masl.echoprint.AudioFingerprinter;
import edu.gvsu.masl.echoprint.AudioFingerprinter.AudioFingerprinterListener;
import intersistemi.it.afp.R;
import intersistemi.it.afp.util.Util;

import static intersistemi.it.afp.util.Util.TAG;

public class FingerPrint extends IntentService implements AudioFingerprinterListener
{
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_TEMP_FILE = "rec_temp";

    private static final int RECORDER_SAMPLERATE = 11025;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private boolean isRecording = false;
    private int secondsToRecord;
    private Utility basFp;
    private Util util;
    private static final String DIR_FP = "/bas_fp/";
    private static final String DIR_WAVE = "/bas_wave/";
    private static final String DIR_TEMP = "/bas_temp/";
    private static final String FILE_USER = "user.txt";
    private static final String SERVER_NOT_FOUND_TITLE = "Server non raggiungibile";
    private static final String SERVER_NOT_FOUND_BODY = "Il server non risulta essere raggiungibile. Ricontrollare la connessione di rete e riprovare, grazie.";
    private String PATH_FP, FILE_FP,PATH_WAVE,FILE_WAVE,PATH_TEMP;
    private File FILE_TEMP;
    String pathBase;
    private Thread recordingThread = null;
    private String idRegistrazione;
    private String startRec;
    private String stopRec;
    private String precodefp;
    private short[] preaudio;
    private byte[] audioData;
    private int minSplitFile=3;
    FragmentActivity activity;
    private ScheduledExecutorService exec=null;
    private ScheduledExecutorService execAuto=null;
    private boolean makeToast=false;
    Context context=null;
    private Properties properties;
    private ProgressDialog dialog;
    private View ll;
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private static final int AUDIO_TRUNK_MAX_DURATION_MS = 10000;
    private static final String LOG_FILENAME = "loguploads.txt";
    private android.util.Log Log;

    private TextView userLogged;

    private String user_name;

    boolean recording, resolved;
    AudioFingerprinter fingerprinter;

    int count = 0;

    public FingerPrint()
    {
        super("FingerPrint");
    }


    public FingerPrint(FragmentActivity activity, String pathBase, int minSplitFile,boolean makeToast)
    {
        super("FingerPrint");
        this.activity=activity;
        this.minSplitFile=minSplitFile;
        this.pathBase=pathBase;
        this.makeToast=makeToast;
        context=activity.getApplicationContext();
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        util= new Util();


    }

    private String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public void createFP() throws Exception {
        it.geosystems.csa.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.Builder acb= it.geosystems.csa.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.newBuilder();

        // fingerprinting the audio file
        if(fingerprinter == null)
            fingerprinter = new AudioFingerprinter(FingerPrint.this);

        String codefp = fingerprinter.getCode();
        short[] audio = fingerprinter.getAudioData();

        fingerprinter.fingerprint(20, false);

        if(codefp!=null && codefp!=precodefp){ //questo if serve per controllare che ci sia una fingerprint da spedire e sia diversa dalla precedente. Può essere sostituito con un corretto controllo sui thread
            //manda fp
            ByteString bs=ByteString.copyFrom(codefp, "UTF-8");

            acb.setData(bs); //invia il file audio
            //nuovi campi per invio info utente

            //spedisce i dati al server
            LogAndroid.info("USER",user_name);
            acb.setUsername(user_name);
            acb.setDatainvio(System.currentTimeMillis());
            acb.setIdperiferica(Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));
            acb.setPassword("user_password");

            byte[] message =  acb.build().toByteArray();
            String urlstr = Util.getInstance().getProperty(context,Util.UPLOAD_SERVER_URL_PROPERTY);
            HttpRequest request = HttpRequest.post(urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
            request.send(message);
            String result = request.body();

            precodefp=codefp;
        }

        if(audio != null && audio!=preaudio) {
            //writeWav(audio);
            preaudio = audio;
        }
    }

    private void getSoundNotification(){
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {

                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(context, notification);
                mp.start();

                FloatingActionButton btnFine = (FloatingActionButton) activity.findViewById(R.id.btnFineRegistrazione);
                btnFine.performClick();

                Toast mess = Toast.makeText(activity, "Errore di comunicazione con il server! Ricontollare le impostazioni di rete e riprovare tra qualche minuto, grazie!", Toast.LENGTH_LONG);
                mess.setGravity(Gravity.CENTER,0,0);
                mess.show();
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        LogAndroid.info(TAG, "INIZIO SCHEDULAZIONE");
        final String action = intent.getAction();
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        util= new Util();

        minSplitFile=1;
        context=getApplicationContext();
    }


    private void inizializzaPath()
    {
        PATH_WAVE=pathBase+DIR_WAVE;
        LogAndroid.info("path per wave", PATH_WAVE);
        File app1= new File(PATH_WAVE);
        if(!app1.exists())
            app1.mkdirs();
    }

    public void setUser_name(String name){
        this.user_name = name;
    }

    public void startRec() throws IOException {

        new CodificaTask().execute(false);
    }

    /**
     * Stops a recording that has been previously started.
     */
    public void stopRec() {
        if(recording)
        {
            fingerprinter.stop();
        }
    }

    public void writeApplicationLog(long opTime){
        Locale locale = new Locale(Locale.ITALIAN.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);
        Date today = new Date();
        String dataStr = sdf.format(today);
        String logOperation = dataStr +" - Durata ascolto: "+Math.floor(opTime/1000)+" sec." ;
        appendLog(logOperation);
    }





    public void fineRegistrazione(boolean avvioDaSchedulatore)
    {
        LogAndroid.info(TAG, "fineRegistrazione");
        stopRec = util.getCurrectTimestamp();

        if (null != recorder)
        {
            isRecording = false;

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();

            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        // Ora questo viene fatto dentro al thread del timer

        /*
        FILE_WAVE=getFilename(PATH_WAVE,".wav");
        copyWaveFile(PATH_TEMP+File.separator+AUDIO_RECORDER_TEMP_FILE, FILE_WAVE);
        */
        new CodificaTask().execute(avvioDaSchedulatore);


    }

    public void deleteRecordedFiles()
    {
        new Thread(new Runnable() {
            public void run() {
                try
                {
                    File dirWave = new File(PATH_WAVE);
                    File[] fpw = dirWave.listFiles();
                    for (File fi : fpw) {
                        fi.delete();
                    }
                }
                catch(final Exception e)
                {
                    LogAndroid.info(TAG, "deleteRecordedFiles "+e.getMessage() + " on " + PATH_WAVE);
                }
            }
        }).start();
    }

    public void didFinishListening()
    {
        recording = false;
    }

    public void didFinishListeningPass()
    {}

    public void willStartListening()
    {
        recording = true;
        resolved = false;
    }

    public void willStartListeningPass()
    {}

    public void didGenerateFingerprintCode(String code)
    {
    }

    public void didFindMatchForCode(final Hashtable<String, String> table,
                                    String code)
    {
        resolved = true;
    }

    public void didNotFindMatchForCode(String code)
    {
        resolved = true;
    }

    public void didFailWithException(Exception e)
    {
        resolved = true;
    }

    private class CodificaTask extends AsyncTask<Boolean,Boolean,Boolean>
    {
        private String excRaised ="";
        private Throwable ezz;
        @Override
        protected Boolean doInBackground(Boolean... avvioDaSchedulatore)
        {
/*
            if ((new File(FILE_WAVE)).exists())
            {

 */
            try {
                createFP();
            } catch (Exception e) {
                excRaised = e.getMessage();
                ezz=e;
            }
            /*}
            else
            {
                LogAndroid.info(TAG, "FILE NOT FOUND " + FILE_WAVE);
            }

             */
            return false;

        }

        @Override
        protected void onPostExecute(Boolean avvioDaSchedulatore)
        {

            if (!excRaised.equals("")){

                getSoundNotification();

            }

            /*
            if (!avvioDaSchedulatore)
            {
                if(exec!=null)
                    exec.shutdown();
                if(execAuto!=null)
                    execAuto.shutdown();


                FILE_TEMP.delete();
                LogAndroid.info(TAG, "FINE SCHEDULAZIONE");
            }
            */
            //dialog.hide();
            //deleteRecordedFiles();
        }

    }


    private String getFilename(String dir,String estensione)
    {
        String uuid = "";
        try
        {
            uuid = Settings.Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            // LogAndroid.info(Util.TAG, "this.getApplicationContext().getContentResolver()");
        }
        catch(Exception e)
        {
            uuid = Settings.Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID);
            // LogAndroid.info(Util.TAG, "context.getContentResolver()");
        }
        String fileName = dir + uuid + "_" +idRegistrazione+"_"+startRec + "_" + stopRec + estensione;
        return (fileName);
    }

    public void appendLog(String text)
    {
        File logFile = new File(pathBase+"/"+LOG_FILENAME);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
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
            TextView output = (TextView) activity.findViewById(R.id.textView);
            output.append(text);
            output.setMovementMethod(new ScrollingMovementMethod());
            if (output!=null) {
                output.refreshDrawableState();
                output.invalidate();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}