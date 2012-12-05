/*
 * Created on 02.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

import java.awt.Window;

/**
 * @author jonas.reese
 */
public class VstWindow {
    public static final int STYLE_WITH_TITLE = 0;
    public static final int STYLE_WITHOUT_TITLE = 1;
    
    private String title;
    private int x;
    private int y;
    private int width;
    private int height;
    private int style;
    private Window parent;
    
    public VstWindow() {
        this( "", null, STYLE_WITH_TITLE, 0, 0, 0, 0 );
    }
    
    public VstWindow( String title, Window parent, int style, int x, int y, int width, int height ) {
        this.title = title;
        this.parent = parent;
        this.style = style;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Window getParent() {
        return parent;
    }
    public void setParent( Window parent ) {
        this.parent = parent;
    }
    public int getStyle() {
        return style;
    }
    public void setStyle( int style ) {
        this.style = style;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle( String title ) {
        this.title = title;
    }
}
