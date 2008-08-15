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

package org.eclipse.dltk.tcl.parser.tests;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.dltk.tcl.ast.Script;
import org.eclipse.dltk.tcl.ast.TclArgument;
import org.eclipse.dltk.tcl.ast.TclCommand;
import org.eclipse.dltk.tcl.definitions.Scope;
import org.eclipse.dltk.tcl.parser.TclErrorCollector;
import org.eclipse.dltk.tcl.parser.TclParser;
import org.eclipse.dltk.tcl.parser.definitions.DefinitionLoader;
import org.eclipse.emf.common.util.EList;

public class IfCommandTests extends TestCase {
	TestScopeProcessor processor = new TestScopeProcessor();

	
	public void test001() throws Exception {
		String source = "if {} {set a 20}";
		typedCheck(source, 0, 1);
	}

	
	public void test002() throws Exception {
		String source = "if {} then {set a 20}";
		typedCheck(source, 0, 1);
	}

	
	public void test003() throws Exception {
		String source = "if {} {set a 20} elseif {} {set a 20}";
		typedCheck(source, 0, 2);
	}

	
	public void test004() throws Exception {
		String source = "if {} {set a 20} elseif {} then {set a 20}";
		typedCheck(source, 0, 2);
	}

	
	public void test005() throws Exception {
		String source = "if {} {set a 20} elseif {} {set a 20} elseif {} {set a 20}";
		typedCheck(source, 0, 3);
	}

	
	public void test006() throws Exception {
		String source = "if {} {set a 20} elseif {} {set a 20} else {set a 20}";
		typedCheck(source, 0, 3);
	}

	
	public void test007() throws Exception {
		String source = "if {} {set a 20} elseif {} {set a 20} else {set a 20}";
		typedCheck(source, 0, 3);
	}

	// ----------------------------------------------------------------------

	
	public void test008() throws Exception {
		String source = "if {} {set a 20} {set a 20}";
		typedCheck(source, 0, 2);
	}

	
	public void test009() throws Exception {
		String source = "if {} {set a 20} else {set a 20} else {set a 20}";
		typedCheck(source, 1, 0);
	}

	
	public void test010_FAILED() throws Exception {
		String source = "if {} {set a 20} elseif";
		typedCheck(source, 0, 2);
	}

	
	public void test011() throws Exception {
		String source = "if {} {set a 20} elseif {}";
		typedCheck(source, 1, 0);
	}

	
	public void test012() throws Exception {
		String source = "if {$a} then {} else if";
		typedCheck(source, 1, 2);
	}

	private void typedCheck(String source, int errs, int code) throws Exception {
		Scope scope = DefinitionLoader
				.loadDefinitions(new URL(
						"platform:///plugin/org.eclipse.dltk.tcl.tcllib/definitions/builtin.xml"));
		TestCase.assertNotNull(scope);
		processor.add(scope);
		TclParser parser = new TclParser();
		TclErrorCollector errors = new TclErrorCollector();
		List<TclCommand> module = parser.parse(source, errors, processor);
		TestCase.assertEquals(1, module.size());
		TclCommand tclCommand = module.get(0);
		EList<TclArgument> arguments = tclCommand.getArguments();
		int scripts = 0;
		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i) instanceof Script) {
				scripts++;
			}
		}
		if (errors.getCount() > 0) {
			TestUtils.outErrors(source, errors);
		}
		TestCase.assertEquals(code, scripts);
		TestCase.assertEquals(errs, errors.getCount());
	}
}
