package com.microfocus.rlc.plugin;

import com.microfocus.rlc.plugin.domain.SmaxBulkOperationResult;
import com.microfocus.rlc.plugin.domain.SmaxEntity;
import com.microfocus.rlc.plugin.domain.SmaxEntityOperation;
import com.microfocus.rlc.plugin.domain.SmaxSession;
import com.serena.rlc.provider.annotations.*;
import com.serena.rlc.provider.domain.*;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IIntegrationEntityProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.microfocus.rlc.plugin.domain.SmaxEntity.*;

public class SmaxEntityProvider extends SmaxBaseServiceProvider implements IIntegrationEntityProvider {

    private static final Logger logger = LoggerFactory.getLogger(SmaxEntityProvider.class);
    
    private static final String CREATE_INTEGRATION_ENTITY = "createIntegrationEntity";

    @ConfigProperty(name = SmaxEntity.ENTITY_ADDITIONAL_PARAMS_KEY, displayName = "Additional Entity Fields to Return",
                    description = "Insert field names defined in SMAX that you wish to retrieve. These must be in the following format: fieldName:displayName:dataTypeInSmax", defaultValue = "", dataType = DataType.TEXTAREA)
    private String strEntityAddtlParams;

    public String getEntityAddtlParams() {
        return strEntityAddtlParams;
    }

    @Override
    @Service(name = CREATE_INTEGRATION_ENTITIES, description = "Create a SMAX Record")
    public ProviderInfoResult createIntegrationEntities(String action, List<Field> properties)
            throws ProviderException {
        if (action.equalsIgnoreCase(CREATE_INTEGRATION_ENTITY))
            return createIntegrationEntity(action, properties);

        return null;
    }

    @Override
    @Service(name = FIND_INTEGRATION_ENTITIES, displayName = "Find Items", description = "Find SMAX Item integration entities from the selected table")
    @Params(params = {})
    public ProviderInfoResult findIntegrationEntities(List<Field> properties, Long startIndex,
            Long resultCount) throws ProviderException {
        List<String> propertyLayout = new ArrayList<>();
        propertyLayout.add(getEntityTitleParam());
        propertyLayout.add(getEntityDescParam());
        propertyLayout.add(getEntityCreatorParam());
        propertyLayout.add(getEntityCreationTimestampParam());
        SmaxSession session = new SmaxSession(
                client.startSmaxSession(getServerUrl(), getTenantId(), getUserName(), getPassword()));
        logger.debug("SMAX Session ID: " + session.getSessionId());
        List<ProviderInfo> resultsList = client
                .getRecords(getServerUrl(), getTenantId(), session.getSessionId(), getEntityType(), propertyLayout)
                .stream().map(this::createProviderInfo).collect(Collectors.toList());
        logger.debug("In SmaxEntityProvider:findIntegrationEntities(), resultsListsize: " + resultsList.size());
        return new ProviderInfoResult(0, resultsList.size(), resultsList.toArray(new ProviderInfo[resultsList.size()]));
    }

    @Action(name = CREATE_INTEGRATION_ENTITY, displayName = "Create SMAX Item")
    @Params(params = {
            @Param(fieldName = ENTITY_TITLE_KEY, displayName = "Title", description = "Specify the value of the Title field", required = true, dataType = DataType.TEXT),
            @Param(fieldName = ENTITY_DESCRIPTION_KEY, displayName = "Description", description = "Specify the value of the Description field", required = true, dataType = DataType.TEXTAREA),
            @Param(fieldName = ENTITY_ADDITIONAL_PARAMS_KEY, displayName = "Additional Field Values", description = "propertyName1:propertyValue1,propertyName2:propertyValue2", dataType = DataType.TEXTAREA)
    })
    public ProviderInfoResult createIntegrationEntity(String action, List<Field> properties) throws ProviderException {
        try {
            SmaxEntity entity = new SmaxEntity();
            entity.setEntityType(getEntityType());
            entity.setEntityProperty(getEntityTitleParam(), Field.getFieldByName(properties, ENTITY_TITLE_KEY).getValue());
            entity.setEntityProperty(getEntityDescParam(), Field.getFieldByName(properties, ENTITY_DESCRIPTION_KEY).getValue());

            if (!Field.getFieldByName(properties, ENTITY_ADDITIONAL_PARAMS_KEY).getValue().isEmpty()) {
                List<String> additionalFields;
                additionalFields = Arrays.asList(Field.getFieldByName(properties, ENTITY_ADDITIONAL_PARAMS_KEY).getValue().split(","));
                for (String field : additionalFields) {
                    String[] propertySplit = field.split(":");
                    entity.setEntityProperty(propertySplit[0], propertySplit[1]);
                }
            }
            ArrayList<SmaxEntity> smaxEntityArrayList = new ArrayList<>();
            smaxEntityArrayList.add(entity);
            SmaxSession session = new SmaxSession(client.startSmaxSession(getServerUrl(), getTenantId(), getUserName(), getPassword()));
            logger.debug("In SmaxEntityProvider:createIntegrationEntity(), SMAX Session ID: " + session.getSessionId());
            SmaxBulkOperationResult results = client.createSmaxRecord(getServerUrl(), getTenantId(), session.getSessionId(), new SmaxEntityOperation(smaxEntityArrayList, "CREATE"));
            if (!results.getOperationResultStatus().equals("OK")) {
                throw new ProviderException(String.format("Create operation failed in SMAX, received BulkOperationResultStatus of %s from REST call", results.getOperationResultStatus()));
            }
            // Now we need to recreate the SMAX Entity object because SMAX doesn't return all the properties we need right off the bat.
            List<ProviderInfo> providerInfos = new ArrayList<>();
            ProviderInfo newEntityProviderInfo = getIntegrationEntity(new Field(ENTITY_ID_KEY, "ID", results.getSmaxEntityId()));
            if (newEntityProviderInfo == null) {
                throw new ProviderException("Error creating ProviderInfo object for new record");
            }
            providerInfos.add(newEntityProviderInfo);
            if (providerInfos.isEmpty()) {
                throw new ProviderException("In SmaxEntityProvider::createIntegrationEntity - No ProviderInfo object in providerInfos");
            } else {
                logger.debug("In SmaxEntityProvider::createIntegrationEntity - ProviderInfo object created and added to ArrayList successfully.");
            }
            return new ProviderInfoResult(0, providerInfos.size(), providerInfos.toArray(new ProviderInfo[providerInfos.size()]));
        } catch (Exception e) {
            throw new ProviderException("In SmaxEntityProvider::createIntegrationEntity - An error occurred: ", e.getCause());
        }
    }

    @Override
    public ProviderInfo getIntegrationEntity(Field requestId) throws ProviderException {
        List<String> propertyLayout = new ArrayList<>();
        propertyLayout.add(getEntityTitleParam());
        propertyLayout.add(getEntityDescParam());
        propertyLayout.add(getEntityCreatorParam());
        propertyLayout.add(getEntityCreationTimestampParam());
        SmaxSession session = new SmaxSession(client.startSmaxSession(getServerUrl(), getTenantId(), getUserName(), getPassword()));
        // logger.debug("SMAX Session ID: " + session.getSessionId());
        return Optional.of(client.getRecord(getServerUrl(), getTenantId(), session.getSessionId(), getEntityType(),
                propertyLayout, requestId.getValue())).map(this::createProviderInfo).orElse(null);
    }

    private ProviderInfo createProviderInfo(SmaxEntity entity) {
        logger.debug("In SmaxEntityProvider::createProviderInfo() for entity ID: " + entity.getEntityId());
        String itemUrl = getServerUrl() + "/saw/" + getEntityType() + "/" + entity.getEntityId()
                + "?TENANTID=" + getTenantId();
        ProviderInfo provInfo = new ProviderInfo(entity.getEntityId(),
                entity.getPropertyAsString(getEntityTitleParam()), entity.getEntityType(),
                entity.getPropertyAsString(getEntityTitleParam()), itemUrl);
        provInfo.setDescription(entity.getPropertyAsString(getEntityDescParam()));

        List<Field> fields = new ArrayList<>();
        addField(fields, SmaxEntity.ENTITY_CREATION_DATE_KEY, "Created On",
                entity.getPropertyAsLong(getEntityCreationTimestampParam()).toString(), DataType.DATETIME);
        addField(fields, SmaxEntity.ENTITY_CREATED_BY_KEY, "Created By",
                entity.getPropertyAsString(getEntityCreatorParam()), DataType.TEXT);

        provInfo.setProperties(fields);
        logger.debug("In SmaxEntityProvider::createProviderInfo() - Completed populating ProviderInfo object");
        return provInfo;
    }

    private void addField(List<Field> fieldCollection, String fieldName, String fieldDisplayName,
            String fieldValue, DataType dataType) {
        logger.debug("In SmaxEntityProvider::addField()");
        if (StringUtils.isNotEmpty(fieldValue)) {
            logger.debug("In SmaxEntityProvider::addField() - " + fieldName + " has value of " + fieldValue);
            Field fieldToAdd = new Field(fieldName, fieldDisplayName);
            fieldToAdd.setValue(fieldValue);
            fieldToAdd.setType(dataType);
            if (!fieldCollection.add(fieldToAdd)) {
                logger.debug("Could not add field to FieldCollection");
            } else {
                logger.debug("Field added to fieldCollection");
            }
        } else {
            logger.debug("In SmaxEntityProvider::addField() - " + fieldName + " has empty fieldValue.");
        }
    }
}