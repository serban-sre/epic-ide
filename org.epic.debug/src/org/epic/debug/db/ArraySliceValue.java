package org.epic.debug.db;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class ArraySliceValue extends PerlValue
{
	private final PerlVariable[] elements;
	private final int startIndex;
	
	public ArraySliceValue(PerlVariable array, List<PerlVariable> elements, int startIndex)
	{
		super(array.getDebugTarget());
		
		this.elements = (PerlVariable[]) elements.toArray(new PerlVariable[elements.size()]);
		this.startIndex = startIndex;
	}
	
	public int getEndIndex()
	{
		return startIndex + elements.length - 1;
	}
	
	public int getStartIndex()
	{
		return startIndex;
	}
	
	public String getReferenceTypeName() throws DebugException
	{
        return null;
    }

    public String getValueString() throws DebugException
    {
        return "...";
    }
    public String getDetailValue() throws DebugException
    {
        return "...";
    }    
    public PerlVariable[] getVariables() throws DebugException
    {
        return elements;
    }

    public boolean hasVariables() throws DebugException
    {
        return true;
    }

    public boolean isAllocated() throws DebugException
    {
        return true;
    }

    public String getModelIdentifier()
    {
        return getDebugTarget().getModelIdentifier();
    }
}
