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

package org.osgi.test.cases.event.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;

/**
 * The helpers these tests used from the test.support classes
 * DefaultTestBundleControl (getService, ungetAllServices) and OSGiTestCase
 * (assertConstant), unchanged: fetched services are remembered by their
 * service object, so they can be released.
 */
abstract class EventTestSupport {
	@InjectBundleContext
	protected BundleContext							context;

	private final Map<Object,ServiceReference< ? >>	fetchedServices	= new HashMap<>();

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

	/**
	 * Assert a constant from class has a specific value.
	 *
	 * @param expected Expected value.
	 * @param fieldName Name of the field.
	 * @param fieldClass Class containing the field.
	 */
	public static void assertConstant(Object expected, String fieldName,
			Class< ? > fieldClass) {
		try {
			Field f = fieldClass.getField(fieldName);
			assertThat(Modifier.isPublic(f.getModifiers())).isTrue();
			assertThat(Modifier.isStatic(f.getModifiers())).isTrue();
			assertThat(Modifier.isFinal(f.getModifiers())).isTrue();
			assertThat(f.get(null)).as(fieldName).isEqualTo(expected);
		} catch (NoSuchFieldException e) {
			fail("missing field: " + fieldName + ": " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			fail("bad field: " + fieldName + ": " + e.getMessage(), e);
		}
	}
}
