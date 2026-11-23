package controller;

import dao.OrderDAO;
import model.Order;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ShipperController", urlPatterns = {"/shipper/dashboard", "/shipper"})
public class ShipperController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!user.isShipper() && !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                String newStatus = request.getParameter("status");
                String location = "Tuyến giao hàng Quận 5";
                String note = "";
                if ("DELIVERING".equals(newStatus)) {
                    note = "Shipper đã nhận kiện và đang trên đường giao tới địa chỉ nhận";
                } else if ("ARRIVED_DOORSTEP".equals(newStatus)) {
                    note = "Shipper đã đến trước nhà khách, liên hệ giao hàng tận tay";
                } else if ("PAYMENT_DONE".equals(newStatus)) {
                    note = "Khách hàng đã thanh toán đầy đủ tiền hàng (COD/Chuyển khoản)";
                } else if ("DELIVERED".equals(newStatus)) {
                    note = "Khách hàng đã nhận hàng nguyên vẹn và ký nhận thành công";
                } else if ("RETURNED".equals(newStatus)) {
                    note = "Giao hàng không thành công (Khách từ chối nhận hoặc không liên hệ được)";
                }
                boolean ok = orderDAO.updateOrderStatus(orderId, newStatus, user, location, note);
                if (ok) {
                    session.setAttribute("toastSuccess", "Cập nhật đơn #" + orderId + " sang: " + newStatus);
                }
            } catch (Exception e) {
                session.setAttribute("toastError", "Lỗi: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/shipper/dashboard");
            return;
        }

        List<Order> allOrders = orderDAO.getAllOrders();
        List<Order> shipperOrders = new ArrayList<>();
        int countAssigned = 0;   // Đơn mới nhận
        int countDelivering = 0; // Đang đi giao
        int countDoorstep = 0;   // Đã đến nơi
        int countPaid = 0;       // Đã thu tiền
        int countCompleted = 0;  // Đã giao thành công

        if (allOrders != null) {
            for (Order o : allOrders) {
                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                if ("SHIPPER_ASSIGNED".equals(st)) {
                    countAssigned++;
                    shipperOrders.add(o);
                } else if ("DELIVERING".equals(st) || "SHIPPING".equals(st) || "VAN_CHUYEN".equals(st)) {
                    countDelivering++;
                    shipperOrders.add(o);
                } else if ("ARRIVED_DOORSTEP".equals(st)) {
                    countDoorstep++;
                    shipperOrders.add(o);
                } else if ("PAYMENT_DONE".equals(st)) {
                    countPaid++;
                    shipperOrders.add(o);
                } else if ("DELIVERED".equals(st) || "COMPLETED".equals(st) || "DA_GIAO".equals(st)) {
                    countCompleted++;
                    shipperOrders.add(o);
                }
            }
        }

        request.setAttribute("shipperOrders", shipperOrders);
        request.setAttribute("countAssigned", countAssigned);
        request.setAttribute("countDelivering", countDelivering);
        request.setAttribute("countDoorstep", countDoorstep);
        request.setAttribute("countPaid", countPaid);
        request.setAttribute("countCompleted", countCompleted);

        request.getRequestDispatcher("/shipper-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
