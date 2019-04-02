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

import java.util.Arrays;


/**
 * Represent list of instance referenced by object array, sorted by instance ID.
 * Ordering may improve performance of further array scanning.
 *
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class ObjectArraySortedList extends InstanceList {


    ObjectArraySortedList(HprofHeap h, HprofByteBuffer buf, int len, long off) {
        super(h, collectIDs(buf, len, off));
    }

    private static long[] collectIDs(HprofByteBuffer buf, int len, long off) {
        long[] array = new long[len];
        long ptr = off;
        long idSize = buf.getIDSize();
        for(int i = 0; i != len; ++i) {
            array[i] = buf.getID(ptr);
            ptr += idSize;
        }
        Arrays.sort(array);
        return array;
    }
}
