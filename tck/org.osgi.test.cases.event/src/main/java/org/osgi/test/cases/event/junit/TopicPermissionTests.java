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

import static org.assertj.core.api.Assertions.fail;
import static org.osgi.test.assertj.permission.PermissionAssertions.assertThat;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.PropertyPermission;

import org.junit.jupiter.api.Test;
import org.osgi.service.event.TopicPermission;

public class TopicPermissionTests {

	@Test
	public void testInvalid() {
		invalidTopicPermission("a/b/c", "x");
		invalidTopicPermission("a/b/c", "   subscribe  ,  x   ");
		invalidTopicPermission("a/b/c", "");
		invalidTopicPermission("a/b/c", "      ");
		invalidTopicPermission("a/b/c", null);
		invalidTopicPermission("a/b/c", ",");
		invalidTopicPermission("a/b/c", ",xxx");
		invalidTopicPermission("a/b/c", "xxx,");
		invalidTopicPermission("a/b/c", "subscribe,");
		invalidTopicPermission("a/b/c", "publish,   ");
		invalidTopicPermission("a/b/c", "subscribeme,");
		invalidTopicPermission("a/b/c", "publishme,");
		invalidTopicPermission("a/b/c", ",subscribe");
		invalidTopicPermission("a/b/c", ",publish");
		invalidTopicPermission("a/b/c", "   subscribeme   ");
		invalidTopicPermission("a/b/c", "   publishme     ");
		invalidTopicPermission("a/b/c", "   subscrib");
		invalidTopicPermission("a/b/c", "   publis"); 
		invalidTopicPermission("", "   publish"); 
	}

	@Test
	public void testActions() {
		Permission op = new PropertyPermission("java.home", "read"); 

		TopicPermission p11 = new TopicPermission("com/foo/service1",
				"    SUBSCRIBE,publish   ");
		TopicPermission p12 = new TopicPermission("com/foo/service1",
				"PUBLISH  ,   subscribe");
		TopicPermission p13 = new TopicPermission("com/foo/service1",
				"publISH   ");
		TopicPermission p14 = new TopicPermission("com/foo/service1",
				"    Subscribe    "); 

		assertThat(p11).implies(p11);
		assertThat(p11).implies(p12);
		assertThat(p11).implies(p13);
		assertThat(p11).implies(p14);

		assertThat(p12).implies(p11);
		assertThat(p12).implies(p12);
		assertThat(p12).implies(p13);
		assertThat(p12).implies(p14);

		assertThat(p13).implies(p13);
		assertThat(p14).implies(p14);

		assertThat(p13).doesNotImply(p11);
		assertThat(p13).doesNotImply(p12);

		assertThat(p14).doesNotImply(p11);
		assertThat(p14).doesNotImply(p12);

		assertThat(p13).doesNotImply(p14);
		assertThat(p14).doesNotImply(p13);

		assertThat(p11).doesNotImply(op);

		assertThat(p11).isEquivalentTo(p11);
		assertThat(p11).isEquivalentTo(p12);
		assertThat(p12).isEquivalentTo(p11);
		assertThat(p12).isEquivalentTo(p12);
		assertThat(p13).isEquivalentTo(p13);
		assertThat(p14).isEquivalentTo(p14);

		assertThat(p11).isNotEquivalentTo(p13);
		assertThat(p11).isNotEquivalentTo(p14);
		assertThat(p12).isNotEquivalentTo(p13);
		assertThat(p12).isNotEquivalentTo(p14);
		assertThat(p13).isNotEquivalentTo(p11);
		assertThat(p13).isNotEquivalentTo(p12);
		assertThat(p13).isNotEquivalentTo(p14);
		assertThat(p14).isNotEquivalentTo(p11);
		assertThat(p14).isNotEquivalentTo(p12);
		assertThat(p14).isNotEquivalentTo(p13);

		PermissionCollection pc = p13.newPermissionCollection();

		assertThat(pc).hasNoElements();

		assertThat(pc).doesNotImply(p11);

		assertThat(pc).accepts(p14);
		assertThat(pc).implies(p14);
		assertThat(pc).doesNotImply(p11);
		assertThat(pc).doesNotImply(p12);
		assertThat(pc).doesNotImply(p13);

		assertThat(pc).accepts(p13);
		assertThat(pc).implies(p11);
		assertThat(pc).implies(p12);
		assertThat(pc).implies(p13);
		assertThat(pc).implies(p14);

		assertThat(pc).rejects(op);

		pc = p13.newPermissionCollection();

		assertThat(pc).accepts(p13);
		assertThat(pc).implies(p13);
		assertThat(pc).doesNotImply(p11);
		assertThat(pc).doesNotImply(p12);
		assertThat(pc).doesNotImply(p14);

		assertThat(pc).accepts(p14);
		assertThat(pc).implies(p11);
		assertThat(pc).implies(p12);
		assertThat(pc).implies(p13);
		assertThat(pc).implies(p14);

		pc = p11.newPermissionCollection();

		assertThat(pc).accepts(p11);
		assertThat(pc).implies(p11);
		assertThat(pc).implies(p12);
		assertThat(pc).implies(p13);
		assertThat(pc).implies(p14);

		pc.setReadOnly();

		assertThat(pc).rejects(p12);

		assertThat(pc).hasElements();

		assertThat(p11).isSerializable();
		assertThat(p12).isSerializable();
		assertThat(p13).isSerializable();
		assertThat(p14).isSerializable();
	}

	@Test
	public void testNames() {
		TopicPermission p21 = new TopicPermission("com/foo/service2",
				"subscribe");
		TopicPermission p22 = new TopicPermission("com/foo/*", "subscribe");
		TopicPermission p23 = new TopicPermission("com/*", "subscribe");
		TopicPermission p24 = new TopicPermission("*", "subscribe"); 

		assertThat(p21).implies(p21);
		assertThat(p22).implies(p21);
		assertThat(p23).implies(p21);
		assertThat(p24).implies(p21);

		assertThat(p22).implies(p22);
		assertThat(p23).implies(p22);
		assertThat(p24).implies(p22);

		assertThat(p23).implies(p23);
		assertThat(p24).implies(p23);

		assertThat(p24).implies(p24);

		assertThat(p21).doesNotImply(p22);
		assertThat(p21).doesNotImply(p23);
		assertThat(p21).doesNotImply(p24);

		assertThat(p22).doesNotImply(p23);
		assertThat(p22).doesNotImply(p24);

		assertThat(p23).doesNotImply(p24);

		PermissionCollection pc = p21.newPermissionCollection();

		assertThat(pc).accepts(p21);
		assertThat(pc).implies(p21);
		assertThat(pc).doesNotImply(p22);
		assertThat(pc).doesNotImply(p23);
		assertThat(pc).doesNotImply(p24);

		assertThat(pc).accepts(p22);
		assertThat(pc).implies(p21);
		assertThat(pc).implies(p22);
		assertThat(pc).doesNotImply(p23);
		assertThat(pc).doesNotImply(p24);

		assertThat(pc).accepts(p23);
		assertThat(pc).implies(p21);
		assertThat(pc).implies(p22);
		assertThat(pc).implies(p23);
		assertThat(pc).doesNotImply(p24);

		assertThat(pc).accepts(p24);
		assertThat(pc).implies(p21);
		assertThat(pc).implies(p22);
		assertThat(pc).implies(p23);
		assertThat(pc).implies(p24);

		pc = p22.newPermissionCollection();

		assertThat(pc).accepts(p22);
		assertThat(pc).implies(p21);
		assertThat(pc).implies(p22);
		assertThat(pc).doesNotImply(p23);
		assertThat(pc).doesNotImply(p24);

		pc = p23.newPermissionCollection();

		assertThat(pc).accepts(p23);
		assertThat(pc).implies(p21);
		assertThat(pc).implies(p22);
		assertThat(pc).implies(p23);
		assertThat(pc).doesNotImply(p24);

		pc = p24.newPermissionCollection();

		assertThat(pc).accepts(p24);
		assertThat(pc).implies(p21);
		assertThat(pc).implies(p22);
		assertThat(pc).implies(p23);
		assertThat(pc).implies(p24);

		assertThat(p21).isSerializable();
		assertThat(p22).isSerializable();
		assertThat(p23).isSerializable();
		assertThat(p24).isSerializable();
	}
	
	@Test
	public void testActionImplications() {
		TopicPermission publish = new TopicPermission("*", "publish");
		TopicPermission subscribe = new TopicPermission("*", "subscribe");

		assertThat(publish).implies(publish);
		assertThat(publish).doesNotImply(subscribe);
		assertThat(subscribe).doesNotImply(publish);
		assertThat(subscribe).implies(subscribe);
	}

	private void invalidTopicPermission(String name, String actions) {
		try {
			TopicPermission p = new TopicPermission(name, actions);
			fail(p + " created with invalid actions"); 
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}
}
