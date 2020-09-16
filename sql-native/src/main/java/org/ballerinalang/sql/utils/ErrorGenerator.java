/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.sql.utils;

import org.ballerinalang.jvm.api.BErrorCreator;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.BValueCreator;
import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.api.values.BMap;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.sql.Constants;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating SQL Client errors.
 *
 * @since 1.2.0
 */
public class ErrorGenerator {
    private ErrorGenerator() {
    }

    public static BError getSQLBatchExecuteError(SQLException exception,
                                                     List<BMap<BString, Object>> executionResults,
                                                     String messagePrefix) {
        String sqlErrorMessage =
                exception.getMessage() != null ? exception.getMessage() : Constants.BATCH_EXECUTE_ERROR_MESSAGE;
        int vendorCode = exception.getErrorCode();
        String sqlState = exception.getSQLState();
        String errorMessage = messagePrefix + sqlErrorMessage + ".";
        return getSQLBatchExecuteError(errorMessage, vendorCode, sqlState, executionResults);
    }

    public static BError getSQLDatabaseError(SQLException exception, String messagePrefix) {
        String sqlErrorMessage =
                exception.getMessage() != null ? exception.getMessage() : Constants.DATABASE_ERROR_MESSAGE;
        int vendorCode = exception.getErrorCode();
        String sqlState = exception.getSQLState();
        String errorMessage = messagePrefix + sqlErrorMessage + ".";
        return getSQLDatabaseError(errorMessage, vendorCode, sqlState);
    }

    public static BError getSQLApplicationError(String errorMessage) {
        return BErrorCreator.createDistinctError(Constants.APPLICATION_ERROR, Constants.SQL_PACKAGE_ID,
                                                 BStringUtils.fromString(errorMessage));
    }

    private static BError getSQLBatchExecuteError(String message, int vendorCode, String sqlState,
                                                      List<BMap<BString, Object>> executionResults) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(Constants.ErrorRecordFields.ERROR_CODE, vendorCode);
        valueMap.put(Constants.ErrorRecordFields.SQL_STATE, sqlState);
        valueMap.put(Constants.ErrorRecordFields.EXECUTION_RESULTS,
                BValueCreator.createArrayValue(executionResults.toArray(), new BArrayType(
                        new BRecordType(Constants.EXECUTION_RESULT_RECORD, Constants.SQL_PACKAGE_ID, 0, false, 0))));

        BMap<BString, Object> sqlClientErrorDetailRecord = BValueCreator.
                createRecordValue(Constants.SQL_PACKAGE_ID, Constants.BATCH_EXECUTE_ERROR_DETAIL, valueMap);
        return BErrorCreator.createDistinctError(Constants.BATCH_EXECUTE_ERROR, Constants.SQL_PACKAGE_ID,
                                                 BStringUtils.fromString(message), sqlClientErrorDetailRecord);
    }

    private static BError getSQLDatabaseError(String message, int vendorCode, String sqlState) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(Constants.ErrorRecordFields.ERROR_CODE, vendorCode);
        valueMap.put(Constants.ErrorRecordFields.SQL_STATE, sqlState);
        BMap<BString, Object> sqlClientErrorDetailRecord = BValueCreator.
                createRecordValue(Constants.SQL_PACKAGE_ID, Constants.DATABASE_ERROR_DETAILS, valueMap);
        return BErrorCreator.createDistinctError(Constants.DATABASE_ERROR, Constants.SQL_PACKAGE_ID,
                                                 BStringUtils.fromString(message), sqlClientErrorDetailRecord);
    }
}
