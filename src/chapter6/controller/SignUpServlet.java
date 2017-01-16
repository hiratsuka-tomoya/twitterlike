package chapter6.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import chapter6.beans.User;
import chapter6.service.UserService;

@WebServlet(urlPatterns = { "/signup" })
public class SignUpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		request.getRequestDispatcher("signup.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		List<String> messages = new ArrayList<String>();

		HttpSession session = request.getSession();
		session.removeAttribute("account");
		session.removeAttribute("email");
		if (isValid(request, messages) == true) {

			User user = new User();
			user.setName(request.getParameter("name"));
			user.setAccount(request.getParameter("account"));
			user.setPassword(request.getParameter("password"));
			user.setEmail(request.getParameter("email"));
			user.setDescription(request.getParameter("description"));

			new UserService().register(user);

			response.sendRedirect("./");
		} else {
			session.setAttribute("errorMessages", messages);
			session.setAttribute("name", request.getParameter("name"));
			response.sendRedirect("signup");
		}
	}

	private boolean isValid(HttpServletRequest request, List<String> messages) {
		HttpSession session = request.getSession();
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		String email = request.getParameter("email");

		session.removeAttribute("account");
		session.removeAttribute("email");

		if (StringUtils.isEmpty(account) == true) {
			messages.add("アカウント名を入力してください");
		} else if (new UserService().getUser(account) != null) {
			messages.add("そのアカウント名は登録できません");
		} else {
			session.setAttribute("account", account);
		}
		if (StringUtils.isEmpty(password) == true) {
			messages.add("パスワードを入力してください");
		}
		if (StringUtils.isNotEmpty(email) == true) {
			if (email.matches("\\w+@\\w+") == false) {
				messages.add("メールアドレスの形式が不正です");
			} else if (new UserService().getUser(email) != null) {
				messages.add("そのメールアドレスは登録できません");
			}
			else {
				session.setAttribute("email", email);
			}
		}

		if (messages.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

}
