package testplug.handlers;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.edit.command.ReplaceCommand;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

public class RequestGenerator {

	HttpRequest req;
	String originalScriptVariables="";
	String originalScriptRequest="";
	String originalScriptimports="";
	String destinationPath="";
	
	
	public RequestGenerator(HttpRequest req,String destinationPath) {

		this.req=req;
		this.destinationPath=destinationPath;

		originalScriptimports="import static org.testng.Assert.assertEquals;\r\n" + 
				"import java.util.HashMap;\r\n" + 
				"import java.util.Map;\r\n" + 
				"import org.testng.annotations.Test;\r\n" + 
				"import io.restassured.RestAssured;\r\n" + 
				"import io.restassured.http.Headers;\r\n" + 
				"import io.restassured.http.Method;\r\n" + 
				"import io.restassured.response.Response;\r\n" + 
				"import io.restassured.specification.RequestSpecification;\n\n";


		originalScriptVariables+="String baseUrl=\""+req.baseUrl+"\";\n" + 
				"String basePath=\""+req.basePath+"\";\n";

		originalScriptRequest+=	"RestAssured.baseURI = baseUrl;\r\n" + 
				"RestAssured.basePath = basePath;\n";



		if (!req.HeaderParameters.isEmpty()) {

			originalScriptRequest+="//Make  Header parameters list \n";
			originalScriptRequest+="Map<String,Object> headerMap = new HashMap<String,Object>();\n";

			for(HeaderParameter header : req.HeaderParameters) {

				originalScriptRequest+="headerMap.put(\""+header.getName()+"\", H_"+header.getName()+");\n";	

				String type=header.getType();

				if (type.matches("array")) {
					type=header.getItems().getType();
					type=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase()+"[]";


				}else {

					type=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase();
				}



				originalScriptVariables+=type+" H_"+header.getName()+"="+header.getFormat()+";\n";

			}
		}

		if (!req.queryParameters.isEmpty()) {

			originalScriptRequest+="//Make Query parameter list  \n";
			originalScriptRequest+="Map<String,Object> queryMap = new HashMap<String,Object>();\n";

			for(QueryParameter queryparam : req.queryParameters) {

				originalScriptRequest+="queryMap.put(\""+queryparam.getName()+"\", Q_"+queryparam.getName()+");\n";	



				String type=queryparam.getType();

				if (type.matches("array")) {
					type=queryparam.getItems().getType();
					type=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase()+"[]";


				}else {

					type=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase();
				}


				originalScriptVariables+=type+" Q_"+queryparam.getName()+"="+queryparam.getFormat()+";\n";

			}
		}

		if (!req.formParameters.isEmpty()) {

			originalScriptRequest+="//Make Form parameter list \n";
			originalScriptRequest+="Map<String,Object> formMap = new HashMap<String,Object>();\n";

			for(FormParameter formparam : req.formParameters) {

				originalScriptRequest+="formMap.put(\""+formparam.getName()+"\", F_"+formparam.getName()+");\n";

				String type=formparam.getType();

				if (type.matches("array")) {
					type=formparam.getItems().getType();
					type=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase()+"[]";


				}else {

					type=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase();
				}


				originalScriptVariables+=type+" F_"+formparam.getName()+"="+formparam.getFormat()+";\n";

			}
		}

		if (!req.PathParameters.isEmpty()) {

			originalScriptRequest+="//Add Path parameters \n";
			originalScriptRequest+="Map<String,Object> pathMap = new HashMap<String,Object>();\n";

			for(PathParameter path : req.PathParameters) {

				originalScriptRequest+="pathMap.put(\""+path.getName()+"\", P_"+path.getName()+");\n";	

				String type=path.getType();
				originalScriptVariables+=type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase()+" P_"+path.getName()+"="+path.getFormat()+";\n";


			}
		}



		/*	for(Entry<String, Property> b : req.body) {

			script+=p.getClass().getTypeName()+" "+p.getName()+"= \"\";\n";

		}
		 */
		//List<String> required = new 


	}

	public String  validRequest() {


		String className="";
		if (req.operationId !="") {

			className=req.operationId+"ValidRequest";

		} else {

			className=req.method+req.path.replaceAll("/", "")+"ValidRequest";

		}
		className=className.replaceAll(" ", "");



		String scriptRequest="";
		String scriptVariables="";
		String scriptValidation="";
		String script="";

		script+=originalScriptimports +"public class "+className+" {\r\n" + 
				"\r\n" + 
				"	@Test\r\n" + 
				"	public void "+className+"()\r\n" + 
				"	{ \n  ";



		// Declare Permanent variables Declaration

		scriptVariables=originalScriptVariables;
		scriptRequest=originalScriptRequest;

		scriptRequest+="RequestSpecification httpRequest = RestAssured.given();\n";


		if (!req.HeaderParameters.isEmpty()) {

			scriptRequest+="httpRequest.headers(headerMap);\n";

		}

		if (!req.queryParameters.isEmpty()) {

			scriptRequest+="httpRequest.queryParams(queryMap);\n";

		}

		if (!req.formParameters.isEmpty()) {

			scriptRequest+="httpRequest.formParams(formMap);\n";

		}

		if (!req.PathParameters.isEmpty()) {

			scriptRequest+="httpRequest.pathParams(pathMap);\n";


		}
		if (req.body!=null) {

			scriptRequest+="httpRequest.body(\""+req.body.GetValidBodyString()+"\");\n";

		}
		



		scriptRequest+="\n\n// send request\n";
		scriptRequest+="Response response = httpRequest.request(Method."+req.method+", \""+req.path+"\");\n";


		scriptValidation+="//response Validation\n"+
				"String responseBody = response.getBody().asString();\n"+
				"System.out.println(\"Response Body is =>  \" + responseBody);\n"+
				"";

		scriptValidation+="assertEquals(response.getStatusCode(), "+req.SuccesResponsestatus+");\n";		
		scriptValidation+="assertEquals(response.jsonPath().get(\"hasError\").toString(), \"false\");\n";		




		script+=scriptVariables+"\n\n"+scriptRequest+"\n\n"+scriptValidation+"\n\n }\n}";	

		testcaseFileGenerator(className, script);


		return script;
	}

	public String  invalidHeaderRequests() {

		String className="";
		if (req.operationId !="") {

			className=req.operationId+"InvalidHeaderRequest";

		} else {

			className=req.method+req.path.replaceAll("/", "")+"InvalidHeaderRequest";

		}
		className=className.replaceAll(" ", "");



		String scriptRequest="";
		String scriptVariables="";
		String scriptValidation="";
		String script="";


		script+=originalScriptimports +"public class "+className+" {\r\n" + 
				"\r\n" + 
				"	@Test\r\n" + 
				"	public void "+className+"()\r\n" + 
				"	{ \n  ";






		// Declare Permanent variables Declaration

		scriptVariables=originalScriptVariables;
		scriptRequest=originalScriptRequest;


		//get required headers

		List<String> required=req.HeaderParameters
				.stream()
				.filter(H -> H.getRequired()==true)
				.map(name->"\""+name.getName()+"\"")
				.collect(Collectors.toList());
  
		if(required.isEmpty()) {
			
			System.out.println(className.replace("invalidHeaderRequest", " ")+"API doesn't have any required header");
			return "";
			
		}
		scriptVariables+="String[] requiredHeaders= {"+required.toString().substring(1, required.toString().length()-1)+"};\n";

		//add script for make request without required headers

		scriptRequest+="//delete one required header each time:\n";

		scriptRequest+="for (String key:requiredHeaders) {\n" + 
				"Map<String,Object> headerMap2 = new HashMap<String,Object>();\r\n" + 
				"headerMap2=headerMap;\r\n" + 
				"headerMap2.remove(key);\r\n" + 
				"RequestSpecification httpRequest = RestAssured.given();\n"+
				"httpRequest.headers(headerMap2);\n" ;


		if (!req.queryParameters.isEmpty()) {

			scriptRequest+="httpRequest.queryParams(queryMap);\n";

		}

		if (!req.formParameters.isEmpty()) {

			scriptRequest+="httpRequest.formParams(formMap);\n";

		}

		if (!req.PathParameters.isEmpty()) {

			scriptRequest+="httpRequest.pathParams(pathMap);\n";


		}
		
		if (req.body!=null) {

			scriptRequest+="httpRequest.body(\""+req.body.GetValidBodyString()+"\");\n";

		}



		scriptRequest+="\n\n// send request\n";
		scriptRequest+="Response response = httpRequest.request(Method."+req.method+", \""+req.path+"\");\n";
		scriptRequest+="\n";


		scriptValidation+="//response Validation\n"+
				"String responseBody = response.getBody().asString();\n"+
				"System.out.println(\"Response Body is =>  \" + responseBody);\n"+
				"";

		scriptValidation+="assertEquals(response.getStatusCode(), *** );\n";	
	



		script+=scriptVariables+"\n\n"+scriptRequest+"\n\n"+scriptValidation+"		}\n"+"\n\n }\n}";	

		testcaseFileGenerator(className, script);

		return script;


	}


	public String  invalidParametersRequests() {
		
		
		boolean	notrequired=true;

		String className="";
		if (req.operationId !="") {

			className=req.operationId+"InvalidParameterRequest";

		} else {

			className=req.method+req.path.replaceAll("/", "")+"InvalidParameterRequest";

		}

		className=className.replaceAll(" ", "");


		String scriptRequest="";
		String scriptVariables="";
		String scriptValidation="";
		String script="";


		script+=originalScriptimports +"public class "+className+" {\r\n" + 
				"\r\n" + 
				"	@Test\r\n" + 
				"	public void "+className+"()\r\n" + 
				"	{ \n  ";






		// Declare Permanent variables Declaration

		scriptVariables=originalScriptVariables;
		scriptRequest=originalScriptRequest;


		//get required parameters
		if (!req.queryParameters.isEmpty()) {

			List<String> required=req.queryParameters
					.stream()
					.filter(H -> H.getRequired()==true)
					.map(name->"\""+name.getName()+"\"")
					.collect(Collectors.toList());
			if(!required.isEmpty()) {
				notrequired=false;
				scriptVariables+="String[] requiredQueryParameters= {"+required.toString().substring(1, required.toString().length()-1)+"};\n";
				scriptRequest+="//delete one required QueryParameters each time:\n";

				scriptRequest+="for (String key : requiredQueryParameters) {\n" + 
						"Map<String,Object> queryMap2 = new HashMap<String,Object>();\r\n" + 
						"queryMap2=queryMap;\r\n" + 
						"queryMap2.remove(key);\r\n" + 
						"RequestSpecification httpRequest = RestAssured.given();\n"+
						"httpRequest.queryParams(queryMap2);\n" ;
				

				if (!req.HeaderParameters.isEmpty()) {

					scriptRequest+="httpRequest.headers(headerMap);\n";

				}
				
				
				if (!req.formParameters.isEmpty()) {

					scriptRequest+="httpRequest.formParams(formMap);\n";

				}

				if (!req.PathParameters.isEmpty()) {

					scriptRequest+="httpRequest.pathParams(pathMap);\n";


				}
			}
			
		}


		
		



		if (!req.formParameters.isEmpty()) {

			List<String> required=req.formParameters
					.stream()
					.filter(H -> H.getRequired()==true)
					.map(name->"\""+name.getName()+"\"")
					.collect(Collectors.toList());
			if(!required.isEmpty()) {
				notrequired=false;
				scriptVariables+="String[] requiredformParameters= {"+required.toString().substring(1, required.toString().length()-1)+"};\n";
				scriptRequest+="//delete one required FormParameters each time:\n";

				scriptRequest+="for (String key : requiredformParameters) {\n" + 
						"Map<String,Object> formMap2 = new HashMap<String,Object>();\r\n" + 
						"formMap2=formMap;\r\n" + 
						"formMap2.remove(key);\r\n" + 
						"RequestSpecification httpRequest = RestAssured.given();\n"+
						"httpRequest.formParams(formMap2);\n" ;
				

				if (!req.HeaderParameters.isEmpty()) {

					scriptRequest+="httpRequest.headers(headerMap);\n";

				}
				
				
				if (!req.queryParameters.isEmpty()) {

					scriptRequest+="httpRequest.queryParams(queryMap);\n";

				}

				if (!req.PathParameters.isEmpty()) {

					scriptRequest+="httpRequest.pathParams(pathMap);\n";


				}
			}
			
			
		}

		 if(notrequired==true) {
			
			System.out.println(className.replace("invalidParameterRequest", " ")+"API doesn't have any required parameter");
			return"";
			
		}


		scriptRequest+="\n\n// send request\n";
		scriptRequest+="Response response = httpRequest.request(Method."+req.method+", \""+req.path+"\");\n";
		scriptRequest+="\n";


		scriptValidation+="//response Validation\n"+
				"String responseBody = response.getBody().asString();\n"+
				"System.out.println(\"Response Body is =>  \" + responseBody);\n"+
				"";

		scriptValidation+="assertEquals(response.getStatusCode(), *** );\n";	
		



		script+=scriptVariables+"\n\n"+scriptRequest+"\n\n"+scriptValidation+"		}\n"+"\n\n }\n}";	

		testcaseFileGenerator(className, script);

		return script;

	}

	public String  invalidinputParameters() {


		String className="";
		if (req.operationId !="") {

			className=req.operationId+"InvalidInputParametersRequest";

		} else {

			className=req.method+req.path.replaceAll("/", "")+"InvalidInputParametersRequest";

		}
		className=className.replaceAll(" ", "");


		String scriptRequest="";
		String scriptVariables="";
		String scriptValidation="";
		String script="";

		script+=originalScriptimports +"public class "+className+" {\r\n" + 
				"\r\n" + 
				"	@Test\r\n" + 
				"	public void "+className+"()\r\n" + 
				"	{ \n  ";



		// Declare Permanent variables Declaration
		scriptVariables="// initialize variables with invalid value\n\n";
		scriptVariables+=originalScriptVariables;
		scriptRequest=originalScriptRequest;

		scriptRequest+="RequestSpecification httpRequest = RestAssured.given();\n";


		if (!req.HeaderParameters.isEmpty()) {

			scriptRequest+="httpRequest.headers(headerMap);\n";

		}

		if (!req.queryParameters.isEmpty()) {

			scriptRequest+="httpRequest.queryParams(queryMap);\n";

		}

		if (!req.formParameters.isEmpty()) {

			scriptRequest+="httpRequest.formParams(formMap);\n";

		}

		if (!req.PathParameters.isEmpty()) {

			scriptRequest+="httpRequest.pathParams(pathMap);\n";


		}



		scriptRequest+="\n\n// send request\n";
		scriptRequest+="Response response = httpRequest.request(Method."+req.method+", \""+req.path+"\");\n";


		scriptValidation+="//response Validation\n"+
				"String responseBody = response.getBody().asString();\n"+
				"System.out.println(\"Response Body is =>  \" + responseBody);\n"+
				"";

		scriptValidation+="assertEquals(response.getStatusCode(), *** );\n";		
		




		script+=scriptVariables+"\n\n"+scriptRequest+"\n\n"+scriptValidation+"\n\n }\n}";	

		testcaseFileGenerator(className, script);


		return script;


	}


	private   void testcaseFileGenerator(String name, String script){

		PrintWriter writer;

		try {

			writer = new PrintWriter(destinationPath+name+".java", "UTF-8");
			writer.println(script);	
			writer.close();



		} catch (FileNotFoundException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}



	}


}
