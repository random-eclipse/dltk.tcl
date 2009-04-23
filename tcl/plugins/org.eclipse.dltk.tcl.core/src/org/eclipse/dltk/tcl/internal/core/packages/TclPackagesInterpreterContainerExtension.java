package org.eclipse.dltk.tcl.internal.core.packages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IAccessRule;
import org.eclipse.dltk.core.IBuildpathAttribute;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IInterpreterContainerExtension;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.internal.core.BuildpathEntry;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterContainerHelper;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.tcl.core.TclCorePreferences;
import org.eclipse.dltk.tcl.core.internal.packages.TclPackagesManager;
import org.eclipse.dltk.tcl.core.packages.TclPackageInfo;
import org.eclipse.emf.common.util.EList;

public class TclPackagesInterpreterContainerExtension implements
		IInterpreterContainerExtension {

	private static final IAccessRule[] EMPTY_RULES = new IAccessRule[0];

	public TclPackagesInterpreterContainerExtension() {
	}

	public void processEntres(IScriptProject project, List buildpathEntries) {
		if (TclCorePreferences.USE_PACKAGE_CONCEPT) {
			return;
		}
		IPath[] locations = null;
		IInterpreterInstall install = null;
		try {
			install = ScriptRuntime.getInterpreterInstall(project);
			List locs = new ArrayList();
			for (Iterator iterator = buildpathEntries.iterator(); iterator
					.hasNext();) {
				IBuildpathEntry entry = (IBuildpathEntry) iterator.next();
				if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY
						&& entry.isExternal()) {
					locs.add(entry.getPath());
				}
				locations = (IPath[]) locs.toArray(new IPath[locs.size()]);
			}
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		if (install != null) {
			Set<String> set = InterpreterContainerHelper
					.getInterpreterContainerDependencies(project);

			List<TclPackageInfo> infos = TclPackagesManager.getPackageInfos(
					install, set, true);
			if (infos.size() == 0) {
				return;
			}
			IEnvironment env = EnvironmentManager.getEnvironment(project);
			if (env == null) {
				return;
			}
			Set<IPath> allPaths = new HashSet<IPath>();
			for (TclPackageInfo info : infos) {
				EList<String> sources = info.getSources();
				for (String path : sources) {
					IPath rpath = new Path(path).removeLastSegments(1);
					IPath fullPath = EnvironmentPathUtils.getFullPath(env,
							rpath);
					allPaths.add(fullPath);
				}
			}

			Set rawEntries = new HashSet(allPaths.size());
			for (Iterator iterator = allPaths.iterator(); iterator.hasNext();) {
				IPath entryPath = (IPath) iterator.next();

				if (!entryPath.isEmpty()) {
					// TODO Check this
					// resolve symlink
					// {
					// IFileHandle f = env.getFile(entryPath);
					// if (f == null)
					// continue;
					// entryPath = new Path(f.getCanonicalPath());
					// }
					if (rawEntries.contains(entryPath))
						continue;

					/*
					 * if (!entryPath.isAbsolute()) Assert.isTrue(false, "Path
					 * for IBuildpathEntry must be absolute"); //$NON-NLS-1$
					 */
					IBuildpathAttribute[] attributes = new IBuildpathAttribute[0];
					ArrayList excluded = new ArrayList(); // paths to exclude
					for (Iterator iterator2 = allPaths.iterator(); iterator2
							.hasNext();) {
						IPath otherPath = (IPath) iterator2.next();
						if (otherPath.isEmpty())
							continue;
						// TODO Check this
						// resolve symlink
						// {
						// IFileHandle f = env.getFile(entryPath);
						// if (f == null)
						// continue;
						// entryPath = new Path(f.getCanonicalPath());
						// }
						// compare, if it contains some another
						if (entryPath.isPrefixOf(otherPath)
								&& !otherPath.equals(entryPath)) {
							IPath pattern = otherPath.removeFirstSegments(
									entryPath.segmentCount()).append("*");
							if (!excluded.contains(pattern)) {
								excluded.add(pattern);
							}
						}
					}
					boolean inInterpreter = false;
					if (locations != null) {
						for (int i = 0; i < locations.length; i++) {
							IPath path = locations[i];
							if (path.isPrefixOf(entryPath)) {
								inInterpreter = true;
								break;
							}
						}
					}
					if (!inInterpreter) {
						// Check for interpreter container libraries.
						buildpathEntries.add(DLTKCore.newLibraryEntry(
								entryPath, EMPTY_RULES, attributes,
								BuildpathEntry.INCLUDE_ALL, (IPath[]) excluded
										.toArray(new IPath[excluded.size()]),
								false, true));
						rawEntries.add(entryPath);
					}
				}
			}
		}
	}
}
