package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import model.Order;
import model.Product;
import model.User;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminDashboardController", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        ProductDAO productDAO = new ProductDAO();
        OrderDAO orderDAO = new OrderDAO();

        List<Product> products = productDAO.getAllProducts();
        List<Order> orders = orderDAO.getAllOrders();

        double totalRevenue = 0;
        int totalOrders = orders.size();
        for (Order o : orders) {
            totalRevenue += o.getTotalMoney();
        }

        request.setAttribute("totalProducts", products.size());
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("recentOrders", orders);

        request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
    }
}
