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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTDeclarationStatement extends CPPASTNode implements
        IASTDeclarationStatement, IASTAmbiguityParent {

    private IASTDeclaration declaration;

    
    public CPPASTDeclarationStatement() {
	}

	public CPPASTDeclarationStatement(IASTDeclaration declaration) {
		setDeclaration(declaration);
	}

	public IASTDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(IASTDeclaration declaration) {
        this.declaration = declaration;
        if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(DECLARATION);
		}
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( declaration != null ) if( !declaration.accept( action ) ) return false;
        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( declaration == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            declaration = (IASTDeclaration) other;
        }
    }
}
