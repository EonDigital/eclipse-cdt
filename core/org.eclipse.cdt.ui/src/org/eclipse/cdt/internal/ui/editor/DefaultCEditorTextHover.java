package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.index.TagFlags;
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.HTMLPrinter;
import org.eclipse.cdt.ui.IFunctionSummary;

public class DefaultCEditorTextHover implements ITextHover 
{
	protected IEditorPart fEditor;
	
	/**
	 * Constructor for DefaultCEditorTextHover
	 */
	public DefaultCEditorTextHover( IEditorPart editor ) 
	{
		fEditor = editor;
	}

	/**
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo( ITextViewer viewer, IRegion region ) 
	{
		String expression = null;
		
		if(fEditor == null) 
			return null;
		try
		{
			expression = viewer.getDocument().get( region.getOffset(), region.getLength() );
			expression = expression.trim();
			if ( expression.length() == 0 )
				return null; 

			StringBuffer buffer = new StringBuffer();

			// We are just doing some C, call the Help to get info

			IFunctionSummary fs = CCompletionContributorManager.getFunctionInfo(expression);
			if(fs != null) {
				buffer.append("<b>" + HTMLPrinter.convertToHTMLContent(expression) + 
							  "()</b> - " + HTMLPrinter.convertToHTMLContent(fs.getSummary()) +
							  "<br><br>" + HTMLPrinter.convertToHTMLContent(fs.getSynopsis()));
				int i;
				for(i = 0; i < buffer.length(); i++) {
					if(buffer.charAt(i) == '\\') {
						if((i + 1 < buffer.length()) && buffer.charAt(i+1) == 'n') {
							buffer.replace(i, i + 2, "<br>");
						}
					}
				}
			} else {
				// Query the C model
				IndexModel model = IndexModel.getDefault();
				IEditorInput input = fEditor.getEditorInput();
				if(input instanceof IFileEditorInput) {
					IProject project = ((IFileEditorInput)input).getFile().getProject();

					// Bail out quickly, if the project was deleted.
					if (!project.exists())
						throw new CoreException(new Status(0, "", 0, "", null));

					IProject[] refs = project.getReferencedProjects();
					
					ITagEntry[] tags= model.query(project, expression, false, true);
					
					if(tags == null || tags.length == 0) {
						for ( int j= 0; j < refs.length; j++ ) {
							if (!refs[j].exists())
								continue;
							tags= model.query(refs[j], expression, false, true);
							if(tags != null && tags.length > 0) 
								break;
						}
					}
					
					if(tags != null && tags.length > 0) {
						ITagEntry selectedTag = selectTag(tags);
						// Show only the first element
						buffer.append("<b>" + HTMLPrinter.convertToHTMLContent(expression) +
									  "()</b> - " + selectedTag.getIFile().getFullPath().toString() + "[" + selectedTag.getLineNumber()+"]" );
						// Now add the pattern
						buffer.append("<br><br>" + HTMLPrinter.convertToHTMLContent(selectedTag.getPattern()));
					}	
				}
			}
			if (buffer.length() > 0) {
				HTMLPrinter.insertPageProlog(buffer, 0);
				HTMLPrinter.addPageEpilog(buffer);
				return buffer.toString();
			}
		}
		catch( BadLocationException x )
		{
			// ignore
		}
		catch( CoreException x )
		{
			// ignore
		}
		return null;
	}

	/**
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion( ITextViewer viewer, int offset ) 
	{
		Point selectedRange = viewer.getSelectedRange();
		if ( selectedRange.x >= 0 && 
			 selectedRange.y > 0 &&
			 offset >= selectedRange.x &&
			 offset <= selectedRange.x + selectedRange.y )
			return new Region( selectedRange.x, selectedRange.y );
		if ( viewer != null )
			return CWordFinder.findWord( viewer.getDocument(), offset );
		return null;
	}
	
	private ITagEntry selectTag(ITagEntry[] tags) {
		// Rules are to return a function prototype/declaration, and if
		// not found first entry
		for(int i = 0; i < tags.length; i++) {
			if(tags[i].getKind() == TagFlags.T_PROTOTYPE) {
				return tags[i];
			}
		}
		for(int i = 0; i < tags.length; i++) {
			if(tags[i].getKind() == TagFlags.T_FUNCTION) {
				return tags[i];
			}
		}
		return tags[0];
	}
}

