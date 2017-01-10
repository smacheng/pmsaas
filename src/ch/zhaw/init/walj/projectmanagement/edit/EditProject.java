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
 * Projectmanagement tool, Page to edit project
 * 
 * @author Janine Walther, ZHAW
 * 
 */
@SuppressWarnings("serial")
@WebServlet("/Projects/EditProject")
public class EditProject extends HttpServlet {
	
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
		
		// get the parameters and user ID
		int projectID = Integer.parseInt(request.getParameter("id"));
		String name = request.getParameter("name");
		String shortname = request.getParameter("shortname");
		double budget = Double.parseDouble(request.getParameter("budget"));
		String currency = request.getParameter("currency");
		String start = request.getParameter("start");
		String end = request.getParameter("end");
		String partners = request.getParameter("partner");
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
				// update project
				con.updateProject(projectID, name, shortname, budget, currency, DateFormatter.getInstance().formatDateForDB(start), DateFormatter.getInstance().formatDateForDB(end), partners);
				
				// success message
				message = "<div class=\"callout success\">"
						+ "<h5>Project successfully updated</h5>"
						+ "<p>The new project has succsessfully been updated with the following data:</p>"
						+ "<p>Name: " + name + "</p>"
						+ "<p>Shortname: " + shortname + "</p>"
						+ "<p>Budget: " + budget + "</p>"
						+ "<p>Currency: " + currency + ""
						+ "<p>Duration: " + start + " - " + end + "</p>"
						+ "<p>Partners: " + partners + "</p>"
						+ "<a href=\"/Projektverwaltung/Projects/Edit?projectID=" + projectID + "\">Click here to go back to the edit page</a>"
						+ "<br>"
						+ "<a href=\"/Projektverwaltung/Projects/Overview/Project?id=" + projectID + "\">Click here to go to the project overview</a>"
						+ "</div>";
			} catch (SQLException e) {
				
				// error message
				message = "<div class=\"callout alert\">"
					    + "<h5>Project could not be updated</h5>"
					    + "<p>An error occured and the project could not be updated.</p>"
						+ "<a href=\"/Projektverwaltung/Projects/Edit?projectID=" + projectID + "\">Click here to go back to the edit page</a>"
						+ "<br>"
						+ "<a href=\"/Projektverwaltung/Projects/Overview/Project?id=" + projectID + "\">Click here to go to the project overview</a>"
						+ "</div>";
			}
						
			// print HTML
			out.println(HTMLHeader.getInstance().printHeader("Edit " + shortname, "../", "Edit " + shortname, "")
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
		} else {
			String url = request.getContextPath() + "/AccessDenied";
	        response.sendRedirect(url);
		}
	}
}
