/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 23.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionElementEvent;
import com.jonasreese.sound.sg.SessionElementListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.StaticActionPool;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;
import com.jonasreese.util.Updatable;
import com.jonasreese.util.resource.ResourceLoader;
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class FilesView implements View, Icon {
    private ResourceBundle rb;
    private static final Icon ICON =
        new ResourceLoader( FilesView.class, "/resource/files.gif" ).getAsIcon();

    private ViewConfiguratorImpl viewConfigurator;

	/**
     * Default constructor as invoked by the plugin subsystem.
     */
    public FilesView() {
        rb = SgEngine.getInstance().getResourceBundle();
    }
    
    /**
     * Since this <code>View</code> relies on session data
     * only, this method returns always <code>true</code>.
     * @return <code>true</code>.
     */
	public boolean canHandle( SessionElementDescriptor any ) { return true; }

	public ViewInstance createViewInstance( Session session, SessionElementDescriptor descriptor ) {
        return new ViewInstanceImpl( session );
	}

	public String getName() {
		return rb.getString( "plugin.filesView.name" );
	}

	public String getShortDescription() {
        return rb.getString( "plugin.filesView.shortDescription" );
	}

	public String getDescription() {
		return rb.getString( "plugin.filesView.description" );
	}

    public String getPluginName() {
        return "SoundsGood (c) File Manager Plugin";
    }
    
    public String getPluginVersion() {
        return "1.0";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public boolean isAutostartView() { return true; }

    public boolean isMultipleInstancePerSessionAllowed() { return false; }
    public boolean isMultipleInstancePerSessionElementAllowed() { return false; }

    public void init() {}
    public void exit() {}

    class ViewInstanceImpl implements ViewInstance, SessionElementListener, PropertyChangeListener {
        FilePanel fp;
        Session session;
        ViewInstanceImpl( Session session ) { this.session = session; }
        public Object getUiObject( ViewContainer parentUiObject ) {
            if (session != null) {
                fp = new FilePanel( session );
                session.addSessionElementListener( fp );
                return fp;
            } else {
                return null;
            }
        }
        
        public void activate() {
            if (fp != null) {
                fp.removeAction.setActionReceiver( new Updatable() {
                    public void update( Object o ) {
                        SessionElementDescriptor[] selectedObjects = fp.session.getSelectedElements();
                        if (selectedObjects == null || selectedObjects.length == 0) {
                            return;
                        }
                        HashSet<String> openViews = new HashSet<String>();
                        
                        for (int i = 0; i < selectedObjects.length; i++) {
                            SessionElementDescriptor sessionElement =
                                (SessionElementDescriptor) selectedObjects[i];
                            ViewInstance[] vis = sessionElement.getRegisteredViewInstances();
                            if (vis == null || vis.length == 0) {
                                
                            } else {
                                for (int k = 0; k < vis.length; k++) {
                                    openViews.add( vis[k].getView().getName() );
                                }
                            }
                        }
                        if (openViews.isEmpty()) {
                            int option = JOptionPane.showConfirmDialog(
                                    UiToolkit.getMainFrame(),
                                    rb.getString( selectedObjects.length > 1 ?
                                        "plugin.filesView.deleteMore.confirm.text" :
                                        "plugin.filesView.delete.confirm.text" ),
                                    rb.getString( "plugin.filesView.delete.confirm" ),
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE, null );
                                
                            if (option == JOptionPane.YES_OPTION) {
                                for (int i = 0; i < selectedObjects.length; i++) {
                                    fp.session.removeElement( selectedObjects[i] );
                                }
                            }
                        } else {
                            StringBuffer views = new StringBuffer();
                            for (String s : openViews) {
                                views.append( "\n" );
                                views.append( s );
                            }
                            JOptionPane.showMessageDialog(
                                    UiToolkit.getMainFrame(),
                                    rb.getString(
                                            "plugin.filesView.error.followingViewsRegisteredOnElement" )
                                            + views,
                                    rb.getString( "plugin.filesView.error.cannotDelete" ),
                                    JOptionPane.ERROR_MESSAGE );
                        }
                    }
                } );
                fp.removeAction.setEnabled( !fp.list.getSelectionModel().isSelectionEmpty() );
                fp.selectNoneAction.setActionReceiver( new Updatable() {
                    public void update( Object o ) {
                        fp.list.clearSelection();
                    }
                } );
                fp.selectNoneAction.setEnabled( !fp.list.getSelectionModel().isSelectionEmpty() );
                fp.selectAllAction.setActionReceiver( new Updatable() {
                    public void update( Object o ) {
                        fp.list.selectAll();
                    }
                } );
                fp.propertiesAction.setActionReceiver( new Updatable() {
    				public void update( Object o ) {
    					Object[] selObjs = fp.session.getSelectedElements();
                        showPropertiesDialog( selObjs, fp );
    				}
                } );
                fp.propertiesAction.setEnabled( !fp.list.getSelectionModel().isSelectionEmpty() );
            }
        }
        
        public void deactivate() {}

		/* (non-Javadoc)
		 * @see com.jonasreese.sound.sg.ViewInstance#getView()
		 */
		public View getView() {
			return FilesView.this;
		}

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.ViewInstance#open()
         */
        public void open() {
            if (session != null) {
                session.addSessionElementListener( this );
                SessionElementDescriptor[] sed = session.getAllElements();
                for (int i = 0; i < sed.length; i++) {
                    sed[i].addPropertyChangeListener( this );
                }
            }
            fp.opened();
        }

        public void close() {
            if (session != null) {
                session.removeSessionElementListener( this );
                SessionElementDescriptor[] sed = session.getAllElements();
                for (int i = 0; i < sed.length; i++) {
                    sed[i].removePropertyChangeListener( this );
                }
                session.removeSessionElementListener( fp );
            }
            fp.closed();
            //System.out.println( "removed all listeners" );
        }

        public void elementAdded( SessionElementEvent e ) {
            e.getSessionElement().addPropertyChangeListener( this );
        }

        public void elementRemoved( SessionElementEvent e ) {
            e.getSessionElement().removePropertyChangeListener( this );
        }

        public void propertyChange( PropertyChangeEvent e ) {
            if ("changed".equals( e.getPropertyName() ) && fp != null) {
                fp.list.updateUI();
            }
        }
		/* (non-Javadoc)
		 * @see com.jonasreese.sound.sg.plugin.view.ViewInstance#isSetBoundsAllowed()
		 */
		public boolean isSetBoundsAllowed() {
			return true;
		}
        
        public void finalize() throws Throwable {
            System.out.println( "finalizing FilesViewInstance" );
            super.finalize();
        }
    }
    
    private static Icon getIconForDesc( SessionElementDescriptor d ) {
        return new ImageIcon( d.getType().getSmallIcon() );
    }
    
    class CustomTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1;
        CustomTableModel( Object[] identifiers, int n ) {
            super( identifiers, n );
        }
        public void setValueAt( Object aValue, int row, int column ) {
            if (aValue instanceof Status) {
                ((SessionElementDescriptor) getValueAt( row, 1 )).setStatus(
                    ((Status) aValue).getStatus() );
            }
            super.setValueAt( aValue, row, column );
        }
    }
    
    class CustomCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1;
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean selected, boolean focused, int row, int col ) {
            JLabel label;
            if (value instanceof Icon) {
                label = new JLabel( (Icon) value );
            } else if (value instanceof JComponent) {
                return (JComponent) value;
            } else if (value instanceof Status) {
                label = ((Status) value).getLabel();
            } else {
                String s = "";
                if (value instanceof SessionElementDescriptor) {
                    s = value.toString();
                    if (((SessionElementDescriptor) value).isChanged()) {
                        s += "*";
                    }
                } else if (value != null) {
                    s = value.toString();
                    if (s == null) { s = ""; }
                    s = s.trim();
                    int index = s.indexOf( '\n' );
                    if (index >= 0) { s = s.substring( 0, index ) + "..."; }
                }
                label = new JLabel( s );
            }
            JPanel panel = new JPanel( new FlowLayout( 10 ) );
            panel.add( label );
            if (selected && value instanceof SessionElementDescriptor) {
                label.setForeground( table.getSelectionForeground() );
                panel.setBackground( table.getSelectionBackground() );
            }
            
            if (table.getRowHeight() < panel.getPreferredSize().height) {
                table.setRowHeight( panel.getPreferredSize().height );
            }
            return panel;
        }
    }
    
    class FilePanel extends JPanel
        implements SessionElementListener, ObjectSelectionChangeListener, PropertyChangeListener {

        private static final long serialVersionUID = 1;
        SgAction removeAction;
        SgAction selectNoneAction;
        SgAction selectAllAction;
        SgAction propertiesAction;
        
        FileListSelectionListener listSelectionListener;

        FileList list;
        Session session;
        CustomTableModel tm;
        Object[] identifiers = {
            " ",
            rb.getString( "plugin.filesView.tableFileName" ),
            rb.getString( "plugin.filesView.tableStatus" ),
            rb.getString( "plugin.filesView.tableNotes" ) };


        FilePanel( Session session ) {
            super( new BorderLayout() );
            this.session = session;
            tm = new CustomTableModel( identifiers, 0 );
            list = new FileList( tm );
            
            // execute default view plugin on mouse doubleclick
            list.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                    if (e.getClickCount() == 2) {
                        Object[] elements = FilePanel.this.session.getSelectedElements();
                        if (elements == null || elements.length != 1) {
                            return;
                        }
                        View view = (View) SgEngine.getInstance().getDefaultPlugin(
                                View.class, (SessionElementDescriptor) elements[0] );
                        System.out.println( "default view plugin: " + view );
                        if (view != null) {
                            // search for correct ViewAction
                            SessionUi sessionUi = UiToolkit.getSessionUi( FilePanel.this.session );
                            ViewAction[] viewActions = sessionUi.getActionPool().getViewActions();
                            for (int i = 0; i < viewActions.length; i++) {
                                if (viewActions[i].getView() == view) {
                                    viewActions[i].actionPerformed();
                                    break;
                                }
                            }
                        }
                    }
                }
            } );
            //list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
            addData();
            list.setDefaultRenderer( Object.class, new CustomCellRenderer() );
            list.setDefaultEditor( Object.class, null );
            TableColumn tc = list.getColumn( identifiers[0] );
            tc.setMaxWidth( 28 );
            tc.setMinWidth( 28);
            JScrollPane sp = new JScrollPane( list );
            
            SessionUi sessionUi = UiToolkit.getSessionUi( session );
            SessionActionPool sessionActionPool = sessionUi.getActionPool();
            System.out.println( "sessionActionPool = " + sessionActionPool.hashCode() );
            JToolBar toolbar = new JToolBar( rb.getString( "plugin.filesView.toolbar.title" ) );
            toolbar.putClientProperty( Options.HEADER_STYLE_KEY, HeaderStyle.BOTH );
            toolbar.setRollover( true );
            SessionActionPool actionPool = UiToolkit.getSessionUi( session ).getActionPool();
            removeAction = actionPool.getAction( SessionActionPool.DELETE );
            propertiesAction = sessionActionPool.getAction( SessionActionPool.PROPERTIES );
            selectNoneAction = actionPool.getAction( SessionActionPool.SELECT_NONE );
            selectAllAction = actionPool.getAction( SessionActionPool.SELECT_ALL );
            AbstractButton removeButton = UiToolkit.createToolbarButton( removeAction );
            
            JPopupMenu contextMenu = new JPopupMenu();
            JMenu openWithMenu = new JMenu( rb.getString( "plugin.filesView.contextMenu.openWith" ) );
            openWithMenu.setIcon( UiToolkit.SPACER );
            // search default plugin for 'open' item
            Object[] elements = FilePanel.this.session.getSelectedElements();
            Plugin plugin = null;
            if (elements != null && elements.length == 1) {
                plugin = SgEngine.getInstance().getDefaultPlugin(
                        View.class, (SessionElementDescriptor) elements[0] );
            }
            ViewAction[] viewActions = sessionActionPool.getViewActions();
            for (int i = 0; i < viewActions.length; i++) {
                if (viewActions[i].getView() == plugin) {
                    contextMenu.add( viewActions[i] );
                    openWithMenu.add( viewActions[i] );
                    openWithMenu.addSeparator();
                    break;
                }
            }
            for (int i = 0; i < viewActions.length; i++) {
                if (viewActions[i].getView() != plugin &&
                    viewActions[i].getView().isMultipleInstancePerSessionAllowed()) {
                    openWithMenu.add( viewActions[i] );
                }
            }
            contextMenu.add( openWithMenu );
            contextMenu.addSeparator();
            contextMenu.add(
                UiToolkit.getActionPool().getAction( StaticActionPool.INSERT_FILE_INTO_SESSION ) );
            contextMenu.add( removeAction );
            contextMenu.addSeparator();
            contextMenu.add( propertiesAction );
            DefaultPopupListener popupListener = new DefaultPopupListener( contextMenu );
            list.addMouseListener( popupListener );            

            tc = list.getColumn( identifiers[2] );
            JComboBox comboBox = new JComboBox(
                new Object[] { new Status( 0 ), new Status( 1 ), new Status( 2 ), new Status( 3 ) } );
            tc.setPreferredWidth( comboBox.getPreferredSize().width + 10 );
            tc.setMaxWidth( comboBox.getPreferredSize().width + 10 );
            tc.setCellEditor( new DefaultCellEditor( comboBox ) );

            tc = list.getColumn( identifiers[3] );
            tc.setPreferredWidth( comboBox.getPreferredSize().width + 10 );
            tc.setMaxWidth( comboBox.getPreferredSize().width + 10 );
            tc.setCellEditor( new TableCellEditor() {
                JTextArea textArea = new JTextArea( 15, 5 );
                JScrollPane sp = new JScrollPane( textArea );
                public Object getCellEditorValue() {
                    return textArea.getText();
                }
                public Component getTableCellEditorComponent( JTable t, Object v, boolean s, int c, int r ) {
                    textArea.setText( (v != null ? v.toString() : "") );
                    textArea.setLineWrap( true );
                    textArea.setWrapStyleWord( true );
                    Object[] message = new Object[] { sp, };
                    int option = JOptionPane.showConfirmDialog(
                        getParent(), message,
                        rb.getString( "plugin.filesView.notes.title" ),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE, null );
                    if (option == JOptionPane.OK_OPTION) {
                        if (v instanceof Description) {
                            list.col = -2;
                            ((Description) v).desc.setDescriptionText(
                                textArea.getText() );
                            t.setValueAt( v, c, r );
                        }
                    }
                    
                    return null;
                }
                public void cancelCellEditing() {}
                public boolean stopCellEditing() { return true; }
                public boolean isCellEditable( EventObject e ) { return true; }
                public boolean shouldSelectCell( EventObject e ) { return true; }
                public void addCellEditorListener( CellEditorListener e ) {}
                public void removeCellEditorListener( CellEditorListener e ) {}
            } );

            // create some toolbar buttons
            AbstractButton addButton = UiToolkit.createToolbarButton(
                UiToolkit.getActionPool().getAction( StaticActionPool.INSERT_FILE_INTO_SESSION ) );
            AbstractButton propertiesButton = UiToolkit.createToolbarButton( propertiesAction );

            toolbar.add( addButton );
            toolbar.add( removeButton );
            toolbar.add( propertiesButton );
            add( toolbar, BorderLayout.NORTH );
            add( sp );
            Object[] objs = session.getSelectedElements();
            if (objs != null) {
                for (int i = 0; i < objs.length; i++) {
                    int idx = findIndex( objs[i] );
                    if (idx >= 0) {
                        if (i == 0) {
                            ((ListSelectionModel) list.getSelectionModel()).setSelectionMode(
                                ListSelectionModel.SINGLE_SELECTION );
                            ((ListSelectionModel) list.getSelectionModel()).setLeadSelectionIndex( idx );
                            ((ListSelectionModel) list.getSelectionModel()).setSelectionMode(
                                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
                        } else {
                            ((ListSelectionModel) list.getSelectionModel()).addSelectionInterval( idx, idx );
                        }
                    }
                }
            }

            listSelectionListener = new FileListSelectionListener( this );
            list.getSelectionModel().addListSelectionListener( listSelectionListener );
            listSelectionListener.valueChanged( null );
        }

        void opened() {
            SgEngine.getInstance().getProperties().addPropertyChangeListener(
                SgProperties.PLUGIN_KEY_PREFIX + FilesView.class.getName() + ".toolTipsEnabled", this );
            list.ttEnabled = SgEngine.getInstance().getProperties().getPluginProperty(
                FilesView.this, "toolTipsEnabled", true );
            ToolTipManager.sharedInstance().registerComponent( list );
            FilesView.FilePanel.this.session.addObjectSelectionChangeListener(
                FilesView.FilePanel.this );

            // restore table column widths
            try {
                SwingUtilities.invokeAndWait( new Runnable() {
                    public void run() {
                        for (Object identifier : identifiers) {
                            TableColumn column = list.getColumn( identifier );
                            if (column.getResizable()) {
                                String s = session.getPersistentClientProperty( "filesView.columm_" + identifier + "_width" );
                                if (s != null) {
                                    int width = 0;
                                    try {
                                        width = Integer.parseInt( s );
                                    } catch (NumberFormatException ignored) {
                                    }
                                    if (width > 0) {
                                        System.out.println( "setting column " + identifier + " to " + width );
                                        column.setPreferredWidth( width );
                                    }
                                }
                            }
                        }
                    }
                } );
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }
            
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    list.getColumnModel().addColumnModelListener( new TableColumnModelListener() {
                        public void columnAdded(TableColumnModelEvent e) {
                        }
                        public void columnMarginChanged(ChangeEvent e) {
                            // persist table column widths
                            System.out.println( "FilesView: persisting table column widths" );
                            for (Object identifier : identifiers) {
                                TableColumn column = list.getColumn( identifier );
                                if (column.getResizable()) {
                                    FilePanel.this.session.putPersistentClientProperty(
                                            "filesView.columm_" + identifier + "_width", Integer.toString( column.getWidth() ) );
                                }
                            }
                        }
                        public void columnMoved(TableColumnModelEvent e) {
                        }
                        public void columnRemoved(TableColumnModelEvent e) {
                        }
                        public void columnSelectionChanged(ListSelectionEvent e) {
                        }
                    } );
                }
            } );
        }
        
        void closed() {
            SgEngine.getInstance().getProperties().removePropertyChangeListener( this );
            ToolTipManager.sharedInstance().unregisterComponent( list );
            FilesView.FilePanel.this.session.removeObjectSelectionChangeListener(
                FilesView.FilePanel.this );
            super.removeNotify();
        }

        public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
            String s = null;
            SessionElementDescriptor[] selObjects = session.getSelectedElements();
            if (selObjects != null && selObjects.length > 0) {
                s = "" + selObjects[0];
            }
            System.out.println( "objectSelectionChanged() + " + s );
            DefaultListSelectionModel lm = (DefaultListSelectionModel) list.getSelectionModel();
            
            synchronized (listSelectionListener) {
                lm.removeListSelectionListener( listSelectionListener );
                
                HashSet<Integer> indices = new HashSet<Integer>();
                if (selObjects != null) {
                    for (int i = 0; i < selObjects.length; i++) {
                        int idx = findIndex( selObjects[i] );
                        if (idx >= 0) {
                            indices.add( idx );
                        }
                    }
                    list.clearSelection();
                    for (int i = 0; i < list.getModel().getRowCount(); i++) {
                        if (indices.contains( i )) {
                            lm.addSelectionInterval( i, i );
                        }
                    }
                    lm.setValueIsAdjusting( false );
                }
                
                listSelectionListener.updateActions();
            
                lm.addListSelectionListener( listSelectionListener );
            }
        }
        
        int findIndex( Object selObject ) {
            if (selObject == null) {
                return -1;
            }
            for (int i = 0; i < tm.getRowCount(); i++) {
                SessionElementDescriptor e = (SessionElementDescriptor) tm.getValueAt( i, 1 );
                if (e.equals( selObject )) {
                    return i;
                }
            }
            return -1;
        }
        
        synchronized void addData() {
            SessionElementDescriptor[] descs = session.getAllElements();
            for (int i = 0; i < descs.length; i++) {
                tm.addRow(
                    new Object[] {
                        getIconForDesc( descs[i] ),
                        descs[i],
                        new Status( descs[i] ),
                        new Description( descs[i] ) } );
            }
        }
        
		public synchronized void elementAdded( SessionElementEvent e ) {
            //System.out.println( "fileAdded()" );
            SessionElementDescriptor d =
                (SessionElementDescriptor) e.getSessionElement();
            tm.addRow(
                new Object[] {
                    getIconForDesc( d ),
                    d,
                    new Status( d ),
                    new Description( d ) } );
		}

		public synchronized void elementRemoved( SessionElementEvent e ) {
            //System.out.println( "elementRemoved()" );
            for (int i = 0; i < tm.getRowCount(); i++) {
                if (tm.getValueAt( i, 1 ).equals( e.getSessionElement() )) {
                    tm.removeRow( i );
                    return;
                }
            }
		}

        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange( PropertyChangeEvent e ) {
            System.out.println( "FilesView: propertyChange : " + e.getPropertyName() );
            list.ttEnabled = ((Boolean) e.getNewValue()).booleanValue();
        }
    }
    
    class FileListSelectionListener implements ListSelectionListener {
        
        FilePanel fp;
        
        FileListSelectionListener( FilePanel fp ) {
            this.fp = fp;
        }
        
        void updateActions() {
            boolean b = !(fp.session.getSelectedElements() == null ||
                    fp.session.getSelectedElements().length == 0);
            fp.removeAction.setEnabled( b );
            fp.selectNoneAction.setEnabled( b );
            fp.propertiesAction.setEnabled( b );
        }
        
        public synchronized void valueChanged( ListSelectionEvent e ) {
            synchronized (fp.list) {
                int[] indices = fp.list.getSelectedRows();
                SessionElementDescriptor[] objects = new SessionElementDescriptor[indices.length];
                for (int i = 0; i < indices.length; i++) {
                    objects[i] = (SessionElementDescriptor) fp.tm.getValueAt( indices[i], 1 );
                }
                fp.session.setSelectedElements( objects, FilesView.this );
                updateActions();
            }
        }
    }
    
    static final Color[] STATUS_COLORS = new Color[] {
        Color.white, Color.yellow, Color.green, Color.red
    };
    class Description {
        SessionElementDescriptor desc;
        Description( SessionElementDescriptor desc ) {
            this.desc = desc;
        }
        public String toString() { return desc.getDescriptionText(); }
    }
    class Status {
        SessionElementDescriptor desc;
        int id;
        Status( SessionElementDescriptor desc ) {
            this.desc = desc;
        }
        Status( int id ) { this.id = id; }
        public String toString() {
            if (desc != null) {
                id = desc.getStatus();
            }
            switch (id) {
                case SessionElementDescriptor.STATUS_UNMODIFIED:
                    return rb.getString( "plugin.filesView.status.unmodified" );
                case SessionElementDescriptor.STATUS_IN_MODIFICATION:
                    return rb.getString( "plugin.filesView.status.inModification" );
                case SessionElementDescriptor.STATUS_DONE:
                    return rb.getString( "plugin.filesView.status.done" );
                default:
                    return rb.getString( "plugin.filesView.status.unknown" );
            }
        }
        JLabel getLabel() {
            JLabel label = new JLabel( toString() );
            FontMetrics fm = label.getFontMetrics( label.getFont() );
            if (fm != null) {
                Icon icon = new LabelIcon( fm.getHeight(), ((desc == null) ? id : desc.getStatus()) );
                label.setIcon( icon );
            }
            return label;
        }
        public boolean equals( Status another ) {
            return (another != null && another.desc.getStatus() == desc.getStatus());
        }
        int getStatus() { return ((desc == null) ? id : desc.getStatus()); }
    }
    class LabelIcon implements Icon {
        int px;
        int id;
        LabelIcon( int px, int id ) { this.px = px; this.id = id; }
        public int getIconHeight() {
            return px;
        }
        public int getIconWidth() {
            return px;
        }
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            g.setColor( STATUS_COLORS[id] );
            g.fillOval( x, y, px, px );
        }
    }

	/* (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon( Component arg0, Graphics arg1, int arg2, int arg3 ) {
        ICON.paintIcon( arg0, arg1, arg2, arg3 );
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth() {
		return ICON.getIconWidth();
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight() {
		return ICON.getIconHeight();
	}

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#createViewConfigurator()
     */
    public PluginConfigurator getPluginConfigurator() {
        if (viewConfigurator == null) {
            viewConfigurator = new ViewConfiguratorImpl();
        }
        return viewConfigurator;
    }
    
    /**
     * Shows a dialog that displays/edits the given session items.
     * @param elements The session elements, typically <code>SessionElementDescriptor</code>
     *        objects.
     * @param filePanel The <code>FilePanel</code> that is parent to the dialog to be shown.
     */
    public void showPropertiesDialog( Object[] elements, FilePanel filePanel ) {
        PropertiesPanel propertiesPanel = new PropertiesPanel( elements, filePanel );
        
        Object[] message = new Object[] {
            propertiesPanel,
        };
        int option = JOptionPane.showConfirmDialog(
        filePanel, message,
            rb.getString( "plugin.filesView.properties.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            propertiesPanel.applyChanges();
        }
    }
    
    class FileList extends JTable {
        private static final long serialVersionUID = 1;
        FileList( TableModel tm ) {
            super( tm );
        }
        
        boolean ttEnabled;
        MouseEvent e = null;
        String ttt;
        int col = -2;
        int row = -2;
        String filePrefix = rb.getString( "plugin.filesView.toolTip.filePrefix" );
        public void processMouseMotionEvent( MouseEvent e ) {
            this.e = e;
            super.processMouseMotionEvent( e );
        }
        public String getToolTipText() {
            if (e == null || !ttEnabled) {
                return null;
            }
            Point p = e.getPoint();
            int col = columnAtPoint( p );
            int row = rowAtPoint( p );
            if (col < 0 || row < 0) { return null; }
            if (col == this.col && row == this.row) { return ttt; }
            this.col = col;
            this.row = row;
            Object val = getValueAt( row, col );
            if (val instanceof Description) {
                String s = val.toString();
                if (s != null) { s = s.trim(); }
                if (s != null && !"".equals( s )) {
                    s = s.substring( 0, Math.min( 512, s.length() ) );
                    s = Session.getXmlEncoded( s.replaceAll( "\n", "<br>" ) );
                    ttt = "<html>" + s + "</html>";
                }
            } else if (val instanceof SessionElementDescriptor) {
                File f = ((SessionElementDescriptor) val).getFile();
                if (f != null) {
                    ttt = "<html><b>" + filePrefix + ":</b> " + f.getAbsolutePath();
                }
            } else {
                ttt = null;
            }
                    
            return ttt;
        }
    }    
    /**
     * This is the JPanel that contains element properties.
     */
    class PropertiesPanel extends JPanel
        implements ActionListener, DocumentListener, MouseListener {

        private static final long serialVersionUID = 1;
        Object[] elements;

        JLabel iconLabel;
        JTextField nameField;
        JTextField pathField;
        JButton pathButton;
        JTextArea textArea;
        boolean textChanged;
        FilePanel filePanel;
        
		/**
		 * @param elements
		 */
		PropertiesPanel( Object[] elements, FilePanel filePanel ) {
            super( new BorderLayout() );
            this.elements = elements;
            this.filePanel = filePanel;
            
            ArrayList<JComponent> t = new ArrayList<JComponent>();
            iconLabel = new JLabel();
            JLabel pathLabel = new JLabel( rb.getString( "plugin.filesView.properties.path" ) );
            t.add( pathLabel );
            nameField = new JTextField( 30 );
            nameField.setEditable( false );
            pathField = new JTextField( 30 );
            pathField.setEditable( false );
            pathLabel.setLabelFor( pathField );
            pathButton = new JButton( rb.getString( "plugin.filesView.properties.changePath" ) );
            pathButton.addActionListener( this );

            JPanel namePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            JLabel nameLabel = new JLabel( rb.getString( "plugin.filesView.properties.name" ) );
            t.add( nameLabel );
            namePanel.add( nameLabel );
            namePanel.add( nameField );
        
            JPanel pathPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            pathPanel.add( pathLabel );
            pathPanel.add( pathField );
            pathPanel.add( pathButton );
        
            JLabel typeLabel = new JLabel( rb.getString( "plugin.filesView.properties.type" ) );
            t.add( typeLabel );

            JPanel iconPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            iconPanel.add( typeLabel );
            iconPanel.add( iconLabel );

            textArea = new JTextArea( 5, 40 );
            textChanged = false;
//            textArea.setPreferredSize(
//                new Dimension(
//                    pathField.getPreferredSize().width, textArea.getPreferredSize().height ) );
//            textArea.setMinimumSize( textArea.getPreferredSize() );
            textArea.setLineWrap( true );
            textArea.setWrapStyleWord( true );
            JLabel textTitleLabel = new JLabel(
                rb.getString( "plugin.filesView.properties.description" ) );
            textTitleLabel.setLabelFor( textArea );
            t.add( textTitleLabel );
            JPanel textPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            textPanel.add( textTitleLabel );
            JScrollPane sp = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
            textPanel.add( sp );

            // set all title components to same preferred width
            int maxWidth = 0;
            for (int i = 0; i < t.size(); i++) {
                Dimension d = t.get( i ).getPreferredSize();
                if (d.width > maxWidth) {
                    maxWidth = d.width;
                }
            }
            for (int i = 0; i < t.size(); i++) {
                t.get( i ).setPreferredSize(
                    new Dimension( maxWidth,
                        t.get( i ).getPreferredSize().height ) );
            }
            
            JPanel p = new JPanel( new BorderLayout() );
            p.add( pathPanel, BorderLayout.NORTH );
            p.add( textPanel );

            add( iconPanel, BorderLayout.NORTH );
            add( namePanel );
            add( p, BorderLayout.SOUTH );
            
            updateChildComponents();
		}

        void change() {
            textChanged = true;
        }
        public void changedUpdate( DocumentEvent e ) {}
        public void insertUpdate( DocumentEvent e ) {
            System.out.println( "insertUpdate" );
            change();
        }
        public void removeUpdate( DocumentEvent e ) {
            System.out.println( "removeUpdate" );
            change();
        }
        
        void updateChildComponents() {
            textArea.removeMouseListener( this );
            if (elements.length == 1) {
                if (elements[0] instanceof SessionElementDescriptor) {
                    SessionElementDescriptor desc = (SessionElementDescriptor) elements[0];
                    iconLabel.setText( desc.getType().getName() );
                    Icon icon = getIconForDesc( (SessionElementDescriptor) elements[0] );
                    if (icon != null) {
                        iconLabel.setIcon( icon );
                    }
                    nameField.setText( desc.getName() );
                    File f = desc.getFile();
                    if (f == null) {
                        pathField.setText( rb.getString( "plugin.filesView.properties.noFile" ) );
                    } else {
                        pathField.setText( f.getAbsolutePath() );
                    }
                    textArea.getDocument().removeDocumentListener( this );
                    textArea.setText( desc.getDescriptionText() );
                    textArea.getDocument().addDocumentListener( this );
                }
            } else {
                iconLabel.setText( rb.getString( "plugin.filesView.properties.type.moreThanOne" ) );
                nameField.setText( rb.getString( "plugin.filesView.properties.name.moreThanOne" ) );
                pathField.setText( rb.getString( "plugin.filesView.properties.moreThanOnePath" ) );
                pathField.setEnabled( false );
                pathButton.setEnabled( false );
                textArea.setEnabled( false );
                textArea.setText(
                    rb.getString( "plugin.filesView.properties.description.clickToSetForAll" ) );
                textArea.addMouseListener( this );
            }
        }
        
        /**
         * Applies any changes that have been made.
         */
        public void applyChanges() {
            if (elements.length == 1 && elements[0] instanceof SessionElementDescriptor) {
                if (textChanged) {
                    ((SessionElementDescriptor) elements[0]).setDescriptionText( textArea.getText() );
                }
            } else {
                if (textArea.isEnabled()) {
                    for (int i = 0; i < elements.length; i++) {
                        if (elements[i] instanceof SessionElementDescriptor) {
                            ((SessionElementDescriptor) elements[i]).setDescriptionText(
                                textArea.getText() );
                        }
                    }
                }
            }
            if (filePanel != null) {
                filePanel.list.updateUI();
            }
        }
        
        public void actionPerformed( ActionEvent e ) {
            if (elements.length != 1 || !(elements[0] instanceof SessionElementDescriptor)) {
                return;
            }
            SessionElementDescriptor desc = (SessionElementDescriptor) elements[0];
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            // to change the file path, the descriptor shall not
            // be changed and no ViewInstances shall be registered
            // to it...
            String err = null;
            if (desc.isChanged()) {
                err = rb.getString(
                    "plugin.filesView.properties.changePath.descriptorChangedMessage" );
            } else if (desc.isViewInstanceRegistered()) {
                err = rb.getString(
                    "plugin.filesView.properties.changePath.descriptorOccupiedMessage" );
            }
            if (err != null) {
                JOptionPane.showMessageDialog(
                    ((Component) e.getSource()),
                    err,
                    rb.getString(
                        "plugin.filesView.properties.changePath.cannotChangePathError" ),
                    JOptionPane.ERROR_MESSAGE );
            } else {
                String s = SgEngine.getInstance().getProperties().getSessionDirectory();
                String directory = SgEngine.getInstance().getProperties().getPluginProperty(
                    FilesView.this, "elementDirectory", s );
                    
                JFileChooser fileChooser = new JFileChooser( directory );
                fileChooser.setDialogTitle( rb.getString(
                    "plugin.filesView.properties.changePath.openDialogTitle" ) );
                int result = fileChooser.showOpenDialog( (Component) e.getSource() );
                    
                // perform...
                if (result == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    SgEngine.getInstance().getProperties().setPluginProperty(
                        FilesView.this, "elementDirectory", f.getParent() );
                    if (!f.equals( desc.getFile() )) {
                        desc.resetData();
                        desc.setFile( f );
                        desc.getUndoManager().discardAllEdits();
                        desc.getSession().setChanged( true );
                    }
                    updateChildComponents();
                }
            }
        }

        public void mouseClicked( MouseEvent e ) {
            if (!textArea.isEnabled()) {
                textArea.setEnabled( true );
                textArea.setText( "" );
                textArea.requestFocus();
            }
		}
		public void mouseEntered( MouseEvent e) {}
		public void mouseExited( MouseEvent e ) {}
		public void mousePressed( MouseEvent e ) {}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent arg0) {
		}
     }
     
     
     class ViewConfiguratorImpl extends JTabbedPane implements PluginConfigurator {
         private static final long serialVersionUID = 1;
         JCheckBox toolTipsCheckBox;
         ViewConfiguratorImpl() {
             addTab( rb.getString( "plugin.filesView.configurator.view.title" ), createViewTab() );
         }
         
         JPanel createViewTab() {
             JPanel panel = new JPanel( new BorderLayout() );
             JPanel toolTipsPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
             toolTipsCheckBox = new JCheckBox(
                rb.getString( "plugin.filesView.configurator.toolTips.enable" ),
                SgEngine.getInstance().getProperties().getPluginProperty(
                    FilesView.this, "toolTipsEnabled", true ) );
             toolTipsPanel.add( toolTipsCheckBox );
             toolTipsPanel.setBorder( new TitledBorder(
                rb.getString( "plugin.filesView.configurator.toolTips.title" ) ) );
             panel.add( toolTipsPanel, BorderLayout.NORTH );
             return panel;
         }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#getTitle()
         */
        public String getTitle() {
            return rb.getString( "plugin.filesView.configurator.title" );
        }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#getUiObject()
         */
        public Object getUiObject() {
            return this;
        }

        public Plugin getPlugin() {
            return FilesView.this;
        }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#open()
         */
        public void open() {
        }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#ok()
         */
        public void ok() {
            SgEngine.getInstance().getProperties().setPluginProperty(
                FilesView.this, "toolTipsEnabled", toolTipsCheckBox.isSelected() );
        }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#cancel()
         */
        public void cancel() {
            toolTipsCheckBox.setSelected(
                SgEngine.getInstance().getProperties().getPluginProperty(
                    FilesView.this, "toolTipsEnabled", true ) );
        }
     }
}