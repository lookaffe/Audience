/**
 * AudioFingerprinter.java
 * EchoprintLib
 *
 * Created by Alex Restrepo on 1/22/12.
 * Copyright (C) 2012 Grand Valley State University (http://masl.cis.gvsu.edu/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.gvsu.masl.echoprint;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * Main fingerprinting class<br>
 * This class will record audio from the microphone, generate the fingerprint code using a native library and query the data server for a match
 *
 * @author Alex Restrepo (MASL)
 *
 */
public class AudioFingerprinter implements Runnable
{
	private final int FREQUENCY = 11025;
	private final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
	private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private Thread thread;
	private volatile boolean isRunning = false;
	AudioRecord mRecordInstance = null;

	private short audioData[], audioDataCopy[];
	private int bufferSize;
	private int secondsToRecord;
	private volatile boolean continuous;
	public String code;

	private AudioFingerprinterListener listener;

	/**
	 * Constructor for the class
	 *
	 * @param listener is the AudioFingerprinterListener that will receive the callbacks
	 */
	public AudioFingerprinter(AudioFingerprinterListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Starts the listening / fingerprinting process using the default parameters:<br>
	 * A single listening pass of 20 seconds 
	 */
	public void fingerprint()
	{
		// set default listening time to 20 seconds
		this.fingerprint(10);
	}

	/**
	 * Starts a single listening / fingerprinting pass
	 *
	 * @param seconds the seconds of audio to record.
	 */
	public void fingerprint(int seconds)
	{
		// no continuous listening
		this.fingerprint(seconds, false);
	}

	/**
	 * Starts the listening / fingerprinting process
	 *
	 * @param seconds the number of seconds to record per pass
	 * @param continuous if true, the class will start a new fingerprinting pass after each pass
	 */
	public void fingerprint(int seconds, boolean continuous)
	{
		if(this.isRunning)
			return;

		this.continuous = continuous;

		// cap to 30 seconds max, 10 seconds min.
		this.secondsToRecord = Math.max(Math.min(seconds, 30), 10);

		// start the recording thread
		thread = new Thread(this);
		android.util.Log.d("HANDLER", "startato");
		thread.start();
	}

	/**
	 * stops the listening / fingerprinting process if there's one in process
	 */
	public void stop()
	{
		this.continuous = false;
		if(mRecordInstance != null)
			mRecordInstance.stop();
	}

	public String getCode()
	{
		String co = code;
		return co;
	}

	public short[] getAudioData()
	{
		return audioDataCopy;
	}

	public boolean isItRunning(){
		return this.isRunning;
	}

	/**
	 * The main thread<br>
	 * Records audio and generates the audio fingerprint, then it queries the server for a match and forwards the results to the listener.
	 */
	@Override
	public void run()
	{
		this.isRunning = true;
		try
		{
			// create the audio buffer
			// get the minimum buffer size
			int minBufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);

			// and the actual buffer size for the audio to record
			// frequency * seconds to record.
			bufferSize = Math.max(minBufferSize, this.FREQUENCY * this.secondsToRecord);

			audioData = new short[bufferSize];
			audioDataCopy = null;

			// start recorder
			mRecordInstance = new AudioRecord(
					MediaRecorder.AudioSource.MIC,
					FREQUENCY, CHANNEL,
					ENCODING, minBufferSize);

			willStartListening();

			mRecordInstance.startRecording();

			boolean firstRun = true;
			do
			{
				try
				{
					willStartListeningPass();

					long time = System.currentTimeMillis();
					// fill audio buffer with mic data.
					int samplesIn = 0;
					do
					{
						samplesIn += mRecordInstance.read(audioData, samplesIn, bufferSize - samplesIn);

						if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
							break;
					}
					while (samplesIn < bufferSize);
					//Log.d("Fingerprinter", "Audio recorded: " + (System.currentTimeMillis() - time) + " millis");

					// see if the process was stopped.
					if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED || (!firstRun && !this.continuous))
						break;

					audioDataCopy = audioData;

					// create an echoprint codegen wrapper and get the code
					time = System.currentTimeMillis();



					Thread ttt = new CodegenThread(samplesIn);
					ttt.start();

					if(code.length() == 0)
					{
						// no code?
						// not enough audio data?
						continue;
					}


					didGenerateFingerprintCode(code);

					// fetch data from echonest
					time = System.currentTimeMillis();

					firstRun = false;

					didFinishListeningPass();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Log.e("Fingerprinter", e.getLocalizedMessage());

					didFailWithException(e);
				}

			}
			while (this.continuous);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("Fingerprinter", e.getLocalizedMessage());

			didFailWithException(e);
		}

		if(mRecordInstance != null)
		{

			mRecordInstance.stop();
			mRecordInstance.release();
			mRecordInstance = null;
		}
		this.isRunning = false;

		didFinishListening();
	}

	private static String convertStreamToString(InputStream is)
	{
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private String messageForCode(int code)
	{
		try{
			String codes[] = {
					"NOT_ENOUGH_CODE", "CANNOT_DECODE", "SINGLE_BAD_MATCH",
					"SINGLE_GOOD_MATCH", "NO_RESULTS", "MULTIPLE_GOOD_MATCH_HISTOGRAM_INCREASED",
					"MULTIPLE_GOOD_MATCH_HISTOGRAM_DECREASED", "MULTIPLE_BAD_HISTOGRAM_MATCH", "MULTIPLE_GOOD_MATCH"
			};

			return codes[code];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return "UNKNOWN";
		}
	}

	private void didFinishListening()
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.didFinishListening();
				}
			});
		}
		else
			listener.didFinishListening();
	}

	private void didFinishListeningPass()
	{
		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.didFinishListeningPass();
				}
			});
		}
		else
			listener.didFinishListeningPass();
	}

	private void willStartListening()
	{
		//Log.v("AudioFingerprinter", "willStartListening");

		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.willStartListening();
				}
			});
		}
		else
			listener.willStartListening();
	}

	private void willStartListeningPass()
	{
		//Log.v("AudioFingerprinter", "willStartListeningPass");

		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.willStartListeningPass();
				}
			});
		}
		else
			listener.willStartListeningPass();
	}

	private void didGenerateFingerprintCode(final String code)
	{
		//Log.v("AudioFingerprinter", "didGenerateFingerprintCode - code: " + code);

		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.didGenerateFingerprintCode(code);
				}
			});
		}
		else
			listener.didGenerateFingerprintCode(code);
	}

	private void didFindMatchForCode(final Hashtable<String, String> table, final String code)
	{
		Log.v("AudioFingerprinter", "didFindMatchForCode - table: " + table);

		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.didFindMatchForCode(table, code);
				}
			});
		}
		else
			listener.didFindMatchForCode(table, code);
	}

	private void didNotFindMatchForCode(final String code)
	{
		Log.v("AudioFingerprinter", "didNotFindMatchForCode");

		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.didNotFindMatchForCode(code);
				}
			});
		}
		else
			listener.didNotFindMatchForCode(code);
	}

	private void didFailWithException(final Exception e)
	{
		Log.v("AudioFingerprinter", "didFailWithException - e: " + e.getLocalizedMessage());

		if(listener == null)
			return;

		if(listener instanceof Activity)
		{
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					listener.didFailWithException(e);
				}
			});
		}
		else
			listener.didFailWithException(e);
	}

	/**
	 * Interface for the fingerprinter listener<br>
	 * Contains the different delegate methods for the fingerprinting process
	 * @author Alex Restrepo
	 *
	 */
	public interface AudioFingerprinterListener
	{
		/**
		 * Called when the fingerprinter process loop has finished
		 */
		public void didFinishListening();

		/**
		 * Called when a single fingerprinter pass has finished
		 */
		public void didFinishListeningPass();

		/**
		 * Called when the fingerprinter is about to start
		 */
		public void willStartListening();

		/**
		 * Called when a single listening pass is about to start
		 */
		public void willStartListeningPass();

		/**
		 * Called when the codegen libary generates a fingerprint code
		 * @param code the generated fingerprint as a zcompressed, base64 string
		 */
		public void didGenerateFingerprintCode(String code);

		/**
		 * Called if the server finds a match for the submitted fingerprint code
		 * @param table a hashtable with the metadata returned from the server
		 * @param code the submited fingerprint code
		 */
		public void didFindMatchForCode(Hashtable<String, String> table, String code);

		/**
		 * Called if the server DOES NOT find a match for the submitted fingerprint code
		 * @param code the submited fingerprint code
		 */
		public void didNotFindMatchForCode(String code);

		/**
		 * Called if there is an error / exception in the fingerprinting process
		 * @param e an exception with the error
		 */
		public void didFailWithException(Exception e);
	}

	public class	CodegenThread extends Thread{
		int samIn;
		Codegen codegen;
		public CodegenThread( int sIn) {
			samIn = sIn;
			codegen  = new Codegen();
		}
		public void run() {
			//Log.d("Fingerprinter", "Audio bytes: " + Arrays.toString(audioData));
			code = codegen.generate(audioData, samIn);
			//Log.d("Fingerprinter", "Codegen created in: " + (System.currentTimeMillis() - time) + " millis");
			Log.d("Fingerprinter", "Fingerprint: " + code);
		}
	}
}