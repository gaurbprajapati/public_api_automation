package io.rcrm.api.commanfunctions.albatross;

import org.testng.ITestContext;
import java.util.ArrayList;
import java.util.List;


public class RBAC6LevelDataProvider {

    public static Object[][] getViewAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");

        // Define comprehensive test cases for 6-level access permissions
        Object[][] allTestCases = {
            // Format: {creator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // Account Owner created entities - testing different executors
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can access " + entityType + " created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can access " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot access " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access " + entityType + " created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access " + entityType + " created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Custom Role Team Only - TC030"},
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

    public static Object[][] getEditAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");
        // Define comprehensive test cases for 6-level access permissions
        Object[][] allTestCases = {
            // Format: {creator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // Account Owner created entities - testing different executors
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can access " + entityType + " created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can access " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot access " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access " + entityType + " created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access " + entityType + " created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Custom Role Team Only - TC030"},
        };

        return filterTestCases(allTestCases, roleParam);
    }

    public static Object[][] getDeleteAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");
        // Define comprehensive test cases for 6-level delete access permissions
        Object[][] allTestCases = {
            // Format: {creator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // Account Owner created entities - testing different executors
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can delete " + entityType + " created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can delete " + entityType + " created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 401, "Forbidden", "Team Member cannot delete " + entityType + " created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot delete " + entityType + " created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot delete " + entityType + " created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot delete " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can delete " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can delete " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", 401, "Forbidden", "Team Member cannot delete " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot delete " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot delete " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot delete " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can delete " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can delete " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 401, "Forbidden", "Team Member cannot delete " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot delete " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot delete " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot delete " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can delete " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can delete " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 401, "Forbidden", "Team Member cannot delete " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot delete " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot delete " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot delete " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can delete " + entityType + " created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can delete " + entityType + " created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 401, "Forbidden", "Team Member cannot delete " + entityType + " created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot delete " + entityType + " created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only cannot delete " + entityType + " created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot delete " + entityType + " created by Custom Role Team Only - TC030"},
        };

        return filterTestCases(allTestCases, roleParam);
    }

    private static Object[][] filterTestCases(Object[][] allTestCases, String roleParam) {
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

    public static Object[][] getDealViewAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");

        // Define comprehensive test cases for 6-level access permissions
        Object[][] allTestCases = {
            // Format: {creator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // Account Owner created entities - testing different executors
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can access " + entityType + " created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can access " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot access " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access " + entityType + " created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access " + entityType + " created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Custom Role Team Only - TC030"},
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

    public static Object[][] getDealEditAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");

        // Define comprehensive test cases for 6-level access permissions
        Object[][] allTestCases = {
            // Format: {creator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // Account Owner created entities - testing different executors
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can access " + entityType + " created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can access " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 200, "Success", "Restricted Team Member can access " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot access " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access " + entityType + " created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access " + entityType + " created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Custom Role Team Only - TC030"},
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

    public static Object[][] getDealDeleteAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");

        // Define comprehensive test cases for 6-level access permissions
        Object[][] allTestCases = {
            // Format: {creator, executor, expectedStatusCode, expectedMessage, testDescription}
            
            // Account Owner created entities - testing different executors
            {"AccountOwner", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Account Owner - TC001"},
            {"AccountOwner", "Admin", 200, "Success", "Admin can access " + entityType + " created by Account Owner - TC002"},
            {"AccountOwner", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Account Owner - TC003"},
            {"AccountOwner", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Account Owner - TC004"},
            {"AccountOwner", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Account Owner - TC005"},
            {"AccountOwner", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Account Owner - TC006"},

            // Admin created entities - testing different executors
            {"Admin", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Admin - TC007"},
            {"Admin", "Admin", 200, "Success", "Admin can access " + entityType + " created by Admin - TC008"},
            {"Admin", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Admin - TC009"},
            {"Admin", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Admin - TC010"},
            {"Admin", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Admin - TC011"},
            {"Admin", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Admin - TC012"},

            // Team Member created entities - testing different executors
            {"TeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Team Member - TC013"},
            {"TeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Team Member - TC014"},
            {"TeamMember", "TeamMember", 200, "Success", "Team Member can access " + entityType + " created by Team Member - TC015"},
            {"TeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Team Member - TC016"},
            {"TeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only can access " + entityType + " created by Team Member - TC017"},
            {"TeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Team Member - TC018"},

            // Restricted Team Member created entities - testing different executors
            {"RestrictedTeamMember", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Restricted Team Member - TC019"},
            {"RestrictedTeamMember", "Admin", 200, "Success", "Admin can access " + entityType + " created by Restricted Team Member - TC020"},
            {"RestrictedTeamMember", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Restricted Team Member - TC021"},
            {"RestrictedTeamMember", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member can access " + entityType + " created by Restricted Team Member - TC022"},
            {"RestrictedTeamMember", "CustomRoleTeamOnly", 401, "Forbidden", "Custom Role Team Only cannot access " + entityType + " created by Restricted Team Member - TC023"},
            {"RestrictedTeamMember", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Restricted Team Member - TC024"},

            // Custom Role Team Only created entities - testing different executors
            {"CustomRoleTeamOnly", "AccountOwner", 200, "Success", "Account Owner can access " + entityType + " created by Custom Role Team Only - TC025"},
            {"CustomRoleTeamOnly", "Admin", 200, "Success", "Admin can access " + entityType + " created by Custom Role Team Only - TC026"},
            {"CustomRoleTeamOnly", "TeamMember", 401, "Forbidden", "Team Member can access " + entityType + " created by Custom Role Team Only - TC027"},
            {"CustomRoleTeamOnly", "RestrictedTeamMember", 401, "Forbidden", "Restricted Team Member cannot access " + entityType + " created by Custom Role Team Only - TC028"},
            {"CustomRoleTeamOnly", "CustomRoleTeamOnly", 200, "Success", "Custom Role Team Only can access " + entityType + " created by Custom Role Team Only - TC029"},
            {"CustomRoleTeamOnly", "CustomRoleNothing", 401, "Forbidden", "Custom Role Nothing cannot access " + entityType + " created by Custom Role Team Only - TC030"},
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

    // Deal-specific data providers
    public static Object[][] getDealViewAccessData(ITestContext context) {
        return getDealViewAccessData(context, "deal");
    }

    public static Object[][] getDealEditAccessData(ITestContext context) {
        return getDealEditAccessData(context, "deal");
    }

    public static Object[][] getDealDeleteAccessData(ITestContext context) {
        return getDealDeleteAccessData(context, "deal");
    }
}
