package org.eclipse.dltk.tcl.activestatedebugger.spawnpoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;
import org.eclipse.dltk.core.builder.ISourceLineTracker;
import org.eclipse.dltk.tcl.activestatedebugger.TclActiveStateDebuggerConstants;
import org.eclipse.dltk.tcl.ast.StringArgument;
import org.eclipse.dltk.tcl.ast.TclArgument;
import org.eclipse.dltk.tcl.ast.TclCommand;
import org.eclipse.dltk.tcl.ast.TclModule;
import org.eclipse.dltk.tcl.internal.validators.TclBuildContext;
import org.eclipse.dltk.tcl.parser.TclParserUtils;
import org.eclipse.dltk.tcl.parser.TclVisitor;
import org.eclipse.dltk.utils.TextUtils;
import org.eclipse.osgi.util.NLS;

public class TclSpawnpointBuildParticipant implements IBuildParticipant {

	private final Set<String> spawnCommands = new HashSet<String>();

	public TclSpawnpointBuildParticipant() {
		spawnCommands.addAll(SpawnpointCommandManager.loadFromPreferences()
				.getSelectedCommands());
	}

	private static class SpawnpointInfo {
		Set<String> commands;
		int charStart;
		int charEnd;
	}

	private static class SpawnpointCollector extends TclVisitor {

		private static final String PROC_COMMAND = "proc"; //$NON-NLS-1$

		private final IBuildContext buildContext;
		private final Set<String> spawnCommands;

		public SpawnpointCollector(IBuildContext buildContext,
				Set<String> spawnCommands) {
			this.buildContext = buildContext;
			this.spawnCommands = spawnCommands;
		}

		private ISourceLineTracker lineTracker;

		private StringArgument getStringArgument(TclCommand command, int index) {
			if (index < command.getArguments().size()) {
				TclArgument argument = command.getArguments().get(index);
				if (argument instanceof StringArgument) {
					return (StringArgument) argument;
				}
			}
			return null;
		}

		@Override
		public boolean visit(TclCommand command) {
			if (PROC_COMMAND.equals(command.getQualifiedName())
					&& command.getArguments().size() == 3) {
				final StringArgument procName = getStringArgument(command, 0);
				if (procName != null
						&& spawnCommands.contains(procName.getValue())) {
					return false;
				}
			} else if (spawnCommands.contains(command.getQualifiedName())) {
				if (lineTracker == null) {
					lineTracker = buildContext.getLineTracker();
				}
				int lineNumber = lineTracker.getLineNumberOfOffset(command
						.getStart());
				addSpawnpoint(lineNumber, command.getQualifiedName(), command
						.getStart(), command.getEnd());
			}
			return true;
		}

		private final Map<Integer, SpawnpointInfo> spawnpoints = new HashMap<Integer, SpawnpointInfo>();

		private void addSpawnpoint(int lineNumber, String commandName,
				int start, int end) {
			Integer lineObj = Integer.valueOf(lineNumber);
			SpawnpointInfo info = spawnpoints.get(lineObj);
			if (info == null) {
				info = new SpawnpointInfo();
				info.commands = Collections.singleton(commandName);
				info.charStart = start;
				info.charEnd = end;
				spawnpoints.put(lineObj, info);
			} else {
				if (!info.commands.contains(commandName)) {
					final Set<String> commandNames = new HashSet<String>();
					commandNames.addAll(info.commands);
					commandNames.add(commandName);
					info.commands = commandNames;
				}
				if (start < info.charStart) {
					info.charStart = start;
				}
				if (end > info.charEnd) {
					info.charEnd = end;
				}
			}
		}
	}

	public void build(IBuildContext context) throws CoreException {
		if (context.getBuildType() == IBuildContext.RECONCILE_BUILD) {
			return;
		}
		final IFile file = context.getFile();
		if (file == null) {
			return;
		}
		TclModule tclModule = TclBuildContext.getStatements(context);
		List<TclCommand> commands = tclModule.getStatements();
		if (commands == null) {
			return;
		}
		SpawnpointCollector collector = new SpawnpointCollector(context,
				spawnCommands);
		TclParserUtils.traverse(commands, collector);
		file.deleteMarkers(
				TclActiveStateDebuggerConstants.SPAWNPOINT_MARKER_TYPE, true,
				IResource.DEPTH_ZERO);
		if (!collector.spawnpoints.isEmpty()) {
			for (Map.Entry<Integer, SpawnpointInfo> entry : collector.spawnpoints
					.entrySet()) {
				final IMarker marker = file
						.createMarker(TclActiveStateDebuggerConstants.SPAWNPOINT_MARKER_TYPE);
				final SpawnpointInfo info = entry.getValue();
				marker.setAttributes(
						new String[] { IMarker.LINE_NUMBER, IMarker.CHAR_START,
								IMarker.CHAR_END, IMarker.MESSAGE },
						new Object[] { entry.getKey(), info.charStart,
								info.charEnd, buildMessage(info) });
			}
		}
	}

	/**
	 * @param info
	 * @return
	 */
	private String buildMessage(SpawnpointInfo info) {
		return NLS
				.bind(
						TclSpawnpointMessages.participantMarkerMessage_template,
						TextUtils.join(info.commands, ','),
						info.commands.size() == 1 ? TclSpawnpointMessages.participantMarkerMessage_commandSingular
								: TclSpawnpointMessages.participantMarkerMessage_commandPlurar);
	}
}
