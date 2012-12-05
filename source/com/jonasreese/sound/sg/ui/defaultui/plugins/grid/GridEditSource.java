/*
 * Created on 03.05.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

/**
 * <p>
 * This class is responsible for wrapping all information required
 * to determine an edit performed by a <code>GridComponent</code>.
 * It does not contain a reference to the source <code>GridComponent</code>,
 * because this would mean that a <code>GridComponent</code> remains in memory
 * all the time an edit is residing in the <code>UndoManager</code>. Thus,
 * this class decouples a <code>GridComponent</code> being responsible for
 * a change on a MIDI sequence from the <code>GridComponent</code> itself.
 * </p>
 * @author jreese
 */
public class GridEditSource
{
    public static final int MOVE = 0;
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int CHANGE = 3;
    public static final int PASTE = 4;
    public static final int ADD_TRACK = 10;
    public static final int REMOVE_TRACK = 11;
    public static final int CHANGE_TRACK_NAME = 12;
    public static final int CHANGE_TEMPO = 18;
    
    private int editType;
    private int gcHashCode;
    private Object specificData;
    
    /**
     * Constructs a new <code>GridEditSource</code>.
     * @param editType The type of the edit. Also defines how
     *        <code>specificData</code> has to be interpreted.
     * @param gcHashCode The hash code of the <code>GridCmponent</code> object
     *        that has actually performed the change.
     * @param specificData An <code>Object</code> that is specific for each
     *        <code>editType</code>.
     */
    public GridEditSource( int editType, int gcHashCode, Object specificData )
    {
        this.editType = editType;
        this.gcHashCode = gcHashCode;
        this.specificData = specificData;
    }
    
    /**
     * Gets the edit type.
     * @return The edit type.
     */
    public int getEditType() { return editType; }
    
    /**
     * Determines wether the given <code>GridComponent</code> is responsible
     * for the edit.
     * @param gc The <code>GridComponent</code> to check.
     * @return <code>true</code> if the given <code>GridComponent</code>'s hash code
     *         equals the hash code of the responsible <code>GridComponent</code>.
     */
    public boolean isResponsible( GridComponent gc ) { return gc.hashCode() == gcHashCode; }
    
    /**
     * Gets the specific data. Use <code>getEditType()</code> in order to determine
     * how to interpret the returned <code>Object</code>.
     * @return The specific data <code>Object</code>.
     */
    public Object getSpecificData() { return specificData; }
    
    
    public String toString()
    {
        String etStr = null;
        switch (editType)
        {
            case MOVE:
                etStr = "MOVE";
                break;
            case ADD:
                etStr = "ADD";
                break;
            case DELETE:
                etStr = "DELETE";
                break;
            case CHANGE:
                etStr = "CHANGE";
                break;
            case ADD_TRACK:
                etStr = "TRACK";
                break;
            case REMOVE_TRACK:
                etStr = "REMOVE_TRACK";
                break;
            case CHANGE_TRACK_NAME:
                etStr = "TRACK_NAME";
                break;
            case CHANGE_TEMPO:
                etStr = "CHANGE_TEMPO";
                break;
        }
        return "GridEditSource: editType = " + etStr + ", hashCode = " + gcHashCode;
    }
}