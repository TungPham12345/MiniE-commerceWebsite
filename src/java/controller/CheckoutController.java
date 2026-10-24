package controller;

import dao.OrderDAO;
import model.CartItem;
import model.Order;
import model.OrderDetail;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
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
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        double totalMoney = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalMoney();
        }

        request.setAttribute("cart", cart);
        request.setAttribute("totalMoney", totalMoney);
        request.getRequestDispatcher("checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        User user = (User) session.getAttribute("user");

        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        String fullname = request.getParameter("fullname");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String note = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");

        double totalMoney = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalMoney();
        }

        Order order = new Order();
        if (user != null) {
            order.setUserId(user.getId());
        }
        order.setCustomerName(fullname);
        order.setEmail(email);
        order.setPhone(phone);
        order.setAddress(address);
        order.setNote(note);
        order.setTotalMoney(totalMoney);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
        order.setStatus("PENDING");

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem ci : cart) {
            OrderDetail od = new OrderDetail();
            od.setProductId(ci.getProduct().getId());
            od.setPrice(ci.getProduct().getEffectivePrice());
            od.setQuantity(ci.getQuantity());
            od.setSize(ci.getSize());
            od.setColor(ci.getColor());
            od.setTotalMoney(ci.getTotalMoney());
            orderDetails.add(od);
        }

        OrderDAO orderDAO = new OrderDAO();
        boolean success = orderDAO.createOrder(order, orderDetails);

        if (success) {
            session.removeAttribute("cart");
            request.setAttribute("orderSuccess", true);
            request.setAttribute("orderId", order.getId());
            request.getRequestDispatcher("order-success.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Có lỗi xảy ra khi xử lý đơn hàng. Vui lòng thử lại!");
            doGet(request, response);
        }
    }
}
