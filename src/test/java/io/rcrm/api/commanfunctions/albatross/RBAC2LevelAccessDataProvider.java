package io.rcrm.api.commanfunctions.albatross;
import org.testng.ITestContext;
import java.util.ArrayList;
import java.util.List;
/**
 * Common Data Provider for 2-Level Access Permissions
 * Supports: Yes/No access patterns based on role creation
 * 
 * Can be used for:
 * - Create entity Access
 */
public class RBAC2LevelAccessDataProvider {
    /**
     * Data Provider for 2-Level Access Testing
     * 
     * @param context TestNG context for parameter filtering
     * @return Object array with test data
     */
    public static Object[][] get2LevelAccessData(ITestContext context, String entityType) {
        String roleParam = context.getCurrentXmlTest().getParameter("role");
        // Define comprehensive test cases for 2-level access permissions
        Object[][] allTestCases = {
            // Format: {role, access, expectedStatusCode, expectedMessage, testDescription}
        
            // Account Owner created entities - testing different executors
            {"AccountOwner", "Yes", 200, "Success", "Account Owner can create " + entityType + " - TC001"},
            {"Admin", "Yes", 200, "Success", "Admin can create " + entityType + " - TC002"},
            {"TeamMember", "Yes", 200, "Success", "Team Member can create " + entityType + " - TC003"},
            {"RestrictedTeamMember", "Yes", 200, "Success", "RestrictedTeam Member can create " + entityType + " - TC004"},
            {"CustomRoleTeamOnly", "Yes", 200, "Success", "Custom Role Team Only can create " + entityType + " - TC005"},
            {"CustomRoleNothing", "No", 401, "Forbidden", "Custom Role Nothing cannot create " + entityType + " - TC006"}

        };
        // Dynamic filtering based on role parameter
        if (roleParam == null || roleParam.equals("all")) {
            return allTestCases;
        }
        List<Object[]> filtered = new ArrayList<>();
        
        // Filter by role
        for (Object[] row : allTestCases) {
            String role = (String) row[0];
            if (role.equals(roleParam)) {
                filtered.add(row);
            }
        }
        
        return filtered.toArray(new Object[0][]);
    }
    /**
     * Data Provider specifically for create entity access
     */
    public static Object[][] getCandidateAccessData(ITestContext context) {
        return get2LevelAccessData(context, "candidate");
    }

    /**
     * Data Provider specifically for create company access
     */
    public static Object[][] getCompanyAccessData(ITestContext context) {
        return get2LevelAccessData(context, "company");
    }

    /**
     * Data Provider specifically for create contact access
     */
    public static Object[][] getContactAccessData(ITestContext context) {
        return get2LevelAccessData(context, "contact");
    }

    /**
     * Data Provider specifically for create job access
     */
    public static Object[][] getJobAccessData(ITestContext context) {
        return get2LevelAccessData(context, "job");
    }

    /**
     * Data Provider specifically for create deal access
     */
    public static Object[][] getDealAccessData(ITestContext context) {
        return get2LevelAccessData(context, "deal");
    }
}
