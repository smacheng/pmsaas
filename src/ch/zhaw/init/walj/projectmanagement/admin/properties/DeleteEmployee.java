/*
 	Copyright 2016-2017 Zuercher Hochschule fuer Angewandte Wissenschaften
 	All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License"); you may
   not use this file except in compliance with the License. You may obtain
   a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
   License for the specific language governing permissions and limitations
   under the License.
 */

package ch.zhaw.init.walj.projectmanagement.admin.properties;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.zhaw.init.walj.projectmanagement.util.DBConnection;
import ch.zhaw.init.walj.projectmanagement.util.HTMLFooter;
import ch.zhaw.init.walj.projectmanagement.util.HTMLHeader;
import ch.zhaw.init.walj.projectmanagement.util.dbclasses.Employee;
import ch.zhaw.init.walj.projectmanagement.util.password.PasswordService;

/**
 * project management tool, delete employee
 * 
 * @author Janine Walther, ZHAW
 */
@SuppressWarnings("serial")
@WebServlet("/admin/deleteEmployee")
public class DeleteEmployee extends HttpServlet {

	// Database connection
	private DBConnection con;

	/*
	 * method to handle get requests
	 * deletes the employee
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		con = new DBConnection(this.getServletContext().getRealPath("/"));

		// set response content type to HTML
		response.setContentType("text/html;charset=UTF8");
		// get print writer
		PrintWriter out = response.getWriter();

		// get user id
		int id = Integer.parseInt(request.getParameter("id"));

		// get employee
		// check if the employee has any assignments, projects or expenses
		Employee employee = null;
		boolean assignments = false;
		boolean projects = false;
		boolean expenses = false;
		try {
			employee = con.getEmployee(id);
			if (!con.getAssignedTasks(id).isEmpty()) {
				assignments = true;
			}

			if (con.getProjects(id, true) != null) {
				projects = true;
			}

			if (con.getProjects(id, false) != null) {
				projects = true;
			}

			if (!con.getExpenses(id).isEmpty()) {
				expenses = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// print html
		assert employee != null;
		out.println(HTMLHeader.getInstance().printHeader("Delete " + employee.getName(), "../", "Delete " + employee.getName(), "", "", true)
				+ "<section>"
				+ "<div class=\"row\">");

		String message;
		if (!(assignments || projects || expenses)) {
			try {
				con.deleteEmployee(id);
				message = "<div class=\"callout success\">"
						+ "<h5>Employee successfully deleted</h5>"
						+ "<p>The user " + employee.getName() + " has succsessfully been deleted</p>"
						+ "<a href=\"/admin/properties\">Click here to go back to the properties page</a>"
						+ "</div>";
			} catch (SQLException e) {
				message = "<div class=\"callout alert\">"
						+ "<h5>User could not be deleted</h5>"
						+ "<p>An error occured and the user could not be deleted.</p>"
						+ "<a href=\"/admin/properties\">Click here to go back to the properties page</a>"
						+ "</div>";
			}
		} else {
			message = "<div class=\"callout alert\">"
					+ "<h5>User could not be deleted</h5>"
					+ "<p>The user could not be deleted because of the following reason(s):</p>";

			if (assignments) {
				message += "<p>- The user has at least one assignment to a task.</p>";
			}
			if (projects) {
				message += "<p>- The user is leader of at least one project.</p>";
			}
			if (expenses) {
				message += "<p>- The user has at least one expense in a project.</p>";
			}

			message += "<a href=\"/admin/properties\">Click here to go back to the properties page</a>"
					+ "</div>";
		}

		out.println(message
				+ "</div>"
				+ "</section>"
				+ HTMLFooter.getInstance().printFooter(false)
				+ "</div>"
				+ "<script src=\"../js/vendor/jquery.js\"></script>"
				+ "<script src=\"../js/vendor/foundation.min.js\"></script>"
				+ "<script>$(document).foundation();</script>"
				+ "</body>"
				+ "</html>");
	}
}