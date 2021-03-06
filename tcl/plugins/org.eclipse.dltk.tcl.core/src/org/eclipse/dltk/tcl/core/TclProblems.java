/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.tcl.core;

import org.eclipse.dltk.compiler.problem.IProblemIdentifier;

public enum TclProblems implements IProblemIdentifier {

	UNKNOWN_REQUIRED_PACKAGE, UNKNOWN_REQUIRED_PACKAGE_CORRECTION, UNKNOWN_SOURCE, UNKNOWN_SOURCE_CORRECTION;

	public String contributor() {
		return TclPlugin.PLUGIN_ID;
	}

}
