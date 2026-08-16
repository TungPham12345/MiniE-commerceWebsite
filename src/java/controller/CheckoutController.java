package controller;

import dao.OrderDAO;
import model.CartItem;
import model.Order;
import model.User;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CheckoutController", urlPatterns = {"/checkout"})
public class CheckoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            response.sendRedirect("cart");
            return;
        }

        request.getRequestDispatcher("checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        Double cartTotal = (Double) session.getAttribute("cartTotal");
        User user = (User) session.getAttribute("user");

        if (cart == null || cart.isEmpty()) {
            response.sendRedirect("cart");
            return;
        }

        String customerName = request.getParameter("customerName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String note = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");

        Order order = new Order();
        if (user != null) {
            order.setUserId(user.getId());
        }
        order.setCustomerName(customerName);
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalMoney(cartTotal != null ? cartTotal : 0);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");

        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.createOrder(order, cart);

        if (orderId > 0) {
            session.removeAttribute("cart");
            session.removeAttribute("cartTotal");
            session.removeAttribute("cartSize");

            request.setAttribute("orderId", orderId);
            request.setAttribute("successMessage", "🎉 Đặt hàng thành công! Mã đơn hàng của bạn là #" + orderId);
            request.getRequestDispatcher("order-success.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "❌ Đặt hàng thất bại. Vui lòng thử lại!");
            request.getRequestDispatcher("checkout.jsp").forward(request, response);
        }
    }
}
