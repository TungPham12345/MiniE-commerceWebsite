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

        String catIdRaw = request.getParameter("categoryId");
        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter"); 
        String sort = request.getParameter("sort"); 
        String size = request.getParameter("size");
        String color = request.getParameter("color");
        String priceRange = request.getParameter("priceRange");

        List<Product> products = productDAO.getAllProducts();
        Category currentCategory = null;

        if (catIdRaw != null && !catIdRaw.trim().isEmpty()) {
            try {
                int catId = Integer.parseInt(catIdRaw.trim());
                List<Product> catProducts = productDAO.getProductsByCategoryId(catId);
                if (catProducts != null && !catProducts.isEmpty()) {
                    products = catProducts;
                }
                currentCategory = categoryDAO.getCategoryById(catId);
            } catch (Exception e) {}
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            List<Product> searchProducts = productDAO.searchProductsByName(keyword.trim());
            if (searchProducts != null && !searchProducts.isEmpty()) {
                products = searchProducts;
            }
        }

        // Preset Filters
        if ("bestseller".equalsIgnoreCase(filter)) {
            List<Product> featured = productDAO.getFeaturedProducts();
            if (featured != null && !featured.isEmpty()) products = featured;
        } else if ("sale".equalsIgnoreCase(filter)) {
            List<Product> sales = new ArrayList<>();
            for (Product p : products) {
                if (p.getSalePrice() > 0) sales.add(p);
            }
            if (!sales.isEmpty()) products = sales;
        }

        // Price Filter
        if (priceRange != null && !priceRange.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                double ep = p.getEffectivePrice();
                if ("under_500k".equals(priceRange) && ep < 500000) {
                    filtered.add(p);
                } else if ("500k_1m".equals(priceRange) && ep >= 500000 && ep <= 1000000) {
                    filtered.add(p);
                } else if ("1m_2m".equals(priceRange) && ep >= 1000000 && ep <= 2000000) {
                    filtered.add(p);
                } else if ("2m_3m".equals(priceRange) && ep >= 2000000 && ep <= 3000000) {
                    filtered.add(p);
                } else if ("above_3m".equals(priceRange) && ep > 3000000) {
                    filtered.add(p);
                }
            }
            if (!filtered.isEmpty()) products = filtered;
        }

        // Size Filter
        if (size != null && !size.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getSize() != null && p.getSize().toUpperCase().contains(size.toUpperCase())) {
                    filtered.add(p);
                }
            }
            if (!filtered.isEmpty()) products = filtered;
        }

        // Color Filter
        if (color != null && !color.isEmpty()) {
            List<Product> filtered = new ArrayList<>();
            for (Product p : products) {
                if (p.getColor() != null && p.getColor().toLowerCase().contains(color.toLowerCase())) {
                    filtered.add(p);
                }
            }
            if (!filtered.isEmpty()) products = filtered;
        }

        // Sorting
        if ("price_asc".equalsIgnoreCase(sort)) {
            products.sort(Comparator.comparingDouble(Product::getEffectivePrice));
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            products.sort((p1, p2) -> Double.compare(p2.getEffectivePrice(), p1.getEffectivePrice()));
        } else if ("new".equalsIgnoreCase(sort)) {
            products.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
        } else if ("bestseller".equalsIgnoreCase(sort) || "featured".equalsIgnoreCase(sort)) {
            products.sort((p1, p2) -> Boolean.compare(p2.isIsFeatured(), p1.isIsFeatured()));
        }

        // GUARANTEE FALLBACK: NEVER LEAVE PRODUCTS EMPTY!
        if (products == null || products.isEmpty()) {
            products = productDAO.getMockProducts();
        }

        List<Category> categories = categoryDAO.getAllCategories();

        request.setAttribute("products", products);
        request.setAttribute("categories", categories);
        request.setAttribute("currentCategory", currentCategory);
        request.setAttribute("keyword", keyword);
        request.setAttribute("filter", filter);
        request.setAttribute("sort", sort);
        request.setAttribute("selectedSize", size);
        request.setAttribute("selectedColor", color);
        request.setAttribute("selectedPriceRange", priceRange);

        request.getRequestDispatcher("shop.jsp").forward(request, response);
    }
}
