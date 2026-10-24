package controller;

import dao.ProductDAO;
import model.Product;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ProductDetailController", urlPatterns = {"/product-detail"})
public class ProductDetailController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String idRaw = request.getParameter("id");
        int productId = 1;
        try {
            if (idRaw != null && !idRaw.isEmpty()) {
                productId = Integer.parseInt(idRaw);
            }
        } catch (NumberFormatException e) {
            productId = 1;
        }

        ProductDAO productDAO = new ProductDAO();
        Product product = productDAO.getProductById(productId);
        List<Product> relatedProducts = productDAO.getProductsByCategoryId(product.getCategoryId());

        request.setAttribute("product", product);
        request.setAttribute("relatedProducts", relatedProducts);
        request.getRequestDispatcher("detail.jsp").forward(request, response);
    }
}
