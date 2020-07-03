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
import android.os.Environment;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import basfp.it.bas3.support.HttpRequest;
import basfp.it.bas3.support.LogAndroid;
import basfp.it.bas3.support.Utility;
import intersistemi.it.afp.R;
import intersistemi.it.afp.util.Util;

import static intersistemi.it.afp.util.Util.TAG;


public class FingerPrint_old extends IntentService
{
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_TEMP_FILE = "rec_temp";

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
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


    public FingerPrint_old()
    {
        super("FingerPrint");
    }


    public FingerPrint_old(FragmentActivity activity, String pathBase, int minSplitFile, boolean makeToast)
    {
        super("FingerPrint");
        this.activity=activity;
        this.minSplitFile=minSplitFile;
        this.pathBase=pathBase;
        this.makeToast=makeToast;
        context=activity.getApplicationContext();
        bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        util= new Util();
    }

    public void createFP(String file) throws Exception {
            /*
            ByteArrayOutputStream ous = null;
            InputStream ios = null;
            try {
                byte[] buffer = new byte[bufferSize];
                ous = new ByteArrayOutputStream();
                ios = new FileInputStream(file);
                int read = 0;
                while ((read = ios.read(buffer)) != -1) {
                    ous.write(buffer, 0, read);
                }
            }catch(Exception e){
                LogAndroid.info(TAG, e.getMessage());
            }finally {
                try {
                    if (ous != null)
                        ous.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (ios != null)
                        ios.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            */

            //audioData = ous.toByteArray();
            audioData = FileUtils.readFileToByteArray(new File(file));
            ByteString bs=ByteString.copyFrom(audioData);
            it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.Builder acb= it.geosystems.protocolbuffer.StreamerUploaderProtos.AudioChunkUploader.newBuilder();
            /* invece di passare bs come ByteArray del wav, generiamo qui la fingerprint e passiamo quella

             */
                        acb.setData(bs);
            //nuovi campi per invio info utente
            acb.setUsername("giacomo_test");
            acb.setDatainvio(System.currentTimeMillis());
            acb.setIdperiferica(Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID));
            acb.setPassword("giacomo_password");

            byte[] message =  acb.build().toByteArray();
            String urlstr = Util.getInstance().getProperty(context,Util.UPLOAD_SERVER_URL_PROPERTY);
            //HttpRequest request = HttpRequest.post(urlstr).contentType("application/x-www-form-urlencoded").connectTimeout('\uea60').disconnect();
            //HttpRequest request = HttpRequest.post(urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
            HttpRequest request = HttpRequest.post(urlstr).contentType("application/octet-stream").connectTimeout('\uea60').disconnect();
            request.send(message);
            String result = request.body();
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
        bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        util= new Util();
        pathBase = Environment.getExternalStorageDirectory()+ File.separator + getString(R.string.app_name);
        minSplitFile=1;
        context=getApplicationContext();
        startAutoRecording();
    }

    private void startAutoRecording()
    {
        LogAndroid.info(TAG, "startAutoRecording");
        avviaRegistrazione(false,true);
    }


    private void inizializzaPath()
    {

        PATH_WAVE=pathBase+DIR_WAVE;
        File app1= new File(PATH_WAVE);
        if(!app1.exists())
            app1.mkdirs();

    }


    public void startRec() throws IOException {

        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED))  {
            throw new IOException("SD Card is not mounted.  It is " + state + ".");
        }

        // make sure the directory we plan to store the recording in exists
        inizializzaPath();

        File storageDir = new File(pathBase + DIR_WAVE);
        storageDir.mkdir();
        File outfile=File.createTempFile(AUDIO_RECORDER_TEMP_FILE, ".wav",storageDir);
        FILE_WAVE = outfile.getAbsolutePath();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outfile.getAbsolutePath());

        mediaRecorder.setAudioChannels(2);
        mediaRecorder.setAudioSamplingRate(RECORDER_SAMPLERATE);

        mediaRecorder.setMaxDuration(AUDIO_TRUNK_MAX_DURATION_MS);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
                    mr.release();

                    new CodificaTask().execute(false);
                }
            }
        });

        mediaRecorder.prepare();
        mediaRecorder.start();
    }

    /**
     * Stops a recording that has been previously started.
     */
    public void stopRec() throws IOException {
        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.release();
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


    public void avviaRegistrazione(boolean avvioDaSchedulatore,boolean autoStart)
    {
        inizializzaPath();
        LogAndroid.info(TAG, "avviaRegistrazione");
        if(idRegistrazione==null)
            idRegistrazione = util.getRandommIdRegistrazione();


        startRec = util.getCurrectTimestamp();
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();


        /*
        if(!avvioDaSchedulatore)
        {
            LogAndroid.info(TAG, "Executors.avvioDaSchedulatore");
            exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    gestisciRegistrazione();
                }
            }, minSplitFile, minSplitFile, TimeUnit.MINUTES);
        }
        if(autoStart)
        {
            LogAndroid.info(TAG, "Executors.autoStart");
            execAuto = Executors.newSingleThreadScheduledExecutor();
            execAuto.schedule(new Runnable()
            {
                @Override
                public void run()
                {
                    fineRegistrazione(false);
                }
            }, 180, TimeUnit.SECONDS);
        }
        */

    }

    /*
    public String getProperty(String key){
        try{
            if (null==this.properties){
                this.properties = new Properties();
                this.properties.load(context.getAssets().open(Util.PROPERTY_FILENAME));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return this.properties.get(key).toString();
    }
    */


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

        FILE_WAVE=getFilename(PATH_WAVE,".wav");
        copyWaveFile(PATH_TEMP+File.separator+AUDIO_RECORDER_TEMP_FILE, FILE_WAVE);
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
                    LogAndroid.info(TAG, "deleteRecordedFiles "+e.getMessage());
                }
            }
        }).start();
    }

    public void gestisciRegistrazione()
    {
        LogAndroid.info(TAG, "gestisciRegistrazione");
        fineRegistrazione(true);
        avviaRegistrazione(true,false);
    }

    private void writeAudioDataToFile()
    {
        byte data[] = new byte[bufferSize];
        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(FILE_TEMP);
        }
        catch (FileNotFoundException e)
        {
            LogAndroid.info(TAG, "writeAudioDataToFile FileNotFoundException "+e.getMessage());
        }

        int read = 0;

        if (null != os)
        {
            while (isRecording)
            {
                read = recorder.read(data, 0, bufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read)
                {
                    try
                    {
                        os.write(data);
                    }
                    catch (IOException e)
                    {
                        LogAndroid.info(TAG, "writeAudioDataToFile IOException1 "+e.getMessage());
                    }
                }
            }

            try
            {
                os.close();
            }
            catch (IOException e)
            {
                LogAndroid.info(TAG, "writeAudioDataToFile IOException2 "+e.getMessage());
            }
        }
    }

    private void copyWaveFile(String inFilename, String outFilename)
    {
        LogAndroid.info(TAG, "copyWaveFile");
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
        byte[] data = new byte[bufferSize];
        audioData = new byte[bufferSize];
        try
        {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            while (in.read(data) != -1)
            {
                out.write(data);
            }


            in.close();
            out.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException
    {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
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
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private class CodificaTask extends AsyncTask<Boolean,Boolean,Boolean>
    {
        private String excRaised ="";
        private Throwable ezz;
        @Override
        protected Boolean doInBackground(Boolean... avvioDaSchedulatore)
        {

            if ((new File(FILE_WAVE)).exists())
            {
                try {
                    createFP(FILE_WAVE);
                } catch (Exception e) {
                    excRaised = e.getMessage();
                    ezz=e;
                }
            }
            else
            {
                LogAndroid.info(TAG, "FILE NOT FOUND " + FILE_WAVE);
            }
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
            uuid = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
           // LogAndroid.info(Util.TAG, "this.getApplicationContext().getContentResolver()");
        }
        catch(Exception e)
        {
            uuid = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID);
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