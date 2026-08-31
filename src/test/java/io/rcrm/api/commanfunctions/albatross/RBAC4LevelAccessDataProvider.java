package io.rcrm.api.commanfunctions.albatross;

import org.testng.ITestContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Common Data Provider for 4-Level Access Permissions
 * Supports: can view, can create, can edit, can delete
 * 
 * Can be used for:
 * - Email Templates
 * - Placements
 */
public class RBAC4LevelAccessDataProvider {

    /**
     * Data Provider for 4-Level Access Testing
     * 
     * @param context TestNG context for parameter filtering
     * @param entityType Type of entity being tested (e.g., "emailTemplate", "placement")
     * @return Object array with test data
     */
    public static Object[][] get4LevelAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");

        // Define comprehensive test cases for 4-level access permissions
        Object[][] allTestCases = {
                    // Format: {creator, executor, shareType, expectedStatusCode, expectedMessage, testDescription}
        
        // Account Owner created entities - testing different executors
        {"AccountOwner", "AccountOwner", "Yes", 200, "Success", "Account Owner can access " + entityType + " created by Account Owner - TC001"},
        {"AccountOwner", "Admin", "Yes", 200, "Success", "Admin can access " + entityType + " created by Account Owner - TC002"},
        {"AccountOwner", "TeamMember", "No", 200, "Forbidden", "Team Member cannot access " + entityType + " created by Account Owner - TC003"},
        {"AccountOwner", "RestrictedTeamMember", "No", 200, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Account Owner - TC004"},
        {"AccountOwner", "CustomRoleTeamOnly", "No", 200, "Forbidden", "Custom Role 2 cannot access " + entityType + " created by Account Owner - TC005"},
        {"AccountOwner", "CustomRoleNothing", "No", 200, "Forbidden", "Custom Role 3 cannot access " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", "Yes", 200, "Success", "Account Owner can access " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", "Yes", 200, "Success", "Admin can access " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", "No", 200, "Forbidden", "Team Member cannot access " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", "No", 200, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", "No", 200, "Forbidden", "Custom Role 2 cannot access " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", "No", 200, "Forbidden", "Custom Role 3 cannot access " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", "Yes", 200, "Success", "Account Owner can access " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", "Yes", 200, "Success", "Admin can access " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", "Yes", 200, "Success", "Team Member can access " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", "Yes", 200, "Forbidden", "Restricted Team Member can access " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", "Yes", 200, "Forbidden", "Custom Role 2 can access " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", "Yes", 200, "Forbidden", "Custom Role 3 can access " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", "Yes", 200, "Success", "Account Owner can access " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", "Yes", 200, "Success", "Admin can access " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", "Yes", 200, "Forbidden", "Team Member can access " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", "Yes", 200, "Success", "Restricted Team Member can access " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", "Yes", 200, "Forbidden", "Custom Role 2 can access " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", "Yes", 200, "Forbidden", "Custom Role 3 can access " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role 2 (Team only) created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", "Yes", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role 2 - TC025"},
            {"CustomRoleTeamOnly", "Admin", "Yes", 200, "Success", "Admin can access " + entityType + " created by Custom Role 2 - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", "Yes", 200, "Forbidden", "Team Member can access " + entityType + " created by Custom Role 2 - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", "Yes", 200, "Forbidden", "Restricted Team Member can access " + entityType + " created by Custom Role 2 - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", "Yes", 200, "Forbidden", "Custom Role 2 can access " + entityType + " created by Custom Role 2 - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", "Yes", 200, "Forbidden", "Custom Role 3 can access " + entityType + " created by Custom Role 2 - TC030"},

            // Custom Role 3 (Nothing) created entities - testing different executors
            {"CustomRoleNothing", "Admin", "No", 200, "Success", "Admin can access " + entityType + " created by Custom Role 3 - TC031"},
            {"CustomRoleNothing", "AccountOwner", "No", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role 3 - TC032"},
            {"CustomRoleNothing", "TeamMember", "Yes", 200, "Forbidden", "Team Member cannot access " + entityType + " created by Custom Role 3 - TC033"},
            {"CustomRoleNothing", "RestrictedTeamMember", "No", 200, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Custom Role 3 - TC034"},
            {"CustomRoleNothing", "CustomRoleTeamOnly", "No", 200, "Forbidden", "Custom Role 2 cannot access " + entityType + " created by Custom Role 3 - TC035"},
            {"CustomRoleNothing", "CustomRoleNothing", "No", 200, "Forbidden", "Custom Role 3 cannot access " + entityType + " created by Custom Role 3 - TC036"}
        };

        // Dynamic filtering based on role parameter format
        if (roleParam == null || roleParam.equals("all")) {
            return allTestCases;
        }

        List<Object[]> filtered = new ArrayList<>();
        
        // Check if it's a pairwise format (e.g., "Admin-AccountOwner")
        if (roleParam.contains("-")) {
            String[] parts = roleParam.split("-");
            if (parts.length == 2) {
                String creatorRole = parts[0];
                String executorRole = parts[1];
                
                for (Object[] row : allTestCases) {
                    String creator = (String) row[0];
                    String executor = (String) row[1];
                    
                    if (creator.equals(creatorRole) && executor.equals(executorRole)) {
                        filtered.add(row);
                    }
                }
            }
        } else {
            // Single role format - filter by executor only
            for (Object[] row : allTestCases) {
                String executor = (String) row[1];
                if (executor.equals(roleParam)) {
                    filtered.add(row);
                }
            }
        }
        
        return filtered.toArray(new Object[0][]);
    }

    /**
     * Data Provider specifically for Email Template Access Testing
     */
    public static Object[][] getEmailTemplateAccessData(ITestContext context) {
        return get4LevelAccessData(context, "emailTemplate");
    }

    /**
     * Data Provider specifically for Placement Access Testing
     */
    public static Object[][] getPlacementAccessData(ITestContext context) {
        return get4LevelAccessData(context, "placement");
    }
}
