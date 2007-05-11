/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.tcl.ast;

import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.tcl.internal.parser.TclASTBuilder;

public class TclModuleDeclaration extends ModuleDeclaration {
	public TclModuleDeclaration(int sourceLength) {
		super(sourceLength, true);
	}

	protected void doRebuild() {
		TclASTBuilder.buildAST(this, getTypeList(), getFunctionList(), getVariablesList());
	}

	public void rebuildMethods() {
		TclASTBuilder.rebuildMethods(this);
	}
}
