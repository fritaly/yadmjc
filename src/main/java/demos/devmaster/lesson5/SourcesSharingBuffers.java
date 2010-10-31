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

package demos.devmaster.lesson5;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.java.games.joal.AL;
import net.java.games.joal.ALC;
import net.java.games.joal.ALCcontext;
import net.java.games.joal.ALCdevice;
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

public class SourcesSharingBuffers {

  static ALC alc;
  static AL al;

  //     These index the buffers.
  public static final int THUNDER     = 0;
  public static final int WATERDROP   = 1;
  public static final int STREAM      = 2;
  public static final int RAIN        = 3;

  public static final int CHIMES      = 4;
  public static final int OCEAN       = 5;
  public static final int NUM_BUFFERS = 6;

  //     Buffers hold sound data.
  static int[] buffers = new int[NUM_BUFFERS];

  //     A list of sources for multiple emissions.
  static List sources = new ArrayList();

  //  Position of the source sounds.
  static float[] sourcePos = { 0.0f, 0.0f, 0.0f };

  //  Velocity of the source sounds.
  static float[] sourceVel = { 0.0f, 0.0f, 0.0f };



  //  Position of the listener.
  static float[] listenerPos = { 0.0f, 0.0f, 0.0f };

  //  Velocity of the listener.
  static float[] listenerVel = { 0.0f, 0.0f, 0.0f };

  //  Orientation of the listener. (first 3 elements are "at", second 3 are "up")
  static float[] listenerOri = { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f };

  static void initOpenAL() throws ALException {
    alc = ALFactory.getALC();
    al = ALFactory.getAL();

    ALCdevice device;
    ALCcontext context;
    String deviceSpecifier;

    // Get handle to default device.
    device = alc.alcOpenDevice(null);
    if (device == null) {
      throw new ALException("Error opening default OpenAL device");
    }

    // Get the device specifier.
    deviceSpecifier = alc.alcGetString(device, ALC.ALC_DEVICE_SPECIFIER);
    if (deviceSpecifier == null) {
      throw new ALException("Error getting specifier for default OpenAL device");
    }

    System.out.println("Using device " + deviceSpecifier);

    // Create audio context.
    context = alc.alcCreateContext(device, null);
    if (context == null) {
      throw new ALException("Error creating OpenAL context");
    }

    // Set active context.
    alc.alcMakeContextCurrent(context);

    // Check for an error.
    if (alc.alcGetError(device) != ALC.ALC_NO_ERROR) {
      throw new ALException("Error making OpenAL context current");
    }
  }

  static void exitOpenAL() {
    ALCcontext curContext;
    ALCdevice curDevice;

    // Get the current context.
    curContext = alc.alcGetCurrentContext();

    // Get the device used by that context.
    curDevice = alc.alcGetContextsDevice(curContext);

    // Reset the current context to NULL.
    alc.alcMakeContextCurrent(null);

    // Release the context and the device.
    alc.alcDestroyContext(curContext);
    alc.alcCloseDevice(curDevice);
  }
  static int loadALData() {
    // Variables to load into.
    int[] format = new int[1];
    int[] size = new int[1];
    ByteBuffer[] data = new ByteBuffer[1];
    int[] freq = new int[1];
    int[] loop = new int[1];

    // Load wav data into buffers.
    al.alGenBuffers(NUM_BUFFERS, buffers, 0);

    if(al.alGetError() != AL.AL_NO_ERROR)
      return AL.AL_FALSE;

    ALut.alutLoadWAVFile(SourcesSharingBuffers.class.getClassLoader().getResourceAsStream("demos/data/thunder.wav"),
                         format, data, size, freq, loop);
    al.alBufferData(buffers[THUNDER], format[0], data[0], size[0], freq[0]);

    ALut.alutLoadWAVFile(SourcesSharingBuffers.class.getClassLoader().getResourceAsStream("demos/data/waterdrop.wav"),
                         format, data, size, freq, loop);
    al.alBufferData(buffers[WATERDROP], format[0], data[0], size[0], freq[0]);

    ALut.alutLoadWAVFile(SourcesSharingBuffers.class.getClassLoader().getResourceAsStream("demos/data/stream.wav"),
                         format, data, size, freq, loop);
    al.alBufferData(buffers[STREAM], format[0], data[0], size[0], freq[0]);

    ALut.alutLoadWAVFile(SourcesSharingBuffers.class.getClassLoader().getResourceAsStream("demos/data/rain.wav"),
                         format, data, size, freq, loop);
    al.alBufferData(buffers[RAIN], format[0], data[0], size[0], freq[0]);

    ALut.alutLoadWAVFile(SourcesSharingBuffers.class.getClassLoader().getResourceAsStream("demos/data/ocean.wav"),
                         format, data, size, freq, loop);
    al.alBufferData(buffers[OCEAN], format[0], data[0], size[0], freq[0]);

    ALut.alutLoadWAVFile(SourcesSharingBuffers.class.getClassLoader().getResourceAsStream("demos/data/chimes.wav"),
                         format, data, size, freq, loop);
    al.alBufferData(buffers[CHIMES], format[0], data[0], size[0], freq[0]);

    // Do another error check and return.
    if (al.alGetError() != AL.AL_NO_ERROR)
      return AL.AL_FALSE;

    return AL.AL_TRUE;
  }

  static void addSource(int type) {
    int[] source = new int[1];

    al.alGenSources(1, source, 0);

    if (al.alGetError() != AL.AL_NO_ERROR) {
      System.err.println("Error generating audio source.");
      System.exit(1);
    }

    al.alSourcei (source[0], AL.AL_BUFFER,   buffers[type]);
    al.alSourcef (source[0], AL.AL_PITCH,    1.0f          );
    al.alSourcef (source[0], AL.AL_GAIN,     1.0f          );
    al.alSourcefv(source[0], AL.AL_POSITION, sourcePos    , 0);
    al.alSourcefv(source[0], AL.AL_VELOCITY, sourceVel    , 0);
    al.alSourcei (source[0], AL.AL_LOOPING,  AL.AL_TRUE      );

    al.alSourcePlay(source[0]);

    sources.add(new Integer(source[0]));
  }

  static void setListenerValues() {
    al.alListenerfv(AL.AL_POSITION,    listenerPos, 0);
    al.alListenerfv(AL.AL_VELOCITY,    listenerVel, 0);
    al.alListenerfv(AL.AL_ORIENTATION, listenerOri, 0);
  }

  static void killALData() {
    for (Iterator iter = sources.iterator(); iter.hasNext(); ) {
      al.alDeleteSources(1, new int[] { ((Integer)iter.next()).intValue() }, 0);
    }
    sources.clear();
    al.alDeleteBuffers(NUM_BUFFERS, buffers, 0);
    exitOpenAL();
  }

  static boolean initialized = false;
  static void initialize() {
    if (initialized)
      return;
    initialized = true;
    try {
      initOpenAL();
    } catch (ALException e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (loadALData() == AL.AL_FALSE)
      System.exit(1);
    setListenerValues();
  }

  private static void addButton(JFrame frame, String text, final int whichSound) {
    JButton button = new JButton(text);
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          initialize();
          addSource(whichSound);
        }
      });
    frame.getContentPane().add(button);
  }

  public static void main(String[] args) {
    boolean gui = false;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-gui"))
        gui = true;
    }
    
    if (gui) {
      JFrame frame = new JFrame("Sources Sharing Buffers - DevMaster OpenAL Lesson 5");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().setLayout(new GridLayout(7, 1));
      addButton(frame, "Add Water Drop", WATERDROP);
      addButton(frame, "Add Thunder",    THUNDER);
      addButton(frame, "Add Stream",     STREAM);
      addButton(frame, "Add Rain",       RAIN);
      addButton(frame, "Add Ocean",      OCEAN);
      addButton(frame, "Add Chimes",     CHIMES);

      JButton button = new JButton("Quit");
      button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            System.exit(0);
          }
        });
      frame.getContentPane().add(button);

      frame.pack();
      frame.setVisible(true);
    } else {
      initialize();
      char[] c = new char[1];
      while(c[0] != 'q') {    
        try {
          BufferedReader buf =
            new BufferedReader(new InputStreamReader(System.in));
          System.out.println("Press a key and hit ENTER: \n" +
                             "\t'w' for Water Drop\n" +
                             "\t't' for Thunder\n" +
                             "\t's' for Stream\n" +
                             "\t'r' for Rain\n" +
                             "\t'o' for Ocean\n" +
                             "\t'c' for Chimes\n" +
                             "\n'q' to Quit\n");

          buf.read(c);
          switch(c[0]) {
          case 'w': addSource(WATERDROP); break;
          case 't': addSource(THUNDER); break;
          case 's': addSource(STREAM); break;
          case 'r': addSource(RAIN); break;
          case 'o': addSource(OCEAN); break;
          case 'c': addSource(CHIMES); break;
          }
        } catch (IOException e) {
          killALData();
          System.exit(1);
        }
      }
      killALData();
    }
  }
}
