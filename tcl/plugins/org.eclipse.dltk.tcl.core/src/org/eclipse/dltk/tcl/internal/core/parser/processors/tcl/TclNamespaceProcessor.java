package org.eclipse.dltk.tcl.internal.core.parser.processors.tcl;

import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.compiler.problem.ProblemSeverities;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.tcl.ast.TclStatement;
import org.eclipse.dltk.tcl.ast.expressions.TclBlockExpression;
import org.eclipse.dltk.tcl.core.AbstractTclCommandProcessor;
import org.eclipse.dltk.tcl.core.ITclParser;
import org.eclipse.dltk.tcl.core.TclParseUtil;

public class TclNamespaceProcessor extends AbstractTclCommandProcessor {
	private ASTNode findRealParent(ASTNode node) {
		List levels = TclParseUtil.findLevelsTo(this.getModuleDeclaration(),
				node);
		for (int i = levels.size() - 1; i >= 0; --i) {
			ASTNode n = (ASTNode) levels.get(i);
			if (n instanceof MethodDeclaration || n instanceof TypeDeclaration
					|| n instanceof ModuleDeclaration) {
				return n;
			}
		}
		return null;
	}

	public ASTNode process(TclStatement statement, ITclParser parser,
			ASTNode parent) {
		Expression nameSpaceArg = statement.getAt(1);
		if (nameSpaceArg == null || !(nameSpaceArg instanceof SimpleReference)) {
			this.report(parser, "Syntax error: a namespace name expected.",
					statement, ProblemSeverities.Error);
			if (DLTKCore.DEBUG) {
				System.err
						.println("tcl: namespace argument is null or not simple reference");
			}
			// continue;
		}

		if (!(nameSpaceArg instanceof SimpleReference)) {
			return null;
		}
		String sNameSpaceArg = ((SimpleReference) nameSpaceArg).getName();

		if (sNameSpaceArg.equals("eval")) {
			Expression nameSpaceName = statement.getAt(2);
			if (!(nameSpaceName instanceof SimpleReference)) {
				return null;
			}
			String sNameSpaceName = ((SimpleReference) nameSpaceName).getName();
			if (nameSpaceName == null
					|| !(nameSpaceName instanceof SimpleReference)) {
				this.report(parser, "Syntax error: namespace name expected",
						statement, ProblemSeverities.Error);
				// continue;
				// by now, just ignore
				return null;
			}
			final int FIRST_ARGUMENT_POSITION = 3;
			if (statement.getCount() < 4) {
				return null;
			}

			// List statements = new ArrayList(statement.getCount() -
			// FIRST_ARGUMENT_POSITION);
			int start = statement.getAt(FIRST_ARGUMENT_POSITION).sourceStart();
			int end = statement.getAt(statement.getCount() - 1).sourceEnd();
			Block code = new Block(start, end);
			TypeDeclaration type = new TypeDeclaration(sNameSpaceName,
					nameSpaceName.sourceStart(), nameSpaceName.sourceEnd(),
					statement.sourceStart(), statement.sourceEnd());
			type.setModifiers(Modifiers.AccNameSpace);
			ASTNode realParent = findRealParent(parent);
			if (realParent instanceof TypeDeclaration) {
				TypeDeclaration t = ((TypeDeclaration) realParent);
				type.setEnclosingTypeName(t.getEnclosingTypeName() + "$"
						+ t.getName());
			}
			addToParent(parent, type);
			type.setBody(code);
			for (int i = FIRST_ARGUMENT_POSITION; i < statement.getCount(); i++) {
				Expression expr = statement.getAt(i);
				if (expr == null) {
					return null;
				}
				if (expr instanceof Block) {
					code.getStatements().addAll(((Block) expr).getStatements());
				} else if (expr instanceof TclBlockExpression) {
					TclBlockExpression block = (TclBlockExpression) expr;
					String blockContent = block.getBlock();
					if (blockContent.length() > 2) {
						blockContent = blockContent.substring(1, blockContent
								.length() - 1);
						parser.parse(blockContent, block.sourceStart() + 1
								- parser.getStartPos(), code);
					}
					// code.getStatements().addAll(bl.getStatements());
				} else {
					code.getStatements().add(expr);
				}
			}

			return type;
		}
		return null;
	}
}
