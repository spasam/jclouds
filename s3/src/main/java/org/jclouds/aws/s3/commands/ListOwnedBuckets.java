/**
 *
 * Copyright (C) 2009 Adrian Cole <adrian@jclouds.org>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.aws.s3.commands;

import java.util.List;

import org.jclouds.aws.s3.domain.S3Bucket;
import org.jclouds.http.commands.callables.xml.ParseSax;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Returns a list of all of the buckets owned by the authenticated sender of the
 * request.
 * 
 * @see http
 *      ://docs.amazonwebservices.com/AmazonS3/2006-03-01/index.html?RESTServiceGET
 *      .html
 * @author Adrian Cole
 * 
 */
public class ListOwnedBuckets extends S3FutureCommand<List<S3Bucket.Metadata>> {

    @Inject
    public ListOwnedBuckets(@Named("jclouds.http.address") String amazonHost,
	    ParseSax<List<S3Bucket.Metadata>> callable) {
	super("GET", "/", callable, amazonHost);
    }

}