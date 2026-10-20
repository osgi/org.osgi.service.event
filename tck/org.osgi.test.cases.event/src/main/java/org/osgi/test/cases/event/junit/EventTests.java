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
import static org.osgi.test.cases.event.junit.EventTestSupport.assertConstant;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventProperties;

public class EventTests {

	@Test
	public void testIllegalTopics() {
		String[] illegalTopics = new String[] {"", "*", "/error_topic",
				"//error_topic1", "é/error_topic2", "topic/error_topic3/",
				"error_topic&", "topic//error-topic4", "topic\\error_topic5"};
		Hashtable<String,Object> properties = new Hashtable<>();
		// illegal topics tested
		for (int i = 0; i < illegalTopics.length; i++) {
			String topic = illegalTopics[i];
			try {
				new Event(topic, (Dictionary<String, ? >) properties);
				fail("Excepted IllegalArgumentException for topic:[" + topic
						+ "]");
			}
			catch (IllegalArgumentException e) {
				// expected
			}
		}
	}

	@Test
	public void testTopics() throws Exception {
		String[] legalTopics = new String[] {"ACTION0",
				"org/osgi/test/cases/event/ACTION1",
				"org/osgi/test/cases/event/ACTION2", "0", "_/-", "_topic",
				"-topic", "9topic0/-topic_/_topic-"};
		// legal topics tested
		for (int i = 0; i < legalTopics.length; i++) {
			String topic = legalTopics[i];
			Event event = null;
			try {
				event = new Event(topic, (Dictionary<String, ? >) null);
			}
			catch (Throwable e) {
				fail("Exception in event construction with topic:[" + topic
						+ "]", e);
			}
			assertThat(event.getTopic()).as("topic not set").isEqualTo(new String(topic));
			assertThat(event
					.getProperty("event.topics")).as("topic not in properties").isEqualTo(topic);
			assertThat(event
					.getPropertyNames().length).as("wrong amount of properties").isOne();
			assertThat(event.matches(FrameworkUtil
					.createFilter("(event.topics=" + topic + ")"))).as("filter does not match").isTrue();
		}
	}

	@Test
	public void testProperties() throws Exception {
		assertConstant("event.topics", "EVENT_TOPIC", EventConstants.class);
		String t1 = "a/b";
		Map<Object,Object> m1 = new HashMap<>();
		m1.put("foo", "bar");
		String[] a1 = new String[] {"foo", "bar"};
		m1.put("baz", a1);
		m1.put(Boolean.TRUE, "baz"); // non-string key must not cause error
		m1.put("event.topics", "b/a"); // event.topics in properties is
										// overridden
		Hashtable<String,Object> m2 = new Hashtable<>();
		m2.put("foo", "bar");
		m2.put("baz", a1);
		m2.put("event.topics", "b/a"); // event.topics in properties is
										// overridden
		Dictionary<String,Object> m3 = new Hashtable<>();
		m3.put("foo", "bar");
		m3.put("baz", a1);
		m3.put("event.topics", "b/a"); // event.topics in properties is
										// overridden

		@SuppressWarnings({
				"rawtypes", "unchecked"
		})
		Event e1 = new Event(t1, (Map) m1);
		Event e2 = new Event(t1, (Dictionary<String, ? >) m2);
		Event e3 = new Event(t1, m3);

		assertThat(e1.getTopic()).as("topic not set").isEqualTo(new String(t1));
		assertThat(e2.getTopic()).as("topic not set").isEqualTo(new String(t1));
		assertThat(e3.getTopic()).as("topic not set").isEqualTo(new String(t1));

		assertThat(e1
				.getProperty("event.topics")).as("topic not in properties").isEqualTo(t1);
		assertThat(e2
				.getProperty("event.topics")).as("topic not in properties").isEqualTo(t1);
		assertThat(e3
				.getProperty("event.topics")).as("topic not in properties").isEqualTo(t1);

		assertThat(e1.getProperty("foo")).as("foo property value != bar").isEqualTo("bar");
		assertThat(e2.getProperty("foo")).as("foo property value != bar").isEqualTo("bar");
		assertThat(e3.getProperty("foo")).as("foo property value != bar").isEqualTo("bar");

		assertThat(e1.getPropertyNames().length).as("wrong amount of properties").isEqualTo(3);
		assertThat(e2.getPropertyNames().length).as("wrong amount of properties").isEqualTo(3);
		assertThat(e3.getPropertyNames().length).as("wrong amount of properties").isEqualTo(3);

		assertThat(e1.equals(e2)).as("events not equal").isTrue();
		assertThat(e1.equals(e3)).as("events not equal").isTrue();
		assertThat(e2.equals(e1)).as("events not equal").isTrue();
		assertThat(e2.equals(e3)).as("events not equal").isTrue();
		assertThat(e3.equals(e1)).as("events not equal").isTrue();
		assertThat(e3.equals(e2)).as("events not equal").isTrue();

		assertThat(e2.hashCode()).as("event hashcodes not equal").isEqualTo(e1.hashCode());
		assertThat(e3.hashCode()).as("event hashcodes not equal").isEqualTo(e1.hashCode());
		assertThat(e1.hashCode()).as("event hashcodes not equal").isEqualTo(e2.hashCode());
		assertThat(e3.hashCode()).as("event hashcodes not equal").isEqualTo(e2.hashCode());
		assertThat(e1.hashCode()).as("event hashcodes not equal").isEqualTo(e3.hashCode());
		assertThat(e2.hashCode()).as("event hashcodes not equal").isEqualTo(e3.hashCode());

		Filter f1 = FrameworkUtil.createFilter("(foo=bar)");
		assertThat(e1.matches(f1)).as("filter does not match").isTrue();
		assertThat(e2.matches(f1)).as("filter does not match").isTrue();
		assertThat(e3.matches(f1)).as("filter does not match").isTrue();

		Filter f2 = FrameworkUtil.createFilter("(event.topics=" + t1 + ")");
		assertThat(e1.matches(f2)).as("filter does not match").isTrue();
		assertThat(e2.matches(f2)).as("filter does not match").isTrue();
		assertThat(e3.matches(f2)).as("filter does not match").isTrue();
	}

	@Test
	public void testEventsEquals() {
		String t1 = "a/b";
		Map<String,Object> m1 = new HashMap<>();
		m1.put("foo", "bar");
		m1.put("baz", new String[] {"foo", "bar"});
		m1.put("event.topics", "b/a"); // event.topics in properties is
		// overridden
		Hashtable<String,Object> m2 = new Hashtable<>();
		m2.put("foo", "bar");
		m2.put("baz", new String[] {"foo", "bar"});
		m2.put("event.topics", "b/a"); // event.topics in properties is
		// overridden
		Dictionary<String,Object> m3 = new Hashtable<>();
		m3.put("foo", "bar");
		m3.put("baz", new String[] {"foo", "bar"});
		m3.put("event.topics", "b/a"); // event.topics in properties is
		// overridden

		Event e1 = new Event(t1, m1);
		Event e2 = new Event(t1, (Dictionary<String, ? >) m2);
		Event e3 = new Event(t1, m3);

		assertThat(e1.equals(e2)).as("events equal").isFalse();
		assertThat(e1.equals(e3)).as("events equal").isFalse();
		assertThat(e2.equals(e1)).as("events equal").isFalse();
		assertThat(e2.equals(e3)).as("events equal").isFalse();
		assertThat(e3.equals(e1)).as("events equal").isFalse();
		assertThat(e3.equals(e2)).as("events equal").isFalse();

		assertThat(e1.hashCode() == e2.hashCode()).as("event hashcodes equal").isFalse();
		assertThat(e1.hashCode() == e3.hashCode()).as("event hashcodes equal").isFalse();
		assertThat(e2.hashCode() == e1.hashCode()).as("event hashcodes equal").isFalse();
		assertThat(e2.hashCode() == e3.hashCode()).as("event hashcodes equal").isFalse();
		assertThat(e3.hashCode() == e1.hashCode()).as("event hashcodes equal").isFalse();
		assertThat(e3.hashCode() == e2.hashCode()).as("event hashcodes equal").isFalse();

	}

	@Test
	public void testContainsProperty() {
		String t1 = "a/b";
		Map<Object,Object> m1 = new HashMap<>();
		m1.put("foo", null);
		String[] a1 = new String[] {"foo", "bar"};
		m1.put("baz", a1);
		m1.put(Boolean.TRUE, "baz"); // non-string key must not cause error
		Hashtable<String,Object> m2 = new Hashtable<>();
		m2.put("foo", "bar");
		m2.put("baz", a1);
		Dictionary<String,Object> m3 = new Hashtable<>();
		m3.put("foo", "bar");
		m3.put("baz", a1);

		@SuppressWarnings({
				"unchecked", "rawtypes"
		})
		Event e1 = new Event(t1, (Map) m1);
		Event e2 = new Event(t1, (Dictionary<String, ? >) m2);
		Event e3 = new Event(t1, m3);
		assertThat(e1.containsProperty("foo")).as("foo property not in properties").isTrue();
		assertThat(e2.containsProperty("foo")).as("foo property not in properties").isTrue();
		assertThat(e3.containsProperty("foo")).as("foo property not in properties").isTrue();

		assertThat(e1
				.containsProperty("bar")).as("bar property not in properties").isFalse();
		assertThat(e2
				.containsProperty("bar")).as("bar property not in properties").isFalse();
		assertThat(e3
				.containsProperty("bar")).as("bar property not in properties").isFalse();
	}

	@Test
	public void testEventProperties() throws Exception {
		String t1 = "a/b";
		Map<Object,Object> m1 = new HashMap<>();
		m1.put("foo", "bar");
		String[] a1 = new String[] {"foo", "bar"};
		m1.put("baz", a1);
		m1.put(Boolean.TRUE, "baz"); // non-string key must not cause error
		m1.put("event.topics", "b/a"); // event.topics in properties is
		// overridden

		@SuppressWarnings({
				"unchecked", "rawtypes"
		})
		EventProperties p1 = new EventProperties((Map) m1);
		@SuppressWarnings({
				"unchecked", "rawtypes"
		})
		EventProperties p2 = new EventProperties((Map) m1);

		assertThat(p1.equals(p2)).as("eventproperties not equal").isTrue();
		assertThat(p2.equals(p1)).as("eventproperties not equal").isTrue();

		assertThat(p2
				.hashCode()).as("eventproperties hashcodes not equal").isEqualTo(p1.hashCode());
		assertThat(p1
				.hashCode()).as("eventproperties hashcodes not equal").isEqualTo(p2.hashCode());
		assertThat(p1.size()).as("wrong amount of properties").isEqualTo(2);
		assertThat(p2.size()).as("wrong amount of properties").isEqualTo(2);
		assertThat(p1.keySet()).as("wrong amount of properties").hasSize(2);
		assertThat(p2.keySet()).as("wrong amount of properties").hasSize(2);
		assertThat(p1.values()).as("wrong amount of properties").hasSize(2);
		assertThat(p2.values()).as("wrong amount of properties").hasSize(2);
		assertThat(p1.entrySet()).as("wrong amount of properties").hasSize(2);
		assertThat(p2.entrySet()).as("wrong amount of properties").hasSize(2);
		assertThat(p1.isEmpty()).as("empty").isFalse();
		assertThat(p2.isEmpty()).as("empty").isFalse();
		assertThat(new EventProperties(null).isEmpty()).as("not empty").isTrue();

		try {
			p1.clear();
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		try {
			p1.put("xxx", "zzz");
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		try {
			@SuppressWarnings({
					"rawtypes", "unchecked"
			})
			Map<String, ? > m12 = (Map) m1;
			p1.putAll(m12);
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		try {
			p1.remove("foo");
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}
		try {
			p1.keySet().clear();
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}
		try {
			p1.values().clear();
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}
		try {
			p1.entrySet().clear();
			fail("did not throw exception");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		assertThat(p1.containsKey("event.topics")).as("contains event.topics key").isFalse();
		assertThat(p1.containsKey("foo")).as("foo key not present").isTrue();
		assertThat(p1.containsValue("baz")).as("baz value not present").isFalse();
		assertThat(p1.containsValue("bar")).as("bar value not present").isTrue();

		Event e1 = new Event(t1, p1);
		Event e2 = new Event(t1, p1);
		Event e3 = new Event(t1, p1);

		assertThat(e1.getTopic()).as("topic not set").isEqualTo(new String(t1));
		assertThat(e2.getTopic()).as("topic not set").isEqualTo(new String(t1));
		assertThat(e3.getTopic()).as("topic not set").isEqualTo(new String(t1));

		assertThat(e1
				.getProperty("event.topics")).as("topic not in properties").isEqualTo(t1);
		assertThat(e2
				.getProperty("event.topics")).as("topic not in properties").isEqualTo(t1);
		assertThat(e3
				.getProperty("event.topics")).as("topic not in properties").isEqualTo(t1);

		assertThat(e1.getProperty("foo")).as("foo property value != bar").isEqualTo("bar");
		assertThat(e2.getProperty("foo")).as("foo property value != bar").isEqualTo("bar");
		assertThat(e3.getProperty("foo")).as("foo property value != bar").isEqualTo("bar");

		assertThat(e1.getPropertyNames().length).as("wrong amount of properties").isEqualTo(3);
		assertThat(e2.getPropertyNames().length).as("wrong amount of properties").isEqualTo(3);
		assertThat(e3.getPropertyNames().length).as("wrong amount of properties").isEqualTo(3);

		assertThat(e1.equals(e2)).as("events not equal").isTrue();
		assertThat(e1.equals(e3)).as("events not equal").isTrue();
		assertThat(e2.equals(e1)).as("events not equal").isTrue();
		assertThat(e2.equals(e3)).as("events not equal").isTrue();
		assertThat(e3.equals(e1)).as("events not equal").isTrue();
		assertThat(e3.equals(e2)).as("events not equal").isTrue();

		assertThat(e2.hashCode()).as("event hashcodes not equal").isEqualTo(e1.hashCode());
		assertThat(e3.hashCode()).as("event hashcodes not equal").isEqualTo(e1.hashCode());
		assertThat(e1.hashCode()).as("event hashcodes not equal").isEqualTo(e2.hashCode());
		assertThat(e3.hashCode()).as("event hashcodes not equal").isEqualTo(e2.hashCode());
		assertThat(e1.hashCode()).as("event hashcodes not equal").isEqualTo(e3.hashCode());
		assertThat(e2.hashCode()).as("event hashcodes not equal").isEqualTo(e3.hashCode());

		Filter f1 = FrameworkUtil.createFilter("(baz=bar)");
		assertThat(e1.matches(f1)).as("filter does not match").isTrue();
		assertThat(e2.matches(f1)).as("filter does not match").isTrue();
		assertThat(e3.matches(f1)).as("filter does not match").isTrue();

		Filter f2 = FrameworkUtil.createFilter("(event.topics=" + t1 + ")");
		assertThat(e1.matches(f2)).as("filter does not match").isTrue();
		assertThat(e2.matches(f2)).as("filter does not match").isTrue();
		assertThat(e3.matches(f2)).as("filter does not match").isTrue();

		// change the value object to verify all events are sharing the same
		// EventProperties.
		Filter f3 = FrameworkUtil.createFilter("(baz=baz)");
		assertThat(e1.matches(f3)).as("filter does match").isFalse();
		assertThat(e2.matches(f3)).as("filter does match").isFalse();
		assertThat(e3.matches(f3)).as("filter does match").isFalse();
		a1[1] = "baz";
		assertThat(e1.matches(f3)).as("filter does not match").isTrue();
		assertThat(e2.matches(f3)).as("filter does not match").isTrue();
		assertThat(e3.matches(f3)).as("filter does not match").isTrue();

	}

}
