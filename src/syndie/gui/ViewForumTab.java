package syndie.gui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import syndie.data.SyndieURI;

/**
 *
 */
public class ViewForumTab extends BrowserTab {
    private ViewForum _view;
    private boolean _editable;
    
    public ViewForumTab(BrowserControl browser, SyndieURI uri) { super(browser, uri); }
    
    protected void initComponents() {
        getRoot().setLayout(new FillLayout());
        _view = new ViewForum(getBrowser(), getRoot(), getURI());
        _editable = _view.getEditable();
        reconfigItem();
    }
    
    public Image getIcon() { return ImageUtil.ICON_TAB_ARCHIVE; }
    public String getName() { return _editable ? "Manage forum" : "View forum"; }
    public String getDescription() { return getName(); }
    
    protected void disposeDetails() { _view.dispose(); }
    
    public boolean close() {
        if (allowClose()) 
            return super.close();
        else
            return false;
    }
    
    protected boolean allowClose() { return _view.confirmClose(); }
}