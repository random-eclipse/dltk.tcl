package org.eclipse.dltk.tcl.internal.core.packages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.ISourceParserConstants;
import org.eclipse.dltk.ast.statements.Statement;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.IProblemFactory;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.ProblemSeverities;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.builder.IScriptBuilder;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.tcl.core.TclNature;
import org.eclipse.dltk.tcl.core.TclParseUtil.CodeModel;
import org.eclipse.dltk.tcl.core.ast.TclPackageDeclaration;

public class TclCheckBuilder implements IScriptBuilder {

	private static final String TCL_PROBLEM_REQUIRE = "tcl.problem.require";

	public IStatus[] buildModelElements(IScriptProject project, List elements,
			IProgressMonitor monitor, int status) {
		int est = estimateElementsToBuild(elements);
		if (est == 0) {
			if (monitor != null) {
				monitor.done();
			}
			return null;
		}
		IDLTKLanguageToolkit toolkit;
		try {
			toolkit = DLTKLanguageManager.getLanguageToolkit(project);
		} catch (CoreException e2) {
			if (DLTKCore.DEBUG) {
				e2.printStackTrace();
			}
			return null;
		}
		if (!toolkit.getNatureId().equals(TclNature.NATURE_ID)) {
			return null;
		}
		if (monitor != null) {
			monitor.beginTask("Perfoming code checks", est);
		}

		Map resourceToPackagesList = new HashMap();
		Set packagesInBuild = new HashSet();

		IInterpreterInstall install = null;
		try {
			install = ScriptRuntime.getInterpreterInstall(project);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if (install == null) {
			return null;
		}

		PackagesManager manager = PackagesManager.getInstance();
		Set packageNames = manager.getPackageNames(install);
		Set buildpath = getBuildpath(project);

		processSources(elements, monitor, resourceToPackagesList,
				packagesInBuild);

		// This method will populate all required paths.
		IPath[] paths = manager.getPathsForPackages(install, packagesInBuild);

		Set keySet = resourceToPackagesList.keySet();
		IProblemFactory factory;
		try {
			factory = DLTKLanguageManager.getProblemFactory(toolkit
					.getNatureId());
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
			return null;
		}
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
			ISourceModule module = (ISourceModule) iterator.next();
			List pkgs = (List) resourceToPackagesList.get(module);
			CodeModel model = null;
			try {
				model = new CodeModel(module.getSource());
			} catch (ModelException e) {
				if (DLTKCore.DEBUG) {
					e.printStackTrace();
				}
				continue;
			}

			IProblemReporter reporter = factory.createReporter(module
					.getResource());
			for (Iterator iterator2 = pkgs.iterator(); iterator2.hasNext();) {
				TclPackageDeclaration pkg = (TclPackageDeclaration) iterator2
						.next();
				check(pkg, packageNames, reporter, model, manager, install,
						buildpath);
			}
		}

		if (monitor != null) {
			monitor.done();
		}
		return null;
	}

	private static Set getBuildpath(IScriptProject project) {
		IBuildpathEntry[] resolvedBuildpath;
		try {
			resolvedBuildpath = project.getResolvedBuildpath(true);
		} catch (ModelException e1) {
			e1.printStackTrace();
			return null;
		}
		Set buildpath = new HashSet();
		for (int i = 0; i < resolvedBuildpath.length; i++) {
			if (resolvedBuildpath[i].getEntryKind() == IBuildpathEntry.BPE_LIBRARY
					&& resolvedBuildpath[i].isExternal()) {
				buildpath.add(resolvedBuildpath[i].getPath());
			}
		}
		return buildpath;
	}

	private void processSources(List elements, IProgressMonitor monitor,
			Map resourceToPackagesList, Set packagesInBuild) {
		for (int i = 0; i < elements.size(); i++) {
			IModelElement element = (IModelElement) elements.get(i);
			if (element.getElementType() == IModelElement.SOURCE_MODULE) {
				IProjectFragment projectFragment = (IProjectFragment) element
						.getAncestor(IModelElement.PROJECT_FRAGMENT);
				if (!projectFragment.isExternal()) {
					try {
						if (monitor != null) {
							monitor.subTask("Checking file:"
									+ element.getElementName());
						}
						IDLTKLanguageToolkit toolkit = DLTKLanguageManager
								.getLanguageToolkit(element);
						if (toolkit == null) {
							if (monitor != null) {
								monitor.worked(1);
							}
							continue;
						}

						ISourceModule module = (ISourceModule) element;
						IResource resource = module.getResource();
						cleanMarkers(resource);

						ModuleDeclaration declaration = SourceParserUtil
								.getModuleDeclaration(module, null,
										ISourceParserConstants.RUNTIME_MODEL);

						final ArrayList list = new ArrayList();
						resourceToPackagesList.put(module, list);
						fillPackagesDeclarations(declaration, list,
								packagesInBuild);

						if (monitor != null) {
							monitor.worked(1);
						}
					} catch (CoreException e) {
						if (DLTKCore.DEBUG) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						if (DLTKCore.DEBUG) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static void cleanMarkers(IResource resource) throws CoreException {
		IMarker[] findMarkers = resource.findMarkers(
				DefaultProblem.MARKER_TYPE_PROBLEM, true,
				IResource.DEPTH_INFINITE);
		for (int j = 0; j < findMarkers.length; j++) {
			if( findMarkers[j].getAttribute(TCL_PROBLEM_REQUIRE, null) != null ) {
				findMarkers[j].delete();
			}
		}
	}

	private void fillPackagesDeclarations(ModuleDeclaration declaration,
			final ArrayList list, final Set packagesInBuild) throws Exception {
		declaration.traverse(new ASTVisitor() {
			public boolean visit(Statement s) throws Exception {
				if (s instanceof TclPackageDeclaration) {
					TclPackageDeclaration pkg = (TclPackageDeclaration) s;
					if (pkg.getStyle() == TclPackageDeclaration.STYLE_REQUIRE) {
						TclPackageDeclaration copy = new TclPackageDeclaration(
								pkg);
						list.add(copy);
						packagesInBuild.add(copy.getName());
					}
					return false;
				}
				return super.visit(s);
			}
		});
	}

	private static void reportProblem(TclPackageDeclaration pkg,
			IProblemReporter reporter, CodeModel model, String name,
			String pkgName) {
		try {
			IMarker marker = reporter.reportProblem(new DefaultProblem("",
					name, 777, null, ProblemSeverities.Error,
					pkg.sourceStart(), pkg.sourceEnd(), model.getLineNumber(pkg
							.sourceStart(), pkg.sourceEnd())));
			marker.setAttribute(TCL_PROBLEM_REQUIRE, pkgName);
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
	}

	public static void check(TclPackageDeclaration pkg, Set packageNames,
			IProblemReporter reporter, CodeModel model,
			PackagesManager manager, IInterpreterInstall install, Set buildpath) {
		if (pkg.getStyle() == TclPackageDeclaration.STYLE_REQUIRE) {
			String packageName = pkg.getName();

			// This package is unknown for specified interpreter
//			if (!packageNames.contains(packageName)) {
//				reportProblem(pkg, reporter, model,
//						"Required library not pressent in interprter",
//						packageName);
//			}
			// Receive main package and it paths.
			boolean error = check(pkg, reporter, model, manager, install,
					buildpath, packageName);

			Map dependencies = manager.getDependencies(packageName, install);
			for (Iterator iterator = dependencies.keySet().iterator(); iterator
					.hasNext();) {
				String pkgName = (String) iterator.next();
				boolean fail = check(pkg, reporter, model, manager, install,
						buildpath, pkgName);
				if (fail) {
					error = true;
				}
			}
			if (error) {
				reportProblem(pkg, reporter, model, "Package " + packageName
						+ " has unresolved dependencies.", packageName);
			}
		}
	}

	private static boolean check(TclPackageDeclaration pkg,
			IProblemReporter reporter, CodeModel model,
			PackagesManager manager, IInterpreterInstall install,
			Set buildpath, String packageName) {
		IPath[] paths = manager.getPathsForPackage(install, packageName);
		// Check what package path are in project buildpath.
		return checkPaths(pkg, reporter, model, buildpath, paths, packageName);
	}

	private static boolean checkPaths(TclPackageDeclaration pkg,
			IProblemReporter reporter, CodeModel model, Set buildpath,
			IPath[] paths, String packageName) {
		boolean error = false;
		List notPaths = new ArrayList();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				if (!buildpath.contains(paths[i])) {
					boolean prefix = false;
					for (Iterator iterator = buildpath.iterator(); iterator
							.hasNext();) {
						IPath pp = (IPath) iterator.next();
						if (pp.isPrefixOf(paths[i])) {
							prefix = true;
							break;
						}
					}
					if (!prefix) {
						error = true;
						notPaths.add(paths[i]);
					}
				}
			}
		}
		return error;
	}

	public IStatus[] buildResources(IScriptProject project, List resources,
			IProgressMonitor monitor, int status) {
		return null;
	}

	public int estimateElementsToBuild(List elements) {
		int estimation = 0;
		for (int i = 0; i < elements.size(); i++) {
			IModelElement element = (IModelElement) elements.get(i);
			if (element.getElementType() == IModelElement.SOURCE_MODULE) {
				IProjectFragment projectFragment = (IProjectFragment) element
						.getAncestor(IModelElement.PROJECT_FRAGMENT);
				if (!projectFragment.isExternal())
					estimation++;
			}
		}
		return estimation;
	}

	public List getDependencies(IScriptProject project, List resources) {
		return null;
	}

	public static void check(TclPackageDeclaration pkg,
			IProblemReporter reporter, IScriptProject scriptProject,
			CodeModel model) {
		long time = System.currentTimeMillis();
		IInterpreterInstall install = null;
		try {
			install = ScriptRuntime.getInterpreterInstall(scriptProject);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if (install == null) {
			return;
		}
		Set buildpath = getBuildpath(scriptProject);
		PackagesManager manager = PackagesManager.getInstance();
		Set packageNames = manager.getPackageNames(install);
		check(pkg, packageNames, reporter, model, manager, install, buildpath);
		System.out.println(Long.toString(System.currentTimeMillis() - time));
	}
}