package com.crucibleapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.cert.CRLReason;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;


public class CrucibleAPIsTest {
	
	private int actualResponseCode = 200;
	private int port = 1102;
	private int timeout = 10000;
	private String appVersion = "v223";
	private String appVersionSearch = "v200";
	private String appVersionDelete = "v221";
	private String host = "dft-api-host1";	
	
	private String[] getDataFromWeb(String testUrl, String reqProp){
		
		String[] resArray = new String[]{"",""};
		
		try {
			URL url = new URL(testUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if(reqProp != null){
			 conn.setRequestProperty("Accept", reqProp);
			}						
			int expectedResponseCode = conn.getResponseCode();
			resArray[0] = String.valueOf(expectedResponseCode);
			
			Scanner scan = new Scanner(url.openStream());
			String entireResponse = new String();
			while (scan.hasNext())
			entireResponse += scan.nextLine();

			resArray[1] = entireResponse; 
					
			scan.close();
			
			conn.disconnect();
			
			} catch (MalformedURLException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			
		return resArray;
	}
	
	private String[] postDataToWeb(String testUrl, String reqProp, String input){
		
		String[] resArray = new String[]{"",""};
		
		try {
			URL url = new URL(testUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			if(reqProp != null){
			 conn.setRequestProperty("content-type", reqProp);
			}				
	        			
	      
						
	        OutputStream os = conn.getOutputStream();
	        os.write(input.getBytes());
	        os.flush();
	        
	        resArray[0] = String.valueOf(conn.getResponseCode());	    
			String message = conn.getResponseMessage();
	        BufferedReader br = new BufferedReader(new InputStreamReader(
	                (conn.getInputStream())));

	        String output;
	        while ((output = br.readLine()) != null) {
				resArray[1] = output;
	        }

	        conn.disconnect();
			
			} catch (MalformedURLException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			
		return resArray;
	}
	
	
	private String[] deleteDataFromWeb(String testUrl, String reqProp, String input){
		
		String[] resArray = new String[]{"",""};
		
		try {
			URL url = new URL(testUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
			conn.setRequestMethod("DELETE");
			if(reqProp != null){
			 conn.setRequestProperty("content-type", reqProp);
			}				
	        			
	      
						
	        OutputStream os = conn.getOutputStream();
	        os.write(input.getBytes());
	        os.flush();	        
	  
			resArray[0] = String.valueOf(conn.getResponseCode());	    
	        BufferedReader br = new BufferedReader(new InputStreamReader(
	                (conn.getInputStream())));

	        String output;
	        while ((output = br.readLine()) != null) {
				resArray[1] = output;
	        }

	        conn.disconnect();
			
			} catch (MalformedURLException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				System.err.println(e.getMessage());			
			}
			
		return resArray;
	}	


	private boolean createProject(String appVersion, boolean delete) throws JSONException{
		
		boolean success = false;
		
		String[] resArray = searchProject(appVersion, null);
		int expectedResponseCode = Integer.parseInt(resArray[0]);

		if(expectedResponseCode != 200){
			success = createProjectIfNotExists(appVersion, delete);
		}else{			
		
			int responseCode = deleteProject(appVersion);
			if(responseCode == actualResponseCode){
				success = createProjectIfNotExists(appVersion, delete);
			}			
		}		
		return success;
	}
	
	private boolean createProjectIfNotExists(String appVersion2, boolean delete) throws JSONException {
		// TODO Auto-generated method stub
		boolean success = false;
		System.out.println("\n"+"Create Project API called with appVersion: "+appVersion2);
		
		String web_url = "http://dft-api-host1:1102/dft/scr/crucible/project/create";		
		String input = "{\"groupName\":\"it-cits\", \"functionalGroupName\":\"cc\", \"stackName\":\"tempStack\", \"appName\":\"app1\", \"versionNo\":\""+appVersion2+"\", "
			  +"\"developerAdamGroup\":\"dft-api-developers\", \"reviewerAdamGroup\":\"dft-api-admins\", \"moderatorAdamGroup\":\"dft-api-admins\","
			  +"\"repoUrl\":\"https://repo-svn.cisco.com/svn/CiscoMain\", \"repoPath\":\"/IT/CITS/Architecture/mobile/ios/seedapp/trunk\", "
			  +"\"repoType\":\"Subversion\", \"description\":\"tempdesc\"}";
	
			String[] resArray = postDataToWeb(web_url, "application/json", input);
	
			int expectedResponseCode = Integer.parseInt(resArray[0]);
			String response = resArray[1];
			
			assertTrue(expectedResponseCode, response, "Testcase1 of create Project API. Assertion of response code and response contains permissionSchemeName ", "permissionSchemeName");	
			
			JSONObject obj = new JSONObject(response);
			String message = obj.getString("message");
			String permissionSchemeName = obj.getString("permissionSchemeName");
			String repoName = obj.getString("repoName");
			String projectName = obj.getString("projectName");
			String errorMessage = obj.getString("errorMessage");		
			
			assertEquals("Testcase2 of create project API. Assertion of project creation: ", message, "Project created successfully.");
			
			if(expectedResponseCode == 200 && delete){
				int resCode = deleteProject(appVersion2);
				if(resCode == 200){
					success = true;
				}
			}		
			
			return success;
	}

	private String[] searchProject(String appVersion, String appVersionSearch) throws JSONException{
		boolean success = false;
		if(appVersion == null){
			appVersion = appVersionSearch;
		}
		
		System.out.println("Search Project API called with appVersion: "+appVersion);
		
		String web_url = "http://dft-api-host1:1102/dft/scr/crucible/project/search";	
		
	    
		String input = "{\"groupName\": \"it-cits\", \"functionalGroupName\":\"cc\", \"stackName\":\"tempStack\", \"appName\":\"app1\", \"versionNo\":\""+appVersion+"\","
	        		+"\"developerAdamGroup\": \"dft-api-developers\", \"reviewerAdamGroup\":\"dft-api-admins\", \"moderatorAdamGroup\":\"dft-api-admins\","
	        	+ "\"repoUrl\": \"https://repo-svn.cisco.com/svn/CiscoMain\", \"repoPath\":\"/IT/CITS/Architecture/mobile/ios/seedapp/trunk\","
	        	 +"\"repType\": \"Subversion\", \"description\":\"tempdesc\"}";			
		
		String[] resArray = postDataToWeb(web_url, "application/json", input);		
		
		int expectedResponseCode = Integer.parseInt(resArray[0]);
		
		String response = resArray[1];
		if(expectedResponseCode == 200){
			System.out.println("Project with versionName: "+appVersion+" exists.");
			assertTrue(expectedResponseCode, response, "TestCase1 of search Project API. Assertion of response code and response contains key permissionSchemeName ", "permissionSchemeName");	
		      
			JSONObject obj = new JSONObject(resArray[1] );
			String message = obj.getString("message");
			String permissionSchemeName = obj.getString("permissionSchemeName");
			String repoName = obj.getString("repoName");
			String projectName = obj.getString("projectName");
			String errorMessage = obj.getString("errorMessage");		
			
	 		assertEquals("Testcase2 of search project API. Assertion of message ", message, "PROJECT_EXISTS");
	 		
		}else{			
			System.out.println("Project with versionName: "+appVersion+" doesnot exist.");
		}
		
 		return resArray;		
	}
	
	
	private int deleteProject(String appVersion) throws JSONException{
		System.out.println("\n"+"Delete Project API called with appVersion: "+appVersion);
		
		String web_url = "http://dft-api-host1:1102/dft/scr/crucible/project/delete";		
		
		String input = "{\"groupName\":\"it-cits\", \"functionalGroupName\":\"cc\", \"stackName\":\"tempStack\", \"appName\":\"app1\", \"versionNo\":\""+appVersion+"\", "
				  +"\"developerAdamGroup\":\"dft-api-developers\", \"reviewerAdamGroup\":\"dft-api-admins\", \"moderatorAdamGroup\":\"dft-api-admins\","
				  +"\"repoUrl\":\"https://repo-svn.cisco.com/svn/CiscoMain\", \"repoPath\":\"/IT/CITS/Architecture/mobile/ios/seedapp/trunk\", "
				  +"\"repoType\":\"Subversion\", \"description\":\"tempdesc\"}";		
		
		String[] resArray = deleteDataFromWeb(web_url, "application/json", input);
		
		int expectedResponseCode = Integer.parseInt(resArray[0]);
		String response = resArray[1];
		
		if(expectedResponseCode == actualResponseCode){
		assertTrue(expectedResponseCode, response, "Testcase1 of delete Project API. Assertion of response code and response contains permissionSchemeName  ", "permissionSchemeName");	
		
		JSONObject obj = new JSONObject(resArray[1] );
		String message = obj.getString("message");
		String permissionSchemeName = obj.getString("permissionSchemeName");
		String repoName = obj.getString("repoName");
		String projectName = obj.getString("projectName");
		String errorMessage = obj.getString("errorMessage");		
		
		Assert.assertEquals("TestCase Success: Project has been deleted successfully: ",message, "Deletion operation successful");
		assertEquals("Testcase2 of delete project API. Assertion of project deletion: ", message, "Deletion operation successful");
		}else{
			System.out.println("Delete Project failed: "+expectedResponseCode);
		}
		return expectedResponseCode;
		
	}	
	
	public void assertTrue(int expectedResponseCode, String response, String description, String object){
	     try{
	    	 Assert.assertTrue(expectedResponseCode == actualResponseCode);
	    	 Assert.assertTrue(response.contains(object));
			 System.out.println("\n"+description + " - passed with: "+"responseCode: "+expectedResponseCode+" \n and response is: "+response);
	     }catch(AssertionError e){
	          System.out.println("\n"+description + " - failed");

	        throw e;
	     }
	}
	
	public void assertEquals(String description, String expected, String actual){
	     try{
	 		Assert.assertEquals(description,expected, actual);
	 		System.out.println("\n"+description + " - passed with: "+"message "+expected);
	     }catch(AssertionError e){
	          System.out.println("\n"+description + " - failed"+ "with "+expected);

	        throw e;
	     }
	}
	
	public void assertHostConnected(boolean connnected, String description){
	     try{
	    	 Assert.assertTrue(connnected);
	    	 System.out.println("\n"+description + " - reachable \n");
	     }catch(AssertionError e){
	          System.out.println("\n"+description + " - not reachable \n");
	        throw e;
	     }
	}

	
	private void testABCApi(){
		System.out.println("Test abc API called: ");
		
		String web_url = "http://dft-api-host1:1102/dft/scr/crucible/test/abc";		
		
		String[] resArray = getDataFromWeb(web_url, null);
		
		int expectedResponseCode = Integer.parseInt(resArray[0]);
		String response = resArray[1];
		
		assertTrue(expectedResponseCode, response, "TestCase1 of abc API. Assertion of response code ", "abc");	
		assertEquals("TestCase2 of abc API. Assertion of response ", response, "abc");	
	
	}	
		
	public static boolean pingHost(String host, int port, int timeout) {
	    try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(host, port), timeout);
	        return true;
	    } catch (IOException e) {
	        return false; // Either timeout or unreachable or failed DNS lookup.
	    }
	}	
	
	@Rule
    public TestRulesSetter pr = new TestRulesSetter(System.out);
	
	@Test
	public void crucibleAPITestABC() throws Exception {	
		boolean connected = pingHost(host, port, timeout);
	    assertHostConnected(connected, "crucibleAPITest1 Host Available Check : Host is");
	    testABCApi();
	}	
	
	
	@Test
	public void crucibleAPITestSearchCreateDelete() throws Exception {
		boolean connected = pingHost(host, port, timeout);
	    assertHostConnected(connected, "crucibleAPITest3 Host Available Check : Host is");		
	    createProject(appVersion, true);
	}	
	
	
	}

	


