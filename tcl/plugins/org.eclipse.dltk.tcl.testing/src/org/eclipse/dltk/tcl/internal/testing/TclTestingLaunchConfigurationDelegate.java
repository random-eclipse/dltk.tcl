package org.eclipse.dltk.tcl.internal.testing;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.launching.IInterpreterRunner;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.dltk.tcl.launching.TclLaunchConfigurationDelegate;
import org.eclipse.dltk.tcl.testing.ITclTestingEngine;
import org.eclipse.dltk.testing.DLTKTestingConstants;
import org.eclipse.dltk.testing.DLTKTestingCore;

public class TclTestingLaunchConfigurationDelegate extends TclLaunchConfigurationDelegate {
	private ITclTestingEngine engine;

	@Override
	protected InterpreterConfig createInterpreterConfig(ILaunchConfiguration configuration, ILaunch launch)
			throws CoreException {
		// We need to create correct execute script for this element.
		InterpreterConfig config = super.createInterpreterConfig(configuration, launch);
		ITclTestingEngine[] engines = TclTestingEngineManager.getEngines();
		String engineId = configuration.getAttribute(DLTKTestingConstants.ATTR_ENGINE_ID, Util.EMPTY_STRING);
		for (int i = 0; i < engines.length; i++) {
			if (engines[i].getId().equals(engineId)) {
				engines[i].correctLaunchConfiguration(config, configuration, launch);
				this.engine = engines[i];
				break;
			}
		}
		return config;
	}

	@Override
	protected void runRunner(ILaunchConfiguration configuration, IInterpreterRunner runner, InterpreterConfig config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (engine != null) {
			DLTKTestingCore.registerTestingProcessor(launch, engine.getProcessor(launch));
		}

		super.runRunner(configuration, runner, config, launch, monitor);
	}
}
