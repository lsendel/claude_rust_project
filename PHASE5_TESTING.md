# Phase 5 Testing Guide
## User Invitations & Role-Based Access Control (RBAC)

**Phase 5 Features:**
- User invitation system with email notifications
- Role-based access control (ADMINISTRATOR, EDITOR, VIEWER)
- Team member management UI
- Permission-based authorization on API endpoints

---

## Prerequisites

### Backend Setup

1. **Database Configuration**
   - Ensure PostgreSQL is running
   - Database should have the following tables:
     - `users`
     - `tenants`
     - `user_tenants` (with `role` and `invited_by` columns)
     - `projects`
     - `tasks`

2. **AWS SES Configuration (Optional)**
   - If you want to test actual email sending, configure in `application.properties`:
   ```properties
   aws.ses.enabled=true
   aws.ses.from-email=noreply@yourdomain.com
   aws.ses.region=us-east-1
   ```
   - If not configured, emails will be logged but not sent (still functional for testing)

3. **Start Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   # or
   ./mvnw spring-boot:run
   ```
   - Backend should start on `http://localhost:8080`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Frontend**
   ```bash
   npm run dev
   ```
   - Frontend should start on `http://localhost:5173`

---

## Test Scenarios

### Test 1: User Invitation Flow (Admin)

**Objective:** Verify administrators can invite users and assign roles

**Steps:**
1. Log in as an administrator user
2. Navigate to `/settings` page
3. Fill out invitation form:
   - Email: `testuser@example.com`
   - Role: Select "Editor"
   - Message: "Welcome to the team!"
4. Click "Send Invitation"

**Expected Results:**
- ✅ Success message appears: "Invitation sent to testuser@example.com"
- ✅ User appears in team members table with "EDITOR" badge (blue)
- ✅ If AWS SES enabled: Email sent to testuser@example.com
- ✅ If AWS SES disabled: Log message in backend console
- ✅ Backend logs show: "Invitation email sent to testuser@example.com"

**API Request:**
```http
POST /api/tenants/{tenantId}/users/invite
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "email": "testuser@example.com",
  "role": "EDITOR",
  "message": "Welcome to the team!"
}
```

**Expected Response:**
```json
{
  "userId": "uuid",
  "tenantId": "uuid",
  "email": "testuser@example.com",
  "role": "EDITOR",
  "invitedBy": "uuid",
  "invitedAt": "2025-10-27T...",
  "existingUser": false,
  "emailSent": true
}
```

---

### Test 2: Prevent Duplicate Invitations

**Objective:** Verify users cannot be invited twice to the same tenant

**Steps:**
1. Stay on `/settings` page
2. Try to invite the same email again: `testuser@example.com`
3. Click "Send Invitation"

**Expected Results:**
- ✅ Error message appears: "User testuser@example.com is already a member of tenant {name}"
- ✅ HTTP 400 Bad Request returned from API

---

### Test 3: List Team Members

**Objective:** Verify all users can view team members

**Steps:**
1. View the "Team Members" section on `/settings` page

**Expected Results:**
- ✅ Table displays all users in the tenant
- ✅ Columns show: Name, Email, Role, Joined Date, Actions
- ✅ Role badges are color-coded:
  - **ADMINISTRATOR** - Red background (#fee2e2), red text (#991b1b)
  - **EDITOR** - Blue background (#dbeafe), blue text (#1e40af)
  - **VIEWER** - Gray background (#f3f4f6), gray text (#4b5563)
- ✅ "Remove" button visible for other users (not yourself)

**API Request:**
```http
GET /api/tenants/{tenantId}/users
Authorization: Bearer <jwt-token>
```

**Expected Response:**
```json
[
  {
    "userId": "uuid",
    "email": "admin@example.com",
    "name": "Admin User",
    "role": "ADMINISTRATOR",
    "invitedBy": null,
    "joinedAt": "2025-10-01T...",
    "lastLoginAt": "2025-10-27T..."
  },
  {
    "userId": "uuid",
    "email": "testuser@example.com",
    "name": "Test User",
    "role": "EDITOR",
    "invitedBy": "admin-uuid",
    "joinedAt": "2025-10-27T...",
    "lastLoginAt": null
  }
]
```

---

### Test 4: Remove User (Admin Only)

**Objective:** Verify administrators can remove users

**Steps:**
1. As administrator, on `/settings` page
2. Find `testuser@example.com` in the team members table
3. Click "Remove" button
4. Confirm the removal in the dialog

**Expected Results:**
- ✅ Confirmation dialog appears
- ✅ Success message: "testuser@example.com has been removed from the team"
- ✅ User disappears from the table
- ✅ HTTP 204 No Content returned

**API Request:**
```http
DELETE /api/tenants/{tenantId}/users/{userId}
Authorization: Bearer <jwt-token>
```

---

### Test 5: Prevent Removing Last Admin

**Objective:** Verify last administrator cannot be removed

**Steps:**
1. If you have multiple admins, remove all but one
2. Try to remove the last administrator

**Expected Results:**
- ✅ Error message: "Cannot remove the last administrator from the tenant"
- ✅ HTTP 400 Bad Request returned

---

### Test 6: RBAC - Editor Can Create Project

**Objective:** Verify EDITOR role can create projects

**Steps:**
1. Log in as a user with EDITOR role
2. Navigate to `/projects` page
3. Click "Create Project"
4. Fill in project details and submit

**Expected Results:**
- ✅ Project created successfully
- ✅ HTTP 201 Created returned
- ✅ Project appears in the list

**API Request:**
```http
POST /api/projects
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "Test Project",
  "description": "Editor-created project",
  "status": "PLANNING",
  "ownerId": "editor-user-id",
  "priority": "MEDIUM"
}
```

---

### Test 7: RBAC - Viewer Cannot Create Project

**Objective:** Verify VIEWER role cannot create projects

**Steps:**
1. Log in as a user with VIEWER role
2. Navigate to `/projects` page
3. Attempt to create a project (if button is visible)

**Expected Results:**
- ✅ HTTP 403 Forbidden returned
- ✅ Error message: "You do not have permission to perform this action. Only users with ADMINISTRATOR or EDITOR roles can modify resources."
- ✅ Project not created

**API Request:**
```http
POST /api/projects
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "Test Project",
  ...
}
```

**Expected Response:**
```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "You do not have permission to perform this action. Only users with ADMINISTRATOR or EDITOR roles can modify resources.",
  "path": "/api/projects",
  "timestamp": "2025-10-27T..."
}
```

---

### Test 8: RBAC - Viewer Can Read Projects

**Objective:** Verify VIEWER role can view projects

**Steps:**
1. Log in as a user with VIEWER role
2. Navigate to `/projects` page

**Expected Results:**
- ✅ Projects list displayed successfully
- ✅ HTTP 200 OK returned
- ✅ All projects visible

**API Request:**
```http
GET /api/projects
Authorization: Bearer <jwt-token>
```

---

### Test 9: RBAC - Viewer Cannot Update Project

**Objective:** Verify VIEWER role cannot modify projects

**Steps:**
1. As VIEWER, try to update an existing project via API

**Expected Results:**
- ✅ HTTP 403 Forbidden returned
- ✅ Error message about insufficient permissions

**API Request:**
```http
PUT /api/projects/{projectId}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "Updated Project Name",
  ...
}
```

---

### Test 10: RBAC - Viewer Cannot Delete Project

**Objective:** Verify VIEWER role cannot delete projects

**Steps:**
1. As VIEWER, try to delete a project via API

**Expected Results:**
- ✅ HTTP 403 Forbidden returned
- ✅ Error message about insufficient permissions

**API Request:**
```http
DELETE /api/projects/{projectId}
Authorization: Bearer <jwt-token>
```

---

### Test 11: RBAC - Editor Can Update Task

**Objective:** Verify EDITOR role can modify tasks

**Steps:**
1. Log in as EDITOR
2. Navigate to `/tasks` page
3. Click "Edit" on an existing task
4. Modify task details and save

**Expected Results:**
- ✅ Task updated successfully
- ✅ HTTP 200 OK returned
- ✅ Changes reflected in the UI

---

### Test 12: Non-Admin Cannot Invite Users

**Objective:** Verify only administrators can invite users

**Steps:**
1. Log in as EDITOR or VIEWER
2. Navigate to `/settings` page
3. Try to send an invitation

**Expected Results:**
- ✅ HTTP 403 Forbidden returned
- ✅ Error message: "You do not have permission to invite users. Only administrators can invite users."

**API Request:**
```http
POST /api/tenants/{tenantId}/users/invite
Authorization: Bearer <editor-or-viewer-token>
Content-Type: application/json

{
  "email": "newuser@example.com",
  "role": "EDITOR"
}
```

---

### Test 13: Non-Admin Cannot Remove Users

**Objective:** Verify only administrators can remove users

**Steps:**
1. Log in as EDITOR or VIEWER
2. Navigate to `/settings` page
3. Try to remove a user

**Expected Results:**
- ✅ HTTP 403 Forbidden returned
- ✅ Error message: "You do not have permission to remove users. Only administrators can remove users."

---

## Database Verification

After testing, verify database state:

```sql
-- Check user-tenant associations
SELECT ut.user_id, u.email, ut.role, ut.invited_by, ut.joined_at
FROM user_tenants ut
JOIN users u ON ut.user_id = u.id
WHERE ut.tenant_id = '<your-tenant-id>';

-- Check invitation history
SELECT u.email, ut.role, ut.invited_by, ut.joined_at
FROM user_tenants ut
JOIN users u ON ut.user_id = u.id
WHERE ut.tenant_id = '<your-tenant-id>'
ORDER BY ut.joined_at DESC;
```

---

## Role Permission Matrix

| Action | ADMINISTRATOR | EDITOR | VIEWER |
|--------|--------------|--------|--------|
| View Projects | ✅ | ✅ | ✅ |
| Create Project | ✅ | ✅ | ❌ |
| Update Project | ✅ | ✅ | ❌ |
| Delete Project | ✅ | ✅ | ❌ |
| View Tasks | ✅ | ✅ | ✅ |
| Create Task | ✅ | ✅ | ❌ |
| Update Task | ✅ | ✅ | ❌ |
| Delete Task | ✅ | ✅ | ❌ |
| Invite Users | ✅ | ❌ | ❌ |
| Remove Users | ✅ | ❌ | ❌ |
| View Team Members | ✅ | ✅ | ✅ |

---

## Known Limitations

1. **Email Sending**: Requires AWS SES configuration. If not configured, emails are logged only.
2. **User Signup**: Invited users need to complete signup via OAuth2 (AWS Cognito).
3. **Current User ID**: The backend currently uses a placeholder for getting the current user ID from the security context. This should be replaced with actual JWT token parsing in production.

---

## Troubleshooting

### Issue: 403 Forbidden on all requests
**Solution:** Ensure JWT token includes user's role as authority

### Issue: Invitation email not sent
**Solution:** Check `aws.ses.enabled` property and AWS credentials

### Issue: Cannot remove user
**Solution:** Verify you're not trying to remove the last administrator

### Issue: User already exists error
**Solution:** Check if user is already in the `user_tenants` table

---

## Next Steps

After successful testing:
- ✅ Phase 5 RBAC implementation validated
- ✅ User invitation system working
- ✅ Settings UI functional
- Ready to proceed to **Phase 6: Automation Rules**

---

## Files Modified/Created in Phase 5

### Backend
- `backend/src/main/java/com/platform/saas/dto/InviteUserRequest.java` ✨ NEW
- `backend/src/main/java/com/platform/saas/dto/InviteUserResponse.java` ✨ NEW
- `backend/src/main/java/com/platform/saas/dto/UserResponse.java` ✨ NEW
- `backend/src/main/java/com/platform/saas/service/InvitationService.java` ✨ NEW
- `backend/src/main/java/com/platform/saas/service/EmailService.java` ✨ NEW
- `backend/src/main/java/com/platform/saas/controller/UserController.java` ✨ NEW
- `backend/src/main/java/com/platform/saas/controller/ProjectController.java` ✏️ MODIFIED
- `backend/src/main/java/com/platform/saas/controller/TaskController.java` ✏️ MODIFIED
- `backend/src/main/java/com/platform/saas/controller/GlobalExceptionHandler.java` ✏️ MODIFIED

### Frontend
- `frontend/src/services/userService.ts` ✨ NEW
- `frontend/src/pages/SettingsPage.tsx` ✨ NEW
- `frontend/src/App.tsx` ✏️ MODIFIED

**Total:** 6 new files, 4 modified files
