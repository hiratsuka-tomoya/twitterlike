package chapter6.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import chapter6.beans.User;
import chapter6.exception.NoRowsUpdatedRuntimeException;
import chapter6.service.UserService;

@WebServlet(urlPatterns = { "/settings" })
@MultipartConfig(maxFileSize = 100000)
public class SettingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");

		if (session.getAttribute("editUser") == null) {
			User editUser = new UserService().getUser(loginUser.getId());
			session.setAttribute("editUser", editUser);
		}

		request.getRequestDispatcher("settings.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		List<String> messages = new ArrayList<String>();

		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		User editUser = getEditUser(request);
		session.setAttribute("editUser", editUser);

		if (isValid(request, messages, loginUser) == true) {

			try {
				new UserService().update(editUser);
			} catch (NoRowsUpdatedRuntimeException e) {
				session.removeAttribute("editUser");
				messages.add("他の人によって更新されています。最新のデータを表示しました。データを確認してください。");
				session.setAttribute("errorMessages", messages);
				response.sendRedirect("settings");
			}

			session.setAttribute("loginUser", editUser);
			session.removeAttribute("editUser");

			response.sendRedirect("./");
		} else {
			session.setAttribute("errorMessages", messages);
			response.sendRedirect("settings");
		}
	}

	private User getEditUser(HttpServletRequest request)
			throws IOException, ServletException {

		HttpSession session = request.getSession();
		User editUser = (User) session.getAttribute("editUser");

		editUser.setName(request.getParameter("name"));
		editUser.setAccount(request.getParameter("account"));
		editUser.setPassword(request.getParameter("password"));
		editUser.setEmail(request.getParameter("email"));
		editUser.setDescription(request.getParameter("description"));
//		editUser.setIcon(getIcon(request));
		return editUser;
	}

//	private byte[] getIcon(HttpServletRequest request) throws IOException,
//			ServletException {
//
//		Part part = request.getPart("icon");
//		byte[] icon = null;
//		if (part.getSize() == 0) {
//			return icon;
//		}
//
//		InputStream inputStream = null;
//		try {
//			inputStream = part.getInputStream();
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			StreamUtil.copy(inputStream, baos);
//			icon = baos.toByteArray();
//			return icon;
//		} finally {
//			close(inputStream);
//		}
//	}

	private boolean isValid(HttpServletRequest request, List<String> messages, User loginUser) {

		String account = request.getParameter("account");
		String password = request.getParameter("password");
		String email = request.getParameter("email");

		if (StringUtils.isEmpty(account) == true) {
			messages.add("アカウント名を入力してください");
		} else if (new UserService().getUser(account) != null) {
			if (!account.equals(loginUser.getAccount())) {
				messages.add("そのアカウント名は登録できません");
			}
		}
		if (StringUtils.isEmpty(password) == true) {
			messages.add("パスワードを入力してください");
		}
		if (StringUtils.isNotEmpty(email) == true) {
			if (email.matches("\\w+@\\w+") == false) {
				messages.add("メールアドレスの形式が不正です");
			} else if (new UserService().getUser(email) != null) {
				if (!email.equals(loginUser.getEmail())) {
					messages.add("そのメールアドレスは登録できません");
				}
			}
		}

		if (messages.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

}
