package org.epic.debug.db;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.epic.debug.PerlDebugPlugin;
import org.epic.debug.ui.action.ShowVarAddressActionDelegate;

/**
 * Abstract base class for objects representing values of PerlVariables.
 * 
 * @author jploski
 */
public abstract class PerlValue extends DebugElement implements IValue
{
	private static final String DB_DUMP_ENTITY; 

	private final PerlVariable holder;

	static
	{
		DB_DUMP_ENTITY = HelperScript.load("dump_entity.pl");
	}
	protected PerlValue(IDebugTarget target)
	{
		super(target);
		holder = null;
	}
	protected PerlValue(IDebugTarget target, PerlVariable holder)
			throws DebugException
			{
		super(target);

		this.holder = holder;
			}

	/**
	 * @return the value to be displayed in the detail section
	 *         of the Variables view
	 */
	public String getDetailValue() throws DebugException
	{
		DumpedEntity entity = this.holder.getDumpedEntity();
		if (entity.isCyclicReference()) return "cyclic reference";
		if (!(entity.isDefined())) return "undef";
		if (entity.getValue() != null) return "'" + entity.getValue() + "'";
		// if this is ARRAY
		if (this.holder.isArray()) {
			StringBuilder result = new StringBuilder("[");
			PerlVariable[] ivarArr = getVariables();
			if ((ivarArr == null) || (ivarArr.length == 0)) return "[ ]";
			if (ivarArr[0] instanceof ArraySlice) {
				return " ... ";
			}
			int maxArrayElements = 1;
			for (PerlVariable iv : ivarArr) {
				if (maxArrayElements > 10) {
					result.append("...");
					break;
				}
				String refType = iv.getReferenceTypeName();
				if (refType.equals("SCALAR")) {
					result.append(iv.getValue().getValueString() + ", ");
				}  else
					result.append(iv.getValue().getDataStructValueString() + ", ");
				++maxArrayElements;
			}
			return result.replace(result.length() - 2, result.length() - 1, "]").toString();
		}
		// if this is HASH
		if (this.holder.isHash()) {
			StringBuilder result = new StringBuilder("{");
			PerlVariable[] ivarArr = getVariables();
			if ((ivarArr == null) || (ivarArr.length == 0)) return "{ }";
			int maxHashElements = 1;
			for (PerlVariable iv : ivarArr) {
				if (maxHashElements > 10) {
					result.append("...");
					break;
				}
				if (iv.getReferenceTypeName().equals("SCALAR"))
					result.append(iv.getName() + " => " + iv.getValue().getValueString() + ", ");
				else 
					result.append(iv.getName() + " => " + iv.getValue().getDataStructValueString() + ", ");
				++maxHashElements;
			}
			
			return  result.replace(result.length() - 2, result.length() - 1, "}").toString();
		}
		// no match
		return "NA";
	}
	/**
	 * @return the value string for data structures(ARRAY and HASH) built up from the references it contain
	 * @throws DebugException
	 */
	public String getDataStructValueString() throws DebugException
	{
		DumpedEntity entity = this.holder.getDumpedEntity();
		String[] refChain = entity.getReferenceChain();

		boolean suppressSelfAddress = !(ShowVarAddressActionDelegate.getPreferenceValue());
		int start = (suppressSelfAddress) ? 1 : 0;

		StringBuffer buf = new StringBuffer();
		for (int i = start; i < refChain.length; ++i)
		{
			if (i > start) buf.append("->");
			buf.append(refChain[i]);
		}
		return buf.toString();
	}
	/**
	 * @return the variable which contains this value
	 */
	public PerlVariable getHolder()
	{
		return holder;
	}

	/**
	 * @see org.epic.debug.db.PerlVariable#getReferenceTypeName
	 */
	public String getReferenceTypeName() throws DebugException
	{
		return holder.getReferenceTypeName();
	}

	/**
	 * @return the string displayed for this value in the overview
	 *         section of the Variables view
	 */
	public String getValueString() throws DebugException
	{
		DumpedEntity entity = holder.getDumpedEntity();
		String[] refChain = entity.getReferenceChain();

		boolean suppressSelfAddress = !ShowVarAddressActionDelegate.getPreferenceValue();
		int start = suppressSelfAddress ? 1 : 0;

		StringBuffer buf = new StringBuffer();
		for (int i = start; i < refChain.length; i++)
		{
			if (i > start) buf.append("->");
			buf.append(refChain[i]);
		}

		String detail = getDetailValue();
		if (detail.length() > 0)
		{
			if (buf.length() > 0) buf.append('=');
			if (detail.length() > 128)
				detail = detail.substring(0, 128) + "...";
			buf.append(detail);
		}
		return buf.toString();
	}

	public abstract PerlVariable[] getVariables() throws DebugException;

	public abstract boolean hasVariables() throws DebugException;

	public boolean isAllocated() throws DebugException
	{
		return true;
	}

	public String getModelIdentifier()
	{
		return getDebugTarget().getModelIdentifier();
	}

	/**
	 * Dumps subvariables of the variable which holds this value. 
	 * 
	 * @param subName   name of the dumpvar_epic.pm subroutine which
	 *                  should be invoked
	 */
	protected String dumpEntity(String subName) throws DebugException
	{
		try
		{
			PerlVariable holder = getHolder();
			if (!holder.getStackFrame().getThread().isSuspended()) return "";

			String code = HelperScript.replace(
					DB_DUMP_ENTITY,
					"#SET_OFFSET#",
					"my $offset = " + holder.getStackFrame().getFrameIndex() + ";");

			code = HelperScript.replace(
					code,
					"#SET_VAREXPR#",
					"my $varexpr = <<'EOT';\n" + holder.getExpression() + "\nEOT");

			code = HelperScript.replace(
					code,
					"#SET_SUBREF#",
					"my $subref = \\&dumpvar_epic::" + subName + ";");

			return holder.getDebuggerInterface().eval(code);
		}
		catch (IOException e)
		{
			throw new DebugException(new Status(
					IStatus.ERROR,
					PerlDebugPlugin.getUniqueIdentifier(),
					IStatus.OK,
					"An error occurred while retrieving variables from the debugger process",
					e));
		}
	}
}