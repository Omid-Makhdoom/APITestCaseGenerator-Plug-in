package testplug.handlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.properties.Property;


public class HttpRequest {
	
	
	
	String baseUrl="";
	String basePath="";
	String path="";
	String method="";
	BodyItem body=null;
	List<QueryParameter> queryParameters=new ArrayList<QueryParameter>();
	List<FormParameter> formParameters=new ArrayList<FormParameter>();
	List<PathParameter> PathParameters=new ArrayList<PathParameter>();
	List<HeaderParameter> HeaderParameters=new ArrayList<HeaderParameter>();


	String summary="";
	String operationId="";

	String responseSchema="";
	int  SuccesResponsestatus=0;
	
	
	
	HttpRequest(){
		
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	public void setOperationId(String operationId) {
		
		if(operationId != null) {
			
			this.operationId=operationId;
			
		    } 
		
	}
	
	
	public void setBody(BodyItem body) {
		this.body = body;
	}
	
	
		
	
	public void addFormParameter(FormParameter parameter) {
		if(parameter == null) {
		       //do something
		    } else {
	    	
				this.formParameters.add(parameter);
				
		    }
	

	}
	
	public void addQueryParameter(QueryParameter parameter) {
		if(parameter == null) {
		       //do something
		    } else {
	    	
				this.queryParameters.add(parameter);
				
		    }
	}
	
		public void addPathParameter(PathParameter parameter) {
			if(parameter == null) {
			       //do something
			    } else {
		    	
					this.PathParameters.add(parameter);
					
			    }
			
	}
	
		
		public void addHeaderParameter(HeaderParameter parameter) {
			if(parameter == null) {
			       //do something
			    } else {
		    	
					this.HeaderParameters.add(parameter);
					
			    }
			
	}	
		
		
		
		
		
		
	public void setResponseSchema(String responseSchema) {
		this.responseSchema = responseSchema;
	}
	public void setResponsestatus(int responsestatus) {
		this.SuccesResponsestatus = responsestatus;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void printrequest(){
		
		System.out.println(baseUrl+basePath+path+"\n"+ method +": \nsummary: "+  summary);
		if (!HeaderParameters.isEmpty()) {
			System.out.println("headers: ");
			for(HeaderParameter header : HeaderParameters) {
				System.out.println(header.getName()+" requeired: "+header.getRequired()+" type: "+ header.getType());
			}
		}

		if (!PathParameters.isEmpty()) {
			System.out.println("Pathes: ");
			for(PathParameter path : PathParameters) {
				System.out.println(path.getName()+" requeired: "+path.getRequired()+" type: "+ path.getType());
			}
		}
		
		if (!formParameters.isEmpty()) {
			System.out.println("formparams: ");
			for(FormParameter formparams : formParameters) {
				System.out.println(formparams.getName()+" requeired: "+formparams.getRequired()+" type: "+ formparams.getType());
			}
		}
		
		if (!queryParameters.isEmpty()) {
			System.out.println("queryparams: ");
			for(QueryParameter queryparams : queryParameters) {
				System.out.println(queryparams.getName()+"  requeired: "+queryparams.getRequired()+"  type: "+ queryparams.getType());
			}
		}

		

	}
}
