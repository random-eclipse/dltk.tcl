/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/

package org.eclipse.dltk.tcl.activestatedebugger.preferences;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.debug.ui.preferences.ExternalDebuggingEngineOptionsBlock;
import org.eclipse.dltk.debug.ui.preferences.ScriptDebugPreferencesMessages;
import org.eclipse.dltk.internal.corext.util.Messages;
import org.eclipse.dltk.internal.ui.dialogs.StatusInfo;
import org.eclipse.dltk.tcl.activestatedebugger.TclActiveStateDebuggerConstants;
import org.eclipse.dltk.tcl.activestatedebugger.TclActiveStateDebuggerPlugin;
import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage;
import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
import org.eclipse.dltk.ui.preferences.IFieldValidator;
import org.eclipse.dltk.ui.preferences.PreferenceKey;
import org.eclipse.dltk.ui.preferences.ValidatorMessages;
import org.eclipse.dltk.ui.util.IStatusChangeListener;
import org.eclipse.dltk.ui.util.SWTFactory;
import org.eclipse.dltk.utils.PlatformFileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Tcl ActiveState debugging engine preference page
 */
public class TclActiveStateDebuggerPreferencePage extends
		AbstractConfigurationBlockPropertyAndPreferencePage {

	static PreferenceKey ENGINE_PATH = new PreferenceKey(
			TclActiveStateDebuggerPlugin.PLUGIN_ID,
			TclActiveStateDebuggerConstants.DEBUGGING_ENGINE_PATH_KEY);

	static PreferenceKey PDX_PATH = new PreferenceKey(
			TclActiveStateDebuggerPlugin.PLUGIN_ID,
			TclActiveStateDebuggerConstants.DEBUGGING_ENGINE_PDX_PATH_KEY);

	private static String PREFERENCE_PAGE_ID = "org.eclipse.dltk.tcl.preferences.debug.activestatedebugger";
	private static String PROPERTY_PAGE_ID = "org.eclipse.dltk.tcl.propertyPage.debug.engines.activestatedebugger";

	private static class NonEmptyFilePathValidator implements IFieldValidator {
		public IStatus validate(String text) {
			StatusInfo status = new StatusInfo();

			if (!(text.trim().length() == 0)) {
				File file = PlatformFileUtils
						.findAbsoluteOrEclipseRelativeFile(Path.fromOSString(
								text).toFile());

				if (!file.exists()) {
					status.setError(Messages.format(
							ValidatorMessages.FilePathNotExists, text));
				} else if (!file.isDirectory()) {
					status.setError(Messages.format(
							ValidatorMessages.FilePathIsInvalid, text));
				}
			}

			return status;
		}
	}

	/*
	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#createOptionsBlock(org.eclipse.dltk.ui.util.IStatusChangeListener,
	 *      org.eclipse.core.resources.IProject,
	 *      org.eclipse.ui.preferences.IWorkbenchPreferenceContainer)
	 */
	protected AbstractOptionsBlock createOptionsBlock(
			IStatusChangeListener newStatusChangedListener, IProject project,
			IWorkbenchPreferenceContainer container) {

		return new ExternalDebuggingEngineOptionsBlock(
				newStatusChangedListener, project, new PreferenceKey[] {
						ENGINE_PATH, PDX_PATH }, container) {
			private Text pdxPath;

			protected void createEngineBlock(Composite parent) {

				super.createEngineBlock(parent);

				createPDXGroup(parent);

				addDownloadLink(parent,
						PreferenceMessages.DebuggingEngineDownloadPage,
						PreferenceMessages.DebuggingEngineDownloadPageLink);
			}

			private void createPDXGroup(final Composite parent) {

				final Group group = SWTFactory.createGroup(parent,
						PreferenceMessages.DebuggingEnginePDXGroup, 3, 1,
						GridData.FILL_HORIZONTAL);

				// Engine path
				SWTFactory.createLabel(group,
						ScriptDebugPreferencesMessages.PathLabel, 1);

				pdxPath = SWTFactory.createText(group, SWT.BORDER, 1, "");
				bindControl(pdxPath, PDX_PATH, new NonEmptyFilePathValidator());

				// Browse
				final Button button = SWTFactory.createPushButton(group,
						ScriptDebugPreferencesMessages.BrowseEnginePath, null);
				button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog dialog = new DirectoryDialog(parent
								.getShell(), SWT.OPEN);
						String file = dialog.open();
						if (file != null) {
							pdxPath.setText(file);
						}
					}
				});

			}

			protected PreferenceKey getDebuggingEnginePathKey() {
				return ENGINE_PATH;
			}
		};
	}

	/*
	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#getHelpId()
	 */
	protected String getHelpId() {
		return null;
	}

	/*
	 * @see org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageId()
	 */
	protected String getPreferencePageId() {
		return PREFERENCE_PAGE_ID;
	}

	/*
	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#getProjectHelpId()
	 */
	protected String getProjectHelpId() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @see org.eclipse.dltk.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageId()
	 */
	protected String getPropertyPageId() {
		return PROPERTY_PAGE_ID;
	}

	/*
	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#setDescription()
	 */
	protected void setDescription() {
		setDescription(PreferenceMessages.DebuggingEngineDescription);
	}

	/*
	 * @see org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPropertyAndPreferencePage#setPreferenceStore()
	 */
	protected void setPreferenceStore() {
		setPreferenceStore(TclActiveStateDebuggerPlugin.getDefault()
				.getPreferenceStore());
	}
}
