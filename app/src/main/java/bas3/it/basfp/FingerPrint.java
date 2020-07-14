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

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

    private static final int RECORDER_SAMPLERATE = 22050;
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
        inizializzaPath();
        this.makeToast=makeToast;
        context=activity.getApplicationContext();
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        util= new Util();
    }

    private String readFile(String fileName) throws IOException {
        util.updateLog("Fingerprint - readFile");
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        }
        finally {
            br.close();
        }
    }

    public void createFP() throws Exception {

        // fingerprinting the audio file
        if(fingerprinter == null)
            fingerprinter = new AudioFingerprinter(FingerPrint.this);
        //android.util.Log.d("FINGERPRINT IS RUNNING",Boolean.toString(fingerprinter.isItRunning()));

        String codefp = fingerprinter.getCode();
        short[] audio = fingerprinter.getAudioData();
        if(!fingerprinter.isItRunning()) {
            fingerprinter.fingerprint(20, false);
        }

        //if(checkAndSendFP(codefp)) precodefp = codefp;

        if (audio != null && audio != preaudio) {
            preaudio = audio;
            saveAndSendAudio(audio);
        }
    }

    private boolean checkAndSendFP(String fpcode) throws UnsupportedEncodingException {

        it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.Builder acb= it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.newBuilder();

        if (fpcode != null && fpcode != precodefp) { //questo if serve per controllare che ci sia una fingerprint da spedire e sia diversa dalla precedente. Può essere sostituito con un corretto controllo sui thread
            util.updateLog("Fingerprint - checkAndSendFP");
            //manda fp

            ByteString bs = ByteString.copyFrom(fpcode, "UTF-8");

            acb.setData(bs); //invia il file audio
            //nuovi campi per invio info utente

            //spedisce i dati al server
            acb.setUsername(user_name);
            acb.setDatainvio(System.currentTimeMillis());
            acb.setIdperiferica(Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));
            acb.setPassword("user_password");

            byte[] message = acb.build().toByteArray();
            String urlstr = Util.getInstance().getProperty(context, Util.UPLOAD_SERVER_URL_PROPERTY);
            HttpRequest request = HttpRequest.post(urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
            request.send(message);
            String result = request.body();

            return true;

        } else return false;

    }

    private void saveAndSendAudio(short[] audioShort) throws IOException {
        FILE_WAVE = new String(pathBase + "/" + System.currentTimeMillis()+ ".wav");
        LogAndroid.info("PATH WAVE", FILE_WAVE);
        OutputStream out = new FileOutputStream(FILE_WAVE);
        PCMtoFile(out,audioShort, RECORDER_SAMPLERATE,1,16);

        byte[] wavData = FileUtils.readFileToByteArray(new File(FILE_WAVE));
        ByteString bs=ByteString.copyFrom(wavData);
        it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.Builder acb= it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.newBuilder();
        /* invece di passare bs come ByteArray del wav, generiamo qui la fingerprint e passiamo quella

         */
        Log.d("","Sensitive message");
        acb.setData(bs);
        //nuovi campi per invio info utente
        acb.setUsername(user_name);
        acb.setDatainvio(System.currentTimeMillis()); //android.os.Build.MODEL +"-"+
        acb.setIdperiferica(Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID));
        acb.setPassword("giacomo_password");

        byte[] message =  acb.build().toByteArray();
        String urlstr = Util.getInstance().getProperty(context,Util.UPLOAD_SERVER_URL_PROPERTY);
        //HttpRequest request = HttpRequest.post(urlstr).contentType("application/x-www-form-urlencoded").connectTimeout('\uea60').disconnect();
        //HttpRequest request = HttpRequest.post(urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
        HttpRequest request = HttpRequest.post(urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
        request.send(message);
        String result = request.body();

        //deleteRecordedFile();
    }

    /**
     * Write PCM data as WAV file
     * @param os  Stream to save file to
     * @param pcmdata  8 bit PCMData
     * @param srate  Sample rate - 8000, 16000, etc.
     * @param channel Number of channels - Mono = 1, Stereo = 2, etc..
     * @param format Number of bits per sample (16 here)
     * @throws IOException
     * Salva un file ma questo non è leggibile (forse problema Big Endian, Little endian?)
     */
    public void PCMtoFile(OutputStream os, short[] pcmdata, int srate, int channel, int format) throws IOException {
        byte[] header = new byte[44];
        byte[] data = get16BitPcm(pcmdata);

        long totalDataLen = data.length + 36;
        long bitrate = srate * channel * format;

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = (byte) format;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channel;
        header[23] = 0;
        header[24] = (byte) (srate & 0xff);
        header[25] = (byte) ((srate >> 8) & 0xff);
        header[26] = (byte) ((srate >> 16) & 0xff);
        header[27] = (byte) ((srate >> 24) & 0xff);
        header[28] = (byte) ((bitrate / 8) & 0xff);
        header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
        header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
        header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
        header[32] = (byte) ((channel * format) / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (data.length  & 0xff);
        header[41] = (byte) ((data.length >> 8) & 0xff);
        header[42] = (byte) ((data.length >> 16) & 0xff);
        header[43] = (byte) ((data.length >> 24) & 0xff);

        os.write(header, 0, 44);
        os.write(data);
        os.close();
    }

    public byte[] get16BitPcm(short[] data) {
        byte[] resultData = new byte[2 * data.length];
        int iter = 0;
        for (double sample : data) {
            short maxSample = (short)((sample));// * Short.MAX_VALUE));
            //resultData[iter++] = (byte)(maxSample  >>> 8);
            resultData[iter++] = (byte)(maxSample & 0x00ff);
            resultData[iter++] = (byte)((maxSample & 0xff00) >>> 8); //originale
        }
        return resultData;
    }

    public void deleteRecordedFile()
    {
        new Thread(new Runnable() {
            public void run() {
                try
                {
                    File dirWave = new File(FILE_WAVE);
                    dirWave.delete();
                    LogAndroid.info("DELETE FILE", FILE_WAVE + " deleted!");
                }
                catch(final Exception e)
                {
                    LogAndroid.info(TAG, "deleteRecordedFiles "+e.getMessage());
                }
            }
        }).start();
    }

    private void getSoundNotification(){
        util.updateLog("Fingerprint - getSoundNotification");
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
    protected void onHandleIntent(Intent intent){
        util.updateLog("Fingerprint - onHandleIntent");
        LogAndroid.info(TAG, "INIZIO SCHEDULAZIONE");
        final String action = intent.getAction();
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        util= new Util();

        minSplitFile=1;
        context=getApplicationContext();
    }


    private void inizializzaPath(){
        File app1= new File(pathBase);
        if(!app1.exists())
            app1.mkdirs();
    }

    public void setUser_name(String name){
        this.user_name = name;
    }

    public void startRec() throws IOException {
        //util.updateLog("Fingerprint - startRec");
        new CodificaTask().execute(false);
    }

    /**
     * Stops a recording that has been previously started.
     */
    public void stopRec() {
        if(recording)
        {
            util.updateLog("Fingerprint - stopRec");
            fingerprinter.stop();
        }
    }

    public void writeApplicationLog(long opTime){
        util.updateLog("Fingerprint - writeApplicationLog");
        Locale locale = new Locale(Locale.ITALIAN.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale);

        Date today = new Date(System.currentTimeMillis() - opTime);
        String dataStr = sdf.format(today);
        String logOperation = dataStr +" - Durata ascolto: "+Math.floor(opTime/1000)+" sec." ;

        appendLog(logOperation);
    }

    public void fineRegistrazione(boolean avvioDaSchedulatore)
    {
        util.updateLog("Fingerprint - fineRegistrazione");
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
        new CodificaTask().execute(avvioDaSchedulatore);
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
            try {
                //if( !fingerprinter.isItRunning())
                    createFP();
            } catch (Exception e) {
                util.updateLog("Fingerprint - CodificaTask | " + e);
                excRaised = e.getMessage();
                ezz=e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean avvioDaSchedulatore)
        {
            util.updateLog("Fingerprint - onPostExecute");
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


    private String getFilename(String dir,String estensione) {
        util.updateLog("Fingerprint - getFilename");
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
        util.updateLog("Fingerprint - appendLog");
        File logFile = new File(pathBase+"/"+LOG_FILENAME);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                util.updateLog("Fingerprint - appendLog | " + e);
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
            util.updateLog("Fingerprint - appendLog | " + e);
            e.printStackTrace();
        }
    }

}