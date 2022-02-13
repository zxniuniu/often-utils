package org.haic.often.Network;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/2/13 17:55
 */
public class HttpStatus implements org.apache.http.HttpStatus {
	/**
	 * 状态码 429 - 在一定的时间内用户发送了太多的请求，即超出了“频次限制”
	 */
	public static final int SC_TOO_MANY_REQUEST = 429;
}