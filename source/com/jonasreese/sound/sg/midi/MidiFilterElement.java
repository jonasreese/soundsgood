/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.util.StringTokenizer;


/**
 * A <code>MidiFilterElement</code> is a part of a MIDI filter that defines
 * filtering rules for a single MIDI command. Since some MIDI commands appear
 * only in combination with others (like NOTE_ON/NOTE_OFF, START/STOP/CONTINUE),
 * these cannot be be distinguished. The command value for these shall be
 * <code>NOTE_ON</code> or <code>START</code>.
 * 
 * @author jonas.reese
 */
public class MidiFilterElement {
    
    private short command;
    private short[] channels;
    private short[] data1Fields;
    private boolean[] channelMask;
    private boolean[] data1Mask;
    private short maximumData2;
    private short minimumData2;
    
    /**
     * Constructs a <code>MidiFilterElement</code> from it's string representation.
     * @param stringRepresentation The string representation of the <code>MidiFilterElement</code>.
     * @throws IllegalArgumentException if the string representation is incorrect.
     */
    public MidiFilterElement( String stringRepresentation ) {

        minimumData2 = 0;
        maximumData2 = Short.MAX_VALUE;
        int i = 0;
        for (int index = stringRepresentation.indexOf( '{' ); index >= 0; index = stringRepresentation.indexOf( '{' )) {
            if (i > 4) {
                throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
            }
            int toIndex = stringRepresentation.indexOf( '}' );
            if (toIndex < 0) {
                throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
            }
            String s = stringRepresentation.substring( index + 1, toIndex );
            StringTokenizer st = new StringTokenizer( s, "," );
            if (i == 0) {
                if (!st.hasMoreTokens()) {
                    throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                }
                try {
                    String token = st.nextToken().trim();
                    command = Short.parseShort( token );
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                }
            } else if (i == 1) {
                data1Fields = new short[st.countTokens()];
                int j = 0;
                while (st.hasMoreTokens()) {
                    int val = -1;
                    try {
                        String token = st.nextToken().trim();
                        if ("*".equals( token )) {
                            data1Fields = null;
                            break;
                        }
                        val = Integer.parseInt( token );
                    } catch (NumberFormatException e) {
                    }
                    if (val < 0 || val > 127) {
                        throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                    }
                    data1Fields[j++] = (short) val;
                }
            } else if (i == 2) {
                channels = new short[st.countTokens()];
                int j = 0;
                while (st.hasMoreTokens()) {
                    int val = -1;
                    try {
                        String token = st.nextToken().trim();
                        if ("*".equals( token )) {
                            channels = null;
                            break;
                        }
                        val = Integer.parseInt( token );
                    } catch (NumberFormatException e) {
                    }
                    if (val < 0 || val > 15) {
                        throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                    }
                    channels[j++] = (short) val;
                }
            } else if (i == 3) {
                if (!st.hasMoreTokens()) {
                    throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                }
                try {
                    int val = Integer.parseInt( st.nextToken() );
                    minimumData2 = (short) val;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                }
            } else if (i == 4) {
                if (!st.hasMoreTokens()) {
                    throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                }
                try {
                    int val = Integer.parseInt( st.nextToken() );
                    maximumData2 = (short) val;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException( "Illegal MidiFilterElement string representation" );
                }
            }
            stringRepresentation = stringRepresentation.substring( toIndex + 1 );
            i++;
        }
        createMasks();
    }
    
    /**
     * Constructs a new <code>MidiFilterElement</code>.
     * @param command The MIDI command.
     * @param channels The channels that shall pass the filtering. <code>null</code> indicates
     * that all channels shall pass, an empty array blocks all channels.
     * @param data1Fields The data values that shall pass the filtering. <code>null</code> indicates
     * that all notes shall pass, an empty array blocks all notes.
     * @param minimumVolume The minimum volume for events that pass this filter.
     * @param maximumVolume The maximum volume for events that pass this filter.
     */
    public MidiFilterElement(
            short command, short[] channels, short[] data1Fields, short minimumVolume, short maximumVolume ) {
        this.command = command;
        this.channels = channels;
        this.data1Fields = data1Fields;
        this.minimumData2 = minimumVolume;
        this.maximumData2 = maximumVolume;
        createMasks();
    }

    /**
     * Constructs a new <code>MidiFilterElement</code> with no volume restrictions.
     * @param command The MIDI command.
     * @param channels The channels that shall pass the filtering. <code>null</code> indicates
     * that all channels shall pass, an empty array blocks all channels.
     * @param data1Fields The data1 values that shall pass the filtering. <code>null</code> indicates
     * that all notes shall pass, an empty array blocks all notes.
     */
    public MidiFilterElement( short command, short[] channels, short[] data1Fields ) {
        this( command, channels, data1Fields, (short) 0, Short.MAX_VALUE );
    }
    
    private void createMasks() {
        channelMask = new boolean[16];
        data1Mask = new boolean[128];
        if (channels != null && MidiToolkit.isChannelMessageStatusByte( command )) {
            for (int i = 0; i < channels.length; i++) {
                channelMask[channels[i]] = true;
            }
        } else {
            for (int i = 0; i < channelMask.length; i++) {
                channelMask[i] = true;
            }
        }
        if (data1Fields != null) {
            for (int i = 0; i < data1Fields.length; i++) {
                data1Mask[data1Fields[i]] = true;
            }
        } else {
            for (int i = 0; i < data1Mask.length; i++) {
                data1Mask[i] = true;
            }
        }
    }
    
    /**
     * Gets the MIDI command this <code>MidiFilterElement</code> filters.
     * @return The MIDI command.
     */
    public short getCommand() {
        return command;
    }

    /**
     * Filters the given <code>MidiMessage</code> and returns <code>true</code>
     * if it passes the filter. The command is not checked, this has to be done
     * externally.
     * @param m The MIDI message.
     * @return <code>true</code> if and only if the filter criteria is matched.
     */
    public boolean filter( int channel, int data1, int data2 ) {
        return ((channel < 0 || channelMask[channel]) && data1Mask[data1] &&
                data2 >= minimumData2 && data2 <= maximumData2);
    }
    
    /**
     * Returns a string representation of this <code>MidiFilterElement</code>.
     * Another <code>MidiFilterElement</code> object can be constructed from this string
     * representation by simply passing it to the constructor.
     * @return A (valid) string representation.
     */
    public String getStringRepresentation() {
        StringBuffer sb = new StringBuffer();
        sb.append( "{" );
        sb.append( command );
        sb.append( "}" );
        sb.append( "{" );
        if (data1Fields != null) {
            for (int i = 0; i < data1Fields.length; i++) {
                sb.append( data1Fields[i] );
                if (i + 1 < data1Fields.length) {
                    sb.append( "," );
                }
            }
        } else {
            sb.append( "*" );
        }
        sb.append( "}" );
        sb.append( "{" );
        if (channels != null) {
            for (int i = 0; i < channels.length; i++) {
                sb.append( channels[i] );
                if (i + 1 < channels.length) {
                    sb.append( "," );
                }
            }
        } else {
            sb.append( "*" );
        }
        sb.append( "}" );
        if (minimumData2 > 0 || maximumData2 < 127) {
            sb.append( "{" + maximumData2 + "}" );
            sb.append( "{" + minimumData2 + "}" );
        }
        return sb.toString();
    }

    public short[] getChannels() {
        return channels;
    }
    
    public boolean[] getChannelMask() {
        return channelMask;
    }

    /**
     * Gets the maximum value for the MIDI DATA2 field.
     * @return The maximum value. 127 or greater if no restrictions apply.
     */
    public short getMaximumData2() {
        return maximumData2;
    }

    /**
     * Gets the minimum value for the MIDI DATA2 field.
     * @return The minimum value. 0 if no restrictions apply.
     */
    public short getMinimumData2() {
        return minimumData2;
    }

    public short[] getData1Fields() {
        return data1Fields;
    }
    
    public boolean[] getData1Mask() {
        return data1Mask;
    }
    
    public boolean equals( Object another ) {
        if (another instanceof MidiFilterElement) {
            MidiFilterElement f = (MidiFilterElement) another;
            if (minimumData2 != f.minimumData2) {
                return false;
            }
            if (maximumData2 != f.maximumData2 && maximumData2 < 127 || f.maximumData2 < 127) {
                return false;
            }
            for (int i = 0; i < channelMask.length; i++) {
                if (channelMask[i] != f.channelMask[i]) {
                    return false;
                }
            }
            for (int i = 0; i < data1Mask.length; i++) {
                if (data1Mask[i] != f.data1Mask[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
