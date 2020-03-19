//Vraj Desai
//Registration
package com.mvc.controller;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mvc.bean.RegisterBean;
import com.mvc.dao.RegisterDao;

public class RegisterServlet extends HttpServlet {

    public RegisterServlet() {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullname");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String userName = request.getParameter("country");
        String birthday = request.getParameter("birthday");

        RegisterBean registerBean = new RegisterBean();

        registerBean.setFullName(fullName);
        registerBean.setEmail(email);
        registerBean.setPassword(password);
        registerBean.setUserName(country);
        registerBean.setUserName(birthday);

        RegisterDao registerDao = new RegisterDao();

        String userRegistered = registerDao.registerUser(registerBean);
        //Success
        if(userRegistered.equals("SUCCESS"))
        {
            //request.getRequestDispatcher("/Home.jsp").forward(request, response);
        }
        //Fail
        else
        {
            //request.setAttribute("errMessage", userRegistered);
            //request.getRequestDispatcher("/Register.jsp").forward(request, response);
        }
    }
}