/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTEnumerationSpecifier extends CASTBaseDeclSpecifier implements
        ICASTEnumerationSpecifier {

    private IASTName name;

    
    public CASTEnumerationSpecifier() {
	}

	public CASTEnumerationSpecifier(IASTName name) {
		setName(name);
	}

	public void addEnumerator(IASTEnumerator enumerator) {
    	if (enumerator != null) {
    		enumerator.setParent(this);
			enumerator.setPropertyInParent(ENUMERATOR);
    		enumerators = (IASTEnumerator[]) ArrayUtil.append( IASTEnumerator.class, enumerators, ++enumeratorsPos, enumerator );
    	}
    }

    
    public IASTEnumerator[] getEnumerators() {        
        if( enumerators == null ) return IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
        enumerators = (IASTEnumerator[]) ArrayUtil.removeNullsAfter( IASTEnumerator.class, enumerators, enumeratorsPos );
        return enumerators;
    }

    private IASTEnumerator [] enumerators = null;
    private int enumeratorsPos=-1;

 
    public void setName(IASTName name) {
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATION_NAME);
		}
    }

    public IASTName getName() {
        return name;
    }

    public String getRawSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        IASTEnumerator[] etors = getEnumerators();
        for ( int i = 0; i < etors.length; i++ ) {
            if( !etors[i].accept( action ) ) return false;
        }
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public int getRoleForName(IASTName n ) {
		if( this.name == n  )
			return r_definition;
		return r_unclear;
	}

}
