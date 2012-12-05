/*
 * Created on 20.11.2008
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.ShortMessage;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.OSCNode;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusAdapter;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;
import com.jonasreese.sound.sg.soundbus.TempoNode;

/**
 * @author jonas.reese
 */
public class OSCNodeImpl implements OSCNode, NodeImpl {
    
    public static final int PACKET_SIZE = 4096;
    public static final int QUEUE_SIZE = 16;
    
    // initialize default message addresses with same value, messages are differentiated by their argument values
    public static final String DEFAULT_TEMPO_ADDRESS = "/sg/click";
    public static final String DEFAULT_CLICK_ON_OFF_ADDRESS = DEFAULT_TEMPO_ADDRESS;
    
    private SoundbusImpl parent;
    private String name;
    private SbOutput[] outputs;

    private OSCPortIn receiver;
    
    private boolean open;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    private SoundbusListener soundbusListener;
    private PropertyChangeListener tempoNodePropertyChangeListener;
    
    private int receivePort;
    private int sendToPort;
    private boolean sendEnabled;
    private boolean regularUpdatesEnabled;
    private String clickOnOffMessageAddress = DEFAULT_CLICK_ON_OFF_ADDRESS;
    private String tempoMessageAddress = DEFAULT_TEMPO_ADDRESS;
    private String sendToHost;

    private UpdateThread regularUpdatesThread;
    
    public OSCNodeImpl(SoundbusImpl parent, String name) {
        this.parent = parent;
        open = false;
        clientProperties = new HashMap<String,String>();
        outputs = new SbMidiOutputImpl[] {
                new SbMidiOutputImpl(SgEngine.getInstance().getResourceBundle().getString("midi.output"))
        };;
        propertyChangeSupport = new PropertyChangeSupport(this);
        
        soundbusListener = new SoundbusAdapter() {
            public void tempoChanged(SoundbusEvent e) {
                sendOscMessage(tempoMessageAddress, new Object[] { e.getSoundbus().getTempo() });
            }
        };
        tempoNodePropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                sendOscMessage(clickOnOffMessageAddress, new Object[] { (Boolean) evt.getNewValue() });
            }
        };
    }
    
    private void sendOscMessage(String oscAddress, Object[] attributes) {
        String host = getSendToHost();
                
        if (host != null && host.trim().length() > 0) {
            OSCPortOut sender;
            try {
                sender = new OSCPortOut(InetAddress.getByName(getSendToHost()), getSendToPort());
                sender.send(new OSCMessage(oscAddress, attributes));
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }
    
    public int getReceivePort() {
        if (receivePort <= 0) {
            receivePort = OSCPort.defaultSCOSCPort();
        }
        return receivePort;
    }

    public void setReceivePort(int receivePort)
            throws IllegalStateException {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set receivePort: Soundbus is open" );
        }
        if (this.receivePort == receivePort) {
            return;
        }
        int oldPort = this.receivePort;
        this.receivePort = receivePort;
        propertyChangeSupport.firePropertyChange( "receivePort", oldPort, receivePort );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public String getClickOnOffMessageAddress() {
        return clickOnOffMessageAddress;
    }

    public void setClickOnOffMessageAddress(String clickOnOffMessageAddress) throws IllegalArgumentException {
        if (clickOnOffMessageAddress == null) {
            clickOnOffMessageAddress = DEFAULT_CLICK_ON_OFF_ADDRESS;
        }
        if (clickOnOffMessageAddress.equals(this.clickOnOffMessageAddress)) {
            return;
        }
        if (isOpenImpl()) {
            throw new IllegalStateException("Cannot set OSC click on/off message address while soundbus is open");
        }
        String oldClickOnOffMessageAddress = this.clickOnOffMessageAddress;
        this.clickOnOffMessageAddress = clickOnOffMessageAddress;
        propertyChangeSupport.firePropertyChange("clickOnOffMessageAddress", oldClickOnOffMessageAddress, clickOnOffMessageAddress);
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public boolean isSendEnabled() {
        return sendEnabled;
    }

    public void setSendEnabled(boolean sendEnabled) throws IllegalStateException {
        if (this.sendEnabled == sendEnabled) {
            return;
        }
        if (isOpenImpl()) {
            throw new IllegalStateException("Cannot " + (sendEnabled ? "enable" : "disable") + " OSC send while soundbus is open");
        }
        this.sendEnabled = sendEnabled;
        propertyChangeSupport.firePropertyChange("sendEnabled", !sendEnabled, sendEnabled);
        parent.getSoundbusDescriptor().setChanged(true);
    }
    
    public boolean isSendRegularUpdatesEnabled() {
        return regularUpdatesEnabled;
    }
    
    public void setSendRegularUpdatesEnables(boolean regularUpdatesEnabled) {
        if (this.regularUpdatesEnabled == regularUpdatesEnabled) {
            return;
        }
        if (isOpenImpl()) {
            throw new IllegalStateException("Cannot " + (sendEnabled ? "enable" : "disable") + " OSC regular updates while soundbus is open");
        }
        this.regularUpdatesEnabled = regularUpdatesEnabled;
        propertyChangeSupport.firePropertyChange("regularUpdatesEnabled", !regularUpdatesEnabled, regularUpdatesEnabled);
        parent.getSoundbusDescriptor().setChanged(true);
    }

    public int getSendToPort() {
        if (sendToPort <= 0) {
            sendToPort = OSCPort.defaultSCOSCPort();
        }
        return sendToPort;
    }

    public void setSendToPort(int sendToPort) throws IllegalStateException {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set sendToPort: Soundbus is open" );
        }
        if (this.sendToPort == sendToPort) {
            return;
        }
        int oldPort = this.sendToPort;
        this.sendToPort = sendToPort;
        propertyChangeSupport.firePropertyChange( "sendToPort", oldPort, sendToPort );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public String getTempoMessageAddress() {
        return tempoMessageAddress;
    }

    public void setTempoMessageAddress(String tempoMessageAddress) throws IllegalArgumentException {
        if (tempoMessageAddress == null) {
            tempoMessageAddress = DEFAULT_TEMPO_ADDRESS;
        }
        if (tempoMessageAddress.equals(this.tempoMessageAddress)) {
            return;
        }
        if (isOpenImpl()) {
            throw new IllegalStateException("Cannot set OSC tempo message address while soundbus is open");
        }
        String oldTempoMessageAddress = this.tempoMessageAddress;
        this.tempoMessageAddress = tempoMessageAddress;
        propertyChangeSupport.firePropertyChange("tempoMessageAddress", oldTempoMessageAddress, tempoMessageAddress);
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public String getSendToHost() {
        return sendToHost;
    }

    public void setSendToHost(String sendToHost) {
        if (sendToHost == null) {
            if (this.sendToHost == null) {
                return;
            }
        } else if (sendToHost.equals(this.sendToHost)) {
            return;
        }
        String oldSendToHost = this.sendToHost;
        this.sendToHost = sendToHost;
        propertyChangeSupport.firePropertyChange("sendToHost", oldSendToHost, sendToHost);
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public SbInput[] getInputs() {
        return NO_INPUTS;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return "osc";
    }

    public SbOutput[] getOutputs() {
        return outputs;
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public void setName( String name ) {
        this.name = name;
    }
    
    private TempoNode getTempoNode() {
        for (SbNode node : parent.getNodes()) {
            if (node instanceof TempoNode) {
                return (TempoNode) node;
            }
        }
        return null;
    }

    private void performClickOperation(Object o) {
        float number = 0;
        if (o instanceof Number) {
            number = ((Number) o).floatValue();
        } else if (o instanceof Boolean) {
            number = (((Boolean) o).booleanValue() ? 1 : 0);
        }
        TempoNode node = getTempoNode();
        if (node != null) {
            if (number == 0) {
                // switch off click
                node.setClickEnabled(false);
            } else if (number == 1) {
                // switch on click
                node.setClickEnabled(true);
            } else {
                // set tempo
                parent.setTempo(number);
            }
        }
    }
    
    private void routeMidi(Object o) {
        for (SbOutput output : outputs) {
            if (output instanceof SbMidiOutputImpl) {
                SbMidiOutputImpl midiOutput = (SbMidiOutputImpl) output;
                if (midiOutput.connectedInput != null) {
                    midiOutput.connectedInput.receive((ShortMessage) o, midiOutput);
                }
            }
        }
    }

    private void stopRegularUpdates() {
        if (regularUpdatesThread != null) {
            synchronized (regularUpdatesThread) {
                if (regularUpdatesThread != null) {
                    regularUpdatesThread.end();
                    regularUpdatesThread = null;
                }
            }
        }
    }
    
    public synchronized void openImpl() throws SoundbusException {
        System.out.println( "OSCReceiverNodeImpl.openImpl() : creating UDP socket" );

        try {
            stopRegularUpdates();
            int port = getReceivePort();
            receiver = new OSCPortIn(port);
            OSCListener listener = new OSCListener() {
                public void acceptMessage(Date date, OSCMessage message) {
                    for (Object o : message.getArguments()) {
                        if (o instanceof ShortMessage) { // route MIDI message
                            routeMidi(o);
                        } else { // click
                            performClickOperation(o);
                        }
                    }
                }
            };
            
            receiver.addListener(tempoMessageAddress, listener);
            if (!tempoMessageAddress.equals(clickOnOffMessageAddress)) {
                receiver.addListener(clickOnOffMessageAddress, listener);
            }
            receiver.startListening();
            
            // check if send is enabled and add required listener(s) if so
            if (sendEnabled) {
                parent.addSoundbusListener(soundbusListener);
                TempoNode tn = getTempoNode();
                if (tn != null) {
                    tn.addPropertyChangeListener("clickEnabled", tempoNodePropertyChangeListener);
                }
                
                // start regular updates if enabled
                if (regularUpdatesEnabled) {
                    regularUpdatesThread = new UpdateThread();
                    regularUpdatesThread.start();
                }
            }
            
            open = true;
        } catch (SocketException sex) {
            throw new CannotConnectException(sex);
        }
    }

    public void destroyImpl() throws SoundbusException {
    }

    public void closeImpl() throws SoundbusException {
        stopRegularUpdates();
        receiver.stopListening();
        receiver.close();
        open = false;
        parent.removeSoundbusListener(soundbusListener);
        TempoNode tn = getTempoNode();
        if (tn != null) {
            tn.removePropertyChangeListener("clickEnabled", tempoNodePropertyChangeListener);
        }

    }
    
    public boolean isOpenImpl() {
        return open;
    }

    // Audio output connector impl
    public void putClientProperty( String property, String value ) {
        String oldValue = clientProperties.get( property );
        clientProperties.put( property, value );
        propertyChangeSupport.firePropertyChange( property, oldValue, value );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public boolean removeClientProperty( String property ) {
        Object oldValue = clientProperties.remove( property );
        if (oldValue != null) {
            propertyChangeSupport.firePropertyChange( property, oldValue, null );
            parent.getSoundbusDescriptor().setChanged( true );
            return true;
        }
        return false;
    }

    public String getClientProperty( String property ) {
        return clientProperties.get( property );
    }

    public Map<String, String> getClientProperties() {
        return clientProperties;
    }

    public Map<String, String> getParameters() {
        Map<String, String> p = new HashMap<String, String>();
        p.put("receivePort", Integer.toString(receivePort));
        p.put("sendToPort", Integer.toString(sendToPort));
        p.put("sendEnabled", Boolean.toString(sendEnabled));
        p.put("clickOnOffMessageAddress", clickOnOffMessageAddress);
        p.put("tempoMessageAddress", tempoMessageAddress);
        p.put("sendToHost", sendToHost);
        p.put("regularUpdatesEnabled", Boolean.toString(regularUpdatesEnabled));
        return p;
    }
    
    public void setParameters(Map<String, String> parameters) {
        if (parameters != null) {
            if (parameters.containsKey("receivePort")) {
                String receivePort = parameters.get("receivePort");
                if (receivePort != null) {
                    try { setReceivePort(Integer.parseInt(receivePort)); } catch (NumberFormatException nfex) { nfex.printStackTrace(); };
                }
            }
            if (parameters.containsKey("sendToPort")) {
                String sendToPort = parameters.get("sendToPort");
                if (sendToPort != null) {
                    try { setSendToPort(Integer.parseInt(sendToPort)); } catch (NumberFormatException nfex) { nfex.printStackTrace(); };
                }
            }
            if (parameters.containsKey("sendEnabled")) {
                String sendEnabled = parameters.get("sendEnabled");
                if (sendEnabled != null) {
                    setSendEnabled(Boolean.parseBoolean(sendEnabled));
                }
            }
            if (parameters.containsKey("clickOnOffMessageAddress")) {
                String clickOnOffMessageAddress = parameters.get("clickOnOffMessageAddress");
                if (clickOnOffMessageAddress != null) {
                    setClickOnOffMessageAddress(clickOnOffMessageAddress);
                }
            }
            if (parameters.containsKey("tempoMessageAddress")) {
                String tempoMessageAddress = parameters.get("tempoMessageAddress");
                if (tempoMessageAddress != null) {
                    setTempoMessageAddress(tempoMessageAddress);
                }
            }
            if (parameters.containsKey("sendToHost")) {
                String sendToHost = parameters.get("sendToHost");
                if (sendToHost != null) {
                    setSendToHost(sendToHost);
                }
            }
            if (parameters.containsKey("regularUpdatesEnabled")) {
                String regularUpdatesEnabled = parameters.get("regularUpdatesEnabled");
                if (regularUpdatesEnabled != null) {
                    setSendRegularUpdatesEnables(Boolean.parseBoolean(regularUpdatesEnabled));
                }

            }
        }
    }

    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
    }

    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }

    public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
    }
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    
    class SbMidiOutputImpl implements SbMidiOutput {
        
        private SbMidiInput connectedInput;
        private String name;
        
        SbMidiOutputImpl( String name ) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public SbNode getSbNode() {
            return OSCNodeImpl.this;
        }

        public boolean canConnect( SbInput in ) {
            return (in instanceof SbMidiInput);
        }

        public void connect( SbInput in ) throws CannotConnectException, IllegalStateException {
            if (!canConnect( in )) {
                throw new CannotConnectException("Incompatible types");
            }
            if (connectedInput != null) {
                throw new IllegalStateException("Cannot connect: already connected");
            }
            if (parent.isOpen()) {
                throw new IllegalStateException("Cannot connect: parent soundbus is open");
            }
            connectedInput = (SbMidiInput) in;
            if (in.getConnectedOutput() != null) {
                parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent(parent, in, this));
            }
        }

        public void disconnect() throws IllegalStateException {
            if (parent.isOpen()) {
                throw new IllegalStateException("Cannot disconnect: parent soundbus is open");
            }
            if (connectedInput != null) {
                SbInput in = connectedInput;
                connectedInput = null;
                if (in.getConnectedOutput() == null) {
                    parent.fireNodesDisconnectedEvent(new SoundbusNodesConnectionEvent(parent, in, this));
                }
            }
        }

        public SbInput getConnectedInput() {
            return connectedInput;
        }
    }
    
    class UpdateThread extends Thread {
        boolean running = true;
        
        synchronized void end() {
            running = false;
            notify();
        }
        
        public void run() {
            while (running) {
                synchronized (this) {
                    try {
                        float tempo = Float.valueOf(getSoundbus().getTempo());
                        TempoNode tn = getTempoNode();
                        Boolean on = null;
                        if (tn != null) {
                            on = Boolean.valueOf(tn.isClickEnabled());
                        }
                        if (clickOnOffMessageAddress != null && on != null && clickOnOffMessageAddress.equals(tempoMessageAddress)) {
                            // combined message
                            sendOscMessage(clickOnOffMessageAddress, new Object[] { on, tempo });
                        } else {
                            // single messages
                            if (clickOnOffMessageAddress != null && on != null) {
                                sendOscMessage(clickOnOffMessageAddress, new Object[] { on });
                            }
                            sendOscMessage(tempoMessageAddress, new Object[] { tempo });
                        }
                        wait(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
