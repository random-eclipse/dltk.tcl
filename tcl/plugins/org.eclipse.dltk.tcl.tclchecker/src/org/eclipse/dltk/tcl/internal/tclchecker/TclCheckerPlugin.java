/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.tcl.internal.tclchecker;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.tcl.internal.tclchecker.impl.TclCheckerEnvironment;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TclCheckerPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.dltk.tcl.tclchecker"; //$NON-NLS-1$

	// The shared instance.
	private static TclCheckerPlugin plugin;

	/**
	 * The constructor.
	 */
	public TclCheckerPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static TclCheckerPlugin getDefault() {
		return plugin;
	}

	public static void error(Throwable t) {
		log(IStatus.ERROR, t.toString(), t);
	}

	public static void error(String message) {
		log(IStatus.ERROR, message);
	}

	public static void error(String message, Throwable t) {
		log(IStatus.ERROR, message, t);
	}

	public static void log(int severity, String message) {
		getDefault().getLog().log(new Status(severity, PLUGIN_ID, message));
	}

	public static void log(int severity, String message, Throwable throwable) {
		getDefault().getLog().log(
				new Status(severity, PLUGIN_ID, message, throwable));
	}

	public static ITclCheckerEnvironment getEnvironmentPreferences(
			IEnvironment environment) {
		Assert.isNotNull(environment);
		return new TclCheckerEnvironment(environment);
	}

	// private IPreferenceStore preferenceStore;
	//	
	// public IPreferenceStore getPreferenceStore() {
	// if (preferenceStore == null) {
	// preferenceStore = new ScopedPreferenceStore(new
	// InstanceScope(),getBundle().getSymbolicName());
	//
	// }
	// return preferenceStore;
	// }
}
