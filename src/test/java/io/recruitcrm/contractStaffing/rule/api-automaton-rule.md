# API Automation Rules and Best Practices

## Overview
This document outlines the comprehensive rules and best practices for creating API automation tests based on the DataEnrichmentTest class pattern. It covers POJO creation, test case coverage, data providers, schema validation, Base Test Classes, and Helper Method patterns.

## 1. POJO (Plain Old Java Object) Creation Rules

### 1.1 POJO Structure
- **Package Structure**: Place POJOs in `src/main/java/io/rcrm/api/pojo/albatross/{FeatureName}/`
- **Naming Convention**: Use descriptive names like `CreateDataEnrichmentHistory`, `DataEnrichmentFeedback`
- **Lombok Annotations**: Use Lombok annotations for clean, boilerplate-free code

### 1.2 Required Annotations
```java
@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class YourPOJO {
    // Fields
}
```

### 1.3 Field Naming and Types
- Use `snake_case` for field names to match API JSON structure
- Choose appropriate data types:
  - `String` for text fields
  - `int` for numeric IDs and counts
  - `boolean` for true/false values
  - `Boolean` (wrapper) for nullable boolean values

### 1.4 Example POJO Structure
```java
package io.rcrm.api.pojo.albatross.DataEnrichment;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CreateDataEnrichmentHistory {
    private String entity_id;
    private String record_type;
    private int enrich_field_type;
    private String enriched_data;
    private String enriched_by;
}
```

## 2. Test Class Structure and Setup

### 2.1 Class Declaration
```java
@TestBase.AccountType("Business|AlbatrossTkn")
public class YourFeatureTest extends TestBase {
    // Test methods
}
```

### 2.2 Required Fields
```java
String albatrossAuthToken;
String apiAuthToken;
int ownerAccountID;
commanFunction function;
```

### 2.3 Setup Method
```java
@BeforeClass
public void Setup() {
    albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    ownerAccountID = ThreadManager.getAccount().getAccountId();
    apiAuthToken = ThreadManager.getAccountApiKey();
    function = new commanFunction();
}
```

## 3. Test Case Coverage Strategy

### 3.1 Success Scenarios (200 Status)
- **Happy Path Tests**: Test with valid data
- **Response Validation**: Verify status code, message, and data fields
- **Schema Validation**: Validate response against JSON schema

```java
@Test(dataProvider = "testDataProvider")
public void createResourceTest(List<String> data) {
    // Create POJO with valid data
    YourPOJO pojo = YourPOJO.builder()
        .field1(data.get(0))
        .field2(data.get(1))
        .build();

    // Make API call
    Response response = RestClient.doPost("JSON", albatrossURL, "endpoint",
        albatrossAuthToken, null, true, pojo);

    // Assertions
    Assert.assertEquals(response.statusCode(), 200);
    JsonPath jsonPath = response.jsonPath();
    Assert.assertEquals(jsonPath.get("status"), "success");
    Assert.assertEquals(jsonPath.getString("message"), "Expected message");
    
    // Schema validation
    response.then().assertThat().body(
        matchesJsonSchemaInClasspath("privateApi/FeatureName/SchemaFile.json"));
}
```

### 3.2 Validation Error Scenarios (422 Status)
- **Empty/Null Fields**: Test with missing required fields
- **Invalid Data Types**: Test with wrong data formats
- **Business Rule Violations**: Test with invalid business logic

```java
@Test
public void createResourceWithEmptyRequestBodyTest() {
    YourPOJO pojo = YourPOJO.builder()
        .field1("")
        .field2("")
        .build();

    Response response = RestClient.doPost("JSON", albatrossURL, "endpoint",
        albatrossAuthToken, null, true, pojo);

    Assert.assertEquals(response.statusCode(), 422);
    JsonPath jsonPath = response.jsonPath();
    Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
    Assert.assertTrue(jsonPath.getString("message").contains("field is required"));
}
```

### 3.3 Authentication Error Scenarios (401 Status)
- **Invalid Token**: Test with malformed authentication tokens
- **Expired Token**: Test with expired tokens
- **Missing Token**: Test without authentication

```java
@Test
public void unauthorizedUserCannotCreateResourceTest() {
    YourPOJO pojo = YourPOJO.builder()
        .field1("valid_data")
        .build();

    Response response = RestClient.doPost("JSON", albatrossURL, "endpoint",
        albatrossAuthToken + "invalid", null, true, pojo);

    Assert.assertEquals(response.statusCode(), 401);
    JsonPath jsonPath = response.jsonPath();
    Assert.assertEquals(jsonPath.getString("error"), "Unauthorized");
}
```

### 3.4 GET Request Patterns
```java
@Test
public void getResourceTest() {
    Map<String, String> queryParameters = new HashMap<>();
    queryParameters.put("page", "1");
    queryParameters.put("per_page", "25");

    Response response = RestClient.doGet("JSON", albatrossURL, "endpoint",
        albatrossAuthToken, queryParameters, null, true);

    Assert.assertEquals(response.statusCode(), 200);
    JsonPath jsonPath = response.jsonPath();
    Assert.assertEquals(jsonPath.getString("message"), "Expected message");
    
    // Validate response data
    Assert.assertNotNull(jsonPath.get("data.records"));
}
```

## 4. Data Provider Implementation

### 4.1 Data Provider Structure
```java
@DataProvider
public Object[][] testDataProvider() {
    // Create test entities using common functions
    JsonPath jsonCandidate = function
        .createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
        .jsonPath();
    String candidateId = String.valueOf(jsonCandidate.getInt("id"));

    JsonPath jsonCompany = function
        .createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
        .jsonPath();
    String companySlug = jsonCompany.get("slug");

    JsonPath jsonContact = function
        .createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), companySlug)
        .jsonPath();
    String contactId = String.valueOf(jsonContact.getInt("id"));

    // Return test data arrays
    return new Object[][] {
        { Arrays.asList(candidateId, "Candidate", "data1", "user1") },
        { Arrays.asList(contactId, "Contact", "data2", "user2") }
    };
}
```

### 4.2 Common Function Usage
- **Entity Creation**: Use `function.createNewCandidateWithMandatoryFields()`
- **Company Creation**: Use `function.createNewCompanyWithMandatoryFields()`
- **Contact Creation**: Use `function.createNewContact_POST()`
- **Job Creation**: Use `function.createNewJob()`
- **User Retrieval**: Use `function.getUsers()`
- **Account Details**: Use `function.getAccountDetail()`

### 4.3 Data Provider Creation Rule
**Always use methods from `commanFunction` class to create dynamic test data in data providers:**

```java
@DataProvider
public Object[][] testDataProvider() {
    // Create candidate using commanFunction
    JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, 
        ThreadManager.getAccountApiKey()).jsonPath();
    int candidateId = jsonCandidate.getInt("id");

    // Create company using commanFunction
    JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, 
        ThreadManager.getAccountApiKey()).jsonPath();
    String companySlug = jsonCompany.getString("slug");

    // Create contact using commanFunction
    JsonPath jsonContact = function.createNewContact_POST(baseURL, 
        ThreadManager.getAccountApiKey(), companySlug).jsonPath();
    String contactSlug = jsonContact.getString("slug");

    // Create job using commanFunction
    JsonPath jsonJob = function.createNewJob(baseURL, 
        ThreadManager.getAccountApiKey(), companySlug, contactSlug).jsonPath();
    int jobId = jsonJob.getInt("id");

    // Get users using commanFunction
    Response usersResponse = function.getUsers(baseURL, 
        ThreadManager.getAccountApiKey());
    JsonPath usersJsonPath = usersResponse.jsonPath();
    int userId = usersJsonPath.getInt("data[0].id");

    return new Object[][] {
        { jobId, candidateId, userId },
        { jobId, candidateId, userId }
    };
}
```

**Key Points:**
- Use `baseURL` variable instead of hardcoded URLs
- Use `ThreadManager.getAccountApiKey()` for authentication
- Extract IDs and slugs from response using `jsonPath.getInt()` and `jsonPath.getString()`
- Create all required entities (candidate, company, contact, job) dynamically
- Return data as primitive types (int, String) rather than Lists
- Follow the pattern from `RemoveRecordsFromHotlist.java` for consistency

### 4.4 Data Provider Best Practices
- Create realistic test data using JavaFaker
- Include multiple scenarios (valid, edge cases)
- Clean up test data after tests if needed
- Use meaningful variable names

## 5. Schema Validation

### 5.1 JSON Schema Structure
Create schema files in `src/test/resources/privateApi/{FeatureName}/`

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "message": {
      "type": "string"
    },
    "message_type": {
      "type": "string"
    },
    "data": {
      "type": "object",
      "properties": {
        "id": {
          "type": "integer"
        },
        "field1": {
          "type": "string"
        }
      },
      "required": ["id", "field1"]
    },
    "status": {
      "type": "string"
    }
  },
  "required": ["message", "message_type", "data", "status"]
}
```

### 5.2 Schema Validation in Tests
```java
response.then().assertThat().body(
    matchesJsonSchemaInClasspath("privateApi/FeatureName/SchemaFile.json"));
```

## 6. Test Method Naming Conventions

### 6.1 Method Naming Patterns
- **Success Tests**: `createResourceTest()`, `getResourceTest()`
- **Validation Tests**: `createResourceWithEmptyRequestBodyTest()`
- **Authorization Tests**: `unauthorizedUserCannotCreateResourceTest()`
- **Business Logic Tests**: `createResourceWithInvalidBusinessRuleTest()`

### 6.2 Test Organization
1. **Setup and Configuration Tests**
2. **CRUD Operation Tests**
3. **Validation Tests**
4. **Authorization Tests**
5. **Business Logic Tests**
6. **Edge Case Tests**

## 7. Response Validation Patterns

### 7.1 Status Code Validation
```java
Assert.assertEquals(response.statusCode(), 200);
```

### 7.2 Message Validation
```java
JsonPath jsonPath = response.jsonPath();
Assert.assertEquals(jsonPath.getString("message"), "Expected success message");
Assert.assertEquals(jsonPath.get("status"), "success");
```

### 7.3 Data Field Validation
```java
Assert.assertEquals(jsonPath.getString("data.field1"), expectedValue);
Assert.assertEquals(jsonPath.getInt("data.id"), expectedId);
Assert.assertTrue(jsonPath.getString("data.field2").contains("expected"));
```

### 7.4 Array/List Validation
```java
List<Map<String, Object>> records = jsonPath.getList("data.records");
Assert.assertEquals(records.size(), expectedSize);
Assert.assertEquals(records.get(0).get("field"), expectedValue);
```

## 8. Error Handling and Logging

### 8.1 Response Logging
```java
System.out.println(response.prettyPrint() + "Test response");
```

### 8.2 Error Response Validation
```java
Assert.assertEquals(jsonPath.getString("error"), "Unauthorized");
Assert.assertEquals(jsonPath.get("message_type"), "is-danger");
```

## 9. Test Execution and Configuration

### 9.1 TestNG Configuration
- Use `@Test` annotations with data providers
- Use `@BeforeClass` for setup
- Use `@TestBase.AccountType` for account configuration

### 9.2 Test Data Management
- Create test data in data providers
- Use common functions for entity creation
- Clean up test data when necessary

## 11. Base Test Class Pattern and Helper Methods

### 11.1 Base Test Class Structure
Create a base test class for each feature module that extends `TestBase` and contains common helper methods for API calls.

```java
public class FeatureNameBaseTest extends TestBase {

    protected String serviceBaseURL = "https://" + System.getProperty("envname") 
            + "feature-service.recruitcrm.net/v1";

    /**
     * Helper method for main API call
     */
    public Response apiMethodName(int param1, int param2, String authToken, String startDate, String endDate) {
        // Create request POJO
        RequestPOJO requestPojo = new RequestPOJO();
        requestPojo.setField1(param1);
        requestPojo.setField2(param2);
        requestPojo.setStartDate(Long.parseLong(startDate));
        requestPojo.setEndDate(Long.parseLong(endDate));

        // Make API call
        return RestClient.doPost("JSON", serviceBaseURL, "endpoint",
                authToken, null, true, requestPojo);
    }

    /**
     * Helper method for setup operations (if needed)
     */
    public Response setupMethod(int param1, int param2, String authToken) {
        // Setup logic here
        return RestClient.doPost("JSON", serviceBaseURL, "setup-endpoint",
                authToken, null, true, setupObject);
    }
}
```

### 11.2 Test Class Implementation Pattern
The actual test class should extend the base class and reuse helper methods for all test scenarios.

```java
@TestBase.AccountType("Business|AlbatrossTkn")
public class FeatureNameTest extends FeatureNameBaseTest {

    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    commanFunction function;

    @BeforeClass
    public void Setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        apiAuthToken = ThreadManager.getAccountApiKey();
        function = new commanFunction();
        // Additional setup if needed
    }

    @Test(dataProvider = "testDataProvider")
    public void mainApiTest(int param1, int param2, int param3) {
        // Setup (if needed)
        setupMethod(param1, param2, albatrossAuthToken);

        // Main API call using helper method
        Response response = apiMethodName(param1, param2, albatrossAuthToken, "1751328000", "1759017600");
        
        // Assertions
        Assert.assertEquals(response.statusCode(), 200);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getInt("meta.status"), 200);
        Assert.assertEquals(jsonPath.getString("meta.message"), "Expected success message");
        
        // Schema validation
        response.then().assertThat().body(
                matchesJsonSchemaInClasspath("privateApi/FeatureName/Schema.json"));
    }

    @Test
    public void apiTestWithEmptyRequest() {
        // Use same helper method with minimal/empty parameters
        Response response = apiMethodName(1, 2, albatrossAuthToken, "1751328000", "1759017600");

        Assert.assertEquals(response.statusCode(), 404);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertTrue(jsonPath.getString("error").contains("Bad Request"));
    }

    @Test
    public void unauthorizedApiTest() {
        // Use same helper method with invalid auth token
        Response response = apiMethodName(1, 2, albatrossAuthToken + "invalid", "1751328000", "1759017600");

        Assert.assertEquals(response.statusCode(), 401);
        JsonPath jsonPath = response.jsonPath();
        Assert.assertEquals(jsonPath.getString("meta.message"), "Unauthorised access");
    }
}
```

### 11.3 Helper Method Reusability Rules

#### 11.3.1 Single Responsibility Principle
- **One Method per API Endpoint**: Create one helper method for each API endpoint
- **Parameter Flexibility**: Design methods to accept different parameters for various test scenarios
- **Return Raw Response**: Always return `Response` object for maximum flexibility in assertions

#### 11.3.2 Method Naming Convention
- **Descriptive Names**: Use clear, descriptive method names like `getTimeSheetFreeSlots()`, `enableTimesheet()`
- **Action-Based**: Start with action verbs (get, create, update, delete)
- **Feature Context**: Include feature context in method names

#### 11.3.3 Parameter Design Pattern
```java
public Response apiMethodName(
    int primaryEntityId,           // Main entity identifier
    int secondaryEntityId,         // Related entity identifier  
    int configurationParam,        // Configuration parameter
    String authToken,              // Authentication
    String startDate,              // Date range start
    String endDate                 // Date range end
) {
    // Implementation
}
```

### 11.4 Code Reusability Best Practices

#### 11.4.1 All Tests Use Same Helper Method
```java
// ✅ CORRECT: All tests reuse the same helper method
@Test
public void validScenarioTest() {
    Response response = apiMethodName(validParam1, validParam2, authToken, "start", "end");
    // Assertions for valid scenario
}

@Test  
public void invalidDataTest() {
    Response response = apiMethodName(invalidParam1, invalidParam2, authToken, "start", "end");
    // Assertions for invalid data scenario
}

@Test
public void unauthorizedTest() {
    Response response = apiMethodName(param1, param2, invalidAuthToken, "start", "end");
    // Assertions for unauthorized scenario
}
```

#### 11.4.2 Avoid Duplication
```java
// ❌ WRONG: Duplicating API call logic in each test
@Test
public void test1() {
    RequestPOJO pojo = new RequestPOJO();
    pojo.setField1(value);
    Response response = RestClient.doPost(url, endpoint, auth, null, true, pojo);
}

// ✅ CORRECT: Reuse helper method
@Test  
public void test1() {
    Response response = apiMethodName(param1, param2, authToken, start, end);
}
```

### 11.5 Setup Method Integration

#### 11.5.1 Separate Setup from Main API Call
```java
@Test(dataProvider = "testDataProvider")
public void mainApiTest(int jobId, int candidateId, int userId, int frequency) {
    // Setup step - enable required services/features
    enableTimesheet(candidateId, jobId, userId, albatrossAuthToken, frequency);

    // Main API call - the actual functionality being tested
    Response response = getTimeSheetFreeSlots(candidateId, jobId, frequency, 
                        albatrossAuthToken, "1751328000", "1759017600");
    
    // Assertions
    Assert.assertEquals(response.statusCode(), 200);
}
```

#### 11.5.2 Setup Method Pattern
```java
/**
 * Setup method for enabling prerequisites
 */
public Response enableFeature(int entityId1, int entityId2, int userId, String authToken, int configParam) {
    // Create configuration object
    ConfigurationPOJO config = createDefaultConfiguration(entityId1, entityId2, userId, configParam);
    
    // Enable the feature
    return RestClient.doPost("JSON", serviceBaseURL, "enable-endpoint",
                    authToken, null, true, config);
}
```

### 11.6 Base Class Service URL Pattern
```java
public class FeatureNameBaseTest extends TestBase {
    
    // ✅ CORRECT: Dynamic service URL based on environment
    protected String serviceBaseURL = "https://" + System.getProperty("envname") 
            + "feature-service.recruitcrm.net/v1";
    
    // ✅ CORRECT: For different service types
    protected String timesheetBaseURL = "https://" + System.getProperty("envname")
            + "contract-staffing-timesheet.recruitcrm.net/v1";
}
```

### 11.7 API Automation Architecture Summary

```
BaseTest Class (FeatureNameBaseTest)
├── Service URL Configuration
├── Helper Methods for API Calls
├── Setup/Configuration Methods
└── Common Utilities

Test Implementation Class (FeatureNameTest)  
├── Extends BaseTest Class
├── Test Data Setup (@BeforeClass)
├── Data Providers for Dynamic Data
├── Test Methods (All reuse Base Class helpers)
└── Assertions and Validations
```

**Key Benefits:**
- ✅ **Code Reusability**: One helper method serves all test scenarios
- ✅ **Maintainability**: Changes in API structure require updates in one place only  
- ✅ **Consistency**: All tests follow the same pattern
- ✅ **Scalability**: Easy to add new test scenarios
- ✅ **Clean Architecture**: Clear separation between API logic and test logic

## 10. Best Practices Summary

### 10.1 Code Organization
- Keep POJOs simple and focused
- Use meaningful variable names
- Follow consistent naming conventions
- Group related tests together

### 10.2 Test Coverage
- Cover all HTTP status codes (200, 401, 422, etc.)
- Test both positive and negative scenarios
- Validate response schemas
- Test business logic and edge cases

### 10.3 Maintainability
- Use data providers for test data
- Create reusable common functions
- Keep tests independent and isolated
- Use descriptive test method names

### 10.4 Performance
- Minimize API calls in data providers
- Use appropriate timeouts
- Clean up test data efficiently
- Avoid unnecessary assertions

This comprehensive approach ensures robust, maintainable, and well-structured API automation tests that follow industry best practices and provide thorough coverage of API functionality. 