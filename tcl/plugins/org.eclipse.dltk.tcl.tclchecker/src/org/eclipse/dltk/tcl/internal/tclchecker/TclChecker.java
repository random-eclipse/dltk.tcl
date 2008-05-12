/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.tcl.internal.tclchecker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.IDeployment;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.core.environment.IExecutionEnvironment;
import org.eclipse.jface.preference.IPreferenceStore;

public class TclChecker {
	private static final String CHECKING = "checking:";

	private static final String SCANNING = "scanning:";

	private static class TclCheckerCodeModel {
		private String[] codeLines;

		private int[] codeLineLengths;

		public TclCheckerCodeModel(String code) {
			this.codeLines = code.split("\n");
			int count = this.codeLines.length;

			this.codeLineLengths = new int[count];

			int sum = 0;
			for (int i = 0; i < count; ++i) {
				this.codeLineLengths[i] = sum;
				sum += this.codeLines[i].length() + 1;
			}
		}

		public int[] getBounds(int lineNumber) {
			String codeLine = codeLines[lineNumber];
			String trimmedCodeLine = codeLine.trim();

			int start = codeLineLengths[lineNumber]
					+ codeLine.indexOf(trimmedCodeLine);
			int end = start + trimmedCodeLine.length();

			return new int[] { start, end };
		}
	}

	protected static IMarker reportErrorProblem(IResource resource,
			TclCheckerProblem problem, int start, int end) throws CoreException {

		return TclCheckerMarker.setMarker(resource, problem.getLineNumber(),
				start, end, problem.getDescription().getMessage(),
				IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
	}

	protected static IMarker reportWarningProblem(IResource resource,
			TclCheckerProblem problem, int start, int end) throws CoreException {

		return TclCheckerMarker.setMarker(resource, problem.getLineNumber(),
				start, end, problem.getDescription().getMessage(),
				IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL);
	}

	private ISourceModule checkingModule;
	private IPreferenceStore store;

	public TclChecker(IPreferenceStore store) {
		if (store == null) {
			throw new NullPointerException("store cannot be null");
		}

		this.store = store;
	}

	public boolean canCheck(IEnvironment environment) {
		return TclCheckerHelper.canExecuteTclChecker(store, environment);
	}

	public void check(final List sourceModules, IProgressMonitor monitor,
			OutputStream console, IEnvironment environment) {
		if (!canCheck(environment)) {
			throw new IllegalStateException("TclChecker cannot be executed");
		}

		List arguments = new ArrayList();
		Map pathToSource = new HashMap();
		for (Iterator iterator = sourceModules.iterator(); iterator.hasNext();) {
			ISourceModule module = (ISourceModule) iterator.next();
			if (EnvironmentManager.isLocal(environment)) {
				try {
					char[] sourceAsCharArray = module.getSourceAsCharArray();
					if (sourceAsCharArray.length == 0) {
						continue;
					}
				} catch (ModelException e) {
					if (DLTKCore.DEBUG) {
						e.printStackTrace();
					}
				}
			}
			IPath location = module.getResource().getLocation();
			String loc = null;
			if (location == null) {
				URI locationURI = module.getResource().getLocationURI();
				loc = environment.getFile(locationURI).toOSString();
			} else {
				loc = location.toOSString();
			}
			pathToSource.put(loc, module);
			arguments.add(loc);
		}
		if (arguments.size() == 0) {
			if (monitor != null) {
				monitor.done();
			}
			return;
		}
		List cmdLine = new ArrayList();
		if (!TclCheckerHelper
				.passOriginalArguments(store, cmdLine, environment)) {
			if (console != null) {
				try {
					console.write("Path to TclChecker is not specified."
							.getBytes());
				} catch (IOException e) {
					if (DLTKCore.DEBUG) {
						e.printStackTrace();
					}
				}
			}
		}
		IExecutionEnvironment execEnvironment = (IExecutionEnvironment) environment
				.getAdapter(IExecutionEnvironment.class);
		IDeployment deployment = execEnvironment.createDeployment();
		// IPath stateLocation = TclCheckerPlugin.getDefault().getStateLocation(
		// );
		// IPath patternFile = stateLocation.append("pattern.txt");
		ByteArrayOutputStream baros = new ByteArrayOutputStream();
		try {
			for (Iterator arg = arguments.iterator(); arg.hasNext();) {
				String path = (String) arg.next();
				baros.write((path + "\n").getBytes());
			}
			baros.close();
		} catch (FileNotFoundException e1) {
			if (DLTKCore.DEBUG) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		IPath pattern;
		try {
			pattern = deployment.add(new ByteArrayInputStream(baros
					.toByteArray()), "pattern.txt");
		} catch (IOException e1) {
			if (DLTKCore.DEBUG) {
				TclCheckerPlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, TclCheckerPlugin.PLUGIN_ID,
								"Failed to deploy file list", e1));
				if (DLTKCore.DEBUG) {
					e1.printStackTrace();
				}
			}
			return;
		}
		cmdLine.add("-@");
		cmdLine.add(deployment.getFile(pattern).toOSString());
		Process process;
		BufferedReader input = null;
		String checkingFile = null;
		int scanned = 0;
		int checked = 0;

		TclCheckerCodeModel model = null;
		if (monitor == null)
			monitor = new NullProgressMonitor();

		monitor.beginTask("Executing TclChecker...",
				sourceModules.size() * 2 + 1);

		Map map = DebugPlugin.getDefault().getLaunchManager()
				.getNativeEnvironmentCasePreserved();

		String[] env = new String[map.size()];
		int i = 0;
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			env[i] = key + "=" + value;
			++i;
		}
		try {
			monitor.subTask("Launching TclChecker...");
			process = execEnvironment.exec((String[]) cmdLine
					.toArray(new String[cmdLine.size()]), null, env);

			// process = DebugPlugin.exec((String[]) cmdLine
			// .toArray(new String[cmdLine.size()]), null, env);

			monitor.worked(1);

			input = new BufferedReader(new InputStreamReader(process
					.getInputStream()));

			String line = null;
			while ((line = input.readLine()) != null) {
				// lines.add(line);
				if (console != null) {
					console.write((line + "\n").getBytes());
				}
				TclCheckerProblem problem = TclCheckerHelper.parseProblem(line);
				if (monitor.isCanceled()) {
					process.destroy();
					return;
				}
				if (line.startsWith(SCANNING)) {
					String fileName = line.substring(SCANNING.length() + 1)
							.trim();
					fileName = Path.fromOSString(fileName).lastSegment();
					monitor
							.subTask(MessageFormat
									.format(
											"TclChecker scanning \"{0}\" ({1} to scan)...",
											new Object[] {
													fileName,
													new Integer(sourceModules
															.size()
															- scanned) }));
					monitor.worked(1);
					scanned++;
				}
				if (line.startsWith(CHECKING)) {
					String fileName = line.substring(CHECKING.length() + 1)
							.trim();
					checkingFile = fileName;
					checkingModule = (ISourceModule) pathToSource
							.get(checkingFile);
					if (checkingModule == null) {
						// Lets search for fileName. If it is present one
						// time, associate with it.
						Set paths = pathToSource.keySet();
						String fullPath = null;
						for (Iterator iterator = paths.iterator(); iterator
								.hasNext();) {
							String p = (String) iterator.next();
							if (p.endsWith(fileName)) {
								if (fullPath != null) {
									fullPath = null;
									break;
								}
								fullPath = p;
							}
						}
						if (fullPath != null) {
							checkingModule = (ISourceModule) pathToSource
									.get(fullPath);
						}
					}
					if (checkingModule != null) {
						model = new TclCheckerCodeModel(checkingModule
								.getSource());
					} else {
						model = null;
					}

					fileName = Path.fromOSString(fileName).lastSegment();
					monitor
							.subTask(MessageFormat
									.format(
											"TclChecker checking  \"{0}\" ({1} to check)...",
											new Object[] {
													fileName,
													new Integer(sourceModules
															.size()
															- checked) }));
					monitor.worked(1);
					checked++;
				}
				if (problem != null && checkingFile != null
						&& checkingModule != null) {
					if (model != null) {
						TclCheckerProblemDescription desc = problem
								.getDescription();

						int[] bounds = model
								.getBounds(problem.getLineNumber() - 1);

						IResource res = checkingModule.getResource();
						if (TclCheckerProblemDescription.isError(desc
								.getCategory())) {
							reportErrorProblem(res, problem, bounds[0],
									bounds[1]);
						} else if (TclCheckerProblemDescription.isWarning(desc
								.getCategory()))
							reportWarningProblem(res, problem, bounds[0],
									bounds[1]);
					}
				}
			}
			StringBuffer errorMessage = new StringBuffer();
			// We need also read errors.
			input = new BufferedReader(new InputStreamReader(process
					.getErrorStream()));

			line = null;
			while ((line = input.readLine()) != null) {
				// lines.add(line);
				if (console != null) {
					console.write((line + "\n").getBytes());
				}
				errorMessage.append(line).append("\n");
				if (monitor.isCanceled()) {
					process.destroy();
					return;
				}
			}
			String error = errorMessage.toString();
			if (error.length() > 0) {
				TclCheckerPlugin.getDefault().getLog()
						.log(
								new Status(IStatus.ERROR,
										TclCheckerPlugin.PLUGIN_ID,
										"Error during tcl_checker execution:\n"
												+ error));
			}
		} catch (Exception e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			monitor.done();
			deployment.dispose();
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					if (DLTKCore.DEBUG) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
