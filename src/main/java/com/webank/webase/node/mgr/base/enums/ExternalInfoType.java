/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.base.enums;

public enum ExternalInfoType {
    /**
     * return all external data
     */
    ALL(1),
    /**
     * return normal data(user/contract in webase)
     */
    NORMAL(2),
    /**
     * return data not in webase
     */
    ABNORMAL(3);

    private int value;

    ExternalInfoType(Integer value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
