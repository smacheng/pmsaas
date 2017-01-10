package ch.zhaw.init.walj.projectmanagement.edit;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.zhaw.init.walj.projectmanagement.util.DBConnection;
import ch.zhaw.init.walj.projectmanagement.util.DateFormatter;
import ch.zhaw.init.walj.projectmanagement.util.HTMLFooter;
import ch.zhaw.init.walj.projectmanagement.util.HTMLHeader;
import ch.zhaw.init.walj.projectmanagement.util.dbclasses.Project;

/**
 * Projectmanagement tool, Page to edit workpackages
 * 
 * @author Janine Walther, ZHAW
 * 
 */
@SuppressWarnings("serial")
@WebServlet("/Projects/EditWorkpackage")
public class EditWorkpackage extends HttpServlet {
	
	// create a new DB connection
	private DBConnection con;
		
	/*
	 * 	method to handle post requests
	 * 	makes changes in database
	 * 	returns error/success message
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		con = new DBConnection(this.getServletContext().getRealPath("/"));
		
		// prepare response
		response.setContentType("text/html;charset=UTF8");
		PrintWriter out = response.getWriter();
		
		// get parameters and user ID
		int workpackageID = Integer.parseInt(request.getParameter("id"));
		int projectID = Integer.parseInt(request.getParameter("projectID"));
		String name = request.getParameter("name");
		String start = request.getParameter("start");
		String end = request.getParameter("end");
		int userID = (int) request.getSession(false).getAttribute("ID");
		
		// get project
		Project project = null;
		try {
			project = con.getProject(projectID);
		} catch (SQLException e) {
			String url = request.getContextPath() + "/ProjectNotFound";
            response.sendRedirect(url);
            return;
		}

		// check if user is project leader 	
		if (project.getLeader() == userID) {
			
			String message = "";
			try {
				// update workpackage
				con.updateWorkpackage(workpackageID, name, DateFormatter.getInstance().formatDateForDB(start), DateFormatter.getInstance().formatDateForDB(end));
				
				// success message
				message = "<div class=\"callout success\">"
						+ "<h5>Workpackage successfully updated</h5>"
						+ "<p>The new workpackage has succsessfully been updated with the following data:</p>"
						+ "<p>Name: " + name + "</p>"
						+ "<p>Duration: " + start + " - " + end + "</p>"
						+ "<a href=\"/Projektverwaltung/Projects/Edit?projectID=" + projectID + "#workpackages\">Click here to go back to the edit page</a>"
						+ "<br>"
						+ "<a href=\"/Projektverwaltung/Projects/Overview/Project?id=" + projectID + "\">Click here to go to the project overview</a>"
						+ "</div>";
			} catch (SQLException e) {
				// error message
				message = "<div class=\"callout alert\">"
					    + "<h5>Workpackage could not be updated</h5>"
					    + "<p>An error occured and the workpackage could not be updated.</p>"
						+ "<a href=\"/Projektverwaltung/Projects/Edit?projectID=" + projectID + "\">Click here to go back to the edit page</a>"
						+ "<br>"
						+ "<a href=\"/Projektverwaltung/Projects/Overview/Project?id=" + projectID + "\">Click here to go to the project overview</a>"
						+ "</div>";
			}
							
			// print HTML
			out.println(HTMLHeader.getInstance().printHeader("Edit Workpackage", "../", name, "") 
					  + "<section>"
					  + "<div class=\"row\">"
					  + message
					  + "</div>"
					  + "</section>"
					  + HTMLFooter.getInstance().printFooter(false)
					  // required JavaScript
					  + "<script src=\"../js/vendor/jquery.js\"></script>"
					  + "<script src=\"../js/vendor/foundation.min.js\"></script>"
					  + "<script>$(document).foundation();</script>"
					  + "</body>"
					  + "</html>");
		}
	}
}
