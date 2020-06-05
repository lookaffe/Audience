/**
 * Codegen.java
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

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;


import basfp.it.bas3.support.LogAndroid;

/**
 * Codegen class<br>
 * This class bridges the native Codegen library with the Java side...
 * 
 * @author Alex Restrepo (MASL)
 *
 */
public class Codegen 
{
	private final float normalizingValue = Short.MAX_VALUE;
	String pathBase = Environment.getExternalStorageDirectory()+ File.separator + "Audience";//getString(R.string.app_name);
	private static final String AUDIO_RECORDER_TEMP_FILE = "rec_temp";

	native String codegen(float data[], int numSamples);
	
	static 
	{
			System.loadLibrary("echoprint-jni");
    }
	
	/**
	 * Invoke the echoprint native library and generate the fingerprint code.<br>
	 * Echoprint REQUIRES PCM encoded audio with the following parameters:<br>
	 * Frequency: 11025 khz<br>
	 * Data: MONO - PCM enconded float array
	 * 
	 * @param data PCM encoded data as floats [-1, 1]
	 * @param numSamples number of PCM samples at 11025 KHz
	 * @return The generated fingerprint as a compressed - base64 string.
	 */
	public String generate(float data[], int numSamples)
	{
		return codegen(data, numSamples);
	}
	
	/**
	 * Invoke the echoprint native library and generate the fingerprint code.<br>
	 * Since echoprint requires the audio data to be an array of floats in the<br>
	 * range [-1, 1] this method will normalize the data array transforming the<br>
	 * 16 bit signed shorts into floats. 
	 * 
	 * @param data PCM encoded data as shorts
	 * @param numSamples number of PCM samples at 11025 KHz
	 * @return The generated fingerprint as a compressed - base64 string.
	 */
	public String generate(short data[], int numSamples)
	{
		// echoprint expects data as floats, which is the native value for 
		// core audio data, and I guess ffmpeg
		// Android records data as 16 bit shorts, so we need to normalize the
		// data before sending it to echoprint
		float normalizeAudioData[] = new float[numSamples];
		for (int i = 0; i < numSamples - 1; i++)
			normalizeAudioData[i] = data[i] / normalizingValue;

/*
		String floatName = pathBase + File.separator +"bas_wave"+File.separator + "FLOAT-" +System.currentTimeMillis() + ".txt";
		FileWriter floatwriter = null;
		try {

			try {
				floatwriter = new FileWriter(floatName, true);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			for (int i = 0; i < numSamples; i++) {
				floatwriter.write(normalizeAudioData[i]+" ");
			}

			floatwriter.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}


           String pathBase = Environment.getExternalStorageDirectory()+ File.separator + "Audience";//getString(R.string.app_name);
            String shortName = pathBase + File.separator +"bas_wave"+File.separator + "SHORT-" + System.currentTimeMillis() +".txt";
            FileWriter shortwriter = null;
            try {
                try {
                    shortwriter = new FileWriter(shortName, true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                for (int i = 0; i < audioData.length; i++) {
                    shortwriter.write(audioData[i]+" ");
                }
                //this is the code that you change, this will make a new line between each y value in the array
                shortwriter.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

 */


		return this.codegen(normalizeAudioData, numSamples);
	}
}
