/*
 * Created on 04/giu/2010
 *
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.core.support.io.model;

import org.sejda.core.manipulation.model.output.PdfOutput;

/**
 * Destination where the output will be written
 * 
 * @author Andrea Vacondio
 * 
 */
public interface Destination {

    /**
     * @return the {@link PdfOutput} where the input will be written to
     */
    PdfOutput getOutputDestination();

    /**
     * @return true if the destination should be overwritten if already exists
     */
    boolean isOverwrite();

    /**
     * DSL interface for a file destination
     * 
     * @author Andrea Vacondio
     * 
     */
    interface FileDestination extends Destination {

        // on purpose
    }

}