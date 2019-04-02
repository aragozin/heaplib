/*
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
 */

package org.netbeans.lib.profiler.heap;

import java.util.List;


/**
 * represents instance of array of objects
 * @author Tomas Hurka
 */
public interface ObjectArrayInstance extends Instance {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * return number of elements in the array (arr.length).
     * <br>
     * Speed: fast
     * @return number of elements in the array
     */
    int getLength();

    /**
     * returns list of elements. The elements are instances of {@link Instance}.
     * The list is ordered as the original array.
     * <br>
     * Speed: normal
     * @return list {@link Instance} of elements.
     */
    List<Instance> getValues();

    /**
     * returns list of instance IDs for elements of this array.
     * The list is ordered as the original array.
     * <br>
     * Speed: normal
     * @return list {@link Long} of elements.
     */
    List<Long> getValueIDs();

    /**
     * returns list of elements. The elements are instances of {@link Instance}.
     * The list is ordered by instance ID which could improve performance of further scan,
     * provided that original ordering is not important.
     * <br>
     * Speed: normal
     * @return list {@link Instance} of elements.
     */
    List<Instance> getValuesSortedByID();
}
