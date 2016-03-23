package com.ctrip.apollo.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DNSUtil {

	public static List<String> resolve(String domainName) throws UnknownHostException {
		List<String> result = new ArrayList<String>();

		InetAddress[] addresses = InetAddress.getAllByName(domainName);
		if (addresses != null) {
			for (InetAddress addr : addresses) {
				result.add(addr.getHostAddress());
			}
		}

		return result;
	}

}
