# TODO: Remove Primary and Secondary Role Functionality

## Files to Edit

### Java Files
- [ ] ReliefLinkApplication/src/main/java/com/relieflink/model/User.java
  - Remove `secondaryRole` field
  - Remove `secondaryRoleActive` field
  - Remove `getSecondaryRole()`, `setSecondaryRole()`, `isSecondaryRoleActive()`, `setSecondaryRoleActive()` methods
  - Update `getActiveRole()` to always return `primaryRole`
  - Update constructor to remove secondary role initialization

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/service/RoleManagementService.java
  - Remove entire service class (since it's only for secondary roles)

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/controller/RoleManagementController.java
  - Remove entire controller class

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/controller/AuthController.java
  - Remove `hasSecondaryRole` from session
  - Update login response to remove secondaryRole from JSON

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/service/AuthService.java
  - Remove secondary role initialization in register method

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/repository/DataStore.java
  - Remove `roleRequests` map and related methods
  - Remove secondary role initialization in admin user setup
  - Remove role request related methods

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/model/RoleRequest.java
  - Remove entire model class

- [ ] ReliefLinkApplication/src/main/java/com/relieflink/model/RoleRequestStatus.java
  - Remove entire enum class

### Template Files
- [ ] ReliefLinkApplication/src/main/resources/templates/register.html
  - Update label from "Primary Role" to "Role"
  - Update select name from "primaryRole" to "role"

- [ ] ReliefLinkApplication/src/main/resources/templates/request.html
  - No changes needed (already uses "Primary Role" but it's just a label)

### JavaScript Files
- [ ] ReliefLinkApplication/src/main/resources/static/js/register.js
  - Change `primaryRole` to `role` in data object
  - Remove `secondaryRole` and `secondaryRoleActive` from data object

- [ ] ReliefLinkApplication/src/main/resources/static/js/login.js
  - Remove secondary role display logic
  - Simplify role message to only show primary role

### Database Migration
- [ ] ReliefLinkApplication/src/main/resources/db/migration/V1__Initial_Schema.sql
  - Remove secondary role columns from users table
  - Remove role_requests table

## Followup Steps
- [ ] Test registration and login functionality
- [ ] Verify that users only have one role
- [ ] Ensure no references to secondary roles remain
- [ ] Run the application to confirm everything works
