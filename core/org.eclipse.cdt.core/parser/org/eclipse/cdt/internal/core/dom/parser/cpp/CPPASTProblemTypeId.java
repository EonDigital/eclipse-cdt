/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;

/**
 * @author jcamelon
 */
public class CPPASTProblemTypeId extends CPPASTTypeId implements IASTProblemTypeId {

	private IASTProblem problem;

    
    public CPPASTProblemTypeId() {
	}

	public CPPASTProblemTypeId(IASTProblem problem) {
		setProblem(problem);
	}

	public IASTProblem getProblem() {
        return problem;
    }
    
    public void setProblem(IASTProblem p) {
        problem = p;
        if (p != null) {
			p.setParent(this);
			p.setPropertyInParent(PROBLEM);
		}
    }

}
