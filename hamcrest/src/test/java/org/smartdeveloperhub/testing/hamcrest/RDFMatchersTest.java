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
 *   Artifact    : org.smartdeveloperhub.testing:sdh-testing-hamcrest:0.2.0-SNAPSHOT
 *   Bundle      : sdh-testing-hamcrest-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.testing.hamcrest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.smartdeveloperhub.testing.hamcrest.RDFMatchers.hasProperty;
import static org.smartdeveloperhub.testing.hamcrest.RDFMatchers.hasPropertyValue;
import static org.smartdeveloperhub.testing.hamcrest.RDFMatchers.hasTriple;
import static org.smartdeveloperhub.testing.hamcrest.RDFMatchers.isDefined;
import static org.smartdeveloperhub.testing.hamcrest.References.blankNode;
import static org.smartdeveloperhub.testing.hamcrest.References.from;
import static org.smartdeveloperhub.testing.hamcrest.References.literal;
import static org.smartdeveloperhub.testing.hamcrest.References.property;
import static org.smartdeveloperhub.testing.hamcrest.References.resource;
import static org.smartdeveloperhub.testing.hamcrest.References.typedLiteral;
import static org.smartdeveloperhub.testing.hamcrest.References.uriRef;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class RDFMatchersTest {

	private final String committer = "http://localhost:62521/default-harvester/ldp4j/api/service/committers/1/";
	private final String id="1234";

	private Model model;

	@Before
	public void setUp() throws IOException {
		try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("committer.ttl")) {
			this.model=
				ModelFactory.
					createDefaultModel().
						read(is,this.committer,"TURTLE");
		}
	}

	@Test
	public void testHasTriple() throws Exception {
		assertThat(
			this.model,
			hasTriple(
				uriRef(this.committer),
				property("http://www.smartdeveloperhub.org/vocabulary/scm#committerId"),
				typedLiteral(this.id,"http://www.w3.org/2001/XMLSchema#string")));
	}

	@Test
	public void testIsDefined() throws Exception {
		assertThat(
			resource(uriRef(this.committer),from(this.model)),
			isDefined());
	}

	@Test
	public void testIsDefined$combines() throws Exception {
		assertThat(
			resource(blankNode("example"),from(this.model)),
			not(isDefined()));
	}

	@Test
	public void testHasProperty() throws Exception {
		assertThat(
			resource(uriRef(this.committer),from(this.model)),
			hasProperty(
				property("http://www.smartdeveloperhub.org/vocabulary/scm#committerId")
			));
	}

	@Test
	public void testHasPropertyValue$untypedString() throws Exception {
		assertThat(
			resource(uriRef(this.committer),from(this.model)),
			hasPropertyValue(
				property("http://www.smartdeveloperhub.org/vocabulary/scm#committerId"),
				literal(this.id)
			));
	}

	@Test
	public void testHasPropertyValue$raw() throws Exception {
		assertThat(
			resource(uriRef(this.committer),from(this.model)),
			hasPropertyValue(
				property("http://www.smartdeveloperhub.org/vocabulary/scm#external"),
				literal(true)
			));
	}

}
