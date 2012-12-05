/*
 * Created on 26.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.ConnectSbNodesEdit;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public abstract class PSbNode extends PNode {

    private static final long serialVersionUID = 1L;

    public static final String BOUNDS_PROPERTY_NAME = "bounds";
    public static final String OFFSET_PROPERTY_NAME = "offset";
    
    protected Stroke stroke;
    protected Stroke activeStroke;
    
    protected Paint backgroundPaint;
    protected Color titleTextSecondLinePaint;
    
    protected String titleTextFirstLine;
    protected String titleTextSecondLine;
    
    protected Font firstLineFont;
    protected Font secondLineFont;
    
    protected SbEditorComponent editor;
    protected SbNode node;
    
    private SoundbusDescriptor soundbusDescriptor;
    protected List<InOutNode> inputList;
    protected List<InOutNode> outputList;
    
    private PBounds resizeBounds;
    
    protected static final short TOP = 1;
    protected static final short TOP_LEFT = 2;
    protected static final short LEFT = 3;
    protected static final short BOTTOM_LEFT = 4;
    protected static final short BOTTOM = 5;
    protected static final short BOTTOM_RIGHT = 6;
    protected static final short RIGHT = 7;
    protected static final short TOP_RIGHT = 8;
    
    /**
     * Constructs a new <code>PSbNode</code>.
     * @param editor The parent editor component.
     * @param node The target <code>SbNode</code>.
     * @param soundbusDescriptor the parent <code>SoundbusDescritptor</code>
     */
    public PSbNode( SbEditorComponent editor, SbNode node, SoundbusDescriptor soundbusDescriptor ) {
        String titleTextFirstLine = node.getName();
        String titleTextSecondLine = null;
        this.editor = editor;
        this.node = node;
        this.soundbusDescriptor = soundbusDescriptor;
        stroke = new BasicStroke( 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        activeStroke = new BasicStroke( 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        setPaint( Color.BLACK );
        backgroundPaint = Color.LIGHT_GRAY;
        this.titleTextFirstLine = (titleTextFirstLine == null ? "" : titleTextFirstLine);
        this.titleTextSecondLine = (titleTextSecondLine == null ? "" : titleTextSecondLine);
        setPickable( true );
        setBounds( 0, 0, 180, 95 );
        firstLineFont = new Font( "Monospaced", Font.BOLD, 12 );
        secondLineFont = new Font( "Monospaced", Font.ITALIC, 12 );
        inputList = new ArrayList<InOutNode>();
        outputList = new ArrayList<InOutNode>();
        createInputOutputHandles();
    }
    
    /**
     * Gets the parent soundbus descriptor.
     * @return The parent descriptor.
     */
    public SoundbusDescriptor getSoundbusDescriptor() {
        return soundbusDescriptor;
    }
    
    public void startResizeBounds() {
        super.startResizeBounds();
        resizeBounds = getBounds();
    }
    
    public void endResizeBounds() {
        super.endResizeBounds();
        PBounds resizeBounds = this.resizeBounds;
        if (resizeBounds == null) {
            return;
        }
        SbNodeResizeEdit resizeEdit = new SbNodeResizeEdit(
                soundbusDescriptor, getSbNode(), null, resizeBounds, getBounds() );
        resizeEdit.perform();
        PSbNode.this.soundbusDescriptor.getUndoManager().addEdit( resizeEdit );
    }
    
    /**
     * Gets the bounds set for this node's <code>SbNode</code>, as defined by the node's
     * bounds client property. This value is <b>not</b> taken from the UI component bounds.
     * @param node The node.
     * @return The bounds, or <code>null</code> if no or invalid bounds are set within the
     * node's client properties.
     */
    public static Rectangle2D.Double getBoundsProperty( SbNode node ) {
        Rectangle2D.Double result = null;
        String s = node.getClientProperty( BOUNDS_PROPERTY_NAME );
        if (s != null) {
            StringTokenizer st = new StringTokenizer( s, "," );
            if (st.countTokens() == 4) {
                try {
                    double x = Double.parseDouble( st.nextToken() );
                    double y = Double.parseDouble( st.nextToken() );
                    double width = Double.parseDouble( st.nextToken() );
                    double height = Double.parseDouble( st.nextToken() );
                    result = new Rectangle2D.Double( x, y, width, height );
                } catch (NumberFormatException nfex) {
                }
            }
        }
        return result;
    }
    
    /**
     * Sets the node's bounds client property to the given bounds.
     * This value is <b>not</b> taken from the UI component bounds.
     * @param node The node.
     * @param bounds The bounds to set.
     */
    public static void setBoundsProperty( SbNode node, Rectangle2D.Double bounds ) {
        node.putClientProperty(
                BOUNDS_PROPERTY_NAME,
                "" + bounds.getX() + "," + bounds.getY() +
                "," + bounds.getWidth() + "," + bounds.getHeight() );
    }

    /**
     * Sets the node's offset client property to the given offset.
     * @param node The node.
     * @param xOffset The X offset to set.
     * @param yOffset The Y offset to set.
     */
    public static void setOffsetProperty( SbNode node, double xOffset, double yOffset ) {
        node.putClientProperty( OFFSET_PROPERTY_NAME, "" + xOffset + "," + yOffset );
    }
    
    /**
     * Gets the offset set for this node's <code>SbNode</code>, as defined by the node's
     * offset client property. This value is <b>not</b> taken from the UI component offset.
     * @param node The node.
     * @return The offset, or <code>null</code> if no or an invalid offset are set within the
     * node's client properties.
     */
    public static Point2D.Double getOffsetProperty( SbNode node ) {
        String s = node.getClientProperty( OFFSET_PROPERTY_NAME );
        if (s != null) {
            StringTokenizer st = new StringTokenizer( s, "," );
            if (st.countTokens() == 2) {
                try {
                    double x = Double.parseDouble( st.nextToken() );
                    double y = Double.parseDouble( st.nextToken() );
                    return new Point2D.Double( x, y );
                } catch (NumberFormatException nfex) {
                }
            }
        }
        return null;
    }
    
    protected void addInputHandle( int index, int numInputs, int numOutputs, SbInput input ) {
        InOutNode h = new InOutNode( index, numInputs, numOutputs, 20, 20, input, null );
        addChild( h );
        inputList.add( h );
    }
    
    protected void addOutputHandle( int index, int numInputs, int numOutputs, SbOutput output ) {
        InOutNode h = new InOutNode( index, numInputs, numOutputs, 20, 20, null, output );
        addChild( h );
        outputList.add( h );
    }
    
    /**
     * Creates the handles for input/output.
     */
    protected void createInputOutputHandles() {
        SbInput[] inputs = node.getInputs();
        SbOutput[] outputs = node.getOutputs();
        synchronized (inputList) {
            for (int i = 0; i < inputs.length; i++) {
                addInputHandle( i, inputs.length, outputs.length, inputs[i] );
            }
        }
        synchronized (outputList) {
            for (int i = 0; i < outputs.length; i++) {
                addOutputHandle( i, inputs.length, outputs.length, outputs[i] );
            }
        }
    }

    public void moveToFront() {
        super.moveToFront();
        List<?> l = getChildrenReference();
        for (int i = 0; i < l.size(); i++) {
            Object o = l.get( i );
            if (o instanceof InOutNode) {
                ((InOutNode) o).arrowToFront();
            }
        }
    }
    
    public void setOffset( double x, double y ) {
        super.setOffset( x, y );
        List<?> l = getChildrenReference();
        for (int i = 0; i < l.size(); i++) {
            Object o = l.get( i );
            if (o instanceof InOutNode) {
                ((InOutNode) o).adjustArrow();
            }
        }
    }
    
    public boolean setBounds( double x, double y, double width, double height ) {
        boolean b = super.setBounds( x, y, width, height );
        List<?> l = getChildrenReference();
        for (int i = 0; i < l.size(); i++) {
            Object o = l.get( i );
            if (o instanceof InOutNode) {
                ((InOutNode) o).adjustBounds();
            }
        }
        return b;
    }
    
    public void setTitleTextFirstLine( String titleTextFirstLine ) {
        this.titleTextFirstLine = titleTextFirstLine;
        super.repaint();
    }
    
    public void setTitleTextSecondLine( String titleTextSecondLine ) {
        this.titleTextSecondLine = titleTextSecondLine;
        super.repaint();
    }
    
    /**
     * This method can be overwritten to implement the action that shall be performed
     * when the user selected to 'edit' a node. The default implementation of this
     * method does nothing.
     */
    public void editNode() {}

    /**
     * This method returns the <code>SbNode</code> that is wrapped by this graphical
     * representation of an <code>SbNode</code>.
     * @return The <code>SbNode</code>. Shall <b>not</b> be <code>null</code>.
     */
    public SbNode getSbNode() {
        return node;
    }
    
    protected InOutNode getInOutNodeFor( SbInput input ) {
        synchronized (inputList) {
            for (int i = 0; i < inputList.size(); i++) {
                if (inputList.get( i ).in == input) {
                    return inputList.get( i );
                }
            }
        }
        return null;
    }
    
    protected InOutNode getInOutNodeFor( SbOutput output ) {
        synchronized (outputList) {
            for (int i = 0; i < outputList.size(); i++) {
                if (outputList.get( i ).out == output) {
                    return outputList.get( i );
                }
            }
        }
        return null;
    }
    
    /**
     * Shall be invoked for updating a connection between two nodes.
     * @param destNode The <code>PSbNode</code> that contains the output.
     * @param input The input that belongs to this <code>PSbNode</code>
     * @param output The output that belongs to <code>destNode</code>.
     */
    public void nodesConnected( PSbNode destNode, SbInput input, SbOutput output ) {
        // search for input InOutNode
        final InOutNode thisNode = getInOutNodeFor( input );
        if (thisNode == null) { return; }
        // search for output InOutNode
        final InOutNode dest = destNode.getInOutNodeFor( output );
        if (dest == null) { return; }
            
        thisNode.connectedInOut = dest;
        dest.connectedInOut = thisNode;
        thisNode.arrow = new PArrow( 0, 0, true );
        thisNode.arrow.setInput( input );
        thisNode.arrow.setOutput( output );
        getParent().addChild( thisNode.arrow );
        dest.arrow = thisNode.arrow;
        thisNode.adjustArrow();
    }
    
    /**
     * Shall be invoked for updating a connection between two nodes.
     * @param destNode The <code>PSbNode</code> that contains the output.
     * @param input The input that belongs to this <code>PSbNode</code>
     * @param output The output that belongs to <code>destNode</code>.
     */
    public void nodesDisconnected( PSbNode destNode, SbInput input, SbOutput output ) {
        // search for input InOutNode
        final InOutNode thisNode = getInOutNodeFor( input );
        if (thisNode == null) { return; }
        // search for output InOutNode
        final InOutNode dest = destNode.getInOutNodeFor( output );
        if (dest == null) { return; }
        
        thisNode.connectedInOut.connectedInOut = null;
        thisNode.connectedInOut.arrow = null;
        thisNode.connectedInOut = null;
        thisNode.arrow.getParent().removeChild( thisNode.arrow );
        thisNode.arrow.setInput( null );
        thisNode.arrow.setOutput( null );
        thisNode.arrow = null;
    }

    
    protected void paint( PPaintContext paintContext ) {
        Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke( stroke );
        PBounds b = getBoundsReference();
        g2.setPaint( backgroundPaint );
        g2.fill( b );
        if (node.getSoundbus().isOpen()) {
            g2.setPaint( Color.RED );
            g2.setStroke( activeStroke );
        } else {
            g2.setPaint( getPaint() );
            g2.setStroke( stroke );
        }
        g2.draw( b );
        g2.setPaint( Color.BLACK );
        Shape clip = g2.getClip();
        g2.setClip( getBounds() );
        FontMetrics fm = g2.getFontMetrics( firstLineFont );
        float x = (float) (getX() + getWidth() / 2.0 - fm.stringWidth( titleTextFirstLine ) / 2.0);
        float y = (float) (getY() + getHeight() / 2.0 + fm.getDescent());
        if (titleTextSecondLine != null && titleTextSecondLine.length() > 0) {
            g2.setFont( secondLineFont );
            FontMetrics fm2 = g2.getFontMetrics( secondLineFont );
            Paint p = null;
            if (titleTextSecondLinePaint != null) {
                p = g2.getPaint();
                g2.setPaint( titleTextSecondLinePaint );
            }
            StringTokenizer st = new StringTokenizer( titleTextSecondLine, "\n" );
            int tokens = st.countTokens();
            y -= (float) ((fm.getHeight() + 5) * ((float) tokens)) / 2.0;
            
            for (int i = 0; i < tokens; i++) {
                String token = st.nextToken();
                g2.drawString(
                    token,
                    (float) (getX() + getWidth() / 2.0 - fm2.stringWidth( token ) / 2.0),
                    y + (i + 1) * fm.getHeight() + 5 );
            }
            if (p != null) {
                g2.setPaint( p );
            }
        }
        g2.setFont( firstLineFont );
        g2.drawString( titleTextFirstLine, x, y );
        g2.setClip( clip );
    }
    
    /**
     * Overwrite method to do your own in/out node painting.
     * @param n The in/out node to be painted.
     * @param paintContext The <code>PPaintContext</code>.
     */
    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke( stroke );
        PBounds b = n.getBoundsReference();
        g2.setPaint( n.getPaint() );
        if (n.hot) {
            if (n.draggingArrow == null) {
                g2.setPaint( ((Color) n.getPaint()).darker() );
            } else {
                g2.setPaint( Color.BLACK );
            }
        }
        g2.fill( b );
    }
    
    private Point2D.Double getCenter() {
        PBounds b = getBoundsReference();
        return new Point2D.Double(
                b.x + b.width / 2.0 + getXOffset(), b.y + b.height / 2.0 + getYOffset() );
    }
    
    /**
     * Invoked when the underlying <code>SbNode</code> is added to the soundbus.
     * <p>This is typically directly after initialization.
     */
    public void nodeAdded() {}

    /**
     * Invoked when the underlying <code>SbNode</code> is removed from the soundbus.
     */
    public void nodeRemoved() {}
    
    public class InOutNode extends PNode {
        private static final long serialVersionUID = 1L;
        
        int num;
        int total;
        int numOther;
        double width;
        double height;
        short where;
        SbInput in;
        SbOutput out;
        boolean hot;
        InOutNode connectedInOut;
        PArrow draggingArrow;
        PArrow arrow;
        
        InOutNode(
                int num, int total, int numOther,
                double width, double height, SbInput in, SbOutput out ) {
            this.num = num;
            this.total = total;
            this.numOther = numOther;
            this.width = width;
            this.height = height;
            this.in = in;
            this.out = out;
            hot = false;
            draggingArrow = null;
            where = getDefaultWhere();
            adjustBounds();
            setPaint( (in == null ? Color.LIGHT_GRAY : Color.GRAY) );
            
            addInputEventListener( new PBasicInputEventHandler() {
                public void mouseEntered( PInputEvent e ) {
                    setHot( true );
                }
                public void mouseExited( PInputEvent e ) {
                    setHot( false );
                }
                public void mouseDragged( PInputEvent e ) {
                    if (draggingArrow != null) {
                        draggingArrow.dragTo( e.getPosition().getX(), e.getPosition().getY() );
                    }
                }
                public void mousePressed( PInputEvent e ) {
                    if (draggingArrow == null && arrow == null) {
                        draggingArrow = new PArrow( 
                                e.getPosition().getX(),
                                e.getPosition().getY(),
                                isInputNode() );
                        PSbNode.this.getParent().addChild( draggingArrow );
                    }
                }
                public void mouseReleased( PInputEvent e ) {
                    System.out.println( "mouseReleased()" );
                    if (draggingArrow != null) {
                        boolean remove = true;
                        ArrayList<Object> results = new ArrayList<Object>();
                        PBounds rect = new PBounds(
                                e.getPosition().getX(), e.getPosition().getY(), 1, 1 );
                        PSbNode.this.getParent().findIntersectingNodes( rect, results );
                        InOutNode dest = null;
                        for (int i = 0; i < results.size(); i++) {
                            Object o = results.get( i );
                            if (o instanceof InOutNode) {
                                dest = (InOutNode) o;
                                break;
                            }
                        }
                        if (dest != null && dest.arrow == null) {
                            if (dest != InOutNode.this) {
                                if (InOutNode.this.out != null && dest.in != null) {
                                    if (InOutNode.this.out.canConnect( dest.in )) {
                                        remove = false;
                                    }
                                }
                                if (InOutNode.this.in != null && dest.out != null) {
                                    if (dest.out.canConnect( InOutNode.this.in )) {
                                        remove = false;
                                    }
                                }
                            }
                        }
                        draggingArrow.getParent().removeChild( draggingArrow );
                        if (!remove) {
                            SbInput in;
                            SbOutput out;
                            if (InOutNode.this.in == null) {
                                out = InOutNode.this.out;
                                in = dest.in;
                            } else {
                                out = dest.out;
                                in = InOutNode.this.in;
                            }
                            ConnectSbNodesEdit connectEdit = new ConnectSbNodesEdit(
                                    soundbusDescriptor, in, out );
                            connectEdit.perform();
                            soundbusDescriptor.getUndoManager().addEdit( connectEdit );
                        }
                        draggingArrow = null;
                    }
                }
            } );
        }
        
        private short getDefaultWhere() {
            short where;
            if (isInputNode()) {
                where = TOP;
                if (num == 0) {
                    if (numOther + total > 4) {
                        where = TOP_LEFT;
                    }
                } else if (num == 1) {
                    if (numOther + total > 4) {
                        where = TOP_RIGHT;
                    } else {
                        where = RIGHT;
                    }
                } else if (num == 2) {
                    if (numOther + total > 4) {
                        where = BOTTOM_RIGHT;
                    } else {
                        where = LEFT;
                    }
                } else if (num == 3) {
                    if (numOther + total > 4) {
                        where = LEFT;
                    } else {
                        where = BOTTOM_LEFT;
                    }
                }
            } else {
                where = BOTTOM;
                if (num == 0) {
                    if (numOther + total > 4) {
                        where = BOTTOM_RIGHT;
                    }
                } else if (num == 1) {
                    if (numOther + total > 4) {
                        where = BOTTOM_LEFT;
                    } else {
                        where = LEFT;
                    }
                } else if (num == 2) {
                    if (numOther + total > 4) {
                        where = TOP_LEFT;
                    } else {
                        where = RIGHT;
                    }
                } else if (num == 3) {
                    if (numOther + total > 4) {
                        where = RIGHT;
                    } else {
                        where = TOP_RIGHT;
                    }
                }
            }
            //System.out.println( "where = " + getWhereString( where ) );
            return where;
        }
        
        public void setHot( boolean hot ) {
            this.hot = hot;
            if (hot) {
                String name = in == null ? out.getName() : in.getName();
                String description = (in == null ? out.getDescription() : in.getDescription());
                if (name != null && description != null) {
                    name = "<html>" + name + "<br>" + description + "</html>";
                }
                editor.setToolTipText( name );
            } else {
                editor.setToolTipText( null );
            }
            repaint();
        }
        
        public boolean isInputNode() {
            return (out == null);
        }
        
        public boolean isOutputNode() {
            return (in == null);
        }
        
        boolean paintedInCorner() {
            return where == TOP_LEFT || where == TOP_RIGHT || where == BOTTOM_LEFT || where == BOTTOM_RIGHT;
        }
        
        void arrowToFront() {
            if (arrow != null) {
                arrow.moveToFront();
            }
        }
        
        boolean isInOutAt( short where ) {
            for (InOutNode n : outputList) {
                if (n.where == where) {
                    return true;
                }
            }
            for (InOutNode n : inputList) {
                if (n.where == where) {
                    return true;
                }
            }
            return false;
        }
        
        short findNextFreeWhere( short where ) {
            for (short i = (short) (where + 1); i < 9; i++) {
                if (!isInOutAt( i )) {
                    return i;
                }
            }
            for (short i = 1; i < where; i++) {
                if (!isInOutAt( i )) {
                    return i;
                }
            }
            
            return where;
        }
        
        void adjustBounds() {
            adjustBounds( true );
        }
        void adjustBounds( boolean adjustArrow ) {
            PBounds b = PSbNode.this.getBounds();
            // check for unconnected nodes a the same position and move
            // them to free position if possible
            if (connectedInOut != null) {
                for (InOutNode n : inputList) {
                    if (n.connectedInOut == null) {
                        if (n.where == where) {
                            n.where = findNextFreeWhere( where );
                            n.adjustBounds( false );
                        }
                    }
                }
                for (InOutNode n : outputList) {
                    if (n.connectedInOut == null) {
                        if (n.where == where) {
                            n.where = findNextFreeWhere( where );
                            n.adjustBounds( false );
                        }
                    }
                }
            }
            double x = 0;
            double y = 0;
            double width = this.width;
            double height = this.height;
            if (where == TOP) {
                x = b.x + b.width / 2.0 - width / 2.0;
                y = b.y;
            } else if (where == BOTTOM) {
                x = b.x + b.width / 2.0 - width / 2.0;
                y = b.y + b.height - height;
            } else if (where == LEFT) {
                x = b.x;
                y = b.y + b.height / 2.0 - height / 2.0;
            } else if (where == RIGHT) {
                x = b.x + b.width - width;
                y = b.y + b.height / 2.0 - height / 2.0;
            } else if (where == TOP_LEFT) {
                x = b.x;
                y = b.y;
            } else if (where == TOP_RIGHT) {
                x = b.x + b.width - width;
                y = b.y;
            } else if (where == BOTTOM_LEFT) {
                x = b.x;
                y = b.y + b.height - height;
            } else if (where == BOTTOM_RIGHT) {
                x = b.x + b.width - width;
                y = b.y + b.height - height;
            }
            setBounds( x, y, width, height );
            if (adjustArrow) {
                adjustArrow();
            }
        }
        
        /*
         * Returns direction where the arrow points (BOTTOM, LEFT, RIGHT or TOP), or -1
         * if no arrow.
         */
        private short getArrowDirection() {
            InOutNode connectedInOut = this.connectedInOut;
            if (connectedInOut == null) {
                return -1;
            }
            
            Point2D.Double thisP = getPSbNode().getCenter();
            Point2D.Double thatP = connectedInOut.getPSbNode().getCenter();
            return getArrowDirection( thisP, thatP );
        }
        
        PSbNode getPSbNode() {
            return PSbNode.this;
        }
        
        private short getArrowDirection( Point2D.Double thisP, Point2D.Double thatP ) {
            double a = thatP.y - thisP.y;
            double b = thatP.x - thisP.x;
            double alpha;
            if (b == 0) {
                alpha = Math.PI / 2.0;
            } else {
                alpha = Math.atan( a / b );
            }
            
            short result = 0;
            
            alpha = (alpha * 90.0) / (Math.PI / 2.0);
            if (30.0 <= Math.abs( alpha )) {
                if (thatP.y > thisP.y) {
                    result = BOTTOM;
                } else {
                    result = TOP;
                }
            }
            if (60.0 >= Math.abs( alpha )) {
                if (thatP.x < thisP.x) {
                    if (result == BOTTOM) {
                        result = BOTTOM_LEFT;
                    } else if (result == TOP) {
                        result = TOP_LEFT;
                    } else {
                        result = LEFT;
                    }
                } else {
                    if (result == BOTTOM) {
                        result = BOTTOM_RIGHT;
                    } else if (result == TOP) {
                        result = TOP_RIGHT;
                    } else {
                        result = RIGHT;
                    }
                }
            }
            return result;
        }
        
        String getWhereString( short where ) {
            if (where == TOP) { return "TOP"; }
            if (where == BOTTOM) { return "BOTTOM"; }
            if (where == LEFT) { return "LEFT"; }
            if (where == RIGHT) { return "RIGHT"; }
            if (where == TOP_RIGHT) { return "TOP_RIGHT"; }
            if (where == TOP_LEFT) { return "TOP_LEFT"; }
            if (where == BOTTOM_RIGHT) { return "BOTTOM_RIGHT"; }
            if (where == BOTTOM_LEFT) { return "BOTTOM_LEFT"; }
            return "UNDEFINED";
        }
        
        void adjustArrow() {
            adjustArrow( true );
        }
        
        void adjustArrow( boolean connectedToo ) {
            short where = getArrowDirection();
            if (where >= 0 && where != this.where) {
                this.where = where;
                adjustBounds( false );
            }
            if (arrow != null) {
                Point2D offset = getGlobalTranslation();
                Rectangle2D b = getBoundsReference();
                double x = offset.getX() + b.getX();
                double y = offset.getY() + b.getY();
                if (where == TOP) {
                    x += b.getWidth() / 2;
                } else if (where == BOTTOM) {
                    x += b.getWidth() / 2;
                    y += b.getHeight();
                } else if (where == LEFT) {
                    y += b.getHeight() / 2;
                } else if (where == RIGHT) {
                    x += b.getWidth();
                    y += b.getHeight() / 2;
                } else if (where == TOP_RIGHT) {
                    x += b.getWidth();
                } else if (where == BOTTOM_LEFT) {
                    y += b.getHeight();
                } else if (where == BOTTOM_RIGHT) {
                    x += b.getWidth();
                    y += b.getHeight();
                }
                if (isInputNode()) {
                    arrow.setStart( x, y );
                } else {
                    arrow.setEnd( x, y );
                }
            }
            if (connectedToo && connectedInOut != null) {
                connectedInOut.adjustArrow( false );
            }
        }
        
        protected void paint( PPaintContext paintContext ) {
            PSbNode.this.paintInOutNode( this, paintContext );
        }
    }
    
    static class SbNodeResizeEdit extends SbNodeStateChangeEdit {
        private static final long serialVersionUID = 1L;
        Rectangle2D.Double oldBounds;
        PBounds resizeBounds;

        public SbNodeResizeEdit(
                SoundbusDescriptor soundbusDescriptor,
                SbNode node,
                String presentationName,
                PBounds resizeBounds,
                PBounds oldBounds ) {
            super( soundbusDescriptor, node, presentationName, false );
            this.resizeBounds = resizeBounds;
            this.oldBounds = oldBounds;
        }
        
        public void undoImpl() {
            setBoundsProperty( getNode(), resizeBounds );
        }
        public void redoImpl() {
            performImpl();
        }
        @Override
        public void performImpl() {
            setBoundsProperty( getNode(), oldBounds );
        }
    }
}
