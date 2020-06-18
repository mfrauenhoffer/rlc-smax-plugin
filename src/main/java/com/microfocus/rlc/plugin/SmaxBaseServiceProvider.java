package com.microfocus.rlc.plugin;

import com.microfocus.rlc.plugin.client.SmaxClient;
import com.microfocus.rlc.plugin.domain.SmaxEntity;
import com.microfocus.rlc.plugin.domain.enums.SmaxEntityTypes;
import com.serena.rlc.provider.BaseServiceProvider;
import com.serena.rlc.provider.annotations.ConfigProperty;
import com.serena.rlc.provider.annotations.Getter;
import com.serena.rlc.provider.annotations.Overridable;
import com.serena.rlc.provider.domain.DataType;
import com.serena.rlc.provider.domain.Field;
import com.serena.rlc.provider.domain.FieldInfo;
import com.serena.rlc.provider.domain.FieldValueInfo;
import com.serena.rlc.provider.exceptions.ProviderException;
import com.serena.rlc.provider.exceptions.ProviderValidationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.microfocus.rlc.plugin.domain.SmaxEntity.ENTITY_TYPE_KEY;

public class SmaxBaseServiceProvider extends BaseServiceProvider {

    protected static final String INTEGRATION_ENTITY_ID = "integrationEntityId";

    @Autowired
    protected SmaxClient client;

    @ConfigProperty(name = "server_url", displayName = "Server URL", description = "URL to SMAX Environment", defaultValue = "https://<SERVERNAME>/", 
            dataType = DataType.TEXT, overridable = Overridable.NONE)
    private String serverUrl;

    @ConfigProperty(name = "integration_username", displayName = "Username", description = "User ID to use for authentication to SMAX", defaultValue = "release_control_integration", dataType = DataType.TEXT)
    private String userName;

    @ConfigProperty(name = "integration_password", displayName = "Password", description = "Password for integration user account", defaultValue = "", dataType = DataType.PASSWORD)
    private String password;

    @ConfigProperty(name = "tenant_id", displayName = "Tenant ID", description = "Tenant ID for SMAX Instance", defaultValue = "123456789", dataType = DataType.NUMERIC)
    private String tenantId;

    @ConfigProperty(name = "entityType", displayName = "Entity Type (Request, Incident, etc)",
                    description = "The entity you wish to search for.", defaultValue = "", dataType = DataType.TEXT)
    private String entityType;

    @ConfigProperty(name = SmaxEntity.ENTITY_TITLE_KEY, displayName = "Entity Title Field",
    description = "Name of the field in SMAX that represents an item's title", defaultValue="DisplayLabel", dataType=DataType.TEXT)
    private String entityTitleParam;

    @ConfigProperty(name = SmaxEntity.ENTITY_DESCRIPTION_KEY, displayName = "Entity Description Field",
    description = "Name of the field in SMAX that represents an item's description", defaultValue="Description", dataType=DataType.TEXT)
    private String entityDescParam;

    @ConfigProperty(name = SmaxEntity.ENTITY_CREATION_DATE_KEY, displayName = "Entity Creation Date Field",
    description = "Name of the field in SMAX that represents an item's creation timestamp", defaultValue="EmsCreationTime", dataType=DataType.TEXT)
    private String entityCreationTimestampParam;

    @ConfigProperty(name = SmaxEntity.ENTITY_CREATED_BY_KEY, displayName = "Entity Created By Field",
    description = "Name of the field in SMAX that represents the user who created it", defaultValue = "RequestedByPerson.Name", dataType=DataType.TEXT)
    private String entityCreatorParam;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String url) {
        this.serverUrl = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String id) {
        this.tenantId = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String val) {
        entityType = val;
    }

    public String getEntityTitleParam() {
        return entityTitleParam;
    }

    public String getEntityDescParam() {
        return entityDescParam;
    }

    public void setEntityTitleParam(String val) {
        this.entityTitleParam = val;
    }

    public void setEntityDescParam(String val) {
        this.entityDescParam = val;
    }

    public void setEntityCreationTimestampParam(String val) {
        this.entityCreationTimestampParam = val;
    }

    public void setEntityCreatorParam(String val) {
        this.entityCreatorParam = val;
    }

    public String getEntityCreationTimestampParam() {
        return entityCreationTimestampParam;
    }

    public String getEntityCreatorParam() {
        return entityCreatorParam;
    }
    
    @Override
    public FieldInfo getFieldValues(String fieldName, List<Field> properties) throws ProviderException {
        if (fieldName.equalsIgnoreCase(ENTITY_TYPE_KEY)) {
            return getEntityTypes();
        }
        return null;
    }

    @Override
    public void validateConfiguration() throws ProviderValidationException {
        // TO-DO: Handle invalid certificate response codes
        if (!(client.startSmaxSession(serverUrl, tenantId, userName, password).length() > 0)) {
            throw new ProviderValidationException("Can't connect to server. Response length not greater than 0.");
        }
    }
    
    @Getter(name = ENTITY_TYPE_KEY, displayName = "Select Entity Type")
    public FieldInfo getEntityTypes() {
        FieldInfo fieldInfo = new FieldInfo(ENTITY_TYPE_KEY);
        fieldInfo.setValues(Arrays.stream(SmaxEntityTypes.values()).
                map(type -> new FieldValueInfo(type.name(), type.name())).collect(Collectors.toList()));
        return fieldInfo;
    }

}