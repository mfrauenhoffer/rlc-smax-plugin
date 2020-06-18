package com.microfocus.rlc.plugin;

import com.microfocus.rlc.plugin.domain.SmaxEntity;
import com.microfocus.rlc.plugin.domain.SmaxSession;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.data.model.INotificationInfo;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IExecutionProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmaxExecutionServiceProvider extends SmaxBaseServiceProvider implements IExecutionProvider {

    private static final Logger logger = LoggerFactory.getLogger(SmaxExecutionServiceProvider.class);

    private static final String CHECK_FIELD_VALUE = "checkFieldValue";
    private static final String WAIT_UNTIL = "waitUntil";
    private static final String FIELD_TO_CHECK = "fieldToCheck";
    private static final String EXPECTED_VALUE = "expectedValue";

    @ConfigProperty(name = "polling_interval", displayName = "Polling Interval",
            defaultValue = "10", description = "Specify polling interval in seconds",
            dataType = DataType.NUMERIC)
    private String pollingInterval;

    public String getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(String val) {
        pollingInterval = val;
    }

    @Override
    public ExecutionInfo cancelExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties)
            throws ProviderException {
                executionInfo.setStatus(ExecutionStatus.CANCELED);
                executionInfo.setMessage("Execution cancelled");
                executionInfo.setPollingInterval(-1);
                return executionInfo;
    }

    @Override
    @Service(name = EXECUTE, displayName = "Execute", description = "Execute a action")
    @Params(params = {
            @Param(fieldName = ACTION, description = "Action to execute", required = true, dataType = DataType.SELECT),
    })
    public ExecutionInfo execute(String actionId, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        switch (actionId) {
            case CHECK_FIELD_VALUE:
                return checkFieldValue(actionId, taskTitle, taskDescription, properties);
            default:
                return null;
        }
    }

    @Action(name = CHECK_FIELD_VALUE, displayName = "Check Approval Status (Polling)")
    @Params(params = {
        @Param(fieldName = INTEGRATION_ENTITY_ID, displayName = "SMAX Entity", description = "Select an integration entity item", required = true,
            deployUnit = true, dataType = DataType.SELECT, mappableTypes = "SmaxEntityProvider",
            traits = {@Trait(name = TRAIT_DISPLAY_FIELD, value = SmaxEntity.ENTITY_TITLE_KEY)}),
        @Param(fieldName = FIELD_TO_CHECK, displayName = "Field to Check", description = "The internal value of the field you want to verify in SMAX", required = true,
            dataType = DataType.TEXT),
        @Param(fieldName = EXPECTED_VALUE, displayName = "Expected Value", description = "The expected value that should be in the field you want to verify.", required = true,
            dataType = DataType.TEXT),
        @Param(fieldName = WAIT_UNTIL, displayName = "Wait Until", description = "Wait until some date and time. Action will be marked as failed if item does not receive approval by that moment.", 
            required = false, dataType = DataType.DATETIME, environmentProperty = true)
    })
    public ExecutionInfo checkFieldValue(String actionId, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        validate(actionId, taskTitle, taskDescription, properties);
        ExecutionInfo executionInfo = new ExecutionInfo();

        String id = Field.getFieldByName(properties, INTEGRATION_ENTITY_ID).getValue();
        String fieldToCheckString = Field.getFieldByName(properties, FIELD_TO_CHECK).getValue();
        String expectedValueString = Field.getFieldByName(properties, EXPECTED_VALUE).getValue();

        logger.debug("fieldToCheckString = " + fieldToCheckString);
        logger.debug("expectedValueString = '" + expectedValueString + "'");

        List<String> propertyLayout = new ArrayList<>();
        propertyLayout.add(getEntityTitleParam());
        propertyLayout.add(getEntityDescParam());
        propertyLayout.add(getEntityCreatorParam());
        propertyLayout.add(getEntityCreationTimestampParam());
        propertyLayout.add(fieldToCheckString);
        SmaxSession session = new SmaxSession(
                client.startSmaxSession(getServerUrl(), getTenantId(), getUserName(), getPassword()));
        logger.debug("SMAX Session ID: " + session.getSessionId());
        SmaxEntity entity = client.getRecord(getServerUrl(), getTenantId(), session.getSessionId(), getEntityType(), propertyLayout, id);
        String propertyValue = "";
        if (entity != null) {
            logger.debug("Entity not null");
            executionInfo.setSuccess(true);
            propertyValue = entity.getPropertyAsString(fieldToCheckString);
            logger.debug("Entity Property '" + fieldToCheckString + "' = " + propertyValue);
        }
    
        if (StringUtils.trim(propertyValue).equals(StringUtils.trim(expectedValueString))) {
            executionInfo.setStatus(ExecutionStatus.COMPLETED);
            executionInfo.setMessage("SMAX Entity Field '" + fieldToCheckString + "' equaled the expected value '" + expectedValueString + "'.");
            return executionInfo;
        }

        Field waitUntilField = Field.getFieldByName(properties, WAIT_UNTIL);
        if (waitUntilField != null && waitUntilField.getValue() != null) {
            Date waitUntil = new Date(Long.parseLong(waitUntilField.getValue()));
            if (waitUntil.before(new Date())) {
                executionInfo.setStatus(ExecutionStatus.FAILED);
                executionInfo.setMessage("SMAX Entity Field '" + fieldToCheckString + "' did not match the expected value before " + waitUntil);
                return executionInfo;
            }
        }

        if (executionInfo.getPollingStartTimestamp() == null) {
            executionInfo.setStatus(ExecutionStatus.IN_PROGRESS);
        }
        executionInfo.setMessage("SMAX Entity Field '" + fieldToCheckString + "' does not match the expected value.");
        executionInfo.setPollingInterval(new Integer(getPollingInterval()));

        return executionInfo;
    }

    @Override
    public ActionInfo getActionInfo(String action) throws ProviderException {
        return AnnotationUtil.getActionInfo(this.getClass(), action);
    }

    @Override
    public ActionInfoResult getActions() throws ProviderException {
        List<ActionInfo> actions = AnnotationUtil.getActions(this.getClass());
        return new ActionInfoResult(0, actions.size(), actions.toArray(new ActionInfo[actions.size()]));
    }

    @Override
    public INotificationInfo parseNotification(INotificationInfo arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutionInfo retryExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties)
            throws ProviderException {
        return execute(action, taskTitle, taskDescription, properties);
    }

    @Override
    public ExecutionInfo validate(String actionId, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        // Check and ensure the task is valid.
        // First, check if there is an integration entity associated.
        Field field = Field.getFieldByName(properties, INTEGRATION_ENTITY_ID);
        if (field == null || StringUtils.isEmpty(field.getValue())) {
            throw new ProviderException(String.format("Missing required property \"%s\"!", INTEGRATION_ENTITY_ID));
        }

        String integrationEntityId = field.getValue();

        // Then, check if there is a valid field to check value in place.
        if (actionId.equals(CHECK_FIELD_VALUE)) {
            field = Field.getFieldByName(properties, FIELD_TO_CHECK);
            if (field == null || StringUtils.isEmpty(field.getValue())) {
                throw new ProviderException(String.format("Missing required property \"%s\"!", FIELD_TO_CHECK));
            }
        }

        // Now, check if the integration entity actually exists within SMAX
        SmaxSession session = new SmaxSession(client.startSmaxSession(getServerUrl(), getTenantId(), getUserName(), getPassword()));
        List<String> propertyLayout = new ArrayList<>();
        propertyLayout.add(getEntityTitleParam());
        SmaxEntity integrationEntity = client.getRecord(getServerUrl(), getTenantId(), session.getSessionId(), getEntityType(), propertyLayout, integrationEntityId);
        if (integrationEntity == null) {
            throw new ProviderException(String.format("Integration entity with id '%s' does not exist", integrationEntityId));
        }

        ExecutionInfo info = new ExecutionInfo();
        info.setSuccess(true);
        info.setMessage("Validation successful");
        return info;
    }

    @Override
    public ExecutionInfo checkExecution(ExecutionInfo executionInfo, String action, String taskTitle, String taskDescription, List<Field> properties) throws ProviderException {
        if (action.equals(CHECK_FIELD_VALUE)) {
            return checkFieldValue(action, taskTitle, taskDescription, properties);
        }
        return null;
    }
}