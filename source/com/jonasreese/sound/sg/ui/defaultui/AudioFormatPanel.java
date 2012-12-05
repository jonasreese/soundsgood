/*
 * Created on 26.11.2008
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioToolkit;

/**
 * This UI panel offers controls for editing an audio format.
 * 
 * @author Jonas Reese
 */
public class AudioFormatPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JComboBox sampleRateComboBox;
    private JComboBox sampleSizeComboBox;
    private JRadioButton monoRadioButton;
    private JCheckBox endianCheckBox;
    private JCheckBox signedCheckBox;

    private AudioFormat format;
    
    /**
     * Constructs a new <code>AudioFormatPanel</code> that displays the given audio format.
     * @param format The format. If <code>null</code>, the SG default audio format will
     * be displayed for edit.
     */
    public AudioFormatPanel( AudioFormat format ) {
        
        super( new BorderLayout() );
        
        if (format == null) {
            format = AudioToolkit.getDefaultAudioFormat();
        }
        this.format = format;

        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JPanel sampleRatePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JLabel l0 = new JLabel( rb.getString( "options.audio.format.samplerate" ) );
        JPanel audioFormatPanel = new JPanel( new BorderLayout() );
        sampleRatePanel.add( l0 );
        SampleRate[] sampleRates = new SampleRate[] {
                        new SampleRate( 96000f, rb.getString( "options.audio.format.sr96" ) ),
                        new SampleRate( 48000f, rb.getString( "options.audio.format.sr48" ) ),
                        new SampleRate( 44100f, rb.getString( "options.audio.format.sr44_1" ) ),
                        new SampleRate( 22050f, rb.getString( "options.audio.format.sr22_05" ) ),
                        new SampleRate( 16000f, rb.getString( "options.audio.format.sr16" ) ),
                        new SampleRate( 8000f, rb.getString( "options.audio.format.sr8" ) ),
                };
        sampleRateComboBox = new JComboBox( sampleRates );
        
        for (int i = 0; i < sampleRates.length; i++) {
            if (format.getSampleRate() == sampleRates[i].sr) {
                sampleRateComboBox.setSelectedIndex( i );
                break;
            }
        }
        l0.setLabelFor( sampleRateComboBox );
        sampleRatePanel.add( sampleRateComboBox );
        audioFormatPanel.add( sampleRatePanel, BorderLayout.WEST );
        JPanel sampleSizePanel = new JPanel();
        JLabel l1 = new JLabel( rb.getString( "options.audio.format.samplesize" ) );
        SampleSize[] sampleSizes = new SampleSize[] {
                        new SampleSize( 8, rb.getString( "options.audio.format.ss8" ) ),
                        new SampleSize( 16, rb.getString( "options.audio.format.ss16" ) ),
                        new SampleSize( 32, rb.getString( "options.audio.format.ss32" ) ),
                };
        sampleSizeComboBox = new JComboBox( sampleSizes );
        for (int i = 0; i < sampleSizes.length; i++) {
            if (format.getSampleSizeInBits() == sampleSizes[i].ss) {
                sampleSizeComboBox.setSelectedIndex( i );
                break;
            }
        }
        
        l1.setLabelFor( sampleSizeComboBox );
        sampleSizePanel.add( l1 );
        sampleSizePanel.add( sampleSizeComboBox );
        audioFormatPanel.add( sampleSizePanel );
        JPanel monoStereoPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        monoRadioButton = new JRadioButton(
                rb.getString( "options.audio.format.mono" ),
                format.getChannels() == 1 );
        JRadioButton stereoRadioButton = new JRadioButton(
                rb.getString( "options.audio.format.stereo" ),
                format.getChannels() == 2);
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( monoRadioButton );
        bgr.add( stereoRadioButton );
        monoStereoPanel.add( monoRadioButton );
        monoStereoPanel.add( stereoRadioButton );
        audioFormatPanel.add( monoStereoPanel, BorderLayout.EAST );
        JPanel endianPanel = new JPanel();
        endianCheckBox = new JCheckBox(
                rb.getString( "options.audio.format.bigEndian" ), format.isBigEndian() );
        endianPanel.add( endianCheckBox );
        signedCheckBox = new JCheckBox(
                rb.getString( "options.audio.format.signed" ),
                format.getEncoding() == Encoding.PCM_SIGNED );
        JPanel signedPanel = new JPanel();
        signedPanel.add( signedCheckBox );
        JPanel southPanel = new JPanel( new BorderLayout() );
        southPanel.add( endianPanel, BorderLayout.WEST );
        southPanel.add( signedPanel );
        audioFormatPanel.add( southPanel, BorderLayout.SOUTH );
        add( audioFormatPanel );
    }
    
    /**
     * Gets the resulting audio format that the user has adjusted.
     * @return The <code>AudioFormat</code>.
     */
    public AudioFormat getAudioFormat() {
        return new AudioFormat(
                ((SampleRate) sampleRateComboBox.getSelectedItem()).sr,
                ((SampleSize) sampleSizeComboBox.getSelectedItem()).ss,
                monoRadioButton.isSelected() ? 1 : 2,
                signedCheckBox.isSelected(),
                endianCheckBox.isSelected() );
    }
    
    public boolean hasChanged() {
        return !AudioToolkit.isSameAudioFormat( getAudioFormat(), format );
    }
    

    private static class SampleRate {
        float sr;
        String s;
        SampleRate( float sr, String s ) {
            this.sr = sr;
            this.s = s;
        }
        public String toString() {
            return s;
        }
    }
    private static class SampleSize {
        int ss;
        String s;
        SampleSize( int ss, String s ) {
            this.ss = ss;
            this.s = s;
        }
        public String toString() {
            return s;
        }
    }

}
