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
/**
 * 
 */
package org.eclipse.dltk.tcl.internal.ui.wizards;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.tcl.core.TclNature;
import org.eclipse.dltk.ui.wizards.ProjectWizardFirstPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

final class TclProjectWizardFirstPage extends ProjectWizardFirstPage {
	TclInterpreterGroup fInterpreterGroup;
	private Button useAnalysis;
	private Group packagesGroup;
	private Label labelElement;

	final class TclInterpreterGroup extends AbstractInterpreterGroup {

		public TclInterpreterGroup(Composite composite) {
			super(composite);
		}

		protected String getCurrentLanguageNature() {
			return TclNature.NATURE_ID;
		}

		protected String getIntereprtersPreferencePageId() {
			return "org.eclipse.dltk.tcl.preferences.interpreters";
		}
	}

	protected void createInterpreterGroup(Composite parent) {
		fInterpreterGroup = new TclInterpreterGroup(parent);
	}

	protected Observable getInterpreterGroupObservable() {
		return fInterpreterGroup;
	}

	protected boolean supportInterpreter() {
		return true;
	}

	protected IInterpreterInstall getInterpreter() {
		return fInterpreterGroup.getSelectedInterpreter();
	}

	protected void handlePossibleInterpreterChange() {
		fInterpreterGroup.handlePossibleInterpreterChange();
	}

	protected boolean interpeterRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	protected void createCustomGroups(Composite composite) {
		super.createCustomGroups(composite);

		packagesGroup = new Group(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL, SWT.FILL, true, false);
		gridData.widthHint = convertWidthInCharsToPixels(50);
		packagesGroup.setLayoutData(gridData);
		packagesGroup.setLayout(new GridLayout(1, false));
		packagesGroup.setText("Packages detector");
		this.useAnalysis = new Button(packagesGroup, SWT.CHECK);
		this.useAnalysis.setText("Detect packages from source code");
		this.useAnalysis.setSelection(true);
		labelElement = new Label(packagesGroup, SWT.NONE);
		labelElement
				.setText("Enable package detection for Remote Projects could be realy slow...");
		Observer o = new Observer() {
			public void update(Observable o, Object arg) {
				boolean inWorkspace = fLocationGroup.isInWorkspace();
				packagesGroup.setEnabled(!inWorkspace);
				useAnalysis.setEnabled(!inWorkspace);
				labelElement.setEnabled(!inWorkspace);
			}
		};
		fLocationGroup.addObserver(o);
		fInterpreterGroup.addObserver(o);
	}

	public boolean useAnalysis() {
		final boolean result[] = { false };
		useAnalysis.getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = useAnalysis.getSelection();
			}
		});
		return result[0];
	}
}