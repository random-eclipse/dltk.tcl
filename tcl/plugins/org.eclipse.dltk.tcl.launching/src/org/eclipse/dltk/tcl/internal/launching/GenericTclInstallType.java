/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.tcl.internal.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.tcl.core.TclNature;
import org.eclipse.dltk.tcl.launching.TclLaunchingPlugin;
import org.eclipse.dltk.utils.DeployHelper;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class GenericTclInstallType extends AbstractInterpreterInstallType {
	private static final String INSTALL_TYPE_NAME = "Generic Tcl";

	private static final String[] INTERPRETER_NAMES = { "tclsh", "tclsh84",
			"wish", "wish84", "vtk", "expect" };

	public String getNatureId() {
		return TclNature.NATURE_ID;
	}

	public String getName() {
		return INSTALL_TYPE_NAME;
	}

	protected String getPluginId() {
		return TclLaunchingPlugin.PLUGIN_ID;
	}

	protected String[] getPossibleInterpreterNames() {
		return INTERPRETER_NAMES;
	}

	protected IInterpreterInstall doCreateInterpreterInstall(String id) {
		return new GenericTclInstall(this, id);
	}

	protected void filterEnvironment(Map environment) {
		// make sure that $auto_path is clean
		environment.remove("TCLLIBPATH");
		// block wish from showing window under linux
		environment.remove("DISPLAY");
	}

	protected File createPathFile() throws IOException {
		IPath path = DeployHelper.deploy(TclLaunchingPlugin.getDefault(),
				"scripts").append("auto_path.tcl");
		return path.toFile();
	}

	protected IRunnableWithProgress createLookupRunnable(
			final File installLocation, final List locations,
			final EnvironmentVariable[] variables) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				try {
					// This retrieval could not receive paths in some cases.
					String result = retrivePaths(installLocation, locations,
							monitor, createPathFile(), variables);
					// This is safe retrieval
					String message = "Failed to obtain tcl library locations for "
							+ installLocation.toString()
							+ " with auto_path.tcl.";
					if (result == null) {
						DLTKLaunchingPlugin.log(message);
					}
				} catch (IOException e) {
					DLTKLaunchingPlugin.log(e);
					if (DLTKCore.DEBUG) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	protected String[] parsePaths(String res) {
		ArrayList paths = new ArrayList();
		String subs = null;
		int index = 0;
		String result = res;
		if (result.startsWith(DLTK_PATH_PREFIX)) {
			result = result.substring(DLTK_PATH_PREFIX.length());
		}
		while (index < result.length()) {
			// skip whitespaces
			while (index < result.length()
					&& Character.isWhitespace(result.charAt(index)))
				index++;
			if (index == result.length())
				break;

			if (result.charAt(index) == '{') {
				int start = index;
				while (index < result.length() && result.charAt(index) != '}')
					index++;
				if (index == result.length())
					break;
				subs = result.substring(start + 1, index);
			} else {
				int start = index;
				while (index < result.length() && result.charAt(index) != ' ')
					index++;
				subs = result.substring(start, index);
			}

			paths.add(subs);
			index++;
		}

		return (String[]) paths.toArray(new String[paths.size()]);
	}

	protected ILog getLog() {
		return TclLaunchingPlugin.getDefault().getLog();
	}
}
