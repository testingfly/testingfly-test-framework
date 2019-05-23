package com.javaexcel.automation.core.utils;
//package com.wellsfargo.automation.util;
//
//import java.util.Base64;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
//import com.wellsfargo.automation.core.data.Config;
//import com.wellsfargo.automation.core.data.Configurables;
//import com.wellsfargo.automation.core.data.DatabaseObjectMapper;
//import com.wellsfargo.automation.core.data.MultiUserInfo;
//import com.wellsfargo.automation.core.db.AutomationDatabase;
//import com.wellsfargo.automation.core.table.Record;
//import com.wellsfargo.automation.core.table.Table;
//import com.wellsfargo.automation.core.utils.PasswordGenerator;
//import com.wellsfargo.automation.core.utils.Utils;
//
//public class MultiUserInfoDetails {
//
//	/***
//	 * Get the UserCredentials, URL firstName, lastName from MultiUser table.
//	 * 
//	 * @param applicationName
//	 * @param applicationArea
//	 * @param environment
//	 * @param role
//	 * @return
//	 */
//
//	public static MultiUserInfo getUserDetails(String applicationName, String applicationArea, String environment,
//			String role) {
//		MultiUserInfo multiUserInfo = new MultiUserInfo();
//		try {
//			Table resultTable = AutomationDatabase
//					.query2(userInfoDetailsQuery(applicationName, applicationArea, environment, role));
//
//			for (Record record : resultTable.getRecords()) {
//				multiUserInfo.setUrl(record.getValue("APPLICATION_URL").trim());
//				multiUserInfo.setLoginID(record.getValue("LoginID").trim());
//
//				if (Utils.checkExists(record.getValue("Password").trim())
//						&& Utils.checkExists(record.getValue("SecretKey").trim())) {
//					multiUserInfo.setSecretKey(record.getValue("SecretKey").trim());
//					byte[] decodedKey = Base64.getDecoder().decode(record.getValue("SecretKey").trim());
//					SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//					String password = PasswordGenerator.decrypt(record.getValue("Password").trim(), originalKey);
//					multiUserInfo.setPassword(password);
//				}
//				multiUserInfo.setId(Integer.valueOf(record.getValue("ID")));
//				multiUserInfo.setStatus(record.getValue("Status"));
//				multiUserInfo.setParallel_Login(record.getValue("Parallel_Login"));
//				multiUserInfo.setFirstName(record.getValue("FirstName"));
//				multiUserInfo.setLastName(record.getValue("LastName"));
//			}
//			if (multiUserInfo.getStatus() != null && multiUserInfo.getStatus().equals("Available")
//					&& multiUserInfo.getParallel_Login() != null
//					&& multiUserInfo.getParallel_Login().equals("NotAllowed")) {
//				updateUserStatus("NotAvailable", (int) multiUserInfo.getId());
//
//			}
//
//			return multiUserInfo;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	/***
//	 * Get User Details based on logIn Id and Environment
//	 * 
//	 * @param applicationName
//	 * @param environment
//	 * @param logInId
//	 * @return
//	 */
//	public static MultiUserInfo getUserInfo_LoginId(String applicationName, String environment, String logInId) {
//		MultiUserInfo multiUserInfo = new MultiUserInfo();
//		try {
//			Table resultTable = AutomationDatabase.query2("Select LoginID,FirstName, LastName from "
//					+ Configurables.userCredentialTable + " WHERE " + "LoginID= " + logInId);
//			for (Record record : resultTable.getRecords()) {
//				multiUserInfo.setUrl(record.getValue("APPLICATION_URL").trim());
//				multiUserInfo.setLoginID(record.getValue("LoginID").trim());
//				if (Utils.checkExists(record.getValue("Password").trim())
//						&& Utils.checkExists(record.getValue("SecretKey").trim())) {
//					multiUserInfo.setSecretKey(record.getValue("SecretKey").trim());
//					byte[] decodedKey = Base64.getDecoder().decode(record.getValue("SecretKey").trim());
//					SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//					String password = PasswordGenerator.decrypt(record.getValue("Password").trim(), originalKey);
//					multiUserInfo.setPassword(password);
//				}
//				multiUserInfo.setId(Integer.valueOf(record.getValue("ID")));
//				multiUserInfo.setStatus(record.getValue("Status"));
//				multiUserInfo.setParallel_Login(record.getValue("Parallel_Login"));
//				multiUserInfo.setFirstName(record.getValue("FirstName"));
//				multiUserInfo.setLastName(record.getValue("LastName"));
//			}
//			return multiUserInfo;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//
//	}
//
//	/***
//	 * Update the status in MultiUser table every time we fetch the credentials
//	 * details
//	 * 
//	 * @param status
//	 * @param id
//	 */
//
//	public static void updateUserStatus(String status, int id) {
//		try {
//			String query = "update " + Configurables.userCredentialTable + " set "
//					+ Configurables.objectRepositoryStatusField + " = '" + status + "'" + " where Id= " + id;
//			AutomationDatabase.executeCommand(query);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	/***
//	 * Building the Query for Credential Search based on below parameter 
//	 * 
//	 * @param applicationName
//	 * @param applicationArea
//	 * @param environment
//	 * @param role
//	 * @return
//	 */
//	private static String userInfoDetailsQuery(String applicationName, String applicationArea, String environment,
//			String role) {
//		StringBuilder baseQuery = null;
//		if (Utils.checkExists(applicationArea)) {
//			baseQuery = baseMultiUserQueryWithApplicationArea(applicationName, applicationArea, environment, role);
//		} else {
//			baseQuery = baseMultiUserQuery(applicationName, applicationArea, environment, role);
//		}
//
//		if (Utils.checkExists(role)) {
//			baseQuery.append(" AND " + "MU." + Configurables.objectRepositoryRoleField + " = '" + role + "'")
//					.toString();
//		}
//		return baseQuery.toString();
//
//	}
///***
// * Build the base query to Search the User Credential details
// * @param applicationName
// * @param applicationArea
// * @param environment
// * @param role
// * @return
// */
//	public static StringBuilder baseMultiUserQuery(String applicationName, String applicationArea, String environment,
//			String role) {
//		StringBuilder baseQuery = new StringBuilder(
//				"Select TOP 1  MU.LoginID,MU.Password ,MU.ID, URL.APPLICATION_URL,MU.Status,MU.Parallel_Login, MU.FirstName,MU.LastName,MU.SecretKey"
//						+ " FROM " + Configurables.urlDetailsTable + " URL" + " join "
//						+ Configurables.userCredentialTable + " MU " + " ON " + 
//						"URL" + "."	+ Configurables.objectRepositoryApplNameField + "=" + " MU" + "."
//						+ Configurables.objectRepositoryApplNameField + " AND " + 
//						"URL" + "."	+ Configurables.objectRepositoryEnvironmentField + "=" + " MU" + "."
//						+ Configurables.objectRepositoryEnvironmentField 
//						+ " WHERE " + "URL." + Configurables.objectRepositoryApplNameField + " = '" + applicationName
//						+ "'" + " AND " + "URL." + Configurables.objectRepositoryEnvironmentField + " = '" + environment
//						+ "'" + " AND " + "MU." + Configurables.objectRepositoryStatusField + " = 'Available'");
//		return baseQuery;
//		
//	}
//	public static StringBuilder baseMultiUserQueryWithApplicationArea(String applicationName, String applicationArea, String environment,
//			String role) {
//		StringBuilder baseQuery = new StringBuilder(
//				"Select TOP 1  MU.LoginID,MU.Password ,MU.ID, URL.APPLICATION_URL,MU.Status,MU.Parallel_Login, MU.FirstName,MU.LastName,MU.SecretKey"
//						+ " FROM " + Configurables.urlDetailsTable + " URL" + " join "
//						+ Configurables.userCredentialTable + " MU " + " ON " + 
//						"URL" + "."	+ Configurables.objectRepositoryApplNameField + "=" + " MU" + "."
//						+ Configurables.objectRepositoryApplNameField + " AND " + 
//						"URL" + "."	+ Configurables.objectRepositoryEnvironmentField + "=" + " MU" + "."
//						+ Configurables.objectRepositoryEnvironmentField 
//						+ " AND " + 
//						"URL" + "."	+ Configurables.objectRepositoryApplAreaField + "=" + " MU" + "."
//						+ Configurables.objectRepositoryApplAreaField
//
//						+ " WHERE " + "URL." + Configurables.objectRepositoryApplNameField + " = '" + applicationName
//						+ "'" + " AND " + "URL." + Configurables.objectRepositoryEnvironmentField + " = '" + environment
//						+ "'" + " AND " + "MU." + Configurables.objectRepositoryStatusField + " = 'Available'"
//						+ " AND " + "URL." + Configurables.objectRepositoryApplAreaField + " = '" + applicationArea + "'");
//		return baseQuery;
//	}
//}
