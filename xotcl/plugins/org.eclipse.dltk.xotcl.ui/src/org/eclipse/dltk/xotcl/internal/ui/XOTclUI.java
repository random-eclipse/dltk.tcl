package org.eclipse.dltk.xotcl.internal.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class XOTclUI extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.dltk.xotcl.ui";

	// The shared instance
	private static XOTclUI plugin;

	XOTclTextTools fTools;

	/**
	 * The constructor
	 */
	public XOTclUI() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static XOTclUI getDefault() {
		return plugin;
	}

	public XOTclTextTools getTextTools() {
		if (fTools == null) {
			fTools = new XOTclTextTools(true);
		}
		return fTools;
	}

}
