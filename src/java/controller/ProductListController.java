package controller;

import dao.CategoryDAO;
import dao.ProductDAO;
import model.Category;
import model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ProductListController", urlPatterns = {"/products"})
public class ProductListController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        ProductDAO productDAO = new ProductDAO();
        CategoryDAO categoryDAO = new CategoryDAO();

        String categoryIdRaw = request.getParameter("categoryId");
        String keyword = request.getParameter("keyword");
        String sort = request.getParameter("sort");
        String filter = request.getParameter("filter");
        String size = request.getParameter("size");
        String color = request.getParameter("color");
        String priceRange = request.getParameter("priceRange");

        List<Product> products;

        if (categoryIdRaw != null && !categoryIdRaw.isEmpty()) {
            int catId = Integer.parseInt(categoryIdRaw);
            products = productDAO.getProductsByCategoryId(catId);
            Category cat = categoryDAO.getCategoryById(catId);
            request.setAttribute("currentCategory", cat);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            products = productDAO.searchProductsByName(keyword.trim());
            request.setAttribute("keyword", keyword);
        } else if ("sale".equalsIgnoreCase(filter)) {
            List<Product> all = productDAO.getAllProducts();
            products = new ArrayList<>();
            for (Product p : all) {
                if (p.getSalePrice() > 0 && p.getSalePrice() < p.getPrice()) {
                    products.add(p);
                }
            }
        } else {
            products = productDAO.getAllProducts();
        }

        // Apply Size Filter
        if (size != null && !size.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getSize() != null && p.getSize().toUpperCase().contains(size.toUpperCase())) {
                    filtered.add(p);
                }
            }
            products = filtered;
            request.setAttribute("selectedSize", size);
        }

        // Apply Color Filter
        if (color != null && !color.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getColor() != null && p.getColor().toLowerCase().contains(color.toLowerCase())) {
                    filtered.add(p);
                }
            }
            products = filtered;
            request.setAttribute("selectedColor", color);
        }

        // Apply Price Range Filter
        if (priceRange != null && !priceRange.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                double effPrice = p.getEffectivePrice();
                if ("under_500k".equals(priceRange) && effPrice < 500000) {
                    filtered.add(p);
                } else if ("500k_1m".equals(priceRange) && effPrice >= 500000 && effPrice <= 1000000) {
                    filtered.add(p);
                } else if ("1m_2m".equals(priceRange) && effPrice > 1000000 && effPrice <= 2000000) {
                    filtered.add(p);
                } else if ("2m_3m".equals(priceRange) && effPrice > 2000000 && effPrice <= 3000000) {
                    filtered.add(p);
                } else if ("above_3m".equals(priceRange) && effPrice > 3000000) {
                    filtered.add(p);
                }
            }
            products = filtered;
            request.setAttribute("selectedPriceRange", priceRange);
        }

        // Apply Sorting
        if ("price_asc".equals(sort)) {
            products.sort(Comparator.comparingDouble(Product::getEffectivePrice));
        } else if ("price_desc".equals(sort)) {
            products.sort(Comparator.comparingDouble(Product::getEffectivePrice).reversed());
        } else if ("new".equals(sort)) {
            products.sort(Comparator.comparingInt(Product::getId).reversed());
        } else if ("featured".equals(sort)) {
            List<Product> featured = new ArrayList<>();
            for (Product p : products) {
                if (p.isIsFeatured()) featured.add(p);
            }
            if (!featured.isEmpty()) products = featured;
        }

        List<Category> categories = categoryDAO.getAllCategories();
        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("sort", sort);

        request.getRequestDispatcher("shop.jsp").forward(request, response);
    }
}
