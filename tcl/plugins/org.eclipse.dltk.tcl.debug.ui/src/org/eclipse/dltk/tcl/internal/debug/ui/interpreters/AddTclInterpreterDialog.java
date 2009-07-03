/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.tcl.internal.debug.ui.interpreters;

import java.util.Map;

import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterEnvironmentVariablesBlock;
import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterLibraryBlock;
import org.eclipse.dltk.internal.debug.ui.interpreters.AddScriptInterpreterDialog;
import org.eclipse.dltk.internal.debug.ui.interpreters.IAddInterpreterDialogRequestor;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.tcl.core.TclPackagesManager;
import org.eclipse.dltk.tcl.core.packages.VariableValue;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AddTclInterpreterDialog extends AddScriptInterpreterDialog {
	public AddTclInterpreterDialog(IAddInterpreterDialogRequestor requestor,
			Shell shell, IInterpreterInstallType[] interpreterInstallTypes,
			IInterpreterInstall editedInterpreter) {
		super(requestor, shell, interpreterInstallTypes, editedInterpreter);
	}

	@Override
	protected AbstractInterpreterLibraryBlock createLibraryBlock(
			AddScriptInterpreterDialog dialog) {
		return new TclInterpreterLibraryBlock(dialog);
	}

	@Override
	protected AbstractInterpreterEnvironmentVariablesBlock createEnvironmentVariablesBlock() {
		return new TclInterpreterEnvironmentVariablesBlock(this);
	}

	private GlobalVariableBlock globals;

	@Override
	protected void createDialogControls(Composite parent, int numColumns) {
		super.createDialogControls(parent, numColumns);

		Label l = new Label(parent, SWT.NONE);
		l.setText(TclInterpreterMessages.AddTclInterpreterDialog_0);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		l.setLayoutData(gd);

		globals = new GlobalVariableBlock(this);
		final Control globalBlock = globals.createControl(parent);
		final GridData blockGD = new GridData(GridData.FILL_BOTH);
		blockGD.horizontalSpan = numColumns;
		globalBlock.setLayoutData(blockGD);
	}

	@Override
	protected boolean useInterpreterArgs() {
		return false;
	}

	@Override
	protected boolean isRediscoverSupported() {
		return false;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		// Remove all information for packages infrastructure for this
		// interpreter.
		IInterpreterInstall install = getLastInterpreterInstall();
		if (install != null) {
			TclPackagesManager.removeInterpreterInfo(install);
		}
	}

	@Override
	protected String getDialogSettingsSectionName() {
		return "ADD_TCL_SCRIPT_INTERPRETER_DIALOG_SECTION"; //$NON-NLS-1$
	}

	@Override
	protected void initializeFields(IInterpreterInstall install) {
		super.initializeFields(install);
		if (install != null) {
			globals.setValues(TclPackagesManager.getVariables(install));
		} else {
			globals.setValues(ECollections.<String, VariableValue> emptyEMap());
		}
	}

	private static boolean equalsEMap(EMap<String, VariableValue> a,
			EMap<String, VariableValue> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (Map.Entry<String, VariableValue> entry : a.entrySet()) {
			final VariableValue value = b.get(entry.getKey());
			if (value == null) {
				return false;
			}
			if (!EcoreUtil.equals(entry.getValue(), value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void setFieldValuesToInterpreter(IInterpreterInstall install) {
		super.setFieldValuesToInterpreter(install);
		final EMap<String, VariableValue> newVars = globals.getValues();
		final EMap<String, VariableValue> oldVars = TclPackagesManager
				.getVariables(install);
		if (!equalsEMap(newVars, oldVars)) {
			TclPackagesManager.setVariables(install, newVars);
		}
	}
}
