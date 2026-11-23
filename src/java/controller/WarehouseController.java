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

@WebServlet(name = "WarehouseController", urlPatterns = {"/warehouse/dashboard", "/warehouse"})
public class WarehouseController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!user.isWarehouse() && !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            try {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                String newStatus = request.getParameter("status");
                String location = "Kho Tổng Tân Bình";
                String note = "";
                if ("PICKING".equals(newStatus)) {
                    note = "Nhân viên kho bắt đầu lấy hàng từ kệ";
                } else if ("PACKED".equals(newStatus)) {
                    note = "Hàng đã đóng gói cẩn thận và in mã vận đơn";
                } else if ("HANDOVER_PENDING".equals(newStatus)) {
                    note = "Kiện hàng đã tập kết tại cổng xuất kho, chờ ĐVVC lấy";
                }
                boolean ok = orderDAO.updateOrderStatus(orderId, newStatus, user, location, note);
                if (ok) {
                    session.setAttribute("toastSuccess", "Cập nhật đơn #" + orderId + " sang trạng thái: " + newStatus);
                }
            } catch (Exception e) {
                session.setAttribute("toastError", "Lỗi: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/warehouse/dashboard");
            return;
        }

        List<Order> allOrders = orderDAO.getAllOrders();
        List<Order> warehouseOrders = new ArrayList<>();
        int countConfirmed = 0; // Chờ lấy hàng
        int countPicking = 0;   // Đang lấy hàng
        int countPacked = 0;    // Đã đóng gói
        int countHandover = 0;  // Chờ bàn giao ĐVVC

        if (allOrders != null) {
            for (Order o : allOrders) {
                String st = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                if ("CONFIRMED".equals(st) || "CHO_LAY_HANG".equals(st)) {
                    countConfirmed++;
                    warehouseOrders.add(o);
                } else if ("PICKING".equals(st)) {
                    countPicking++;
                    warehouseOrders.add(o);
                } else if ("PACKED".equals(st)) {
                    countPacked++;
                    warehouseOrders.add(o);
                } else if ("HANDOVER_PENDING".equals(st)) {
                    countHandover++;
                    warehouseOrders.add(o);
                }
            }
        }

        request.setAttribute("warehouseOrders", warehouseOrders);
        request.setAttribute("countConfirmed", countConfirmed);
        request.setAttribute("countPicking", countPicking);
        request.setAttribute("countPacked", countPacked);
        request.setAttribute("countHandover", countHandover);

        request.getRequestDispatcher("/warehouse-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
