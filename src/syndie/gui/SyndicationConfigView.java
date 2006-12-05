package syndie.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import syndie.db.ArchiveIndex;
import syndie.db.SyndicationManager;

/**
 *
 */
public class SyndicationConfigView implements Translatable {
    private BrowserControl _browser;
    private Composite _parent;
    private SyndicationView _view;
    private Composite _root;
    private Button _actionIndex;
    private Button _actionPullOnly;
    private Button _actionPushPull;
    private Button _actionPushOnly;
    private Label _pullStrategyLabel;
    private Combo _pullStrategy;
    private Label _pushStrategyLabel;
    private Combo _pushStrategy;
    private Label _sizeLabel;
    private Spinner _size;
    private Label _concurrencyLabel;
    private Spinner _concurrency;
    private Label _pullExplicitLabel;
    private Button _pullExplicitButton;
    private Label _proxyHostLabel;
    private Text _proxyHost;
    private Label _proxyPortLabel;
    private Text _proxyPort;
    private Label _fcpHostLabel;
    private Text _fcpHost;
    private Label _fcpPortLabel;
    private Text _fcpPort;
    
    public SyndicationConfigView(BrowserControl browser, Composite parent, SyndicationView view) {
        _browser = browser;
        _parent = parent;
        _view = view;
        initComponents();
    }
    
    public Control getControl() { return _root; }
    
    public void dispose() {
        _browser.getTranslationRegistry().unregister(this);
    }
    
    public int getProxyPort() { return (getProxyHost() != null ? getPort(_proxyPort) : -1); }
    public String getProxyHost() { return getHost(_proxyHost); }
    
    public int getFCPPort() { return (getFCPHost() != null ? getPort(_fcpPort) : -1); }
    public String getFCPHost() { return getHost(_fcpHost); }
    
    public static final int ACTION_INDEXES = 0;
    public static final int ACTION_PULL_PUSH = 1;
    public static final int ACTION_PULL_ONLY = 2;
    public static final int ACTION_PUSH_ONLY = 3;
    
    public int getAction() { 
        if (_actionIndex.getSelection())
            return ACTION_INDEXES;
        else if (_actionPullOnly.getSelection())
            return ACTION_PULL_ONLY;
        else if (_actionPushPull.getSelection())
            return ACTION_PULL_PUSH;
        else if (_actionPushOnly.getSelection())
            return ACTION_PUSH_ONLY;
        return -1;
    }
    public int getConcurrency() { return _concurrency.getSelection(); }
    public int getMaxKB() { return _size.getSelection(); }
    public int getPullStrategy() { return _pullStrategy.getSelectionIndex(); }
    public int getPushStrategy() { return _pushStrategy.getSelectionIndex(); }
    
    private static final int getPort(Text txt) {
        String str = txt.getText().trim();
        try {
            int i = Integer.parseInt(str);
            if (i > 0)
                return i;
        } catch (NumberFormatException nfe) {}
        return -1;
    }
    private static final String getHost(Text txt) {
        String str = txt.getText().trim();
        if (str.length() > 0) 
            return str;
        else
            return null;
    }
    
    private void initComponents() {
        _root = new Composite(_parent, SWT.BORDER);
        _root.setLayout(new GridLayout(4, false));
        
        Composite actionRow = new Composite(_root, SWT.NONE);
        actionRow.setLayout(new FillLayout(SWT.HORIZONTAL));
        actionRow.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 4, 1));
        _actionIndex = new Button(actionRow, SWT.RADIO);
        _actionPullOnly = new Button(actionRow, SWT.RADIO);
        _actionPushPull = new Button(actionRow, SWT.RADIO);
        _actionPushOnly = new Button(actionRow, SWT.RADIO);
    
        _pullStrategyLabel = new Label(_root, SWT.NONE);
        _pullStrategyLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _pullStrategy = new Combo(_root, SWT.DROP_DOWN | SWT.READ_ONLY);
        _pullStrategy.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 3, 1));
        
        _pushStrategyLabel = new Label(_root, SWT.NONE);
        _pushStrategyLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _pushStrategy = new Combo(_root, SWT.DROP_DOWN | SWT.READ_ONLY);
        _pushStrategy.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 3, 1));
        
        _sizeLabel = new Label(_root, SWT.NONE);
        _sizeLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _size = new Spinner(_root, SWT.BORDER);
        _size.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, true, false, 3, 1));
        _size.setDigits(0);
        _size.setIncrement(1);
        _size.setMinimum(4);
        _size.setMaximum(-1);
        _size.setSelection((int)(ArchiveIndex.DEFAULT_MAX_SIZE/1024));
        
        _concurrencyLabel = new Label(_root, SWT.NONE);
        _concurrencyLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _concurrency = new Spinner(_root, SWT.BORDER);
        _concurrency.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, true, false, 3, 1));
        _concurrency.setDigits(0);
        _concurrency.setIncrement(1);
        _concurrency.setMinimum(1);
        _concurrency.setMaximum(10);
        _concurrency.setSelection(1);
        
        _pullExplicitLabel = new Label(_root, SWT.NONE);
        _pullExplicitLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _pullExplicitButton = new Button(_root, SWT.NONE);
        _pullExplicitButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, true, false, 3, 1));
        _pullExplicitButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent selectionEvent) { pickExplicit(); _view.showDiff(); }
            public void widgetSelected(SelectionEvent selectionEvent) { pickExplicit(); _view.showDiff(); }
        });
        
        _proxyHostLabel = new Label(_root, SWT.NONE);
        _proxyHostLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _proxyHost = new Text(_root, SWT.SINGLE | SWT.BORDER);
        _proxyHost.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        _proxyPortLabel = new Label(_root, SWT.NONE);
        _proxyPortLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _proxyPort = new Text(_root, SWT.SINGLE | SWT.BORDER);
        _proxyPort.setTextLimit(5);
        _proxyPort.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        
        _fcpHostLabel = new Label(_root, SWT.NONE);
        _fcpHostLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _fcpHost = new Text(_root, SWT.SINGLE | SWT.BORDER);
        _fcpHost.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        _fcpPortLabel = new Label(_root, SWT.NONE);
        _fcpPortLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        _fcpPort = new Text(_root, SWT.SINGLE | SWT.BORDER);
        _fcpPort.setTextLimit(5);
        _fcpPort.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
    
        _actionIndex.setSelection(true);
        
        String host = _browser.getClient().getDefaultFreenetHost();
        if (host == null) host = "127.0.0.1";
        _fcpHost.setText(host);
        int port = _browser.getClient().getDefaultFreenetPort();
        if (port <= 0) port = 8481;
        _fcpPort.setText(port+"");
        
        port = _browser.getClient().getDefaultHTTPProxyPort();
        host = _browser.getClient().getDefaultHTTPProxyHost();
        if ( (port <= 0) || (host == null) || (host.length() <= 0) ) {
            _proxyPort.setText("");
            _proxyHost.setText("");
        } else {
            _proxyPort.setText(port+"");
            _proxyHost.setText(host);
        }
        
        _browser.getTranslationRegistry().register(this);
    }
    
    public void pickExplicit() {
        _pullStrategy.select(SyndicationManager.PULL_STRATEGY_EXPLICIT);
        if (_actionIndex.getSelection()) {
            _actionIndex.setSelection(false);
            _actionPullOnly.setSelection(true);
        }
    }
    
    private void updateStrategy(TranslationRegistry registry) {
        int idx = SyndicationManager.PULL_STRATEGY_DEFAULT;
        if (_pullStrategy.getItemCount() > 0)
            idx = _pullStrategy.getSelectionIndex();
        _pullStrategy.setRedraw(false);
        _pullStrategy.removeAll();
        // order tied to the SyndicationManager.STRATEGY_* values
        _pullStrategy.add(registry.getText(T_PULL_STRATEGY_DELTA, "All differences"));
        _pullStrategy.add(registry.getText(T_PULL_STRATEGY_DELTABOOKMARKED, "Differences in bookmarked forums"));
        _pullStrategy.add(registry.getText(T_PULL_STRATEGY_DELTAKNOWN, "Differences in locally known forums"));
        _pullStrategy.add(registry.getText(T_PULL_STRATEGY_EXPLICIT, "Explicitly selected messages and forums"));
        _pullStrategy.add(registry.getText(T_PULL_STRATEGY_PIR, "PIR (wastes bandwidth, more anonymity)"));
        _pullStrategy.select(idx);
        _pullStrategy.setRedraw(true);
        
        idx = SyndicationManager.PUSH_STRATEGY_DEFAULT;
        if (_pushStrategy.getItemCount() > 0)
            idx = _pushStrategy.getSelectionIndex();
        _pushStrategy.setRedraw(false);
        _pushStrategy.removeAll();
        // order tied to the SyndicationManager.STRATEGY_* values
        _pushStrategy.add(registry.getText(T_PUSH_STRATEGY_DELTA, "All differences"));
        _pushStrategy.add(registry.getText(T_PUSH_STRATEGY_DELTAKNOWN, "Differences in remotely known forums"));
        //_pullStrategy.add(registry.getText(T_STRATEGY_EXPLICIT, "Explicitly selected messages and forums"));
        //_pushStrategy.add(registry.getText(T_STRATEGY_PIR, "PIR (wastes bandwidth, more anonymity)"));
        _pushStrategy.select(idx);
        _pushStrategy.setRedraw(true);
    }
    
    private static final String T_PULL_STRATEGY_DELTA = "syndie.gui.syndicationconfigview.pullstrategy.delta";
    private static final String T_PULL_STRATEGY_DELTABOOKMARKED = "syndie.gui.syndicationconfigview.pullstrategy.deltabookmarked";
    private static final String T_PULL_STRATEGY_DELTAKNOWN = "syndie.gui.syndicationconfigview.pullstrategy.deltaknown";
    private static final String T_PULL_STRATEGY_EXPLICIT = "syndie.gui.syndicationconfigview.pullstrategy.explicit";
    private static final String T_PULL_STRATEGY_PIR = "syndie.gui.syndicationconfigview.pullstrategy.pir";
    private static final String T_PUSH_STRATEGY_DELTA = "syndie.gui.syndicationconfigview.pushstrategy.delta";
    private static final String T_PUSH_STRATEGY_DELTAKNOWN = "syndie.gui.syndicationconfigview.pushstrategy.deltaknown";
    private static final String T_PUSH_STRATEGY_PIR = "syndie.gui.syndicationconfigview.pushstrategy.pir";
    
    private static final String T_ACTION_INDEX = "syndie.gui.syndicationconfigview.action.index";
    private static final String T_ACTION_PULLONLY = "syndie.gui.syndicationconfigview.action.pullonly";
    private static final String T_ACTION_PUSHPULL = "syndie.gui.syndicationconfigview.action.pullpush";
    private static final String T_ACTION_PUSHONLY = "syndie.gui.syndicationconfigview.action.pushonly";
    private static final String T_SIZE = "syndie.gui.syndicationconfigview.size";
    private static final String T_SIZE_TOOLTIP = "syndie.gui.syndicationconfigview.size_tooltip";
    private static final String T_PULL_STRATEGY = "syndie.gui.syndicationconfigview.pullstrategy";
    private static final String T_PUSH_STRATEGY = "syndie.gui.syndicationconfigview.pushstrategy";
    private static final String T_CONCURRENCY = "syndie.gui.syndicationconfigview.concurrency";
    private static final String T_CONCURRENCY_TOOLTIP = "syndie.gui.syndicationconfigview.concurrency_tooltip";
    private static final String T_PULLEXPLICIT = "syndie.gui.syndicationconfigview.pullexplicit";
    private static final String T_PULLEXPLICITBUTTON = "syndie.gui.syndicationconfigview.pullexplicitbutton";
    private static final String T_PROXYHOST = "syndie.gui.syndicationconfigview.proxyhost";
    private static final String T_PROXYPORT = "syndie.gui.syndicationconfigview.proxyport";
    private static final String T_FCPHOST = "syndie.gui.syndicationconfigview.fcphost";
    private static final String T_FCPPORT = "syndie.gui.syndicationconfigview.fcpport";
    
    public void translate(TranslationRegistry registry) {
        _actionIndex.setText(registry.getText(T_ACTION_INDEX, "Fetch archive index"));
        _actionPullOnly.setText(registry.getText(T_ACTION_PULLONLY, "Pull only"));
        _actionPushPull.setText(registry.getText(T_ACTION_PUSHPULL, "Pull and push"));
        _actionPushOnly.setText(registry.getText(T_ACTION_PUSHONLY, "Push only"));
        _pullStrategyLabel.setText(registry.getText(T_PULL_STRATEGY, "Pull strategy: "));
        _pushStrategyLabel.setText(registry.getText(T_PUSH_STRATEGY, "Push strategy: "));
        updateStrategy(registry);
        _sizeLabel.setText(registry.getText(T_SIZE, "Max size: "));
        _size.setToolTipText(registry.getText(T_SIZE_TOOLTIP, "kilobytes"));
        _concurrencyLabel.setText(registry.getText(T_CONCURRENCY, "Concurrency: "));
        _concurrency.setToolTipText(registry.getText(T_CONCURRENCY_TOOLTIP, "Number of locations fetched at a time"));
        _pullExplicitLabel.setText(registry.getText(T_PULLEXPLICIT, "Explicit:"));
        _pullExplicitButton.setText(registry.getText(T_PULLEXPLICITBUTTON, "Customize messages and forums to pull"));
        
        _proxyHostLabel.setText(registry.getText(T_PROXYHOST, "HTTP proxy host:"));
        _proxyPortLabel.setText(registry.getText(T_PROXYPORT, "port:"));
        _fcpHostLabel.setText(registry.getText(T_FCPHOST, "Freenet FCP host:"));
        _fcpPortLabel.setText(registry.getText(T_FCPPORT, "port:"));
    }
}
