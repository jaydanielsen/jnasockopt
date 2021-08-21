/*
 * Copyright (c) 2018 Valassis Digital. All rights reserved.
 */
package org.jna;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JNASockOptTest {
	private static Logger logger = LoggerFactory.getLogger(JNASockOptTest.class);
	private Socket sock;

	@Before
	public void initializeSocket() throws IOException {
		sock = new Socket();
		String connIp = "127.0.0.1";
		int port = 22;
		InetAddress addr = InetAddress.getByAddress(connIp, InetAddress.getByName(connIp).getAddress());
		InetSocketAddress iaddr = new InetSocketAddress(addr, port);
		sock.connect(iaddr);

		int sockfdnum = JNASockOpt.getOutputFd(sock);
		logger.debug("fdnum = " + sockfdnum);
	}

	@After
	public void cleanup() {
		try {
			sock.close();
		}
		catch (IOException e) {
			logger.error("error closing socket");
		}
	}

	public void testOption(JNASockOptionLevel level, JNASockOption option, int option_value) throws Exception {
		int initial_value = JNASockOpt.getSockOpt(sock, level, option);
		logger.info(option.name() + " initial value = " + initial_value);
		JNASockOpt.setSockOpt(sock, level, option, option_value);
		int final_value = JNASockOpt.getSockOpt(sock, level, option);
		logger.info(option.name() + " final   value = " + final_value);
		Assert.assertEquals("Failed to set " + option.name(), option_value, final_value);
	}

	@Test
	public void rcvbufTest() throws Exception {
		int option_value = 65536;
		JNASockOptionLevel level = JNASockOptionLevel.SOL_SOCKET;
		JNASockOption option = JNASockOption.SO_RCVBUF;
		int initial_value = JNASockOpt.getSockOpt(sock, level, option);
		logger.info(option.name() + " initial value = " + initial_value);
		JNASockOpt.setSockOpt(sock, level, option, option_value);
		int final_value = JNASockOpt.getSockOpt(sock, level, option);
		logger.info(option.name() + " final   value = " + final_value);
		Assert.assertTrue("Failed to set " + option.name(), final_value >= option_value);
	}

	@Test
	public void tcpKeepIdleTest() {
		String osName = System.getProperty("os.name");
		int option_value = 60;
		try {
			testOption(JNASockOptionLevel.SOL_TCP, JNASockOption.TCP_KEEPIDLE, option_value);
		}
		catch (Exception e) {
			if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
				logger.info("TCP_KEEPIDLE not supported on macosx");
			}
			else {
				Assert.fail("failed to set TCP_KEEPIDLE");
			}
		}
	}

	@Test
	public void tcpKeepIntervalTest() {
		String osName = System.getProperty("os.name");
		int option_value = 120;
		try {
			testOption(JNASockOptionLevel.SOL_TCP, JNASockOption.TCP_KEEPINTVL, option_value);
		}
		catch (Exception e) {
			if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
				logger.info("TCP_KEEPINTVL not supported on macosx");
			}
			else {
				Assert.fail("failed to set TCP_KEEPINTVL");
			}
		}
	}

	@Test
	public void tcpKeepCountTest() {
		String osName = System.getProperty("os.name");
		int option_value = 16;
		try {
			testOption(JNASockOptionLevel.SOL_TCP, JNASockOption.TCP_KEEPCNT, option_value);
		}
		catch (Exception e) {
			if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
				logger.info("TCP_KEEPCNT not supported on macosx");
			}
			else {
				Assert.fail("failed to set TCP_KEEPCNT");
			}
		}
	}
}
