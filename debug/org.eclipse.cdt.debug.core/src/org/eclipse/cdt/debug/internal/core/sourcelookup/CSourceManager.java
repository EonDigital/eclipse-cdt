/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Locates sources for a C/C++ debug session.
 * 
 * @since: Oct 8, 2002
 */
public class CSourceManager implements ICSourceLocator, 
									   IPersistableSourceLocator, 
									   ISourceMode, 
									   IAdaptable
{
	private ISourceLocator fSourceLocator = null;
	private int fMode = ISourceMode.MODE_SOURCE;
	private int fRealMode = fMode;
	private ILaunch fLaunch = null;
	private CDebugTarget fDebugTarget = null; 
	
	/**
	 * Constructor for CSourceManager.
	 */
	public CSourceManager( ISourceLocator sourceLocator )
	{
		setSourceLocator( sourceLocator );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getLineNumber(IStackFrameInfo)
	 */
	public int getLineNumber( IStackFrame frame )
	{
		if ( getRealMode() == ISourceMode.MODE_SOURCE )
		{
			if ( getCSourceLocator() != null )
			{
				return getCSourceLocator().getLineNumber( frame );
			}
			IStackFrameInfo info = (IStackFrameInfo)frame.getAdapter( IStackFrameInfo.class );
			if ( info != null )
			{
				return info.getFrameLineNumber();
			}
		}
		if ( getRealMode() == ISourceMode.MODE_DISASSEMBLY && getDisassemblyManager( frame ) != null )
		{
			return getDisassemblyManager( frame ).getLineNumber( frame );
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getSourceLocations()
	 */
	public ICSourceLocation[] getSourceLocations()
	{
		return ( getCSourceLocator() != null ) ? getCSourceLocator().getSourceLocations() : new ICSourceLocation[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#setSourceLocations(ICSourceLocation[])
	 */
	public void setSourceLocations( ICSourceLocation[] locations )
	{
		if ( getCSourceLocator() != null )
		{
			getCSourceLocator().setSourceLocations( locations );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#contains(IResource)
	 */
	public boolean contains( IResource resource )
	{
		return ( getCSourceLocator() != null ) ? getCSourceLocator().contains( resource ) : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ISourceMode#getMode()
	 */
	public int getMode()
	{
		return fMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ISourceMode#setMode(int)
	 */
	public void setMode( int mode )
	{
		fMode = mode;
		setRealMode( mode );
	}

	public int getRealMode()
	{
		return fRealMode;
	}

	protected void setRealMode( int mode )
	{
		fRealMode = mode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( CSourceManager.class ) )
			return this;
		if ( adapter.equals( IResourceChangeListener.class ) &&
			 fSourceLocator instanceof IResourceChangeListener )
			return fSourceLocator;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object result = null;
		boolean autoDisassembly = CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_AUTO_DISASSEMBLY );
		
		if ( getMode() == ISourceMode.MODE_SOURCE && getSourceLocator() != null )
		{
			// if the target is suspended by a line breakpoint the source manager 
			// tries to retrieve the file resource from the breakpoint marker.
			if ( getDebugTarget() != null )
				result = getDebugTarget().getCurrentBreakpointFile();
			if ( result == null )
				result = getSourceLocator().getSourceElement( stackFrame );
		}
		if ( result == null && 
			 ( autoDisassembly || getMode() == ISourceMode.MODE_DISASSEMBLY ) && 
			 getDisassemblyManager( stackFrame ) != null )
		{
			setRealMode( ISourceMode.MODE_DISASSEMBLY );
			result = getDisassemblyManager( stackFrame ).getSourceElement( stackFrame );
		}
		else
		{
			setRealMode( ISourceMode.MODE_SOURCE );
		}
		return result;
	}
	
	protected ICSourceLocator getCSourceLocator()
	{
		if ( getSourceLocator() instanceof ICSourceLocator )
			return (ICSourceLocator)getSourceLocator();
		return null;
	}
	
	protected ISourceLocator getSourceLocator()
	{
		if ( fSourceLocator != null )
			return fSourceLocator;
		else if ( fLaunch != null )
			return fLaunch.getSourceLocator();
		return null;
	}

	protected void setSourceLocator( ISourceLocator sl )
	{
		fSourceLocator = sl;
	}
	
	protected DisassemblyManager getDisassemblyManager( IStackFrame stackFrame )
	{
		if ( stackFrame != null )
		{
			return (DisassemblyManager)stackFrame.getDebugTarget().getAdapter( DisassemblyManager.class );
		}
		return null;
	}
	
	public void addSourceLocation( ICSourceLocation location )
	{
		ICSourceLocation[] locations = getSourceLocations();
		ArrayList list = new ArrayList( Arrays.asList( locations ) );
		list.add( location );
		setSourceLocations( (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#findSourceElement(String)
	 */
	public Object findSourceElement( String fileName )
	{
		if ( getCSourceLocator() != null )
		{
			return getCSourceLocator().findSourceElement( fileName );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		if ( getPersistableSourceLocator() != null )
			return getPersistableSourceLocator().getMemento();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException
	{
		if ( getPersistableSourceLocator() != null )
			getPersistableSourceLocator().initializeDefaults( configuration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento( String memento ) throws CoreException
	{
		if ( getPersistableSourceLocator() != null )
			getPersistableSourceLocator().initializeFromMemento( memento );
	}

	private IPersistableSourceLocator getPersistableSourceLocator()
	{
		if ( fSourceLocator instanceof IPersistableSourceLocator )
			return (IPersistableSourceLocator)fSourceLocator;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getProject()
	 */
	public IProject getProject()
	{
		return ( getCSourceLocator() != null ) ? getCSourceLocator().getProject() :  null;
	}

	public void setDebugTarget( CDebugTarget target )
	{
		fDebugTarget = target;
	}

	protected CDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}
}
