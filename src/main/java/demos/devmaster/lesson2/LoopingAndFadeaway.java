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

package demos.devmaster.lesson2;

import java.nio.ByteBuffer;

import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALException;
import net.java.games.joal.ALFactory;
import net.java.games.joal.util.ALut;

/**
 * Adapted from <a href="http://www.devmaster.net/">DevMaster</a>
 * <a href="http://www.devmaster.net/articles/openal-tutorials/lesson2.php">LoopingAndFadeaway Tutorial</a>
 * by Jesse Maurais.
 *
 * @author Athomas Goldberg
 * @author Kenneth Russell
 */

public class LoopingAndFadeaway {

  static int[] buffer = new int[1];
  static int[] source = new int[1];
  static float[] sourcePos = { 0.0f, 0.0f, 0.0f };
  static float[] sourceVel = { 0.0f, 0.0f, 0.1f };
  static float[] listenerPos = { 0.0f, 0.0f, 0.0f };
  static float[] listenerVel = { 0.0f, 0.0f, 0.0f };
  static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };
  static AL al;
  static ALC alc;

  static int loadALData() {
    if (al.alGetError() != AL.AL_NO_ERROR) {
      return AL.AL_FALSE;
    }

    int[] format = new int[1];
    int[] size = new int[1];
    ByteBuffer[] data = new ByteBuffer[1];
    int[] freq = new int[1];
    int[] loop = new int[1];

    // Load wav data into a buffer.
    al.alGenBuffers(1, buffer, 0);
    if (al.alGetError() != AL.AL_NO_ERROR)
      return AL.AL_FALSE;

    ALut.alutLoadWAVFile(
      LoopingAndFadeaway.class.getClassLoader().getResourceAsStream("demos/data/Footsteps.wav"),
      format,
      data,
      size,
      freq,
      loop);
    al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);

    al.alGenSources(1, source, 0);
    al.alSourcei(source[0], AL.AL_BUFFER, buffer[0]);
    al.alSourcef(source[0], AL.AL_PITCH, 1.0f);
    al.alSourcef(source[0], AL.AL_GAIN, 1.0f);
    al.alSourcefv(source[0], AL.AL_POSITION, sourcePos, 0);
    al.alSourcefv(source[0], AL.AL_POSITION, sourceVel, 0);
    al.alSourcei(source[0], AL.AL_LOOPING, AL.AL_TRUE);

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
    al.alDeleteBuffers(1, buffer, 0);
    al.alDeleteSources(1, source, 0);
  }

  public static void main(String[] args) {
    try {
      ALut.alutInit();
      al = ALFactory.getAL();
    } catch (ALException e) {
      e.printStackTrace();
      return;
    }

    if(loadALData() == AL.AL_FALSE) {
      System.exit(1);
    }; 
    setListenerValues();
    al.alSourcePlay(source[0]);
    long startTime = System.currentTimeMillis();
    long elapsed = 0;
    long ticker = 0;
    long lastTime = 0;
    while (elapsed < 10000) {
      elapsed = System.currentTimeMillis() - startTime;            
      if (ticker > 100) {
        ticker = 0;
        sourcePos[0] += sourceVel[0];
        sourcePos[1] += sourceVel[1];
        sourcePos[2] += sourceVel[2];
        al.alSourcefv(source[0],
                      AL.AL_POSITION,
                      sourcePos, 0);
      }
      ticker += System.currentTimeMillis() - lastTime;
      lastTime = System.currentTimeMillis(); 
    }
    System.exit(0);
  }
}
