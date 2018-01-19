package com.iopipe;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * This class provides access to information and functionality which is
 * specific to a single execution of a method.
 *
 * Each execution will have a unique instance of this object.
 *
 * @since 2018/01/19
 */
public final class IOpipeExecution
{
	/** The service which invoked the method. */
	protected final IOpipeService service;
	
	/** The configuration. */
	protected final IOpipeConfiguration config;
	
	/** The context. */
	protected final Context context;
	
	/** The measurement. */
	protected final IOpipeMeasurement measurement;
	
	/**
	 * Initializes the execution information.
	 *
	 * @param __sv The service which initialized this.
	 * @param __conf The configuration for this service.
	 * @param __context The context for the execution.
	 * @param __m Measurement which is used to provide access to tracing.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/01/19
	 */
	IOpipeExecution(IOpipeService __sv, IOpipeConfiguration __conf,
		Context __context, IOpipeMeasurement __m)
		throws NullPointerException
	{
		if (__sv == null || __conf == null || __context == null ||
			__m == null)
			throw new NullPointerException();
		
		this.service = __sv;
		this.config = __conf;
		this.context = __context;
		this.measurement = __m;
	}
	
	/**
	 * Returns the configuration used to initialize the service.
	 *
	 * @return The service configuration.
	 * @since 2018/01/19
	 */
	public final IOpipeConfiguration config()
	{
		return this.config;
	}
	
	/**
	 * Returns the AWS context.
	 *
	 * @return The AWS context.
	 * @since 2018/01/19
	 */
	public final Context context()
	{
		return this.context;
	}
	
	/**
	 * Returns the measurement recorder.
	 *
	 * @return The measurement recorder.
	 * @since 2018/01/19
	 */
	public final IOpipeMeasurement measurement()
	{
		return this.measurement;
	}
	
	/**
	 * Returns the service which ran this execution.
	 *
	 * @return The service which ran this execution.
	 * @since 2018/01/19
	 */
	public final IOpipeService service()
	{
		return this.service;
	}
}

