package com.iopipe.http;

import com.iopipe.http.RemoteRequest;
import com.iopipe.IOpipeEventUploader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is an uploader which is completely serial based and it will only
 * upload events one at a time, blocking for each one. It cannot handle
 * multiple concurrent invocations at once.
 *
 * @since 2018/07/23
 */
public final class SerialEventUploader
	implements IOpipeEventUploader
{
	/** The connection to the remote service to use. */
	protected final RemoteConnection connection;
	
	/** The number of bad requests. */
	private final AtomicInteger _badresultcount =
		new AtomicInteger();
	
	/**
	 * Initializes the serialized event uploader.
	 *
	 * @param __con The connection to use.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/07/23
	 */
	public SerialEventUploader(RemoteConnection __con)
		throws NullPointerException
	{
		if (__con == null)
			throw new NullPointerException();
		
		this.connection = __con;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/07/23
	 */
	public final int badRequestCount()
	{
		return _badresultcount.get();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/07/23
	 */
	@Override
	public final void upload(RemoteRequest __r)
		throws NullPointerException
	{
		if (__r == null)
			throw new NullPointerException();
		
		// Generate report
		try
		{
			RemoteResult result = this.connection.send(RequestType.POST, __r);
			
			// Only the 200 range is valid for okay responses
			int code = result.code();
			if (!(code >= 200 && code < 300))
			{
				this._badresultcount.getAndIncrement();
				
			}
			
		}
		
		// Failed to write to the server
		catch (RemoteException e)
		{
			this._badresultcount.getAndIncrement();
		}
	}
	
	/**
	 * Shows string representation of the body.
	 *
	 * @param __b The body to decode.
	 * @return The string result.
	 * @since 2018/02/24
	 */
	private static final String __debugBody(RemoteBody __b)
	{
		try
		{
			String rv = __b.bodyAsString();
			if (rv.indexOf('\0') >= 0)
				return "BINARY DATA";
			return rv;
		}
		catch (Throwable t)
		{
			return "Could not decode!";
		}
	}
}

