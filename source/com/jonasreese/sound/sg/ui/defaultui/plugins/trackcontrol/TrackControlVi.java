/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 17.09.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.trackcontrol;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;

/**
 * This is the <code>ViewInstance</code> implementation for the <i>TrackControl</i> plugin.
 * The TrackControl plugin allows the user to manage the tracks within a MIDI element,
 * such as muting tracks or selecting them for record or solo play. 
 * @author jonas.reese
 */
public class TrackControlVi implements ViewInstance {
    private TrackControlView view;
    private TrackControlComponent trackControlComponent;
    
    /**
     * Constructs a new <code>TrackControlVi</code> object.
     * @param view The <code>TrackControlView</code> parent to this <code>TrackControlVi</code>.
     * @param session The <code>Session</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> to create a <code>TrackControVi</code>
     *        for.
     */
    public TrackControlVi(TrackControlView view, Session session, MidiDescriptor midiDescriptor) {
        this.view = view;
        trackControlComponent = new TrackControlComponent(session, midiDescriptor);
    }
    
    protected void finalize() throws Throwable {
        System.out.println( "TrackControlVi.finalize()" );
        super.finalize();
    }

    /* (non-Javadoc)
     */
    public Object getUiObject(ViewContainer parentUiObject) {
        return trackControlComponent;
    }

    /* (non-Javadoc)
     */
    public View getView() {
        return view;
    }

    /* (non-Javadoc)
     */
    public void open() {
        trackControlComponent.open();
    }

    /* (non-Javadoc)
     */
    public void activate() {
        trackControlComponent.activate();
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewInstance#deactivate()
     */
    public void deactivate() {
        trackControlComponent.deactivate();
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewInstance#close()
     */
    public void close() {
        trackControlComponent.close();
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewInstance#isSetBoundsAllowed()
     */
    public boolean isSetBoundsAllowed() {
        return true;
    }
}
