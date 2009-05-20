package org.eclipse.dltk.tcl.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.environment.IDeployment;
import org.eclipse.dltk.core.environment.IExecutionEnvironment;
import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.dltk.launching.ScriptLaunchUtil;
import org.eclipse.dltk.tcl.core.packages.TclInterpreterInfo;
import org.eclipse.dltk.tcl.core.packages.TclModuleInfo;
import org.eclipse.dltk.tcl.core.packages.TclPackageInfo;
import org.eclipse.dltk.tcl.core.packages.TclPackagesFactory;
import org.eclipse.dltk.tcl.core.packages.TclProjectInfo;
import org.eclipse.dltk.tcl.internal.core.packages.ProcessOutputCollector;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TclPackagesManager {
	private static final String DLTK_TCL = "scripts/dltk.tcl"; //$NON-NLS-1$
	public static final String END_OF_STREAM = "DLTK-TCL-HELPER-9E7A168E-5EEF-4a46-A86D-0C82E90686E4-END-OF-STREAM";
	private static final String PKG_VERSION = "v20090505";

	private static Resource infos = null;
	private static final Map<String, Resource> projectInfos = new HashMap<String, Resource>();

	public static synchronized List<TclPackageInfo> getPackageInfos(
			IInterpreterInstall install, Set<String> packageNames,
			boolean fetchIfRequired) {
		initialize();
		TclInterpreterInfo interpreterInfo = getTclInterpreter(install);
		return Collections.unmodifiableList(getPackagesForInterpreter(
				packageNames, fetchIfRequired, interpreterInfo, install));
	}

	public static synchronized List<TclPackageInfo> getPackageInfos(
			IInterpreterInstall install) {
		initialize();
		TclInterpreterInfo interpreterInfo = getTclInterpreter(install);
		return Collections.unmodifiableList(interpreterInfo.getPackages());
	}

	public static synchronized TclPackageInfo getPackageInfo(
			IInterpreterInstall install, String name, boolean fetchIfRequired) {
		initialize();
		TclInterpreterInfo interpreterInfo = getTclInterpreter(install);
		EList<TclPackageInfo> packages = interpreterInfo.getPackages();
		for (TclPackageInfo tclPackageInfo : packages) {
			if (name.equals(tclPackageInfo.getName())) {
				if (tclPackageInfo.isFetched() || !fetchIfRequired) {
					return tclPackageInfo;
				} else {
					Set<TclPackageInfo> toFetch = new HashSet<TclPackageInfo>();
					toFetch.add(tclPackageInfo);
					fetchSources(toFetch, install, interpreterInfo);
					return tclPackageInfo;
				}
			}
		}
		return null;
	}

	public static Set<TclPackageInfo> getDependencies(
			IInterpreterInstall install, String name, boolean fetchIfRequired) {
		initialize();
		TclPackageInfo info = getPackageInfo(install, name, fetchIfRequired);
		if (info != null) {
			Set<TclPackageInfo> result = new HashSet<TclPackageInfo>();
			Set<TclPackageInfo> toFetch = new HashSet<TclPackageInfo>();
			processPackage(info, result, toFetch, fetchIfRequired);
			if (toFetch.size() > 0) {
				TclInterpreterInfo interpreter = getTclInterpreter(install);
				fetchSources(toFetch, install, interpreter);
				processPackage(info, result, toFetch, fetchIfRequired);
			}
			result.remove(info);
			return result;
		}
		return null;
	}

	private static synchronized TclInterpreterInfo getTclInterpreter(
			IInterpreterInstall install) {
		EList<EObject> contents = infos.getContents();
		TclInterpreterInfo interpreterInfo = null;
		String interpreterLocation = install.getInstallLocation().toOSString();
		String environmentId = install.getInstallLocation().getEnvironmentId();
		for (EObject eObject : contents) {
			if (eObject instanceof TclInterpreterInfo) {
				TclInterpreterInfo info = (TclInterpreterInfo) eObject;
				String location = info.getInstallLocation();
				String name = info.getName();
				String env = info.getEnvironment();
				if (interpreterLocation.equals(location)
						&& install.getName().equals(name) && env != null
						&& env.equals(environmentId)) {
					interpreterInfo = info;
					break;
				}
			}
		}
		if (interpreterInfo == null) {
			interpreterInfo = TclPackagesFactory.eINSTANCE
					.createTclInterpreterInfo();
			interpreterInfo.setInstallLocation(interpreterLocation);
			interpreterInfo.setName(install.getName());
			interpreterInfo.setEnvironment(environmentId);
			infos.getContents().add(interpreterInfo);
		}
		if (!interpreterInfo.isFetched()
				|| interpreterInfo.getFetchedAt() == null
				|| interpreterInfo.getFetchedAt().getTime()
						+ getPackagesRefreshInterval(install) < System
						.currentTimeMillis()) {
			fetchPackagesForInterpreter(install, interpreterInfo);
		}
		return interpreterInfo;
	}

	private static long getPackagesRefreshInterval(IInterpreterInstall install) {
		return TclPlugin
				.getDefault()
				.getPluginPreferences()
				.getLong(
						install.getEnvironment().isLocal() ? TclCorePreferences.PACKAGES_REFRESH_INTERVAL_LOCAL
								: TclCorePreferences.PACKAGES_REFRESH_INTERVAL_REMOTE);
	}

	public static TclProjectInfo getTclProject(String name) {
		Resource resource = getProjectInfoResource(name);
		synchronized (resource) {
			TclProjectInfo info = null;
			for (EObject eObject : resource.getContents()) {
				if (eObject instanceof TclProjectInfo) {
					TclProjectInfo pinfo = (TclProjectInfo) eObject;
					String pname = pinfo.getName();
					if (name != null && name.equals(pname)) {
						info = pinfo;
					}
				}
			}
			if (info == null) {
				info = TclPackagesFactory.eINSTANCE.createTclProjectInfo();
				info.setName(name);
				resource.getContents().add(info);
			}
			return info;
		}
	}

	private static void fetchPackagesForInterpreter(
			IInterpreterInstall install, TclInterpreterInfo interpreterInfo) {
		IExecutionEnvironment exeEnv = install.getExecEnvironment();
		List<String> content = deployExecute(exeEnv, install
				.getInstallLocation().toOSString(),
				new String[] { "get-pkgs" }, install //$NON-NLS-1$
						.getEnvironmentVariables());
		if (content != null) {
			processContent(content, false, true, interpreterInfo);
			interpreterInfo.setFetched(true);
			interpreterInfo.setFetchedAt(new Date());
			save();
		}
	}

	public static void save() {
		if (infos != null) {
			try {
				infos.save(null);
			} catch (IOException e) {
				TclPlugin.error(e);
			}
		}
		synchronized (projectInfos) {
			for (Map.Entry<String, Resource> entry : projectInfos.entrySet()) {
				try {
					entry.getValue().save(null);
				} catch (IOException e) {
					String msg = NLS.bind("Error saving {0} state: {1}", entry
							.getKey(), e.getMessage());
					TclPlugin.error(msg, e);
				}
			}
		}
	}

	private static String getXMLContent(List<String> content) {
		StringBuffer newList = new StringBuffer();
		if (content != null) {
			for (Iterator<String> iterator = content.iterator(); iterator
					.hasNext();) {
				String line = iterator.next();
				if (line.trim().startsWith("<")) { //$NON-NLS-1$
					newList.append(line).append("\n"); //$NON-NLS-1$
				}
			}
		}
		return newList.toString();
	}

	private static Document getDocument(String text) {
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			InputSource source = new InputSource(new StringReader(text));
			Document document = parser.parse(source);
			return document;
		} catch (IOException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		} catch (SAXException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static void processContent(List<String> content,
			boolean markAsFetched, boolean purgePackages,
			TclInterpreterInfo info) {
		String text = getXMLContent(content);
		Document document = getDocument(text);

		if (document != null) {
			final Set<String> processedPackages = new HashSet<String>();
			Element element = document.getDocumentElement();
			NodeList childNodes = element.getChildNodes();
			int len = childNodes.getLength();
			for (int i = 0; i < len; i++) {
				Node nde = childNodes.item(i);
				if (isElementName(nde, "path")) { //$NON-NLS-1$
					Element el = (Element) nde;
					NodeList elChilds = el.getChildNodes();
					for (int j = 0; j < elChilds.getLength(); j++) {
						Node pkgNde = elChilds.item(j);
						if (isElementName(pkgNde, "package")) { //$NON-NLS-1$
							Element pkgElement = (Element) pkgNde;
							String name = pkgElement.getAttribute("name");
							processedPackages.add(name);
							TclPackageInfo pkg = getCreatePackage(info, name);
							if (markAsFetched) {
								pkg.setFetched(markAsFetched);
							}
							populatePackage(pkg, pkgNde, info);
						}
					}
				}
			}
			if (purgePackages) {
				for (Iterator<TclPackageInfo> i = info.getPackages().iterator(); i
						.hasNext();) {
					TclPackageInfo packageInfo = i.next();
					if (!processedPackages.contains(packageInfo.getName())) {
						i.remove();
					}
				}
			}
		}
	}

	private static TclPackageInfo getCreatePackage(TclInterpreterInfo info,
			String name) {
		TclPackageInfo packageInfo = null;
		for (TclPackageInfo pkgInfo : info.getPackages()) {
			if (pkgInfo.getName().equals(name)) {
				packageInfo = pkgInfo;
				break;
			}
		}
		if (packageInfo == null) {
			packageInfo = TclPackagesFactory.eINSTANCE.createTclPackageInfo();
			packageInfo.setFetched(false);
			packageInfo.setName(name);
			info.getPackages().add(packageInfo);
		}
		return packageInfo;
	}

	private static void populatePackage(TclPackageInfo info, Node pkgNde,
			TclInterpreterInfo interpreterInfo) {
		Element pkg = (Element) pkgNde;

		info.setVersion(pkg.getAttribute("version"));
		NodeList childs = pkg.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node nde = childs.item(i);
			if (isElementName(nde, "source")) { //$NON-NLS-1$
				Element el = (Element) nde;
				String name = el.getAttribute("name"); //$NON-NLS-1$
				info.getSources().add(name);
			} else if (isElementName(nde, "require")) { //$NON-NLS-1$
				Element el = (Element) nde;
				String name = el.getAttribute("name"); //$NON-NLS-1$
				info.getDependencies().add(
						getCreatePackage(interpreterInfo, name));
			}
		}
	}

	private static List<TclPackageInfo> getPackagesForInterpreter(
			Set<String> packageName, boolean fetchIfRequired,
			TclInterpreterInfo interpreterInfo, IInterpreterInstall install) {
		Set<TclPackageInfo> result = new HashSet<TclPackageInfo>();
		Set<TclPackageInfo> toFetch = new HashSet<TclPackageInfo>();
		for (TclPackageInfo tclPackageInfo : interpreterInfo.getPackages()) {
			if (packageName.contains(tclPackageInfo.getName())) {
				processPackage(tclPackageInfo, result, toFetch, fetchIfRequired);
			}
		}
		fetchSources(toFetch, install, interpreterInfo);
		for (TclPackageInfo tclPackageInfo : interpreterInfo.getPackages()) {
			if (packageName.contains(tclPackageInfo.getName())) {
				processPackage(tclPackageInfo, result, toFetch, fetchIfRequired);
			}
		}
		List<TclPackageInfo> resultList = new ArrayList<TclPackageInfo>();
		resultList.addAll(result);
		return resultList;
	}

	private static void processPackage(TclPackageInfo tclPackageInfo,
			Set<TclPackageInfo> result, Set<TclPackageInfo> toFetch,
			boolean fetchIfRequired) {
		if (tclPackageInfo.isFetched() || !fetchIfRequired) {
			result.add(tclPackageInfo);
		} else if (fetchIfRequired) {
			result.add(tclPackageInfo);
			toFetch.add(tclPackageInfo);
		}
		EList<TclPackageInfo> dependencies = tclPackageInfo.getDependencies();
		for (TclPackageInfo tclPackageInfo2 : dependencies) {
			if (!result.contains(tclPackageInfo2)) {
				processPackage(tclPackageInfo2, result, toFetch,
						fetchIfRequired);
			}
		}
	}

	private static void fetchSources(Set<TclPackageInfo> toFetch,
			IInterpreterInstall install, TclInterpreterInfo interpreterInfo) {
		if (toFetch.size() == 0) {
			return;
		}
		IExecutionEnvironment exeEnv = install.getExecEnvironment();
		IDeployment deployment = exeEnv.createDeployment();
		IFileHandle script = deploy(deployment);
		if (script == null) {
			return;
		}

		IFileHandle workingDir = script.getParent();
		InterpreterConfig config = ScriptLaunchUtil.createInterpreterConfig(
				exeEnv, script, workingDir, install.getEnvironmentVariables());
		StringBuffer buf = new StringBuffer();
		for (TclPackageInfo tclPackageInfo : toFetch) {
			buf.append(tclPackageInfo.getName()).append(" "); //$NON-NLS-1$
		}
		String names = buf.toString();
		ByteArrayInputStream bais = new ByteArrayInputStream(names.getBytes());
		IPath packagesPath = null;
		try {
			packagesPath = deployment.add(bais, "packages.txt"); //$NON-NLS-1$
		} catch (IOException e1) {
			if (DLTKCore.DEBUG) {
				e1.printStackTrace();
			}
			return;
		}
		IFileHandle file = deployment.getFile(packagesPath);
		// For wish
		config.removeEnvVar("DISPLAY"); //$NON-NLS-1$
		String[] arguments = new String[] { "get-srcs", "-fpkgs", //$NON-NLS-1$ //$NON-NLS-2$
				file.toOSString() };

		config.addScriptArgs(arguments);

		Process process = null;
		try {
			process = ScriptLaunchUtil.runScriptWithInterpreter(exeEnv, install
					.getInstallLocation().toOSString(), config);
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		if (process == null) {
			return;
		}
		List<String> output = ProcessOutputCollector.execute(process);
		deployment.dispose();
		processContent(output, true, false, interpreterInfo);

		// Mark all toFetch as fetched.
		for (TclPackageInfo info : toFetch) {
			info.setFetched(true);
		}
		save();
	}

	private static URI getInfoLocation() {
		final IPath path = TclPlugin.getDefault().getStateLocation().append(
				"tclPackages_" + PKG_VERSION + ".info");
		return URI.createFileURI(path.toOSString());
	}

	private static URI getProjectLocation(String projectName) {
		final IPath path = TclPlugin.getDefault().getStateLocation().append(
				"project-" + projectName + ".info");
		return URI.createFileURI(path.toOSString());
	}

	private static boolean canLoad(URI location) {
		if (location.isFile()) {
			return new File(location.toFileString()).exists();
		} else {
			return true;
		}
	}

	private static void initialize() {
		if (infos == null) {
			final URI location = getInfoLocation();
			infos = new XMIResourceImpl(location);
			try {
				if (canLoad(location)) {
					infos.load(null);
				}
			} catch (IOException e) {
				TclPlugin.error(e);
			}
		}
	}

	/**
	 * @param name
	 * @return
	 */
	private static Resource getProjectInfoResource(String projectName) {
		synchronized (projectInfos) {
			final Resource resource = projectInfos.get(projectName);
			if (resource != null) {
				return resource;
			}
		}
		final URI location = getProjectLocation(projectName);
		final Resource resource = new XMIResourceImpl(location);
		try {
			if (canLoad(location)) {
				resource.load(null);
			}
		} catch (IOException e) {
			TclPlugin.error(e);
		}
		synchronized (projectInfos) {
			final Resource r = projectInfos.get(projectName);
			if (r != null) {
				return r;
			} else {
				projectInfos.put(projectName, resource);
				return resource;
			}
		}
	}

	private static List<String> deployExecute(IExecutionEnvironment exeEnv,
			String installLocation, String[] arguments,
			EnvironmentVariable[] env) {
		IDeployment deployment = exeEnv.createDeployment();
		if (deployment == null) {
			return null;
		}
		IFileHandle script = deploy(deployment);
		if (script == null) {
			return null;
		}

		IFileHandle workingDir = script.getParent();
		InterpreterConfig config = ScriptLaunchUtil.createInterpreterConfig(
				exeEnv, script, workingDir, env);
		// For wish
		config.removeEnvVar("DISPLAY"); //$NON-NLS-1$

		if (arguments != null) {
			config.addScriptArgs(arguments);
		}

		Process process = null;
		try {
			process = ScriptLaunchUtil.runScriptWithInterpreter(exeEnv,
					installLocation, config);
		} catch (CoreException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
		}
		if (process == null) {
			return null;
		}
		List<String> output = ProcessOutputCollector.execute(process);
		deployment.dispose();
		return output;
	}

	private static IFileHandle deploy(IDeployment deployment) {
		IFileHandle script;
		try {
			IPath path = deployment.add(TclPlugin.getDefault().getBundle(),
					DLTK_TCL);
			script = deployment.getFile(path);
		} catch (IOException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
			return null;
		}
		return script;
	}

	private static boolean isElementName(Node nde, String name) {
		if (nde != null) {
			if (nde.getNodeType() == Node.ELEMENT_NODE) {
				if (name.equalsIgnoreCase(nde.getNodeName())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isValidName(String packageName) {
		return packageName != null && packageName.length() != 0
				&& packageName.indexOf('$') == -1
				&& packageName.indexOf('[') == -1
				&& packageName.indexOf(']') == -1;
	}

	public static synchronized List<TclModuleInfo> getProjectModules(String name) {
		TclProjectInfo info = getTclProject(name);
		return Collections.unmodifiableList(info.getModules());
	}

	public static synchronized void setProjectModules(String name,
			List<TclModuleInfo> modules) {
		TclProjectInfo info = getTclProject(name);
		info.getModules().clear();
		info.getModules().addAll(modules);
		save();
	}

	public static synchronized void removeInterpreterInfo(
			IInterpreterInstall install) {
		initialize();
		TclInterpreterInfo info = getTclInterpreter(install);
		info.getPackages().clear();
		infos.getContents().remove(info);
		save();
	}

	public static synchronized Set<String> getPackageInfosAsString(
			IInterpreterInstall install) {
		initialize();
		Set<String> result = new HashSet<String>();
		List<TclPackageInfo> list = getPackageInfos(install);
		for (TclPackageInfo tclPackageInfo : list) {
			result.add(tclPackageInfo.getName());
		}
		return result;
	}
}
