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
package org.sejda.impl.itext;

import org.sejda.core.service.CombineReorderTaskTest;
import org.sejda.model.parameter.CombineReorderParameters;
import org.sejda.model.task.Task;

public class CombineReorderITextTaskTest extends CombineReorderTaskTest {
    public Task<CombineReorderParameters> getTask() {
        return new CombineReorderTask();
    }
}