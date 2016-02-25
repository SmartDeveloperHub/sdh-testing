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
 *   Artifact    : org.smartdeveloperhub.testing:sdh-testing-hamcrest:0.1.0-SNAPSHOT
 *   Bundle      : sdh-testing-hamcrest-0.1.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.testing.hamcrest;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public final class References {

	public interface Provider<T> {

		T provide();
	}

	public interface Reference<T> {

		T resolve(Model model);

	}

	public static final class Context<T> {

		private final Model model;
		private final Reference<? extends T> reference;

		private Context(final Model model, final Reference<? extends T> reference) {
			this.model = model;
			this.reference = reference;
		}

		Model model() {
			return this.model;
		}

		T entity() {
			return this.reference.resolve(this.model);
		}

		@Override
		public String toString() {
			return this.reference.toString();
		}

	}

	private static final class URIRefReference implements Reference<Resource> {

		private final String uri;

		private URIRefReference(final String uri) {
			this.uri = uri;
		}

		@Override
		public Resource resolve(final Model model) {
			return model.createResource(this.uri);
		}

		@Override
		public String toString() {
			return "<"+this.uri+">";
		}

	}

	private static final class PropertyReference implements Reference<Property> {

		private final String uri;

		private PropertyReference(final String uri) {
			this.uri = uri;
		}

		@Override
		public Property resolve(final Model model) {
			return model.createProperty(this.uri);
		}

		@Override
		public String toString() {
			return "<"+this.uri+">";
		}

	}

	private static final class BNodeReference implements Reference<Resource> {

		private final String id;

		public BNodeReference(final String id) {
			this.id = id;
		}

		@Override
		public Resource resolve(final Model model) {
			return model.createResource(AnonId.create(this.id));
		}

		@Override
		public String toString() {
			return "_:"+this.id;
		}

	}

	private static final class LiteralReference implements Reference<Literal> {

		private final Object value;
		private final String typeURI;
		private final String language;

		private LiteralReference(final Object value, final String datatype) {
			this.value = value;
			this.typeURI = datatype;
			this.language=null;
		}

		private LiteralReference(final String value, final String language) {
			this.value=value;
			this.typeURI=null;
			this.language=language;
		}

		private LiteralReference(final Object value) {
			this.value=value;
			this.typeURI=null;
			this.language=null;
		}

		@Override
		public Literal resolve(final Model model) {
			if(this.typeURI!=null) {
				return model.createTypedLiteral(this.value,this.typeURI);
			} else if(this.language!=null) {
				return model.createLiteral(this.value.toString(), this.language);
			} else {
				return model.createTypedLiteral(this.value);
			}
		}

		@Override
		public String toString() {
			if(this.typeURI!=null) {
				return "\""+this.value+"\"@<"+this.typeURI+">";
			} else if(this.language!=null) {
				return "\""+this.value+"\"^^"+this.language;
			} else {
				return "\""+this.value+"\"@<"+this.value.getClass().getSimpleName()+">";
			}
		}

	}

	private static final Map<String,String> NAMESPACES;

	static {
		NAMESPACES=Maps.newLinkedHashMap();
		useNamespacePrefix("rdf",          "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		useNamespacePrefix("rdfs",         "http://www.w3.org/2000/01/rdf-schema#");
		useNamespacePrefix("xsd",          "http://www.w3.org/2001/XMLSchema#");

		useNamespacePrefix("ldp",          "http://www.w3.org/ns/ldp#");
		useNamespacePrefix("ldp4j",        "http://www.ldp4j.org/ns/application#");
		useNamespacePrefix("cnts",         "http://www.ldp4j.org/ns/constraints#");
		useNamespacePrefix("sh",           "http://www.w3.org/ns/shacl#");
		useNamespacePrefix("http",         "http://www.w3.org/2011/http#");
		useNamespacePrefix("cnt",          "http://www.w3.org/2011/content#");
		useNamespacePrefix("http-methods", "http://www.w3.org/2011/http-methods#");
		useNamespacePrefix("http-headers", "http://www.w3.org/2011/http-headers#");

		useNamespacePrefix("foaf",         "http://xmlns.com/foaf/0.1/");
		useNamespacePrefix("doap",         "http://usefulinc.com/ns/doap#");
		useNamespacePrefix("dctypes",      "http://purl.org/dc/dcmitype/");
		useNamespacePrefix("dcterms",      "http://purl.org/dc/terms/");
	}

	private static void useNamespacePrefix(final String prefix, final String namespace) {
		Objects.requireNonNull(prefix,"Prefix cannot be null");
		Objects.requireNonNull(namespace,"Namespace cannot be null");
		NAMESPACES.put(prefix, namespace);
	}

	public static Reference<Resource> uriRef(final String uri) {
		return new URIRefReference(uri);
	}

	public static Reference<Resource> uriRef(final URI uri) {
		return uriRef(uri.toString());
	}

	public static Reference<Resource> uriRef(final URL uri) {
		return uriRef(uri.toString());
	}

	public static Reference<Resource> blankNode(final String uri) {
		return new BNodeReference(uri);
	}

	public static Reference<Property> property(final String uri) {
		return new PropertyReference(uri);
	}

	public static Reference<Property> property(final URI uri) {
		return property(uri.toString());
	}

	public static Reference<Property> property(final URL uri) {
		return property(uri.toString());
	}

	public static Reference<Literal> literal(final Object value) {
		return new LiteralReference(value);
	}

	public static Reference<Literal> languageliteral(final String value, final String language) {
		return new LiteralReference(value,language);
	}

	public static Reference<Literal> typedLiteral(final Object value, final String datatype) {
		return new LiteralReference(value,datatype);
	}

	public static Reference<Literal> typedLiteral(final Object value, final URI datatype) {
		return typedLiteral(value,datatype.toString());
	}

	public static Reference<Literal> typedLiteral(final Object value, final URL datatype) {
		return typedLiteral(value,datatype.toString());
	}

	public static Provider<Model> from(final Model model) {
		return new Provider<Model>() {
			@Override
			public Model provide() {
				return model;
			}

		};
	}

	public static Context<Resource> resource(final Reference<? extends Resource> reference, final Provider<Model> model) {
		return new Context<Resource>(model.provide(),reference);
	}

}