package com.iopipe.examples;

import java.util.Collections;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.iopipe.IOpipeService;
import com.iopipe.plugin.trace.TraceMeasurement;
import com.iopipe.plugin.trace.TraceUtils;

/**
 * This class wraps the simple request handler and just prefixes "Hello" to
 * an input object containing {name:"Foo"}
 * This manually initializes the IOpipe service.
 *
 * @since 2017/12/18
 */
public class ManualHello
	implements RequestHandler<Map<String,String>, String> {
	/**
	 * {@inheritDoc}
	 * @since 2017/12/13
	 */
	@Override
	public final String handleRequest(Map<String,String> request, Context __context)
	{
		return IOpipeService.instance().<String>run(__context, (__exec) ->
			{
				String name = request.containsKey("name") ? request.get("name") : null;

				if (name == null) {
					throw new RuntimeException("Invoked with no name!");
				}

				// Send a message to the example plugin
				__exec.<ExampleExecution>plugin(ExampleExecution.class,
					(__s) ->
					{
						__s.message("I shall say hello!");
						__s.message(name);
					});
				
				// Custom metrics which could convey important information
				__exec.customMetric("hello", "world");
				
				// Measure performance of this method via the trace plugin
				try (TraceMeasurement m = TraceUtils.measure(__exec, "math"))
				{
					long result = 0;
					for (int i = 1; i < 10000; i++)
						result += new Long(i);
			
					for (int i = 1; i < 10000; i++)
						result *= new Long(i);
					
					// Store the result of the math
					__exec.customMetric("result", (long)result);
				}
				
				// Say hello to them!
				return "Hello " + name + "!";
			});
	}
}

