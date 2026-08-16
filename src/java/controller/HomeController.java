package controller;

import dao.CategoryDAO;
import dao.ProductDAO;
import model.Category;
import model.Product;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "HomeController", urlPatterns = {"/home", ""})
public class HomeController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        List<Product> featuredProducts = productDAO.getFeaturedProducts();
        List<Product> latestProducts = productDAO.getAllProducts();
        if (latestProducts.size() > 8) {
            latestProducts = latestProducts.subList(0, 8);
        }
        List<Category> categories = categoryDAO.getAllCategories();

        request.setAttribute("featuredProducts", featuredProducts);
        request.setAttribute("latestProducts", latestProducts);
        request.setAttribute("categories", categories);

        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
