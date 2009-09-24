/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
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
package org.jclouds.aws.s3.xml;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import org.apache.commons.io.IOUtils;
import org.jclouds.PerformanceTest;
import org.jclouds.aws.s3.domain.BucketMetadata;
import org.jclouds.aws.s3.domain.CanonicalUser;
import org.jclouds.aws.s3.domain.ListBucketResponse;
import org.jclouds.aws.s3.domain.ObjectMetadata;
import org.jclouds.aws.s3.xml.S3ParserFactory;
import org.jclouds.aws.s3.xml.config.S3ParserModule;
import org.jclouds.http.HttpException;
import org.jclouds.http.HttpUtils;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.http.functions.config.ParserModule;
import org.joda.time.DateTime;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests parsing of S3 responses
 * 
 * @author Adrian Cole
 */
@Test(groups = { "performance" }, testName = "s3.S3ParserTest")
public class S3ParserTest extends PerformanceTest {
   Injector injector = null;

   public static final String listAllMyBucketsResultOn200 = "<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/callables/\"><Owner><ID>e1a5f66a480ca99a4fdfe8e318c3020446c9989d7004e7778029fbcc5d990fa0</ID></Owner><Buckets><Bucket><Name>adrianjbosstest</Name><CreationDate>2009-03-12T02:00:07.000Z</CreationDate></Bucket><Bucket><Name>adrianjbosstest2</Name><CreationDate>2009-03-12T02:00:09.000Z</CreationDate></Bucket></Buckets></ListAllMyBucketsResult>";

   S3ParserFactory parserFactory = null;

   @BeforeTest
   protected void setUpInjector() {
      injector = Guice.createInjector(new ParserModule(), new S3ParserModule());
      parserFactory = injector.getInstance(S3ParserFactory.class);
      assert parserFactory != null;
   }

   @AfterTest
   protected void tearDownInjector() {
      parserFactory = null;
      injector = null;
   }

   @Test
   void testParseListAllMyBucketsSerialResponseTime() throws HttpException {
      for (int i = 0; i < LOOP_COUNT; i++)
         runParseListAllMyBuckets();
   }

   private List<BucketMetadata> runParseListAllMyBuckets() throws HttpException {
      return parserFactory.createListBucketsParser().parse(
               IOUtils.toInputStream(listAllMyBucketsResultOn200));
   }

   @Test
   void testParseListAllMyBucketsParallelResponseTime() throws InterruptedException,
            ExecutionException {
      CompletionService<List<BucketMetadata>> completer = new ExecutorCompletionService<List<BucketMetadata>>(
               exec);
      for (int i = 0; i < LOOP_COUNT; i++)
         completer.submit(new Callable<List<BucketMetadata>>() {
            public List<BucketMetadata> call() throws IOException, SAXException, HttpException {
               return runParseListAllMyBuckets();
            }
         });
      for (int i = 0; i < LOOP_COUNT; i++)
         assert completer.take().get() != null;
   }

   @Test
   public void testCanParseListAllMyBuckets() throws HttpException {
      List<BucketMetadata> s3Buckets = runParseListAllMyBuckets();
      BucketMetadata container1 = s3Buckets.get(0);
      assert container1.getName().equals("adrianjbosstest");
      DateTime expectedDate1 = new DateTime("2009-03-12T02:00:07.000Z");
      DateTime date1 = container1.getCreationDate();
      assert date1.equals(expectedDate1);
      BucketMetadata container2 = s3Buckets.get(1);
      assert container2.getName().equals("adrianjbosstest2");
      DateTime expectedDate2 = new DateTime("2009-03-12T02:00:09.000Z");
      DateTime date2 = container2.getCreationDate();
      assert date2.equals(expectedDate2);
      assert s3Buckets.size() == 2;
      CanonicalUser owner = new CanonicalUser(
               "e1a5f66a480ca99a4fdfe8e318c3020446c9989d7004e7778029fbcc5d990fa0");
      assert container1.getOwner().equals(owner);
      assert container2.getOwner().equals(owner);
   }

   public static final String listContainerResult = "<ListContainerHandler xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\"><Name>adrianjbosstest</Name><Prefix></Prefix><Marker></Marker><MaxKeys>1000</MaxKeys><IsTruncated>false</IsTruncated><Contents><Key>3366</Key><LastModified>2009-03-12T02:00:13.000Z</LastModified><ETag>&quot;9d7bb64e8e18ee34eec06dd2cf37b766&quot;</ETag><Size>136</Size><Owner><ID>e1a5f66a480ca99a4fdfe8e318c3020446c9989d7004e7778029fbcc5d990fa0</ID><DisplayName>ferncam</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents></ListContainerHandler>";

   public void testCanParseListContainerResult() throws HttpException, UnsupportedEncodingException {
      ListBucketResponse container = runParseListContainerResult();
      assert !container.isTruncated();
      assert container.getBucketName().equals("adrianjbosstest");
      assert container.size() == 1;
      ObjectMetadata object = container.iterator().next();
      assert object.getKey().equals("3366");
      DateTime expected = new DateTime("2009-03-12T02:00:13.000Z");
      assert object.getLastModified().equals(expected) : String.format(
               "expected %1$s, but got %1$s", expected, object.getLastModified());
      assertEquals(HttpUtils.toHexString(object.getETag()), "9d7bb64e8e18ee34eec06dd2cf37b766");
      assert object.getSize() == 136;
      CanonicalUser owner = new CanonicalUser(
               "e1a5f66a480ca99a4fdfe8e318c3020446c9989d7004e7778029fbcc5d990fa0");
      owner.setDisplayName("ferncam");
      assert object.getOwner().equals(owner);
      assert object.getStorageClass().equals("STANDARD");
   }

   private ListBucketResponse runParseListContainerResult() throws HttpException {
      ParseSax<ListBucketResponse> parser = parserFactory.createListBucketParser();
      return parser.parse(IOUtils.toInputStream(listContainerResult));
   }

   public static final String successfulCopyObject200 = "<CopyObjectResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\"><LastModified>2009-03-19T13:23:27.000Z</LastModified><ETag>\"92836a3ea45a6984d1b4d23a747d46bb\"</ETag></CopyObjectResult>";

   private ObjectMetadata runParseCopyObjectResult() throws HttpException {
      ParseSax<ObjectMetadata> parser = parserFactory.createCopyObjectParser();
      return parser.parse(IOUtils.toInputStream(successfulCopyObject200));
   }

   public void testCanParseCopyObjectResult() throws HttpException, UnsupportedEncodingException {
      ObjectMetadata metadata = runParseCopyObjectResult();
      DateTime expected = new DateTime("2009-03-19T13:23:27.000Z");
      assertEquals(metadata.getLastModified(), expected);
      assertEquals(HttpUtils.toHexString(metadata.getETag()), "92836a3ea45a6984d1b4d23a747d46bb");
   }

   @Test
   void testParseListContainerResultSerialResponseTime() throws HttpException {
      for (int i = 0; i < LOOP_COUNT; i++)
         runParseListContainerResult();
   }

   @Test
   void testParseListContainerResultParallelResponseTime() throws InterruptedException,
            ExecutionException {
      CompletionService<ListBucketResponse> completer = new ExecutorCompletionService<ListBucketResponse>(
               exec);
      for (int i = 0; i < LOOP_COUNT; i++)
         completer.submit(new Callable<ListBucketResponse>() {
            public ListBucketResponse call() throws IOException, SAXException, HttpException {
               return runParseListContainerResult();
            }
         });
      for (int i = 0; i < LOOP_COUNT; i++)
         assert completer.take().get() != null;
   }

}