/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.tcl.tests.model;

import junit.framework.Test;

import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.tests.model.AbstractModelTests;
import org.eclipse.dltk.tcl.internal.parser.TclSourceParser;
import org.eclipse.dltk.tcl.tests.TclTestsPlugin;
import org.eclipse.dltk.utils.CorePrinter;


public class TclASTBuildTests extends AbstractModelTests
{
	public TclASTBuildTests(String name) {
		super( TclTestsPlugin.PLUGIN_NAME, name);
	}
	
	public static Test suite() {
		return new Suite( TclASTBuildTests.class);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();		
	}
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
	}
	
	public void testBuildExtendedAST001() throws Exception {
		String prj = "prj1";
		//IDLTKProject project = setUpScriptProject( prj );
		
		ISourceModule module = this.getSourceModule( prj, "src", new Path("module0.tcl") );
		
		String source = module.getSource();
		
		TclSourceParser parser = new TclSourceParser();
		ModuleDeclaration decl = parser.parse(source.toCharArray(), null);
		CorePrinter printer = new CorePrinter(System.out, true);
		decl.printNode(printer);
		
		//TypeDeclaration[] types = decl.getTypes();
		decl.printNode(printer);
		
		
		deleteProject( prj );
	}
}
