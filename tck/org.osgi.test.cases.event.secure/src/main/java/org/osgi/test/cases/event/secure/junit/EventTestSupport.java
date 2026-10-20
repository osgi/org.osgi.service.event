/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.cases.event.secure.junit;

import static org.assertj.core.api.Assertions.fail;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;

/**
 * The helpers these tests used from the test.support classes
 * DefaultTestBundleControl (getService, ungetAllServices, failException) and
 * OSGiTestCaseProperties (getTimeout, getScaling), unchanged: the same system
 * properties and defaults, and fetched services are remembered by their
 * service object, so they can be released.
 */
abstract class EventTestSupport {
	@InjectBundleContext
	protected BundleContext							context;

	private final Map<Object,ServiceReference< ? >>	fetchedServices	= new HashMap<>();

	private static final long						timeout;
	private static final int						scaling;

	static {
		long t = 60000L;
		try {
			t = getLongProperty("org.osgi.test.testcase.timeout", t);
			if (t < 0) {
				t = 0;
			}
		} catch (SecurityException e) {
			// use default value
		}

		int s = 1;
		try {
			s = getIntegerProperty("org.osgi.test.testcase.scaling", s);
			if (s < 0) {
				s = 0;
			}
		} catch (SecurityException e) {
			// use default value
		}

		timeout = t;
		scaling = s;
	}

	public static long getTimeout() {
		return timeout;
	}

	public static int getScaling() {
		return scaling;
	}

	private static String getProperty(String key) {
		return AccessController.doPrivileged((PrivilegedAction<String>) () -> {
			Bundle bundle = FrameworkUtil.getBundle(EventTestSupport.class);
			if (bundle != null) {
				BundleContext context;
				try {
					context = bundle.getBundleContext();
				} catch (SecurityException e) {
					context = null;
				}
				if (context != null) {
					return context.getProperty(key);
				}
			}
			return System.getProperty(key);
		});
	}

	private static int getIntegerProperty(String key, int defaultValue) {
		String propValue = getProperty(key);
		if (propValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(propValue);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static long getLongProperty(String key, long defaultValue) {
		String propValue = getProperty(key);
		if (propValue == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(propValue);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static void failException(String message,
			Class< ? > expectedExceptionClass) {
		fail(message + " expected:[" + expectedExceptionClass.getName()
				+ "] and got nothing");
	}

	public <S> S getService(Class<S> clazz) {
		try {
			return getService(clazz, null);
		} catch (InvalidSyntaxException e) {
			/* null can not give an exception of this type */
		}
		return null;
	}

	public <S> S getService(Class<S> clazz, String filter)
			throws InvalidSyntaxException {
		@SuppressWarnings("unchecked")
		ServiceReference<S>[] refs = (ServiceReference<S>[]) context
				.getServiceReferences(clazz.getName(), filter);

		if (refs == null) {
			fail("Can't get service reference for " + clazz.getName() + filter);
		}

		ServiceReference<S> chosenRef = pickServiceReference(refs);

		S service = context.getService(chosenRef);

		if (service == null) {
			fail("Can't get service for " + clazz.getName() + filter);
		}

		/* Save the service and its reference */
		synchronized (fetchedServices) {
			fetchedServices.put(service, chosenRef);
		}

		return service;
	}

	public void ungetAllServices() {
		ServiceReference< ? >[] refs;
		synchronized (fetchedServices) {
			refs = fetchedServices.values()
					.toArray(new ServiceReference[fetchedServices.size()]);
			fetchedServices.clear();
		}

		for (int l = refs.length, i = 0; i < l; i++) {
			context.ungetService(refs[i]);
		}
	}

	private static <S> ServiceReference<S> pickServiceReference(
			ServiceReference<S>[] refs) {
		ServiceReference<S> highest = refs[0];

		for (int i = 1; i < refs.length; i++) {
			ServiceReference<S> challenger = refs[i];

			if (ranking(highest) < ranking(challenger)) {
				highest = challenger;
			} else if (ranking(highest) == ranking(challenger)) {
				if (serviceid(highest) > serviceid(challenger)) {
					highest = challenger;
				}
			}
		}

		return highest;
	}

	private static int ranking(ServiceReference< ? > s) {
		Object v = s.getProperty(Constants.SERVICE_RANKING);

		if (v != null && v instanceof Integer) {
			return ((Integer) v).intValue();
		} else {
			return 0;
		}
	}

	private static long serviceid(ServiceReference< ? > s) {
		Long sid = (Long) s.getProperty(Constants.SERVICE_ID);

		return sid.longValue();
	}
}
