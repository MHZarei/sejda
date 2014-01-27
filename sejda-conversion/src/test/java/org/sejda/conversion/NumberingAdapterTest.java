/*
 * Created on 30/dic/2012
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.sejda.conversion.exception.ConversionException;
import org.sejda.model.pdf.headerfooter.NumberingStyle;

/**
 * @author Andrea Vacondio
 * 
 */
public class NumberingAdapterTest {

    @Test
    public void positives() {
        assertThat(new NumberingAdapter("22:arabic").getNumbering().getLogicalPageNumber(), is(22));
        assertThat(new NumberingAdapter("1:arabic").getNumbering().getNumberingStyle(), is(NumberingStyle.ARABIC));
    }

    @Test(expected = ConversionException.class)
    public void invalidNumber() {
        new NumberingAdapter("a:arabic");
    }

    @Test(expected = ConversionException.class)
    public void invalidStyle() {
        new NumberingAdapter("1:noStyle");
    }

    @Test(expected = ConversionException.class)
    public void invalidTokens() {
        new NumberingAdapter("1:arabic:chuck");
    }
}
