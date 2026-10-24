package controller;

import dao.ProductDAO;
import model.CartItem;
import model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CartController", urlPatterns = {"/cart"})
public class CartController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        double totalMoney = 0;
        int totalCount = 0;
        for (CartItem item : cart) {
            totalMoney += item.getTotalMoney();
            totalCount += item.getQuantity();
        }

        session.setAttribute("cartTotal", totalMoney);
        session.setAttribute("cartSize", totalCount);
        request.setAttribute("cart", cart);
        request.setAttribute("totalMoney", totalMoney);
        request.getRequestDispatcher("cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
        }

        ProductDAO productDAO = new ProductDAO();

        if ("add".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String size = request.getParameter("size");
            String color = request.getParameter("color");

            Product p = productDAO.getProductById(productId);
            if (p != null) {
                boolean found = false;
                CartItem addedItem = null;
                for (CartItem item : cart) {
                    if (item.getProduct().getId() == productId 
                        && item.getSize().equalsIgnoreCase(size) 
                        && item.getColor().equalsIgnoreCase(color)) {
                        item.setQuantity(item.getQuantity() + quantity);
                        found = true;
                        addedItem = item;
                        break;
                    }
                }
                if (!found) {
                    addedItem = new CartItem(p, quantity, size, color);
                    cart.add(addedItem);
                }

                // Update session totals
                double total = 0;
                int totalCount = 0;
                for (CartItem ci : cart) {
                    total += ci.getTotalMoney();
                    totalCount += ci.getQuantity();
                }

                session.setAttribute("addedToCart", true);
                session.setAttribute("lastAddedItem", addedItem);
                session.setAttribute("cartSize", totalCount);
                session.setAttribute("cartTotal", total);
            }
            session.setAttribute("cart", cart);
            response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId);
            return;

        } else if ("update".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String size = request.getParameter("size");

            for (CartItem item : cart) {
                if (item.getProduct().getId() == productId && item.getSize().equalsIgnoreCase(size)) {
                    if (quantity <= 0) {
                        cart.remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                    break;
                }
            }
        } else if ("remove".equals(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            String size = request.getParameter("size");

            cart.removeIf(item -> item.getProduct().getId() == productId && item.getSize().equalsIgnoreCase(size));
        }

        double total = 0;
        int totalCount = 0;
        for (CartItem ci : cart) {
            total += ci.getTotalMoney();
            totalCount += ci.getQuantity();
        }

        session.setAttribute("cart", cart);
        session.setAttribute("cartTotal", total);
        session.setAttribute("cartSize", totalCount);
        
        // If request is AJAX, respond with OK status
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
