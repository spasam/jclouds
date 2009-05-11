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
package org.jclouds.aws.s3.handlers;

import java.io.InputStream;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.jclouds.aws.s3.S3ResponseException;
import org.jclouds.aws.s3.domain.S3Error;
import org.jclouds.aws.s3.filters.RequestAuthorizeSignature;
import org.jclouds.aws.s3.reference.S3Headers;
import org.jclouds.aws.s3.xml.S3ParserFactory;
import org.jclouds.http.HttpFutureCommand;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.HttpResponseHandler;
import org.jclouds.logging.Logger;


import com.google.inject.Inject;

/**
 * This will parse and set an appropriate exception on the command object.
 * 
 * @see S3Error
 * @author Adrian Cole
 * 
 */
public class ParseS3ErrorFromXmlContent implements HttpResponseHandler {
    @Resource
    protected Logger logger = Logger.NULL;

    private final S3ParserFactory parserFactory;

    @Inject
    public ParseS3ErrorFromXmlContent(S3ParserFactory parserFactory) {
	this.parserFactory = parserFactory;
    }

    public void handle(HttpFutureCommand<?> command, HttpResponse response) {
	S3Error error = new S3Error();
	error.setRequestId(response.getFirstHeaderOrNull(S3Headers.REQUEST_ID));
	error.setRequestToken(response
		.getFirstHeaderOrNull(S3Headers.REQUEST_TOKEN));
	InputStream errorStream = response.getContent();
	try {
	    if (errorStream != null) {
		error = parserFactory.createErrorParser().parse(errorStream);
		if ("SignatureDoesNotMatch".equals(error.getCode()))
		    error.setStringSigned(RequestAuthorizeSignature
			    .createStringToSign(command.getRequest()));
		error.setRequestToken(response
			.getFirstHeaderOrNull(S3Headers.REQUEST_TOKEN));
	    }
	} catch (Exception e) {
	    logger.warn(e, "error parsing XML reponse: %1$s", response);
	} finally {
	    command.setException(new S3ResponseException(command, response,
		    error));
	    IOUtils.closeQuietly(errorStream);
	}
    }

}