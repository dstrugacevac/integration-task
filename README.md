# Salesforce Integration Application

This application integrates a local database with Salesforce, enabling seamless synchronization of account data. It
supports creating and updating accounts in Salesforce and mapping Salesforce IDs back to the local database for
consistent data management.

---

## Features

- **Authenticate with Salesforce**: Securely authenticates using OAuth 2.0 and retrieves access tokens.
- **Batch Update Accounts**: Efficiently uploads updates for multiple accounts in bulk using the Salesforce Bulk API.
- **Create New Accounts**: Adds individual accounts directly to Salesforce when required.
- **Sync Salesforce IDs**: Updates local database records with Salesforce IDs for reliable mapping.
- **Access Token Validation**: Ensures that the Salesforce access token is valid before making API calls.

---

## Configuration

### Salesforce Setup

1. Log in to your Salesforce instance.
2. Generate the following credentials:
    - `client_id` (Consumer Key)
    - `client_secret`
    - `username`
    - `password`
    - `instance_url`

### Application Properties

Add the following properties to your `application.properties` file:

```properties
# Salesforce configuration
salesforce.loginUrl=https://login.salesforce.com/services/oauth2/token
salesforce.instanceUrl=https://yourInstance.salesforce.com
salesforce.username=your_salesforce_username
salesforce.password=your_salesforce_password
salesforce.customerKey=your_consumer_key
salesforce.customerSecret=your_consumer_secret

# Local database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
