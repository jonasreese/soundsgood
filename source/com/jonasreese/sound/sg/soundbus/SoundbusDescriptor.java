/*
 * Created on 22.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionElementEvent;
import com.jonasreese.sound.sg.SessionElementListener;
import com.jonasreese.sound.sg.SessionElementType;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SessionListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.impl.SoundbusImpl;
import com.jonasreese.util.ProgressMonitoringInputStream;
import com.jonasreese.util.Updatable;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * @author jonas.reese
 */
public class SoundbusDescriptor extends SessionElementDescriptor {
    public static final SessionElementType TYPE = new SessionElementType() {
        public String getName() {
            return SgEngine.getInstance().getResourceBundle().getString(
                    "descriptor.type.soundbus" );
        }
        public String getDescription() {
            return SgEngine.getInstance().getResourceBundle().getString(
                "descriptor.type.soundbus.description" );
        }

        public Image getSmallIcon() {
            return new ResourceLoader( getClass(), "/resource/soundbus.gif" ).getAsImage();
        }

        public Image getLargeIcon() {
            return new ResourceLoader( getClass(), "/resource/soundbus_large.gif" ).getAsImage();
        }
    };

    private String name;
    private Soundbus soundbus;
    
    private SessionListener sessionListener;
    private SessionElementListener sessionElementListener;
    
    /**
     * Constructs a new <code>SoundbusDescriptor</code>.
     */
    public SoundbusDescriptor() {
        name = null;
        soundbus = null;
        sessionListener = new SessionListener() {
            public void sessionAdded( SessionEvent e ) {
            }
            public void sessionRemoved( SessionEvent e ) {
                e.getSession().removeSessionListener( this );
                if (soundbus != null) {
                    try {
                        System.out.println( "SoundbusDescriptor: Parent session removed, closing soundbus" );
                        soundbus.close();
                    } catch (SoundbusException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            public void sessionActivated( SessionEvent e ) {
            }
            public void sessionDeactivated( SessionEvent e ) {
            }
        };
        sessionElementListener = new SessionElementListener() {
            public void elementAdded( SessionElementEvent e ) {
            }
            public void elementRemoved( SessionElementEvent e ) {
                if (e.getSessionElement() == SoundbusDescriptor.this) {
                    getSession().removeSessionElementListener( this );
                    if (soundbus != null) {
                        try {
                            System.out.println(
                                    "SoundbusDescriptor: Session element removed, closing soundbus" );
                            soundbus.close();
                        } catch (SoundbusException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    /**
     * Returns the Soundbus type.
     * @return The type, same as <code>SoundbusDescriptor.TYPE</code>.
     */
    public SessionElementType getType() {
        return TYPE;
    }

    /**
     * Add listener.
     */
    public void setSession( Session session ) {
        Session oldSession = getSession();
        super.setSession( session );
        if (session != null) {
            session.addSessionListener( sessionListener );
            session.addSessionElementListener( sessionElementListener );
        }
        if (oldSession != null) {
            oldSession.removeSessionListener( sessionListener );
            oldSession.removeSessionElementListener( sessionElementListener );
        }
    }


    public void resetData() {
        // nothing to do
    }

    public String getTypeDescription() {
        return SgEngine.getInstance().getResourceBundle().getString( "descriptor.type.soundbus" );
    }

    /**
     * Sets this <code>MidiDescriptor</code>s name.
     * @param name The name to set. Can be <code>null</code> if the
     *        name shall be set to it's default value.
     */
    public void setName( String name ) {
        this.name = name;
    }
    
    /**
     * Gets this <code>SoundbusDescriptor</code>s name.
     * @return The name, or <code>null</code> if none is assigned.
     */
    public String getName() {
        if (name != null) {
            return name;
        }
        File f = getFile();
        if (f != null) {
            return f.getName();
        }
        return SgEngine.getInstance().getResourceBundle().getString(
            "soundbus.new.defaultName" );
    }
    
    /**
     * Returns the <code>Soundbus</code> contained in this <code>SoundbusDescriptor</code>.
     * @return The soundbus (not <code>null</code>). If this descriptor has just been created
     * or reset, an empty soundbus will be returned.
     * @throws IOException if the soundbus could not be loaded.
     */
    public Soundbus getSoundbus() throws IOException, SAXException, IllegalSoundbusDescriptionException {
        if (soundbus == null) {
            SoundbusImpl soundbusImpl = new SoundbusImpl( this );
            soundbus = soundbusImpl;
            File f = getFile();
            if (f != null) {
                load( f, soundbusImpl );
                setChanged( false );
            }
        }
        return soundbus;
    }
    
    public void save() throws IOException {
        try {
            saveCopy( getFile() );
            setChanged( false );
        } catch (IOException ioex) {
            throw ioex;
        }
    }
    
    public void saveCopy( File copy ) throws IOException {
        Soundbus soundbus;
        try {
            soundbus = getSoundbus();
        } catch (Exception ex) {
            // nothing to save
            return;
        }
        // create XML serialization of Soundbus
        try {
            Element root = new Element( "soundbus" );

            root.setAttribute( "tempo", Float.toString( soundbus.getTempo() ) );

            Map<String, String> clientProperties = soundbus.getClientProperties();
            if (clientProperties != null && !clientProperties.isEmpty()) {
                Element properties = new Element( "properties" );
                for (String key : clientProperties.keySet()) {
                    Element elem = new Element( "property" );
                    elem.setAttribute( "name", key );
                    elem.setContent( new Text( clientProperties.get( key ) ) );
                    properties.addContent( elem );
                }
                root.addContent( properties );
            }
            
            // serialize nodes
            SbNode[] sbNodes = soundbus.getNodes();
            SoundbusToolkit.serializeSoundbusNodes( sbNodes, root );
            
            XMLOutputter xmlOutputter = new XMLOutputter();
            FileOutputStream fout = new FileOutputStream( copy );
            org.jdom.Document doc = new org.jdom.Document( root );
            xmlOutputter.setFormat( Format.getPrettyFormat() );
            xmlOutputter.output( doc, fout );
            fout.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException( ex.getMessage() );
        }
    }
    
    @Override
    public void destroy() {
        Soundbus soundbus = this.soundbus;
        if (soundbus != null) {
            try {
                soundbus.destroy();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void load( File f, SoundbusImpl soundbus )
    throws IOException, SAXException, IllegalSoundbusDescriptionException {
        MyProgressMonitoringInputStream is =
                new MyProgressMonitoringInputStream(
                    new FileInputStream( f ),
                    f, 200, SgEngine.getInstance().getLoadingUpdatable() );
        SoundbusToolkit.deserializeSoundbusElements( is, soundbus, null );
    }
    
    /// an own implementation to do some hacking with it
    class MyProgressMonitoringInputStream extends ProgressMonitoringInputStream {
        Updatable updatable;
        boolean done = false;
        boolean firstCall = true;
        public MyProgressMonitoringInputStream(
            InputStream arg0, Object arg1, int arg2, Updatable updatable) throws IOException {
            super(arg0, arg1, arg2);
            setUpdatable( updatable );
            this.updatable = updatable;
        }
    }
}
