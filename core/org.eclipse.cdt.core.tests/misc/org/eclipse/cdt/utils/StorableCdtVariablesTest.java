/*******************************************************************************
 * Copyright (c) 2012 Veaceslav Bacu (Freescale Semiconductor Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Veaceslav Bacu (Freescale Semiconductor Inc.) - initial API and implementation (bug 348884)
 *    
 *******************************************************************************/ 

package org.eclipse.cdt.utils;

import java.util.ConcurrentModificationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.IUserVarSupplier;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariable;
import org.eclipse.core.resources.IProject;

public class StorableCdtVariablesTest extends TestCase {

	public static Test suite() {
		return new TestSuite(StorableCdtVariablesTest.class);
	}
	
	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}
	
	/*
	 * Unit test for Bugzilla #348884
	 */
	public void testSetMacros() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig("projectWithUserVars"); //$NON-NLS-1$

		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();

		StorableCdtVariable varA1 = new StorableCdtVariable("A1", ICdtVariable.VALUE_TEXT, "a1"); //$NON-NLS-1$ //$NON-NLS-2$
		StorableCdtVariable varA2 = new StorableCdtVariable("A2", ICdtVariable.VALUE_TEXT, "a2"); //$NON-NLS-1$ //$NON-NLS-2$
		StorableCdtVariable varA3 = new StorableCdtVariable("A3", ICdtVariable.VALUE_TEXT, "a3"); //$NON-NLS-1$ //$NON-NLS-2$
		StorableCdtVariable varA4 = new StorableCdtVariable("A4", ICdtVariable.VALUE_TEXT, "a4"); //$NON-NLS-1$ //$NON-NLS-2$
		StorableCdtVariable varA5 = new StorableCdtVariable("A5", ICdtVariable.VALUE_TEXT, "a5"); //$NON-NLS-1$ //$NON-NLS-2$
		
		IUserVarSupplier supplier = CCorePlugin.getUserVarSupplier();
		
		try{
			supplier.setMacros(new ICdtVariable[]{varA1, varA2, varA3, varA4}, desc);
		}catch(Throwable e){
			fail("1.0 Cannot set macros"); //$NON-NLS-1$
		}
		
		try{
			supplier.setMacros(new ICdtVariable[]{varA1, varA2, varA5}, desc);
		}catch(ConcurrentModificationException e){
			fail("1.1 Bugzilla #348884 unresolved"); //$NON-NLS-1$
		}catch(Exception e){
			fail("1.2 Cannot set macros"); //$NON-NLS-1$
		}
	}

}
