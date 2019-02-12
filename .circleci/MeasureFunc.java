import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.iopipe.IOpipeExecution;
import com.iopipe.SimpleRequestHandlerWrapper;

/**
 * Basic measure function.
 *
 * @since 2019/02/11
 */
public class MeasureFunc
	extends SimpleRequestHandlerWrapper<Object, Object>
{
	/**
	 * {@inheritDoc}
	 * @since 2019/02/11
	 */
	@Override
	public Object wrappedHandleRequest(IOpipeExecution __exec, Object __v)
	{
		System.out.println("Executed baseline measure method.");
		return __v;
	}
}
