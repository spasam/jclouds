/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.date.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Adrian Cole
 */
public class DateUtils {

   public static final Pattern NANOS_TO_MILLIS_PATTERN = Pattern.compile(".*[0-9][0-9][0-9][0-9][0-9][0-9]");

   public static final Pattern TZ_PATTERN = Pattern.compile("(.*)[+-][0-9][0-9]:?[0-9][0-9]Z?");

   public static String trimNanosToMillis(String toParse) {
      if (NANOS_TO_MILLIS_PATTERN.matcher(toParse).matches())
         toParse = toParse.substring(0, toParse.length() - 3) + 'Z';
      return toParse;
   }

   public static final Pattern SECOND_PATTERN = Pattern.compile(".*[0-2][0-9]:00");

   public static String trimTZ(String toParse) {
      Matcher matcher = TZ_PATTERN.matcher(toParse);
      if (matcher.find()) {
         toParse = matcher.group(1) + 'Z';
      }
      if (toParse.length() == 25 && SECOND_PATTERN.matcher(toParse).matches())
         toParse = toParse.substring(0, toParse.length() - 6) + 'Z';
      return toParse;
   }

}