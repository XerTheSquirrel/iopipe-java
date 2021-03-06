package com.iopipe;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import org.pmw.tinylog.Logger;

/**
 * This represents a custom metric which may have a string or long value,
 * these may be used to add extra data points that are normally not present.
 *
 * Custom metric names are limited to the length specified in
 * {@link IOpipeConstants#NAME_CODEPOINT_LIMIT}.
 *
 * @since 2018/01/20
 */
public final class CustomMetric
	implements Comparable<CustomMetric>
{
	/** The name of this metric. */
	protected final String name;
	
	/** The string value of the metric. */
	protected final String stringvalue;
	
	/** The long value of the metric. */
	protected final long longvalue;
	
	/** Has a long value? */
	protected final boolean haslong;
	
	/** Hashcode. */
	private int _hashcode;
	
	/** String representation. */
	private Reference<String> _string;
	
	/**
	 * Initializes the custom metric with a string value.
	 *
	 * @param __name The metric name.
	 * @param __sv The string value.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/01/20
	 */
	public CustomMetric(String __name, String __sv)
		throws NullPointerException
	{
		if (__name == null || __sv == null)
			throw new NullPointerException();
		
		this.name =
			__limitLength(__name, IOpipeConstants.NAME_CODEPOINT_LIMIT);
		this.stringvalue =
			__limitLength(__sv, IOpipeConstants.VALUE_CODEPOINT_LIMIT);
		this.longvalue = 0L;
		this.haslong = false;
	}
	
	/**
	 * Initializes the custom metric with a long value.
	 *
	 * @param __name The metric name.
	 * @param __lv The long value.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/01/20
	 */
	public CustomMetric(String __name, long __lv)
		throws NullPointerException
	{
		if (__name == null)
			throw new NullPointerException();
		
		this.name = 
			__limitLength(__name, IOpipeConstants.NAME_CODEPOINT_LIMIT);
		this.stringvalue = null;
		this.longvalue = __lv;
		this.haslong = true;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/01/20
	 */
	@Override
	public int compareTo(CustomMetric __o)
	{
		int rv = this.name.compareTo(__o.name);
		if (rv != 0)
			return rv;
		
		String a = this.stringvalue,
			b = __o.stringvalue;
		if ((a == null) != (b == null))
			if (a == null)
				return -1;
			else
				return 1;
		else if (a == null)
			return 0;
		rv = a.compareTo(b);
		if (rv != 0)
			return rv;
		
		return Long.compare(this.longvalue, __o.longvalue);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/01/20
	 */
	@Override
	public boolean equals(Object __o)
	{
		if (__o == this)
			return true;
		
		if (!(__o instanceof CustomMetric))
			return false;
		
		// Equal objects will have the same hash code
		CustomMetric o = (CustomMetric)__o;
		if (this.hashCode() != o.hashCode())
			return false;
		
		// Check equality
		return this.name.equals(o.name) &&
			Objects.equals(this.stringvalue, o.stringvalue) &&
			this.haslong == o.haslong &&
			this.longvalue == o.longvalue;
	}
	
	/**
	 * Does this have a long value?
	 *
	 * @return If this has a long value.
	 * @since 2018/01/20
	 */
	public boolean hasLong()
	{
		return this.haslong;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/01/20
	 */
	@Override
	public int hashCode()
	{
		// Pre-cached?
		int rv = this._hashcode;
		if (rv != 0)
			return rv;
		
		// Cache it
		this._hashcode = (rv = (this.name.hashCode() ^
			Objects.hashCode(this.stringvalue) ^
			Long.hashCode(this.longvalue)) ^ (this.haslong ? ~0 : 0));
		return rv;
	}
	
	/**
	 * Does this have a string value?
	 *
	 * @return If this has a string value.
	 * @since 2018/01/20
	 */
	public boolean hasString()
	{
		return this.stringvalue != null;
	}
	
	/**
	 * Returns the long value.
	 *
	 * @return The long value or {@code 0} if there is no value.
	 * @since 2018/01/20
	 */
	public long longValue()
	{
		if (this.haslong)
			return this.longvalue;
		return 0L;
	}
	
	/**
	 * Returns the name of the custom metric.
	 *
	 * @return The custom metric name.
	 * @since 2018/01/20
	 */
	public String name()
	{
		return this.name;
	}
	
	/**
	 * Returns the string value.
	 *
	 * @return The String value or {@code null} if it is not set.
	 * @since 2018/01/20
	 */
	public String stringValue()
	{
		return this.stringvalue;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/01/20
	 */
	@Override
	public String toString()
	{
		Reference<String> ref = this._string;
		String rv;
		
		if (ref == null || null == (rv = ref.get()))
		{
			String name = this.name,
				stringvalue = this.stringvalue;
			
			if (stringvalue != null)
				rv = String.format("%s=%s", name, stringvalue);
			else
				rv = String.format("%s=%d", name, this.longvalue);
			
			// Cache it
			this._string = new WeakReference<>(rv);
		}
		
		return rv;
	}
	
	/**
	 * Limit length of metric whatever to the given count.
	 *
	 * @param __in The input value.
	 * @param __lim The length to limit.
	 * @return The string with its limited length.
	 * @since 2018/11/20
	 */
	static final String __limitLength(String __in, int __lim)
	{
		if (__in == null || __in.length() < __lim)
			return __in;
		
		// Record it
		Logger.warn("Label, custom metric name, or custom metric value " +
			"exceeds the character length limitation of {} characters.", __lim);
		
		// Trim it
		return __in.substring(0, __lim);
	}
}

