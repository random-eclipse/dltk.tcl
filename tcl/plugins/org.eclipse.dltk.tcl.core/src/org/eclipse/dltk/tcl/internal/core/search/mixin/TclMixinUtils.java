package org.eclipse.dltk.tcl.internal.core.search.mixin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.RuntimePerformanceMonitor;
import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
import org.eclipse.dltk.core.mixin.IMixinElement;
import org.eclipse.dltk.tcl.internal.core.search.mixin.model.ITclMixinElement;

public class TclMixinUtils {
	private static final boolean TRACE_COMPLETION_TIME = false;

	/**
	 * @since 2.0
	 */
	public static IModelElement[] findModelElementsFromMixin(String pattern,
			Class mixinClass, IScriptProject project, IProgressMonitor monitor) {
		PerformanceNode p = RuntimePerformanceMonitor.begin();
		long time = System.currentTimeMillis();
		List<IModelElement> elements = new ArrayList<IModelElement>();
		IMixinElement[] find = TclMixinModel.getInstance().getMixin(project)
				.find(pattern, monitor);
		if (find == null) {
			return new IModelElement[0];
		}
		if (TRACE_COMPLETION_TIME) {
			System.out.println("findMethod from mixin: request model:"
					+ Long.toString(System.currentTimeMillis() - time) + ":"
					+ pattern);
		}
		time = System.currentTimeMillis();
		for (int i = 0; i < find.length; i++) {
			Object[] allObjects = find[i].getAllObjects();
			for (int j = 0; j < allObjects.length; j++) {
				if (allObjects[j] != null
						&& mixinClass.isInstance(allObjects[j])) {
					ITclMixinElement field = (ITclMixinElement) allObjects[j];
					IModelElement element = field.getModelElement();
					if (element != null) {
						elements.add(element);
					}
				}
			}
		}
		p.done("Tcl", "Find elements in mixin", 0);
		return elements.toArray(new IModelElement[elements.size()]);
	}
}
