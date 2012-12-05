/*
 * Created on 02.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbOutput;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PPaintContext;

public class PArrow extends PPath {
    private static final long serialVersionUID = 1L;
    
    Point2D.Double start;
    Point2D.Double end;
    private Stroke stroke;
    private boolean startWithArrow;
    private SbInput input;
    private SbOutput output;
    
    public PArrow( double x, double y, boolean startWithArrow ) {
        this.start = new Point2D.Double( x, y );
        this.end = new Point2D.Double( x, y );;
        this.startWithArrow = startWithArrow;
        stroke = new BasicStroke( 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        setPaint( Color.BLACK );
    }
    
    public boolean startsWithArrow() {
        return startWithArrow;
    }
    
    /**
     * Gets the <code>SbInput</code>.
     * @return The <code>SbInput</code> connected through this arrow, or <code>null</code>
     * if no input connected (yet).
     */
    public SbInput getInput() {
        return input;
    }

    /**
     * Sets the input.
     * @param input The input to set.
     */
    public void setInput( SbInput input ) {
        this.input = input;
    }

    /**
     * Gets the <code>SbOutput</code>.
     * @return The <code>SbOutput</code> connected through this arrow, or <code>null</code>
     * if no output connected (yet).
     */
    public SbOutput getOutput() {
        return output;
    }

    /**
     * Sets the output.
     * @param output The output to set.
     */
    public void setOutput( SbOutput output ) {
        this.output = output;
    }

    public void setOffset( double x, double y ) {
    }
    
    public void dragTo( double x, double y ) {
        if (startWithArrow) {
            end.x = x;
            end.y = y;
        } else {
            start.x = x;
            start.y = y;
        }
        updateBounds();
    }
    
    public void setStart( double x, double y ) {
        start.x = x;
        start.y = y;
        updateBounds();
    }
    
    public void setEnd( double x, double y ) {
        end.x = x;
        end.y = y;
        updateBounds();
    }
    
    protected void paint( PPaintContext paintContext ) {
        Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke( stroke );
        if (input != null && input.getSbNode().getSoundbus().isOpen()) {
            g2.setPaint( Color.RED );
        } else {
            g2.setPaint( getPaint() );
        }
        g2.draw(getAnglePath());
    }

    protected void updateBounds() {
        GeneralPath path = getAnglePath();
        setPathTo( path );
        Rectangle2D b = stroke.createStrokedShape( path ).getBounds2D();
        super.setBounds(b.getX(), b.getY(), b.getWidth(), b.getHeight());
    }
    
    public GeneralPath getAnglePath() {
        GeneralPath p = new GeneralPath();
        Point2D.Double pointOne = null;
        Point2D.Double pointTwo = null;
        if (start == null) {
            pointOne = end;
            pointTwo = end;
        } else {
            pointOne = start;
            pointTwo = end;
        }
        if (pointOne != null) {
            p.moveTo((float)pointOne.getX(), (float)pointOne.getY());
            if (pointTwo != null) {
                p.lineTo((float)pointTwo.getX(), (float)pointTwo.getY());
                
                double a = pointOne.getY() - pointTwo.getY();
                double b = pointOne.getX() - pointTwo.getX();
                double alpha;
                if (b != 0) {
                    alpha = Math.atan( a/b );
                } else {
                    alpha = Math.PI / 2.0;
                }
                double arrowLen = 10.0;
                double arrowAngle = Math.PI / 5.0;

                if (pointOne.getX() > pointTwo.getX() ||
                        (pointOne.getX() == pointTwo.getX() && pointOne.getY() >= pointTwo.getY())) {
                    arrowLen = -arrowLen;
                }
                
                // draw arrow
                double[] p1 = rotate( pointOne, arrowLen, alpha - arrowAngle );
                double[] p2 = rotate( pointOne, arrowLen, alpha + arrowAngle );
                p.moveTo( (float) p1[0], (float) p1[1] );
                p.lineTo((float)pointOne.getX(), (float)pointOne.getY());
                p.lineTo( (float) p2[0], (float) p2[1] );
            }
        }
        return p;
    }
    
    private double[] rotate( Point2D.Double p, double dist, double angle ) {
        // calculate point on line that shall be rotated around
        // pointOne by angle
        double diffX = Math.cos( angle ) * dist;
        double diffY = Math.sin( angle ) * dist;
        return new double[] { p.getX() + diffX, p.getY() + diffY };
    }

    public boolean setBounds( double x, double y, double width, double height ) {
        return false;
    }
}