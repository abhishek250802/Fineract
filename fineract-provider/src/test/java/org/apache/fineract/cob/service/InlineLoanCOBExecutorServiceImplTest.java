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
package org.apache.fineract.cob.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.cob.data.LoanIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.exceptions.LoanAccountLockCannotBeOverruledException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InlineLoanCOBExecutorServiceImplTest {

    @InjectMocks
    private InlineLoanCOBExecutorServiceImpl testObj;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private InlineLoanCOBExecutionDataParser dataParser;
    @Mock
    private LoanRepository loanRepository;

    @Test
    void shouldExceptionThrownIfLoanIsAlreadyLocked() {
        JsonCommand command = mock(JsonCommand.class);
        LoanIdAndLastClosedBusinessDate loan = mock(LoanIdAndLastClosedBusinessDate.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        when(transactionTemplate.execute(any())).thenThrow(new LoanAccountLockCannotBeOverruledException(""));
        when(loanRepository.findAllNonClosedLoansBehindByLoanIds(any(), anyList())).thenReturn(List.of(loan));
        assertThrows(LoanAccountLockCannotBeOverruledException.class, () -> testObj.executeInlineJob(command, "INLINE_LOAN_COB"));
    }

    @Test
    void shouldOldestCloseBusinessDateReturnWithCorrectDate()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LoanIdAndLastClosedBusinessDate loan1 = mock(LoanIdAndLastClosedBusinessDate.class);
        LoanIdAndLastClosedBusinessDate loan2 = mock(LoanIdAndLastClosedBusinessDate.class);
        LoanIdAndLastClosedBusinessDate loan3 = mock(LoanIdAndLastClosedBusinessDate.class);
        when(loan1.getLastClosedBusinessDate()).thenReturn(null);
        when(loan2.getLastClosedBusinessDate()).thenReturn(LocalDate.of(2023, 1, 10));
        when(loan3.getLastClosedBusinessDate()).thenReturn(LocalDate.of(2023, 1, 11));
        assertEquals(LocalDate.of(2023, 1, 10), getOldestCOBBusinessDate().invoke(testObj, List.of(loan1, loan2, loan3)));
    }

    private Method getOldestCOBBusinessDate() throws NoSuchMethodException {
        Method method = InlineLoanCOBExecutorServiceImpl.class.getDeclaredMethod("getOldestCOBBusinessDate", List.class);
        method.setAccessible(true);
        return method;
    }
}
