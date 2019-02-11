import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Basic measure function.
 *
 * @since 2019/02/11
 */
public class MeasureFunc
	implements RequestHandler<Object, Object>
{
	/**
	 * {@inheritDoc}
	 * @since 2019/02/11
	 */
	@Override
	public Object handleRequest(Object __val, Context __context)
	{
		return __val;
	}
}
