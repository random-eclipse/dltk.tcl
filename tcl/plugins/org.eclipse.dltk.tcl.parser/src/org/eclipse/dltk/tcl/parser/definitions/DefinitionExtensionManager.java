/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.  
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html  
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Andrei Sobolev)
 *******************************************************************************/
package org.eclipse.dltk.tcl.parser.definitions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.tcl.internal.parser.TclParserCore;

/**
 * Load all definition extension points.
 */
public class DefinitionExtensionManager {
	private static final String EXTENSION_ID = TclParserCore.PLUGIN_ID
			+ ".definitions";
	private static DefinitionExtensionManager sInstance;
	private Set<URL> locations = new HashSet<URL>();
	private boolean initialized = false;

	public DefinitionExtensionManager() {
	}

	private void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		IConfigurationElement[] configurationElements = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						EXTENSION_ID);
		for (IConfigurationElement config : configurationElements) {
			String url = config.getAttribute("url");
			URL urlValue;
			try {
				urlValue = new URL(url);
				locations.add(urlValue);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	public URL[] getLocations() {
		initialize();
		return this.locations.toArray(new URL[this.locations.size()]);
	}

	public static DefinitionExtensionManager getInstance() {
		if (sInstance == null) {
			sInstance = new DefinitionExtensionManager();
		}
		return sInstance;
	}
}
