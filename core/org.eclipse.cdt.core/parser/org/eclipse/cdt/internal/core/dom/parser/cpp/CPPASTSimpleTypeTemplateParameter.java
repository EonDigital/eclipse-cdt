/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;

/**
 * @author jcamelon
 */
public class CPPASTSimpleTypeTemplateParameter extends CPPASTNode implements
        ICPPASTSimpleTypeTemplateParameter {

    private int type;
    private IASTName name;
    private IASTTypeId typeId;


    public CPPASTSimpleTypeTemplateParameter() {
	}

	public CPPASTSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId) {
		this.type = type;
		setName(name);
		setDefaultType(typeId);
	}

	public int getParameterType() {
        return type;
    }

    public void setParameterType(int value) {
        this.type = value;
    }

    public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(PARAMETER_NAME);
		}
    }

    public IASTTypeId getDefaultType() {
        return typeId;
    }

    public void setDefaultType(IASTTypeId typeId) {
        this.typeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(DEFAULT_TYPE);
		}
    }
    
    public boolean accept( ASTVisitor action ){
        if( action instanceof CPPASTVisitor &&
            ((CPPASTVisitor)action).shouldVisitTemplateParameters ){
		    switch( ((CPPASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( name != null ) if( !name.accept( action ) ) return false;
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        
        if( action instanceof CPPASTVisitor &&
                ((CPPASTVisitor)action).shouldVisitTemplateParameters ){
    		    switch( ((CPPASTVisitor)action).leave( this ) ){
    	            case ASTVisitor.PROCESS_ABORT : return false;
    	            case ASTVisitor.PROCESS_SKIP  : return true;
    	            default : break;
    	        }
    		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_declaration;
		return r_unclear;
	}
}
