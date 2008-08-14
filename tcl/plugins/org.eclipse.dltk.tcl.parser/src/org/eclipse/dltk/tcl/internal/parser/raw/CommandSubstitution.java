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
package org.eclipse.dltk.tcl.internal.parser.raw;

public class CommandSubstitution extends TclElement implements ISubstitution {

	private TclScript script;

	public TclScript getScript() {
		return script;
	}

	public static boolean iAm(CodeScanner input) {
		int c = input.read();
		if (c == -1)
			return false;
		input.unread();
		return (c == '[');
	}

	public boolean readMe(CodeScanner input, SimpleTclParser parser) throws TclParseException {
		if (!iAm(input))
			return false;
		setStart(input.getPosition());
		input.read();
		this.script = parser.parse(input, true);
		setEnd(input.getPosition() - (input.isEOF() ? 0 : 1));
		return true;
	}
}
