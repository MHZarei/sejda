/*
 * Created on Aug 29, 2011
 * Copyright 2010 by Eduard Weissmann (edi.weissmann@gmail.com).
 * 
 * This file is part of the Sejda source code
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.sejda.model.parameter.base.AbstractPdfOutputParameters;
import org.sejda.model.pdf.PdfVersion;

/**
 * Test verifying that the --compressed and --pdfVersion flags can be specified for each task creating pdf outputs
 * 
 * @author Eduard Weissmann
 * 
 */
public class PdfFormatOutputTraitTest extends AbstractTaskTraitTest {

    @Parameters
    public static Collection<Object[]> testParameters() {
        return asParameterizedTestData(TestableTask.allTasksExceptFor(TestableTask.UNPACK, TestableTask.EXTRACT_TEXT, TestableTask.EXTRACT_TEXT_BY_PAGES,
                TestableTask.PDF_TO_SINGLE_TIFF, TestableTask.PDF_TO_MULTIPLE_TIFF, TestableTask.PDF_TO_JPEG));
    }

    public PdfFormatOutputTraitTest(TestableTask testableTask) {
        super(testableTask);
    }

    @Test
    public void onValueCompressed() {
        AbstractPdfOutputParameters result = defaultCommandLine().withFlag("--compressed").invokeSejdaConsole();

        assertTrue(describeExpectations(), result.isCompress());
    }

    @Test
    public void offValueCompressed() {
        AbstractPdfOutputParameters result = defaultCommandLine().invokeSejdaConsole();

        assertFalse(describeExpectations(), result.isCompress());
    }

    @Test
    public void specifiedValuePdfVersion() {
        AbstractPdfOutputParameters result = defaultCommandLine().with("--pdfVersion", "1.4").invokeSejdaConsole();

        assertEquals(describeExpectations(), PdfVersion.VERSION_1_4, result.getVersion());
    }

    @Test
    public void defaultValuePdfVersion() {
        AbstractPdfOutputParameters result = defaultCommandLine().invokeSejdaConsole();

        assertEquals(describeExpectations(), PdfVersion.VERSION_1_6, result.getVersion());
    }
}
