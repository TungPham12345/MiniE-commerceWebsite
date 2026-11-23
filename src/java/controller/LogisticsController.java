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

@WebServlet(name = "LogisticsController", urlPatterns = {"/logistics/dashboard", "/logistics"})
public class LogisticsController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!user.isLogistics() && !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                String newStatus = request.getParameter("status");
                String location = "Trung Tâm Điều Phối ĐVVC";
                String note = "";
                if ("CARRIER_RECEIVED".equals(newStatus)) {
                    note = "Bưu tá ĐVVC đã lấy hàng và ký biên bản tiếp nhận";
                } else if ("CARRIER_CHECKED".equals(newStatus)) {
                    note = "Kiểm tra ngoại quan, trọng lượng kiện hàng đạt chuẩn";
                } else if ("IN_TRANSIT".equals(newStatus)) {
                    note = "Xe tải đường dài đang vận chuyển kiện hàng đến bưu cục phát";
                } else if ("ARRIVED_HUB".equals(newStatus)) {
                    note = "Xe đã đến Bưu cục phát Quận 5, bàn giao cho thủ kho";
                }
                boolean ok = orderDAO.updateOrderStatus(orderId, newStatus, user, location, note);
                if (ok) {
                    session.setAttribute("toastSuccess", "Cập nhật đơn #" + orderId + " sang trạng thái: " + newStatus);
                }
            } catch (Exception e) {
                session.setAttribute("toastError", "Lỗi: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/logistics/dashboard");
            return;
        }

        List<Order> allOrders = orderDAO.getAllOrders();
        List<Order> logisticsOrders = new ArrayList<>();
        int countHandover = 0;  // Chờ tiếp nhận
        int countReceived = 0;  // Đã tiếp nhận
        int countChecked = 0;   // Đã kiểm tra
        int countInTransit = 0; // Đang trung chuyển
        int countArrived = 0;   // Đã đến kho phát

        if (allOrders != null) {
            for (Order o : allOrders) {
                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                if ("HANDOVER_PENDING".equals(st)) {
                    countHandover++;
                    logisticsOrders.add(o);
                } else if ("CARRIER_RECEIVED".equals(st)) {
                    countReceived++;
                    logisticsOrders.add(o);
                } else if ("CARRIER_CHECKED".equals(st)) {
                    countChecked++;
                    logisticsOrders.add(o);
                } else if ("IN_TRANSIT".equals(st)) {
                    countInTransit++;
                    logisticsOrders.add(o);
                } else if ("ARRIVED_HUB".equals(st)) {
                    countArrived++;
                    logisticsOrders.add(o);
                }
            }
        }

        request.setAttribute("logisticsOrders", logisticsOrders);
        request.setAttribute("countHandover", countHandover);
        request.setAttribute("countReceived", countReceived);
        request.setAttribute("countChecked", countChecked);
        request.setAttribute("countInTransit", countInTransit);
        request.setAttribute("countArrived", countArrived);

        request.getRequestDispatcher("/logistics-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
