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

@WebServlet(name = "HubController", urlPatterns = {"/hub/dashboard", "/hub"})
public class HubController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!user.isHub() && !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                String newStatus = request.getParameter("status");
                String location = "Bưu Cục Phát Quận 5";
                String note = "";
                if ("HUB_CHECKED".equals(newStatus)) {
                    note = "Bưu cục đã tiếp nhận kiện hàng, kiểm đếm số lượng đầy đủ";
                } else if ("SHIPPER_ASSIGNED".equals(newStatus)) {
                    note = "Đã phân tuyến cho Shipper Nguyễn Văn Giao nhận đơn";
                }
                boolean ok = orderDAO.updateOrderStatus(orderId, newStatus, user, location, note);
                if (ok) {
                    session.setAttribute("toastSuccess", "Cập nhật đơn #" + orderId + " sang trạng thái: " + newStatus);
                }
            } catch (Exception e) {
                session.setAttribute("toastError", "Lỗi: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/hub/dashboard");
            return;
        }

        List<Order> allOrders = orderDAO.getAllOrders();
        List<Order> hubOrders = new ArrayList<>();
        int countArrived = 0;  // Chờ kiểm đếm
        int countChecked = 0;  // Đã kiểm đếm, chờ giao Shipper
        int countAssigned = 0; // Đã giao cho Shipper

        if (allOrders != null) {
            for (Order o : allOrders) {
                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                if ("ARRIVED_HUB".equals(st)) {
                    countArrived++;
                    hubOrders.add(o);
                } else if ("HUB_CHECKED".equals(st)) {
                    countChecked++;
                    hubOrders.add(o);
                } else if ("SHIPPER_ASSIGNED".equals(st)) {
                    countAssigned++;
                    hubOrders.add(o);
                }
            }
        }

        request.setAttribute("hubOrders", hubOrders);
        request.setAttribute("countArrived", countArrived);
        request.setAttribute("countChecked", countChecked);
        request.setAttribute("countAssigned", countAssigned);

        request.getRequestDispatcher("/hub-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
