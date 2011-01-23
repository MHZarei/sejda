/*
 * Created on 30/mag/2010
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
package org.sejda.core.manipulation.model.output;

import java.io.OutputStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * {@link OutputStream} output destination for a task.
 * 
 * @author Andrea Vacondio
 * 
 */
public final class PdfStreamOutput implements PdfOutput {

    @NotNull
    private final OutputStream stream;

    private PdfStreamOutput(OutputStream stream) {
        this.stream = stream;
    }

    public OutputStream getStream() {
        return stream;
    }

    public OutputType getOutputType() {
        return OutputType.STREAM_OUTPUT;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(getOutputType()).toString();
    }

    /**
     * Creates a new instance of a PdfOutput using the input stream
     * 
     * @param stream
     * @return the newly created instance
     * @throws IllegalArgumentException
     *             if the input stream is null
     */
    public static PdfStreamOutput newInstance(OutputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("A not null stream instance is expected.");
        }
        return new PdfStreamOutput(stream);
    }
}
