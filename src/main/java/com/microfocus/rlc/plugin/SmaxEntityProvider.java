package com.microfocus.rlc.plugin;

import com.microfocus.rlc.plugin.domain.SmaxEntity;
import com.microfocus.rlc.plugin.domain.SmaxEntityProperty;
import com.microfocus.rlc.plugin.domain.SmaxSession;
import com.serena.rlc.provider.annotations.Action;
import com.serena.rlc.provider.annotations.ConfigProperty;
import com.serena.rlc.provider.annotations.Params;
import com.serena.rlc.provider.annotations.Service;
import com.serena.rlc.provider.domain.DataType;
import com.serena.rlc.provider.domain.Field;
import com.serena.rlc.provider.domain.ProviderInfo;
import com.serena.rlc.provider.domain.ProviderInfoResult;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.spi.IIntegrationEntityProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SmaxEntityProvider extends SmaxBaseServiceProvider implements IIntegrationEntityProvider {

    private static final Logger logger = LoggerFactory.getLogger(SmaxEntityProvider.class);
    
    private static final String CREATE_INTEGRATION_ENTITY = "createIntegrationEntity";

    @ConfigProperty(name = SmaxEntity.ENTITY_ADDITIONAL_PARAMS_KEY, displayName = "Additional Entity Fields to Return",
                    description = "Insert field names defined in SMAX that you wish to retrieve. These must be in the following format: fieldName:displayName:dataTypeInSmax", defaultValue = "", dataType = DataType.TEXTAREA)
    private String entityAddtlParams;

    private Map<String, SmaxEntityProperty> entityParamsMap;

    public String getEntityAddtlParams() {
        return entityAddtlParams;
    }

    public void setEntityAddtlParams(String entityAddtlParams) {
        this.entityAddtlParams = entityAddtlParams;
        // Parse parameters in to easily accessible map
        parseAddtlSmaxProperties();
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
        propertyLayout.add(getEntityTitl0eParam());
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
    public ProviderInfoResult createIntegrationEntity(String action, List<Field> properties) {
        return null;
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
        // logger.debug("In SmaxEntityProvider::createProviderInfo() for entity ID: " +
        // entity.getEntityId());
        final String itemUrl = getServerUrl() + "/saw/" + getEntityType() + "/" + entity.getEntityId()
                + "?TENANTID=" + getTenantId();
        final ProviderInfo provInfo = new ProviderInfo(entity.getEntityId(),
                entity.getPropertyAsString(getEntityTitleParam()), entity.getEntityType(),
                entity.getPropertyAsString(getEntityTitleParam()), itemUrl);
        provInfo.setDescription(entity.getPropertyAsString(getEntityDescParam()));

        List<Field> fields = new ArrayList<>();
        addField(fields, SmaxEntity.ENTITY_CREATION_DATE_KEY, "Created On",
                entity.getPropertyAsLong(getEntityCreationTimestampParam()).toString(), DataType.DATETIME);
        addField(fields, SmaxEntity.ENTITY_CREATED_BY_KEY, "Created By", entity.getPropertyAsString(getEntityCreatorParam()),
                DataType.TEXT);

        provInfo.setProperties(fields);
        return provInfo;
    }

    private void addField(List<Field> fieldCollection, String fieldName, String fieldDisplayName,
            String fieldValue, DataType dataType) {
        if (StringUtils.isNotEmpty(fieldValue)) {
            Field fieldToAdd = new Field(fieldName, fieldDisplayName);
            fieldToAdd.setValue(fieldValue);
            fieldToAdd.setType(dataType);
            fieldCollection.add(fieldToAdd);
        }
    }

    private void parseAddtlSmaxProperties() {
        List<String> values = new ArrayList<String>();
        // Handle the

        values = Arrays.asList(getEntityAddtlParams().split(","));

        for (String property : values) {
            String[] propertySplit = property.split(":");
            if (propertySplit.length > 0) {
                entityParamsMap.put(propertySplit[0], new SmaxEntityProperty(propertySplit[0], propertySplit[1], propertySplit[2]));
            }
        }
    }
}