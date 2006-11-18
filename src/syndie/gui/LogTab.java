package syndie.gui;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import syndie.db.DBClient;
import syndie.db.Opts;
import syndie.db.TextEngine;
import syndie.db.UI;
import syndie.data.SyndieURI;

/**
 *
 */
class LogTab extends BrowserTab implements Browser.UIListener {
    private StyledText _out;
    private Text _in;
    private Button _levelError;
    private Button _levelStatus;
    private Button _levelDebug;
    private boolean _error;
    private boolean _status;
    private boolean _debug;
    private boolean _closed;
    
    private int _sizeModifier;
    
    private List _pendingMessages = new ArrayList();
    
    private static final boolean STYLE_LOGS = false; // doesn't work with font resizing
    
    public LogTab(BrowserControl browser, SyndieURI uri) {
        super(browser, uri);
        _sizeModifier = 0;
        Thread t = new Thread(new Runnable() {
            public void run() {
                List records = new ArrayList();
                while (!_closed) {
                    synchronized (_pendingMessages) {
                        if (_pendingMessages.size() > 0) {
                            records.addAll(_pendingMessages);
                            _pendingMessages.clear();
                        } else {
                            try {
                                _pendingMessages.wait();
                            } catch (InterruptedException ie) {
                                if (_pendingMessages.size() > 0) {
                                    records.addAll(_pendingMessages);
                                    _pendingMessages.clear();
                                }
                            }
                        }
                    }
                    if (records.size() > 0) {
                        append(records);
                    }
                }
            }
        }, "LogTabRenderer");
        t.setDaemon(true);
        t.start();
    }
    
    protected void initComponents() {
        getRoot().setLayout(new GridLayout(1, true));
        
        _out = new StyledText(getRoot(), SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
        _out.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        _out.setFont(new Font(_out.getDisplay(), "Courier", 12, SWT.NONE));
        
        _out.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent evt) { }
            public void keyPressed(KeyEvent evt) {
                switch (evt.character) {
                    case '=': // ^=
                    case '+': // ^+
                        if ( (evt.stateMask & SWT.MOD1) != 0) {
                            _sizeModifier += 2;
                            rerender();
                        }
                        break;
                    case '_': // ^_
                    case '-': // ^-
                        if ( (evt.stateMask & SWT.MOD1) != 0) {
                            _sizeModifier -= 2;
                            rerender();
                        }
                        break;
                }
            }
        });
        
        Group levels = new Group(getRoot(), SWT.NONE);
        levels.setText("Log levels");
        levels.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        levels.setLayout(new FillLayout(SWT.HORIZONTAL));
        _levelError = new Button(levels, SWT.CHECK);
        _levelError.setText("errors");
        _levelError.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _error = _levelError.getSelection(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _error = _levelError.getSelection(); }
        });
        _levelStatus = new Button(levels, SWT.CHECK);
        _levelStatus.setText("status");
        _levelStatus.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _status = _levelStatus.getSelection(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _status = _levelStatus.getSelection(); }
        });
        _levelDebug = new Button(levels, SWT.CHECK);
        _levelDebug.setText("debug");
        _levelDebug.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { _debug = _levelDebug.getSelection(); }
            public void widgetSelected(SelectionEvent selectionEvent) { _debug = _levelDebug.getSelection(); }
        });
        
        _debug = false;
        _error = true;
        _status = true;
        _levelError.setSelection(_error);
        _levelStatus.setSelection(_status);
        _levelDebug.setSelection(_debug);
        
        getBrowser().addUIListener(this);
    }

    protected void disposeDetails() { 
        getBrowser().removeUIListener(this);
        _closed = true; 
        synchronized (_pendingMessages) { 
            _pendingMessages.notify(); 
        }
    }
    
    private static final SimpleDateFormat _fmt = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
    private static final String ts(long when) { 
        synchronized (_fmt) {
            return _fmt.format(new Date(when));
        }
    }
    
    private void rerender() {
        _out.setRedraw(false);
        Font f = _out.getFont();
        FontData fd[] = f.getFontData();
        FontData nfd[] = new FontData[fd.length];
        for (int j = 0; j < fd.length; j++) {
            nfd[j] = new FontData(fd[j].name, Math.max(6, fd[j].height+_sizeModifier), fd[j].style);
        }
        Font old = f;
        f = new Font(_out.getDisplay(), nfd);
        StyleRange ranges[] = _out.getStyleRanges();
        StyleRange nrange[] = new StyleRange[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            StyleRange r = (StyleRange)ranges[i].clone();
            r.font = f;
        }
        _out.setStyleRanges(nrange);
        //System.out.println("new font: " + f + " valid? " + !f.isDisposed());
        // todo: problem with disposing wrt styledtext's ranges
        //old.dispose();
        _out.setFont(f);
        
        _sizeModifier = 0;
        _out.setRedraw(true);
        _out.redraw();
    }
    
    private static int MAX_LINES = 100;
    
    private Color _tsBGColor = ColorUtil.getColor("gray", null);
    private Color _tsFGColor = ColorUtil.getColor("black", null);
    private Color _statusColor = ColorUtil.getColor("yellow", null);
    private Color _debugColor = ColorUtil.getColor("cyan", null);
    private Color _errorColor = ColorUtil.getColor("red", null);
    
    private void append(int type, String msg) { append(type, msg, null); }
    private void append(final int type, final String msg, final Exception e) {
        if ( (DEBUG == type) && (!_debug) ) return;
        if ( (STATUS == type) && (!_status) ) return;
        if ( (ERROR == type) && (!_error) ) return;
        synchronized (_pendingMessages) {
            _pendingMessages.add(new Record(type, msg, e));
            _pendingMessages.notify();
        }
    }
    
    /** called by the log thread */
    private void append(final List records) {
        // maybe stylize STATUS/DEBUG/ERROR w/ colors in the out buffer?
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if ( (_out == null) || (_out.isDisposed()) ) return;
                _out.setRedraw(false);
                int overallStart = _out.getCharCount();
                
                while (records.size() > 0) {
                    Record r = (Record)records.remove(0);
                    int start = _out.getCharCount();
                    int end = -1;
                    if (r.msg != null) {
                        _out.append(ts(r.when) + ":");
                        if (STYLE_LOGS) {
                            end = _out.getCharCount();
                            StyleRange range = new StyleRange(start, end-start, _tsFGColor, _tsBGColor);
                            _out.setStyleRange(range);
                            start = end;
                        }
                        _out.append(" " + r.msg + "\n");
                        end = _out.getCharCount();
                    }
                    if (r.e != null) {
                        StringWriter out = new StringWriter();
                        r.e.printStackTrace(new PrintWriter(out));
                        start = _out.getCharCount();
                        _out.append(ts(r.when));
                        if (STYLE_LOGS) {
                            end = _out.getCharCount();
                            _out.setStyleRange(new StyleRange(start, end-start, _tsFGColor, _tsBGColor));
                            start = end;
                        }
                        _out.append("\n" + out.getBuffer().toString() + "\n");
                    }
                    end = _out.getCharCount();
                    if (end > overallStart) {
                        if (STYLE_LOGS) {
                            int startLine = _out.getLineAtOffset(overallStart);
                            int curLine = _out.getLineCount()-1;
                            if (r.type == STATUS)
                                _out.setLineBackground(startLine, curLine-startLine, _statusColor);
                            else if (r.type == DEBUG)
                                _out.setLineBackground(startLine, curLine-startLine, _debugColor);
                            else
                                _out.setLineBackground(startLine, curLine-startLine, _errorColor);                    
                        }
                    }
                }

                int lines = _out.getLineCount();
                if (lines > MAX_LINES) {
                    int off = _out.getOffsetAtLine(lines-MAX_LINES);
                    _out.replaceTextRange(0, off, "");
                }

                // scroll to the end
                if (_out.getLineCount() > 0)
                    _out.setTopIndex(_out.getLineCount()-1);
                
                _out.setRedraw(true);
            }
        });
    }
    
    private static final int DEBUG = 1;
    private static final int STATUS = 2;
    private static final int ERROR = 3;

    public void errorMessage(String msg) { append(ERROR, msg); }
    public void errorMessage(String msg, Exception cause) { append(ERROR, msg, cause); }
    public void statusMessage(String msg) { append(STATUS, msg); }
    public void debugMessage(String msg) { append(DEBUG, msg); }
    public void debugMessage(String msg, Exception cause) { append(DEBUG, msg, cause); }
    public void commandComplete(final int status, final List location) {}
    
    public Image getIcon() { return ImageUtil.ICON_TAB_LOGS; }
    public String getName() { return "Logs"; }
    public String getDescription() { return "Log messages"; }
    
    private static class Record {
        long when;
        int type;
        String msg;
        Exception e;
        public Record(int stat, String newMsg) { this(stat, newMsg, null); }
        public Record(int stat, String newMsg, Exception cause) { when = System.currentTimeMillis(); type = stat; msg = newMsg; e = cause; }
    }
}
