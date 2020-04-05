package testplug.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

public class ProjectGenerator {

		
		public  ProjectGenerator(String ProjectName,String SwaggerPath) throws CoreException {
			
			 IProgressMonitor progressMonitor = new NullProgressMonitor();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(ProjectName);
			if (!project.exists()) {
			    project.create(progressMonitor);
			    project.open(progressMonitor);
			}
			
			// Configure the project to be a Java project and a maven project
			
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] {"org.eclipse.jdt.core.javanature" ,"org.eclipse.m2e.core.maven2Nature" });

			project.setDescription(description, progressMonitor);
			
			// src
			IFolder src = project.getFolder("src");
			src.create(true, true, progressMonitor);

			// src/main
			IFolder srcMain = src.getFolder("main");
			srcMain.create(true, true, progressMonitor);

			// src/main/java
			IFolder srcMainJava = srcMain.getFolder("java");
			srcMainJava.create(true, true, progressMonitor);
			
			// src/main/resources
			IFolder srcMainResources = srcMain.getFolder("resources");
			srcMainResources.create(true, true, progressMonitor);

			
			// src/test
			IFolder srcTest = src.getFolder("test");
			srcTest.create(true, true, progressMonitor);

			// src/test/java
			IFolder srcTestJava = srcTest.getFolder("java");
			srcTestJava.create(true, true, progressMonitor);
						
			// src/test/resources
			IFolder srcTestResources = srcTest.getFolder("resources");
			srcTestResources.create(true, true, progressMonitor);

			// Let's create our target/classes output folder
			IFolder target = project.getFolder("target");
			target.create(true, true, progressMonitor);

			IFolder classes = target.getFolder("classes");
			classes.create(true, true, progressMonitor);
						
			IFolder testclasses = target.getFolder("test-classes");
			testclasses.create(true, true, progressMonitor);
			
			
			IJavaProject javaProject = JavaCore.create(project);
			
			
			// Let's add target/classes as our output folder for compiled ".class"
			javaProject.setOutputLocation(classes.getFullPath(), progressMonitor);
						
						
						
			// Let's add JavaSE-1.8 to our classpath
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime.getExecutionEnvironmentsManager();
			IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
			for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
			    // We will look for JavaSE-1.8 as the JRE container to add to our classpath
			    if ("JavaSE-1.8".equals(iExecutionEnvironment.getId())) {
			        entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
			        break;
			    }
			}

			// Let's add the maven container to our classpath to let the maven plug-in add the dependencies computed from a pom.xml file to our classpath
			IClasspathEntry mavenEntry = JavaCore.newContainerEntry(new Path("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER"), new IAccessRule[0], new IClasspathAttribute[] {JavaCore.newClasspathAttribute("maven.pomderived", "true") }, false);
			entries.add(mavenEntry);


			
			// Now let's add our source folder  our classpath
			
			IPackageFragmentRoot packageRoot = javaProject.getPackageFragmentRoot(srcMainJava);
			IClasspathAttribute[] extraAttributes=new IClasspathAttribute[] {JavaCore.newClasspathAttribute("optional", "true") ,JavaCore.newClasspathAttribute("maven.pomderived", "true") };
			IClasspathEntry sourceEntry = JavaCore.newSourceEntry(packageRoot.getPath(),  new Path[] {}, new Path[] {}, classes.getFullPath(), extraAttributes);
			entries.add(sourceEntry);
			
			
			packageRoot = javaProject.getPackageFragmentRoot(srcTestJava);
			extraAttributes=new IClasspathAttribute[] {JavaCore.newClasspathAttribute("optional", "true") ,JavaCore.newClasspathAttribute("maven.pomderived", "true"),JavaCore.newClasspathAttribute("test", "true") };
			IClasspathEntry sourceEntry2 = JavaCore.newSourceEntry(packageRoot.getPath(),  new Path[] {}, new Path[] {}, testclasses.getFullPath(), extraAttributes);
			entries.add(sourceEntry2);
			
			
			packageRoot = javaProject.getPackageFragmentRoot(srcMainResources);
			extraAttributes=new IClasspathAttribute[] {JavaCore.newClasspathAttribute("maven.pomderived", "true") };
			IClasspathEntry sourceEntry3 = JavaCore.newSourceEntry(packageRoot.getPath(),  new Path[] {}, new Path[] {new Path("**")}, classes.getFullPath(), extraAttributes);
			entries.add(sourceEntry3);
									
			packageRoot = javaProject.getPackageFragmentRoot(srcTestResources);
			extraAttributes=new IClasspathAttribute[] {JavaCore.newClasspathAttribute("maven.pomderived", "true"),JavaCore.newClasspathAttribute("test", "true") };
			IClasspathEntry sourceEntry4 = JavaCore.newSourceEntry(packageRoot.getPath(),  new Path[] {}, new Path[] {new Path("**")}, testclasses.getFullPath(), extraAttributes);
			entries.add(sourceEntry4);
			
			
			createPOMFile(project);

			
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

			
			javaProject.close();
			
			project.close(progressMonitor);
			
			
			///generate TestCases and add to project
			
			Parser parser=new Parser();

			String destinationPath=project.getLocation().toString()+"\\src\\main\\java\\";

			ArrayList<HttpRequest> reqList =  parser.pares(SwaggerPath);
			for(HttpRequest req: reqList) {
							
				RequestGenerator reqgenerator=new RequestGenerator(req,destinationPath);
						System.out.println(reqgenerator.validRequest()+"\n-----------------------\n"+
						reqgenerator.invalidinputParameters()+"\n-----------------------\n"+
						reqgenerator.invalidHeaderRequests()+"\n-----------------------\n"+
						reqgenerator.invalidParametersRequests()+"\n-----------------------\n"	);
			
				
			}
			
			project.open(progressMonitor);
			project.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);


			
			
		}
	

		
		private static void createPOMFile(final IProject project) throws CoreException {
			
			
			String s=project.getFullPath().toString()+"/pom.xml";

			Model model = new Model(); 
			Writer writer = null;
			try {

				writer = new FileWriter(project.getLocation().toString()+"\\pom.xml");
			
			} catch (IOException e1) {
				e1.printStackTrace();
			}  
			
			
			List<Dependency> dependencyList = new ArrayList<Dependency>();  
			model.setModelVersion("4.0.0");
			model.setGroupId( "TestGroupArtifactID" );    
			model.setArtifactId(project.getName());    
			model.setVersion("1.0.0");    

			Dependency dep = new Dependency();    
			dep.setGroupId("org.testng");    
			dep.setArtifactId("testng");    
			dep.setVersion("7.1.0");
			dependencyList.add(dep);

			Dependency dep2 = new Dependency();    
			dep2.setGroupId("io.rest-assured");    
			dep2.setArtifactId("rest-assured");   
			dep2.setVersion("4.2.0");     
			dependencyList.add(dep2);       

			model.setDependencies(dependencyList);    
			try {
				new MavenXpp3Writer().write(writer, model );
				writer.close();

			} catch (IOException e) {
				System.out.println("MavenXpp3Writer");
				e.printStackTrace();
			}    
			
		
		  }

		
	
}
