/*
 * Copyright 2015 by Edi Weissmann (edi.weissmann@gmail.com)
 *
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
 */
package org.sejda.core.support.prefix.processor;

public class PrefixUtils {

    /**
     * Strips characters deemed usafe for a filename
     */
    public static String toSafeFilename(String input) {
        return input.replaceAll("[`\0\f\t\n\r\\\\/:*?\\\"<>|]", "");
    }

    /**
     * Strips all but characters that are known to be safe: alphanumerics for now.
     */
    public static String toStrictFilename(String input) {
        String safe = input.replaceAll("[^A-Za-z0-9_ .-]", "");
        if(safe.length() > 255) {
            safe = safe.substring(0, 255);
        }
        return safe;
    }
}
