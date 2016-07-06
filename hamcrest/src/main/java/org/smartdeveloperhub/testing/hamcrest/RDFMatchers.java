/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.testing:sdh-testing-hamcrest:0.1.0
 *   Bundle      : sdh-testing-hamcrest-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.testing.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.smartdeveloperhub.testing.hamcrest.References.Context;
import org.smartdeveloperhub.testing.hamcrest.References.Reference;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public final class RDFMatchers {

	private static final class IsDefined extends TypeSafeMatcher<Context<Resource>> {

		private IsDefined() {
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("was defined");
		}

		@Override
		protected boolean matchesSafely(final Context<Resource> item) {
			final StmtIterator properties = item.entity().listProperties();
			try {
				return properties.hasNext();
			} finally {
				properties.close();
			}
		}

	}

	private static final class HasProperty extends TypeSafeMatcher<Context<Resource>> {

		private final Reference<Property> property;

		private HasProperty(final Reference<Property> property) {
			this.property = property;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("had property ").appendValue(this.property);
		}

		@Override
		protected boolean matchesSafely(final Context<Resource> item) {
			final StmtIterator properties = item.entity().listProperties(this.property.resolve(item.model()));
			try {
				return properties.hasNext();
			} finally {
				properties.close();
			}
		}

	}

	private static final class HasPropertyValue extends TypeSafeMatcher<Context<Resource>> {

		private final Reference<Property> property;
		private final Reference<? extends RDFNode> value;

		private HasPropertyValue(final Reference<Property> property, final Reference<? extends RDFNode> value) {
			this.property = property;
			this.value = value;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("had value ").appendValue(this.value).appendText(" for property ").appendValue(this.property);
		}

		@Override
		protected boolean matchesSafely(final Context<Resource> item) {
			final Model model = item.model();
			final StmtIterator values=
				model.
					listStatements(
						item.entity(),
						this.property.resolve(model),
						this.value.resolve(model));
			try {
				final boolean result = values.hasNext();
				if(!result) {
					diagnose(item);
				}
				return result;
			} finally {
				values.close();
			}
		}

		private void diagnose(final Context<Resource> item) {
			final Model model = item.model();
			final StmtIterator values=item.entity().listProperties(this.property.resolve(model));
			final RDFNode expected = this.value.resolve(model);
			try {
				while(values.hasNext()) {
					final Statement statement = values.next();
					final RDFNode actual = statement.getObject();
					System.err.println(actual+".equals("+expected+")="+actual.equals(expected));
				}
			} finally {
				values.close();
			}
		}

	}

	private static final class HasTriple extends TypeSafeDiagnosingMatcher<Model> {

		private final Reference<? extends Resource> subject;
		private final Reference<? extends Property> predicate;
		private final Reference<? extends RDFNode> object;

		private HasTriple(final Reference<? extends Resource> subject, final Reference<? extends Property> predicate, final Reference<? extends RDFNode> object) {
			this.subject = subject;
			this.predicate = predicate;
			this.object = object;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("had triple {").appendValue(this.subject).appendText(", ").appendValue(this.predicate).appendText(", ").appendValue(this.object).appendText("}");
		}

		@Override
		protected boolean matchesSafely(final Model item, final Description mismatchDescription) {
			final Resource resource = this.subject.resolve(item);
			if(!isResourceDefined(item, resource)) {
				mismatchDescription.appendText("Resource '").appendValue(this.subject).appendText("' is not defined");
				return false;
			}
			final Property property = this.predicate.resolve(item);
			if(!item.contains(resource, property)) {
				mismatchDescription.appendText("Property '").appendValue(this.predicate).appendText("' is not defined for resource '").appendValue(this.subject).appendText("'");
				return false;
			}
			if(!item.contains(resource,property,this.object.resolve(item))) {
				mismatchDescription.appendText("Property '").appendValue(this.predicate).appendText("' value '").appendValue(this.object).appendText("' is not defined for resource '").appendValue(this.subject).appendText("'");
				return false;
			}
			return true;
		}

		private boolean isResourceDefined(final Model item, final Resource resource) {
			final StmtIterator resourceStatements = item.listStatements(resource,(Property)null,(RDFNode)null);
			try {
				return resourceStatements.hasNext();
			} finally {
				resourceStatements.close();
			}
		}

	}

	public static Matcher<Model> hasTriple(final Reference<? extends Resource> subject, final Reference<? extends Property> predicate, final Reference<? extends RDFNode> object) {
		return new HasTriple(subject, predicate, object);
	}

	public static Matcher<? super Context<Resource>> isDefined() {
		return new IsDefined();
	}

	public static Matcher<? super Context<Resource>> hasProperty(final Reference<Property> property) {
		return new HasProperty(property);
	}

	public static Matcher<? super Context<Resource>> hasPropertyValue(final Reference<Property> property, final Reference<? extends RDFNode> value) {
		return new HasPropertyValue(property, value);
	}

}