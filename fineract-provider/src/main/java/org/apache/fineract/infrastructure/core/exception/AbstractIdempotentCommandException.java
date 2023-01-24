/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.exception;

import lombok.Getter;

public abstract class AbstractIdempotentCommandException extends AbstractPlatformException {

    public static final String IDEMPOTENT_CACHE_HEADER = "x-served-from-cache";
    @Getter
    private final String action;

    @Getter
    private final String entity;
    @Getter
    private final String idempotencyKey;
    @Getter
    private final String response;

    protected AbstractIdempotentCommandException(String action, String entity, String idempotencyKey, String response) {
        super(null, null);
        this.action = action;
        this.entity = entity;
        this.idempotencyKey = idempotencyKey;
        this.response = response;
    }
}
