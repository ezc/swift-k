/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class CloseDataset extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);
	
	private ArgRef<DSHandle> var;
	private ArgRef<Object> path;
	private ArgRef<Boolean> childrenOnly;
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("var", optional("path", Path.EMPTY_PATH), optional("childrenOnly", Boolean.FALSE)));
    }

	public Object function(Stack stack) {
		Path path = parsePath(this.path.getValue(stack));
		DSHandle var = this.var.getValue(stack);
		if (var == null) {
		    return null;
		}
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing " + var);
			}
			var = var.getField(path);
			
			if (childrenOnly.getValue(stack)) {
			    closeChildren((AbstractDataNode) var);
			}
			else {
			    var.closeDeep();
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(this, e);
		}
		return null;
	}
}
