package controller;

import dao.StoreDAO;
import model.Store;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StoreListController", urlPatterns = {"/stores"})
public class StoreListController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        StoreDAO storeDAO = new StoreDAO();
        List<Store> stores = storeDAO.getAllStores();

        request.setAttribute("stores", stores);
        request.getRequestDispatcher("stores.jsp").forward(request, response);
    }
}
