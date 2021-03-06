package org.eclipse.dltk.tcl.internal.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.ast.expressions.StringLiteral;
import org.eclipse.dltk.ast.parser.AbstractSourceParser;
import org.eclipse.dltk.ast.parser.ISourceParser;
import org.eclipse.dltk.ast.parser.ISourceParserExtension;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.core.RuntimePerformanceMonitor;
import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
import org.eclipse.dltk.core.builder.ISourceLineTracker;
import org.eclipse.dltk.tcl.ast.ComplexString;
import org.eclipse.dltk.tcl.ast.Script;
import org.eclipse.dltk.tcl.ast.StringArgument;
import org.eclipse.dltk.tcl.ast.Substitution;
import org.eclipse.dltk.tcl.ast.TclArgument;
import org.eclipse.dltk.tcl.ast.TclArgumentList;
import org.eclipse.dltk.tcl.ast.TclCodeModel;
import org.eclipse.dltk.tcl.ast.TclCommand;
import org.eclipse.dltk.tcl.ast.TclModule;
import org.eclipse.dltk.tcl.ast.TclModuleDeclaration;
import org.eclipse.dltk.tcl.ast.TclStatement;
import org.eclipse.dltk.tcl.ast.VariableReference;
import org.eclipse.dltk.tcl.ast.expressions.TclBlockExpression;
import org.eclipse.dltk.tcl.ast.expressions.TclExecuteExpression;
import org.eclipse.dltk.tcl.core.ITclCommandDetector;
import org.eclipse.dltk.tcl.core.ITclCommandDetectorExtension;
import org.eclipse.dltk.tcl.core.ITclCommandProcessor;
import org.eclipse.dltk.tcl.core.ITclParser;
import org.eclipse.dltk.tcl.core.ITclSourceParser;
import org.eclipse.dltk.tcl.core.TclNature;
import org.eclipse.dltk.tcl.core.TclParseUtil;
import org.eclipse.dltk.tcl.core.TclPlugin;
import org.eclipse.dltk.tcl.core.ITclCommandDetector.CommandInfo;
import org.eclipse.dltk.tcl.core.ast.TclAdvancedExecuteExpression;
import org.eclipse.dltk.tcl.internal.parser.ext.CommandManager;
import org.eclipse.dltk.tcl.parser.TclErrorCollector;
import org.eclipse.dltk.tcl.parser.TclParser;
import org.eclipse.dltk.tcl.parser.definitions.DefinitionManager;
import org.eclipse.dltk.tcl.parser.definitions.NamespaceScopeProcessor;
import org.eclipse.dltk.tcl.parser.printer.SimpleCodePrinter;
import org.eclipse.dltk.utils.TextUtils;
import org.eclipse.emf.common.util.EList;

public class NewTclSourceParser extends AbstractSourceParser implements
		ITclParser, ISourceParser, ISourceParserExtension, ITclSourceParser {
	private IProblemReporter problemReporter;
	private String fileName;
	boolean useProcessors = true;
	private boolean useDetectors = true;

	private TclModuleDeclaration moduleDeclaration;
	// private TclModule tclModule;
	private ISourceLineTracker tracker;

	private Set<ASTNode> processedForContentNodes = new HashSet<ASTNode>();
	private NamespaceScopeProcessor coreProcessor = DefinitionManager
			.getInstance().createProcessor();;

	public ModuleDeclaration parse(String fileName, TclModule tclModule,
			IProblemReporter reporter) {
		processedForContentNodes.clear();
		initDetectors();
		// this.tclModule = tclModule;
		// TclCodeModel model = this.tclModule.getCodeModel();
		this.tracker = createLineTracker(tclModule);
		this.problemReporter = reporter;
		this.fileName = fileName;

		this.moduleDeclaration = new TclModuleDeclaration(tclModule.getSize());
		this.moduleDeclaration.setTclModule(tclModule);
		this.parse(tclModule, moduleDeclaration);
		return moduleDeclaration;
	}

	public static ISourceLineTracker createLineTracker(TclModule tclModule) {
		TclCodeModel model = tclModule.getCodeModel();
		EList<Integer> list = model.getLineOffsets();
		int[] offsets = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			offsets[i] = list.get(i);
		}
		EList<String> delimeters = model.getDelimeters();
		String[] delimetersAsArray = delimeters.toArray(new String[delimeters
				.size()]);
		return new TextUtils.DefaultSourceLineTracker(tclModule.getSize(),
				offsets, delimetersAsArray);
	}

	private void initDetectors() {
		if (detectors == null) {
			detectors = CommandManager.getInstance().getDetectors();
		}
	}

	private ITclCommandProcessor localProcessor = new ITclCommandProcessor() {
		public ASTNode process(TclStatement st, ITclParser parser,
				ASTNode parent) {
			if (parent != null) {
				TclParseUtil.addToDeclaration(parent, st);
				// Re process internal blocks.
			}
			return st;
		}

		public void setCurrentASTTree(ModuleDeclaration module) {
		}

		public void setDetectedParameter(Object parameter) {
		}
	};
	private ITclCommandDetector[] detectors;
	private int globalOffset;

	protected void parse(TclModule module, ASTNode decl) {
		processedForContentNodes.clear();
		initDetectors();

		List<TclCommand> commands = module.getStatements();
		processStatements(decl, commands);
	}

	private void processStatements(ASTNode decl, List<TclCommand> commands) {
		for (Iterator<TclCommand> iter = commands.iterator(); iter.hasNext();) {
			TclCommand command = iter.next();
			// Command handling
			TclStatement st = convertToAST(command);
			if (st == null) {
				continue; // could be null on errors in source code
			}
			runStatementProcessor(decl, st);
		}
	}

	private void runStatementProcessor(ASTNode decl, TclStatement st) {
		ITclCommandProcessor processor = this.locateProcessor(st, decl);
		if (processor != null) {
			try {
				ASTNode nde = processor.process(st, this, decl);
				if (nde == null) {
					nde = localProcessor.process(st, this, decl);
				}
				if (nde != null && this.useDetectors) {
					for (int i = 0; i < this.detectors.length; i++) {
						if (detectors[i] != null) {
							detectors[i].processASTNode(nde);
						}
					}
				}
				if (nde != null) {
					// Lets store some position information
					int globalOffset = this.globalOffset;
					boolean userProcessor = this.useProcessors;
					boolean useDetectors = this.useDetectors;
					nde.traverse(new ASTVisitor() {
						public boolean visit(TypeDeclaration s)
								throws Exception {
							if (processedForContentNodes.add(s)) {
								List stats = s.getStatements();
								processStatements(s, stats);
							}
							return true;
						}

						private void processStatements(ASTNode s, List stats) {
							List<ASTNode> statements = new ArrayList<ASTNode>(
									stats);
							stats.clear();
							for (ASTNode node : statements) {
								if (node instanceof TclStatement) {
									runStatementProcessor(s,
											(TclStatement) node);
								} else {
									stats.add(node);
								}
							}
						}

						public boolean visit(MethodDeclaration s)
								throws Exception {
							if (processedForContentNodes.add(s)) {
								List stats = s.getStatements();
								processStatements(s, stats);
							}
							return true;
						}

						public boolean visit(Expression s) throws Exception {
							if (s instanceof Block) {
								if (processedForContentNodes.add(s)) {
									Block bl = (Block) s;
									List stats = bl.getStatements();
									processStatements(bl, stats);
								}
								return true;
							} else if (s instanceof TclAdvancedExecuteExpression) {
								if (processedForContentNodes.add(s)) {
									TclAdvancedExecuteExpression ex = (TclAdvancedExecuteExpression) s;
									List stats = ex.getStatements();
									processStatements(ex, stats);
								}
							} else if (s instanceof TclExecuteExpression) {
								// This should not happen at all.
							}
							return true;
						}
					});
					// Restore values
					this.useDetectors = useDetectors;
					this.useProcessors = userProcessor;
					this.globalOffset = globalOffset;
				}
			} catch (Exception e) {
				TclPlugin.error(e);
			}
		}

	}

	private TclStatement convertToAST(TclCommand command) {
		List<ASTNode> expressions = new ArrayList<ASTNode>();
		expressions.add(convertToAST(command.getName()));
		for (TclArgument arg : command.getArguments()) {
			expressions.add(convertToAST(arg));
		}
		return new TclStatement(expressions);
	}

	private ASTNode convertToAST(TclArgument arg) {
		if (arg instanceof StringArgument) {
			// Simple absolute or relative source'ing.
			StringArgument argument = (StringArgument) arg;
			String value = argument.getValue();
			return stringToAST(argument, value);
		} else if (arg instanceof ComplexString) {
			ComplexString carg = (ComplexString) arg;
			return stringToAST(carg, SimpleCodePrinter.getArgumentString(carg,
					carg.getStart()));
		} else if (arg instanceof Script) {
			Script st = (Script) arg;
			EList<TclCommand> eList = st.getCommands();
			Block block = new Block(st.getStart(), st.getEnd());
			for (TclCommand tclArgument : eList) {
				TclStatement stat = convertToAST(tclArgument);
				block.addStatement(stat);
			}
			return block;

		} else if (arg instanceof VariableReference) {
			VariableReference variableReference = (VariableReference) arg;
			String content = SimpleCodePrinter.getArgumentString(
					variableReference, variableReference.getStart());
			return new SimpleReference(arg.getStart(), arg.getEnd(), content);
		} else if (arg instanceof Substitution) {
			Substitution st = (Substitution) arg;
			EList<TclCommand> eList = st.getCommands();
			TclAdvancedExecuteExpression block = new TclAdvancedExecuteExpression(
					st.getStart(), st.getEnd());
			for (TclCommand cmd : eList) {
				TclStatement stat = convertToAST(cmd);
				block.addStatement(stat);
			}
			// block.acceptStatements(exprs);
			return block;
		} else if (arg instanceof TclArgumentList) {
			TclArgumentList st = (TclArgumentList) arg;
			String str = SimpleCodePrinter.getArgumentString(st, st.getStart());
			return stringToAST(st, str);
		}
		throw new RuntimeException(
				"TODO: Not all cases are matched in TCL Parser AST converter");
	}

	private ASTNode stringToAST(TclArgument argument, String value) {
		int slen = value.length();
		int end = argument.getEnd();
		int start = argument.getStart();
		if (slen >= 2
				&& value.charAt(0) == '{'
				&& (value.charAt(slen - 1) == '}' || (moduleDeclaration != null && end == moduleDeclaration
						.sourceEnd()))) {
			// This is block argument
			TclBlockExpression bl = new TclBlockExpression(start, end, value);
			bl.setProcessedArgument(argument);
			return bl;
		} else if (slen >= 2
				&& value.charAt(0) == '"'
				&& (value.charAt(slen - 1) == '"' || end == moduleDeclaration
						.sourceEnd())) {
			// This is string literal
			return new StringLiteral(start, end, value);
		} else {
			int len = end - start;
			if (value.length() > len) {
				value = value.substring(0, len);
			}
			// Simple reference
			return new SimpleReference(start, end, value);
		}
	}

	private ITclCommandProcessor locateProcessor(TclStatement command,
			ASTNode decl) {
		if (this.useProcessors == false) {
			return localProcessor;
		}

		if (command != null && command.getCount() > 0) {
			Expression expr = command.getAt(0);
			if (!(expr instanceof SimpleReference)) {
				return localProcessor;
			}
			String name = ((SimpleReference) expr).getName();
			if (name.startsWith("::")) {
				name = name.substring(2);
			}

			ITclCommandProcessor processor = CommandManager.getInstance()
					.getProcessor(name);
			if (processor == null) {
				// advanced command detection.
				if (this.useDetectors) {
					for (int i = 0; i < detectors.length; i++) {
						if (detectors[i] == null) {
							continue;
						}
						if (detectors[i] instanceof ITclCommandDetectorExtension) {
							((ITclCommandDetectorExtension) detectors[i])
									.setBuildRuntimeModelFlag(false);
						}
						CommandInfo commandName = detectors[i].detectCommand(
								command, this.moduleDeclaration, decl);
						if (commandName != null) {
							processor = CommandManager.getInstance()
									.getProcessor(commandName.commandName);
							if (processor != null) {
								processor
										.setDetectedParameter(commandName.parameter);
							}
							break;
						}
					}
				}
			}
			if (processor != null) {
				processor.setCurrentASTTree(this.moduleDeclaration);
				return processor;
			}
		}
		return this.localProcessor;
	}

	public IProblemReporter getProblemReporter() {
		return this.problemReporter;
	}

	public String getFileName() {
		return this.fileName;
	}

	public int getStartPos() {
		return 0;
	}

	public void setProcessorsState(boolean state) {
		this.useProcessors = state;
	}

	public void setUseDetectors(boolean b) {
		this.useDetectors = false;
	}

	public ISourceLineTracker getCodeModel() {
		return tracker;
	}

	/**
	 * Assume parsing are in same module.
	 */
	public void parse(String content, int offset, ASTNode parent) {
		PerformanceNode p = RuntimePerformanceMonitor.begin();
		initDetectors();
		processedForContentNodes.clear();
		TclParser newParser = new TclParser();
		TclErrorCollector collector = null;
		if (problemReporter != null) {
			collector = new TclErrorCollector();
		}
		newParser.setGlobalOffset(offset);
		List<TclCommand> module = newParser.parse(content, collector,
				coreProcessor);
		if (problemReporter != null) {
			collector.reportAll(problemReporter, tracker);
		}
		processStatements(parent, module);
		p.done(TclNature.NATURE_ID, "New tcl parser: Parse of code", content
				.length());
	}

	public ModuleDeclaration parse(IModuleSource input,
			final IProblemReporter reporter) {
		PerformanceNode node = RuntimePerformanceMonitor.begin();
		processedForContentNodes.clear();
		this.problemReporter = reporter;
		TclParser newParser = new TclParser();
		TclErrorCollector collector = null;
		if (reporter != null) {
			collector = new TclErrorCollector();
		}
		newParser.setGlobalOffset(globalOffset);
		String content = input.getSourceContents();
		TclModule module = newParser.parseModule(content, collector,
				coreProcessor);
		// TODO: Add error passing to reporter here.
		ModuleDeclaration result = parse(input.getFileName(), module, reporter);
		if (collector != null) {
			collector.reportAll(reporter, tracker);
		}

		node.done(TclNature.NATURE_ID, "new tcl source parser:time", content
				.length());
		return result;
	}

	public void setFlags(int flags) {
	}

	public void setOffset(int offset) {
		this.globalOffset = offset;
	}

	public void parse(Script script, Block bll) {
		PerformanceNode p = RuntimePerformanceMonitor.begin();
		processedForContentNodes.clear();
		processStatements(bll, script.getCommands());
		p.done(TclNature.NATURE_ID, "New tcl parser: Parse of block", 0);
	}
}
