package controller;

import dao.StoreDAO;
import model.Store;
import model.User;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminStoreController", urlPatterns = {"/admin/stores"})
public class AdminStoreController extends HttpServlet {

    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        StoreDAO storeDAO = new StoreDAO();
        List<Store> stores = storeDAO.getAllStores();

        request.setAttribute("stores", stores);
        request.getRequestDispatcher("/admin-stores.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;

        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        StoreDAO storeDAO = new StoreDAO();

        if ("add".equals(action)) {
            String name = request.getParameter("name");
            String address = request.getParameter("address");
            String city = request.getParameter("city");
            String phone = request.getParameter("phone");
            String hours = request.getParameter("hours");
            double lat = Double.parseDouble(request.getParameter("latitude"));
            double lng = Double.parseDouble(request.getParameter("longitude"));
            boolean isMain = "1".equals(request.getParameter("isMain"));

            Store s = new Store(0, name, address, city, phone, hours, lat, lng, isMain);
            storeDAO.insertStore(s);
        } else if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            storeDAO.deleteStore(id);
        }

        response.sendRedirect(request.getContextPath() + "/admin/stores");
    }
}
