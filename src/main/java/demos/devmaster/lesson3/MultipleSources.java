/**
 * Copyright (c) 2003-2005 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the
 * design, construction, operation or maintenance of any nuclear facility.
 *
 */

package demos.devmaster.lesson3;

import java.nio.ByteBuffer;
import java.util.Random;

import net.java.games.joal.AL;
import net.java.games.joal.ALException;
import net.java.games.joal.ALFactory;
import net.java.games.joal.util.ALut;

/**
 * Adapted from <a href="http://www.devmaster.net/">DevMaster</a>
 * <a href="http://www.devmaster.net/articles/openal-tutorials/lesson3.php">MultipleSources Tutorial</a>
 * by Jesse Maurais.
 *
 * @author Athomas Goldberg
 * @author Kenneth Russell
 */

public class MultipleSources {

  static AL al;
  static final int NUM_BUFFERS = 3;
  static final int NUM_SOURCES = 3;

  static final int BATTLE = 0;
  static final int GUN1 = 1;
  static final int GUN2 = 2;

  static int[] buffers = new int[NUM_BUFFERS];
  static int[] sources = new int[NUM_SOURCES];

  static float[][] sourcePos = new float[NUM_SOURCES][3];
  static float[][] sourceVel = new float[NUM_SOURCES][3];
  static float[] listenerPos = { 0.0f, 0.0f, 0.0f };
  static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
  static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

  static int loadALData() {
    //variables to load into
    int[] format = new int[1];
    int[] size = new int[1];
    ByteBuffer[] data = new ByteBuffer[1];
    int[] freq = new int[1];
    int[] loop = new int[1];
        
    // load wav data into buffers
    al.alGenBuffers(NUM_BUFFERS, buffers, 0);
    if (al.alGetError() != AL.AL_NO_ERROR) {
      return AL.AL_FALSE;
    }

    ALut.alutLoadWAVFile(
      MultipleSources.class.getClassLoader().getResourceAsStream("demos/data/Battle.wav"),
      format,
      data,
      size,
      freq,
      loop);
    al.alBufferData(buffers[BATTLE],
                    format[0],
                    data[0],
                    size[0],
                    freq[0]);

    ALut.alutLoadWAVFile(
      MultipleSources.class.getClassLoader().getResourceAsStream("demos/data/Gun1.wav"),
      format,
      data,
      size,
      freq,
      loop);
    al.alBufferData(buffers[GUN1],
                    format[0],
                    data[0],
                    size[0],
                    freq[0]);

    ALut.alutLoadWAVFile(
      MultipleSources.class.getClassLoader().getResourceAsStream("demos/data/Gun2.wav"),
      format,
      data,
      size,
      freq,
      loop);
    al.alBufferData(buffers[GUN2],
                    format[0],
                    data[0],
                    size[0],
                    freq[0]);

    // bind buffers into audio sources
    al.alGenSources(NUM_SOURCES, sources, 0);

    al.alSourcei(sources[BATTLE], AL.AL_BUFFER, buffers[BATTLE]);
    al.alSourcef(sources[BATTLE], AL.AL_PITCH, 1.0f);
    al.alSourcef(sources[BATTLE], AL.AL_GAIN, 1.0f);
    al.alSourcefv(sources[BATTLE], AL.AL_POSITION, sourcePos[BATTLE], 0);
    al.alSourcefv(sources[BATTLE], AL.AL_POSITION, sourceVel[BATTLE], 0);
    al.alSourcei(sources[BATTLE], AL.AL_LOOPING, AL.AL_TRUE);

    al.alSourcei(sources[GUN1], AL.AL_BUFFER, buffers[GUN1]);
    al.alSourcef(sources[GUN1], AL.AL_PITCH, 1.0f);
    al.alSourcef(sources[GUN1], AL.AL_GAIN, 1.0f);
    al.alSourcefv(sources[GUN1], AL.AL_POSITION, sourcePos[GUN1], 0);
    al.alSourcefv(sources[GUN1], AL.AL_POSITION, sourceVel[GUN1], 0);
    al.alSourcei(sources[GUN1], AL.AL_LOOPING, AL.AL_FALSE);

    al.alSourcei(sources[GUN2], AL.AL_BUFFER, buffers[GUN2]);
    al.alSourcef(sources[GUN2], AL.AL_PITCH, 1.0f);
    al.alSourcef(sources[GUN2], AL.AL_GAIN, 1.0f);
    al.alSourcefv(sources[GUN2], AL.AL_POSITION, sourcePos[GUN2], 0);
    al.alSourcefv(sources[GUN2], AL.AL_POSITION, sourceVel[GUN2], 0);
    al.alSourcei(sources[GUN2], AL.AL_LOOPING, AL.AL_FALSE);

    // do another error check and return
    if (al.alGetError() != AL.AL_NO_ERROR) {
      return AL.AL_FALSE;
    }

    return AL.AL_TRUE;
  }

  static void setListenerValues() {
    al.alListenerfv(AL.AL_POSITION, listenerPos, 0);
    al.alListenerfv(AL.AL_VELOCITY, listenerVel, 0);
    al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
  }

  static void killAllData() {
    al.alDeleteBuffers(NUM_BUFFERS, buffers, 0);
    al.alDeleteSources(NUM_SOURCES, sources, 0);
  }

  public static void main(String[] args) {
    try {
      ALut.alutInit();
      al = ALFactory.getAL();
    } catch (ALException e) {
      e.printStackTrace();
      System.exit(1);
    }
    al.alGetError();
        
    if(loadALData() == AL.AL_FALSE) {
      System.exit(1);    
    }
    setListenerValues();
    al.alSourcePlay(sources[BATTLE]);
    long startTime = System.currentTimeMillis();
    long elapsed = 0;
    long totalElapsed = 0;
    Random rand = new Random();
    int[] state = new int[1];
    while (totalElapsed < 10000) {
      elapsed = System.currentTimeMillis() - startTime;
      if (elapsed > 50) {
        totalElapsed += elapsed;
        startTime = System.currentTimeMillis();
        // pick one of the two guns
        int pick = Math.abs((rand.nextInt()) % 2) + 1;
        al.alGetSourcei(sources[pick], AL.AL_SOURCE_STATE, state, 0);
        if (state[0] != AL.AL_PLAYING) {
          double theta = (rand.nextInt() % 360) * 3.14 / 180.0;
          sourcePos[pick][0] = - ((float) Math.cos(theta));
          sourcePos[pick][1] = - ((float) (rand.nextInt() % 2));
          sourcePos[pick][2] = - ((float) Math.sin(theta));

          al.alSourcefv(sources[pick],
                        AL.AL_POSITION,
                        sourcePos[pick], 0);

          al.alSourcePlay(sources[pick]);
        }
      }
    }
    killAllData();
    System.exit(0);
  }
}
