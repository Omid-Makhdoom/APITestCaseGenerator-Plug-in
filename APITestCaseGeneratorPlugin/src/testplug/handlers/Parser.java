package testplug.handlers;
import java.util.ArrayList;
import java.util.Map;

import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.refs.RefFormat;
import io.swagger.parser.SwaggerParser;

public class Parser {

	public  ArrayList<HttpRequest>  pares(String swaggerpath) {

		String BaseUrl = "";
		String basePath = "";	
		String Schema = "";
		
		ArrayList<HttpRequest> reqList = new ArrayList<HttpRequest>();

		// OpenAPI openAPI= new OpenAPIV3Parser().read("D:\\IDE\\Eclps
		// wrkplc\\APITestCaseGenerator\\src\\main\\resources\\swagger.json");
		//"D:\\IDE\\Eclps wrkplc\\APITestCaseGenerator\\src\\main\\resources\\swaggerPOD.json"
		// OpenAPI openAPI= new
		// OpenAPIV3Parser().read("http://iot.pod.ir/apidocs/api-docs.json");
		Swagger swagger = new SwaggerParser()
				.read(swaggerpath);
		
		BaseUrl = swagger.getHost();
		basePath=swagger.getBasePath();
		HttpRequest req = new HttpRequest();

		for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {

			String Path = entry.getKey();

			Map<HttpMethod, Operation> operationMap = entry.getValue().getOperationMap();

			for (Map.Entry<HttpMethod, Operation> op : operationMap.entrySet()) {

				req = new HttpRequest();
				Schema = "";

				req.setBaseUrl(BaseUrl);
				req.setBasePath(basePath);
				req.setPath(Path);

				req.setMethod(op.getKey().toString());

				req.setSummary(op.getValue().getSummary());
				req.setOperationId(op.getValue().getOperationId());
				
				
				// Get Parameters:
				for (Parameter p : op.getValue().getParameters()) {
					
					
					if(p instanceof HeaderParameter) {

						req.addHeaderParameter((HeaderParameter)p);
					}
					
					else  if(p instanceof QueryParameter) {

						req.addQueryParameter((QueryParameter)p);
					}
					else if(p instanceof FormParameter) {
						req.addFormParameter((FormParameter)p);
					}
					if(p instanceof PathParameter) {

						req.addPathParameter((PathParameter)p);
					}
					
					////BodyParameter-----
					
					
					else if (p instanceof BodyParameter) {
						BodyItem Body= new BodyItem();
						Body.SetName(p.getName());
						Body.setDefinitions(swagger.getDefinitions());
						Model m=((BodyParameter) p).getSchema();
						
						if(m.getClass().getSimpleName().matches("ModelImpl")) {
							
							Body.setModel(m);

						}

						else if(m.getClass().getSimpleName().matches("RefModel")) {
							RefProperty rp = new RefProperty(m.getReference());


							if (rp.getRefFormat().equals(RefFormat.INTERNAL)
									&& swagger.getDefinitions().containsKey(rp.getSimpleRef())) {
								
								 m = swagger.getDefinitions().get(rp.getSimpleRef());
								 Body.setModel(m);
								 

								/*if (m instanceof ArrayModel) {
									ArrayModel arrayModel = (ArrayModel) m;
									// System.out.println(rp.getSimpleRef() + "[]");
									if (arrayModel.getItems() instanceof RefProperty) {
										RefProperty arrayModelRefProp = (RefProperty) arrayModel.getItems();
										// printReference(swagger, arrayModelRefProp);
									}
								}

								if (m.getProperties() != null) {

									for (Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {
										req.addBody(propertyEntry);
										System.out.println(" " + propertyEntry.getKey() + " : " +
												propertyEntry.getValue().getType()+"  R"+propertyEntry.getValue().getRequired()+"class"+propertyEntry.getValue().getClass().getSimpleName());

									}
								}*/
							}
						}
						
						
						
						req.setBody(Body);
					}
					

				}
				
				
				for (Map.Entry<String, Response> response : op.getValue().getResponses().entrySet()) {

					if (response.getKey().startsWith("2")) {
						//Schema += "\n" + response.getKey() + ": " + response.getValue().getDescription();
						req.setResponsestatus(Integer.parseInt(response.getKey()));
						//req.setResponseSchema(responseSchema);
					}
				}
				reqList.add(req);
			}

			/*	System.out.println("Responses:");
				for (Map.Entry<String, Response> response : op.getValue().getResponses().entrySet()) {

					if (response.getKey().startsWith("2")) {
						Schema += "\n" + response.getKey() + ": " + response.getValue().getDescription();

						if (response.getValue().getSchema() instanceof RefProperty) {
							RefProperty rp = (RefProperty) response.getValue().getSchema();
							Schema += "\n" + gettReference(swagger, rp);
						}

						if (response.getValue().getSchema() instanceof ArrayProperty) {
							ArrayProperty ap = (ArrayProperty) response.getValue().getSchema();
							if (ap.getItems() instanceof RefProperty) {
								RefProperty rp = (RefProperty) ap.getItems();
								Schema += "\n" + rp.getSimpleRef() + "[]";
								Schema += "\n" + gettReference(swagger, rp);
							}
						}
					}
				}

				System.out.println(Schema);
				System.out.println("--------------------------------------");*/

			
		}
		
		return reqList;
	}

	/*
	 * for(int i=0;i<reqList.size();i++) {
	 * 
	 * reqList.get(i).printrequest();
	 * System.out.println("--------------------------------------");
	 * System.out.println(Schema);
	 * 
	 * }
	 */



	/*private static String getReference(Swagger swagger, RefProperty rp) {
		String Schema = "";
		if (swagger.getDefinitions().containsKey(rp.getSimpleRef())) {
			Model m = swagger.getDefinitions().get(rp.getSimpleRef());

			if (m instanceof ArrayModel) {
				ArrayModel arrayModel = (ArrayModel) m;
				System.out.println(rp.getSimpleRef() + "[]");
				if (arrayModel.getItems() instanceof RefProperty) {
					RefProperty arrayModelRefProp = (RefProperty) arrayModel.getItems();
					getReference(swagger, arrayModelRefProp);
				}
			}

			if (m.getProperties() != null) {
				for (Map.Entry<String, Property> propertyEntry : m.getProperties().entrySet()) {
					Schema += "\n" + ("  " + propertyEntry.getKey() + " : " + propertyEntry.getValue().getType());
				}
			}
		}

		return Schema;
	}*/

}
