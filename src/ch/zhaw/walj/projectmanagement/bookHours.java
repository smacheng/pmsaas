package ch.zhaw.walj.projectmanagement;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Projectmanagement tool, Page to book hours
 * 
 * @author Janine Walther, ZHAW
 *
 */
@SuppressWarnings("serial")
@WebServlet("/Overview/bookHours")
public class bookHours extends HttpServlet {

	// Database access information
	String url = "jdbc:mysql://localhost:3306/";
	String dbName = "projectmanagement";
	String userName = "Janine";
	String password = "test123";

	// Database connection
	private DBConnection con = new DBConnection(url, dbName, userName, password);

	@Override
	// method to handle get-requests
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF8");

		// get projectID given from parameter
		int projectID = Integer.parseInt(request.getParameter("projectID"));

		ArrayList<Employee> employees = new ArrayList<Employee>();
		try {
			// TODO Get ID from logged-in user
			employees = con.getAllEmployees(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		PrintWriter out = response.getWriter();

		// print HTML
		out.println("<!DOCTYPE html>" 
				  + "<html>" 
				  // HTML head
				  + "<head>" 
				  + "<meta charset=\"UTF-8\">"
				  + "<title>Book Hours</title>"
				  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/foundation.css\" />"
				  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/style.css\" />"
				  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/font-awesome/css/font-awesome.min.css\" />"
				  + "</head>" 
				  // HTML body
				  + "<body>" 
				  + "<div id=\"wrapper\">" 
				  + "<header>" 
				  + "<div class=\"row\">"
				  + "<div class=\"small-8 medium-6 columns\">"
				  // title
				  + "<h1>Book Hours</h1><a href=\"Project?id=" + projectID + "\" class=\"back\">"
				  + "<i class=\"fa fa-chevron-left\" aria-hidden=\"true\"></i> back to Project</a></div>"
				  // menu
				  + "<div class=\"small-12 medium-6 columns\">" 
				  + "<div class=\"float-right menu\">"
				  + "<a href=\"/Projektverwaltung/Overview\" class=\"button\">All Projects</a>"
				  + "<a href=\"../newProject\" class=\"button\">New Project</a>"
				  + "<a href=\"../newEmployee\" class=\"button\">New Employee</a>"
				  + "<a href=\"help\" class=\"button\">Help</a>" 
				  + "<a href=\"logout\" class=\"button\">Logout</a>"
				  + "</div>" 
				  + "</div>" 
				  + "</div>" 
				  + "</header>" 
				  + "<section>");
		
		out.println("<div class=\"row\">" 
				  + "<form method=\"get\" action=\"bookHours/chooseTask\" data-abide novalidate>"

				  // error message (if something's wrong with the form)
				  + "<div data-abide-error class=\"alert callout\" style=\"display: none;\">"
				  + "<p><i class=\"fa fa-exclamation-triangle\"></i> Please choose an employee.</p></div>"

				  + "<h3></h3></br>" // TODO Titel überlegen
				  // project ID
				  + "<input type=\"hidden\" name=\"projectID\" value=\"" + projectID + "\">"

				  // select employee
				  + "<h5 class=\"small-12 medium-2 columns\">Employee</h5>"
				  + "<div class=\"small-12 medium-6 end columns\">" 
				  + "<select name=\"employee\" required>"
				  + "<option></option>");

		// option for every employee
		for (Employee employee : employees) {
			out.println("<option value =\"" + employee.getID() + "\">" + employee.getName() + "</option>");
		}

		out.println("</select></div>" 
				  + "</div>" 
				  + "<div class=\"row\">"
				  + "<button type=\"submit\" class=\"small-3 columns large button float-right create\">Choose Task  <i class=\"fa fa-chevron-right\"></i></button>"
				  + "</div>");

		out.println(
				"</section></div><script src=\"../js/vendor/jquery.js\"></script><script src=\"../js/vendor/foundation.min.js\"></script><script>$(document).foundation();</script></body></html>");

	}

	@Override
	// method to handle post-requests
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF8");

		// variable declaration, get parameters
		int projectID = Integer.parseInt(request.getParameter("projectID"));
		String[] assignment = request.getParameterValues("assignmentID");
		String[] hour = request.getParameterValues("hours");
		String[] month = request.getParameterValues("months");
		ArrayList<Integer> months = new ArrayList<Integer>();
		ArrayList<Double> hours = new ArrayList<Double>();
		ArrayList<Integer> assignments = new ArrayList<Integer>();

		// error message
		String message = "<div class=\"row\">" 
					   + "<div class=\"callout alert\">" 
					   + "<h5>Something went wrong</h5>"
					   + "<p>The hours could not be booked.</p>" 
					   + "</div></div>";

		// get Project
		Project project = null;
		try {
			project = con.getProject(projectID);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// parse the hour strings to double, add them to arraylist
		for (String s : hour) {
			hours.add(Double.parseDouble(s));
		}

		// get number of the month in the project, add month to arraylist
		for (String s : month) {
			DateHelper dh = new DateHelper();
			int monthNbr = dh.getMonthsBetween(project.getStart(), s);
			months.add(monthNbr);
		}

		// parse assignment IDs to integer, add them to arraylist
		for (String s : assignment) {
			assignments.add(Integer.parseInt(s));
		}

		// create new bookings in DB
		for (int i = 0; i < hour.length; i++) {
			try {
				con.newBooking(assignments.get(i), months.get(i), hours.get(i));
				// success message
				message = "<div class=\"row\">" 
						+ "<div class=\"callout success\">"
						+ "<h5>Hours successfully booked</h5>" 
						+ "<p>The hours were successfully booked to the project "
						+ project.getName() 
						+ ".</p>" 
						+ "</div></div>";
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// get all employees
		ArrayList<Employee> employees = new ArrayList<Employee>();
		try {
			// TODO Get ID from logged-in user
			employees = con.getAllEmployees(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		PrintWriter out = response.getWriter();

		// print HTML
		out.println("<!DOCTYPE html>" 
				  + "<html>" 
				  // HTML head
				  + "<head>" 
				  + "<meta charset=\"UTF-8\">"
				  + "<title>Book Hours</title>"
				  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/foundation.css\" />"
				  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/style.css\" />"
				  + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/font-awesome/css/font-awesome.min.css\" />"
				  + "</head>" 
				  // HTML body
				  + "<body>" 
				  + "<div id=\"wrapper\">" 
				  + "<header>" 
				  + "<div class=\"row\">"
				  + "<div class=\"small-8 medium-6 columns\">" 
				  // title
				  + "<h1>Book Hours</h1><a href=\"Project?id=" + projectID + "\" class=\"back\">"
				  + "<i class=\"fa fa-chevron-left\" aria-hidden=\"true\"></i> back to Project</a></div>"
				  // menu
				  + "<div class=\"small-12 medium-6 columns\">" 
				  + "<div class=\"float-right menu\">"
				  + "<a href=\"/Projektverwaltung/Overview\" class=\"button\">All Projects</a>"
				  + "<a href=\"../newProject\" class=\"button\">New Project</a>"
				  + "<a href=\"../newEmployee\" class=\"button\">New Employee</a>"
				  + "<a href=\"help\" class=\"button\">Help</a>" 
				  + "<a href=\"logout\" class=\"button\">Logout</a>"
				  + "</div>" 
				  + "</div>" 
				  + "</div>" 
				  + "</header>" 
				  + "<section>" + message);

		// print HTML section with form
		out.println("<div class=\"row\">" 

				  // error message (if something's wrong with the form)
				  + "<form method=\"get\" action=\"bookHours/chooseTask\" data-abide novalidate>"
				  + "<div data-abide-error class=\"alert callout\" style=\"display: none;\">"
				  + "<p><i class=\"fa fa-exclamation-triangle\"></i> Please choose an employee.</p></div>"

				  + "<h3></h3></br>" // TODO Titel überlegen
				  // project ID
				  + "<input type=\"hidden\" name=\"projectID\" value=\"" + projectID + "\">"

				  // select employee
				  + "<h5 class=\"small-12 medium-2 columns\">Employee</h5>"
				  + "<div class=\"small-12 medium-6 end columns\">" 
				  + "<select name=\"employee\" required>"
				  + "<option></option>");

		// option for every employee
		for (Employee employee : employees) {
			out.println("<option value =\"" + employee.getID() + "\">" + employee.getName() + "</option>");
		}

		// submit button
		out.println("</select></div></div>" 
				  + "<div class=\"row\">"
				  + "<button type=\"submit\" class=\"small-3 columns large button float-right create\">Choose Task  <i class=\"fa fa-chevron-right\"></i></button>"
				  + "</div>");

		out.println(
				"</section></div><script src=\"../js/vendor/jquery.js\"></script><script src=\"../js/vendor/foundation.min.js\"></script><script>$(document).foundation();</script></body></html>");

	}
}
