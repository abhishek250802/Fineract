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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.cob.data.JobBusinessStepDetail;

@Slf4j
public final class IdempotencyHelper {

    private static final String BUSINESS_STEPS_API_URL_START = "/fineract-provider/api/v1/jobs/";
    private static final String BUSINESS_STEPS_API_URL_END = "/steps?" + Utils.TENANT_IDENTIFIER;
    private static final String GET_AVAILABLE_BUSINESS_STEPS_API_URL_END = "/available-steps?" + Utils.TENANT_IDENTIFIER;

    private IdempotencyHelper() {

    }

    public static String toJsonString(final List<BusinessStep> batchRequests) {
        return new Gson().toJson(new BusinessStepWrapper(batchRequests));
    }

    public static JobBusinessStepConfigData configuredBusinessStepFromJsonString(final String json) {
        return new Gson().fromJson(json, new TypeToken<JobBusinessStepConfigData>() {}.getType());
    }

    private static JobBusinessStepDetail availableBusinessStepFromJsonString(final String json) {
        return new Gson().fromJson(json, new TypeToken<JobBusinessStepDetail>() {}.getType());
    }

    public static JobBusinessStepConfigData getConfiguredBusinessStepsByJobName(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, String jobName) {
        final String response = Utils.performServerGet(requestSpec, responseSpec,
                BUSINESS_STEPS_API_URL_START + jobName + BUSINESS_STEPS_API_URL_END);
        log.info("BusinessStepConfigurationHelper Response: {}", response);
        return configuredBusinessStepFromJsonString(response);
    }

    public static JobBusinessStepDetail getAvailableBusinessStepsByJobName(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, String jobName) {
        final String response = Utils.performServerGet(requestSpec, responseSpec,
                BUSINESS_STEPS_API_URL_START + jobName + GET_AVAILABLE_BUSINESS_STEPS_API_URL_END);
        log.info("BusinessStepConfigurationHelper Response: {}", response);
        return availableBusinessStepFromJsonString(response);
    }

    public static Response updateBusinessStepOrder(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            String jobName, String jsonBodyToSend, String idempotencyKey) {
        Response response = Utils.performServerPutRaw(requestSpec, responseSpec,
                BUSINESS_STEPS_API_URL_START + jobName + BUSINESS_STEPS_API_URL_END,
                request -> request.header("Idempotency-Key", idempotencyKey).body(jsonBodyToSend));
        log.info("BusinessStepConfigurationHelper Response: {}", response.getBody().asString());
        return response;
    }

    public static Response updateBusinessStepOrderWithError(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, String jobName, String jsonBodyToSend, String idempotencyKey) {
        String url = BUSINESS_STEPS_API_URL_START + jobName + BUSINESS_STEPS_API_URL_END;
        return Utils.performServerPutRaw(requestSpec, responseSpec, url,
                request -> request.header("Idempotency-Key", idempotencyKey).body(jsonBodyToSend));
    }

    private static final class BusinessStepWrapper {

        private List<BusinessStep> businessSteps;

        private BusinessStepWrapper(List<BusinessStep> businessSteps) {
            this.businessSteps = businessSteps;
        }
    }
}
