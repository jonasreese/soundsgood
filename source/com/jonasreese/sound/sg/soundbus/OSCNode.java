/*
 * Created on 19.10.2010
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;


/**
 * <p>
 * This class defines the <code>SbNode</code> interface for a node that sends/receives
 * OSC data and performs operations on the soundbus or routes MIDI data.
 * </p>
 * @author jonas.reese
 */
public interface OSCNode extends SbNode {
    /**
     * Gets the UDP port on which OSC protocol data shall be received.
     * @return The OSC data packet port.
     */
    public int getReceivePort();

    /**
     * Sets the UDP port for receiving OSC data.
     * @param port The port number.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setReceivePort(int port) throws IllegalStateException;
    
    /**
     * Returns <code>true</code> if and only if sending of OSC messages is enabled.
     * @return <code>true</code> if OSC message sending is enabled, <code>false</code> otherwise.
     */
    public boolean isSendEnabled();

    /**
     * Enables/disables the sending of OSC messages.
     * Please note that the implementation determines what kind of OSC messages will be sent.
     * As a minimum, this shall include MIDI messages coming through a MIDI input and tempo update messages
     * which can be configured by the message address setter methods provided by this interface.
     * @see #setTempoMessageAddress(String)
     * @see #setClickOnOffMessageAddress(String)
     * @param enabled If <code>true</code>, OSC messages will be sent, <code>false</code> otherwise.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setSendEnabled(boolean enabled) throws IllegalStateException;
    
    /**
     * Gets the current state for sending regular updates. If regular updates are enabled and
     * {@link #isSendEnabled()} returns <code>true</code>, information information on soundbus
     * properties such as tempo, click on/off, ... are sent regularly so that an external device
     * can sync with the soundbus state. 
     * @return <code>true</code> if and only if regular updates are enabled.
     */
    public boolean isSendRegularUpdatesEnabled();
    
    /**
     * Enables/disables regular updates. For details on regular updates, see
     * {@link #isSendRegularUpdatesEnabled()} method description.
     * @param regularUpdatesEnabled <code>true</code> to enable regular updates, <code>false</code>
     * to disable. Please note that no updates will be sent unless <code>sendEnabled</code> is enabled.
     * @see #setSendEnabled(boolean)
     */
    public void setSendRegularUpdatesEnables(boolean regularUpdatesEnabled);
    
    /**
     * Gets the UDP port to which OSC protocol data shall be sent.
     * @return The OSC data packet port.
     */
    public int getSendToPort();

    /**
     * Sets the UDP port for sending OSC data.
     * @param port The port number.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setSendToPort(int port) throws IllegalStateException;
    
    /**
     * Gets the tempo OSC message address, e.g. "/sg/click".
     * @return The tempo message address. Message type for tempo messages shall be numerical (Integer or Float).
     */
    public String getTempoMessageAddress();
    
    /**
     * Sets the tempo message address.
     * @param tempoMessageAddress The tempo message address to set. Must start with a "/" character if not
     * <code>null</code> and can be <code>null</code> to reset to default value.
     * @throws IllegalArgumentException if the address does not have a correct OSC address format.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setTempoMessageAddress(String tempoMessageAddress) throws IllegalStateException, IllegalArgumentException;

    /**
     * Gets the click on/off OSC message address, e.g. "/sg/click".
     * @return The click message address. Message type for click on/off messages shall be either numerical
     * (Integer or Float) with values of 0 (off) or 1 (on), or boolean with 'T' (on) or 'F' (off).
     */
    public String getClickOnOffMessageAddress();

    /**
     * Sets the click on/off message address.
     * @param clickMessageAddress The click message address to set. Must start with a "/" character if not
     * <code>null</code> and can be <code>null</code> to reset to default value.
     * @throws IllegalArgumentException if the address does not have a correct OSC address format.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setClickOnOffMessageAddress(String clickOnOffMessageAddress) throws IllegalStateException, IllegalArgumentException;
    
    /**
     * Gets the "send-to" host.
     * @return The host name or IP address string. Can be <code>null</code> or empty.
     */
    public String getSendToHost();

    /**
     * Sets the "send-to" host.
     * @param host The host name or IP address string to set. May be <code>null</code> or empty.
     */
    public void setSendToHost(String host);
}
