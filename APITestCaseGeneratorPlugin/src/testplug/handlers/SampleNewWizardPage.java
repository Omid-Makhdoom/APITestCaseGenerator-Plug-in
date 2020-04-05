package testplug.handlers;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class SampleNewWizardPage extends WizardPage {
	private Text SwaggerText;

	private Text ProjectText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public SampleNewWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("API Test Generator");
		setDescription("This wizard creates a new API Test Project from Swagger.");
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Swagger file URL or Path:");

		SwaggerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		SwaggerText.setLayoutData(gd);
		SwaggerText.addModifyListener(e -> dialogChanged());

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("&Project Name:");

		ProjectText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		ProjectText.setLayoutData(gd);
		ProjectText.addModifyListener(e -> dialogChanged());
		//initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				SwaggerText.setText(container.getFullPath().toString());
			}
		}
		ProjectText.setText("new_file.mpe");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		
	FileDialog dialog = new FileDialog(getShell(), SWT.NULL);
	dialog.setFilterExtensions(new String[]{"*.yaml;*.json", "*.*"});
    String path = dialog.open();
    if (path != null) {

      File file = new File(path);
      if (file.isFile())
        displayFiles(new String[] { file.toString()});
      else
        displayFiles(file.list());

    }
	}
	
	public void displayFiles(String[] files) {
		  for (int i = 0; files != null && i < files.length; i++) {
			  SwaggerText.setText(files[i]);
			  SwaggerText.setEditable(true);
		  }
		}
	
	

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		String ProjectName = getProjectName();
		String SwaggerPath = getSwaggerPath();


		if (SwaggerPath.length() == 0) {
			updateStatus("swagger URL or Path must be specified");
			return;
		}
		
		if (ProjectName.length() == 0) {
			updateStatus("Project name must be specified");
			return;
		}
		
		String[] INVALID_CHARACTERS = new String[] {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
		for(String invalidChar:INVALID_CHARACTERS) {
			if (ProjectName.contains(invalidChar)) {
				updateStatus( invalidChar+" is an invalid Character,Project name must be valid");
				return;
			}
		}
		
		updateStatus(null);

		}
		

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getSwaggerPath() {
		return SwaggerText.getText();
	}

	public String getProjectName() {
		return ProjectText.getText();
	}
}