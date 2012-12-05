/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 31.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;

/**
 * <p>
 * This class wraps a tool bar logically.
 * </p>
 * @author jreese
 */
public class SgToolBar
{
    private List<Object> toolBar;
    
    /**
     * Constructs a new empty <code>SgToolBar</code>.
     */
    public SgToolBar() {
        toolBar = new ArrayList<Object>();
    }
    
    public void add( Action action ) {
        toolBar.add( action );
    }
    public void add( JComponent c ) {
        toolBar.add( c );
    }
    public void addSeparator() {
        toolBar.add( new Object() );
    }
    public void addSeparator( Dimension size ) {
        toolBar.add( size );
    }
    public void fillJToolBar( JToolBar toFill ) {
        for (int i = 0; i < toolBar.size(); i++) {
            Object o = toolBar.get( i );
            if (o instanceof Action) {
                toFill.add( (Action) o );
            } else if (o instanceof JComponent) {
                toFill.add( (JComponent) o );
            } else if (o instanceof Dimension) {
                toFill.addSeparator( (Dimension) o );
            } else {
                toFill.addSeparator();
            }
        }
    }
}
